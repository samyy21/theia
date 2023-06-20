/**
 * 
 */
package com.paytm.pgplus.cashier.pay.validator;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.exception.CashierInvalidParameterException;
import com.paytm.pgplus.cashier.pay.model.PaymentRequest;
import com.paytm.pgplus.cashier.validator.BeanParameterValidator;

/**
 * @author amit.dubey
 *
 */
public class PayParameterValidator {
    private PaymentRequest paymentRequest;

    /**
     * @param paymentRequest
     */
    public PayParameterValidator(PaymentRequest paymentRequest) {
        this.paymentRequest = paymentRequest;
    }

    public boolean isValidPayMethod() throws CashierCheckedException {

        switch (paymentRequest.getPaymentType()) {
        case ONLY_WALLET:
            userLogedInValidation();
            break;
        case HYBRID:
            userLogedInValidation();
            break;
        case ADDNPAY:
            userLogedInValidation();
            break;
        case HYBRID_COD:
        case ONLY_COD:
        case ONLY_PG:
        case OTHER:
        default:
            break;
        }
        return true;
    }

    /**
     * @throws CashierInvalidParameterException
     */
    private void userLogedInValidation() throws CashierInvalidParameterException {
        BeanParameterValidator.validateInputStringParam(paymentRequest.getPayerUserId(), "payerUserId");
        BeanParameterValidator.validateInputObjectParam(paymentRequest.getPayBillOptions().getWalletBalance(),
                "walletBalance");
        BeanParameterValidator.validateInputStringParam(paymentRequest.getPayBillOptions().getPayerAccountNo(),
                "payerAccountNumber");
    }
}