package com.paytm.pgplus.theia.services.helper;

import com.paytm.pgplus.biz.core.model.request.CacheCardResponseBean;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.service.helper.BizProdHelper;
import com.paytm.pgplus.biz.workflow.service.helper.MerchantBizProdHelper;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cache.model.ExpressCardModel;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.facade.merchant.models.SavedAssetInfo;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.savedcardclient.models.SavedCardResponse;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;
import com.paytm.pgplus.savedcardclient.service.ISavedCardService;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.ExpressCardTokenRequest;
import com.paytm.pgplus.theia.models.response.ExpressCardTokenResponse;
import com.paytm.pgplus.theia.nativ.service.CoftTokenDataService;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ExpressPaymentServiceHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressPaymentServiceHelper.class);
    private static final String MAESTRO = "MAESTRO";
    private static final String MAESTRO_YEAR = "2049";
    private static final String MAESTRO_MONTH = "12";
    private static final String MAESTRO_CVV = "123";
    private static final long EXPRESS_TOKEN_EXPIRY = 600;

    @Autowired
    @Qualifier("savedCardService")
    ISavedCardService savedCardService;

    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    @Autowired
    @Qualifier("BizProdHelper")
    private BizProdHelper bizProdHelper;

    @Autowired
    private MerchantBizProdHelper merchantBizProdHelper;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    @Qualifier("coftTokenDataService")
    private CoftTokenDataService coftTokenDataService;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    public void setCardDetilsInCache(String token, ExpressCardTokenRequest requestData) {
        try {
            /*
             * Preparing data for Redis cache.
             * cardNumber|cvvNumber|expirydate|cardType|cardScheme . Also
             * "expirydate" must be in format(MMYYYY)
             */
            String cardNumber = requestData.getCardNumber();
            String cvv = MAESTRO.equals(requestData.getCardScheme()) ? MAESTRO_CVV : requestData.getCvv();
            String expiryMonth = MAESTRO.equals(requestData.getCardScheme()) ? MAESTRO_MONTH : requestData
                    .getExpiryMonth();
            String expiryYear = MAESTRO.equals(requestData.getCardScheme()) ? MAESTRO_YEAR : requestData
                    .getExpiryYear();
            String cardType = requestData.getCardType();
            String cardScheme = requestData.getCardScheme();

            StringBuilder cacheCardData = new StringBuilder();
            cacheCardData.append(cardNumber).append("|").append(cvv).append("|").append(expiryMonth).append(expiryYear)
                    .append("|").append(cardType).append("|").append(cardScheme);

            StringBuilder key = new StringBuilder();
            key.append(ExtraConstants.EXPRESS_PAYMENT).append(token);
            LOGGER.debug("Data being pushed in Redis:{}", cacheCardData.toString());
            theiaTransactionalRedisUtil.set(key.toString(), cacheCardData.toString(), 1 * 60 * 60);
        } catch (Exception e) {
            throw new TheiaServiceException("Exception occuured while saving details into Redis", e);
        }

    }

    public void setCardTokenDetailsInCache(String requestId, ExpressCardTokenRequest requestData,
            CacheCardResponseBean cacheCardResponseBean, boolean isEnableGcinOnCoftPromo, String uniqueCardIdentifier,
            String merchantCoftConfig) {
        try {
            LOGGER.info("Express requestId :{} , RequestData :{}", requestId, requestData);
            String key = ExtraConstants.EXPRESS_CARD_TOKEN + requestId;
            ExpressCardModel expressCardModel = new ExpressCardModel(requestData.getMid(), requestData.getUserId(),
                    cacheCardResponseBean.getTokenId(), cacheCardResponseBean.getCardIndexNo(), requestData
                            .getCardNumber().substring(0, 6));
            if (isEnableGcinOnCoftPromo) {
                updateExpressCardModel(requestData, expressCardModel, uniqueCardIdentifier, merchantCoftConfig);
            }
            theiaTransactionalRedisUtil.set(key, expressCardModel, EXPRESS_TOKEN_EXPIRY);
        } catch (Exception e) {
            LOGGER.error("Exception occuured while saving details into Redis", e);
        }
    }

    public void fetchSavedCardDetailsAndProcess(ExpressCardTokenRequest requestData, ExpressCardTokenResponse response) {

        SavedCardResponse<SavedCardVO> savedCardsBean = null;

        boolean isCustIdFlow = false;
        boolean isLoggedInUserFlow = true;

        if (StringUtils.isNotBlank(requestData.getLoginUserId())) {
            savedCardsBean = savedCardService.getSavedCardByCardId(Long.valueOf(requestData.getSavedCardId()),
                    requestData.getLoginUserId());
        }

        if ((savedCardsBean == null) || !savedCardsBean.getStatus()) {

            savedCardsBean = savedCardService.getSavedCardByCardId(Long.valueOf(requestData.getSavedCardId()),
                    requestData.getUserId());

            if ((savedCardsBean == null) || !savedCardsBean.getStatus()) {

                savedCardsBean = savedCardService.getSavedCardByCardId(Long.valueOf(requestData.getSavedCardId()),
                        requestData.getUserId(), requestData.getMid());

                isCustIdFlow = true;
            }

            isLoggedInUserFlow = false;
        }

        LOGGER.debug("Saved Cards Response returned as:::{}", savedCardsBean);

        validateSavedCardResponse(requestData, response, savedCardsBean, isCustIdFlow, isLoggedInUserFlow);

        LOGGER.debug("Request data updated:{}", requestData);
    }

    private void validateSavedCardResponse(ExpressCardTokenRequest requestData, ExpressCardTokenResponse response,
            SavedCardResponse<SavedCardVO> savedCardsBean, boolean isCustIdFlow, boolean isLoggedInUserFlow) {

        if ((savedCardsBean == null) || !savedCardsBean.getStatus()) {
            response.setStatus(ResponseConstants.FAILURE.getMessage());
            response.setErrorCode(ResponseConstants.INTERNAL_PROCESSING_ERROR.getCode());
            if (savedCardsBean != null
                    && TheiaConstant.ResponseConstants.INVALID_REPONSE_FROM_SERVICE.equals(savedCardsBean.getMessage())) {
                LOGGER.error("cardInfo is null");
                response.setErrorMessage(TheiaConstant.ResponseConstants.CARD_DETAILS_NOT_FOUND);
                return;
            }
            LOGGER.error("Error occured while fetching savedCards for express payment");
            response.setErrorMessage(ResponseConstants.INTERNAL_PROCESSING_ERROR.getMessage());

        } else if (isCustIdFlow
                && !isLoggedInUserFlow
                && (null == savedCardsBean.getResponseData().getCustId() || !savedCardsBean.getResponseData()
                        .getCustId().equals(requestData.getUserId()))) {

            LOGGER.error("Invalid CustId sent in express flow request.");
            response.setStatus(ResponseConstants.FAILURE.getMessage());
            response.setErrorCode(ResponseConstants.INVALID_JSON_DATA.getCode());
            response.setErrorMessage(ResponseConstants.INVALID_JSON_DATA.getMessage());

        } else if (!isCustIdFlow
                && !isLoggedInUserFlow
                && (null == savedCardsBean.getResponseData().getUserId() || !savedCardsBean.getResponseData()
                        .getUserId().equals(requestData.getUserId()))) {

            LOGGER.error("Invalid UserId sent in express flow request.");
            response.setStatus(ResponseConstants.FAILURE.getMessage());
            response.setErrorCode(ResponseConstants.INVALID_JSON_DATA.getCode());
            response.setErrorMessage(ResponseConstants.INVALID_JSON_DATA.getMessage());

        } else if (isLoggedInUserFlow
                && (null == savedCardsBean.getResponseData().getUserId() || !savedCardsBean.getResponseData()
                        .getUserId().equals(requestData.getLoginUserId()))) {

            LOGGER.error("Invalid UserId sent in express flow request.");
            response.setStatus(ResponseConstants.FAILURE.getMessage());
            response.setErrorCode(ResponseConstants.INVALID_JSON_DATA.getCode());
            response.setErrorMessage(ResponseConstants.INVALID_JSON_DATA.getMessage());

        } else {

            LOGGER.info("Successfully fetched card details for savedCardId:{}", requestData.getSavedCardId());
            requestData.setCardNumber(savedCardsBean.getResponseData().getCardNumber());
            requestData.setExpiryMonth(savedCardsBean.getResponseData().getExpiryDate().substring(0, 2));
            requestData.setExpiryYear(savedCardsBean.getResponseData().getExpiryDate().substring(2, 6));
        }

    }

    /**
     * @param requestData
     * @param workFlowRequestBean
     */
    public void mapExpressFlowReqData(ExpressCardTokenRequest requestData, WorkFlowRequestBean workFlowRequestBean) {

        workFlowRequestBean.setPaytmMID(requestData.getMid());
        GenericCoreResponseBean<MappingMerchantData> merchantMappingResponse = merchantMappingService
                .fetchMerchanData(requestData.getMid());
        if ((merchantMappingResponse != null) && (merchantMappingResponse.getResponse() != null)) {
            LOGGER.info("Mapping service response received:{}", merchantMappingResponse.getResponse());
            workFlowRequestBean.setAlipayMID(merchantMappingResponse.getResponse().getAlipayId());
        } else {
            final String error = merchantMappingResponse == null ? "Could not map merchant" : merchantMappingResponse
                    .getFailureMessage();
            throw new PaymentRequestValidationException(error);
        }

        /*
         * Similar value need to set for IMPS also. This PaymentTypeId will be
         * using while creating request for cacheCard API in biz module.
         */
        workFlowRequestBean.setPaymentTypeId(PaymentTypeIdEnum.CC.value);
        if (requestData.getCardTokenInfo() != null) {
            workFlowRequestBean.setCoftTokenTxn(true);
        }

        setPG2Preference(workFlowRequestBean);
        setCardDetails(requestData, workFlowRequestBean);

    }

    /**
     * @param requestData
     * @param workFlowRequestBean
     */
    private void setCardDetails(ExpressCardTokenRequest requestData, WorkFlowRequestBean workFlowRequestBean) {
        workFlowRequestBean.setCardNo(requestData.getCardNumber());
        // Checking if card is MAESTRO & CVV,expiry is not provided
        if (MAESTRO.equals(requestData.getCardScheme())) {
            workFlowRequestBean.setCvv2("123");
            workFlowRequestBean.setExpiryMonth(Short.valueOf("12"));
            workFlowRequestBean.setExpiryYear(Short.valueOf("2049"));
        } else {
            if (StringUtils.isNotBlank(requestData.getExpiryMonth())
                    && StringUtils.isNotBlank(requestData.getExpiryYear())
                    && StringUtils.isNotBlank(requestData.getCvv())) {
                workFlowRequestBean.setCvv2(requestData.getCvv());
                workFlowRequestBean.setExpiryMonth(Short.valueOf(requestData.getExpiryMonth()));
                workFlowRequestBean.setExpiryYear(Short.valueOf(requestData.getExpiryYear()));
            } else {
                workFlowRequestBean.setRequestType(ERequestType.CC_BILL_PAYMENT);
            }
        }

        workFlowRequestBean.setInstId(requestData.getInstId());
        workFlowRequestBean.setCardType(requestData.getCardType());
        workFlowRequestBean.setCardScheme(requestData.getCardScheme());
        // setting when cardIndexNumber is coming in savedCardId
        if (StringUtils.isNotBlank(requestData.getSavedCardId()) && requestData.getSavedCardId().length() > 15) {
            workFlowRequestBean.setCardIndexNo(requestData.getSavedCardId());
        }
    }

    public BinDetail fetchBinRelatedDetails(String cardNumber) throws PaytmValidationException {

        final String binNumber = cardNumber.substring(0, 6);

        BinDetail binDetail = null;

        try {

            binDetail = cardUtils.fetchBinDetails(binNumber);

            if (binDetail == null) {
                LOGGER.error("No bin details found for bin number {}", binNumber);
            }

        } catch (PaytmValidationException exception) {

            LOGGER.error("Error while fetching Bin Details {}", exception);

            binDetail = null;
        }

        return binDetail;
    }

    public void fetchSavedCardDetailsAndProcessWithCardIndexNumber(ExpressCardTokenRequest requestData,
            ExpressCardTokenResponse response) {

        SavedAssetInfo savedAssetInfo = bizProdHelper.getSavedCardByUserIdAndCardId(requestData.getSavedCardId(),
                requestData.getLoginUserId());

        if (null == savedAssetInfo) {

            savedAssetInfo = bizProdHelper.getSavedCardByUserIdAndCardId(requestData.getSavedCardId(),
                    requestData.getUserId());

            if (null == savedAssetInfo) {

                savedAssetInfo = merchantBizProdHelper.getSavedCardByMidCustIdAndCardId(requestData.getSavedCardId(),
                        requestData.getMid(), requestData.getUserId());

            }
            if (null == savedAssetInfo) {
                response.setStatus(ResponseConstants.FAILURE.getMessage());
                response.setErrorCode(ResponseConstants.INTERNAL_PROCESSING_ERROR.getCode());
                // handling of msg to be done
                /*
                 * if (savedAssetInfoResponse != null &&
                 * TheiaConstant.ResponseConstants
                 * .INVALID_REPONSE_FROM_SERVICE.equals
                 * (savedAssetInfoResponse.getMessage())) {
                 * LOGGER.error("cardInfo is null");
                 * response.setErrorMessage(TheiaConstant
                 * .ResponseConstants.CARD_DETAILS_NOT_FOUND); return; }
                 */
                LOGGER.error("Error occured while fetching savedCards for express payment");
                response.setErrorMessage(ResponseConstants.INTERNAL_PROCESSING_ERROR.getMessage());
                return;
            }

        }

        requestData.setCardNumber(savedAssetInfo.getMaskedCardNo().substring(0, 6));
        requestData.setExpiryMonth(savedAssetInfo.getExpiryMonth());
        requestData.setExpiryYear(savedAssetInfo.getExpiryYear());
        requestData.setCardScheme(savedAssetInfo.getCardScheme());
        LOGGER.debug("Request data updated:{}", requestData);
    }

    public void updateExpressCardModel(ExpressCardTokenRequest requestData, ExpressCardModel expressCardModel,
            String uniqueCardIdentifier, String merchantCoftConfig) {

        if (StringUtils.isNotBlank(requestData.getSavedCardId())) {
            setDetailsInExpressModelForSavedCard(requestData, expressCardModel, merchantCoftConfig,
                    uniqueCardIdentifier);
        } else if (Objects.nonNull(requestData.getCardTokenInfo())) {
            expressCardModel.setUniqueCardIdentifier(uniqueCardIdentifier);
        } else if (StringUtils.isNotBlank(requestData.getCardNumber())) {
            setDetailsInExpressModelForCardNumber(requestData, expressCardModel, merchantCoftConfig,
                    uniqueCardIdentifier);
        }
    }

    public void setDetailsInExpressModelForSavedCard(ExpressCardTokenRequest requestData,
            ExpressCardModel expressCardModel, String merchantCoftConfig, String uniqueCardIdentifier) {
        String savedCardId = requestData.getSavedCardId();
        if (savedCardId.length() > 15 && savedCardId.length() < 45) {
            expressCardModel.setUniqueCardIdentifier(uniqueCardIdentifier);
        } else if (savedCardId.length() > 15) {
            if (merchantCoftConfig.equals("PAR")) {
                String par = coftTokenDataService.getTokenData(requestData.getMid(), savedCardId, "CIN",
                        merchantCoftConfig);
                expressCardModel.setUniqueCardIdentifier(par);
            } else if (merchantCoftConfig.equals("GCIN")) {
                expressCardModel.setUniqueCardIdentifier(uniqueCardIdentifier);
            }
        }
    }

    public void setDetailsInExpressModelForCardNumber(ExpressCardTokenRequest requestData,
            ExpressCardModel expressCardModel, String merchantCoftConfig, String uniqueCardIdentifier) {
        if (merchantCoftConfig.equals("PAR")) {
            String par = coftTokenDataService.getSavedCardIdFromCardNumber(requestData.getMid(),
                    requestData.getCardNumber(), merchantCoftConfig);
            expressCardModel.setUniqueCardIdentifier(par);
        } else if (merchantCoftConfig.equals("GCIN")) {
            if (StringUtils.isBlank(uniqueCardIdentifier)) {
                uniqueCardIdentifier = coftTokenDataService.getSavedCardIdFromCardNumber(requestData.getMid(),
                        requestData.getCardNumber(), merchantCoftConfig);
            }
            expressCardModel.setUniqueCardIdentifier(uniqueCardIdentifier);
        }
    }

    public void setPG2Preference(WorkFlowRequestBean flowRequestBean) {
        if (StringUtils.isNotBlank(flowRequestBean.getPaytmMID())) {
            flowRequestBean.setFullPg2TrafficEnabled(merchantPreferenceService.isFullPg2TrafficEnabled(flowRequestBean
                    .getPaytmMID()));
        }
    }
}
