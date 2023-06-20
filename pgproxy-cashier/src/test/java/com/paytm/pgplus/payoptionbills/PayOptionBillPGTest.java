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
public class PayOptionBillPGTest {

    private static final int SIZE = 1;
    private PayOptionBillGeneratorFactory factory;
    private List<CashierPayOptionBill> lst;
    private Map<PayMethod, String> payOptions = new HashMap<>();

    @Before
    public void prepareTestData() {
        factory = new PayOptionBillGeneratorFactory();
    }

    // @Test()
    public void payByDebitCard() throws CashierCheckedException {
        payOptions.put(PayMethod.DEBIT_CARD, "DEBIT_CARD_VISA");

        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 2L, payOptions).build();

        lst = factory.getPayOptionBillGenerator(PaymentType.ONLY_PG).generatePayOptionBill(payBillOptions);

        Assert.assertEquals(SIZE, lst.size());
    }

    // @Test()
    public void payByDebitCardWithWalletBalance() throws CashierCheckedException {
        payOptions.put(PayMethod.DEBIT_CARD, "DEBIT_CARD_VISA");

        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 2L, payOptions).setWalletBalance(100L).build();

        lst = factory.getPayOptionBillGenerator(PaymentType.ONLY_PG).generatePayOptionBill(payBillOptions);

        Assert.assertEquals(SIZE, lst.size());
    }

    // @Test()
    public void payByATM() throws CashierCheckedException {
        payOptions.put(PayMethod.ATM, "ATM");

        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 2L, payOptions).build();

        lst = factory.getPayOptionBillGenerator(PaymentType.ONLY_PG).generatePayOptionBill(payBillOptions);

        Assert.assertEquals(SIZE, lst.size());
    }

    // @Test()
    public void payByCreditCard() throws CashierCheckedException {
        payOptions.put(PayMethod.CREDIT_CARD, "CREDIT_CARD_VISA");

        PayBillOptions payBillOptions = new PayBillOptionsBuilder(0L, 0L, payOptions).build();

        lst = factory.getPayOptionBillGenerator(PaymentType.ONLY_PG).generatePayOptionBill(payBillOptions);

        Assert.assertEquals(SIZE, lst.size());
    }

    // @Test(expected = CashierCheckedException.class)
    public void payByOnlyWalletWithZeroBalance() throws CashierCheckedException {
        payOptions.put(PayMethod.BALANCE, "BALANCE");

        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 2L, payOptions).build();

        lst = factory.getPayOptionBillGenerator(PaymentType.ONLY_PG).generatePayOptionBill(payBillOptions);

        Assert.assertEquals(SIZE, lst.size());
    }

    // @Test(expected = CashierCheckedException.class)
    public void payByOnlyWalletWithBalance() throws CashierCheckedException {
        payOptions.put(PayMethod.BALANCE, "BALANCE");

        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 2L, payOptions).setWalletBalance(100L).build();

        lst = factory.getPayOptionBillGenerator(PaymentType.ONLY_PG).generatePayOptionBill(payBillOptions);

        Assert.assertEquals(SIZE, lst.size());
    }

}
