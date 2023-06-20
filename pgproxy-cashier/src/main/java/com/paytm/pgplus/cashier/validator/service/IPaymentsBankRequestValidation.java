package com.paytm.pgplus.cashier.validator.service;

import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;

/**
 * @author kartik
 * @date 21-Sep-2017
 */
public interface IPaymentsBankRequestValidation {

    public void validatePaymentRequestViaSavingsAccount(CashierRequest cashierRequest) throws PaytmValidationException;

}
