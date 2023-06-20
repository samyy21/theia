package com.paytm.pgplus.theia.emiSubvention.controller;

import com.paytm.pgplus.theia.emiSubvention.model.request.banks.EmiBanksRequest;
import com.paytm.pgplus.theia.emiSubvention.model.request.tenures.EmiTenuresRequest;
import com.paytm.pgplus.theia.emiSubvention.model.request.validate.ValidateEmiRequest;
import com.paytm.pgplus.theia.emiSubvention.model.response.banks.EmiBanksResponse;
import com.paytm.pgplus.theia.emiSubvention.model.response.tenures.EmiTenuresResponse;
import com.paytm.pgplus.theia.emiSubvention.model.response.validate.ValidateEmiResponse;
import com.paytm.pgplus.theia.emiSubvention.util.EmiSubventionUtils;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.paymentoffer.util.PaymentOfferUtils;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.REQUEST_ID;

@NativeControllerAdvice
@RestController
@RequestMapping("api/v1/emiSubvention")
public class EmiSubventionController {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmiSubventionController.class);

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    private EmiSubventionUtils emiSubventionUtils;

    @RequestMapping(value = "/banks", method = { RequestMethod.POST })
    public EmiBanksResponse banksEmi(
            @ApiParam(required = true) @RequestParam(value = "referenceId", required = false) String referenceId,
            @RequestBody EmiBanksRequest request) throws Exception {

        long startTime = System.currentTimeMillis();

        try {
            EmiSubventionUtils.setRequestHeader(request.getHead());
            emiSubventionUtils.setParamsForBanksRequest(request, referenceId);
            LOGGER.info("Native request received for API: /banks is: {}", request);
            IRequestProcessor<EmiBanksRequest, EmiBanksResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.BANKS_EMI_SUBVENTION);
            EmiBanksResponse emiBanksResponse = requestProcessor.process(request);
            LOGGER.info("Native response returned  for API: /banks is: {}", emiBanksResponse);
            return emiBanksResponse;
        } finally {
            LOGGER.info("Total time taken for banks is {} ms ", System.currentTimeMillis() - startTime);
        }

    }

    @RequestMapping(value = " /tenures", method = { RequestMethod.POST })
    public EmiTenuresResponse tenuresEmi(
            @ApiParam(required = true) @RequestParam(value = "referenceId", required = false) String referenceId,
            @RequestBody EmiTenuresRequest request) throws Exception {

        long startTime = System.currentTimeMillis();
        try {
            EmiSubventionUtils.setRequestHeader(request.getHead());
            emiSubventionUtils.setParamsForTenureRequest(request, referenceId);
            LOGGER.info("Native request received for API: /tenures is: {}", request);
            IRequestProcessor<EmiTenuresRequest, EmiTenuresResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.TENURE_EMI_SUBVENTION);
            EmiTenuresResponse emiTenuresResponse = requestProcessor.process(request);
            LOGGER.info("Native response returned  for API: /tenures is: {}", emiTenuresResponse);
            return emiTenuresResponse;
        } finally {
            LOGGER.info("Total time taken for tenures is {} ms ", System.currentTimeMillis() - startTime);
        }

    }

    @RequestMapping(value = "/validateEmi", method = { RequestMethod.POST })
    public ValidateEmiResponse validateEmi(
            @ApiParam(required = true) @RequestParam(value = "referenceId", required = false) String referenceId,
            @RequestBody ValidateEmiRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            EmiSubventionUtils.setRequestHeader(request.getHead());
            emiSubventionUtils.setParamsForValidateRequest(request, referenceId);
            LOGGER.info("Native request received for API: /ValidateEmi is: {}", request);
            IRequestProcessor<ValidateEmiRequest, ValidateEmiResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.VALIDATE_EMI_SUBVENTION);
            ValidateEmiResponse validateEmiResponse = requestProcessor.process(request);
            LOGGER.info("Native response returned  for API: /ValidateEmi is: {}", validateEmiResponse);
            return validateEmiResponse;
        } finally {
            LOGGER.info("Total time taken for ValidateEmi is {} ms", System.currentTimeMillis() - startTime);
        }
    }
}
