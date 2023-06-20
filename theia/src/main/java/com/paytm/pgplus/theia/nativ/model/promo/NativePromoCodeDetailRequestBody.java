package com.paytm.pgplus.theia.nativ.model.promo;

import java.io.Serializable;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NativePromoCodeDetailRequestBody implements Serializable {

    private static final long serialVersionUID = 3142604241200884769L;

    @JsonProperty("cardNumber")
    private String cardNumber;

    @JsonProperty("txnType")
    private String txnType;

    @JsonProperty("bankCode")
    private String bankCode;

    private String promoCode;

    private String mid;

    private String isEnhancedFlow;

    private UserDetailsBiz userDetailsBiz;

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getIsEnhancedFlow() {
        return isEnhancedFlow;
    }

    public void setIsEnhancedFlow(String isEnhancedFlow) {
        this.isEnhancedFlow = isEnhancedFlow;
    }

    public UserDetailsBiz getUserDetailsBiz() {
        return userDetailsBiz;
    }

    public void setUserDetailsBiz(UserDetailsBiz userDetailsBiz) {
        this.userDetailsBiz = userDetailsBiz;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NativePromoCodeDetailRequestBody{");
        sb.append(", txnType='").append(txnType).append('\'');
        sb.append(", bankCode='").append(bankCode).append('\'');
        sb.append(", promoCode='").append(promoCode).append('\'');
        sb.append(", mid='").append(mid).append('\'');
        sb.append(", isEnhancedFlow='").append(isEnhancedFlow).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
