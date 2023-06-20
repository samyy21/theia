package com.paytm.pgplus.theia.nativ.processor.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.service.IPayviewConsultService;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.offline.model.response.CashierInfoResponse;

//TODO: This need to implement once offline API migrate to this design.
@Service
public class OfflinePayviewConsultRequestProcessor extends
        AbstractRequestProcessor<CashierInfoRequest, CashierInfoResponse, CashierInfoRequest, CashierInfoResponse> {

    @Autowired
    @Qualifier("userLoggedInLitePayviewConsultService")
    private IPayviewConsultService userLoggedInPayviewService;

    @Override
    protected CashierInfoRequest preProcess(CashierInfoRequest request) {
        /*
         * enrich(request); return request;
         */
        return null;
    }

    @Override
    protected CashierInfoResponse onProcess(CashierInfoRequest request, CashierInfoRequest serviceRequest) {
        /*
         * IPayviewConsultService payviewConsultService =
         * getPayviewConsultService(serviceRequest);
         * payviewConsultService.validate(serviceRequest); CashierInfoResponse
         * response = payviewConsultService.process(request, serviceRequest);
         * return response;
         */
        return null;
    }

    @Override
    protected CashierInfoResponse postProcess(CashierInfoRequest request, CashierInfoRequest serviceReq,
            CashierInfoResponse response) {
        // decorate based on offline request flow.
        /*
         * String orderIdGenerated =
         * OfflinePaymentUtils.generateOrderId(request.getHead().getMid());
         * response.getBody().setOrderId(orderIdGenerated);
         * OfflinePaymentUtils.updateOrderIdInMDC(orderIdGenerated); return
         * response;
         */
        return null;
    }

    private IPayviewConsultService getPayviewConsultService(CashierInfoRequest request) {
        return userLoggedInPayviewService;
    }

    private void enrich(CashierInfoRequest request) {
        request.getBody().setOrderId("dummyOrderId");
    }

}