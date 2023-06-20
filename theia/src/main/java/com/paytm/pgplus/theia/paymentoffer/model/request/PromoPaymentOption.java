package com.paytm.pgplus.theia.paymentoffer.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.common.model.CardTokenInfo;
import com.paytm.pgplus.enums.PayMethod;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PromoPaymentOption implements Serializable {

    private static final long serialVersionUID = 8502977008143550122L;

    @NotBlank(message = "amount cannot be blank")
    @Length(max = 16, message = "Amount cannot be longer thann 16 characters")
    @Pattern(regexp = "[0-9]+([.][0-9]{1,2})?")
    private String transactionAmount;
    @NotNull
    private PayMethod payMethod;
    private String bankCode;
    @Mask
    private String cardNo;
    private String vpa;
    private String savedCardId;
    private CardTokenInfo cardTokenInfo;

    @Transient
    private String uniquePromoIdentifier;

    @Transient
    private Map<String, String> promoContext;

    /**
     * Added field to send bin8Alias to support Promo APi
     */

    private String eightDigitBinHash;
    private String tenure;

    public Map<String, String> getPromoContext() {
        return promoContext;
    }

    public void setPromoContext(Map<String, String> promoContext) {
        this.promoContext = promoContext;
    }

    public String getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(String transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    @NotNull
    public PayMethod getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(@NotNull PayMethod payMethod) {
        this.payMethod = payMethod;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getVpa() {
        return vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public String getSavedCardId() {
        return savedCardId;
    }

    public void setSavedCardId(String savedCardId) {
        this.savedCardId = savedCardId;
    }

    public String getEightDigitBinHash() {
        return eightDigitBinHash;
    }

    public void setEightDigitBinHash(String eightDigitBinHash) {
        this.eightDigitBinHash = eightDigitBinHash;
    }

    public CardTokenInfo getCardTokenInfo() {
        return cardTokenInfo;
    }

    public void setCardTokenInfo(CardTokenInfo cardTokenInfo) {
        this.cardTokenInfo = cardTokenInfo;
    }

    public String getUniquePromoIdentifier() {
        return uniquePromoIdentifier;
    }

    public void setUniquePromoIdentifier(String uniquePromoIdentifier) {
        this.uniquePromoIdentifier = uniquePromoIdentifier;
    }

    public String getTenure() {
        return tenure;
    }

    public void setTenure(String tenure) {
        this.tenure = tenure;
    }

    @Override
    public String toString() {
        return (new MaskToStringBuilder(this)).toString();
    }
}
