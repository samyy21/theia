package com.paytm.pgplus.theia.paymentoffer.requestbuilder;

import com.paytm.pgplus.enums.PayMethod;
import com.paytm.pgplus.theia.offline.utils.ThieaApplicationContextProvider;

public class ApplyPromoPaymentOptionBuilderFactory {

    public static ApplyPromoPaymentOptionBuilder getApplyPromoPaymentOptionBuilder(PayMethod payMethod,
            boolean isSimplifiedPromoForPCF) {

        switch (payMethod) {
        case CREDIT_CARD:
        case DEBIT_CARD:
        case EMI:
        case EMI_DC:
            if (isSimplifiedPromoForPCF)
                return getBean("basePaymentOptionBuilder");
            return getBean("cardPaymentOptionBuilder");
        case NET_BANKING:
            return getBean("netbankingPaymentOptionBuilder");

        default:
            return getBean("basePaymentOptionBuilder");

        }
    }

    private static ApplyPromoPaymentOptionBuilder getBean(String beanName) {

        return (ApplyPromoPaymentOptionBuilder) ThieaApplicationContextProvider.getApplicationContext().getBean(
                beanName);
    }

    public static ApplyPromoPaymentOptionBuilder getApplyPromoPaymentOptionBuilder(PayMethod payMethod) {
        return getApplyPromoPaymentOptionBuilder(payMethod, false);
    }

}
