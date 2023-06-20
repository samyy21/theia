package com.paytm.pgplus.theia.nativ.model.cardindexnumber;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;

import java.io.Serializable;

public class NativeFetchCardIndexNumberAPIRequestBody implements Serializable {

    private final static long serialVersionUID = -8166474187301401727L;

    @JsonProperty(value = "cardNumber")
    @Mask(prefixNoMaskLen = 6, maskStr = "*", suffixNoMaskLen = 4)
    private String cardNumber;

    @JsonProperty(value = "cardExpiry")
    @Mask
    private String cardExpiry;

    @JsonIgnore
    @JsonProperty(value = "mid")
    private String mid;

    @JsonIgnore
    @JsonProperty(value = "referenceId")
    private String referenceId;

    @JsonIgnore
    @JsonProperty(value = "bankAccountNumber")
    @Mask(prefixNoMaskLen = 6, maskStr = "*", suffixNoMaskLen = 4)
    private String bankAccountNumber;

    private String bankIfsc;

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardExpiry() {
        return cardExpiry;
    }

    public void setCardExpiry(String cardExpiry) {
        this.cardExpiry = cardExpiry;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getBankIfsc() {
        return bankIfsc;
    }

    public void setBankIfsc(String bankIfsc) {
        this.bankIfsc = bankIfsc;
    }
}
