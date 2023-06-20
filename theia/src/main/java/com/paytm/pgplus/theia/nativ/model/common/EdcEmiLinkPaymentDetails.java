package com.paytm.pgplus.theia.nativ.model.common;

import java.io.Serializable;

public class EdcEmiLinkPaymentDetails implements Serializable {
    private static final long serialVersionUID = -2603917284389149365L;
    private String bankCode;
    private String bankName;
    private String emiAmount;
    private String emiMonths;
    private String emiInterestRate;
    private String emiTotalAmount;
    private String emiCashback;
    private String additionalCashback;
    private String pgPlanId;
    private String cardType;

    public String getEmiAmount() {
        return emiAmount;
    }

    public void setEmiAmount(String emiAmount) {
        this.emiAmount = emiAmount;
    }

    public String getEmiMonths() {
        return emiMonths;
    }

    public void setEmiMonths(String emiMonths) {
        this.emiMonths = emiMonths;
    }

    public String getEmiInterestRate() {
        return emiInterestRate;
    }

    public void setEmiInterestRate(String emiInterestRate) {
        this.emiInterestRate = emiInterestRate;
    }

    public String getEmiTotalAmount() {
        return emiTotalAmount;
    }

    public void setEmiTotalAmount(String emiTotalAmount) {
        this.emiTotalAmount = emiTotalAmount;
    }

    public String getEmiCashback() {
        return emiCashback;
    }

    public void setEmiCashback(String emiCashback) {
        this.emiCashback = emiCashback;
    }

    public String getAdditionalCashback() {
        return additionalCashback;
    }

    public void setAdditionalCashback(String additionalCashback) {
        this.additionalCashback = additionalCashback;
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

    public String getPgPlanId() {
        return pgPlanId;
    }

    public void setPgPlanId(String pgPlanId) {
        this.pgPlanId = pgPlanId;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    @Override
    public String toString() {
        return "EdcEmiLinkPaymentDetails{" + "bankCode='" + bankCode + '\'' + ", bankName='" + bankName + '\''
                + ", emiAmount='" + emiAmount + '\'' + ", emiMonths='" + emiMonths + '\'' + ", emiInterestRate='"
                + emiInterestRate + '\'' + ", emiTotalAmount='" + emiTotalAmount + '\'' + ", emiCashback='"
                + emiCashback + '\'' + ", additionalCashback='" + additionalCashback + '\'' + ", pgPlanId='" + pgPlanId
                + '\'' + ", cardType='" + cardType + '\'' + '}';
    }
}
