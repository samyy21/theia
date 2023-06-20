package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.biz.workflow.model.ValidationServicePreTxnResponse;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.theia.models.ImeiValidateRequest;
import com.paytm.pgplus.theia.models.ImeiValidateRequestBody;
import com.paytm.pgplus.theia.models.ImeiValidateResponse;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.exception.ImeiValidationException;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@NativeControllerAdvice
@RequestMapping("api/v1/")
public class NativeImeiController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeImeiController.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(NativeImeiController.class);
    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @RequestMapping(value = "imei", method = { RequestMethod.POST })
    public ImeiValidateResponse imeiValidate(@RequestBody ImeiValidateRequest imeiValidateRequest) {
        final long startTime = System.currentTimeMillis();
        LOGGER.info("request of imeiValidateBlockController : {}", imeiValidateRequest);
        ImeiValidateResponse imeiValidateResponse = null;
        try {
            IRequestProcessor<ImeiValidateRequestBody, ValidationServicePreTxnResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.IMEI_VALIDATE_API);
            ValidationServicePreTxnResponse validationServiceResponse = requestProcessor.process(imeiValidateRequest
                    .getBody());
            imeiValidateResponse = mapValidationServiceResponse(validationServiceResponse);
        } catch (ImeiValidationException ive) {
            imeiValidateResponse = new ImeiValidateResponse();
            imeiValidateResponse.setResultInfo(ive.getImeiValidationExceptionResultInfo());
            imeiValidateResponse.setMid(imeiValidateRequest.getBody().getMid());
            imeiValidateResponse.setOrderId(imeiValidateRequest.getBody().getOrderId());
        } catch (Exception ex) {
            LOGGER.error("Something went wrong in Imei Validation: {}", ex);
            imeiValidateResponse = new ImeiValidateResponse();
            imeiValidateResponse.setResultInfo(OfflinePaymentUtils.resultInfo(ResultCode.FAILED));
            imeiValidateResponse.setMid(imeiValidateRequest.getBody().getMid());
            imeiValidateResponse.setOrderId(imeiValidateRequest.getBody().getOrderId());
        }
        LOGGER.info("Total time taken for Imei Block is {} ms", System.currentTimeMillis() - startTime);
        return imeiValidateResponse;
    }

    private ImeiValidateResponse mapValidationServiceResponse(ValidationServicePreTxnResponse validationServiceResponse) {
        ImeiValidateResponse imeiValidateResponse = new ImeiValidateResponse();
        imeiValidateResponse.setOrderId(validationServiceResponse.getBody().getOrderId());
        imeiValidateResponse.setMid(validationServiceResponse.getBody().getMid());
        ResultInfo resultInfo = new ResultInfo(validationServiceResponse.getBody().getResultInfo().getResultStatus(),
                validationServiceResponse.getBody().getResultInfo().getResultCode(), validationServiceResponse
                        .getBody().getResultInfo().getResultMsg());
        imeiValidateResponse.setResultInfo(resultInfo);
        return imeiValidateResponse;
    }
}