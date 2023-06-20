package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.model.validateCard.CardNumberValidationRequest;
import com.paytm.pgplus.theia.nativ.model.validateCard.CardNumberValidationResponse;
import com.paytm.pgplus.theia.nativ.model.validateCard.ValidateCardRequest;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
@NativeControllerAdvice
public class NativeCardValidationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeCardValidationController.class);

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @RequestMapping(value = "/cardNumberValidation", method = { RequestMethod.POST })
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 500, message = "Internal Server error") })
    public CardNumberValidationResponse cardNumberValidation(@RequestBody CardNumberValidationRequest request)
            throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            setInMDC(request);
            LOGGER.info("Native request received for API: /cardNumberValidation is: {}", request);
            IRequestProcessor<CardNumberValidationRequest, CardNumberValidationResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.CARD_NUMBER_VALIDATION_REQUEST);
            CardNumberValidationResponse response = requestProcessor.process(request);
            LOGGER.info("Native response returned for API: /cardNumberValidation is: {}", response);
            return response;
        } finally {
            LOGGER.info("Total time taken for /cardNumberValidation is {} ms", System.currentTimeMillis() - startTime);
        }
    }

    private void setInMDC(CardNumberValidationRequest request) {
        if (request != null && request.getBody() != null) {
            if (StringUtils.isNotBlank(request.getBody().getRequestId())) {
                MDC.put(TheiaConstant.RequestParams.REQUEST_ID, request.getBody().getRequestId());
            }
            if (StringUtils.isBlank(MDC.get(TheiaConstant.RequestParams.MID))
                    && StringUtils.isNotBlank(request.getBody().getMid())) {
                MDC.put(TheiaConstant.RequestParams.MID, request.getBody().getMid());
            }
        }
    }
}
