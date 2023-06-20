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
public class PayOptionBillHybridTest {

    private static final int SIZE = 2;
    private PayOptionBillGeneratorFactory factory;
    private List<CashierPayOptionBill> lst;
    PaymentType paymentType;
    private Map<PayMethod, String> payOptions = new HashMap<>();

    @Before
    public void prepareTestData() {
        payOptions.put(PayMethod.BALANCE, "BALANCE");

        factory = new PayOptionBillGeneratorFactory();
        paymentType = PaymentType.HYBRID;
    }

    // @Test(expected = CashierCheckedException.class)
    public void onlyOnePayMethod() throws CashierCheckedException {
        payOptions = new HashMap<>();
        payOptions.put(PayMethod.DEBIT_CARD, "DEBIT_CARD_VISA");

        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 2L, payOptions).setWalletBalance(15L).build();

        lst = factory.getPayOptionBillGenerator(paymentType).generatePayOptionBill(payBillOptions);
    }

    // @Test(expected = CashierCheckedException.class)
    public void twoPayMethodWithBalance() throws CashierCheckedException {
        payOptions = new HashMap<>();
        payOptions.put(PayMethod.DEBIT_CARD, "DEBIT_CARD_VISA");
        payOptions.put(PayMethod.CREDIT_CARD, "CREDIT_CARD_VISA");

        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 2L, payOptions).setWalletBalance(15L).build();

        lst = factory.getPayOptionBillGenerator(paymentType).generatePayOptionBill(payBillOptions);
    }

    // @Test(expected = CashierCheckedException.class)
    public void payByDebitCard() throws CashierCheckedException {
        payOptions.put(PayMethod.DEBIT_CARD, "DEBIT_CARD_VISA");

        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 2L, payOptions).setWalletBalance(15L).build();

        lst = factory.getPayOptionBillGenerator(paymentType).generatePayOptionBill(payBillOptions);
    }

    // @Test()
    public void payByATM() throws CashierCheckedException {
        payOptions.put(PayMethod.ATM, "ATM");

        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 2L, payOptions).setWalletBalance(11L)
                .setPayerAccountNumber("23333").build();

        lst = factory.getPayOptionBillGenerator(paymentType).generatePayOptionBill(payBillOptions);

        Assert.assertEquals(SIZE, lst.size());
    }

    // @Test(expected = CashierCheckedException.class)
    public void payByCreditCardWithWalletBalance() throws CashierCheckedException {
        payOptions.put(PayMethod.CREDIT_CARD, "CREDIT_CARD_VISA");

        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 2L, payOptions).setWalletBalance(12L).build();

        lst = factory.getPayOptionBillGenerator(paymentType).generatePayOptionBill(payBillOptions);

    }

    // @Test(expected = CashierCheckedException.class)
    public void payByCreditCardWithoutWalletBalance() throws CashierCheckedException {
        payOptions.put(PayMethod.CREDIT_CARD, "CREDIT_CARD_VISA");
        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 2L, payOptions).setWalletBalance(0L).build();

        lst = factory.getPayOptionBillGenerator(paymentType).generatePayOptionBill(payBillOptions);

        Assert.assertEquals(SIZE, lst.size());
    }

    // @Test(expected = CashierCheckedException.class)
    public void payByCreditCardWithNULLWalletBalance() throws CashierCheckedException {
        payOptions.put(PayMethod.CREDIT_CARD, "CREDIT_CARD_VISA");
        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 2L, payOptions).build();

        lst = factory.getPayOptionBillGenerator(paymentType).generatePayOptionBill(payBillOptions);

        Assert.assertEquals(SIZE, lst.size());
    }
}