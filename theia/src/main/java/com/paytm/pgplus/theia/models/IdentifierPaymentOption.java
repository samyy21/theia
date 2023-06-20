package com.paytm.pgplus.theia.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.common.model.CardTokenInfo;
import com.paytm.pgplus.enums.PayMethod;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IdentifierPaymentOption implements Serializable {

    private static final long serialVersionUID = 8502977008143550122L;

    @NotNull
    private PayMethod payMethod;
    @Mask
    private String cardNo;
    private String savedCardId;
    private CardTokenInfo cardTokenInfo;

    @Transient
    private String uniqueIdentifier;

    @NotNull
    public PayMethod getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(@NotNull PayMethod payMethod) {
        this.payMethod = payMethod;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getSavedCardId() {
        return savedCardId;
    }

    public void setSavedCardId(String savedCardId) {
        this.savedCardId = savedCardId;
    }

    public CardTokenInfo getCardTokenInfo() {
        return cardTokenInfo;
    }

    public void setCardTokenInfo(CardTokenInfo cardTokenInfo) {
        this.cardTokenInfo = cardTokenInfo;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    @Override
    public String toString() {
        return (new MaskToStringBuilder(this)).toString();
    }
}
