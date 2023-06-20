/**
 *
 */
package com.paytm.pgplus.theia.services;

import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.cashier.models.SeamlessBankCardPayRequest;

/**
 * @author amitdubey
 * @date Dec 6, 2016
 */
public interface ISeamlessBankCardService {
    String doSeamlessBankCardPayment(String cashierRequestId, String transactionId) throws PaytmValidationException;

    SeamlessBankCardPayRequest getSeamlessBankCardPayRequest(String cashierRequestId);
}
