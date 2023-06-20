package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.theia.nativ.model.one.click.request.CheckEnrollStatusRequest;
import com.paytm.pgplus.theia.nativ.model.one.click.request.DeEnrollOneClickRequest;
import com.paytm.pgplus.theia.nativ.model.one.click.response.CheckEnrollStatusResponse;
import com.paytm.pgplus.theia.nativ.model.one.click.response.DeEnrollOneClickResponse;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
public class OneClickController {

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    public static final Logger LOGGER = LoggerFactory.getLogger(OneClickController.class);

    @RequestMapping(value = "/oneClick/deEnroll", method = { RequestMethod.POST })
    public DeEnrollOneClickResponse deEnroll(@RequestBody DeEnrollOneClickRequest request) {

        DeEnrollOneClickResponse response = null;
        try {
            IRequestProcessor<DeEnrollOneClickRequest, DeEnrollOneClickResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.DE_ENROLL_ONE_CLICK);

            response = requestProcessor.process(request);
        } catch (Exception e) {
            response = new DeEnrollOneClickResponse(ResultCode.FAILED);
        }
        return response;
    }

    @RequestMapping(value = "/card/deEnroll", method = { RequestMethod.POST })
    public ResponseEntity<DeEnrollOneClickResponse> carddeEnroll(
            @RequestParam(value = "mid", required = false) String mid,
            @RequestParam(value = "referenceId", required = false) String referenceId,
            @RequestBody DeEnrollOneClickRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        DeEnrollOneClickResponse response = null;
        try {
            LOGGER.info("OneClickController.carddeEnroll DeEnrollOneClickRequest :{}, mid:{}, referenceId:{}", request,
                    mid, referenceId);
            IRequestProcessor<DeEnrollOneClickRequest, DeEnrollOneClickResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.DE_ENROLL_ONE_CLICK);
            if (request.getBody() != null) {
                request.getBody().setMid(mid);
                request.getBody().setReferenceId(referenceId);
            }
            response = requestProcessor.process(request);
        } catch (RequestValidationException e) {
            if (e.getResultInfo() != null && "2004".equals(e.getResultInfo().getResultCode())) {
                return new ResponseEntity<DeEnrollOneClickResponse>(new DeEnrollOneClickResponse(e.getResultInfo()),
                        HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<DeEnrollOneClickResponse>(new DeEnrollOneClickResponse(e.getResultInfo()),
                    HttpStatus.OK);
        } finally {
            LOGGER.info("Total time taken for deenroll status check is {} ms", System.currentTimeMillis() - startTime);
        }
        return new ResponseEntity<DeEnrollOneClickResponse>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/card/enrollmentStatus", method = { RequestMethod.POST })
    public ResponseEntity<CheckEnrollStatusResponse> enrollmentStatus(
            @RequestParam(value = "mid", required = false) String mid,
            @RequestParam(value = "referenceId", required = false) String referenceId,
            @RequestBody CheckEnrollStatusRequest request) throws Exception {
        CheckEnrollStatusResponse response = null;
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("OneClickController.enrollmentStatus CheckEnrollStatusRequest :{}, mid:{}, referenceId:{}",
                    request, mid, referenceId);
            IRequestProcessor<CheckEnrollStatusRequest, CheckEnrollStatusResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.CARD_ENROLLMENT_STATUS);
            if (request.getBody() != null) {
                request.getBody().setMid(mid);
                request.getBody().setReferenceId(referenceId);
            }
            response = requestProcessor.process(request);
        } catch (RequestValidationException e) {
            if (e.getResultInfo() != null && "2004".equals(e.getResultInfo().getResultCode())) {
                return new ResponseEntity<CheckEnrollStatusResponse>(new CheckEnrollStatusResponse(e.getResultInfo()),
                        HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<CheckEnrollStatusResponse>(new CheckEnrollStatusResponse(e.getResultInfo()),
                    HttpStatus.OK);
        } finally {
            LOGGER.info("Total time taken for Enroll status check is {} ms", System.currentTimeMillis() - startTime);
        }
        return new ResponseEntity<CheckEnrollStatusResponse>(response, HttpStatus.OK);
    }
}