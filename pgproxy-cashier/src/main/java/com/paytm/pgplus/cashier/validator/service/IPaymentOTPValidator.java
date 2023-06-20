/**
 * 
 */
package com.paytm.pgplus.cashier.validator.service;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.validator.model.PaymentOTP;

/**
 * @author amit.dubey
 *
 */
public interface IPaymentOTPValidator {
    boolean validate(PaymentOTP paymentOTP) throws CashierCheckedException;
}
