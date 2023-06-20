package com.paytm.pgplus.theia.promo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

public class PaymethodInfo implements Serializable {

    @JsonProperty("payMethod")
    private String payMethod;

    @JsonProperty("displayName")
    private String displayName;

    public PaymethodInfo() {
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
    }
}
