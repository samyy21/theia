package com.paytm.pgplus.theia.sessiondata;

import com.dyuproject.protostuff.Tag;

import java.io.Serializable;

/**
 * @author kartik
 * @date 19-04-2017
 */
public class DigitalCreditInfo implements Serializable {

    private static final long serialVersionUID = -4279948730708515109L;
    @Tag(value = 1)
    private boolean digitalCreditEnabled;
    @Tag(value = 2)
    private boolean digitalCreditInactive;
    @Tag(value = 3)
    private String accountBalance;
    @Tag(value = 4)
    private String externalAccountNo;
    @Tag(value = 5)
    private String lenderId;
    @Tag(value = 6)
    private int paymentRetryCount;
    @Tag(value = 7)
    private String invalidPassCodeMessage;
    @Tag(value = 8)
    private boolean passcodeRequired;

    public boolean isDigitalCreditEnabled() {
        return digitalCreditEnabled;
    }

    public void setDigitalCreditEnabled(boolean digitalCreditEnabled) {
        this.digitalCreditEnabled = digitalCreditEnabled;
    }

    public boolean isDigitalCreditInactive() {
        return digitalCreditInactive;
    }

    public void setDigitalCreditInactive(boolean digitalCreditInactive) {
        this.digitalCreditInactive = digitalCreditInactive;
    }

    public String getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(String accountBalance) {
        this.accountBalance = accountBalance;
    }

    public String getExternalAccountNo() {
        return externalAccountNo;
    }

    public void setExternalAccountNo(String externalAccountNo) {
        this.externalAccountNo = externalAccountNo;
    }

    public String getLenderId() {
        return lenderId;
    }

    public void setLenderId(String lenderId) {
        this.lenderId = lenderId;
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

    public boolean isPasscodeRequired() {
        return passcodeRequired;
    }

    public void setPasscodeRequired(boolean passcodeRequired) {
        this.passcodeRequired = passcodeRequired;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DigitalCreditInfo [digitalCreditEnabled=");
        builder.append(digitalCreditEnabled);
        builder.append(", digitalCreditInactive=");
        builder.append(digitalCreditInactive);
        builder.append(", accountBalance=");
        builder.append(accountBalance);
        builder.append(", externalAccountNo=");
        builder.append(externalAccountNo);
        builder.append(", lenderId=");
        builder.append(lenderId);
        builder.append(", paymentRetryCount=");
        builder.append(paymentRetryCount);
        builder.append(", invalidPassCodeMessage=");
        builder.append(invalidPassCodeMessage);
        builder.append(", passcodeRequired=");
        builder.append(passcodeRequired);
        builder.append("]");
        return builder.toString();
    }

}
