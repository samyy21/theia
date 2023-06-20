package com.paytm.pgplus.cashier.validator.service;

import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;

/**
 * @author kartik
 * @date 11-05-2017
 */
public interface IDigitalCreditPassCodeValidator {

    void validatePassCodeForPaytmCC(CashierRequest cashierRequest) throws PaytmValidationException;
}
