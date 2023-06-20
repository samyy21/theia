package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.cache.model.MerchantExtendedInfoResponse;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.facade.user.helper.AuthenticationFixedResponses;
import com.paytm.pgplus.facade.user.models.request.GenerateSendOtpRequest;
import com.paytm.pgplus.facade.user.models.response.GenerateLoginOtpResponse;
import com.paytm.pgplus.facade.user.services.IAuthentication;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.pgproxycommon.utils.MobileNumberUtils;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.request.SubscriptionTransactionRequestBody;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.enums.ESmsTemplate;
import com.paytm.pgplus.theia.helper.MobileMaskHelper;
import com.paytm.pgplus.theia.nativ.model.auth.GenerateOtpRequest;
import com.paytm.pgplus.theia.nativ.model.auth.GenerateOtpResponse;
import com.paytm.pgplus.theia.nativ.model.auth.GenerateOtpResponseBody;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.MERCHANT_NAME;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.MerchantPreference.PreferenceKeys.MASK_MOBILE_ON_CASHIER_PAGE_ENABLED;
import static com.paytm.pgplus.theia.constants.TheiaConstant.PaytmPropertyConstants.IS_ORDER_ID_ENABLE_FOR_OTPS;

@Service("nativeGenerateOtpRequestProcessor")
public class NativeGenerateOtpRequestProcessor
        extends
        AbstractRequestProcessor<GenerateOtpRequest, GenerateOtpResponse, GenerateSendOtpRequest, GenerateLoginOtpResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeGenerateOtpRequestProcessor.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(NativeGenerateOtpRequestProcessor.class);

    private static final String SCOPE = "wallet";

    @Autowired
    @Qualifier("authenticationImpl")
    private IAuthentication authService;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    private IMerchantDataService merchantDataService;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private MobileMaskHelper mobileMaskHelper;

    // +91,91,0 and 10 digit mobile number pattern validation.
    private static Pattern MOBILE_VALIDATION_PATTERN = Pattern
            .compile("^([+][9][1]|[9][1]|[0]){0,1}([7-9]{1})([0-9]{9})$");

    Function<String, String> getLastTenDigitsOfMobile = mobile -> mobile.substring(mobile.length() - 10);

    @Override
    protected GenerateSendOtpRequest preProcess(GenerateOtpRequest request) {

        InitiateTransactionRequestBody orderDetail = validate(request);
        String mid = validateAndGetMid(request, orderDetail);

        boolean isOrderIdEnableForOTPs = Boolean.parseBoolean(ConfigurationUtil.getTheiaProperty(
                IS_ORDER_ID_ENABLE_FOR_OTPS, "false"));

        GenerateSendOtpRequest serviceRequest = null;

        if (orderDetail != null && StringUtils.isNotBlank(request.getHead().getTxnToken())
                && mobileMaskHelper.isValidMaskedMobileNumber(request.getBody().getMobileNumber())
                && ff4jUtils.isFeatureEnabledOnMid(mid, MASK_MOBILE_ON_CASHIER_PAGE_ENABLED, false)) {

            validateAndUpdateMobileNumber(request, orderDetail);

        }
        if (isOrderIdEnableForOTPs && orderDetail != null) {
            serviceRequest = transformRequest(request, mid, orderDetail);
        } else {
            serviceRequest = transformRequest(request, mid, null);
        }

        return serviceRequest;
    }

    @Override
    protected GenerateLoginOtpResponse onProcess(GenerateOtpRequest request, GenerateSendOtpRequest serviceRequest)
            throws Exception {

        boolean sendOTPLimitBreached = checkOtpLimitBreached(request);
        if (sendOTPLimitBreached) {
            GenerateLoginOtpResponse response = new GenerateLoginOtpResponse("FAILURE", null,
                    TheiaConstant.ResponseConstants.ResponseCodes.OTP_LIMIT_BREACHED,
                    "Oops ! You have reached OTP limit, please raise a query at paytm.com/care.");
            if (TokenType.GUEST == request.getHead().getTokenType()) {
                response.setStatus("F");
            }
            return response;
        }

        boolean isValidMobile = MobileNumberUtils.isValidMobileExtended(serviceRequest.getLoginId());
        if (!isValidMobile) {
            return sendFailLoginOTPResponse(request);
        }

        GenerateLoginOtpResponse serviceResponse = authService.generateSendOtp(serviceRequest);
        updateMessageForFailure(serviceResponse);
        try {
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("RESPONSE_STATUS", serviceResponse.getStatus());
            responseMap.put("RESPONSE_MESSAGE", serviceResponse.getMessage());
            statsDUtils.pushResponse("GENERATE_LOGIN_OTP_V5", responseMap);
        } catch (Exception exception) {
            LOGGER.error("Error in pushing response message " + "GENERATE_LOGIN_OTP_V5" + "to grafana", exception);
        }

        if ("SUCCESS".equals(serviceResponse.getStatus())) {

            setSendOtpResponseInCache(request, serviceResponse);

            if (TheiaConstant.EnhancedCashierFlow.ENHANCED_CASHIER_FLOW.equals(request.getHead().getWorkFlow())) {

                NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.validate(request.getHead()
                        .getTxnToken());
                InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();

                if (!request.getBody().getMobileNumber().equals(orderDetail.getUserInfo().getMobile())) {
                    orderDetail.getUserInfo().setMobile(request.getBody().getMobileNumber());
                    nativeSessionUtil.setOrderDetail(request.getHead().getTxnToken(), orderDetail);
                }
            }
        }
        return serviceResponse;
    }

    private void updateMessageForFailure(GenerateLoginOtpResponse serviceResponse) {
        if (serviceResponse == AuthenticationFixedResponses.generateLoginOtpFailureResponse()) {
            serviceResponse.setMessage(TheiaConstant.ResponseConstants.ResponseCodeMessages.SEND_OTP_FAILURE_MESSAGE);
        }
    }

    private void setSendOtpResponseInCache(GenerateOtpRequest request, GenerateLoginOtpResponse serviceResponse) {
        String token = request.getHead().getTxnToken();

        if (TokenType.GUEST == request.getHead().getTokenType() || TokenType.ACCESS == request.getHead().getTokenType()) {
            token = request.getHead().getToken();
        }

        nativeSessionUtil.setSendOtpResponse(token, serviceResponse);
    }

    private boolean checkOtpLimitBreached(GenerateOtpRequest request) {
        String token = request.getHead().getTxnToken();

        if (TokenType.GUEST == request.getHead().getTokenType() || TokenType.ACCESS == request.getHead().getTokenType()) {
            token = request.getHead().getToken();
        }

        return nativeSessionUtil.checkOTPLimit(token);
    }

    private GenerateLoginOtpResponse sendFailLoginOTPResponse(GenerateOtpRequest request) {
        GenerateLoginOtpResponse response = new GenerateLoginOtpResponse("FAILURE", null, "431", "Invalid Mobile");
        if (TokenType.GUEST == request.getHead().getTokenType()) {
            response.setStatus("F");
        }
        return response;
    }

    @Override
    protected GenerateOtpResponse postProcess(GenerateOtpRequest request, GenerateSendOtpRequest serviceRequest,
            GenerateLoginOtpResponse serviceResponse) {
        GenerateOtpResponse response = responseTransformer(request, serviceResponse);
        return response;
    }

    private InitiateTransactionRequestBody validate(GenerateOtpRequest request) {

        if (TokenType.GUEST == request.getHead().getTokenType()) {
            nativeSessionUtil.validateGuestTokenForCheckoutFlow(request.getHead().getToken());
            String mid = (String) nativeSessionUtil.getField(request.getHead().getToken(),
                    TheiaConstant.RequestParams.Native.MID);
            nativeValidationService.validateMid(mid);
            return null;
        } else if (TokenType.ACCESS.equals(request.getHead().getTokenType())) {
            validateAccessToken(request);
            return null;
        }

        InitiateTransactionRequestBody orderDetail = null;
        orderDetail = nativeValidationService.validateTxnToken(request.getHead().getTxnToken());
        nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());
        return orderDetail;

    }

    private String validateAndGetMid(GenerateOtpRequest request, InitiateTransactionRequestBody orderDetail) {
        if (TokenType.GUEST == request.getHead().getTokenType() || TokenType.ACCESS == request.getHead().getTokenType()) {
            return (String) nativeSessionUtil.getField(request.getHead().getToken(),
                    TheiaConstant.RequestParams.Native.MID);
        }
        nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());
        return orderDetail.getMid();
    }

    private GenerateSendOtpRequest transformRequest(GenerateOtpRequest request, String mid,
            InitiateTransactionRequestBody orderDetail) {
        String clientId = configurationDataService
                .getPaytmPropertyValue(ExtraConstants.OAUTH_PG_PLUS_EXPRESS_CLIENT_ID);
        String secretKey = configurationDataService
                .getPaytmPropertyValue(ExtraConstants.OAUTH_PG_PLUS_EXPRESS_CLIENT_SECRET_KEY);
        boolean isOrderIdEnableForOTPs = Boolean.parseBoolean(ConfigurationUtil.getTheiaProperty(
                IS_ORDER_ID_ENABLE_FOR_OTPS, "false"));
        boolean allowRegisteredUserOnly = merchantPreferenceService.isAllowRegisteredUserOnlyLogin(mid);
        String actionType = OAUTH_ACTION_TYPE_REGISTER;
        if (allowRegisteredUserOnly) {
            actionType = OAUTH_ACTION_TYPE_VERIFY_PHONE;
        }
        String uniqueId = (isOrderIdEnableForOTPs && orderDetail != null) ? orderDetail.getOrderId() : null;
        GenerateSendOtpRequest generateSendOtpRequest = new GenerateSendOtpRequest(clientId, secretKey, request
                .getBody().getMobileNumber(), SCOPE, actionType, null, OAUTH_EVALUATION_TYPE_OAUTH_LOGIN, uniqueId);
        String merchantName = StringUtils.EMPTY;
        try {
            MerchantExtendedInfoResponse merchantExtendedData = merchantDataService.getMerchantExtendedData(mid);
            EXT_LOGGER.customInfo("Mapping response - MerchantExtendedInfoResponse :: {}", merchantExtendedData);
            merchantName = merchantExtendedData.getExtendedInfo().getMerchantName();
        } catch (Exception e) {
            LOGGER.error("Exception occured while fetching merchantExtendedData for Merchant Name for OTP");
        }
        String otpMessage = null;
        if (orderDetail != null && ERequestType.NATIVE_SUBSCRIPTION.getType().equals(orderDetail.getRequestType()))
            otpMessage = generateSubscriptionOTPMessage(merchantName, orderDetail, generateSendOtpRequest, mid);
        else
            otpMessage = generateOTPMessage(merchantName, request, generateSendOtpRequest, mid);
        generateSendOtpRequest.setOtpSmsText(otpMessage);
        if (allowRegisteredUserOnly) {
            request.getBody().setAllowRegisteredUserOnly(allowRegisteredUserOnly);
        }
        return generateSendOtpRequest;
    }

    private String generateSubscriptionOTPMessage(String merchantName, InitiateTransactionRequestBody orderDetail,
            GenerateSendOtpRequest generateSendOtpRequest, String mid) {
        ESmsTemplate smsTemplate = ESmsTemplate.withDefault(ESmsTemplate.SUBSCRIPTION_CREATE_OTP_MESSAGE,
                ESmsTemplate.LOGIN_OTP_DEFAULT_MESSAGE);
        populateSendOtpV5Fields(generateSendOtpRequest, mid, smsTemplate);
        String otpSmsText = smsTemplate.getMessage();
        otpSmsText = prepareOtpSmsText(otpSmsText, merchantName,
                ConfigurationUtil.getTheiaProperty(MERCHANT_NAME_LENGTH_WITHOUT_HASH, "12"));
        otpSmsText = populateSubsDetail(otpSmsText, orderDetail);
        return otpSmsText;
    }

    private String populateSubsDetail(String otpSmsText, InitiateTransactionRequestBody orderDetail) {
        SubscriptionTransactionRequestBody subsDetail = (SubscriptionTransactionRequestBody) orderDetail;
        otpSmsText = otpSmsText.replace(TXN_AMT, subsDetail.getTxnAmount().getValue());
        otpSmsText = otpSmsText.replace(SUBS_MAX_AMT, subsDetail.getSubscriptionMaxAmount());
        String subscriptionFrequency = new StringBuilder().append(subsDetail.getSubscriptionFrequency()).append(" ")
                .append(subsDetail.getSubscriptionFrequencyUnit()).toString();
        otpSmsText = otpSmsText.replace(SUBSCRIPTION_FREQUENCY, subscriptionFrequency);
        return otpSmsText;
    }

    private String generateOTPMessage(String merchantName, GenerateOtpRequest request,
            GenerateSendOtpRequest generateSendOtpRequest, String mid) {
        String otpSmsText = null;
        if (StringUtils.isBlank(request.getBody().getAutoReadHash())) {
            ESmsTemplate smsTemplate = ESmsTemplate.withDefault(ESmsTemplate.LOGIN_OTP_MESSAGE_WITHOUT_HASH,
                    ESmsTemplate.LOGIN_OTP_DEFAULT_MESSAGE);
            populateSendOtpV5Fields(generateSendOtpRequest, mid, smsTemplate);
            otpSmsText = smsTemplate.getMessage();
            otpSmsText = prepareOtpSmsText(otpSmsText, merchantName,
                    ConfigurationUtil.getTheiaProperty(MERCHANT_NAME_LENGTH_WITHOUT_HASH, "12"));
        } else {
            ESmsTemplate smsTemplate = ESmsTemplate.withDefault(ESmsTemplate.LOGIN_OTP_MESSAGE_WITH_HASH,
                    ESmsTemplate.LOGIN_OTP_DEFAULT_MESSAGE);
            populateSendOtpV5Fields(generateSendOtpRequest, mid, smsTemplate);
            otpSmsText = smsTemplate.getMessage();
            otpSmsText = prepareOtpSmsText(otpSmsText, merchantName,
                    ConfigurationUtil.getTheiaProperty(MERCHANT_NAME_LENGTH_WITH_HASH, "7"));
            otpSmsText = otpSmsText.replace(AUTO_READ_HASH, request.getBody().getAutoReadHash());
        }
        return otpSmsText;
    }

    private void populateSendOtpV5Fields(GenerateSendOtpRequest generateSendOtpRequest, String mid,
            ESmsTemplate smsTemplate) {
        if (ff4jUtils.isFeatureEnabledOnMid(mid, FEATURE_SEND_OTP_V5, false)) {
            String templateId = smsTemplate.getTemplateId();
            String entityId = ESmsTemplate.getEntityId();
            String smsSenderId = ESmsTemplate.getSmsSenderId();
            LOGGER.info("parameters for v5/sendOtp, templateId : {}, entityId : {}, senderId : {}", templateId,
                    entityId, smsSenderId);
            generateSendOtpRequest.setTemplateId(templateId);
            generateSendOtpRequest.setEntityId(entityId);
            generateSendOtpRequest.setSmsSenderId(smsSenderId);
        }
    }

    private String prepareOtpSmsText(String otpSmsText, String merchantName, String merchantNameLength) {
        if (StringUtils.isBlank(merchantName)) {
            otpSmsText = otpSmsText.replace(MERCHANT_NAME, "..");
        } else {
            if (merchantName.length() <= Integer.parseInt(merchantNameLength))
                otpSmsText = otpSmsText.replace(MERCHANT_NAME, merchantName);
            else
                otpSmsText = otpSmsText.replace(MERCHANT_NAME,
                        merchantName.substring(0, Integer.parseInt(merchantNameLength) - 2).concat(".."));
        }
        return otpSmsText;
    }

    private GenerateOtpResponse responseTransformer(GenerateOtpRequest request, GenerateLoginOtpResponse serviceResponse) {
        GenerateOtpResponse response = new GenerateOtpResponse();
        GenerateOtpResponseBody responseBody = new GenerateOtpResponseBody();
        if (request.getBody().getAllowRegisteredUserOnly() != null && request.getBody().getAllowRegisteredUserOnly()
                && "FAILURE".equals(serviceResponse.getStatus()) && "441".equals(serviceResponse.getResponseCode())) {
            serviceResponse.setMessage(USER_NOT_REGISTERED_MESSAGE);
        }
        if (TheiaConstant.EnhancedCashierFlow.ENHANCED_CASHIER_FLOW.equals(request.getHead().getWorkFlow())
                || TokenType.GUEST == request.getHead().getTokenType()) {
            if ("SUCCESS".equals(serviceResponse.getStatus())) {
                serviceResponse.setStatus("S");
            } else {
                serviceResponse.setStatus("F");
            }
        }
        responseBody.setResultInfo(new ResultInfo(serviceResponse.getStatus(), serviceResponse.getResponseCode(),
                serviceResponse.getMessage()));
        response.setBody(responseBody);
        response.setHead(new ResponseHeader());
        return response;
    }

    private void validateAccessToken(GenerateOtpRequest request) {
        String mid = (String) nativeSessionUtil.getField(request.getHead().getToken(),
                TheiaConstant.RequestParams.Native.MID);
        String referenceId = request.getBody().getReferenceId();
        String token = request.getHead().getToken();
        nativeValidationService.validateMid(mid);
        accessTokenUtils.validateAccessToken(mid, referenceId, token);
    }

    private void validateAndUpdateMobileNumber(GenerateOtpRequest request, InitiateTransactionRequestBody orderDetail) {
        if (orderDetail != null && orderDetail.getUserInfo() != null) {
            if (MobileNumberUtils.isValidMobileExtended(orderDetail.getUserInfo().getMobile())) {
                String maskedMobileNumber = request.getBody().getMobileNumber();
                String unmaskedMobileNumber = orderDetail.getUserInfo().getMobile();
                String reMaskedMobileNumber = mobileMaskHelper.getMaskedNumber(unmaskedMobileNumber);

                if (getLastTenDigitsOfMobile.apply(maskedMobileNumber).equals(
                        getLastTenDigitsOfMobile.apply(reMaskedMobileNumber))) {
                    LOGGER.debug("Setting Mobile Number in request : {}", unmaskedMobileNumber);
                    request.getBody().setMobileNumber(unmaskedMobileNumber);
                } else {
                    LOGGER.error("Masked Mobile Number didn't matched with Mobile Number in orderDetail");
                }

            } else {
                LOGGER.error("Mobile Number not found in orderDetail");
            }
        }
    }
}