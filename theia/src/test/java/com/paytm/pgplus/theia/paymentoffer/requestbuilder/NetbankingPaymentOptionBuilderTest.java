package com.paytm.pgplus.theia.paymentoffer.requestbuilder;

import com.paytm.pgplus.enums.PayMethod;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.paymentoffer.model.request.PromoPaymentOption;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class NetbankingPaymentOptionBuilderTest {

    private NetbankingPaymentOptionBuilder netbankingPaymentOptionBuilder = new NetbankingPaymentOptionBuilder();

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testValidatePromoPaymentOption() {
        PromoPaymentOption promoPaymentOption = new PromoPaymentOption();
        promoPaymentOption.setBankCode("AMEX");
        promoPaymentOption.setPayMethod(PayMethod.COD);
        promoPaymentOption.setTransactionAmount("1000");
        netbankingPaymentOptionBuilder.validatePromoPaymentOption(promoPaymentOption);

        PromoPaymentOption promoPaymentOption1 = new PromoPaymentOption();
        promoPaymentOption1.setPayMethod(PayMethod.COD);
        promoPaymentOption1.setTransactionAmount("1000");
        exceptionRule.expect(RequestValidationException.class);
        netbankingPaymentOptionBuilder.validatePromoPaymentOption(promoPaymentOption1);
    }
}