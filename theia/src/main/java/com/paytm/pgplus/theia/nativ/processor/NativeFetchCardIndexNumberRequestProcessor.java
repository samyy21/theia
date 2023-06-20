package com.paytm.pgplus.theia.nativ.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.biz.core.cachecard.utils.CacheCardInfoHelper;
import com.paytm.pgplus.biz.core.model.request.CacheCardRequestBean;
import com.paytm.pgplus.biz.core.model.request.CacheCardResponseBean;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.CoftUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.cashier.exception.SaveCardValidationException;
import com.paytm.pgplus.cashier.util.CashierUtilService;
import com.paytm.pgplus.common.signature.JWTWithHmacSHA256;
import com.paytm.pgplus.facade.enums.InstNetworkType;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.user.models.request.CacheCardRequest;
import com.paytm.pgplus.facade.user.models.request.FetchUserDetailsRequest;
import com.paytm.pgplus.facade.user.models.response.CacheCardResponse;
import com.paytm.pgplus.facade.user.models.response.CacheCardResponseBody;
import com.paytm.pgplus.facade.user.models.response.FetchUserDetailsResponse;
import com.paytm.pgplus.facade.user.services.IAsset;
import com.paytm.pgplus.facade.user.services.IAuthentication;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.UPSHelper;
import com.paytm.pgplus.theia.nativ.model.cardindexnumber.*;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import com.paytm.pgplus.theia.validator.service.ExpressPaymentValidation;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.apache.commons.lang.StringUtils.isEmpty;

@Service(value = "nativeFetchCardIndexNumberRequestProcessor")
public class NativeFetchCardIndexNumberRequestProcessor
        extends
        AbstractRequestProcessor<NativeFetchCardIndexNumberAPIRequest, NativeFetchCardIndexNumberAPIResponse, NativeCardIndexNumberServReq, NativeCardIndexNumberServResp> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeFetchCardIndexNumberRequestProcessor.class);

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    @Qualifier("expressPaymentValidation")
    ExpressPaymentValidation expressPaymentValidation;

    @Autowired
    CashierUtilService cashierUtilService;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    private IAuthentication authFacade;

    @Autowired
    @Qualifier("assetImpl")
    private IAsset assetFacade;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private TokenValidationHelper tokenValidationHelper;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    private Environment environment;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Autowired
    @Qualifier("upsHelper")
    private UPSHelper upsHelper;

    @Autowired
    private CoftUtil coftUtil;

    @Autowired
    private WorkFlowHelper workFlowHelper;

    private static final String BANK_ACCOUNT_NUMBER = "bankAccountNumber";

    @Override
    protected NativeCardIndexNumberServReq preProcess(NativeFetchCardIndexNumberAPIRequest request) {
        return validateRequest(request);
    }

    @Override
    protected NativeCardIndexNumberServResp onProcess(NativeFetchCardIndexNumberAPIRequest request,
            NativeCardIndexNumberServReq servReq) throws Exception {
        String cardNumber = request.getBody().getCardNumber();
        boolean isCINforBankAccountNumber = false;
        Short expiryMonth = null;
        Short expiryYear = null;
        String cardExpiry = null;

        if (StringUtils.isNotBlank(request.getBody().getBankAccountNumber())) {

            cardNumber = request.getBody().getBankAccountNumber();
            isCINforBankAccountNumber = true;
        } else {
            cardExpiry = request.getBody().getCardExpiry();
            String[] cardExpiryDetails = cardExpiry.split("/");
            expiryMonth = Short.parseShort(cardExpiryDetails[0]);
            expiryYear = Short.parseShort(cardExpiryDetails[1]);

            if (servReq.isSavedCardId()) {
                String savedCardId = cardNumber.replace("|", "");
                cardNumber = getSavedCardNumFromCardId(savedCardId, servReq.getEffectivePaytmSsoToken(),
                        servReq.getOrderDetail());

                if (StringUtils.isBlank(cardNumber)) {
                    // return error because cardnumber not got from
                    // savedcardservice
                    throw RequestValidationException.getException(ResultCode.FAILED);
                }
            }

        }

        final CacheCardRequestBean cacheCardReqBean = new CacheCardRequestBean.CacheCardRequestBeanBuilder(cardNumber,
                null, request.getBody().getBankIfsc(), null, null, null, expiryYear, expiryMonth).build();

        final CacheCardRequest cacheCardReq = createCacheCardRequest(cacheCardReqBean, isCINforBankAccountNumber);

        final CacheCardResponse cacheCardFacadeResponse = callAPlusForCardIdxNo(cacheCardReq);

        NativeCardIndexNumberServResp servResp = null;

        if (cacheCardFacadeResponse != null && cacheCardFacadeResponse.getBody() != null) {
            servResp = new NativeCardIndexNumberServResp();
            servResp.setCacheCardResponseBody(cacheCardFacadeResponse.getBody());

            // saves expiry date and cardmasked number in redis against CIN
            if (!isCINforBankAccountNumber) {
                setCardMetaDataInCache(cacheCardFacadeResponse, cardExpiry);
            }

        }

        return servResp;
    }

    private void setCardMetaDataInCache(CacheCardResponse cacheCardFacadeResponse, String cardExpiry) {
        int expiryCachingTime = Integer.parseInt(ConfigurationUtil.getProperty(
                TheiaConstant.ExtraConstants.CARD_META_DATA_CACHE_TIME, "900"));
        String cardIndexNo = cacheCardFacadeResponse.getBody().getCardIndexNo();
        String maskedCardNo = cacheCardFacadeResponse.getBody().getMaskedCardNo();
        nativeSessionUtil.setCardMetaDataInCache(cardIndexNo, cardExpiry, maskedCardNo, expiryCachingTime);
        LOGGER.info("Successfully saved cardExpiry and maskedCardNo in cache for Card Index No : {}", cardIndexNo);
    }

    @Override
    protected NativeFetchCardIndexNumberAPIResponse postProcess(NativeFetchCardIndexNumberAPIRequest request,
            NativeCardIndexNumberServReq servReq, NativeCardIndexNumberServResp servResp) throws Exception {
        return createResponse(request, servResp);
    }

    private CacheCardResponse callAPlusForCardIdxNo(CacheCardRequest cacheCardReq) {
        try {
            coftUtil.updateCacheCardRequest(cacheCardReq);
            if (ff4jUtils.isFeatureEnabled(BizConstant.Ff4jFeature.ENABLE_ROUTE_CACHE_CARD_FOR_FETCH_CARD_INDEX, false)) {
                GenericCoreResponseBean<CacheCardResponseBean> cacheCardResponseBeanGenericCoreResponseBean = workFlowHelper
                        .fetchAssetIdforPG2BankTransferRequest(null, cacheCardReq);
                if (cacheCardResponseBeanGenericCoreResponseBean.isSuccessfullyProcessed()
                        && Objects.nonNull(cacheCardResponseBeanGenericCoreResponseBean.getResponse())) {
                    CacheCardResponseBean cacheCardResponseBean = cacheCardResponseBeanGenericCoreResponseBean
                            .getResponse();
                    CacheCardResponseBody responseBody = new CacheCardResponseBody();
                    responseBody.setResultInfo(new com.paytm.pgplus.facade.common.model.ResultInfo("S", "00000000",
                            "SUCCESS", "SUCCESS"));
                    responseBody.setTokenId(cacheCardResponseBean.getTokenId());
                    responseBody.setCardIndexNo(cacheCardResponseBean.getCardIndexNo());
                    responseBody.setMaskedCardNo(cacheCardResponseBean.getMaskedCardNo());
                    return new CacheCardResponse(responseBody, null);
                }
            } else
                return assetFacade.cacheCard(cacheCardReq);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occured in cache card info in /fetchCardIndexNo: ", e);
        }
        return null;
    }

    private CacheCardRequest createCacheCardRequest(final CacheCardRequestBean cacheCardRequestBean,
            boolean isCINforBankAccountNumber) {
        GenericCoreResponseBean<CacheCardRequest> cacheCardReq = null;
        if (!isCINforBankAccountNumber) {
            cacheCardReq = CacheCardInfoHelper.createCacheCardRequestForCardPayment(cacheCardRequestBean,
                    InstNetworkType.ISOCARD);
        } else {
            cacheCardReq = CacheCardInfoHelper.createCacheCardRequestForBankAccountNumberPayment(cacheCardRequestBean);
        }
        if (!cacheCardReq.isSuccessfullyProcessed()) {
            throw RequestValidationException.getException(ResultCode.FAILED);
        }
        return cacheCardReq.getResponse();
    }

    private String getSavedCardNumFromCardId(final String cardId, final String paytmSsoToken,
            final InitiateTransactionRequestBody orderDetail) {

        String savedCardNum = null;
        FetchUserDetailsResponse fetchUserDetailsResponse = null;

        boolean isStoreCardEnabled = false;
        boolean isCustIdPresent = false;

        if (orderDetail != null) {
            isStoreCardEnabled = merchantPreferenceService.isStoreCardEnabledForMerchant(orderDetail.getMid());

            if (orderDetail.getUserInfo() != null && StringUtils.isNotBlank(orderDetail.getUserInfo().getCustId())) {
                isCustIdPresent = true;
            }
        }

        try {
            if (StringUtils.isNotBlank(paytmSsoToken)) {
                String clientId = configurationDataService.getPaytmProperty(
                        TheiaConstant.ExtraConstants.OAUTH_CLIENT_ID).getValue();
                String clientSecret = configurationDataService.getPaytmProperty(
                        TheiaConstant.ExtraConstants.OAUTH_CLIENT_SECRET_KEY).getValue();

                fetchUserDetailsResponse = getUserDetailsFromAuthFacadeService(clientId, clientSecret, paytmSsoToken,
                        orderDetail.getMid());

                try {
                    if ((fetchUserDetailsResponse != null) && (fetchUserDetailsResponse.getUserDetails() != null)) {

                        // get saved card number on basis of userId
                        savedCardNum = cashierUtilService.getCardNumer(Long.parseLong(cardId), fetchUserDetailsResponse
                                .getUserDetails().getUserId());
                    }
                } catch (SaveCardValidationException e1) {
                    // this is when card number is not got form
                    // cashierUtilService on the basis of userId
                    if (isStoreCardEnabled && isCustIdPresent) {

                        LOGGER.info("card number not fetched on userId, now fetching on custId Mid...");

                        // get cardnumber by mid custId
                        SavedCardVO saveCardCustMid = cashierUtilService.getSavedCardDetailByCustMid(
                                Long.parseLong(cardId), orderDetail.getUserInfo().getCustId(), orderDetail.getMid());
                        savedCardNum = saveCardCustMid.getCardNumber();
                    }
                }
            } else {
                if (isStoreCardEnabled && isCustIdPresent) {

                    // get cardnumber by mid custId
                    SavedCardVO saveCardCustMid = cashierUtilService.getSavedCardDetailByCustMid(
                            Long.parseLong(cardId), orderDetail.getUserInfo().getCustId(), orderDetail.getMid());
                    savedCardNum = saveCardCustMid.getCardNumber();
                }
            }

        } catch (SaveCardValidationException e2) {
            LOGGER.error("No saved card found for this savedCardId,", e2);
            // this case is when savedCardService does not return a cardnumber,
            // i.e.
            // saveCardId is invalid
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        } catch (Exception e3) {
            LOGGER.error("failed to get Saved cardnumber from savedCard Service, ", e3);
        }

        return savedCardNum;
    }

    private FetchUserDetailsResponse getUserDetailsFromAuthFacadeService(final String clientId,
            final String clientSecret, final String paytmSsoToken, String mid) {
        try {
            final FetchUserDetailsRequest fetchUserDetailsRequest = new FetchUserDetailsRequest(paytmSsoToken,
                    clientId, clientSecret, mid);
            final FetchUserDetailsResponse fetchUserDetailsResponse;
            if (StringUtils.isBlank(fetchUserDetailsRequest.getMid())
                    || ff4jUtils.isFeatureEnabledOnMid(fetchUserDetailsRequest.getMid())) {
                LOGGER.info("oAuth V2 enabled, Mid Available {}",
                        StringUtils.isNotBlank(fetchUserDetailsRequest.getMid()));
                fetchUserDetailsResponse = authFacade.fetchUserDetailsV2(fetchUserDetailsRequest);
            } else
                fetchUserDetailsResponse = authFacade.fetchUserDetails(fetchUserDetailsRequest);

            if (!fetchUserDetailsResponse.isSuccessfullyProcessed()
                    || (fetchUserDetailsResponse.getUserDetails() == null)) {
                LOGGER.error("User details fetching failed or UserDetails is null");
            } else {
                upsHelper.updateUserPostpaidAccStatusFromUPS(mid, fetchUserDetailsResponse.getUserDetails());
            }

            return fetchUserDetailsResponse;

        } catch (final Exception e) {
            LOGGER.error("Exception fetching user details from authFacade : ", e);
        }

        return null;
    }

    private NativeCardIndexNumberServReq validateRequest(NativeFetchCardIndexNumberAPIRequest request) {

        NativeCardIndexNumberServReq servReq = null;

        if (request.getBody() == null || request.getHead() == null) {
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }

        if (StringUtils.isNotBlank(request.getBody().getCardNumber())
                && StringUtils.isNotBlank(request.getBody().getBankAccountNumber())) {
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }

        validateMid(request.getBody().getMid());

        // Currently Checksum and AccessToken are supported
        validateToken(request);

        servReq = new NativeCardIndexNumberServReq();

        validateCardDetails(request, servReq);

        return servReq;
    }

    private void validateMid(String mid) {

        /*
         * No mid validation required for recharge request
         */
        if (StringUtils.isBlank(mid)) {
            return;
        }

        String merchantKey = merchantExtendInfoUtils.getMerchantKey(mid);
        if (isEmpty(merchantKey)) {
            throw RequestValidationException.getException(ResultCode.INVALID_MID);
        }
    }

    private void validateToken(NativeFetchCardIndexNumberAPIRequest request) {

        String validateChecksum = ConfigurationUtil.getProperty("VALIDATE_CHECKSUM_VSC_TP", "Y");

        TokenType tokenType = request.getHead().getTokenType();
        String mid = request.getBody().getMid();
        String referenceId = request.getBody().getReferenceId();
        String token = request.getHead().getToken();

        if (TokenType.CHECKSUM.equals(tokenType)) {
            if ("Y".equals(validateChecksum)) {

                tokenValidationHelper.validateChecksum(token, request.getBody(), mid);
            }
        } else if (TokenType.JWT.equals(tokenType)) {
            verifyJwtToken(request);
        } else if (TokenType.ACCESS.equals(tokenType)) {
            accessTokenUtils.validateAccessToken(mid, referenceId, token);
        } else {
            throw RequestValidationException.getException(ResultCode.INVALID_TOKEN_TYPE_EXCEPTION);
        }
    }

    private void validateCardDetails(NativeFetchCardIndexNumberAPIRequest request, NativeCardIndexNumberServReq servReq) {

        if (StringUtils.isNotBlank(request.getBody().getBankAccountNumber())) {
            return;
        }

        // throw Exception as cardNumber is mandatory
        if (StringUtils.isBlank(request.getBody().getCardNumber())
                || StringUtils.isBlank(request.getBody().getCardExpiry())) {
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }

        // validation of cardNumber, cardnumber ending with pipe is savedCardId
        if (!request.getBody().getCardNumber().endsWith("|")) {
            if (!expressPaymentValidation.validateCardNumber(request.getBody().getCardNumber())) {
                LOGGER.error("Invalid card number");
                throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
            }
        } else {
            servReq.setSavedCardId(true);
        }

        if (!validateCardExpiry(request.getBody().getCardExpiry())) {
            LOGGER.error("Invalid expiry date");
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
    }

    private boolean validateCardExpiry(String cardExpiry) {
        return cardExpiry.matches("(?:[0-9]{2})/[0-9]{4}");
    }

    private NativeFetchCardIndexNumberAPIResponse createResponse(NativeFetchCardIndexNumberAPIRequest request,
            NativeCardIndexNumberServResp servResp) {

        NativeFetchCardIndexNumberAPIResponseBody responseBody = new NativeFetchCardIndexNumberAPIResponseBody();

        if ((servResp == null) || (servResp.getCacheCardResponseBody() == null)
                || !servResp.getCacheCardResponseBody().getResultInfo().getResultCode().equals(BizConstant.SUCCESS)) {
            responseBody.setResultInfo(getResultInfoForSystemError());
        } else {
            responseBody.setCardIndexNumber(servResp.getCacheCardResponseBody().getCardIndexNo());
        }

        return new NativeFetchCardIndexNumberAPIResponse(new ResponseHeader(), responseBody);
    }

    @NotNull
    private ResultInfo getResultInfoForSystemError() {
        ResultInfo result = new ResultInfo(com.paytm.pgplus.common.enums.ResultCode.SYSTEM_ERROR.getResultStatus(),
                com.paytm.pgplus.common.enums.ResultCode.SYSTEM_ERROR.getResultCodeId(),
                com.paytm.pgplus.common.enums.ResultCode.SYSTEM_ERROR.getCode());
        return result;
    }

    private void verifyJwtToken(NativeFetchCardIndexNumberAPIRequest request) {
        Map jwtMap = new HashMap();
        if (StringUtils.isNotBlank(request.getBody().getBankAccountNumber())) {
            jwtMap.put(BANK_ACCOUNT_NUMBER, request.getBody().getBankAccountNumber());
        }
        String signature = request.getHead().getToken();
        String secretKey = environment.getProperty("jwt.recharge.secret.key");
        if (!JWTWithHmacSHA256.verifyJsonWebToken(jwtMap, signature, secretKey)) {
            throw RequestValidationException.getException(ResultCode.INVALID_TOKEN_TYPE_EXCEPTION);
        }
    }

    public static String getPayloadData(Object payloadData) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(payloadData);
        } catch (JsonProcessingException e) {
            return payloadData.toString();
        }
    }
}
