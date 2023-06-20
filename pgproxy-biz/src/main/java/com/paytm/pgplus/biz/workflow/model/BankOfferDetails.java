package com.paytm.pgplus.biz.workflow.model;

import com.paytm.pgplus.models.Money;

import java.io.Serializable;
import java.util.Map;

public class BankOfferDetails implements Serializable {

    private static final long serialVersionUID = -4697405261342232178L;

    private Money amount;
    private String percentage;
    private String offerid;
    private String type;

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }

    public String getOfferid() {
        return offerid;
    }

    public void setOfferid(String offerid) {
        this.offerid = offerid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "BankOfferDetails{" + "amount=" + amount + ", percentage='" + percentage + '\'' + ", offerid='"
                + offerid + '\'' + ", type='" + type + '\'' + '}';
    }
}
