/**
 * Alipay.com Inc. * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.model.user.SetUserPreferenceRequest;
import com.paytm.pgplus.theia.nativ.model.user.SetUserPreferenceResponse;
import com.paytm.pgplus.theia.nativ.model.user.SetUserPreferenceResponseBody;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.UPSConstants.ResponseConstants.SET_USER_PREFERENCE_FAILURE_MESSAGE;

@RestController
@RequestMapping("api/v1")
@NativeControllerAdvice
public class UserPreferenceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserPreferenceController.class);

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @RequestMapping(value = "/captureUserPreference", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "/captureUserPreference")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "SUCCESS"),
            @ApiResponse(code = 500, message = "Internal Server Error") })
    public SetUserPreferenceResponse setUserPreference(
            @ApiParam(required = true) @RequestBody SetUserPreferenceRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Request received for API: /v1/captureUserPreference is: {}", request);

            IRequestProcessor<SetUserPreferenceRequest, SetUserPreferenceResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.SET_USER_PREFERENCE);
            SetUserPreferenceResponse response = requestProcessor.process(request);
            LOGGER.info("Response returned for API: /v1/captureUserPreference is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return response;
        } finally {
            LOGGER.info("Total time taken for SetUserPreference api is {} ms", System.currentTimeMillis() - startTime);
        }
    }

    private SetUserPreferenceResponse generateSetUserPreferenceErrorResponse() {
        SetUserPreferenceResponse response = new SetUserPreferenceResponse(new ResponseHeader(),
                new SetUserPreferenceResponseBody());
        response.getBody().setMessage(SET_USER_PREFERENCE_FAILURE_MESSAGE);
        response.getBody().setResultInfo(new ResultInfo("00000900", "F", "SYSTEM_ERROR"));
        return response;
    }

}