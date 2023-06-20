package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.localisationProcessor.annotation.LocaleAPI;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.model.auth.*;
import com.paytm.pgplus.theia.nativ.model.response.ValidateVpaResponse;
import com.paytm.pgplus.theia.nativ.model.vpaValidate.VpaValidateRequest;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory.RequestType;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.V1_ENHANCED_LOGOUT_USER;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.V1_ENHANCED_VALIDATE_OTP;

@RestController
@RequestMapping("api/v1")
@NativeControllerAdvice
@Api(value = "NativeLoginAuthController", description = "It contains API's for user authentication using sendOtp and validateOtp.")
public class NativeLoginAuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeLoginAuthController.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(NativeLoginAuthController.class);

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;
    @Autowired
    private AWSStatsDUtils statsDUtils;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @SuppressWarnings("unchecked")
    @LocaleAPI(apiName = "/theia/api/v1/login/sendOtp", responseClass = GenerateOtpResponse.class)
    @RequestMapping(value = "/login/sendOtp", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "/login/sendOtp", notes = "API used to send OTP to user mobile")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "SUCCESS"),
            @ApiResponse(code = 500, message = "Internal Server Error") })
    public GenerateOtpResponse sendOtp(@ApiParam(required = true) @RequestBody GenerateOtpRequest request)
            throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /login/sendOtp is: {}", request);
            nativePaymentUtil.setReferenceIdInBody(request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            IRequestProcessor<GenerateOtpRequest, GenerateOtpResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.LOGIN_SEND_OTP_REQUEST);
            GenerateOtpResponse response = requestProcessor.process(request);
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", response.getBody().getResultInfo().getResultStatus());
                responseMap.put("RESPONSE_MESSAGE", response.getBody().getResultInfo().getResultMsg());
                statsDUtils.pushResponse("api/v1/login/sendOtp", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "api/v1/login/sendOtp" + "to grafana", exception);
            }
            LOGGER.info("Native response returned for API: /sendOtp is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return response;
        } finally {
            LOGGER.info("Total time taken for NativeSendOtp is {} ms", System.currentTimeMillis() - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

    @SuppressWarnings("unchecked")
    @LocaleAPI(apiName = "/theia/api/v1/login/validateOtp", responseClass = ValidateOtpResponse.class)
    @RequestMapping(value = "/login/validateOtp", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "/login/validateOtp", notes = "API used to validate OTP send by user")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "SUCCESS"),
            @ApiResponse(code = 500, message = "Internal Server Error") })
    public ValidateOtpResponse validateOtp(@ApiParam(required = true) @RequestBody ValidateOtpRequest request)
            throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /login/validateOtp is: {}", request);
            nativePaymentUtil.setReferenceIdInBody(request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            IRequestProcessor<ValidateOtpRequest, ValidateOtpResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.LOGIN_VALIDATE_OTP_REQUEST);
            ValidateOtpResponse response = requestProcessor.process(request);
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", response.getBody().getResultInfo().getResultStatus());
                responseMap.put("RESPONSE_MESSAGE", response.getBody().getResultInfo().getResultMsg());
                statsDUtils.pushResponse("v1/login/validateOtp", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "v1/login/validateOtp" + "to grafana", exception);
            }
            LOGGER.info("Native response returned for API: /login/validateOtp is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return response;
        } finally {
            LOGGER.info("Total time taken for NativeValidateOtp is {} ms", System.currentTimeMillis() - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

    @RequestMapping(value = "/logout/user", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "/logout/user", notes = "API used to logout user")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "SUCCESS"),
            @ApiResponse(code = 500, message = "Internal Server Error") })
    public NativeUserLogoutResponse nativeLogoutUser(
            @ApiParam(required = true) @RequestBody NativeUserLogoutRequest userLogoutRequest,
            @RequestParam(value = "referenceId", required = false) String referenceId,
            @RequestParam(value = "mid", required = false) String mid) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native userLogoutRequest request received is: {}", userLogoutRequest);
            nativePaymentUtil.setReferenceIdAndMidInBody(userLogoutRequest);
            IRequestProcessor<NativeUserLogoutRequest, NativeUserLogoutResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.NATIVE_LOGOUT_USER);
            NativeUserLogoutResponse logoutResponse = requestProcessor.process(userLogoutRequest);
            LOGGER.info("Native /logout/user response returned is: {}", logoutResponse);
            nativePaymentUtil.logNativeResponse(logoutResponse == null ? null : nativePaymentUtil
                    .getResultInfo(logoutResponse.getBody()));
            return logoutResponse;
        } finally {
            LOGGER.info("Total time taken for API /logout/user is {} ms", System.currentTimeMillis() - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

    @LocaleAPI(apiName = V1_ENHANCED_VALIDATE_OTP, responseClass = EnhancedValidateOtpResponse.class)
    @RequestMapping(value = "enhanced/login/validate/otp", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "enhanced/login/validate/otp", notes = "API used to validate OTP send by user and for auto login")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "SUCCESS"),
            @ApiResponse(code = 500, message = "Internal Server Error") })
    public EnhancedValidateOtpResponse autoLoginValidateOtp(
            @ApiParam(required = true) @RequestBody ValidateOtpRequest validateOtpRequest) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native enhanced validate OTP request received is: {}", validateOtpRequest);
            IRequestProcessor<ValidateOtpRequest, EnhancedValidateOtpResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.ENHANCED_LOGIN_VALIDATE_OTP_REQUEST);
            EnhancedValidateOtpResponse enhancedValidateOtpResponse = requestProcessor.process(validateOtpRequest);
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", enhancedValidateOtpResponse.getBody().getResultInfo()
                        .getResultStatus());
                responseMap.put("RESPONSE_MESSAGE", enhancedValidateOtpResponse.getBody().getResultInfo()
                        .getResultMsg());
                statsDUtils.pushResponse("api/v1/enhanced/login/validate/otp", responseMap);
            } catch (Exception exception) {
                LOGGER.error(
                        "Error in pushing response message " + "api/v1/enhanced/login/validate/otp" + "to grafana",
                        exception);
            }
            LOGGER.info("Native enhanced validate OTP response returned is: {}", enhancedValidateOtpResponse);
            nativePaymentUtil.logNativeResponse(enhancedValidateOtpResponse == null ? null : nativePaymentUtil
                    .getResultInfo(enhancedValidateOtpResponse.getBody()));
            return enhancedValidateOtpResponse;
        } finally {
            LOGGER.info("Total time taken for API is {} ms", System.currentTimeMillis() - startTime);
        }
    }

    @LocaleAPI(apiName = V1_ENHANCED_LOGOUT_USER, responseClass = EnhancedNativeUserLogoutResponse.class)
    @RequestMapping(value = "enhanced/logout/user", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "enhanced/logout/user", notes = "API used to logout user")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "SUCCESS"),
            @ApiResponse(code = 500, message = "Internal Server Error") })
    public EnhancedNativeUserLogoutResponse autoLoginValidateOtp(
            @ApiParam(required = true) @RequestBody EnhancedNativeUserLogoutRequest userLogoutRequest) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native enhanced userLogoutRequest request received is: {}", userLogoutRequest);
            IRequestProcessor<EnhancedNativeUserLogoutRequest, EnhancedNativeUserLogoutResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.ENHANCED_LOGOUT_USER_REQUEST);
            EnhancedNativeUserLogoutResponse logoutResponse = requestProcessor.process(userLogoutRequest);
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", logoutResponse.getBody().getResultInfo().getResultStatus());
                responseMap.put("RESPONSE_MESSAGE", logoutResponse.getBody().getResultInfo().getResultMsg());
                statsDUtils.pushResponse("api/v1/enhanced/logout/user", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "api/v1/enhanced/logout/user" + "to grafana",
                        exception);
            }
            LOGGER.info("Native enhanced user logout response returned is: {}", logoutResponse);
            nativePaymentUtil.logNativeResponse(logoutResponse == null ? null : nativePaymentUtil
                    .getResultInfo(logoutResponse.getBody()));
            return logoutResponse;
        } finally {
            LOGGER.info("Total time taken for API is {} ms", System.currentTimeMillis() - startTime);
        }
    }

    @LocaleAPI(apiName = "/theia/api/v1/vpa/validate", responseClass = ValidateOtpResponse.class)
    @RequestMapping(value = "vpa/validate", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "vpa/validate", notes = "API used to validate VPA")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "SUCCESS"),
            @ApiResponse(code = 500, message = "Internal Server Error") })
    public ValidateVpaResponse vpaValidate(
            @ApiParam(required = true) @RequestBody VpaValidateRequest vpaValidateRequest, HttpServletRequest request)
            throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            nativePaymentUtil.setReferenceIdInBody(vpaValidateRequest);
            LOGGER.info("Vpa validate request received as: {}", vpaValidateRequest);
            IRequestProcessor<VpaValidateRequest, ValidateVpaResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.VALIDATE_VPA_REQUEST);
            vpaValidateRequest.getBody().setQueryParams(request.getQueryString());
            // TODO : Remove logger after debugging
            EXT_LOGGER.info("Query Params : {} , {}", request.getParameter(TheiaConstant.RequestParams.Native.MID),
                    request.getParameter(TheiaConstant.RequestParams.Native.ORDER_ID));
            ValidateVpaResponse validateVpaResponse = requestProcessor.process(vpaValidateRequest);
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", validateVpaResponse.getBody().getResultInfo().getResultStatus());
                responseMap.put("RESPONSE_MESSAGE", validateVpaResponse.getBody().getResultInfo().getResultMsg());
                statsDUtils.pushResponse("api/v1/vpa/validate", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "api/v1/vpa/validate" + "to grafana", exception);
            }
            LOGGER.info("VPA validate  response returned is: {}", validateVpaResponse);
            nativePaymentUtil.logNativeResponse(validateVpaResponse == null ? null : nativePaymentUtil
                    .getResultInfo(validateVpaResponse.getBody()));
            return validateVpaResponse;
        } finally {
            LOGGER.info("Total time taken for API is {} ms", System.currentTimeMillis() - startTime);
        }
    }

}