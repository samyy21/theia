package com.paytm.pgplus.theia.nativ.model.validateCard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.customaspects.annotations.Mask;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardNumberValidationRequestBody implements Serializable {

    private static final long serialVersionUID = -2010017404077453569L;

    @Mask(prefixNoMaskLen = 6, suffixNoMaskLen = 4)
    @JsonProperty("cardNumber")
    private String cardNumber;

    @Mask
    @JsonProperty("expireDate")
    private String expireDate;

    private String mid;

    private String requestId;

    public CardNumberValidationRequestBody() {
    }

    public CardNumberValidationRequestBody(String cardNumber, String expireDate) {
        this.cardNumber = cardNumber;
        this.expireDate = expireDate;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
