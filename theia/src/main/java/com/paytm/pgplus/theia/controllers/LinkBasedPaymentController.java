//package com.paytm.pgplus.theia.controllers;
//
//import com.paytm.pgplus.biz.utils.ConfigurationUtil;
//import com.paytm.pgplus.common.enums.EventNameEnum;
//import com.paytm.pgplus.common.util.MaskingUtil;
//import com.paytm.pgplus.facade.enums.OAuthServiceUrl;
//import com.paytm.pgplus.facade.exception.FacadeCheckedException;
//import com.paytm.pgplus.facade.user.models.request.*;
//import com.paytm.pgplus.facade.user.models.response.GenerateLoginOtpResponse;
//import com.paytm.pgplus.facade.user.models.response.ValidateLoginOtpResponse;
//import com.paytm.pgplus.facade.user.models.response.ValidateSignInOtpResponse;
//import com.paytm.pgplus.facade.user.services.IAuthentication;
//import com.paytm.pgplus.facade.utils.JsonMapper;
//import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
//import com.paytm.pgplus.requestidclient.IdManager;
//import com.paytm.pgplus.stats.util.AWSStatsDUtils;
//import com.paytm.pgplus.theia.constants.TheiaConstant;
//import static com.paytm.pgplus.theia.constants.TheiaConstant.LINK_RESEND_OTP_LIMIT;
//import static com.paytm.pgplus.theia.constants.TheiaConstant.LINK_RESEND_OTP_MAX_COUNT;
//import static com.paytm.pgplus.theia.constants.TheiaConstant.LINK_RESEND_OTP_IN_TIMEFRAME_LIMIT;
//import static com.paytm.pgplus.theia.constants.TheiaConstant.LINK_RESEND_OTP_IN_TIMEFRAME_MAX_COUNT;
//import static com.paytm.pgplus.theia.constants.TheiaConstant.LINK_VALIDATE_OTP_LIMIT;
//import static com.paytm.pgplus.theia.constants.TheiaConstant.LINK_VALIDATE_OTP_MAX_COUNT;
//import com.paytm.pgplus.theia.helper.LinkBasedPaymentHelper;
//import com.paytm.pgplus.theia.models.LinkPaymentValidateOTPResponse;
//import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
//import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
//import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
//import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
//import com.paytm.pgplus.theia.utils.EventUtils;
//import com.paytm.pgplus.theia.utils.MerchantDataUtil;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.ws.rs.Consumes;
//import javax.ws.rs.Produces;
//import javax.ws.rs.core.MediaType;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.Locale;
//import java.util.Map;
//
//import static com.paytm.pgplus.payloadvault.theia.constant.EnumValueToMask.SSOTOKEN;
//import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.CANCEL_TXN_STATUS;
//import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
//import static com.paytm.pgplus.theia.constants.TheiaConstant.LINK_OTP_TIMEOUT;
//import static com.paytm.pgplus.theia.constants.TheiaConstant.LINK_OTP_TIMEOUT_CONSTANT;
//import static com.paytm.pgplus.theia.constants.TheiaConstant.PaytmPropertyConstants.IS_LINK_ON_NEW_OTP_APIS;
//import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.*;
//
///**
// * @author Raman Preet Singh
// * @since 30/10/17
// **/
//
//@RestController
//@RequestMapping("/linkPayment")
//public class LinkBasedPaymentController {
//
//    @Autowired
//    @Qualifier("authenticationImpl")
//    private IAuthentication authenticationImpl;
//
//    @Autowired
//    private AWSStatsDUtils statsDUtils;
//
//    @Autowired
//    private LinkBasedPaymentHelper linkBasedPaymentHelper;
//
//    @Autowired
//    @Qualifier(value = "theiaViewResolverService")
//    private ITheiaViewResolverService theiaViewResolverService;
//
//    @Autowired
//    private MerchantDataUtil merchantDataUtil;
//
//    @Autowired
//    @Qualifier("theiaSessionRedisUtil")
//    ITheiaSessionRedisUtil theiaSessionRedisUtil;
//
//    @Autowired
//    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;
//
//    private static final String VIEW_BASE = "/WEB-INF/views/jsp/";
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(LinkBasedPaymentController.class);
//
//    @Deprecated
//    @RequestMapping(value = "/generateLoginOTP", method = { RequestMethod.GET, RequestMethod.POST })
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    public String generateLoginOTP(final HttpServletRequest request, final HttpServletResponse response,
//            final Model model, final Locale locale) throws ServletException, IOException {
//
//        GenerateLoginOtpResponse generateLoginOtpResponse = null;
//        final long startTime = System.currentTimeMillis();
//
//        try {
//            Integer resendCount = Integer.valueOf((request.getParameter(RESEND_COUNT)));
//            if (resendCount >= MAX_RESEND_OTP_COUNT) {
//                LOGGER.error(TheiaConstant.ExtraConstants.RESEND_OTP_EXCEPTION_MESSAGE);
//                generateLoginOtpResponse = new GenerateLoginOtpResponse(TheiaConstant.ExtraConstants.FAILURE, null,
//                        TheiaConstant.ResponseConstants.ResponseCodes.OTP_LIMIT_BREACHED, RETRY_LIMIT_EXCEEDED_MESSAGE);
//                return generateLoginResponseString(generateLoginOtpResponse);
//            }
//
//            GenerateSignInOtpRequest generateSignInOtpRequest = linkBasedPaymentHelper.getSignInOtpRequest(request);
//
//            try {
//                generateLoginOtpResponse = authenticationImpl.generateSignInOtp(generateSignInOtpRequest);
//            } catch (FacadeCheckedException e) {
//                LOGGER.error("Exception while generating SignIn OTP for {}",
//                        StringUtils.overlay(generateSignInOtpRequest.getPhone(), "****", 0, 4), e);
//                generateLoginOtpResponse = new GenerateLoginOtpResponse(
//                        TheiaConstant.ExtraConstants.SC_INTERNAL_SERVER_ERROR, null,
//                        TheiaConstant.ResponseConstants.ResponseCodes.SC_INTERNAL_SERVER_ERROR,
//                        TheiaConstant.ExtraConstants.OTP_GENERATION_EXCEPTION_MESSAGE);
//            }
//        } catch (Exception e) {
//            LOGGER.error("SYSTEM_ERROR : ", e);
//        } finally {
//            LOGGER.info("Total time taken for LinkBasedPaymentController : GenerateLoginOTP is {} ms",
//                    System.currentTimeMillis() - startTime);
//        }
//
//        return generateLoginResponseString(generateLoginOtpResponse);
//    }
//
//    @RequestMapping(value = "/generateSendOTP", method = { RequestMethod.GET, RequestMethod.POST })
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    public String genrateLoginOTPV2(final HttpServletRequest request, final HttpServletResponse response,
//            final Model model, final Locale locale, @RequestBody(required = false) SendOtpRequest sendOtpRequest) {
//        GenerateLoginOtpResponse generateLoginOtpResponse = null;
//        final long startTime = System.currentTimeMillis();
//
//        if (sendOtpRequest == null) {
//            sendOtpRequest = linkBasedPaymentHelper.buildSendOtpRequest(request);
//        }
//        try {
//            String generateOTPv1 = ConfigurationUtil.getProperty(LINK_CONSULT_ENABLE, "FALSE");
//
//            boolean generateOTPv1Bool = StringUtils.equalsIgnoreCase(generateOTPv1, "TRUE");
//            linkBasedPaymentHelper.validateRequest(sendOtpRequest, generateOTPv1Bool);
//
//            if (sendOtpRequest != null && StringUtils.isBlank(sendOtpRequest.getLinkId())) {
//                LOGGER.info("LINK_ID_MISSING {}", sendOtpRequest.getMid());
//            }
//
//            if (generateOTPv1Bool) {
//                sendOtpRequest = linkBasedPaymentHelper.createSendOTPRequestFromLink(sendOtpRequest);
//            }
//
//            generateLoginOtpResponse = generateLoginOTPResponse(sendOtpRequest);
//
//        } catch (RequestValidationException e) {
//            LOGGER.error("Request validation failed");
//            generateLoginOtpResponse = new GenerateLoginOtpResponse(ResponseConstants.INVALID_REQUEST.getCode(), null,
//                    ResponseConstants.INVALID_REQUEST.getAlipayResultMsg(),
//                    ResponseConstants.INVALID_REQUEST.getMessage());
//        } catch (FacadeCheckedException e) {
//            LOGGER.error("Exception while generating SignIn OTP for {}",
//                    StringUtils.overlay(sendOtpRequest.getMobileNumber(), "****", 0, 4), e);
//            String exceptionMessage = e.getMessage();
//            if (StringUtils.isBlank(exceptionMessage)) {
//                exceptionMessage = TheiaConstant.ExtraConstants.OTP_GENERATION_EXCEPTION_MESSAGE;
//            }
//            generateLoginOtpResponse = new GenerateLoginOtpResponse(
//                    TheiaConstant.ExtraConstants.SC_INTERNAL_SERVER_ERROR, null,
//                    TheiaConstant.ResponseConstants.ResponseCodes.SC_INTERNAL_SERVER_ERROR, exceptionMessage);
//        } catch (Exception e) {
//            LOGGER.error("SYSTEM_ERROR : {}", e);
//            generateLoginOtpResponse = new GenerateLoginOtpResponse(ResponseConstants.SYSTEM_ERROR.getCode(), null,
//                    ResponseConstants.SYSTEM_ERROR.getAlipayResultMsg(), ResponseConstants.SYSTEM_ERROR.getMessage());
//        } finally {
//            LOGGER.info("Total time taken for LinkBasedPaymentController : GenerateLoginOTP is {} ms",
//                    System.currentTimeMillis() - startTime);
//        }
//
//        return generateLoginResponseString(generateLoginOtpResponse);
//    }
//
//    @RequestMapping(value = "/v1/generateSendOTP", method = RequestMethod.POST)
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    public String generateSendOTPV1(final HttpServletRequest request, final HttpServletResponse response,
//            final Model model, final Locale locale, @RequestBody(required = false) SendOTPRequestV1 sendOTPRequestV1) {
//        GenerateLoginOtpResponse generateLoginOtpResponse = null;
//        final long startTime = System.currentTimeMillis();
//
//        try {
//            linkBasedPaymentHelper.validateRequest(sendOTPRequestV1);
//            generateLoginOtpResponse = generateLoginOTPResponse(sendOTPRequestV1.getBody());
//        } catch (RequestValidationException e) {
//            LOGGER.error("Request validation failed");
//            generateLoginOtpResponse = new GenerateLoginOtpResponse(e.getResultInfo().getResultCodeId(), null, e
//                    .getResultInfo().getResultCode(), e.getResultInfo().getResultMsg());
//        } catch (Exception e) {
//            LOGGER.error("SYSTEM_ERROR : {}", e);
//            generateLoginOtpResponse = new GenerateLoginOtpResponse(ResponseConstants.SYSTEM_ERROR.getCode(), null,
//                    ResponseConstants.SYSTEM_ERROR.getAlipayResultMsg(), ResponseConstants.SYSTEM_ERROR.getMessage());
//        } finally {
//            LOGGER.info("Total time taken for LinkBasedPaymentController : GenerateLoginOTP is {} ms",
//                    System.currentTimeMillis() - startTime);
//        }
//
//        return generateLoginResponseString(generateLoginOtpResponse);
//    }
//
//    @Deprecated
//    @RequestMapping(value = "/validateLoginOTP", method = { RequestMethod.GET, RequestMethod.POST })
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    public String validateOTPAndGenerateOrderId(final HttpServletRequest request, final HttpServletResponse response,
//            final Model model, final Locale locale) throws IOException {
//
//        final long startTime = System.currentTimeMillis();
//        ValidateSignInOtpResponse validateSignInOtpResponse = null;
//        String orderId = null;
//        Integer count = null;
//        String mid = null;
//
//        try {
//
//            ValidateSignInOtpRequest validateSignInOtpRequest = linkBasedPaymentHelper
//                    .getValidateSignInOtpRequest(request);
//
//            String state = request.getParameter(STATE);
//            mid = request.getParameter(MID);
//            count = (Integer) theiaTransactionalRedisUtil.get(state);
//
//            try {
//                validateSignInOtpResponse = authenticationImpl.validateSignInOtp(validateSignInOtpRequest);
//            } catch (FacadeCheckedException e) {
//                LOGGER.error("Exception occured while validating login OTP", e);
//                validateSignInOtpResponse = new ValidateSignInOtpResponse();
//                validateSignInOtpResponse
//                        .setResponseMessage(TheiaConstant.ExtraConstants.OTP_VALIDATION_EXCEPTION_MESSAGE);
//                validateSignInOtpResponse
//                        .setStatus(TheiaConstant.ResponseConstants.ResponseCodes.SC_INTERNAL_SERVER_ERROR);
//                return generateValidateSignInResponseString(validateSignInOtpResponse, count, orderId);
//            }
//
//            if (validateSignInOtpResponse.getStatus() != null
//                    && validateSignInOtpResponse.getStatus().equalsIgnoreCase(TheiaConstant.ExtraConstants.FAILURE)) {
//                LOGGER.error("OTP validation failed");
//
//                if (count == null) {
//                    count = 1;
//                } else {
//                    count++;
//                }
//
//                if (count == 3) {
//                    theiaTransactionalRedisUtil.del(state);
//                } else {
//                    theiaTransactionalRedisUtil.set(state, count);
//                }
//
//            } else {
//                try {
//                    mid = getAggregatorMid(mid);
//                    orderId = IdManager.getInstance().getOrderId(mid);
//                } catch (IllegalAccessException e) {
//                    LOGGER.error("Exception while generating orderID", e);
//                }
//            }
//            return generateValidateSignInResponseString(validateSignInOtpResponse, count, orderId);
//        } catch (Exception e) {
//            LOGGER.error("SYSTEM_ERROR : ", e);
//        } finally {
//            LOGGER.info("Total time taken for LinkBasedPaymentController : ValidateLoginOTP is {} ms",
//                    System.currentTimeMillis() - startTime);
//            Map<String, String> metaData = new LinkedHashMap<>();
//            metaData.put(TheiaConstant.LinkBasedParams.VALIDATE_END_POINT, OAuthServiceUrl.VALIDATE_SIGNIN_OTP.getUrl());
//            if (validateSignInOtpResponse != null) {
//                metaData.put(TheiaConstant.LinkBasedParams.VALIDATE_STATUS, validateSignInOtpResponse.getStatus());
//                metaData.put(TheiaConstant.LinkBasedParams.RESPONSE_MESSAGE,
//                        validateSignInOtpResponse.getResponseMessage());
//                metaData.put(TheiaConstant.LinkBasedParams.RESPONSE_CODE, validateSignInOtpResponse.getResponseCode());
//            }
//            String aggregatorMid = mid != null ? getAggregatorMid(mid) : null;
//            EventUtils.pushTheiaEvents(aggregatorMid, orderId, EventNameEnum.PAYMENT_LINK_VALIDATE_OTP, metaData);
//        }
//
//        return generateValidateSignInResponseString(validateSignInOtpResponse, count, orderId);
//    }
//
//    @RequestMapping(value = "/validateSendOTP", method = { RequestMethod.GET, RequestMethod.POST })
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    public String validateSendOTPAndGenerateOrderId(final HttpServletRequest request,
//            final HttpServletResponse response, final Model model, final Locale locale,
//            @RequestBody(required = false) ValidateOtpRequest validateOtpRequest) throws IOException {
//
//        final long startTime = System.currentTimeMillis();
//        ValidateLoginOtpResponse validateLoginOtpResponse = null;
//        String orderId = null;
//        Integer count = null;
//        String mid = null;
//        LOGGER.info("Validate Otp Request received for API: /validateSendOtp is {}", validateOtpRequest);
//        if (validateOtpRequest == null) {
//            validateOtpRequest = linkBasedPaymentHelper.buildValidateOtpRequest(request);
//        }
//        boolean isLinkOnNewOtpApis = Boolean.parseBoolean(ConfigurationUtil.getTheiaProperty(IS_LINK_ON_NEW_OTP_APIS,
//                "false"));
//        try {
//            linkBasedPaymentHelper.validateRequest(validateOtpRequest);
//            ValidateLoginOtpRequest validateLoginOtpRequest = linkBasedPaymentHelper
//                    .getValidateLoginOTPRequest(validateOtpRequest);
//
//            String state = validateOtpRequest.getState();
//            mid = validateOtpRequest.getMid();
//            count = (Integer) theiaTransactionalRedisUtil.get(state);
//
//            try {
//                validateLoginOtpResponse = authenticationImpl.validateLoginOtp(validateLoginOtpRequest);
//            } catch (FacadeCheckedException e) {
//                LOGGER.error("Exception occured while validating login OTP", e);
//                validateLoginOtpResponse = new ValidateLoginOtpResponse();
//                validateLoginOtpResponse.setMessage(TheiaConstant.ExtraConstants.OTP_VALIDATION_EXCEPTION_MESSAGE);
//                validateLoginOtpResponse
//                        .setStatus(TheiaConstant.ResponseConstants.ResponseCodes.SC_INTERNAL_SERVER_ERROR);
//                return generateValidateLoginResponseString(validateLoginOtpResponse, count, orderId);
//            }
//
//            if (validateLoginOtpResponse.getStatus() != null
//                    && validateLoginOtpResponse.getStatus().equalsIgnoreCase(TheiaConstant.ExtraConstants.FAILURE)) {
//                LOGGER.error("OTP validation failed");
//
//                if (count == null) {
//                    count = 1;
//                } else {
//                    count++;
//                }
//                Integer maxValidateOTPCount = Integer.parseInt(ConfigurationUtil.getProperty(LINK_VALIDATE_OTP_LIMIT,
//                        LINK_VALIDATE_OTP_MAX_COUNT));
//                if (count >= maxValidateOTPCount) {
//                    theiaTransactionalRedisUtil.del(state);
//                } else {
//                    theiaTransactionalRedisUtil.set(state, count);
//                }
//
//            } else {
//                if (!isLinkOnNewOtpApis) {
//                    try {
//                        mid = getAggregatorMid(mid);
//
//                        orderId = IdManager.getInstance().getOrderId(mid);
//                    } catch (IllegalAccessException e) {
//                        LOGGER.error("Exception while generating orderID", e);
//                    }
//                } else {
//                    orderId = validateLoginOtpRequest.getTransactionId();
//                }
//            }
//            return generateValidateLoginResponseString(validateLoginOtpResponse, count, orderId);
//        } catch (RequestValidationException e) {
//            LOGGER.error("Request Validation failed ", e);
//            validateLoginOtpResponse = new ValidateLoginOtpResponse();
//            validateLoginOtpResponse.setMessage(ResponseConstants.INVALID_REQUEST.getMessage());
//            validateLoginOtpResponse.setStatus(TheiaConstant.ResponseConstants.FAIL);
//            validateLoginOtpResponse.setResponseCode(ResponseConstants.INVALID_REQUEST.getCode());
//            return generateValidateLoginResponseString(validateLoginOtpResponse, count,
//                    validateOtpRequest.getUniqueId());
//        } catch (Exception e) {
//            LOGGER.error("SYSTEM_ERROR : {}", e);
//            validateLoginOtpResponse = new ValidateLoginOtpResponse();
//            validateLoginOtpResponse.setMessage(ResponseConstants.SYSTEM_ERROR.getMessage());
//            validateLoginOtpResponse.setResponseCode(ResponseConstants.SYSTEM_ERROR.getCode());
//            validateLoginOtpResponse.setStatus(TheiaConstant.ResponseConstants.FAIL);
//            return generateValidateLoginResponseString(validateLoginOtpResponse, count, orderId);
//        } finally {
//            LOGGER.info("Total time taken for LinkBasedPaymentController : ValidateLoginOTP is {} ms",
//                    System.currentTimeMillis() - startTime);
//            Map<String, String> metaData = new LinkedHashMap<>();
//            metaData.put(TheiaConstant.LinkBasedParams.VALIDATE_END_POINT, OAuthServiceUrl.VALIDATE_LOGIN_OTP.getUrl());
//            if (validateLoginOtpResponse != null) {
//                metaData.put(TheiaConstant.LinkBasedParams.VALIDATE_STATUS, validateLoginOtpResponse.getStatus());
//                metaData.put(TheiaConstant.LinkBasedParams.RESPONSE_MESSAGE, validateLoginOtpResponse.getMessage());
//                metaData.put(TheiaConstant.LinkBasedParams.RESPONSE_CODE, validateLoginOtpResponse.getResponseCode());
//            }
//            String aggregatorMid = mid != null ? getAggregatorMid(mid) : null;
//            EventUtils.pushTheiaEvents(aggregatorMid, orderId, EventNameEnum.PAYMENT_LINK_VALIDATE_OTP, metaData);
//        }
//    }
//
//    private String getAggregatorMid(final String mid) {
//        return merchantDataUtil.getAggregatorMid(mid);
//    }
//
//    @RequestMapping(value = "/cancelTxn", method = { RequestMethod.GET, RequestMethod.POST })
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    public void handleCancelTxnForLinkPayment(final HttpServletRequest request, final HttpServletResponse response,
//            final Model model, final Locale locale) throws IOException, ServletException {
//
//        LOGGER.info("Forwarding request to cancel txn - link payment status page");
//
//        request.setAttribute(PAYMENT_STATUS, CANCEL_TXN_STATUS);
//        request.setAttribute(SHOW_VIEW_FLAG, PAYMENT_SCREEN);
//        request.getRequestDispatcher(VIEW_BASE + theiaViewResolverService.returnLinkPaymentStatusPage(request) + ".jsp")
//                .forward(request, response);
//    }
//
//    private String generateLoginResponseString(GenerateLoginOtpResponse response) {
//        String jsonResponse = null;
//
//        if (response != null && response.getResponseCode() != null
//                && SC_ACCEPTED_STATUS_CODE.equalsIgnoreCase(response.getResponseCode())) {
//            response.setMessage(TheiaConstant.ExtraConstants.OTP_GENERATION_EXCEPTION_MESSAGE);
//        }
//
//        try {
//            jsonResponse = JsonMapper.mapObjectToJson(response);
//        } catch (FacadeCheckedException e) {
//            LOGGER.error("JSON mapping exception :", e);
//        }
//        LOGGER.info("JSON response generated : {}", jsonResponse);
//        return jsonResponse;
//    }
//
//    private String generateValidateLoginResponseString(ValidateLoginOtpResponse validateLoginOtpResponse,
//            Integer count, String orderId) {
//        String jsonResponse = null;
//
//        LinkPaymentValidateOTPResponse linkPaymentValidateOTPResponse = new LinkPaymentValidateOTPResponse();
//        linkPaymentValidateOTPResponse.setRetryCount(count);
//        linkPaymentValidateOTPResponse.setOrderId(orderId);
//        linkPaymentValidateOTPResponse.setStatus(validateLoginOtpResponse.getStatus());
//        if (validateLoginOtpResponse.getResponseCode().equalsIgnoreCase(SC_ACCEPTED_STATUS_CODE)) {
//            linkPaymentValidateOTPResponse.setMessage(TheiaConstant.ExtraConstants.OTP_VALIDATION_EXCEPTION_MESSAGE);
//        } else {
//            linkPaymentValidateOTPResponse.setMessage(validateLoginOtpResponse.getMessage());
//        }
//
//        if (validateLoginOtpResponse.getStatus().equalsIgnoreCase(TheiaConstant.ExtraConstants.OAUTH_SUCCESS)) {
//
//            if (validateLoginOtpResponse.getResponseData() != null) {
//                linkPaymentValidateOTPResponse.setSsoToken(validateLoginOtpResponse.getResponseData().getAccess_token()
//                        .get(0).getValue());
//            }
//            linkPaymentValidateOTPResponse.setCustId(validateLoginOtpResponse.getResponseData().getUserId());
//        } else {
//            Integer maxValidateOTPCount = Integer.parseInt(ConfigurationUtil.getProperty(LINK_VALIDATE_OTP_LIMIT,
//                    LINK_VALIDATE_OTP_MAX_COUNT));
//            if (count != null
//                    && count >= maxValidateOTPCount
//                    && !validateLoginOtpResponse.getStatus().equalsIgnoreCase(
//                            TheiaConstant.ExtraConstants.SC_INTERNAL_SERVER_ERROR)) {
//                /*
//                 * if retry count is 3, then override the Oauth message with
//                 * retry count limit exceeded message
//                 */
//                linkPaymentValidateOTPResponse.setMessage(TheiaConstant.ExtraConstants.RETRY_LIMIT_EXCEEDED_MESSAGE);
//            }
//        }
//        linkPaymentValidateOTPResponse.setStatus(validateLoginOtpResponse.getStatus());
//
//        try {
//            jsonResponse = JsonMapper.mapObjectToJson(linkPaymentValidateOTPResponse);
//        } catch (FacadeCheckedException e) {
//            LOGGER.error("JSON mapping exception :", e);
//        }
//        LOGGER.info("linkPaymentValidateOTPResponse response generated : {}", linkPaymentValidateOTPResponse);
//        return jsonResponse;
//    }
//
//    private String generateValidateSignInResponseString(ValidateSignInOtpResponse validateSignInOtpResponse,
//            Integer count, String orderId) {
//        String jsonResponse = null;
//
//        LinkPaymentValidateOTPResponse linkPaymentValidateOTPResponse = new LinkPaymentValidateOTPResponse();
//        if (count != null) {
//            linkPaymentValidateOTPResponse.setRetryCount(count);
//        }
//        if (orderId != null) {
//            linkPaymentValidateOTPResponse.setOrderId(orderId);
//        }
//        if (validateSignInOtpResponse.getStatus() == null) {
//            linkPaymentValidateOTPResponse.setStatus("SUCCESS");
//        } else {
//            linkPaymentValidateOTPResponse.setStatus(validateSignInOtpResponse.getStatus());
//        }
//        if (validateSignInOtpResponse.getResponseMessage() != null) {
//            linkPaymentValidateOTPResponse.setMessage(validateSignInOtpResponse.getResponseMessage());
//        }
//        if (validateSignInOtpResponse.getAccess_token() != null) {
//            linkPaymentValidateOTPResponse.setSsoToken(validateSignInOtpResponse.getAccess_token());
//        }
//        if (validateSignInOtpResponse.getResourceOwnerId() != null) {
//            linkPaymentValidateOTPResponse.setCustId(validateSignInOtpResponse.getResourceOwnerId());
//        }
//        /*
//         * if retry count is 3, then override the Oauth message with retry count
//         * limit exceeded message
//         */
//        if (count != null
//                && count == MAX_RETRY_COUNT
//                && !validateSignInOtpResponse.getStatus().equalsIgnoreCase(
//                        TheiaConstant.ExtraConstants.SC_INTERNAL_SERVER_ERROR)) {
//            linkPaymentValidateOTPResponse.setMessage(TheiaConstant.ExtraConstants.RETRY_LIMIT_EXCEEDED_MESSAGE);
//        }
//        try {
//            jsonResponse = JsonMapper.mapObjectToJson(linkPaymentValidateOTPResponse);
//        } catch (FacadeCheckedException e) {
//            LOGGER.error("JSON mapping exception :", e);
//        }
//        LOGGER.info("JSON response generated : {}", MaskingUtil.maskingString(jsonResponse, SSOTOKEN.getFieldName(),
//                SSOTOKEN.getPrex(), SSOTOKEN.getEndx()));
//        return jsonResponse;
//    }
//
//    private GenerateLoginOtpResponse generateLoginOTPResponse(SendOtpRequest sendOtpRequest) {
//
//        GenerateLoginOtpResponse generateLoginOtpResponse = null;
//
//        boolean isLinkOnNewOtpApis = Boolean.parseBoolean(ConfigurationUtil.getTheiaProperty(IS_LINK_ON_NEW_OTP_APIS,
//                "false"));
//        String orderId = null;
//        String mid = sendOtpRequest.getMid();
//        Integer resendCount = Integer.valueOf((sendOtpRequest.getResendCount()));
//        Integer maxResendOTPCount = Integer.parseInt(ConfigurationUtil.getProperty(LINK_RESEND_OTP_LIMIT,
//                LINK_RESEND_OTP_MAX_COUNT));
//        if (resendCount >= maxResendOTPCount) {
//            LOGGER.error(TheiaConstant.ExtraConstants.RESEND_OTP_EXCEPTION_MESSAGE);
//            generateLoginOtpResponse = new GenerateLoginOtpResponse(TheiaConstant.ExtraConstants.FAILURE, null,
//                    TheiaConstant.ResponseConstants.ResponseCodes.OTP_LIMIT_BREACHED, RETRY_LIMIT_EXCEEDED_MESSAGE);
//            return generateLoginOtpResponse;
//        }
//        Integer multipleOtp = null;
//        multipleOtp = (Integer) theiaSessionRedisUtil.get(LINK_BASED_KEY + sendOtpRequest.getMobileNumber());
//        Integer maxOTPCountInTimeFrame = Integer.parseInt(ConfigurationUtil.getProperty(
//                LINK_RESEND_OTP_IN_TIMEFRAME_LIMIT, LINK_RESEND_OTP_IN_TIMEFRAME_MAX_COUNT));
//        if (multipleOtp == null) {
//            multipleOtp = 1;
//            theiaSessionRedisUtil.set(LINK_BASED_KEY + sendOtpRequest.getMobileNumber(), multipleOtp,
//                    Long.valueOf(ConfigurationUtil.getTheiaProperty(LINK_OTP_TIMEOUT_CONSTANT, LINK_OTP_TIMEOUT)));
//        } else if (multipleOtp >= maxOTPCountInTimeFrame) {
//            LOGGER.error(TheiaConstant.ExtraConstants.OTP_LIMIT_EXCEEDED_MESSAGE);
//            generateLoginOtpResponse = new GenerateLoginOtpResponse(TheiaConstant.ExtraConstants.FAILURE, null,
//                    TheiaConstant.ResponseConstants.ResponseCodes.OTP_LIMIT_BREACHED, OTP_LIMIT_EXCEEDED_MESSAGE);
//            return generateLoginOtpResponse;
//        } else {
//            multipleOtp++;
//            Long ttl = theiaSessionRedisUtil.ttl(LINK_BASED_KEY + sendOtpRequest.getMobileNumber());
//            theiaSessionRedisUtil.set(LINK_BASED_KEY + sendOtpRequest.getMobileNumber(), multipleOtp, ttl);
//        }
//        if (isLinkOnNewOtpApis) {
//            try {
//                orderId = sendOtpRequest.getUniqueId();
//                if (StringUtils.isBlank(orderId)) {
//                    mid = getAggregatorMid(mid);
//                    orderId = IdManager.getInstance().getOrderId(mid);
//                    sendOtpRequest.setUniqueId(orderId);
//                }
//            } catch (IllegalAccessException e) {
//                LOGGER.error("Exception while generating orderID", e);
//            }
//        }
//        GenerateSendOtpRequest generateSendOtpRequest = linkBasedPaymentHelper.generateSendOTPRequest(sendOtpRequest);
//        try {
//            generateLoginOtpResponse = authenticationImpl.generateSendOtp(generateSendOtpRequest);
//            generateLoginOtpResponse.setUniqueId(orderId);
//            try {
//                Map<String, String> responseMap = new HashMap<>();
//                responseMap.put("RESPONSE_STATUS", generateLoginOtpResponse.getStatus());
//                responseMap.put("RESPONSE_MESSAGE", generateLoginOtpResponse.getMessage());
//                statsDUtils.pushResponse("GENERATE_LOGIN_OTP_V5", responseMap);
//            } catch (Exception exception) {
//                LOGGER.error("Error in pushing response message " + "GENERATE_LOGIN_OTP_V5" + "to grafana", exception);
//            }
//        } catch (FacadeCheckedException e) {
//            LOGGER.error("Exception while generating SignIn OTP for {}",
//                    StringUtils.overlay(sendOtpRequest.getMobileNumber(), "****", 0, 4), e);
//            generateLoginOtpResponse = new GenerateLoginOtpResponse(
//                    TheiaConstant.ExtraConstants.SC_INTERNAL_SERVER_ERROR, null,
//                    TheiaConstant.ResponseConstants.ResponseCodes.SC_INTERNAL_SERVER_ERROR,
//                    TheiaConstant.ExtraConstants.OTP_GENERATION_EXCEPTION_MESSAGE);
//        }
//
//        return generateLoginOtpResponse;
//    }
// }
