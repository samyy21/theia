package com.paytm.pgplus.theia.nativ.model.payview.emi;

import java.io.Serializable;

import com.paytm.pgplus.models.Money;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FetchEmiPayChannelRequestBody implements Serializable {

    private final static long serialVersionUID = 692838953026217847L;

    @JsonProperty("amount")
    private Money amount;

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("amount", amount).toString();
    }

}
