package com.paytm.pgplus.cashier.workflow;

import org.springframework.stereotype.Component;

import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.InitiatePaymentResponse;

/**
 * Seamless Workflow
 *
 * @author Lalit Mehra
 * @since March 8, 2016
 */
@Component(value = "SeamlessWorkflow")
public class SeamlessWorkflow extends PaymentWorkflow {

    @Override
    public InitiatePaymentResponse initiatePayment(CashierRequest payload) throws CashierCheckedException {
        throw new UnsupportedOperationException(CashierConstant.UNSUPPORTED_OPERATION);
    }

}
