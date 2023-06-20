package com.paytm.pgplus.biz.workflow.model;

import com.paytm.pgplus.models.Money;

import java.io.Serializable;
import java.util.Map;

public class OfferDetails implements Serializable {

    private static final long serialVersionUID = -4127331938506396431L;

    private Money amount;
    private String offerId;
    private String type;

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "OfferDetails{" + "amount=" + amount + ", offerId='" + offerId + '\'' + ", type='" + type + '\'' + '}';
    }
}
