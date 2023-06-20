/**
 * 
 */
package com.paytm.pgplus.cashier.payoption;

import java.util.List;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.pay.model.CashierPayOptionBill;

/**
 * @author amit.dubey
 *
 */
public interface IPayOptionBillGenerator {
    List<CashierPayOptionBill> generatePayOptionBill(PayBillOptions payOption) throws CashierCheckedException;
}
