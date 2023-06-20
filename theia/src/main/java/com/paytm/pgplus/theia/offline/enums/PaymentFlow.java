package com.paytm.pgplus.theia.offline.enums;

import com.paytm.pgplus.biz.enums.EPayMode;

/**
 * Created by rahulverma on 4/9/17.
 */
public enum PaymentFlow {
    ADD_AND_PAY("ADD_AND_PAY"), HYBRID("HYBRID"), PPI("PPI"), PG_ONLY("PG_ONLY"), PG_ANNONYMOUS("PG_ANNONYMOUS"), NONE(
            "NONE");

    private String type;

    PaymentFlow(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static PaymentFlow paymentFlowByEPayMode(EPayMode ePayMode) {
        if (EPayMode.ADDANDPAY.equals(ePayMode))
            return ADD_AND_PAY;
        if (EPayMode.HYBRID.equals(ePayMode))
            return HYBRID;
        if (EPayMode.NONE.equals(ePayMode))
            return NONE;
        return null;
    }
}
