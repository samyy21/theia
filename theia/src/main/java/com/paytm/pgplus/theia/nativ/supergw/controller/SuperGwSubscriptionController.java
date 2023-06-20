package com.paytm.pgplus.theia.nativ.supergw.controller;

import com.paytm.pgplus.request.SubscriptionTransactionRequest;
import com.paytm.pgplus.response.SubscriptionTransactionResponse;
import com.paytm.pgplus.theia.annotation.SignedResponseBody;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.controller.NativeSubscriptionController;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
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

@RestController
@RequestMapping("api/v4")
@NativeControllerAdvice
public class SuperGwSubscriptionController {

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeSubscriptionController.class);

    @ApiOperation(value = "create subscription", notes = "To create subscription and start its transaction in native flow")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 500, message = "Internal Server error") })
    @RequestMapping(value = "/subscription/create", method = { RequestMethod.POST })
    @SuppressWarnings("unchecked")
    @SignedResponseBody()
    public SubscriptionTransactionResponse createSubscriptionTransaction(
            @ApiParam(required = true) @RequestBody SubscriptionTransactionRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Subscription Native request received for API: /v4/create/subscription is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());

            NativeInitiateRequest nativeInitiateRequest = new NativeInitiateRequest();
            nativeInitiateRequest.setInitiateTxnReq(request);
            request.setSuperGwHit(true);
            IRequestProcessor<SubscriptionTransactionRequest, SubscriptionTransactionResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.NATIVE_SUBSCRIPTION);
            SubscriptionTransactionResponse response = requestProcessor.process(request);
            LOGGER.info("Subscription response returned for API: /v4/create/subscription {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return response;
        } finally {
            LOGGER.info("Total time taken for v4CreateSubscriptionTransaction is {} ms", System.currentTimeMillis()
                    - startTime);
        }
    }
}
