package com.paytm.pgplus.cashier.workflow;

import org.springframework.stereotype.Component;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.looper.model.CashierPaymentStatus;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.InitiatePaymentResponse;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

/**
 * CC DC Specific Workflow
 *
 * @author Lalit Mehra, Amit Dubey
 * @since March 7, 2016
 */
@Component(value = "BankcardWorkflow")
public class BankcardWorkflow extends CardPaymentWorkflow {

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_INIT)
    public InitiatePaymentResponse initiatePayment(CashierRequest cashierRequest) throws CashierCheckedException,
            PaytmValidationException {
        if (null == cashierRequest) {
            throw new CashierCheckedException("Process failed : cashier request can not be null");
        }

        if (null == cashierRequest.getCardRequest()) {
            throw new CashierCheckedException("Process failed : card request can not be null");
        }

        // call AbstractPaymentWorkflow initiatePayment
        InitiatePaymentResponse initiatePaymentResponse = super.initiatePayment(cashierRequest);

        if (null == initiatePaymentResponse) {
            throw new CashierCheckedException("Process failed : initiatePaymentResponse received null");
        }

        // fetch bank payment form ~ looper call
        CashierPaymentStatus cashierPaymentStatus = fetchBankForm(initiatePaymentResponse.getCashierRequestId());
        if (null == cashierPaymentStatus) {
            throw new CashierCheckedException("Process failed : cashierPaymentStatus received null");
        }
        initiatePaymentResponse.setCashierPaymentStatus(cashierPaymentStatus);

        return initiatePaymentResponse;

    }

}
