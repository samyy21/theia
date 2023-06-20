package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.core.model.request.CacheCardResponseBean;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.CardCenterHelper;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.facade.coft.model.FetchTokenDetailRequest;
import com.paytm.pgplus.facade.coft.model.FetchTokenDetailRequestBody;
import com.paytm.pgplus.facade.coft.model.FetchTokenDetailResponse;
import com.paytm.pgplus.facade.coft.model.GenerateTokenDataRequestHead;
import com.paytm.pgplus.facade.coft.service.ICoftService;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.user.models.response.QueryNonSensitiveAssetInfoResponse;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.accesstoken.model.request.CreateAccessTokenServiceRequest;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.models.ExpressCardTokenRequest;
import com.paytm.pgplus.theia.models.response.ExpressCardTokenResponse;
import com.paytm.pgplus.theia.nativ.service.CoftTokenDataService;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativePersistData;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.services.helper.ExpressPaymentServiceHelper;
import com.paytm.pgplus.theia.validator.service.ExpressPaymentValidation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.UUID;

import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.ENABLE_GCIN_ON_COFT_PROMO;

@Service("expressPaymentService")
public class ExpressPaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressPaymentService.class);

    @Autowired
    ExpressPaymentValidation expressPaymentValidation;

    @Autowired
    @Qualifier("expressTokenGeneratorFlow")
    IWorkFlow expressTokenGeneratorFlow;

    @Autowired
    ExpressPaymentServiceHelper expressPaymentServiceHelper;

    @Autowired
    @Qualifier("bizService")
    protected IBizService bizService;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    @Qualifier("coftTokenDataService")
    private CoftTokenDataService coftTokenDataService;

    @Autowired
    @Qualifier("CoftService")
    private ICoftService coftService;

    @Autowired
    @Qualifier("cardCenterHelper")
    private CardCenterHelper cardCenterHelper;

    public ExpressCardTokenResponse getCardToken(ExpressCardTokenRequest requestData, boolean validateExpiryAndCvv) {
        ExpressCardTokenResponse response = new ExpressCardTokenResponse();
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();

        try {
            response = mandatoryValidations(requestData, response);
            if (StringUtils.isBlank(response.getErrorCode())) {
                boolean isEnableGcinOnCoftPromo = ff4jUtils.isFeatureEnabledOnMid(requestData.getMid(),
                        ENABLE_GCIN_ON_COFT_PROMO, false);
                String merchantCoftConfig = coftTokenDataService.getMerchantConfig(requestData.getMid());

                // If saved card payment with cardIndexNumber then fetch card
                // details first
                if (!isEnableGcinOnCoftPromo) {
                    if (StringUtils.isNotBlank(requestData.getSavedCardId())
                            && requestData.getSavedCardId().length() > 15) {
                        expressPaymentServiceHelper.fetchSavedCardDetailsAndProcessWithCardIndexNumber(requestData,
                                response);
                        if (StringUtils.isNotBlank(response.getErrorCode())) {
                            return response;
                        }
                        businessValidationsWithSavedCardIndexNumber(requestData, response, validateExpiryAndCvv);
                    } else {
                        if (StringUtils.isNotBlank(requestData.getSavedCardId())) {
                            expressPaymentServiceHelper.fetchSavedCardDetailsAndProcess(requestData, response);
                            if (StringUtils.isNotBlank(response.getErrorCode())) {
                                return response;
                            }
                        }
                        response = businessValidations(requestData, response, validateExpiryAndCvv);
                    }
                } else {
                    if (StringUtils.isNotBlank(requestData.getSavedCardId())) {
                        if (requestData.getSavedCardId().length() > 15 && requestData.getSavedCardId().length() < 45) {
                            setBinDetailsForTin(requestData, isEnableGcinOnCoftPromo, merchantCoftConfig, response);
                            return response;
                        } else if (requestData.getSavedCardId().length() > 15) {
                            setBinDetailsForCin(requestData, isEnableGcinOnCoftPromo, merchantCoftConfig, response);
                            businessValidationsWithSavedCardIndexNumber(requestData, response, validateExpiryAndCvv);
                        }
                    } else if (requestData.getCardTokenInfo() != null) {
                        String uniqueCardIdentifier = null;
                        if (merchantCoftConfig.equals("PAR")) {
                            uniqueCardIdentifier = requestData.getCardTokenInfo().getPanUniqueReference();
                        } else if (merchantCoftConfig.equals("GCIN")
                                && StringUtils.isNotBlank(requestData.getCardTokenInfo().getPanUniqueReference())) {
                            uniqueCardIdentifier = coftTokenDataService.getTokenData(requestData.getMid(), requestData
                                    .getCardTokenInfo().getPanUniqueReference(), "PAR", merchantCoftConfig);
                        }
                        if (uniqueCardIdentifier == null) {
                            response.setStatus(ResponseConstants.FAILURE.getMessage());
                            response.setErrorCode(ResponseConstants.INTERNAL_PROCESSING_ERROR.getCode());
                            response.setErrorMessage(ResponseConstants.INTERNAL_PROCESSING_ERROR.getMessage());
                            return response;
                        }
                        setBinDetailsForCoftToken(requestData, isEnableGcinOnCoftPromo, merchantCoftConfig,
                                uniqueCardIdentifier, response);
                        return response;
                    }

                }
                if (StringUtils.isBlank(response.getErrorCode())) {
                    // Map request data to biz request model
                    expressPaymentServiceHelper.mapExpressFlowReqData(requestData, workFlowRequestBean);
                    processBizFlow(requestData, response, workFlowRequestBean, isEnableGcinOnCoftPromo,
                            merchantCoftConfig);

                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception occured", e);
            response.setStatus(ResponseConstants.FAILURE.getMessage());
            response.setErrorCode(ResponseConstants.INTERNAL_PROCESSING_ERROR.getCode());
            response.setErrorMessage(ResponseConstants.INTERNAL_PROCESSING_ERROR.getMessage());
        }

        return response;
    }

    /**
     * @param requestData
     * @param response
     * @param workFlowRequestBean
     * @param merchantCoftConfig
     */
    private void processBizFlow(ExpressCardTokenRequest requestData, ExpressCardTokenResponse response,
            WorkFlowRequestBean workFlowRequestBean, boolean isEnableGcinOnCoftPromo, String merchantCoftConfig) {

        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = bizService.processWorkFlow(workFlowRequestBean,
                expressTokenGeneratorFlow);

        if (!bizResponseBean.isSuccessfullyProcessed() && bizResponseBean.getResponseConstant() != null
                && bizResponseBean.getResponseConstant().isResponseToMerchant()) {
            LOGGER.error("Cache card for express payment failed due to {}", bizResponseBean.getFailureMessage());
            response.setStatus(ResponseConstants.FAILURE.getMessage());
            response.setErrorCode(ResponseConstants.TOKEN_NOT_GENERATD.getCode());
            response.setErrorMessage(ResponseConstants.TOKEN_NOT_GENERATD.getMessage());
        } else if (bizResponseBean.getResponse() != null
                && bizResponseBean.getResponse().getCacheCardResponseBean() != null
                && StringUtils.isNotBlank(bizResponseBean.getResponse().getCacheCardResponseBean().getTokenId())) {
            // Token successfully received
            LOGGER.info("Token successfully generated for express payment flow, Token Id : {}, Card Index No : {}",
                    bizResponseBean.getResponse().getCacheCardResponseBean().getTokenId(), bizResponseBean
                            .getResponse().getCacheCardResponseBean().getCardIndexNo());

            // Obtain a request id from IdManager and set
            // mid|custId|tokenId|cardIndexNo in cache
            String requestId = UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis();
            response.setStatus(ResponseConstants.SUCCESS.getMessage());
            response.setToken(requestId);
            String uniqueCardIdentifier = null;
            if (merchantCoftConfig.equals("GCIN") && null != bizResponseBean.getResponse().getWorkFlowRequestBean()
                    && null != bizResponseBean.getResponse().getWorkFlowRequestBean().getGcin()) {
                uniqueCardIdentifier = bizResponseBean.getResponse().getWorkFlowRequestBean().getGcin();
            }
            expressPaymentServiceHelper.setCardTokenDetailsInCache(response.getToken(), requestData, bizResponseBean
                    .getResponse().getCacheCardResponseBean(), isEnableGcinOnCoftPromo, uniqueCardIdentifier,
                    merchantCoftConfig);
            /*
             * Caching card details into Redis for further processing. Token
             * being used as Key
             */

            expressPaymentServiceHelper.setCardDetilsInCache(bizResponseBean.getResponse().getCacheCardResponseBean()
                    .getTokenId(), requestData);
        } else {
            response.setStatus(ResponseConstants.FAILURE.getMessage());
            response.setErrorCode(ResponseConstants.TOKEN_NOT_GENERATD.getCode());
            response.setErrorMessage(ResponseConstants.TOKEN_NOT_GENERATD.getMessage());
        }
    }

    private ExpressCardTokenResponse mandatoryValidations(ExpressCardTokenRequest expressCardTokenRequest,
            ExpressCardTokenResponse response) {
        if (StringUtils.isBlank(expressCardTokenRequest.getMid())) {
            response.setStatus(ResponseConstants.FAILURE.getMessage());
            response.setErrorCode(ResponseConstants.INVALID_MID.getCode());
            response.setErrorMessage(ResponseConstants.INVALID_MID.getMessage());
        } else if (StringUtils.isBlank(expressCardTokenRequest.getUserId())) {
            response.setStatus(ResponseConstants.FAILURE.getMessage());
            response.setErrorCode(ResponseConstants.INVALID_JSON_DATA.getCode());
            response.setErrorMessage(ResponseConstants.INVALID_JSON_DATA.getMessage());
        }
        return response;
    }

    private ExpressCardTokenResponse businessValidations(ExpressCardTokenRequest requestData,
            ExpressCardTokenResponse response, boolean validateExpiryAndCvv) {
        // For MAESTRO cards, expiryDate & CVV not mandatory
        if (!expressPaymentValidation.validateCardNumber(requestData.getCardNumber())) {
            response.setStatus(ResponseConstants.FAILURE.getMessage());
            response.setErrorCode(ResponseConstants.INVALID_CARD_NUMBER.getCode());
            response.setErrorMessage(ResponseConstants.INVALID_CARD_NUMBER.getMessage());
        } else if (validateExpiryAndCvv && !expressPaymentValidation.validateCvv(requestData)) {
            response.setStatus(ResponseConstants.FAILURE.getMessage());
            response.setErrorCode(ResponseConstants.INCOMPLETE_CARD_INFORMATION.getCode());
            response.setErrorMessage(ResponseConstants.INCOMPLETE_CARD_INFORMATION.getMessage());
        } else if (validateExpiryAndCvv
                && !expressPaymentValidation.validateExpiryDate(requestData.getExpiryMonth(),
                        requestData.getExpiryYear(), requestData.getCardScheme())) {
            response.setStatus(ResponseConstants.FAILURE.getMessage());
            response.setErrorCode(ResponseConstants.INVALID_EXPIRY_DATE.getCode());
            response.setErrorMessage(ResponseConstants.INVALID_EXPIRY_DATE.getMessage());
        }
        return response;
    }

    private ExpressCardTokenResponse businessValidationsWithSavedCardIndexNumber(ExpressCardTokenRequest requestData,
            ExpressCardTokenResponse response, boolean validateExpiryAndCvv) {
        // For MAESTRO cards, expiryDate & CVV not mandatory
        if (validateExpiryAndCvv
                && !expressPaymentValidation.validatingExpireByCardscheme(requestData.getCvv(),
                        requestData.getCardScheme())) {
            response.setStatus(ResponseConstants.FAILURE.getMessage());
            response.setErrorCode(ResponseConstants.INCOMPLETE_CARD_INFORMATION.getCode());
            response.setErrorMessage(ResponseConstants.INCOMPLETE_CARD_INFORMATION.getMessage());
        } else if (validateExpiryAndCvv
                && !expressPaymentValidation.validateExpiryDate(requestData.getExpiryMonth(),
                        requestData.getExpiryYear(), requestData.getCardScheme())) {
            response.setStatus(ResponseConstants.FAILURE.getMessage());
            response.setErrorCode(ResponseConstants.INVALID_EXPIRY_DATE.getCode());
            response.setErrorMessage(ResponseConstants.INVALID_EXPIRY_DATE.getMessage());
        }
        return response;
    }

    public void setUserDetailsForLoggedInUser(final HttpServletRequest request, ExpressCardTokenRequest requestData) {

        String accessToken = request.getParameter("ACCESS_TOKEN");
        String mid = request.getParameter("MID");
        String referenceId = request.getParameter("referenceId");

        if (StringUtils.isNotBlank(accessToken)) {
            CreateAccessTokenServiceRequest accessTokenData = accessTokenUtils.validateAccessToken(mid, referenceId,
                    accessToken);

            if (accessTokenData.getNativePersistData() != null) {
                NativePersistData nativePersistData = accessTokenData.getNativePersistData();
                if (nativePersistData != null && nativePersistData.getUserDetails() != null) {
                    requestData.setLoginUserId(nativePersistData.getUserDetails().getUserId());
                }
            }
        }

    }

    public FetchTokenDetailRequest createFetchTokenDetailRequest(ExpressCardTokenRequest requestData) {
        FetchTokenDetailRequestBody fetchTokenDetailRequestBody = new FetchTokenDetailRequestBody();
        fetchTokenDetailRequestBody.setMid(requestData.getMid());
        fetchTokenDetailRequestBody.setTokenIndexNumber(requestData.getSavedCardId());

        FetchTokenDetailRequest fetchTokenDetailRequest = new FetchTokenDetailRequest();
        fetchTokenDetailRequest.setHead(new GenerateTokenDataRequestHead());
        fetchTokenDetailRequest.setBody(fetchTokenDetailRequestBody);
        return fetchTokenDetailRequest;
    }

    public void setBinDetailsForTin(ExpressCardTokenRequest requestData, boolean isEnableGcinOnCoftPromo,
            String merchantCoftConfig, ExpressCardTokenResponse response) {
        FetchTokenDetailRequest fetchTokenDetailRequest = createFetchTokenDetailRequest(requestData);
        FetchTokenDetailResponse fetchTokenDetailResponse = null;

        try {
            fetchTokenDetailResponse = coftService.fetchTokenDetail(fetchTokenDetailRequest);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Unable to fetch token bin : ", e);
        }
        if (Objects.nonNull(fetchTokenDetailResponse) && Objects.nonNull(fetchTokenDetailResponse.getBody())
                && Objects.nonNull(fetchTokenDetailResponse.getBody().getTokenInfo())
                && StringUtils.isNotEmpty(fetchTokenDetailResponse.getBody().getTokenInfo().getTokenBin())) {
            BinDetail binDetail = coftTokenDataService.getCardBinDetails(fetchTokenDetailResponse.getBody()
                    .getTokenInfo().getTokenBin());
            if (binDetail.getBinAttributes() != null) {
                String accountRangeCardBin = binDetail.getBinAttributes().get(
                        BizConstant.ExtendedInfoKeys.ACCOUNT_RANGE_CARD_BIN);
                requestData.setCardNumber(accountRangeCardBin);
                requestData.setExpiryMonth(fetchTokenDetailResponse.getBody().getTokenInfo().getTokenExpiry()
                        .substring(0, 2));
                requestData.setExpiryYear(fetchTokenDetailResponse.getBody().getTokenInfo().getTokenExpiry()
                        .substring(2, 6));
                requestData.setCardScheme(fetchTokenDetailResponse.getBody().getTokenInfo().getCardScheme());
                LOGGER.debug("Request data updated:{}", requestData);
                String uniqueCardIdentifier = null;
                if ("PAR".equals(merchantCoftConfig)) {
                    uniqueCardIdentifier = fetchTokenDetailResponse.getBody().getTokenInfo().getPanUniqueReference();
                } else if ("GCIN".equals(merchantCoftConfig)) {
                    uniqueCardIdentifier = fetchTokenDetailResponse.getBody().getTokenInfo().getGlobalPanIndex();
                }
                setParamsINCacheForTin(merchantCoftConfig, requestData, isEnableGcinOnCoftPromo, uniqueCardIdentifier,
                        response);
            }
        }
        if (StringUtils.isBlank(requestData.getCardNumber())) {
            response.setStatus(ResponseConstants.FAILURE.getMessage());
            response.setErrorCode(ResponseConstants.INTERNAL_PROCESSING_ERROR.getCode());
            response.setErrorMessage(ResponseConstants.INTERNAL_PROCESSING_ERROR.getMessage());
        }
    }

    public void setBinDetailsForCin(ExpressCardTokenRequest requestData, boolean isEnableGcinOnCoftPromo,
            String merchantCoftConfig, ExpressCardTokenResponse response) {
        QueryNonSensitiveAssetInfoResponse queryNonSensitiveAssetInfoResponse = cardCenterHelper
                .queryNonSensitiveAssetInfo(null, requestData.getSavedCardId());
        if (queryNonSensitiveAssetInfoResponse != null) {
            requestData.setCardNumber(queryNonSensitiveAssetInfoResponse.getCardInfo().getCardBin());
            requestData.setExpiryMonth(queryNonSensitiveAssetInfoResponse.getCardInfo().getExtendInfo()
                    .get("cardExpireMonth"));
            requestData.setExpiryYear(queryNonSensitiveAssetInfoResponse.getCardInfo().getExtendInfo()
                    .get("cardExpireYear"));
            requestData.setCardScheme(queryNonSensitiveAssetInfoResponse.getCardInfo().getCardScheme());
            LOGGER.debug("Request data updated:{}", requestData);
        } else {
            response.setStatus(ResponseConstants.FAILURE.getMessage());
            response.setErrorCode(ResponseConstants.INTERNAL_PROCESSING_ERROR.getCode());
            response.setErrorMessage(ResponseConstants.INTERNAL_PROCESSING_ERROR.getMessage());
        }
    }

    private void setParamsINCacheForTin(String merchantCoftConfig, ExpressCardTokenRequest requestData,
            boolean isEnableGcinOnCoftPromo, String uniqueCardIdentifier, ExpressCardTokenResponse response) {
        String requestId = UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis();
        CacheCardResponseBean cacheCardResponseBean = new CacheCardResponseBean(requestId, null,
                requestData.getSavedCardId());
        expressPaymentServiceHelper.setCardTokenDetailsInCache(requestId, requestData, cacheCardResponseBean,
                isEnableGcinOnCoftPromo, uniqueCardIdentifier, merchantCoftConfig);
        expressPaymentServiceHelper.setCardDetilsInCache(requestId, requestData);
        setTokenInExpressCardTokenResponse(response, uniqueCardIdentifier, requestId);
    }

    public void setBinDetailsForCoftToken(ExpressCardTokenRequest requestData, boolean isEnableGcinOnCoftPromo,
            String merchantCoftConfig, String uniqueCardIdentifier, ExpressCardTokenResponse response) {
        BinDetail binDetail = coftTokenDataService.getCardBinDetails(requestData.getCardTokenInfo().getCardToken());
        if (binDetail != null && binDetail.getBinAttributes() != null) {
            String accountRangeCardBin = binDetail.getBinAttributes().get(
                    BizConstant.ExtendedInfoKeys.ACCOUNT_RANGE_CARD_BIN);
            requestData.setCardScheme(binDetail.getCardName());
            requestData.setCardNumber(accountRangeCardBin);
            LOGGER.debug("Request data updated:{}", requestData);
            setParamsINCacheForTokenCards(merchantCoftConfig, requestData, isEnableGcinOnCoftPromo,
                    uniqueCardIdentifier, response);
        } else {
            response.setStatus(ResponseConstants.FAILURE.getMessage());
            response.setErrorCode(ResponseConstants.INTERNAL_PROCESSING_ERROR.getCode());
            response.setErrorMessage(ResponseConstants.INTERNAL_PROCESSING_ERROR.getMessage());
        }
    }

    private void setParamsINCacheForTokenCards(String merchantCoftConfig, ExpressCardTokenRequest requestData,
            boolean isEnableGcinOnCoftPromo, String uniqueCardIdentifier, ExpressCardTokenResponse response) {
        String requestId = UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis();
        CacheCardResponseBean cacheCardResponseBean = new CacheCardResponseBean(requestId, null, null);
        expressPaymentServiceHelper.setCardTokenDetailsInCache(requestId, requestData, cacheCardResponseBean,
                isEnableGcinOnCoftPromo, uniqueCardIdentifier, merchantCoftConfig);
        expressPaymentServiceHelper.setCardDetilsInCache(requestId, requestData);
        setTokenInExpressCardTokenResponse(response, uniqueCardIdentifier, requestId);

    }

    private void setTokenInExpressCardTokenResponse(ExpressCardTokenResponse response, String uniqueCardIdentifier,
            String requestId) {

        if (StringUtils.isNotBlank(uniqueCardIdentifier)) {
            response.setStatus(ResponseConstants.SUCCESS.getMessage());
            response.setToken(requestId);
        } else {
            response.setStatus(ResponseConstants.FAILURE.getMessage());
            response.setErrorCode(ResponseConstants.INTERNAL_PROCESSING_ERROR.getCode());
            response.setErrorMessage(ResponseConstants.INTERNAL_PROCESSING_ERROR.getMessage());
        }
    }
}
