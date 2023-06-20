package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.paytm.pgplus.facade.merchantlimit.enums.LimitType;

import java.io.Serializable;

public class MerchantRemainingLimit implements Serializable {

    private static final long serialVersionUID = 50064627835638569L;

    private LimitType limitType;

    private String amount;

    public LimitType getLimitType() {
        return limitType;
    }

    public void setLimitType(LimitType limitType) {
        this.limitType = limitType;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "MerchantRemainingLimit{" + "limitType=" + limitType + ", amount='" + amount + '\'' + '}';
    }
}
