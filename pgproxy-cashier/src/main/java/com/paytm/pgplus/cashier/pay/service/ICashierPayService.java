/**
 *
 */
package com.paytm.pgplus.cashier.pay.service;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;

/**
 * @author amit.dubey
 *
 */
public interface ICashierPayService {
    String submitPay(CashierRequest cashierRequest) throws CashierCheckedException, PaytmValidationException;
}
