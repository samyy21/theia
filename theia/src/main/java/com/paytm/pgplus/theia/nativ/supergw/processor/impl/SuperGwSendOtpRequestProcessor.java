package com.paytm.pgplus.theia.nativ.supergw.processor.impl;

import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.cache.model.MerchantExtendedInfoResponse;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.user.models.request.GenerateSendOtpRequest;
import com.paytm.pgplus.facade.user.models.response.GenerateLoginOtpResponse;
import com.paytm.pgplus.facade.user.services.IAuthentication;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.pgproxycommon.utils.MobileNumberUtils;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.ESmsTemplate;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.supergw.util.SuperGwValidationUtil;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theiacommon.enums.ERequestType;
import com.paytm.pgplus.theiacommon.exception.InvalidRequestParameterException;
import com.paytm.pgplus.theiacommon.exception.RequestValidationException;
import com.paytm.pgplus.theiacommon.response.ResultInfo;
import com.paytm.pgplus.theiacommon.supergw.constants.Constants;
import com.paytm.pgplus.theiacommon.supergw.models.SubscriptionDetails;
import com.paytm.pgplus.theiacommon.supergw.request.SendOtpRequest;
import com.paytm.pgplus.theiacommon.supergw.request.SendOtpServiceRequest;
import com.paytm.pgplus.theiacommon.supergw.response.SendOtpResponse;
import com.paytm.pgplus.theiacommon.supergw.response.SendOtpResponseBody;
import com.paytm.pgplus.theiacommon.supergw.response.SendOtpServiceResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.OAUTH_ACTION_TYPE_REGISTER;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.OAUTH_EVALUATION_TYPE_OAUTH_LOGIN;
import static com.paytm.pgplus.theia.constants.TheiaConstant.PaytmPropertyConstants.IS_ORDER_ID_ENABLE_FOR_OTPS;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.REFERENCE_ID;

@Service("superGwSendOtpRequestProcessor")
public class SuperGwSendOtpRequestProcessor extends
        AbstractRequestProcessor<SendOtpRequest, SendOtpResponse, SendOtpServiceRequest, SendOtpServiceResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SuperGwSendOtpRequestProcessor.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(SuperGwSendOtpRequestProcessor.class);

    private static final String SCOPE = "wallet";

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    /*
     * @Autowired
     * 
     * @Qualifier("sgwSendOtpRequestProcessor") private
     * SgwSendOtpRequestProcessor sgwSendOtpRequestProcessor;
     */

    @Autowired
    @Qualifier("authenticationImpl")
    private IAuthentication authService;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    private IMerchantDataService merchantDataService;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private Ff4jUtils ff4jUtils;

    private static final String SUCCESS = "SUCCESS";
    private static final String OTP_STATE = "otpState";
    private static final String OTP_RETRY_LIMIT = "otpRetryLimit";

    @Autowired
    private Environment environment;

    @Override
    protected SendOtpServiceRequest preProcess(SendOtpRequest request) {
        validateRequest(request);
        return getServiceRequest(request);
    }

    @Override
    protected SendOtpServiceResponse onProcess(SendOtpRequest request, SendOtpServiceRequest serviceRequest)
            throws Exception {
        SendOtpServiceResponse serviceResponse = null;
        String referenceId = MDC.get(REFERENCE_ID);
        String mid = request.getBody().getMid();
        String redisKey = nativeSessionUtil.getCacheKeyForSuperGw(mid, referenceId, ERequestType.NATIVE.getType());
        /*
         * hack to add key in redis in case the key is not already present
         * (possible when fpo call is made to Subscription service and sendOtp
         * call is made on Theia)
         */
        if (!nativeSessionUtil.isExist(redisKey)) {
            nativeSessionUtil.setField(redisKey, OTP_RETRY_LIMIT, 0, 900);
        }
        boolean sendOTPLimitBreached = nativeSessionUtil.checkOTPLimit(redisKey);
        if (sendOTPLimitBreached) {
            serviceResponse = new SendOtpServiceResponse();
            serviceResponse.setStatus(Constants.Alphabets.F);
            serviceResponse.setMessage("Oops ! You have reached OTP limit, please raise a query at paytm.com/care.");
            serviceResponse.setResponseCode(TheiaConstant.ResponseConstants.ResponseCodes.OTP_LIMIT_BREACHED);
            return serviceResponse;
        }

        // code to be moved in common jar - start
        validateRequest(serviceRequest);
        boolean isValidMobile = MobileNumberUtils.isValidMobileExtended(serviceRequest.getMobileNumber());
        if (!isValidMobile) {
            return getInvalidMobileResponse();
        }

        try {
            GenerateSendOtpRequest generateSendOtpRequest = getGenerateSendOtpRequest(serviceRequest);
            GenerateLoginOtpResponse generateLoginOtpResponse = authService.generateSendOtp(generateSendOtpRequest);
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", generateLoginOtpResponse.getStatus());
                responseMap.put("RESPONSE_MESSAGE", generateLoginOtpResponse.getMessage());
                statsDUtils.pushResponse("GENERATE_LOGIN_OTP_V5", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "GENERATE_LOGIN_OTP_V5" + "to grafana", exception);
            }
            serviceResponse = getSendOtpServiceResponse(generateLoginOtpResponse);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occurred while sending OTP {}", e);
            serviceResponse = new SendOtpServiceResponse();
            serviceResponse.setMessage("Something went wrong while validating OTP");
            serviceResponse.setStatus("SC_INTERNAL_SERVER_ERROR");
            return serviceResponse;
        }
        return serviceResponse;
        // code to be moved in common jar - end
    }

    @Override
    protected SendOtpResponse postProcess(SendOtpRequest request, SendOtpServiceRequest serviceRequest,
            SendOtpServiceResponse serviceResponse) throws Exception {
        setResponseInCache(request, serviceResponse);
        SendOtpResponse generateOtpResponse = getGenerateOtpResponse(serviceResponse);
        return generateOtpResponse;
    }

    private SendOtpServiceResponse getInvalidMobileResponse() {
        SendOtpServiceResponse serviceResponse = new SendOtpServiceResponse();
        serviceResponse.setStatus(Constants.Alphabets.F);
        serviceResponse.setResponseCode("431");
        serviceResponse.setMessage("Invalid Mobile");
        return serviceResponse;
    }

    private void validateRequest(SendOtpServiceRequest inRequest) throws InvalidRequestParameterException {
        if (StringUtils.isBlank(inRequest.getOtpSmsText())) {
            throw new InvalidRequestParameterException("SMS text can't be null or blank");
        }
        if (StringUtils.isBlank(inRequest.getClientId())) {
            throw new InvalidRequestParameterException("ClientId can't be null or blank");
        }
        if (StringUtils.isBlank(inRequest.getSecretKey())) {
            throw new InvalidRequestParameterException("SecretKey can't be null or blank");
        }
        if (StringUtils.isBlank(inRequest.getScope())) {
            throw new InvalidRequestParameterException("Scope can't be null or blank");
        }
        if (StringUtils.isBlank(inRequest.getActionType())) {
            throw new InvalidRequestParameterException("ActionType can't be null or blank");
        }
        if (StringUtils.isBlank(inRequest.getEvaluationType())) {
            throw new InvalidRequestParameterException("EvaluationType can't be null or blank");
        }
    }

    private SendOtpServiceResponse getSendOtpServiceResponse(GenerateLoginOtpResponse generateLoginOtpResponse) {
        SendOtpServiceResponse serviceResponse;
        serviceResponse = new SendOtpServiceResponse();
        serviceResponse.setResponseCode(generateLoginOtpResponse.getResponseCode());
        serviceResponse.setMessage(generateLoginOtpResponse.getMessage());
        serviceResponse.setStatus(generateLoginOtpResponse.getStatus());
        serviceResponse.setState(generateLoginOtpResponse.getState());
        serviceResponse.setUniqueId(generateLoginOtpResponse.getUniqueId());
        return serviceResponse;
    }

    private GenerateSendOtpRequest getGenerateSendOtpRequest(SendOtpServiceRequest serviceRequest) {
        String clientId = serviceRequest.getClientId();
        String secretKey = serviceRequest.getSecretKey();
        String mobileNumber = serviceRequest.getMobileNumber();
        String scope = serviceRequest.getScope();
        String actionType = serviceRequest.getActionType();
        String otpSmsText = serviceRequest.getOtpSmsText();
        String evaluationType = serviceRequest.getEvaluationType();
        GenerateSendOtpRequest generateSendOtpRequest = new GenerateSendOtpRequest(clientId, secretKey, mobileNumber,
                scope, actionType, otpSmsText, evaluationType);
        generateSendOtpRequest.setEntityId(serviceRequest.getEntityId());
        generateSendOtpRequest.setSmsSenderId(serviceRequest.getSmsSenderId());
        generateSendOtpRequest.setTemplateId(serviceRequest.getTemplateId());
        return generateSendOtpRequest;
    }

    private void setResponseInCache(SendOtpRequest request, SendOtpServiceResponse serviceResponse) {
        if (SUCCESS.equals(serviceResponse.getStatus())) {
            // state is being used in validate OTP
            String referenceId = MDC.get(REFERENCE_ID);
            String mid = request.getBody().getMid();
            String redisKey = nativeSessionUtil.getCacheKeyForSuperGw(mid, referenceId, ERequestType.NATIVE.getType());
            nativeSessionUtil.setField(redisKey, OTP_STATE, serviceResponse.getState());
        }
    }

    private SendOtpResponse getGenerateOtpResponse(SendOtpServiceResponse serviceResponse) {
        if (SUCCESS.equals(serviceResponse.getStatus())) {
            serviceResponse.setStatus(Constants.Alphabets.S);
        } else {
            serviceResponse.setStatus(Constants.Alphabets.F);
        }

        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultStatus(serviceResponse.getStatus());
        resultInfo.setResultCode(serviceResponse.getResponseCode());
        resultInfo.setResultMsg(serviceResponse.getMessage());

        ResponseHeader responseHeader = new ResponseHeader(TheiaConstant.RequestHeaders.SUPERGW_VERSION);

        SendOtpResponseBody responseBody = new SendOtpResponseBody();
        responseBody.setResultInfo(resultInfo);

        SendOtpResponse response = new SendOtpResponse();
        response.setBody(responseBody);
        response.setHead(responseHeader);
        return response;
    }

    private SendOtpServiceRequest getServiceRequest(SendOtpRequest request) {
        String clientId = configurationDataService
                .getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_PG_PLUS_EXPRESS_CLIENT_ID);
        String secretKey = configurationDataService
                .getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_PG_PLUS_EXPRESS_CLIENT_SECRET_KEY);
        boolean isOrderIdEnableForOTPs = Boolean.parseBoolean(ConfigurationUtil.getTheiaProperty(
                IS_ORDER_ID_ENABLE_FOR_OTPS, "false"));
        String uniqueId = isOrderIdEnableForOTPs ? request.getHead().getRequestId() : null;
        SendOtpServiceRequest sendOtpServiceRequest = new SendOtpServiceRequest(clientId, secretKey, request.getBody()
                .getMobileNumber(), SCOPE, OAUTH_ACTION_TYPE_REGISTER, null, OAUTH_EVALUATION_TYPE_OAUTH_LOGIN,
                uniqueId);
        String merchantName = StringUtils.EMPTY;
        try {
            MerchantExtendedInfoResponse merchantExtendedData = merchantDataService.getMerchantExtendedData(request
                    .getBody().getMid());
            EXT_LOGGER.customInfo("Mapping response - MerchantExtendedInfoResponse :: {}", merchantExtendedData);
            merchantName = merchantExtendedData.getExtendedInfo().getMerchantName();
        } catch (Exception e) {
            LOGGER.error("Exception occurred while fetching merchantExtendedData for Merchant Name for OTP");
        }
        String otpMessage = null;
        if (isOrderIdEnableForOTPs && request.getBody().getRequestTypes().size() == 1
                && request.getBody().getRequestTypes().contains(ERequestType.NATIVE_SUBSCRIPTION)) {
            otpMessage = generateSubscriptionMessage(merchantName, request, sendOtpServiceRequest);
        } else {
            otpMessage = generateOTPMessage(merchantName, request, sendOtpServiceRequest);
        }
        sendOtpServiceRequest.setOtpSmsText(otpMessage);
        return sendOtpServiceRequest;
    }

    private String generateSubscriptionMessage(String merchantName, SendOtpRequest request,
            SendOtpServiceRequest sendOtpServiceRequest) {
        ESmsTemplate smsTemplate = ESmsTemplate.withDefault(ESmsTemplate.SUBSCRIPTION_CREATE_OTP_MESSAGE,
                ESmsTemplate.LOGIN_OTP_DEFAULT_MESSAGE);
        String otpSmsText = smsTemplate.getMessage();
        populateSendOtpV5Fields(sendOtpServiceRequest, request.getBody().getMid(), smsTemplate);
        otpSmsText = prepareOtpSmsText(otpSmsText, merchantName,
                ConfigurationUtil.getTheiaProperty(MERCHANT_NAME_LENGTH_WITHOUT_HASH, "12"));
        SubscriptionDetails subscriptionDetails = request.getBody().getSubscriptionDetails();
        otpSmsText = otpSmsText.replace(TXN_AMT, subscriptionDetails.getTxnAmount().getValue());
        otpSmsText = otpSmsText.replace(SUBS_MAX_AMT, subscriptionDetails.getMaxAmount().getValue());
        String subscriptionFrequency = new StringBuilder().append(subscriptionDetails.getFrequency()).append(" ")
                .append(subscriptionDetails.getFrequencyUnit()).toString();
        otpSmsText = otpSmsText.replace(SUBSCRIPTION_FREQUENCY, subscriptionFrequency);
        return otpSmsText;
    }

    private String generateOTPMessage(String merchantName, SendOtpRequest request,
            SendOtpServiceRequest sendOtpServiceRequest) {
        String otpSmsText = null;
        if (StringUtils.isBlank(request.getBody().getAutoReadHash())) {
            ESmsTemplate smsTemplate = ESmsTemplate.withDefault(ESmsTemplate.LOGIN_OTP_MESSAGE_WITHOUT_HASH,
                    ESmsTemplate.LOGIN_OTP_DEFAULT_MESSAGE);
            populateSendOtpV5Fields(sendOtpServiceRequest, request.getBody().getMid(), smsTemplate);
            otpSmsText = smsTemplate.getMessage();
            otpSmsText = prepareOtpSmsText(otpSmsText, merchantName,
                    ConfigurationUtil.getTheiaProperty(MERCHANT_NAME_LENGTH_WITHOUT_HASH, "12"));
        } else {
            ESmsTemplate smsTemplate = ESmsTemplate.withDefault(ESmsTemplate.LOGIN_OTP_MESSAGE_WITH_HASH,
                    ESmsTemplate.LOGIN_OTP_DEFAULT_MESSAGE);
            populateSendOtpV5Fields(sendOtpServiceRequest, request.getBody().getMid(), smsTemplate);
            otpSmsText = smsTemplate.getMessage();
            otpSmsText = prepareOtpSmsText(otpSmsText, merchantName,
                    ConfigurationUtil.getTheiaProperty(MERCHANT_NAME_LENGTH_WITH_HASH, "7"));
            otpSmsText = otpSmsText.replace(AUTO_READ_HASH, request.getBody().getAutoReadHash());
        }
        return otpSmsText;
    }

    private void populateSendOtpV5Fields(SendOtpServiceRequest sendOtpServiceRequest, String mid,
            ESmsTemplate smsTemplate) {
        if (ff4jUtils.isFeatureEnabledOnMid(mid, FEATURE_SEND_OTP_V5, false)) {
            String templateId = smsTemplate.getTemplateId();
            String entityId = ESmsTemplate.getEntityId();
            String smsSenderId = ESmsTemplate.getSmsSenderId();
            LOGGER.info("parameters for v5/sendOtp, templateId : {}, entityId : {}, senderId : {}", templateId,
                    entityId, smsSenderId);
            sendOtpServiceRequest.setTemplateId(templateId);
            sendOtpServiceRequest.setEntityId(entityId);
            sendOtpServiceRequest.setSmsSenderId(smsSenderId);
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

    private void validateRequest(SendOtpRequest request) {
        validateMandatoryParams(request);
        nativeValidationService.validateMid(request.getBody().getMid());
        validateJwt(request);
    }

    private void validateMandatoryParams(SendOtpRequest request) {
        if (StringUtils.isBlank(request.getBody().getMid())
                || CollectionUtils.isEmpty(request.getBody().getRequestTypes())) {
            com.paytm.pgplus.common.model.ResultInfo resultInfo = new com.paytm.pgplus.common.model.ResultInfo();
            ResultCode resultCode = ResultCode.MISSING_MANDATORY_ELEMENT;
            resultInfo.setResultCode(resultCode.getResultCodeId());
            resultInfo.setResultMsg(resultCode.getResultMsg());
            resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());
            throw new RequestValidationException(resultInfo);
        }
    }

    private void validateJwt(SendOtpRequest request) {
        String clientId = request.getHead().getClientId();
        if (StringUtils.isBlank(clientId)) {
            throw new InvalidRequestParameterException("clientId can't be null");
        }
        String clientSecret = environment.getProperty(clientId);
        Map<String, String> jwtClaims = new HashMap<>();
        jwtClaims.put(FacadeConstants.MID, request.getBody().getMid());
        jwtClaims.put(FacadeConstants.MOBILE_NUMBER, request.getBody().getMobileNumber());
        SuperGwValidationUtil.validateJwtToken(jwtClaims, clientId, clientSecret);
    }

}