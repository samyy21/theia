package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.risk.RiskConstants;
import com.paytm.pgplus.biz.core.risk.RiskVerifierPayload;
import com.paytm.pgplus.checksum.exception.SecurityException;
import com.paytm.pgplus.checksum.utils.CryptoUtils;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.model.DccPageData;
import com.paytm.pgplus.common.util.ThreadLocalUtil;
import com.paytm.pgplus.facade.payment.models.ChannelAccount;
import com.paytm.pgplus.facade.ppb.models.FetchAccountBalanceResponse;
import com.paytm.pgplus.facade.user.models.response.GenerateLoginOtpResponse;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.request.InitiateTransactionRequest;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.InitiateTransactionResponseBody;
import com.paytm.pgplus.theia.accesstoken.model.request.CreateAccessTokenServiceRequest;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.directpage.NativeDirectBankPageCacheData;
import com.paytm.pgplus.theia.nativ.model.enhancenative.NativeUpiData;
import com.paytm.pgplus.theia.nativ.model.payment.request.NativePaymentRequestBody;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.QrDetail;
import com.paytm.pgplus.theia.nativ.model.token.InitiateTokenBody;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.SessionExpiredException;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.EventUtils;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.paytm.pgplus.biz.utils.BizConstant.ExtendedInfoKeys.SSO_TOKEN;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ENHANCED_VALIDATE_OTP_LIMIT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ENHANCED_VALIDATE_OTP_MAX_COUNT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RedisKeysConstant.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RedisKeysConstant.NativeSession.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.REFERER;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.ORDER_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.TXN_TOKEN;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.WORKFLOW;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.*;

@Component
public class NativeSessionUtil {

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeSessionUtil.class);

    int maxSendOTPLimit = Integer.parseInt(ConfigurationUtil.getProperty("send.otp.limit", "5"));
    int maxVPAValidationLimit = Integer.parseInt(ConfigurationUtil.getProperty(
            TheiaConstant.ExtraConstants.VPA_VALIDATION_LIMIT, "5"));

    public InitiateTokenBody createToken(NativeInitiateRequest request) {
        int txnTokenExpiryInSeconds = getTokenExpiryTime(false);
        String txnId = UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis();
        InitiateTokenBody initiateTokenBody = new InitiateTokenBody();

        if (theiaSessionRedisUtil.setnx(getMidOrderIdKeyForRedis(request.getInitiateTxnReq()), txnId,
                txnTokenExpiryInSeconds)) {
            theiaSessionRedisUtil.hset(txnId, "orderDetail", request, txnTokenExpiryInSeconds);
            if (StringUtils.isNotEmpty(request.getInitiateTxnReq().getBody().getCardHash())) {
                theiaSessionRedisUtil.hset(txnId, TheiaConstant.RequestParams.Native.CARD_HASH, request
                        .getInitiateTxnReq().getBody().getCardHash(), txnTokenExpiryInSeconds);
            }
            // Copy session created on before initiateTxn
            /*
             * Commenting this code because it will already cached
             * fetchPayOptions response is not to be used, instead, call
             * fetchPayOptions API again in v1/ptc
             */
            if (StringUtils.isNotBlank(request.getInitiateTxnReq().getBody().getPaytmSsoToken())) {
                // copyMidSsoSessionToTxnTokenSession(request, txnId);
            }

            // Setting mock flag for shadow request
            if (ThreadLocalUtil.getForMockRequest()) {
                theiaSessionRedisUtil.hset(txnId, "isMockRequest", TRUE, txnTokenExpiryInSeconds);
            }
            initiateTokenBody.setTxnId(txnId);
        } else {
            initiateTokenBody.setTxnId((String) theiaSessionRedisUtil.get(getMidOrderIdKeyForRedis(request
                    .getInitiateTxnReq())));
            initiateTokenBody.setIdempotent(true);
        }
        return initiateTokenBody;
    }

    private void copyMidSsoSessionToTxnTokenSession(NativeInitiateRequest request, String txnId) {
        String tokenForMidSSOFlow = createTokenForMidSSOFlow(request.getInitiateTxnReq());
        NativeCashierInfoResponse nativeCashierInfoResponse = getCashierInfoResponse(tokenForMidSSOFlow);
        Map<String, String> extendInfo = getExtendInfo(tokenForMidSSOFlow);
        UserDetailsBiz userDetailsBiz = getUserDetails(tokenForMidSSOFlow);
        EntityPaymentOptionsTO entityPaymentOptionsTO = getEntityPaymentOptions(tokenForMidSSOFlow);
        if (nativeCashierInfoResponse != null) {
            LOGGER.info("copying nativeCashierInfoResponse to txnToken session");
            setCashierInfoResponse(txnId, nativeCashierInfoResponse);
        }
        if (extendInfo != null) {
            LOGGER.info("copying extendInfo to txnToken session");
            setExtendInfo(txnId, extendInfo);
        }
        if (userDetailsBiz != null) {
            LOGGER.info("copying userDetailsBiz to txnToken session");
            setUserDetails(txnId, userDetailsBiz);
        }
        if (entityPaymentOptionsTO != null) {
            LOGGER.info("copying entityPaymentOptionsTO to txnToken session");
            setEntityPaymentOptions(txnId, entityPaymentOptionsTO);
        }
    }

    public void updateTokenExpiry(String txnToken, String mid, String orderId) {
        int tokenExpiryInSeconds = getTokenExpiryTime(true);
        StringBuilder midOrderIdKey = new StringBuilder("NativeTxnInitiateRequest");
        midOrderIdKey.append(mid).append("_").append(orderId);
        expireKey(midOrderIdKey.toString(), tokenExpiryInSeconds);
        expireKey(txnToken, tokenExpiryInSeconds);
    }

    public int getTokenExpiryTime(boolean isRetry) {
        if (!isRetry) {
            int txnTokenExpiryInSeconds = 900;
            String txnTokenExpiryInSecondsString = ConfigurationUtil.getProperty(
                    "txn.token.expiry.native.payment.seconds", DEFAULT_TXN_TOKEN_EXPIRY_FOR_NATIVE_PAYMENT_IN_SECONDS);
            if (txnTokenExpiryInSecondsString != null && !txnTokenExpiryInSecondsString.isEmpty()) {
                txnTokenExpiryInSeconds = Integer.parseInt(txnTokenExpiryInSecondsString);
            }
            return txnTokenExpiryInSeconds;
        } else {
            // Updated expiry for retry payment
            int tokenExpiry = 600;
            String tokenExpiryString = ConfigurationUtil.getProperty("txn.token.native.payment.retry.expiry.seconds",
                    "600");
            if (tokenExpiryString != null && !tokenExpiryString.isEmpty()) {
                tokenExpiry = Integer.parseInt(tokenExpiryString);
            }
            return tokenExpiry;
        }
    }

    public void updateTokenExpiryForGuestReqInFetchBinDetailApi(String txnToken) {
        int tokenExpiryInSeconds = 900;
        String txnTokenExpiryInSecondsString = ConfigurationUtil.getProperty(
                "txn.token.expiry.for.fetchBinDetailsApi.seconds", "900");
        if (txnTokenExpiryInSecondsString != null && !txnTokenExpiryInSecondsString.isEmpty()) {
            tokenExpiryInSeconds = Integer.parseInt(txnTokenExpiryInSecondsString);
        }
        expireKey(txnToken, tokenExpiryInSeconds);
    }

    public NativeInitiateRequest validate(String txnToken) {
        if (StringUtils.isBlank(txnToken)) {
            throw SessionExpiredException.getException();
        }
        NativeInitiateRequest nativeInitiateRequest = getNativeInitiateRequest(txnToken);
        if (nativeInitiateRequest == null || nativeInitiateRequest.getInitiateTxnReq() == null) {
            throw SessionExpiredException.getException();
        } else {
            return nativeInitiateRequest;
        }
    }

    public boolean checkOTPLimit(String txnToken) {
        int retryCount;
        if (StringUtils.isBlank(txnToken)) {
            throw SessionExpiredException.getException();
        }
        Object obj = fetchField(txnToken, "otpRetryLimit");
        if (obj != null) {
            retryCount = (int) obj;
            if (retryCount >= maxSendOTPLimit) {
                return true;
            }
            saveField(txnToken, "otpRetryLimit", retryCount + 1);
        } else {
            saveField(txnToken, "otpRetryLimit", 1);
        }
        return false;
    }

    public boolean checkEnhanceOTPValidateLimit(String txnToken) {
        int maxEnhancedValidateOTPLimit = Integer.parseInt(ConfigurationUtil.getProperty(ENHANCED_VALIDATE_OTP_LIMIT,
                ENHANCED_VALIDATE_OTP_MAX_COUNT));

        int retryCount;
        if (StringUtils.isBlank(txnToken)) {
            throw SessionExpiredException.getException();
        }
        Object obj = fetchField(txnToken, "validateOtpRetryLimit");
        if (obj != null) {
            retryCount = (int) obj;
            if (retryCount >= maxEnhancedValidateOTPLimit) {
                return true;
            }
            saveField(txnToken, "validateOtpRetryLimit", retryCount + 1);
        } else {
            saveField(txnToken, "validateOtpRetryLimit", 1);
        }
        return false;
    }

    public RiskVerifierPayload validateRiskVerifierToken(String token) {
        if (StringUtils.isBlank(token)) {
            throw SessionExpiredException.getException();
        }
        RiskVerifierPayload riskVerifierPayload = getRiskVerificationData(token);
        if (riskVerifierPayload == null) {
            throw SessionExpiredException.getException();
        } else {
            return riskVerifierPayload;
        }
    }

    public boolean validateMidSSOBasedTxnToken(String txnToken, String ssoToken) {
        if (StringUtils.isBlank(txnToken)) {
            throw SessionExpiredException.getException();
        }
        NativeCashierInfoResponse cashierInfoResponse = getCashierInfoResponse(txnToken);
        if (cashierInfoResponse == null) {
            throw SessionExpiredException.getException();
        }
        String ssoTokenFromCache = getSsoToken(txnToken);
        if (!StringUtils.equals(ssoToken, ssoTokenFromCache)) {
            LOGGER.error("ssoToken provided is different from the one provided in fetchPaymentOption");
            throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                    .isHTMLResponse(false).isRetryAllowed(false).isRedirectEnhanceFlow(true)
                    .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
        }
        return true;
    }

    public NativeInitiateRequest getNativeInitiateRequest(String txnToken) {
        return (NativeInitiateRequest) fetchField(txnToken, "orderDetail");
    }

    public InitiateTransactionRequestBody getOrderDetail(String token) {
        NativeInitiateRequest nativeInitiateRequest = (NativeInitiateRequest) fetchField(token, "orderDetail");
        if (nativeInitiateRequest == null || nativeInitiateRequest.getInitiateTxnReq() == null) {
            throw SessionExpiredException.getException();
        } else {
            return nativeInitiateRequest.getInitiateTxnReq().getBody();
        }
    }

    public void setOrderDetail(String token, InitiateTransactionRequestBody orderDetail, long ttl) {
        NativeInitiateRequest nativeInitiateRequest = new NativeInitiateRequest(orderDetail);
        theiaSessionRedisUtil.hset(token, "orderDetail", nativeInitiateRequest, ttl);
    }

    public Boolean setOrderDetail(String token, InitiateTransactionRequestBody orderDetail) {
        NativeInitiateRequest nativeInitiateRequest = new NativeInitiateRequest(orderDetail);
        return saveField(token, "orderDetail", nativeInitiateRequest);
    }

    public NativeCashierInfoResponse getCashierInfoResponseForContext(String token) {
        return (NativeCashierInfoResponse) fetchField(token, "cashierInfo");
    }

    public NativeCashierInfoResponse getCashierInfoResponse(String token) {
        return (NativeCashierInfoResponse) fetchField(token, "cashierInfo");
    }

    public void setCashierInfoResponse(String token, NativeCashierInfoResponse payOptionResponse) {
        saveField(token, "cashierInfo", payOptionResponse);
    }

    public void setCashierInfoResponseNoInitiateFlow(String token, NativeCashierInfoResponse payOptionResponse) {
        theiaSessionRedisUtil.hset(token, "cashierInfo", payOptionResponse, getTokenExpiryTime(false));
    }

    public GenerateLoginOtpResponse getSendOtpResponse(String token) {
        return (GenerateLoginOtpResponse) fetchField(token, "sendOtpResponse");
    }

    public void setSendOtpResponse(String token, GenerateLoginOtpResponse generateLoginOtpResponse) {
        saveField(token, "sendOtpResponse", generateLoginOtpResponse);
    }

    public void setEntityPaymentOptions(String token, EntityPaymentOptionsTO entityPaymentOptionsTO) {
        saveField(token, "entityPaymentOption", entityPaymentOptionsTO);
    }

    public void setAcquirementIdCreateOrder(String token, String acqId) {
        saveField(token, "acquirementId", acqId);
    }

    public String getAcquirementIdCreateOrder(String token) {
        return (String) fetchField(token, "acquirementId");
    }

    public EntityPaymentOptionsTO getEntityPaymentOptions(String token) {
        return (EntityPaymentOptionsTO) fetchField(token, "entityPaymentOption");
    }

    public Integer getRetryPaymentCount(String token) {
        return (Integer) fetchField(token, "paymentTryCount");
    }

    public void setRetryPaymentCount(String token, Integer count) {
        saveField(token, "paymentTryCount", count);
    }

    public Integer getTotalPaymenCount(String token) {
        return (Integer) fetchField(token, "totalPaymentCount");
    }

    public void setTotalPaymentCount(String token, Integer count) {
        saveField(token, "totalPaymentCount", count);
    }

    public void setTxnId(String token, String txnId) {
        saveField(token, "transactionId", txnId);
    }

    public String getTxnId(String token) {
        return (String) fetchField(token, "transactionId");
    }

    public void setInitiateTxnResponse(String token, InitiateTransactionResponseBody responseBody) {
        saveField(token, "initiateTxnResponse", responseBody);
    }

    public void setPollingStatus(String txnToken, String pollingStatus) {
        saveField(txnToken, "pollingStatus", pollingStatus);
    }

    public String getPollingStatus(String token) {
        return (String) fetchField(token, "pollingStatus");
    }

    public void setRetryDataAndMerchantConfigForCheckOutJs(String txnToken,
            NativePaymentRequestBody nativePaymentRequestBody) {
        saveField(txnToken, RETRY_DATA_MERCHANT_CONFIG, nativePaymentRequestBody);
    }

    public NativePaymentRequestBody getRetryDataAndMerchantConfigForCheckOutJs(String token) {
        return (NativePaymentRequestBody) fetchField(token, RETRY_DATA_MERCHANT_CONFIG);
    }

    public InitiateTransactionResponseBody getInitiateTxnResponse(String token) {
        return (InitiateTransactionResponseBody) fetchField(token, "initiateTxnResponse");
    }

    public Map<String, Object> getEmiSubventionValidationContent(String token) {
        return (Map<String, Object>) getKey(token);
    }

    public String getAggregateMidInCache(String token, String aggMid) {
        return (String) fetchField(token, "aggMid");
    }

    public void setAggregateMidInCache(String txnToken, String aggMid) {
        saveField(txnToken, "aggMid", aggMid);
    }

    public boolean getOrderClosedonCheckoutJS(String token) {
        return BooleanUtils.isTrue((Boolean) fetchField(token, "orderClosedCheckoutJS"));
    }

    public void setOrderClosedonCheckoutJS(String txnToken, boolean orderClosed) {
        saveField(txnToken, "orderClosedCheckoutJS", orderClosed);
    }

    public boolean isDuplicateRequest(InitiateTransactionRequest request) {
        String key = getMidOrderIdKeyForRedis(request);
        Object result = getKey(key);
        return result == null ? false : true;
    }

    public String getMidOrderIdKeyForRedis(InitiateTransactionRequest request) {
        StringBuilder sb = new StringBuilder("NativeTxnInitiateRequest");
        sb.append(request.getBody().getMid()).append("_").append(request.getBody().getOrderId());

        return sb.toString();
    }

    public String getMidOrderIdKeyForRedis(final String mid, final String orderId) {
        StringBuilder sb = new StringBuilder("NativeTxnInitiateRequest");
        sb.append(mid).append("_").append(orderId);
        return sb.toString();
    }

    public void setPostPaidMPinRequired(String token, Boolean isPasscodeRequired) {
        saveField(token, "passcodeRequired", isPasscodeRequired);
    }

    public Boolean getPostPaidMPinRequired(String token) {
        return (Boolean) fetchField(token, "passcodeRequired");
    }

    public void setNativeTxnInProcessFlag(String token, Boolean flag) {
        saveField(token, "nativeTxnInProcessFlag", flag);
    }

    public Boolean getNativeTxnInProcessFlag(String token) {
        return (Boolean) fetchField(token, "nativeTxnInProcessFlag");
    }

    public String isMockRequest(String txnToken) {
        return (String) fetchField(txnToken, "isMockRequest");
    }

    private Boolean saveField(String key, String field, Object value) {
        return theiaSessionRedisUtil.hsetIfExist(key, field, value);
    }

    private Object fetchField(String key, String field) {
        return theiaSessionRedisUtil.hget(key, field);
    }

    // temporary method for shifting native cache from static redis to session
    // redis
    public Object getKey(String key) {
        return theiaSessionRedisUtil.get(key);
    }

    // temporary method for shifting native cache from static redis to session
    // redis
    public void setKey(String key, Object data, long seconds) {
        theiaSessionRedisUtil.set(key, data, seconds);
    }

    // temporary method for shifting native cache from static redis to session
    // redis
    public Boolean expireKey(String key, long seconds) {
        return theiaSessionRedisUtil.expire(key.toString(), seconds);
    }

    public void setUserDetails(String txnToken, UserDetailsBiz userDetails) {
        saveField(txnToken, "userDetails", userDetails);
    }

    public UserDetailsBiz getUserDetails(String token) {
        return (UserDetailsBiz) fetchField(token, "userDetails");
    }

    public void setExtendInfo(String txnToken, Map<String, String> extendedInfo) {
        saveField(txnToken, "extendInfo", extendedInfo);
    }

    public void setLitepayviewCache(String txnToken, Map<String, Object> cachedInfo) {
        saveField(txnToken, "litepayviewCacheInfo", cachedInfo);
    }

    public Map<String, Object> getLitepayviewCacheInfo(String token) {
        return (Map<String, Object>) fetchField(token, "litepayviewCacheInfo");
    }

    public void setAdvanceDepositDetails(String txnToken, ChannelAccount advanceDepositDetails) {
        saveField(txnToken, "advanceDepositDetails", advanceDepositDetails);
    }

    public ChannelAccount getAdvanceDepositDetails(String token) {
        return (ChannelAccount) fetchField(token, "advanceDepositDetails");
    }

    public Map<String, String> getExtendInfo(String token) {
        return (Map<String, String>) fetchField(token, "extendInfo");
    }

    public void deleteKey(String... keys) {
        theiaSessionRedisUtil.del(keys);
    }

    public Integer getKYCRetryCount(String token) {
        return (Integer) fetchField(token, "kycRetryCount");
    }

    public void setKYCRetryCount(String token, Integer count) {
        saveField(token, "kycRetryCount", count);
    }

    public boolean isIdempotentRequest(InitiateTransactionRequestBody request, final String txnId) {

        final InitiateTransactionRequestBody idempotentInitiateRequest = getOrderDetail(txnId);

        if (null != idempotentInitiateRequest.getTxnAmount()) {

            double requestTxnAmount = Double.parseDouble(request.getTxnAmount().getValue());
            double cachedTxnAmount = Double.parseDouble(idempotentInitiateRequest.getTxnAmount().getValue());

            if (requestTxnAmount != cachedTxnAmount) {
                LOGGER.error(
                        "REPEAT_REQUEST_INCONSISTENT, Difference in txn amount, for cache Response {}, for current Request {}",
                        idempotentInitiateRequest.getTxnAmount().getValue(), request.getTxnAmount().getValue());
                return false;
            }
        }
        if (null != idempotentInitiateRequest.getUserInfo()
                && !StringUtils.equals(idempotentInitiateRequest.getUserInfo().getCustId(), request.getUserInfo()
                        .getCustId())) {
            LOGGER.error(
                    "REPEAT_REQUEST_INCONSISTENT, Difference in custId, for cache Response {}, for current Request {}",
                    idempotentInitiateRequest.getUserInfo().getCustId(), request.getUserInfo().getCustId());
            return false;
        }
        if (!idempotentInitiateRequest.isSetSSOViaOptLogin()
                && !StringUtils.equals(idempotentInitiateRequest.getPaytmSsoToken(), request.getPaytmSsoToken())) {
            LOGGER.error("REPEAT_REQUEST_INCONSISTENT, Difference in sso token");
            return false;
        }
        if (!StringUtils.equals(idempotentInitiateRequest.getPromoCode(), request.getPromoCode())) {
            LOGGER.error(
                    "REPEAT_REQUEST_INCONSISTENT, Difference in promocode, for cache Response {}, for current Request {}",
                    idempotentInitiateRequest.getPromoCode(), request.getPromoCode());
            return false;
        }
        if (!StringUtils.equals(idempotentInitiateRequest.getCallbackUrl(), request.getCallbackUrl())
                && BooleanUtils.isFalse(merchantPreferenceService.byPassCallBackUrlInRepeatRequest(request.getMid(),
                        false))) {
            LOGGER.error(
                    "REPEAT_REQUEST_INCONSISTENT, Difference in callbackUrl, for cache Response {}, for current Request {}",
                    idempotentInitiateRequest.getCallbackUrl(), request.getCallbackUrl());
            return false;
        }
        if (!StringUtils.equals(idempotentInitiateRequest.getCardHash(), request.getCardHash())) {
            LOGGER.error(
                    "REPEAT_REQUEST_INCONSISTENT, Difference in cardHash, for cache Response {}, for current Request {}",
                    idempotentInitiateRequest.getCardHash(), request.getCardHash());
            return false;
        }

        // Updating callback URL in cache if merchant preference is enabled to
        // by-pass callback URL check in repeat request inconsistent.
        if (!StringUtils.equals(idempotentInitiateRequest.getCallbackUrl(), request.getCallbackUrl())
                && merchantPreferenceService.byPassCallBackUrlInRepeatRequest(request.getMid(), false)) {
            idempotentInitiateRequest.setCallbackUrl(request.getCallbackUrl());
            setOrderDetail(txnId, idempotentInitiateRequest);
        }
        return true;
    }

    public NativeUpiData getNativeUpiData(String txnToken) {
        return (NativeUpiData) fetchField(txnToken, "nativeUpiData");
    }

    public void setNativeUpiData(String txnToken, NativeUpiData nativeUpiData) {
        saveField(txnToken, "nativeUpiData", nativeUpiData);
    }

    public void setDirectBankPageRenderData(String txnToken, NativeDirectBankPageCacheData cacheData) {
        if (txnToken != null && cacheData != null && cacheData.getBankForm() != null
                && cacheData.getBankForm().getDirectForms() != null) {
            saveField(txnToken, "directBankPageRenderPayload", cacheData);
        }
    }

    public void setDirectBankPageRenderDataAOA(String txnToken, NativeDirectBankPageCacheData cacheData) {
        if (txnToken != null && cacheData != null && cacheData.getBankForm() != null
                && cacheData.getBankForm().getDirectForms() != null) {
            setField(txnToken, "directBankPageRenderPayload", cacheData, getTokenExpiryTime(false));
        }
    }

    public NativeDirectBankPageCacheData getDirectBankPageRenderData(String txnToken) {
        Object obj = fetchField(txnToken, "directBankPageRenderPayload");
        if (obj == null) {
            LOGGER.error("Unable to find directBankPageRenderPayload in cache for token {}", txnToken);
            throw SessionExpiredException.getException();
        }
        return (NativeDirectBankPageCacheData) obj;
    }

    public String createTokenForMidSSOFlow(InitiateTransactionRequest request) {
        return createTokenForMidSSOFlow(request.getBody().getPaytmSsoToken(), request.getBody().getMid());
    }

    public void setLinkId(String txnToken, String linkId) {
        saveField(txnToken, "linkId", linkId);
    }

    public void setInvoiceId(String txnToken, String invoiceId) {
        saveField(txnToken, "invoiceId", invoiceId);
    }

    public void setDirectBankPageSubmitRetryCount(String txnToken, Integer directCount) {
        saveField(txnToken, "directBankPageSubmitRetryCount", directCount);
    }

    public Integer getDirectBankPageSubmitRetryCount(String txnToken) {
        Object count = fetchField(txnToken, "directBankPageSubmitRetryCount");
        if (count == null) {
            return 0;
        }
        return (Integer) fetchField(txnToken, "directBankPageSubmitRetryCount");
    }

    public void setSubsOtpAuthorisedFlag(String txnToken, boolean authorised) {
        saveField(txnToken, "subsOtpAuthorised", authorised);
    }

    public boolean getSubsOtpAuthorisedFlag(String txnToken) {
        return BooleanUtils.isTrue((Boolean) fetchField(txnToken, "subsOtpAuthorised"));
    }

    public void setDirectBankPageResendOtpRetryCount(String txnToken, Integer directCount) {
        saveField(txnToken, "directBankPageResendOtpRetryCount", directCount);
    }

    public Integer getDirectBankPageResendOtpRetryCount(String txnToken) {
        Object count = fetchField(txnToken, "directBankPageResendOtpRetryCount");
        if (count == null) {
            return 1;
        }
        return (Integer) fetchField(txnToken, "directBankPageResendOtpRetryCount");
    }

    public String getLinkId(String txnToken) {
        return (String) fetchField(txnToken, "linkId");
    }

    public String getInvoiceId(String txnToken) {
        return (String) fetchField(txnToken, "invoiceId");
    }

    public void setSsoToken(String txnToken, String ssoToken) {
        saveField(txnToken, "ssoToken", ssoToken);
    }

    public String getSsoToken(String token) {
        return (String) fetchField(token, "ssoToken");
    }

    public String createTokenForMidSSOFlow(String ssoToken, String merchantID) {
        String ssoHash;
        try {
            ssoHash = CryptoUtils.getSHA256(ssoToken);
        } catch (SecurityException e) {
            LOGGER.error("Exception generating hash of SSO token", e);
            throw new NativeFlowException.ExceptionBuilder(ResponseConstants.SYSTEM_ERROR).build();
        }
        return new StringBuilder().append(merchantID).append(ssoHash).toString();
    }

    public String createTokenForGuest(String mid) {
        String token = UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis();
        return new StringBuilder().append("GUEST").append("_").append(token).toString();
    }

    public String createTokenOnMidForGuest(String mid) {
        return new StringBuilder().append("GUEST").append("_").append(mid).toString();
    }

    public String createGuestTokenOnMidForFetchBinDetailApi(String mid) {
        return new StringBuilder().append(FETCH_BIN_DETAIL).append("_").append("GUEST").append("_").append(mid)
                .toString();
    }

    public RiskVerifierPayload getRiskVerificationData(String transId) {
        return (RiskVerifierPayload) getKey(RiskConstants.DO_VIEW_CACHE_KEY_PREFIX + transId);
    }

    public String getCashierRequestId(String txnToken) {
        return (String) fetchField(txnToken, "cashierRequestId");
    }

    public void setCashierRequestId(String txnToken, String cashierRequestId) {
        setField(txnToken, "cashierRequestId", cashierRequestId, 900);
    }

    public String getPaymentTypeId(String txnToken) {
        return (String) fetchField(txnToken, "paymentTypeId");
    }

    public void setPaymentTypeId(String txnToken, String paymentTypeId) {
        saveField(txnToken, "paymentTypeId", paymentTypeId);
    }

    public String getPaymentOption(String txnToken) {
        return (String) fetchField(txnToken, "paymentOption");
    }

    public void setPaymentOption(String txnToken, String paymentOption) {
        saveField(txnToken, "paymentOption", paymentOption);
    }

    public Boolean isSubscriptionAuthorized(String txnToken) {
        return (Boolean) fetchField(txnToken, "isSubscriptionAuthorised");
    }

    public void markSubscriptionAuthorized(String txnToken) {
        saveField(txnToken, "isSubscriptionAuthorised", Boolean.TRUE);
    }

    private String getKeyForHostForOldPG(String mid, String orderId) {
        return mid + "~" + orderId + "~" + "host_old_pg";
    }

    public void setHostForOldPgRequest(String mid, String orderID, String host) {
        setKey(getKeyForHostForOldPG(mid, orderID), host, 900);
    }

    public String getTransId(String txnToken) {
        return (String) fetchField(txnToken, "transId");
    }

    public void setTransId(String txnToken, String transId) {
        theiaSessionRedisUtil.hsetIfExist(txnToken, "transId", transId);
    }

    public String getHostForOldPgRequest(String mid, String orderID) {
        try {
            return (String) getKey(getKeyForHostForOldPG(mid, orderID));
        } catch (Exception e) {
            LOGGER.error("something went wrong while fetching key from redis");
            return null;
        }
    }

    public boolean isRetryPossibleForPaytmExpress(PaymentRequestBean paymentRequestBean) {

        int maxPaymentCount = Integer.parseInt(ConfigurationUtil.getProperty("max.payment.count", "5"));

        String mid = paymentRequestBean.getMid();
        String orderId = paymentRequestBean.getOrderId();
        int count;
        if (StringUtils.isNotBlank(mid) && StringUtils.isNotBlank(orderId)) {
            String key = "PAYTM_EXPRESS_" + mid + "_" + orderId;
            Object value = fetchField(key, "payment_count");
            if (value != null) {
                count = (int) value;
                if (count >= maxPaymentCount) {
                    return false;
                } else {
                    saveField(key, "payment_count", count + 1);
                }
            } else {
                theiaSessionRedisUtil.hset(key, "payment_count", 1, 900);
            }
        }
        return true;
    }

    public void validateGuestTokenForCheckoutFlow(String token) {
        if (StringUtils.isBlank(token)) {
            LOGGER.info("token in blank in checkout flow, throwing SessionExpiredException");
            throw SessionExpiredException.getException();
        }
        boolean isExist = theiaSessionRedisUtil.isExist(token);
        if (!isExist) {
            LOGGER.info("token:{} not present in cache for checkout flow, throwing SessionExpiredException", token);
            throw SessionExpiredException.getException();
        }
    }

    public void setCashierInfoResponseForGuest(NativeCashierInfoRequest request, NativeCashierInfoResponse response) {
        String token = request.getHead().getTxnToken();
        if (StringUtils.isNotBlank(request.getBody().getAccessToken())) {
            theiaSessionRedisUtil.hsetIfExist(token, "cashierInfo", response);
        } else {
            theiaSessionRedisUtil.hset(token, "cashierInfo", response, getTokenExpiryTime(false));
        }
    }

    public void deleteField(String key, String field) {
        theiaSessionRedisUtil.hdel(key, field);
    }

    public void deleteFields(String key, List<String> list) {
        String[] fields = list.toArray(new String[0]);
        theiaSessionRedisUtil.hdel(key, fields);
    }

    public void deleteLoginCookie() {
        String cookieKey = ConfigurationUtil.getProperty("pg.auto.login.cookie.key");
        if (StringUtils.isBlank(cookieKey)) {
            LOGGER.error("Skipping as cookie is not configured in property_file");
            return;
        }
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        Cookie cookie = null;
        Cookie[] cookies = servletRequest.getCookies();
        if (null != cookies) {
            for (Cookie tCookie : cookies) {
                if (cookieKey.equals(tCookie.getName())) {
                    cookie = tCookie;
                    break;
                }
            }
        }

        if (null == cookie) {
            LOGGER.info("cookie not exist");
            return;
        }
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getResponse();

        cookie.setValue(null);
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/theia");

        response.addCookie(cookie);

        LOGGER.info("delete cookie added in response header successfully");
    }

    public void setLoginCookie(String ssoToken) {

        LOGGER.info("Adding cookie in response");

        if (StringUtils.isBlank(ssoToken)) {
            LOGGER.error("Error occurred as required parameter for cookie is blank");
            return;
        }

        String cookieKey = ConfigurationUtil.getProperty("pg.auto.login.cookie.key");
        if (StringUtils.isBlank(cookieKey)) {
            LOGGER.error("Skipping as cookie is not configured in property_file");
            return;
        }

        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getResponse();
        String cookieValue = ssoToken;

        String eCookieValue = null;
        try {
            eCookieValue = CryptoUtils.encryptWithPrimaryKey(cookieValue);

        } catch (Exception e) {
            LOGGER.error("Unable to setCookie, Exception occurred while encrypting CookieValue {} ", e);
            return;
        }

        setCookieInResponse(response, cookieKey, eCookieValue);

        if (cookieKey.equals("pg_login")) {
            EventUtils.pushTheiaEventMessages(EventNameEnum.COOKIE_PG_LOGIN_SUCCESS);
        }

        LOGGER.info("Cookie {} added Successfully", cookieKey);
    }

    private void setCookieInResponse(HttpServletResponse response, String cookieKey, String eCookieValue) {
        String ttl = ConfigurationUtil.getProperty(TheiaConstant.ExtraConstants.AUTO_LOGIN_EXPIRY_TIME, "7776000");
        Cookie cookie = new Cookie(cookieKey, eCookieValue);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(Integer.parseInt(ttl));
        cookie.setPath("/theia");
        response.addCookie(cookie);
    }

    public void setField(String token, String fieldName, Object fieldValue) {
        saveField(token, fieldName, fieldValue);
    }

    public void setField(String token, String fieldName, Object fieldValue, int ttl) {
        theiaSessionRedisUtil.hset(token, fieldName, fieldValue, ttl);
    }

    public boolean isExist(String token) {
        if (StringUtils.isNotBlank(token)) {
            return theiaSessionRedisUtil.isExist(token);
        }
        return false;
    }

    public Object getField(String token, String fieldName) {
        return fetchField(token, fieldName);
    }

    public void setCardMetaDataInCache(String cardIndexNo, String cardExpiry, String maskedCardNo, int expiryCachingTime) {
        theiaSessionRedisUtil.hset(cardIndexNo, CARD_EXPIRY_DETAILS, cardExpiry, expiryCachingTime);
        theiaSessionRedisUtil.hset(cardIndexNo, MASKED_CARD_NO, maskedCardNo, expiryCachingTime);
    }

    public String getMaskCardNumberFromCIN(String cardIndexNo) {
        return (String) fetchField(cardIndexNo, MASKED_CARD_NO);
    }

    public void updateAuthenticatedFlagInCache(String txnToken, boolean authenticatedFlag) {
        if (StringUtils.isNotBlank(txnToken)) {
            InitiateTransactionResponseBody initiateTransactionResponseBody = getInitiateTxnResponse(txnToken);
            if (initiateTransactionResponseBody != null) {
                initiateTransactionResponseBody.setAuthenticated(authenticatedFlag);
                setInitiateTxnResponse(txnToken, initiateTransactionResponseBody);
                LOGGER.info("updated isAuthenticatedFlag={} in initiateTransactionResponseBody in cache",
                        authenticatedFlag);
            }
        }
    }

    public String getTxnToken(String mid, String orderId) {
        StringBuilder midOrderIdKey = new StringBuilder(NATIVE_TXN_INITIATE_REQUEST);
        midOrderIdKey.append(mid).append(UNDER_SCORE).append(orderId);
        return (String) getKey(midOrderIdKey.toString());
    }

    public void setFlowType(String txnToken, String flowType) {
        saveField(txnToken, FLOW_TYPE, flowType);
    }

    public String getFlowTypeOnTxnToken(String txnToken, String mid, String orderId) {
        if (StringUtils.isBlank(txnToken)) {
            txnToken = getTxnToken(mid, orderId);
        }
        return (String) getField(txnToken, FLOW_TYPE);
    }

    public void setScanAndPayFlag(String token, boolean flag) {
        saveField(token, "scanAndPayFlag", flag);
    }

    public boolean getScanAndPayFlag(String token) {
        Object val = fetchField(token, "scanAndPayFlag");
        if (val != null)
            return (boolean) val;
        return false;
    }

    public void cacheEightDigitBinHash(String key, String field, String data) {
        int txnTokenExpiryInSeconds = getTokenExpiryTime(false);
        theiaSessionRedisUtil.hset(key, field, data, txnTokenExpiryInSeconds);
    }

    public void setRefIdOrderIdMapping(String refId, String orderId, String mid, String ssoToken) {
        theiaSessionRedisUtil.hset(refId, MID, mid, 900);
        saveField(refId, SSO_TOKEN, ssoToken);
        saveField(refId, ORDER_ID, orderId);
    }

    public void setAccountBalanceResponseInCache(String txnToken, FetchAccountBalanceResponse accountBalanceResponse) {
        saveField(txnToken, "accountBalanceResponse", accountBalanceResponse);
    }

    public FetchAccountBalanceResponse getAccountBalanceResponseFromCache(String txnToken) {
        return (FetchAccountBalanceResponse) fetchField(txnToken, "accountBalanceResponse");
    }

    public String getCacheKeyForSuperGw(String mid, String referenceId, String requestType) {
        StringBuilder sb = new StringBuilder();
        sb.append(requestType).append("~").append(mid).append("~").append(referenceId);

        return sb.toString();
    }

    public boolean checkVPAValidationLimit(String txnToken) {
        int vpaValidationLimit = 1;
        Object obj = fetchField(txnToken, VPA_VALIDATION_LIMIT);
        if (obj != null) {
            vpaValidationLimit = (int) obj;
            if (vpaValidationLimit >= maxVPAValidationLimit) {
                return true;
            }
            saveField(txnToken, VPA_VALIDATION_LIMIT, vpaValidationLimit + 1);
        } else {
            saveField(txnToken, VPA_VALIDATION_LIMIT, vpaValidationLimit);
        }
        return false;
    }

    public String getOrderIdMappedToRefId(String refId) {
        String key = "REF_ID_" + refId + "_ORDER_ID_MAPPING";
        return (String) getField(key, ORDER_ID);
    }

    public String getWorkflow(String token) {
        return (String) fetchField(token, WORKFLOW);
    }

    public void setWorkflow(String token, String workflow) {
        saveField(token, WORKFLOW, workflow);
    }

    public QrDetail getQrDetail(String token) {
        return (QrDetail) fetchField(token, QR_DETAIL);
    }

    public void setQrDetail(String token, QrDetail qrDetail) {
        saveField(token, QR_DETAIL, qrDetail);
    }

    public void setAccessTokenDetail(String token, CreateAccessTokenServiceRequest createAccessTokenServiceRequest) {
        theiaSessionRedisUtil.hsetIfExist(token, "tokenDetail", createAccessTokenServiceRequest);

    }

    public Integer getPermitsUsedForAPICall(String token, String flagName) {
        Integer permitsUsed = (Integer) theiaSessionRedisUtil.hget(token, flagName);
        if (permitsUsed == null) {
            permitsUsed = 0;
        }
        saveField(token, flagName, ++permitsUsed);
        return permitsUsed;
    }

    public void setDccPageData(String txnToken, DccPageData dccPageData) {
        saveField(txnToken, DCC_PAGE_DATA, dccPageData);

    }

    public DccPageData getDccPageData(String txnToken) {
        return (DccPageData) fetchField(txnToken, DCC_PAGE_DATA);
    }

    public void setRequestParamMapForDcc(String txnToken, Map<String, String[]> NativeRequestMapDcc) {
        saveField(txnToken, NATIVE_REQUEST_MAP_DCC, NativeRequestMapDcc);
    }

    public Map<String, String[]> getRequestParamForDcc(String txnToken) {
        return (Map<String, String[]>) fetchField(txnToken, NATIVE_REQUEST_MAP_DCC);
    }

    public void setRefererURL(String txnToken, String refererURL) {
        saveField(txnToken, REFERER, refererURL);
    }

    public String getRefererURL(String txnToken) {
        return (String) fetchField(txnToken, REFERER);
    }

    public void setTxnTokenAndWorkflowOnMidOrderId(String mid, String orderId, String txnToken, String workflow) {
        theiaSessionRedisUtil.hset(getMidOrderIdKeyForTxnTokenWorkflow(mid, orderId), TXN_TOKEN, txnToken,
                getTokenExpiryTime(false));
        theiaSessionRedisUtil.hset(getMidOrderIdKeyForTxnTokenWorkflow(mid, orderId), WORKFLOW, workflow,
                getTokenExpiryTime(false));
    }

    public String getTxnTokenAndWorkflowOnMidOrderId(String key, String field) {
        return (String) fetchField(key, field);
    }

    public String getMidOrderIdKeyForTxnTokenWorkflow(String mid, String orderId) {
        StringBuilder sb = new StringBuilder(MID_ORDER_TXN_TOKEN_WORKFLOW);
        sb.append(mid).append("_").append(orderId);
        return sb.toString();
    }

    public void setIsEdcRequest(String token, String isEdcRequest) {
        theiaSessionRedisUtil.hset(token, "isEdcRequest", isEdcRequest, 900);
    }

    public String getIsEdcRequest(String token) {
        return (String) fetchField(token, "isEdcRequest");
    }

    public String getFieldValue(String key, String field) {
        return (String) fetchField(key, field);
    }

    public void setFieldAndkey(String key, String field, String value) {
        setField(key, field, value, getTokenExpiryTime(false));
    }

    public void setPOSOrderExtendInfo(String mid, String orderId, Map<String, String> extendInfo) {
        setKey(generateKey(mid, orderId), extendInfo, 900);
    }

    private String generateKey(String mid, String orderId) {
        return "POS_ORDER_" + mid + orderId;
    }

    public Map<String, String> getPOSOrderExtendInfo(String mid, String orderId) {
        Object value = getKey(generateKey(mid, orderId));
        return value == null ? null : (Map<String, String>) value;
    }

    public long fetchTTL(String key) {
        return theiaSessionRedisUtil.ttl(key);
    }

    public void setPaymentRequestBeanAgainstEsn(String esn, PaymentRequestBean requestBean, long ttl) {
        theiaSessionRedisUtil.hset(esn, "paymentRequestBean", requestBean, ttl);
    }

    public PaymentRequestBean getPaymentRequestBeanAgainstEsn(String esn) {
        PaymentRequestBean paymentRequestBean = (PaymentRequestBean) fetchField(esn, "paymentRequestBean");
        return paymentRequestBean;
    }

    public String getMidOrderIdToken(String mid, String orderId) {
        return mid + orderId;
    }

    public void setLinkIdForQR(String token, String linkId) {
        theiaSessionRedisUtil.hset(token, QR_LINK_ID, linkId, getTokenExpiryTime(false));
    }

    public void setInvoiceIdForQR(String token, String invoiceId) {
        theiaSessionRedisUtil.hset(token, QR_INVOICE_ID, invoiceId, getTokenExpiryTime(false));
    }

    public String getLinkIdForQR(String txnToken) {
        return (String) fetchField(txnToken, QR_LINK_ID);
    }

    public String getInvoiceIdForQR(String txnToken) {
        return (String) fetchField(txnToken, QR_INVOICE_ID);
    }

}