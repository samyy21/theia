/**
 * 
 */
package com.paytm.pgplus.cashier.payoption;

import java.util.ArrayList;
import java.util.List;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.pay.model.CashierPayOptionBill;

/**
 * @author amit.dubey
 *
 */
public class PayOptionBillGeneratorOther implements IPayOptionBillGenerator {

    /*
     * (non-Javadoc)
     * 
     * @see com.paytm.pgplus.cashier.payoption.IPayOptionBillGenerator#
     * generatePayOptionBill(com.paytm.pgplus.cashier.payoption.PayBillOptions)
     */
    @Override
    public List<CashierPayOptionBill> generatePayOptionBill(PayBillOptions payOption) throws CashierCheckedException {
        return new ArrayList<CashierPayOptionBill>();
    }

}
