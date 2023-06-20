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
 * ATM Specific Workflow
 *
 * @author Lalit Mehra, Amit Dubey
 * @since March 7, 2016
 */
@Component(value = "ATMWorkflow")
public class ATMWorkflow extends PaymentWorkflow {

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_INIT)
    public InitiatePaymentResponse initiatePayment(CashierRequest cashierRequest) throws CashierCheckedException,
            PaytmValidationException {
        validate(cashierRequest);

        // call AbstractPaymentWorkflow initiatePayment
        InitiatePaymentResponse initiatePaymentResponse = super.initiatePayment(cashierRequest);

        if (null == initiatePaymentResponse) {
            throw new CashierCheckedException("Process failed : initiate payment response can not be null");
        }

        // fetch bank payment form ~ looper call
        CashierPaymentStatus cashierPaymentStatus = fetchBankForm(initiatePaymentResponse.getCashierRequestId());

        if (null == cashierPaymentStatus) {
            throw new CashierCheckedException("Process failed : cashier payment status can not be null");
        }

        initiatePaymentResponse.setCashierPaymentStatus(cashierPaymentStatus);

        return initiatePaymentResponse;

    }

    private void validate(CashierRequest cashierRequest) throws CashierCheckedException {
        if (null == cashierRequest) {
            throw new CashierCheckedException("Process failed : cashier request can not be null");
        }

        switch (cashierRequest.getCashierWorkflow()) {
        case ADD_MONEY_ATM:
        case ATM:
            break;
        case ADD_MONEY_IMPS:
        case ADD_MONEY_ISOCARD:
        case ADD_MONEY_NB:
        case ADD_MONEY_UPI:
        case COD:
        case DIRECT_BANK_CARD_PAYMENT:
        case IMPS:
        case ISOCARD:
        case NB:
        case SUBSCRIPTION:
        case UPI:
        case WALLET:
        case DIGITAL_CREDIT_PAYMENT:
        case RISK_POLICY_CONSULT:
        default:
            throw new CashierCheckedException("Process failed : ATM or ADD_MONEY_ATM accepted as cashier work flow");
        }
    }

}