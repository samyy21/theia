package com.paytm.pgplus.theia.helper;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.common.signature.JWTWithHmacSHA256;
import com.paytm.pgplus.common.util.CommonConstants;
import com.paytm.pgplus.enums.TokenType;
import com.paytm.pgplus.facade.common.model.ResultInfo;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.linkService.services.impl.LinkService;
import com.paytm.pgplus.facade.user.models.request.*;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.ESmsTemplate;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.FEATURE_SEND_OTP_V5;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.MERCHANT_NAME;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.STATE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.MID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.MOBILE_NO;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.OTP;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.*;

/**
 * @author Raman Preet Singh
 * @since 25/10/17
 **/

@Service
public class LinkBasedPaymentHelper {

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    @Qualifier("linkService")
    private LinkService linkService;

    @Autowired
    private Ff4jUtils ff4jUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkBasedPaymentHelper.class);

    public GenerateLoginOtpRequest getLoginOTPRequest(HttpServletRequest request) {

        String clientId = configurationDataService.getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_ID);
        String secretKey = configurationDataService
                .getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_SECRET_KEY);
        String loginId = request.getParameter(MOBILE_NO);

        LOGGER.info("Generate Login OTP request received for MID = {}, for mobile no {}", request.getParameter(MID),
                StringUtils.overlay(loginId, "****", 0, 4));

        return new GenerateLoginOtpRequest(loginId, OTP_ACTION_TYPE, OTP_SCOPE, clientId, secretKey);
    }

    public SendOtpRequest buildSendOtpRequest(HttpServletRequest request) {

        SendOtpRequest sendOtpRequest = new SendOtpRequest();
        sendOtpRequest.setMerchantName(request.getParameter(MERCHANT_NAME));
        sendOtpRequest.setMid(request.getParameter(MID));
        sendOtpRequest.setMobileNumber(request.getParameter(MOBILE_NO));
        sendOtpRequest.setTxnAmount(request.getParameter(TXN_AMT));
        sendOtpRequest.setTxnDiscription(request.getParameter(TXN_DISCRIPTION));
        sendOtpRequest.setUniqueId(request.getParameter(UNIQUE_ID));
        sendOtpRequest.setResendCount(request.getParameter(RESEND_COUNT));
        sendOtpRequest.setLinkId(request.getParameter(FacadeConstants.LINK_CONSULT_LINK_ID));

        return sendOtpRequest;
    }

    public void validateRequest(SendOtpRequest request, boolean generateOTPv1Bool) {
        if (request == null || StringUtils.isBlank(request.getMid()) || StringUtils.isBlank(request.getMerchantName())
                || StringUtils.isBlank(request.getMobileNumber()) || StringUtils.isBlank(request.getResendCount())
                || (generateOTPv1Bool && StringUtils.isBlank(request.getLinkId()))) {
            throw RequestValidationException.getException();
        }
    }

    public void validateRequest(SendOTPRequestV1 request) {

        if (request == null || request.getHead() == null || request.getHead() == null
                || StringUtils.isBlank(request.getHead().getRequestTimestamp())
                || StringUtils.isBlank(request.getHead().getVersion())
                || StringUtils.isBlank(request.getBody().getMid())
                || StringUtils.isBlank(request.getBody().getMerchantName())
                || StringUtils.isBlank(request.getBody().getMobileNumber())
                || StringUtils.isBlank(request.getBody().getResendCount())) {
            throw RequestValidationException.getException();
        }

        validateJWTToken(request);
    }

    private void validateJWTToken(SendOTPRequestV1 request) {

        if (TokenType.JWT.equals(request.getHead().getTokenType())) {
            Map<String, String> jwtMap = new HashMap<>();
            jwtMap.put(Native.MID, request.getBody().getMid());
            jwtMap.put(CommonConstants.MOBILE_NO, request.getBody().getMobileNumber());
            if (!JWTWithHmacSHA256.verifyJsonWebToken(jwtMap, request.getHead().getToken()))
                throw RequestValidationException.getException(ResultCode.TOKEN_VALIDATION_EXCEPTION);
        } else {
            throw RequestValidationException.getException(ResultCode.INVALID_TOKEN_TYPE_EXCEPTION);
        }
    }

    public void validateRequest(ValidateOtpRequest request) {
        if (request == null || StringUtils.isBlank(request.getUniqueId()) || StringUtils.isBlank(request.getOtp())
                || StringUtils.isBlank(request.getState())) {
            throw RequestValidationException.getException();
        }
    }

    public GenerateSendOtpRequest generateSendOTPRequest(SendOtpRequest request) {

        String clientId = configurationDataService.getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_ID);
        String secretKey = configurationDataService
                .getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_SECRET_KEY);
        String loginId = request.getMobileNumber();
        GenerateSendOtpRequest generateSendOtpRequest = new GenerateSendOtpRequest(clientId, secretKey, loginId,
                SIGN_IN_OTP_SCOPE, OAUTH_ACTION_TYPE_REGISTER, null, OAUTH_EVALUATION_TYPE_OAUTH_LOGIN,
                request.getUniqueId());
        String otpSmsText = getOtpMessage(request, generateSendOtpRequest);
        generateSendOtpRequest.setOtpSmsText(otpSmsText);
        LOGGER.info("Generate Send OTP request received for MID = {}, for mobile no {}", request.getMid(),
                StringUtils.overlay(loginId, "****", 0, 4));
        return generateSendOtpRequest;
    }

    private String getOtpMessage(SendOtpRequest request, GenerateSendOtpRequest generateSendOtpRequest) {
        ESmsTemplate smsTemplate = ESmsTemplate.withDefault(ESmsTemplate.LINK_LOGIN_OTP,
                ESmsTemplate.LINK_DEFAULT_MESSAGE);
        if (ff4jUtils.isFeatureEnabledOnMid(request.getMid(), FEATURE_SEND_OTP_V5, false)) {
            String templateId = smsTemplate.getTemplateId();
            String entityId = ESmsTemplate.getEntityId();
            String smsSenderId = ESmsTemplate.getSmsSenderId();
            LOGGER.info("parameters for v5/sendOtp, templateId : {}, entityId : {}, senderId : {}", templateId,
                    entityId, smsSenderId);
            generateSendOtpRequest.setTemplateId(templateId);
            generateSendOtpRequest.setEntityId(entityId);
            generateSendOtpRequest.setSmsSenderId(smsSenderId);
        }
        String otpSmsText = smsTemplate.getMessage();
        if (StringUtils.isNotBlank(otpSmsText)) {
            if (StringUtils.isNotBlank(request.getTxnAmount())) {
                otpSmsText = otpSmsText.replace(TXN_AMT, "Rs. " + request.getTxnAmount() + " to ");
            } else {
                otpSmsText = otpSmsText.replace(TXN_AMT, "");
            }
            otpSmsText = otpSmsText.replace(MERCHANT_NAME, request.getMerchantName());
            if (StringUtils.isNotBlank(request.getTxnDiscription())) {
                otpSmsText = otpSmsText.replace(TXN_DISCRIPTION, " for " + trimLength(request.getTxnDiscription(), 30));
            } else {
                otpSmsText = otpSmsText.replace(TXN_DISCRIPTION, "");
            }
        }
        return otpSmsText;
    }

    private String trimLength(String text, int targetLength) {
        if (StringUtils.isNotBlank(text) && text.length() > targetLength) {
            return text.substring(0, targetLength - 2).concat("..");
        }
        return text;
    }

    public GenerateSignInOtpRequest getSignInOtpRequest(HttpServletRequest request) {

        String clientId = configurationDataService.getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_ID);
        String secretKey = configurationDataService
                .getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_SECRET_KEY);
        String phone = request.getParameter(MOBILE_NO);

        LOGGER.info("Generate Send OTP request received for MID = {}, for mobile no {}",
                request.getParameter(TheiaConstant.RequestParams.MID), StringUtils.overlay(phone, "****", 0, 4));

        return new GenerateSignInOtpRequest("", phone, clientId, SIGN_IN_OTP_SCOPE, secretKey, "token");
    }

    public ValidateOtpRequest buildValidateOtpRequest(HttpServletRequest request) {

        ValidateOtpRequest validateOtpRequest = new ValidateOtpRequest();
        validateOtpRequest.setMid(request.getParameter(MID));
        validateOtpRequest.setState(request.getParameter(STATE));
        validateOtpRequest.setOtp(request.getParameter(OTP));
        validateOtpRequest.setUniqueId(request.getParameter(UNIQUE_ID));

        return validateOtpRequest;
    }

    public ValidateLoginOtpRequest getValidateLoginOTPRequest(ValidateOtpRequest request) {

        String clientId = configurationDataService.getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_ID);
        String secretKey = configurationDataService
                .getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_SECRET_KEY);
        String state = request.getState();
        String otp = request.getOtp();
        String uniqueId = request.getUniqueId();

        LOGGER.info("Validate Login OTP request {}, received for MID = {}", request, request.getMid());

        return new ValidateLoginOtpRequest(otp, state, clientId, secretKey, uniqueId);
    }

    public ValidateSignInOtpRequest getValidateSignInOtpRequest(HttpServletRequest request) {

        String clientId = configurationDataService.getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_ID);
        String secretKey = configurationDataService
                .getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_CLIENT_SECRET_KEY);
        String state = request.getParameter(STATE);
        String otp = request.getParameter(OTP);

        LOGGER.info("Validate Login OTP request received for MID = {}", request.getParameter(MID));

        return new ValidateSignInOtpRequest(otp, state, clientId, secretKey);
    }

    public static String getLinkPaymentStatusDate(Date date) {
        // 12:30 pm, 17 Oct 2017
        if (date == null) {
            return "";
        }
        DateFormat df = new SimpleDateFormat("hh:mm a, dd MMM yyyy");
        df.setLenient(false);
        return df.format(date);
    }

    public SendOtpRequest createSendOTPRequestFromLink(SendOtpRequest sendOtpRequest) throws FacadeCheckedException {

        LinkOTPConsultResponseBody linkOTPConsultResponseBody = linkService.getLinkOTPConsult(
                sendOtpRequest.getLinkId(), sendOtpRequest.getMobileNumber(), sendOtpRequest.getTxnAmount(),
                sendOtpRequest.getMid());

        ResultInfo resultInfo = linkOTPConsultResponseBody.getResultInfo();
        if (resultInfo != null
                && StringUtils.equals(resultInfo.getResultCode(), FacadeConstants.LINK_CONSULT_SUCCESS_RESULT_CODE)) {
            sendOtpRequest.setMerchantName(linkOTPConsultResponseBody.getMerchantName());
            sendOtpRequest.setMid(linkOTPConsultResponseBody.getMid());
            sendOtpRequest.setTxnDiscription(linkOTPConsultResponseBody.getLinkDescription());
        } else {
            LOGGER.error("Link consult response code received other than 200, resultInfo :{} ", resultInfo);
            if (resultInfo != null) {
                throw new FacadeCheckedException(resultInfo.getResultMsg());
            }
            throw new FacadeCheckedException();
        }

        return sendOtpRequest;

    }
}
