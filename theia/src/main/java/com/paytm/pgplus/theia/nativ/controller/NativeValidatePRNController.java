package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.model.auth.ValidateOtpRequest;
import com.paytm.pgplus.theia.nativ.model.auth.ValidateOtpResponse;
import com.paytm.pgplus.theia.nativ.model.prn.NativeValidatePRNRequest;
import com.paytm.pgplus.theia.nativ.model.prn.NativeValidatePRNResponse;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.service.PRNValidationService;
import com.paytm.pgplus.theia.utils.EventUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;

@RestController
@RequestMapping("api/v1")
@NativeControllerAdvice
public class NativeValidatePRNController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeValidatePRNController.class);

    @Autowired
    private PRNValidationService prnValidationService;

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @RequestMapping(value = "/validatePRN", method = RequestMethod.POST)
    public NativeValidatePRNResponse validatePRN(@RequestBody NativeValidatePRNRequest request) throws Exception {

        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /validatePRN is: {}", request);
            EventUtils.pushTheiaEvents(EventNameEnum.PRN_VALIDATION_REQUESTED);
            IRequestProcessor<NativeValidatePRNRequest, NativeValidatePRNResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.VALIDATE_PRN_REQUEST);
            NativeValidatePRNResponse response = requestProcessor.process(request);
            LOGGER.info("Native response returned for API: /validatePRN is: {}", response);
            return response;
        } finally {
            LOGGER.info("Total time taken for /validatePRN is {} ms", System.currentTimeMillis() - startTime);
        }
    }
}
