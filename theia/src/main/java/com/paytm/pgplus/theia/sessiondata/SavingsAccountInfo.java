package com.paytm.pgplus.theia.sessiondata;

import com.dyuproject.protostuff.Tag;

import java.io.Serializable;

/**
 * @author kartik
 * @date 20-Sep-2017
 */
public class SavingsAccountInfo implements Serializable {

    private static final long serialVersionUID = -5212536028997572806L;

    @Tag(value = 1)
    private boolean savingsAccountEnabled;
    @Tag(value = 2)
    private boolean savingsAccountInactive;
    @Tag(value = 3)
    private String paymentsBankCode;
    @Tag(value = 4)
    private String accountNumber;
    @Tag(value = 5)
    private String effectiveBalance;
    @Tag(value = 6)
    private String slfdBalance;
    @Tag(value = 7)
    private int paymentRetryCount;
    @Tag(value = 8)
    private String invalidPassCodeMessage;
    @Tag(value = 9)
    private String accountRefId;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getEffectiveBalance() {
        return effectiveBalance;
    }

    public void setEffectiveBalance(String effectiveBalance) {
        this.effectiveBalance = effectiveBalance;
    }

    public String getSlfdBalance() {
        return slfdBalance;
    }

    public void setSlfdBalance(String slfdBalance) {
        this.slfdBalance = slfdBalance;
    }

    public boolean isSavingsAccountEnabled() {
        return savingsAccountEnabled;
    }

    public void setSavingsAccountEnabled(boolean savingsAccountEnabled) {
        this.savingsAccountEnabled = savingsAccountEnabled;
    }

    public boolean isSavingsAccountInactive() {
        return savingsAccountInactive;
    }

    public void setSavingsAccountInactive(boolean savingsAccountInactive) {
        this.savingsAccountInactive = savingsAccountInactive;
    }

    public int getPaymentRetryCount() {
        return paymentRetryCount;
    }

    public void setPaymentRetryCount(int paymentRetryCount) {
        this.paymentRetryCount = paymentRetryCount;
    }

    public String getInvalidPassCodeMessage() {
        return invalidPassCodeMessage;
    }

    public void setInvalidPassCodeMessage(String invalidPassCodeMessage) {
        this.invalidPassCodeMessage = invalidPassCodeMessage;
    }

    public String getPaymentsBankCode() {
        return paymentsBankCode;
    }

    public void setPaymentsBankCode(String paymentsBankCode) {
        this.paymentsBankCode = paymentsBankCode;
    }

    public String getAccountRefId() {
        return accountRefId;
    }

    public void setAccountRefId(String accountRefId) {
        this.accountRefId = accountRefId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SavingsAccountInfo [savingsAccountEnabled=");
        builder.append(savingsAccountEnabled);
        builder.append(", savingsAccountInactive=");
        builder.append(savingsAccountInactive);
        builder.append(", paymentsBankCode=");
        builder.append(paymentsBankCode);
        builder.append(", accountNumber=");
        builder.append(accountNumber);
        builder.append(", effectiveBalance=");
        builder.append(effectiveBalance);
        builder.append(", slfdBalance=");
        builder.append(slfdBalance);
        builder.append(", paymentRetryCount=");
        builder.append(paymentRetryCount);
        builder.append(", invalidPassCodeMessage=");
        builder.append(invalidPassCodeMessage);
        builder.append(", accountRefId=");
        builder.append(accountRefId);
        builder.append("]");
        return builder.toString();
    }

}
