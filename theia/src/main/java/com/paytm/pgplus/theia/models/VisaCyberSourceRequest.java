package com.paytm.pgplus.theia.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.customaspects.annotations.Mask;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VisaCyberSourceRequest implements Serializable {

    private static final long serialVersionUID = -4518681020982636163L;

    @Mask(prefixNoMaskLen = 6, suffixNoMaskLen = 4)
    private String cardNumber;

    private String uniqueId;

    private String amount;

    private String currency;

    @Mask
    private String expiryMonth;

    @Mask
    private String expiryYear;

    public VisaCyberSourceRequest() {
    }

    public String getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public String getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }

}
