package com.paytm.pgplus.cashier.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.looper.model.CashierPaymentStatus;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.InitiatePaymentResponse;
import com.paytm.pgplus.cashier.validator.ValidationHelper;
import com.paytm.pgplus.cashier.validator.service.IPaymentsBankRequestValidation;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

/**
 * Netbanking Specific Workflow
 *
 * @author Lalit Mehra, Amit Dubey
 * @since March 7, 2016
 */
@Component(value = "NetbankingWorkflow")
public class NetbankingWorkflow extends PaymentWorkflow {

    @Autowired
    @Qualifier("paymentsBankRequestValidationImpl")
    private IPaymentsBankRequestValidation paymentsBankRequestValidationImpl;

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_VALIDATION)
    public ValidationHelper validate(ValidationHelper validationHelper) {
        validationHelper = super.validate(validationHelper);
        return validationHelper.validateSBIAddMoneyRequest().validateNBMaxTxnAmountLimit();
    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_INIT)
    public InitiatePaymentResponse initiatePayment(CashierRequest cashierRequest) throws CashierCheckedException,
            PaytmValidationException {
        validateInitiatePaymentRequest(cashierRequest);
        paymentsBankRequestValidationImpl.validatePaymentRequestViaSavingsAccount(cashierRequest);

        // call AbstractPaymentWorkflow initiatePayment
        InitiatePaymentResponse initiatePaymentResponse = super.initiatePayment(cashierRequest);
        if (null == initiatePaymentResponse) {
            throw new CashierCheckedException("Process failed : initiatePaymentResponse received as null");
        }

        // fetch bank payment form ~ looper call
        CashierPaymentStatus cashierPaymentStatus = fetchBankForm(initiatePaymentResponse.getCashierRequestId());
        if (null == cashierPaymentStatus) {
            throw new CashierCheckedException("Process failed : cashierPaymentStatus received as null");
        }

        initiatePaymentResponse.setCashierPaymentStatus(cashierPaymentStatus);

        return initiatePaymentResponse;

    }

    private void validateInitiatePaymentRequest(CashierRequest cashierRequest) throws CashierCheckedException {
        if (null == cashierRequest) {
            throw new CashierCheckedException("Manadatory parameter is missing :  cashier request");
        }

        if (null == cashierRequest.getPaymentRequest()) {
            throw new CashierCheckedException("Manadatory parameter is missing : payment request");
        }

    }

}
