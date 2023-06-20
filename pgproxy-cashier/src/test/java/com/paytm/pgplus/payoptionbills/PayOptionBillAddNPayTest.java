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
public class PayOptionBillAddNPayTest {
    private static final int SIZE = 2;
    private PayOptionBillGeneratorFactory factory;
    private List<CashierPayOptionBill> lst;
    PaymentType paymentType;
    boolean addNpay;
    private Map<PayMethod, String> payOptions = new HashMap<>();

    @Before
    public void prepareTestData() {
        payOptions.put(PayMethod.BALANCE, "BALANCE");

        factory = new PayOptionBillGeneratorFactory();
        paymentType = PaymentType.ADDNPAY;
    }

    // @Test(expected = CashierCheckedException.class)
    public void payWithoutAddNPayParam() throws CashierCheckedException {
        payOptions.put(PayMethod.DEBIT_CARD, "DEBIT_CARD_VISA");

        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 2L, payOptions).setWalletBalance(10L).build();

        lst = factory.getPayOptionBillGenerator(paymentType).generatePayOptionBill(payBillOptions);
    }

    // @Test(expected = CashierCheckedException.class)
    public void payWithAddNPayParamFalse() throws CashierCheckedException {
        payOptions.put(PayMethod.DEBIT_CARD, "DEBIT_CARD_VISA");

        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 2L, payOptions).setWalletBalance(10L)
                .setTopAndPay(addNpay).build();

        lst = factory.getPayOptionBillGenerator(paymentType).generatePayOptionBill(payBillOptions);
    }

    // @Test()
    public void payWithAddNPayParamTrue() throws CashierCheckedException {
        addNpay = true;
        payOptions.put(PayMethod.ATM, "ATM");

        Map<String, String> extendInfo = new HashMap<>();
        extendInfo.put("merchantTransId", "order_id_nair35");
        extendInfo.put("paytmMerchantId", "klbGlV59135347348753");
        extendInfo.put("alipayMerchantId", "216820000000000023235");
        extendInfo.put("ssoToken", "9a00d797-8b09-429b-aae1-ca8539be3a75");
        extendInfo.put("mccCode", "Retail");

        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 2L, payOptions).setWalletBalance(11L)
                .setTopAndPay(addNpay).setPayerAccountNumber("123").setExtendInfo(extendInfo).build();

        lst = factory.getPayOptionBillGenerator(paymentType).generatePayOptionBill(payBillOptions);

        org.junit.Assert.assertEquals(SIZE, lst.size());
    }

    // @Test(expected = CashierCheckedException.class)
    public void payWithoutAddNPayParamTrueAndWalletBalanceLarge() throws CashierCheckedException {
        addNpay = true;
        payOptions.put(PayMethod.ATM, "ATM");

        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 2L, payOptions).setWalletBalance(15L)
                .setTopAndPay(addNpay).build();

        lst = factory.getPayOptionBillGenerator(paymentType).generatePayOptionBill(payBillOptions);
    }

    // @Test()
    public void payByCreditCard() throws CashierCheckedException {
        addNpay = true;

        payOptions.put(PayMethod.DEBIT_CARD, "DEBIT_CARD_VISA");

        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 2L, payOptions).setWalletBalance(10L)
                .setTopAndPay(addNpay).build();

        lst = factory.getPayOptionBillGenerator(paymentType).generatePayOptionBill(payBillOptions);

        Assert.assertEquals(SIZE, lst.size());
    }

    // @Test(expected = CashierCheckedException.class)
    public void payByCreditCardWithOnlyOnePayMethod() throws CashierCheckedException {
        addNpay = true;

        PayBillOptions payBillOptions = new PayBillOptionsBuilder(10L, 2L, payOptions).setWalletBalance(10L)
                .setTopAndPay(addNpay).build();

        lst = factory.getPayOptionBillGenerator(paymentType).generatePayOptionBill(payBillOptions);
    }

}
