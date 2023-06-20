package com.paytm.pgplus.theia.promo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

public class BankInfo implements Serializable {

    @JsonProperty("bankCode")
    private String bankCode;

    @JsonProperty("bankName")
    private String bankName;

    @JsonProperty("logoUrl")
    private String logoUrl;

    @JsonProperty("displayName")
    private String displayName;

    public BankInfo() {
    }

    public BankInfo(String bankCode, String bankName, String logoUrl, String displayName) {
        this.bankCode = bankCode;
        this.bankName = bankName;
        this.logoUrl = logoUrl;
        this.displayName = displayName;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
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
