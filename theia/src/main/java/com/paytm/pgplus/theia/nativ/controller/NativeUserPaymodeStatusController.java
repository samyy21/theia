package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.localisationProcessor.annotation.LocaleAPI;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.model.user.UserPayModeStatusRequest;
import com.paytm.pgplus.theia.nativ.model.user.UserPayModeStatusResponse;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/v1")
@NativeControllerAdvice
public class NativeUserPaymodeStatusController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeUserPaymodeStatusController.class);

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;
    @Autowired
    private AWSStatsDUtils statsDUtils;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @LocaleAPI(apiName = "/theia/api/v1/fetchUserPaymentModeStatus", responseClass = UserPayModeStatusResponse.class)
    @RequestMapping(value = "/fetchUserPaymentModeStatus", method = { RequestMethod.POST })
    public UserPayModeStatusResponse fetchUserPaymodeStatus(@RequestBody UserPayModeStatusRequest request)
            throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: v1/fetchUserPaymentModeStatus is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            IRequestProcessor<UserPayModeStatusRequest, UserPayModeStatusResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.FETCH_USER_PAYMODE_STATUS);
            UserPayModeStatusResponse response = requestProcessor.process(request);
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", response.getBody().getResultInfo().getResultStatus());
                responseMap.put("RESPONSE_MESSAGE", response.getBody().getResultInfo().getResultMsg());
                statsDUtils.pushResponse("api/v1/fetchUserPaymentModeStatus", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "api/v1/fetchUserPaymentModeStatus" + "to grafana",
                        exception);
            }
            LOGGER.info("Native response returned for API: v1/fetchUserPaymentModeStatus is: {}", response);

            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return response;
        } finally {
            LOGGER.info("Total time taken for NativeFetchUserPaymentModeStatus is {} ms", System.currentTimeMillis()
                    - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

}
