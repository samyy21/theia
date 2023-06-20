package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.model.validateCard.ValidateCardRequest;
import com.paytm.pgplus.theia.nativ.model.validateCard.ValidateCardResponse;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
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
public class NativeValidateCardController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeValidateCardController.class);

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @RequestMapping(value = "/validateBin", method = { RequestMethod.POST })
    public ValidateCardResponse validateCard(@RequestBody ValidateCardRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /validateCard is: {}", request);
            IRequestProcessor<ValidateCardRequest, ValidateCardResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.VALIDATE_CARD_REQUEST);
            ValidateCardResponse response = requestProcessor.process(request);
            LOGGER.info("Native response returned for API: /validateBin is: {}", response);
            return response;
        } finally {
            LOGGER.info("Total time taken for /validateBin is {} ms", System.currentTimeMillis() - startTime);
        }
    }
}
