package com.paytm.pgplus.cashier.models.validator.impl;

import org.springframework.stereotype.Component;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.validator.IResponseValidator;
import com.paytm.pgplus.facade.payment.models.response.PayResultQueryResponse;

@Component
public class PayResultQueryResponseValidator implements IResponseValidator<PayResultQueryResponse> {

    @Override
    public void validInput(PayResultQueryResponse payResultQueryResponse) throws CashierCheckedException {
        String errorMsg = null;
        if (isEmpty(payResultQueryResponse)) {
            errorMsg = "Process failed : payResultQueryResponse received as null";
        } else if (isEmpty(payResultQueryResponse.getBody())) {
            errorMsg = "Process failed : payResultQueryResponse body received as null";
        } else if (isEmpty(payResultQueryResponse.getBody().getResultInfo())) {
            errorMsg = "Process failed : payResultQueryResponse body Result Info received as null";
        }
        if (errorMsg != null)
            throw new CashierCheckedException(errorMsg);
    }

}
