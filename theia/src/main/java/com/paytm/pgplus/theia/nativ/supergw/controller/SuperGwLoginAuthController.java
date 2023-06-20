package com.paytm.pgplus.theia.nativ.supergw.controller;

import com.paytm.pgplus.localisationProcessor.annotation.LocaleAPI;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.model.response.ValidateVpaV4Response;
import com.paytm.pgplus.theia.nativ.model.vpaValidate.ValidateVpaV4Request;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import com.paytm.pgplus.theiacommon.supergw.constants.Constants;
import com.paytm.pgplus.theiacommon.supergw.request.SendOtpRequest;
import com.paytm.pgplus.theiacommon.supergw.request.ValidateOtpRequest;
import com.paytm.pgplus.theiacommon.supergw.response.SendOtpResponse;
import com.paytm.pgplus.theiacommon.supergw.response.ValidateOtpResponse;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.VALIDATE_VPA_V4_URI;

@RestController
@RequestMapping("api/v4")
@NativeControllerAdvice
@Api(value = "NativeLoginAuthControllerV4", description = "It contains version-4 of API's for user authentication using sendOtp and validateOtp.")
public class SuperGwLoginAuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuperGwLoginAuthController.class);

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    private static class LoginAuthConstants {
        private static final String REQUEST_RECEIVED_LOGGER = "request received for api {} is : {}";
        private static final String RESPONSE_RETURNED_LOGGER = "response returned for api {} is : {}";
        private static final String TIME_TAKEN_BY_API_LOGGER = "Total time taken for api {} is {} ms";
    }

    @RequestMapping(value = "vpa/validate", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "vpa/validate", notes = "API used to validate VPA")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "SUCCESS"),
            @ApiResponse(code = 500, message = "Internal Server Error") })
    public ValidateVpaV4Response vpaValidate(
            @Valid @ApiParam(required = true) @RequestBody ValidateVpaV4Request request,
            HttpServletRequest httpServletRequest) throws Exception {
        long startTime = System.currentTimeMillis();
        ValidateVpaV4Response response = null;
        try {
            LOGGER.info(LoginAuthConstants.REQUEST_RECEIVED_LOGGER, VALIDATE_VPA_V4_URI, request);
            IRequestProcessor<ValidateVpaV4Request, ValidateVpaV4Response> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.SUPERGW_VALIDATE_VPA_REQUEST);
            request.getBody().setQueryParams(httpServletRequest.getQueryString());
            response = requestProcessor.process(request);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
        } finally {
            LOGGER.info(LoginAuthConstants.TIME_TAKEN_BY_API_LOGGER, VALIDATE_VPA_V4_URI, System.currentTimeMillis()
                    - startTime);
            LOGGER.info(LoginAuthConstants.RESPONSE_RETURNED_LOGGER, VALIDATE_VPA_V4_URI, response);
            nativePaymentUtil.logNativeResponse(startTime);
        }
        return response;
    }

    @LocaleAPI(apiName = "/theia/api/v4/login/sendOtp", responseClass = SendOtpResponse.class)
    @RequestMapping(value = "/login/sendOtp", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "/login/sendOtp", notes = "API used to send OTP to user mobile")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "SUCCESS"),
            @ApiResponse(code = 500, message = "Internal Server Error") })
    public SendOtpResponse sendOtp(@Valid @ApiParam(required = true) @RequestBody SendOtpRequest request)
            throws Exception {
        long startTime = System.currentTimeMillis();
        SendOtpResponse response = null;
        try {
            LOGGER.info(LoginAuthConstants.REQUEST_RECEIVED_LOGGER, Constants.ApiUrls.NATIVE_SEND_OTP_V4_URI, request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            IRequestProcessor<SendOtpRequest, SendOtpResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.SUPERGW_SEND_OTP_REQUEST);
            response = requestProcessor.process(request);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
        } finally {
            LOGGER.info(LoginAuthConstants.TIME_TAKEN_BY_API_LOGGER, Constants.ApiUrls.NATIVE_SEND_OTP_V4_URI,
                    System.currentTimeMillis() - startTime);
            LOGGER.info(LoginAuthConstants.RESPONSE_RETURNED_LOGGER, Constants.ApiUrls.NATIVE_SEND_OTP_V4_URI, response);
            nativePaymentUtil.logNativeResponse(startTime);
        }
        return response;
    }

    @LocaleAPI(apiName = "/theia/api/v4/login/validateOtp", responseClass = ValidateOtpResponse.class)
    @RequestMapping(value = "/login/validateOtp", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "/login/validateOtp", notes = "API used to validate OTP send by user")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "SUCCESS"),
            @ApiResponse(code = 500, message = "Internal Server Error") })
    public ValidateOtpResponse validateOtp(@Valid @ApiParam(required = true) @RequestBody ValidateOtpRequest request)
            throws Exception {
        long startTime = System.currentTimeMillis();
        ValidateOtpResponse response = null;
        try {
            LOGGER.info(LoginAuthConstants.REQUEST_RECEIVED_LOGGER, Constants.ApiUrls.NATIVE_VALIDATE_OTP_V4_URI,
                    request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            IRequestProcessor<ValidateOtpRequest, ValidateOtpResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.SUPERGW_VALIDATE_OTP_REQUEST);
            response = requestProcessor.process(request);
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", response.getBody().getResultInfo().getResultStatus());
                responseMap.put("RESPONSE_MESSAGE", response.getBody().getResultInfo().getResultMsg());
                statsDUtils.pushResponse("v4/login/validateOtp", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "v4/login/validateOtp" + "to grafana", exception);
            }
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
        } finally {
            LOGGER.info(LoginAuthConstants.TIME_TAKEN_BY_API_LOGGER, Constants.ApiUrls.NATIVE_VALIDATE_OTP_V4_URI,
                    System.currentTimeMillis() - startTime);
            LOGGER.info(LoginAuthConstants.RESPONSE_RETURNED_LOGGER, Constants.ApiUrls.NATIVE_VALIDATE_OTP_V4_URI,
                    response);
            nativePaymentUtil.logNativeResponse(startTime);
        }
        return response;
    }

}