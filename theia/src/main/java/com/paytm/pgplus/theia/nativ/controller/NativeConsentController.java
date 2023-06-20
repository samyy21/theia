package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.facade.consent.model.ConsentServiceRequest;
import com.paytm.pgplus.facade.consent.service.IConsentService;
import com.paytm.pgplus.facade.linkService.helper.LinkServiceHelper;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.model.consent.NativeConsentResponse;
import com.paytm.pgplus.theia.nativ.model.consent.NativeConsentResponseBody;
import com.paytm.pgplus.theia.nativ.model.consent.NativeConsentResponseHead;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by: satyamsinghrajput at 22/10/19
 */
@RestController
@RequestMapping("api/v1")
@NativeControllerAdvice
public class NativeConsentController {

    @Autowired
    IConsentService consentService;

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;
    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeConsentController.class);

    @RequestMapping(path = "/setConsent", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public NativeConsentResponse consent(@RequestBody ConsentServiceRequest consentServiceRequest) throws Exception {
        long startTime = System.currentTimeMillis();
        NativeConsentResponse response = new NativeConsentResponse();
        NativeConsentResponseBody body = new NativeConsentResponseBody();
        try {
            LOGGER.info("Native request received for API: /setConsent is: {}", consentServiceRequest);
            IRequestProcessor<ConsentServiceRequest, NativeConsentResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.NATIVE_CONSENT_API);
            response = requestProcessor.process(consentServiceRequest);
            LOGGER.info("Native response returned for API: /setConsent is: {}", response);

        } catch (Exception e) {
            LOGGER.error("Exception occured while processing consent api");
            throw e;
        } finally {
            LOGGER.info("Total time taken for NativeConsentController is {} ms", System.currentTimeMillis() - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
        return response;

    }

}
