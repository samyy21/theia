package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.common.log.EventLogger;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.FetchMerchantUserInfoRequest;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantUserInfoResponse;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import io.swagger.annotations.ApiParam;
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
public class FetchMerchantUserInfoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchMerchantUserInfoController.class);

    private static final Logger EVENT_LOGGER = LoggerFactory.getLogger("EVENT_LOGGER");

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/fetchMerchantUserInfo", method = { RequestMethod.POST })
    public MerchantUserInfoResponse fetchMerchantUserInfo(
            @ApiParam(required = true) @RequestBody FetchMerchantUserInfoRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Fetch Merchant/User info request received for API: /fetchMerchantUserInfo is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            IRequestProcessor<FetchMerchantUserInfoRequest, MerchantUserInfoResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.FETCH_MERCHANT_USER_INFO);
            MerchantUserInfoResponse merchantUserInfoResponse = requestProcessor.process(request);
            LOGGER.error("Fetch Merchant/User info response for API: /fetchMerchantUserInfo is: {}",
                    merchantUserInfoResponse);
            return merchantUserInfoResponse;
        } finally {
            LOGGER.info("Total time taken for NativeFetchMerchantUserInfo is {} ms", System.currentTimeMillis()
                    - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }
}