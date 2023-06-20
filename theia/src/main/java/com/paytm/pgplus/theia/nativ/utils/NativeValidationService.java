package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.risk.RiskVerifierPayload;
import com.paytm.pgplus.checksum.ValidateChecksum;
import com.paytm.pgplus.checksum.exception.SecurityException;
import com.paytm.pgplus.checksum.model.CheckSumInput;
import com.paytm.pgplus.checksum.utils.CryptoUtils;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.enums.TxnType;
import com.paytm.pgplus.models.ExtendInfo;
import com.paytm.pgplus.models.SimplifiedPaymentOffers;
import com.paytm.pgplus.pgproxycommon.models.MappingMerchantUrlInfo;
import com.paytm.pgplus.pgproxycommon.models.MerchantUrlInput;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.promo.service.client.model.PromoCodeResponse;
import com.paytm.pgplus.request.InitiateTransactionRequest;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.accesstoken.model.request.CreateAccessTokenServiceRequest;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.cache.IMerchantUrlService;
import com.paytm.pgplus.theia.constants.ResponseCodeConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.helper.PGPreferenceHelper;
import com.paytm.pgplus.theia.logging.ExceptionLogUtils;
import com.paytm.pgplus.theia.nativ.IOAuthHelper;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.token.UpdateTransactionDetailRequest;
import com.paytm.pgplus.theia.nativ.promo.IPromoHelper;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.*;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.EventUtils;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import com.paytm.pgplus.theia.utils.TheiaRequestValidator;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isEmpty;

@Service("nativeValidationService")
public class NativeValidationService implements INativeValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeValidationService.class);

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    private IOAuthHelper oAuthHelper;

    @Autowired
    @Qualifier("promoHelperImpl")
    private IPromoHelper nativePromoHelper;

    @Autowired
    private TokenValidationHelper tokenValidationHelper;

    @Autowired
    @Qualifier("merchantUrlService")
    private IMerchantUrlService merchantUrlService;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Autowired
    private PGPreferenceHelper pgPreferenceHelper;

    @Autowired
    private AOAUtils aoaUtils;

    @Override
    public void validateMidOrderId(String mid, String orderId) {
        String requestMid = MDC.get(TheiaConstant.RequestParams.MID);
        String requestOrderId = MDC.get(TheiaConstant.RequestParams.ORDER_ID);
        if (!mid.equals(requestMid)) {
            throw MidDoesnotMatchException.getException();
        } else if (!orderId.equals(requestOrderId)) {
            throw OrderIdDoesnotMatchException.getException();
        }
    }

    @Override
    public void validateMidOrderIdinRequest(String mid, String orderId, String requestMid, String requestOrderId) {
        if (!mid.equals(requestMid)) {
            throw MidDoesnotMatchException.getException();
        } else if (!orderId.equals(requestOrderId)) {
            throw OrderIdDoesnotMatchException.getException();
        }
    }

    @Override
    public InitiateTransactionRequestBody validateTxnToken(String txnToken) {
        NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.validate(txnToken);
        InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
        return orderDetail;
    }

    @Override
    public void validateChecksum(InitiateTransactionRequest request) {
        try {
            String mid = request.getBody().getMid();
            if (merchantPreferenceService.isChecksumEnabled(mid)) {
                // LOGGER.info("Checksum validation is enabled");
                String merchantKey = getMerchantKey(request.getBody().getMid(), request.getHead().getClientId());
                Map<String, String> checksumParamMap = getChecksumParams(request);
                String paytmChecksumString = ValidateChecksum.getInstance().getPaytmChecksumStringV2(checksumParamMap);
                LOGGER.info("Paytm Checksum String is : {} ", paytmChecksumString);
                CheckSumInput checkSumInput = new CheckSumInput();
                checkSumInput.setMerchantChecksumHash(request.getHead().getSignature());
                checkSumInput.setPaytmChecksumHash(paytmChecksumString);
                checkSumInput.setMerchantKey(merchantKey);
                if (!ValidateChecksum.getInstance().verifyCheckSum(checkSumInput)) {
                    throw RequestValidationException.getException(ResultCode.INVALID_CHECKSUM);
                }
            } else {
                LOGGER.info("Checksum validation is disabled");
                return;
            }
        } catch (SecurityException e) {
            LOGGER.error("Exception : ", e);
            throw RequestValidationException.getException(ResultCode.INVALID_CHECKSUM);
        }
        LOGGER.info("Checksum validation successfully");
    }

    @Override
    public CreateAccessTokenServiceRequest validateAccessToken(NativeCashierInfoRequest request) {
        String referenceId = request.getBody().getReferenceId();
        String mid = request.getBody().getMid();
        return accessTokenUtils.validateAccessToken(mid, referenceId, request.getHead().getToken());
    }

    private String getMerchantKey(String mid, String clientId) {
        String merchantKey = merchantExtendInfoUtils.getMerchantKey(mid, clientId);
        if (isEmpty(merchantKey)) {
            throw RequestValidationException.getException(ResultCode.INVALID_MID);
        }
        return merchantKey;
    }

    private Map<String, String> getChecksumParams(InitiateTransactionRequest request) {
        Map<String, String> checksumParamMap = new HashMap<String, String>();
        checksumParamMap.put("requestType", request.getBody().getRequestType());
        checksumParamMap.put("mid", request.getBody().getMid());
        checksumParamMap.put("orderId", request.getBody().getOrderId());
        checksumParamMap.put("websiteName", request.getBody().getWebsiteName());
        checksumParamMap.put("txnAmount", request.getBody().getTxnAmount().getValue());
        checksumParamMap.put("custId", request.getBody().getUserInfo().getCustId());
        checksumParamMap.put("paytmSsoToken", request.getBody().getPaytmSsoToken());
        return checksumParamMap;
    }

    @Override
    public NativePersistData validate(NativeInitiateRequest request) {
        validateMandatoryParam(request.getInitiateTxnReq());
        validateMidOrderId(request.getInitiateTxnReq().getBody().getMid(), request.getInitiateTxnReq().getBody()
                .getOrderId());
        validateTxnAmount(request.getInitiateTxnReq().getBody().getTxnAmount().getValue());
        validatePromoOfferRequest(request.getInitiateTxnReq());
        if (!isEmpty(request.getInitiateTxnReq().getBody().getPaytmSsoToken())) {
            try {
                UserDetailsBiz userDetailsBiz = getUserDetails(request.getInitiateTxnReq().getBody());
                return new NativePersistData(userDetailsBiz);
            } catch (PaymentRequestProcessingException e) {
                throw new PaymentRequestValidationException(e.getResultInfo());
            }
        }
        return null;
    }

    @Override
    public UserDetailsBiz getUserDetails(InitiateTransactionRequestBody body) {
        /**
         * for corporate advance Request checking feasibilty
         */
        return oAuthHelper.validateSSOToken(body);
    }

    @Override
    public void validate(InitiateTransactionRequest request) {
        validateMandatoryParam(request);
        validateMidOrderId(request.getBody().getMid(), request.getBody().getOrderId());
        if (request.getBody().getRequestType().contains("SUBSCRIPTION")
                || request.getBody().getRequestType().contains("SUBSCRIBE")
                || request.getBody().getRequestType().contains("MF_SIP")) {
            validateTxnAmountForSubscription(request.getBody().getTxnAmount().getValue());
        } else {
            validateTxnAmount(request.getBody().getTxnAmount().getValue());
        }
        if (!isEmpty(request.getBody().getPaytmSsoToken())) {
            validateSSOToken(request.getBody().getPaytmSsoToken(), request.getBody().getMid());
        }
    }

    private void validateMandatoryParam(InitiateTransactionRequest request) {
        if (isEmpty(request.getBody().getRequestType()) || isEmpty(request.getBody().getMid())
                || isEmpty(request.getBody().getOrderId()) || null == request.getBody().getTxnAmount()
                || isEmpty(request.getBody().getTxnAmount().getValue())
                || isEmpty(request.getBody().getUserInfo().getCustId())) {
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        } else if ((request.getBody().getRequestType()).contains("|")
                || (request.getBody().getMid().contains("|"))
                || (StringUtils.isNotBlank(request.getBody().getWebsiteName()) && request.getBody().getWebsiteName()
                        .contains("|")) || (request.getBody().getTxnAmount().getValue().contains("|"))
                || request.getBody().getTxnAmount().getValue().contains("'")) {
            throw RequestValidationException.getException(ResultCode.PIPE_CHAR_IS_NOT_ALLOWED);
        } else if (request.getBody().getCcBillPayment() != null
                && StringUtils.isBlank(request.getBody().getCcBillPayment().getCcBillNo())) {
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        } else if (isEmpty(request.getBody().getbId()) && !isEmpty(request.getBody().getCorporateCustId())) {
            LOGGER.error("BID is mandatory with corporate custID ");
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }
        validateWebsite(request);

        if (request.getBody().getPayableAmount() != null && isEmpty(request.getBody().getPayableAmount().getValue())) {
            LOGGER.error("PayableAmount value is empty");
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }
    }

    private void validateWebsite(InitiateTransactionRequest request) {
        // validation for callback url
        if (StringUtils.isBlank(request.getBody().getCallbackUrl())
                && StringUtils.isBlank(request.getBody().getWebsiteName())) {
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }

        // validation for peon url
        boolean merchantPeonable = merchantExtendInfoUtils.isPeonEnabled(request.getBody().getMid())
                || merchantExtendInfoUtils.isCallbackEnabled(request.getBody().getMid())
                || merchantPreferenceService.isCustomTransactionWebhookUrlEnabled(request.getBody().getMid());

        if (merchantPeonable && StringUtils.isBlank(request.getBody().getPEON_URL())
                && StringUtils.isBlank(request.getBody().getWebsiteName())) {
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }

        // set call back/peon url
        setCallBackOrPeonUrl(request, merchantPeonable);

    }

    private void setCallBackOrPeonUrl(InitiateTransactionRequest request, boolean merchantPeonable) {
        if (StringUtils.isBlank(request.getBody().getCallbackUrl())
                || (merchantPeonable && StringUtils.isBlank(request.getBody().getPEON_URL()))) {

            if (TxnType.AUTH.equals(request.getBody().getTxnType())
                    || TxnType.ESCROW.equals(request.getBody().getTxnType())) {
                if (setCallBackOrPeonUrlForPreAuth(request, merchantPeonable)) {
                    return;
                }
            }
            try {
                final MerchantUrlInput input = new MerchantUrlInput(request.getBody().getMid(),
                        MappingMerchantUrlInfo.UrlTypeId.RESPONSE, request.getBody().getWebsiteName());
                MappingMerchantUrlInfo merchantUrlInfo = merchantUrlService.getMerchantUrlInfo(input);
                if (merchantUrlInfo != null) {
                    if (StringUtils.isBlank(request.getBody().getCallbackUrl())) {
                        request.getBody().setCallbackUrl(merchantUrlInfo.getPostBackurl());
                        if (request.getBody().getExtendInfo() == null) {
                            request.getBody().setExtendInfo(new ExtendInfo());
                        }
                        request.getBody().getExtendInfo().setIsCallbackWebsiteDerived("true");
                        // adding logger to find faulty configuration cases
                        if (StringUtils.isBlank(merchantUrlInfo.getPostBackurl())) {
                            LOGGER.error("EMPTY_CALLBACK_URL for website {}", request.getBody().getWebsiteName());
                        }
                    }
                    if (merchantPeonable && StringUtils.isBlank(request.getBody().getPEON_URL())) {
                        request.getBody().setPEON_URL(merchantUrlInfo.getNotificationStatusUrl());
                        // adding logger to find faulty configuration cases
                        if (StringUtils.isBlank(merchantUrlInfo.getNotificationStatusUrl())) {
                            LOGGER.error("EMPTY_PEON_URL for website {}", request.getBody().getWebsiteName());
                        }
                    }
                } else {
                    throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
                }
            } catch (PaymentRequestValidationException e) {
                throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
            }
        }
    }

    private boolean setCallBackOrPeonUrlForPreAuth(InitiateTransactionRequest request, boolean merchantPeonable) {
        final MerchantUrlInput input = new MerchantUrlInput(request.getBody().getMid(),
                MappingMerchantUrlInfo.UrlTypeId.PAY_CONFIRM, "PAYTM");
        try {
            MappingMerchantUrlInfo merchantUrlInfo = merchantUrlService.getMerchantUrlInfo(input);
            if (merchantUrlInfo != null) {
                if (StringUtils.isBlank(request.getBody().getCallbackUrl())) {
                    request.getBody().setCallbackUrl(merchantUrlInfo.getPostBackurl());
                }
                if (merchantPeonable && StringUtils.isBlank(request.getBody().getPEON_URL())) {
                    request.getBody().setPEON_URL(merchantUrlInfo.getNotificationStatusUrl());
                }
            } else {
                LOGGER.error("Callback or Peon Data is null for website PAYTM and txn PreAuth");
                return false;
            }
        } catch (PaymentRequestValidationException e) {
            LOGGER.error("Callback or Peon not set for website PAYTM and txn PreAuth");
            return false;
        }
        return true;
    }

    @Override
    public void validateTxnAmount(String amount) {
        try {
            TheiaRequestValidator.validateAmount(amount);
        } catch (TheiaDataMappingException e) {
            throw RequestValidationException.getException(e, ResultCode.INVALID_TXN_AMOUNT);
        }
    }

    @Override
    public void validateTxnAmountForSubscription(String amount) {
        try {
            TheiaRequestValidator.validateAmountForSubscription(amount);
        } catch (TheiaDataMappingException e) {
            throw RequestValidationException.getException(e, ResultCode.INVALID_TXN_AMOUNT);
        }
    }

    @Override
    @Deprecated
    public UserDetailsBiz validateSSOToken(String paytmSsoToken) {
        /**
         * forward compatabilty for corporate Advance Deposit
         */
        return oAuthHelper.validateSSOToken(paytmSsoToken);
    }

    @Override
    public UserDetailsBiz validateSSOToken(String paytmSsoToken, String mid) {
        /**
         * forward compatabilty for corporate Advance Deposit
         */
        return oAuthHelper.validateSSOToken(paytmSsoToken, mid);
    }

    private boolean isDuplicateRequest(InitiateTransactionRequest request) {
        return nativeSessionUtil.isDuplicateRequest(request);
    }

    @Override
    public boolean validatePromoCode(InitiateTransactionRequest request) {

        if (StringUtils.isBlank(request.getBody().getPromoCode())) {
            return false;
        }
        PromoCodeResponse promoCodeResponse = nativePromoHelper.validatePromoCode(request.getBody().getPromoCode(),
                request.getBody().getMid());
        if (promoCodeResponse != null
                && ResponseCodeConstant.PROMO_SUCCESS.equals(promoCodeResponse.getPromoResponseCode())) {
            return true;
        }
        return false;
    }

    @Override
    public void validateUpdateTxnDetail(UpdateTransactionDetailRequest request) {

        if (BooleanUtils.isTrue(nativeSessionUtil.getNativeTxnInProcessFlag(request.getHead().getTxnToken()))) {
            throw RequestValidationException.getException(ResultCode.TXN_ALREADY_IN_PROCESS);
        }
        if (request.getBody().getTxnAmount() != null) {
            validateTxnAmount(request.getBody().getTxnAmount().getValue());
        }
    }

    @Override
    public UserDetailsBiz validateLoginViaCookie(String mid) {

        LOGGER.info("Validating Login Via Cookie");
        boolean isPgAutoLoginEnabled = pgPreferenceHelper.checkPgAutologinEnabledFlag(mid);

        if (!isPgAutoLoginEnabled) {
            LOGGER.info("Pg Auto Login is not enabled on merchant");
            EventUtils.pushTheiaEventMessages(EventNameEnum.AUTOLOGIN_NOT_ENABLED);
            return null;
        }

        String ssoToken = null;
        UserDetailsBiz userDetailsBiz = null;

        try {

            String requestCookieValue = getCookie();

            if (StringUtils.isBlank(requestCookieValue)) {
                LOGGER.info("Skipping auto-login as cookie does not exist");
                return null;
            }

            ssoToken = CryptoUtils.decryptWithPrimaryAndSecondaryKey(requestCookieValue);

            LOGGER.info("Validating sso token from Cookie");

            userDetailsBiz = validateSSOToken(ssoToken, mid);
            userDetailsBiz.setUserToken(ssoToken);

        } catch (SecurityException | RuntimeException e) {
            // LOGGER.error("Exception occurred while decrypting cookie", e);
            LOGGER.error("Exception occurred while decrypting cookie", ExceptionLogUtils.limitLengthOfStackTrace(e));
            EventUtils.pushTheiaEventMessages(EventNameEnum.COOKIE_DECRYPTION_ERROR);
            return null;
        } catch (Exception e) {
            // LOGGER.error("Exception occurred while validating SSO Token for login via cookie",
            // e);
            LOGGER.error("Exception occurred while validating SSO Token for login via cookie",
                    ExceptionLogUtils.limitLengthOfStackTrace(e));
            EventUtils.pushTheiaEventMessages(EventNameEnum.SSO_COOKIE_VALIDATION_EXCEPTION);
            return null;
        }

        LOGGER.info("Login via cookie validation is successful");
        EventUtils.pushTheiaEventMessages(EventNameEnum.LOGIN_COOKIE_VALIDATION_SUCCESS);

        return userDetailsBiz;
    }

    @Override
    public void validateMid(String mid) {
        String requestMid = MDC.get(TheiaConstant.RequestParams.MID);
        if (mid == null || !mid.equals(requestMid)) {
            throw MidDoesnotMatchException.getException();
        }
    }

    @Override
    public void validateRiskDoViewRequest(String token) {
        RiskVerifierPayload riskVerifierPayload = nativeSessionUtil.getRiskVerificationData(token);
        if (riskVerifierPayload == null) {
            LOGGER.error("Invalid risk doView Request");
            throw RequestValidationException.getException(ResultCode.INVALID_DO_VIEW_REQUEST);
        }
    }

    @Override
    public void validateRiskDoVerifyRequest(String token) {
        RiskVerifierPayload riskVerifierPayload = nativeSessionUtil.getRiskVerificationData(token);
        if (riskVerifierPayload == null) {
            LOGGER.error("Invalid risk doVerify Request");
            throw RequestValidationException.getException(ResultCode.INVALID_DO_VERIFY_REQUEST);
        }
    }

    @Override
    public RiskVerifierPayload validateRiskVerifierToken(String token) {
        RiskVerifierPayload riskVerifierPayload = nativeSessionUtil.validateRiskVerifierToken(token);
        return riskVerifierPayload;
    }

    @Override
    public boolean validateTransid(String transId, String txnToken) {
        String transIdFromCache = nativeSessionUtil.getTxnId(txnToken);
        if (StringUtils.isBlank(transIdFromCache)) {
            throw SessionExpiredException.getException();
        }
        return transIdFromCache.equals(transId);
    }

    private String getCookie() {
        String cookieKey = ConfigurationUtil.getProperty("pg.auto.login.cookie.key");
        if (StringUtils.isBlank(cookieKey)) {
            LOGGER.error("Skipping as cookie is not configured in property_file");
            return null;
        }
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        Cookie[] cookies = servletRequest.getCookies();
        if (cookies == null) {
            return null;
        }
        if (null != cookies) {
            for (Cookie cookie : cookies) {
                if (cookieKey.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private void validatePromoOfferRequest(InitiateTransactionRequest request) {
        Long transAmount = null;
        Long promoAmount = null;
        if (request.getBody().getPaymentOffersApplied() != null
                && request.getBody().getSimplifiedPaymentOffers() != null) {
            throw RequestValidationException.getException(ResultCode.INVALID_PROMO_PARAM);
        } else if (request.getBody().getSimplifiedPaymentOffers() != null) {
            // handling for cases with promo code as empty or includes only
            // spaces
            if (StringUtils.isBlank(request.getBody().getSimplifiedPaymentOffers().getPromoCode())) {
                request.getBody().getSimplifiedPaymentOffers().setPromoCode(null);
            }
            SimplifiedPaymentOffers paymentOffers = request.getBody().getSimplifiedPaymentOffers();
            if (StringUtils.isEmpty(paymentOffers.getPromoCode()) && !paymentOffers.isApplyAvailablePromo()) {
                throw RequestValidationException.getException(ResultCode.INVALID_PROMO_PARAM);
            }
            if (StringUtils.isNotBlank(paymentOffers.getPromoAmount())) {
                try {
                    transAmount = request.getBody().getTxnAmount() != null ? Long.valueOf(AmountUtils
                            .getTransactionAmountInPaise(request.getBody().getTxnAmount().getValue())) : null;
                    promoAmount = Long.valueOf(AmountUtils.getTransactionAmountInPaise(request.getBody()
                            .getSimplifiedPaymentOffers().getPromoAmount()));
                } catch (Exception e) {
                    LOGGER.error("invalid values for trans/promo amount :{},{}",
                            (request.getBody().getTxnAmount() != null) ? request.getBody().getTxnAmount().getValue()
                                    : null, request.getBody().getSimplifiedPaymentOffers().getPromoAmount());
                }
                if (transAmount != null && promoAmount != null && promoAmount > transAmount) {
                    throw RequestValidationException.getException(ResultCode.INVALID_PROMO_AMOUNT);
                }
            }
        } else if (request.getBody().getPaymentOffersApplied() != null) {
            try {
                transAmount = request.getBody().getTxnAmount() != null ? Long.valueOf(AmountUtils
                        .getTransactionAmountInPaise(request.getBody().getTxnAmount().getValue())) : null;
                promoAmount = Long.valueOf(AmountUtils.getTransactionAmountInPaise(request.getBody()
                        .getPaymentOffersApplied().getTotalTransactionAmount()));
            } catch (Exception e) {
                LOGGER.error(
                        "invalid values for trans/promo amount :{},{}",
                        (request.getBody().getTxnAmount() != null) ? request.getBody().getTxnAmount().getValue() : null,
                        request.getBody().getPaymentOffersApplied().getTotalTransactionAmount());
            }
            if (transAmount != null && promoAmount != null && promoAmount > transAmount) {
                throw RequestValidationException.getException(ResultCode.INVALID_PROMO_AMOUNT);
            }
        }
    }

    public boolean validateRequest(TokenType tokenType, String token, Object reqBody, String mid) {
        if (tokenType == null) {
            LOGGER.info("tokenType is null");
            return false;
        }

        boolean valid = false;

        try {
            if (TokenType.TXN_TOKEN == tokenType) {
                nativeSessionUtil.validate(token);
                valid = true;
            }

            if (TokenType.SSO == tokenType) {
                if (oAuthHelper.validateSSOToken(token) == null) {
                    valid = false;
                }
                valid = true;
            }

            if (TokenType.CHECKSUM == tokenType) {
                tokenValidationHelper.validateChecksum(token, reqBody, mid);
                valid = true;
            }
        } catch (Exception e) {
            LOGGER.info("token not valid, ", e);
            valid = false;
        }

        return valid;
    }

}