package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.common.enums.Channel;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.user.models.request.ValidateLoginOtpRequest;
import com.paytm.pgplus.facade.user.models.response.GenerateLoginOtpResponse;
import com.paytm.pgplus.facade.user.models.response.ValidateLoginOtpResponse;
import com.paytm.pgplus.facade.user.services.IAuthentication;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.InitiateTransactionResponse;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.helper.PGPreferenceHelper;
import com.paytm.pgplus.theia.nativ.model.auth.EnhancedValidateOtpResponse;
import com.paytm.pgplus.theia.nativ.model.auth.EnhancedValidateOtpResponseBody;
import com.paytm.pgplus.theia.nativ.model.auth.ValidateOtpRequest;
import com.paytm.pgplus.theia.nativ.model.common.EnhanceCashierPageCachePayload;
import com.paytm.pgplus.theia.nativ.model.common.EnhancedCashierPage;
import com.paytm.pgplus.theia.nativ.model.common.EnhancedCashierPageDynamicQR;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.subscription.INativeSubscriptionHelper;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.exceptions.SessionExpiredException;
import com.paytm.pgplus.theia.services.helper.EnhancedCashierPageServiceHelper;
import com.paytm.pgplus.theia.services.impl.EnhancedCashierPageServiceImpl;
import com.paytm.pgplus.theia.utils.EventUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import static com.paytm.pgplus.theia.constants.TheiaConstant.PaytmPropertyConstants.IS_ORDER_ID_ENABLE_FOR_OTPS;

@Service("enhancedValidateOtpRequestProcessor")
public class EnhancedValidateOtpRequestProcessor
        extends
        AbstractRequestProcessor<ValidateOtpRequest, EnhancedValidateOtpResponse, ValidateLoginOtpRequest, EnhancedValidateOtpResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnhancedValidateOtpRequestProcessor.class);
    @Autowired
    @Qualifier("authenticationImpl")
    private IAuthentication authService;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    EnhancedCashierPageServiceHelper enhancedCashierPageServiceHelper;

    @Autowired
    @Qualifier("enhancedCashierPageService")
    EnhancedCashierPageServiceImpl enhancedCashierPageService;

    @Autowired
    private NativePayviewConsultRequestProcessor nativePayviewConsultRequestProcessor;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("nativeSubscriptionHelper")
    private INativeSubscriptionHelper nativeSubscriptionHelper;

    @Autowired
    private PGPreferenceHelper pgPreferenceHelper;

    @Override
    protected ValidateLoginOtpRequest preProcess(ValidateOtpRequest request) {
        InitiateTransactionRequestBody orderDetail = nativeValidationService.validateTxnToken(request.getHead()
                .getTxnToken());
        validate(request, orderDetail);
        boolean isOrderIdEnableForOTPs = Boolean.parseBoolean(com.paytm.pgplus.biz.utils.ConfigurationUtil
                .getTheiaProperty(IS_ORDER_ID_ENABLE_FOR_OTPS, "false"));
        ValidateLoginOtpRequest serviceRequest = null;
        if (isOrderIdEnableForOTPs) {
            serviceRequest = requestTransform(request, orderDetail.getOrderId());
        } else {
            serviceRequest = requestTransform(request, null);
        }
        return serviceRequest;
    }

    @Override
    protected EnhancedValidateOtpResponse onProcess(ValidateOtpRequest request, ValidateLoginOtpRequest serviceRequest)
            throws Exception {

        ValidateLoginOtpResponse validateLoginOtpResponse = null;
        EnhancedValidateOtpResponse response = null;

        boolean validateOTPLimitBreached = checkValidateOtpLimitBreached(request);

        if (validateOTPLimitBreached) {
            validateLoginOtpResponse = createOtpValidationLimitExceededResponse();
            response = getAutoLoginValidateOtpResponse(validateLoginOtpResponse);
            return response;
        }

        try {
            validateLoginOtpResponse = authService.validateLoginOtp(serviceRequest);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occured while validating login OTP", e);
            validateLoginOtpResponse = new ValidateLoginOtpResponse();
            validateLoginOtpResponse.setMessage(TheiaConstant.ExtraConstants.OTP_VALIDATION_EXCEPTION_MESSAGE);
            validateLoginOtpResponse.setStatus(TheiaConstant.ResponseConstants.ResponseCodes.SC_INTERNAL_SERVER_ERROR);
            response = getAutoLoginValidateOtpResponse(validateLoginOtpResponse);
            return response;
        }

        if (TheiaConstant.ResponseConstants.AUTH_SERVICE_OTP_LIMIT_BREACH_CODE.equals(validateLoginOtpResponse
                .getResponseCode())) {
            validateLoginOtpResponse = createOtpValidationLimitExceededResponse();
        }

        response = getAutoLoginValidateOtpResponse(validateLoginOtpResponse);

        if (validateLoginOtpResponse.getResponseData() != null
                && !CollectionUtils.isEmpty(validateLoginOtpResponse.getResponseData().getAccess_token())) {

            NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.validate(request.getHead().getTxnToken());
            InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();

            populateResponseData(validateLoginOtpResponse, response, orderDetail, request.getHead().getTxnToken());

            if (ERequestType.NATIVE_SUBSCRIPTION.getType().equals(orderDetail.getRequestType())
                    && merchantPreferenceService.isSubscriptionLimitOnWalletEnabled(orderDetail.getMid())) {
                nativeSubscriptionHelper.markSubscriptionAuthorized(request.getHead().getTxnToken(), orderDetail,
                        validateLoginOtpResponse);
            }

            response.getBody().getResultInfo().setResultStatus("S");

            boolean isPgAutoLoginEnabled = pgPreferenceHelper.checkPgAutologinEnabledFlag(orderDetail.getMid());

            if (request.getBody().isRememberMe() && isPgAutoLoginEnabled) {
                nativeSessionUtil.setLoginCookie(orderDetail.getPaytmSsoToken());
            } else {
                LOGGER.info("Skipping cookie for auto login as pg-auto-login is not enabled on merchant");
            }
        }

        return response;
    }

    private void populateResponseData(ValidateLoginOtpResponse serviceResponse, EnhancedValidateOtpResponse response,
            InitiateTransactionRequestBody orderDetail, String txnToken) throws Exception {

        LOGGER.info("Populating app Data from cache for enhanced validate otp flow");

        String ssoToken = serviceResponse.getResponseData().getAccess_token().get(0).getValue();

        String key = enhancedCashierPageServiceHelper.fetchRedisKey(orderDetail.getMid(), orderDetail.getOrderId());

        EnhanceCashierPageCachePayload enhanceCashierPageCachePayload = (EnhanceCashierPageCachePayload) nativeSessionUtil
                .getKey(key);

        if (null == enhanceCashierPageCachePayload) {
            LOGGER.error("Unable to get enhanceCashierPageCachePayload data from cache");
            throw new SessionExpiredException();
        }
        EnhancedCashierPageDynamicQR qr = enhanceCashierPageCachePayload.getEnhancedCashierPage().getQr();
        PaymentRequestBean paymentRequestBean = enhanceCashierPageCachePayload.getMerchantRequestData();
        paymentRequestBean.setDynamicQrRequired(qr == null);
        InitiateTransactionResponse initiateTransactionResponse = enhanceCashierPageCachePayload
                .getInitiateTransactionResponse();

        paymentRequestBean.setPaytmToken(ssoToken);
        paymentRequestBean.setNativeAddMoney(orderDetail.isNativeAddMoney());
        orderDetail.setPaytmSsoToken(ssoToken);
        orderDetail.setSetSSOViaOptLogin(true);
        initiateTransactionResponse.getBody().setAuthenticated(true);

        nativeSessionUtil.setOrderDetail(txnToken, orderDetail);

        initiateTransactionResponse.getBody().setResultInfo(null);
        EnhancedCashierPage enhancedCashierPage = enhancedCashierPageService.getEnhancedCashierPage(paymentRequestBean,
                initiateTransactionResponse);
        if (qr != null && qr.isUPIQR()) {
            enhancedCashierPage.setQr(qr);
        }
        enhanceCashierPageCachePayload = (EnhanceCashierPageCachePayload) nativeSessionUtil.getKey(key);
        initiateTransactionResponse = enhanceCashierPageCachePayload.getInitiateTransactionResponse();
        enhancedCashierPageService.setEnhancedCashierPageInCache(paymentRequestBean, initiateTransactionResponse,
                enhancedCashierPage);

        response.getBody().setAuthenticated(true);
        // Setting Show card flag User login case.
        enhancedCashierPage.setShowStoreCardEnabled(true);
        if (orderDetail.getLinkDetailsData() != null) {
            EventUtils.pushLinkBasedPaymentLoginEvent(orderDetail, Channel.WEB.getName());
        }
        LOGGER.info("App Data successfully populated from cache for enhanced validate otp flow :",
                JsonMapper.mapObjectToJson(enhancedCashierPage));

        response.getBody().setEnhancedCashierPage(enhancedCashierPage);
    }

    private EnhancedValidateOtpResponse getAutoLoginValidateOtpResponse(ValidateLoginOtpResponse serviceResponse) {
        EnhancedValidateOtpResponse response = new EnhancedValidateOtpResponse();
        EnhancedValidateOtpResponseBody responseBody = new EnhancedValidateOtpResponseBody();
        responseBody.setResultInfo(new ResultInfo(serviceResponse.getStatus(), serviceResponse.getResponseCode(),
                serviceResponse.getMessage()));
        response.setBody(responseBody);
        response.setHead(new ResponseHeader());
        response.getBody().setAuthenticated(false);
        response.getBody().getResultInfo().setResultStatus("F");
        return response;
    }

    @Override
    protected EnhancedValidateOtpResponse postProcess(ValidateOtpRequest request,
            ValidateLoginOtpRequest serviceRequest, EnhancedValidateOtpResponse serviceResponse) {
        return serviceResponse;
    }

    private boolean checkValidateOtpLimitBreached(ValidateOtpRequest request) {
        String token = request.getHead().getTxnToken();

        return nativeSessionUtil.checkEnhanceOTPValidateLimit(token);
    }

    private void validate(ValidateOtpRequest request, InitiateTransactionRequestBody orderDetail) {
        nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());
    }

    private ValidateLoginOtpRequest requestTransform(ValidateOtpRequest request, String uniqueId) {
        String secretKey = configurationDataService
                .getPaytmPropertyValue(ExtraConstants.OAUTH_PG_PLUS_EXPRESS_CLIENT_SECRET_KEY);

        GenerateLoginOtpResponse generateLoginOtpResponse = nativeSessionUtil.getSendOtpResponse(request.getHead()
                .getTxnToken());

        String clientId = configurationDataService
                .getPaytmPropertyValue(ExtraConstants.OAUTH_PG_PLUS_EXPRESS_CLIENT_ID);

        String state = generateLoginOtpResponse.getState();
        return new ValidateLoginOtpRequest(request.getBody().getOtp(), state, clientId, secretKey, uniqueId);
    }

    private ValidateLoginOtpResponse createOtpValidationLimitExceededResponse() {
        ValidateLoginOtpResponse validateLoginOtpResponse = new ValidateLoginOtpResponse();
        validateLoginOtpResponse.setMessage(ExtraConstants.OTP_VALIDATE_LIMIT_EXCEEDED_MESSAGE);
        validateLoginOtpResponse.setStatus(ExtraConstants.FAILURE);
        validateLoginOtpResponse.setResponseCode(TheiaConstant.ResponseConstants.ResponseCodes.OTP_LIMIT_BREACHED);

        return validateLoginOtpResponse;
    }

}