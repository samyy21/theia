package com.paytm.pgplus.theia.paymentoffer.requestbuilder;

import com.paytm.pgplus.enums.PayMethod;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.paymentoffer.model.request.PromoPaymentOption;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class VPAPaymentOptionBuilderTest {

    private VPAPaymentOptionBuilder vpaPaymentOptionBuilder = new VPAPaymentOptionBuilder();

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testValidatePromoPaymentOption() {
        PromoPaymentOption promoPaymentOption = new PromoPaymentOption();
        promoPaymentOption.setVpa("vpa");
        promoPaymentOption.setPayMethod(PayMethod.COD);
        promoPaymentOption.setTransactionAmount("1000");
        vpaPaymentOptionBuilder.validatePromoPaymentOption(promoPaymentOption);

        PromoPaymentOption promoPaymentOption1 = new PromoPaymentOption();
        promoPaymentOption1.setPayMethod(PayMethod.COD);
        promoPaymentOption1.setTransactionAmount("1000");
        exceptionRule.expect(RequestValidationException.class);
        vpaPaymentOptionBuilder.validatePromoPaymentOption(promoPaymentOption1);
    }
}