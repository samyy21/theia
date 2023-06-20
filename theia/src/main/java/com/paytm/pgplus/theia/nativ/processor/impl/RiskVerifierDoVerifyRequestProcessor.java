package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.core.risk.RiskVerifierPayload;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.nativ.model.risk.response.DoVerifyRequest;
import com.paytm.pgplus.theia.nativ.model.risk.response.DoVerifyResponse;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.service.IRiskVerifierService;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("riskVerifierDoVerifyRequestProcessor")
public class RiskVerifierDoVerifyRequestProcessor extends
        AbstractRequestProcessor<DoVerifyRequest, DoVerifyResponse, DoVerifyRequest, DoVerifyResponse> {

    @Autowired
    private IRiskVerifierService riskVerifierService;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Override
    protected DoVerifyRequest preProcess(DoVerifyRequest request) {
        return request;
    }

    @Override
    protected DoVerifyResponse onProcess(DoVerifyRequest request, DoVerifyRequest request2) throws Exception {
        RiskVerifierPayload payload = nativeValidationService.validateRiskVerifierToken(request.getHead().getToken());
        nativeValidationService.validateMidOrderId(payload.getMid(), payload.getOrderId());
        return riskVerifierService.doVerify(request);
    }

    @Override
    protected DoVerifyResponse postProcess(DoVerifyRequest request, DoVerifyRequest request2,
            DoVerifyResponse doVerifyResponse) throws Exception {
        return doVerifyResponse;
    }
}
