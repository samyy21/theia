package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.accesstoken.exception.RequestValidationException;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.model.auth.*;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory.RequestType;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.utils.CustomObjectMapperUtil;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/v2")
@NativeControllerAdvice
@Api(value = "NativeLoginAuthControllerV2", description = "It contains API's for user authentication using sendOtp and validateOtp.")
public class NativeLoginAuthControllerV2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeLoginAuthControllerV2.class);

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;
    @Autowired
    private AWSStatsDUtils statsDUtils;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/login/validateOtp", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "/login/validateOtp", notes = "API used to validate OTP send by user")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "SUCCESS"),
            @ApiResponse(code = 500, message = "Internal Server Error") })
    public Map<Object, Object> validateOtp(@ApiParam(required = true) @RequestBody ValidateOtpRequest request)
            throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /v2/login/validateOtp is: {}", request);
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
                statsDUtils.pushResponse("v2/login/validateOtp", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "v2/login/validateOtp" + "to grafana", exception);
            }
            LOGGER.info("Native response returned for API: /v2/login/validateOtp is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return CustomObjectMapperUtil.jsonToMap(CustomObjectMapperUtil.convertToString(response));
        } catch (RequestValidationException e) {
            ValidateOtpResponse response = new ValidateOtpResponse();
            response.setBody(new ValidateOtpResponseBody());
            response.getBody().setResultInfo(
                    nativePaymentUtil.resultInfo(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION));
            return CustomObjectMapperUtil.jsonToMap(CustomObjectMapperUtil.convertToString(response));
        } finally {
            LOGGER.info("Total time taken for NativeValidateOtp v2 is {} ms", System.currentTimeMillis() - startTime);
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
            LOGGER.info("Native userLogoutRequest v2 request received is: {}", userLogoutRequest);
            nativePaymentUtil.setReferenceIdAndMidInBody(userLogoutRequest);
            IRequestProcessor<NativeUserLogoutRequest, NativeUserLogoutResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.NATIVE_LOGOUT_USER);
            NativeUserLogoutResponse logoutResponse = requestProcessor.process(userLogoutRequest);
            LOGGER.info("Native /v2/logout/user response returned is: {}", logoutResponse);
            nativePaymentUtil.logNativeResponse(logoutResponse == null ? null : nativePaymentUtil
                    .getResultInfo(logoutResponse.getBody()));
            return logoutResponse;
        } finally {
            LOGGER.info("Total time taken for API /v2/logout/user is {} ms", System.currentTimeMillis() - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }
}
