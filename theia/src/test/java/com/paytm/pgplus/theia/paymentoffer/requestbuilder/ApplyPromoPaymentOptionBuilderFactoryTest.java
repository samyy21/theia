package com.paytm.pgplus.theia.paymentoffer.requestbuilder;

import com.paytm.pgplus.enums.PayMethod;
import com.paytm.pgplus.theia.offline.utils.ThieaApplicationContextProvider;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

public class ApplyPromoPaymentOptionBuilderFactoryTest {

    @Test
    public void testGetApplyPromoPaymentOptionBuilder() {
        ThieaApplicationContextProvider thieaApplicationContextProvider = new ThieaApplicationContextProvider();
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        thieaApplicationContextProvider.setApplicationContext(applicationContext);
        ApplyPromoPaymentOptionBuilderFactory.getApplyPromoPaymentOptionBuilder(PayMethod.CREDIT_CARD, true);
        ApplyPromoPaymentOptionBuilderFactory.getApplyPromoPaymentOptionBuilder(PayMethod.DEBIT_CARD, true);
        ApplyPromoPaymentOptionBuilderFactory.getApplyPromoPaymentOptionBuilder(PayMethod.EMI, true);
        ApplyPromoPaymentOptionBuilderFactory.getApplyPromoPaymentOptionBuilder(PayMethod.EMI_DC, true);
        ApplyPromoPaymentOptionBuilderFactory.getApplyPromoPaymentOptionBuilder(PayMethod.EMI_DC, false);
        ApplyPromoPaymentOptionBuilderFactory.getApplyPromoPaymentOptionBuilder(PayMethod.NET_BANKING, true);
        ApplyPromoPaymentOptionBuilderFactory.getApplyPromoPaymentOptionBuilder(PayMethod.BALANCE, true);
        ApplyPromoPaymentOptionBuilderFactory.getApplyPromoPaymentOptionBuilder(PayMethod.COD);
    }
}