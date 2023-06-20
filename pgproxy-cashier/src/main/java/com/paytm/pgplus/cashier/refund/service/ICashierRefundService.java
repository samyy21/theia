package com.paytm.pgplus.cashier.refund.service;

/**
 * 
 */

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.refund.model.RefundRequest;
import com.paytm.pgplus.cashier.refund.model.RefundResponse;

/**
 * @author amit.dubey
 *
 */
public interface ICashierRefundService {

    RefundResponse processRefund(RefundRequest refundRequest) throws CashierCheckedException;

}
