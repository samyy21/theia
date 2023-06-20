package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.core.risk.RiskVerifierPayload;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.nativ.model.risk.request.DoViewRequest;
import com.paytm.pgplus.theia.nativ.model.risk.request.DoViewResponse;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.service.IRiskVerifierService;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("riskVerifierDoViewRequestProcessor")
public class RiskVerifierDoViewRequestProcessor extends
        AbstractRequestProcessor<DoViewRequest, DoViewResponse, DoViewRequest, DoViewResponse> {

    @Autowired
    private IRiskVerifierService riskVerifierService;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Override
    protected DoViewRequest preProcess(DoViewRequest request) {
        return request;
    }

    @Override
    protected DoViewResponse onProcess(DoViewRequest request, DoViewRequest serviceRequest) throws Exception {
        RiskVerifierPayload payload = nativeValidationService.validateRiskVerifierToken(request.getHead().getToken());
        nativeValidationService.validateMidOrderId(payload.getMid(), payload.getOrderId());
        return riskVerifierService.doView(request);
    }

    @Override
    protected DoViewResponse postProcess(DoViewRequest request, DoViewRequest serviceRequest,
            DoViewResponse doViewResponse) throws Exception {
        return doViewResponse;
    }
}
