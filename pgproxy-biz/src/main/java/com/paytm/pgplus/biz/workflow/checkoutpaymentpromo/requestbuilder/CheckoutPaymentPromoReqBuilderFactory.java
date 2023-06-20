package com.paytm.pgplus.biz.workflow.checkoutpaymentpromo.requestbuilder;

import com.paytm.pgplus.biz.utils.BizApplicationContextProvider;
import com.paytm.pgplus.facade.enums.PayMethod;

public class CheckoutPaymentPromoReqBuilderFactory {

    public static CheckoutPaymentPromoReqBuilder getApplyPromoPaymentOptionBuilder(PayMethod payMethod) {
        switch (payMethod) {
        case CREDIT_CARD:
        case DEBIT_CARD:
        case EMI:
        case EMI_DC:
            return getBean("cardCheckoutPaymentPromoReqBuilder");
        case NET_BANKING:
            return getBean("netbankingCheckoutPaymentPromoReqBuilder");

        default:
            return getBean("baseCheckoutPaymentPromoReqBuilder");

        }
    }

    private static CheckoutPaymentPromoReqBuilder getBean(String beanName) {
        return (CheckoutPaymentPromoReqBuilder) BizApplicationContextProvider.getApplicationContext().getBean(beanName);
    }

}
