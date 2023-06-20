/**
 * 
 */
package com.paytm.pgplus.payoptionbills;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;

import com.paytm.pgplus.cashier.enums.PaymentType;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.pay.model.CashierPayOptionBill;
import com.paytm.pgplus.cashier.payoption.PayBillOptions;
import com.paytm.pgplus.cashier.payoption.PayBillOptions.PayBillOptionsBuilder;
import com.paytm.pgplus.cashier.payoption.PayOptionBillGeneratorFactory;
import com.paytm.pgplus.facade.enums.PayMethod;

/**
 * @author amit.dubey
 *
 */
public class PayOptionBillWalletTest {
    private static final int SIZE = 1;
    private PayOptionBillGeneratorFactory factory;
    private List<CashierPayOptionBill> lst;
    private Map<PayMethod, String> payOptions = new HashMap<>();

    @Before
    public void prepareTestData() {
        payOptions.put(PayMethod.BALANCE, "BALANCE");

        factory = new PayOptionBillGeneratorFactory();
    }

    // @Test(expected = CashierCheckedException.class)
    public void zeroWalletBalance() throws CashierCheckedException {
        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 2L, payOptions).setWalletBalance(0L).build();

        lst = factory.getPayOptionBillGenerator(PaymentType.ONLY_WALLET).generatePayOptionBill(payBillOptions);
    }

    // @Test(expected = CashierCheckedException.class)
    public void lowAmount() throws CashierCheckedException {
        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 2L, payOptions).setWalletBalance(10L).build();

        lst = factory.getPayOptionBillGenerator(PaymentType.ONLY_WALLET).generatePayOptionBill(payBillOptions);
    }

    // @Test()
    public void ExactAmount() throws CashierCheckedException {
        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 2L, payOptions).setWalletBalance(12L).build();

        lst = factory.getPayOptionBillGenerator(PaymentType.ONLY_WALLET).generatePayOptionBill(payBillOptions);

        Assert.assertEquals(SIZE, lst.size());
    }

    // @Test()
    public void GreaterAmount() throws CashierCheckedException {
        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 2L, payOptions).setWalletBalance(15L).build();

        lst = factory.getPayOptionBillGenerator(PaymentType.ONLY_WALLET).generatePayOptionBill(payBillOptions);

        Assert.assertEquals(SIZE, lst.size());
    }

    // @Test()
    public void noChargeAmount() throws CashierCheckedException {
        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 0L, payOptions).setWalletBalance(15L).build();

        lst = factory.getPayOptionBillGenerator(PaymentType.ONLY_WALLET).generatePayOptionBill(payBillOptions);

        Assert.assertEquals(SIZE, lst.size());
    }

    // @Test()
    public void onlyChargeAmount() throws CashierCheckedException {
        PayBillOptions payBillOptions = new PayBillOptionsBuilder(0L, 2L, payOptions).setWalletBalance(2L).build();

        lst = factory.getPayOptionBillGenerator(PaymentType.ONLY_WALLET).generatePayOptionBill(payBillOptions);

        Assert.assertEquals(SIZE, lst.size());
    }

    // @Test(expected = CashierCheckedException.class)
    public void otherPayMethod() throws CashierCheckedException {
        payOptions.put(PayMethod.ATM, "ATM");

        PayBillOptions payBillOptions = new PayBillOptionsBuilder(0L, 2L, payOptions).setWalletBalance(2L).build();

        lst = factory.getPayOptionBillGenerator(PaymentType.ONLY_WALLET).generatePayOptionBill(payBillOptions);

        Assert.assertEquals(SIZE, lst.size());
    }

}
