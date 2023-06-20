package com.paytm.pgplus.biz.core.model;

import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import java.io.Serializable;

public class ActiveSubscriptionBeanBiz implements Serializable {

    private static final long serialVersionUID = -3021466932819706818L;

    private String accountNumber;
    private String accountHolderName;
    private String bankIFSC;
    private String bankName;
    private Long savedCardId;
    private String subscriptionId;
    private String paymentMode;

    public ActiveSubscriptionBeanBiz() {
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public Long getSavedCardId() {
        return savedCardId;
    }

    public void setSavedCardId(Long savedCardId) {
        this.savedCardId = savedCardId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getBankIFSC() {
        return bankIFSC;
    }

    public void setBankIFSC(String bankIFSC) {
        this.bankIFSC = bankIFSC;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }

}
