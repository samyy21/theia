package com.paytm.pgplus.theia.offline.services;

import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.offline.exceptions.PaymentRequestProcessingException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;

/**
 * Created by rahulverma on 7/9/17.
 */
public interface IOfflinePaymentService {

    WorkFlowResponseBean processPaymentRequest(PaymentRequestBean requestData) throws PaymentRequestProcessingException;

    void validateRequestBean(CashierInfoRequest cashierInfoRequest) throws RequestValidationException;

}
