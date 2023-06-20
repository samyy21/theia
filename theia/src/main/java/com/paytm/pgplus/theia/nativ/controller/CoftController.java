package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.model.cardtoken.CardTokenDetailResponse;
import com.paytm.pgplus.theia.nativ.model.cardtoken.FetchCardTokenDetailRequest;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
@NativeControllerAdvice
public class CoftController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoftController.class);

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/fetchTokenDetail", method = { RequestMethod.POST })
    public CardTokenDetailResponse getTokenDetail(@RequestBody FetchCardTokenDetailRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Request received for API: /fetchTokenDetail is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            IRequestProcessor<FetchCardTokenDetailRequest, CardTokenDetailResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.FETCH_CARD_TOKEN_DETAILS);
            CardTokenDetailResponse response = requestProcessor.process(request);

            LOGGER.info("Response returned for API: /fetchTokenDetail is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return response;
        } finally {
            LOGGER.info("Total time taken for CoftController is {} ms", System.currentTimeMillis() - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }
}
