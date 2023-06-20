package com.paytm.pgplus.cashier.models;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

/**
 * @author kartik
 * @date 21-Sep-2017
 */
public class PaymentsBankRequest implements Serializable {

    private static final long serialVersionUID = -6866141978166857570L;

    private String savingsAccountBalance;
    private String accountNumber;
    private String passCode;
    private String userMobile;
    private String clientId;
    private String clientSecret;
    private int paymentRetryCount;
    private String bankCode;
    private String paytmToken;
    private String accountRefId;

    public PaymentsBankRequest(String accountNumber, String savingsAccountBalance, int paymentRetryCount) {
        this.accountNumber = accountNumber;
        this.savingsAccountBalance = savingsAccountBalance;
        this.paymentRetryCount = paymentRetryCount;
    }

    public String getSavingsAccountBalance() {
        return savingsAccountBalance;
    }

    public void setSavingsAccountBalance(String savingsAccountBalance) {
        this.savingsAccountBalance = savingsAccountBalance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getPassCode() {
        return passCode;
    }

    public void setPassCode(String passCode) {
        this.passCode = passCode;
    }

    public String getUserMobile() {
        return userMobile;
    }

    public void setUserMobile(String userMobile) {
        this.userMobile = userMobile;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public int getPaymentRetryCount() {
        return paymentRetryCount;
    }

    public void setPaymentRetryCount(int paymentRetryCount) {
        this.paymentRetryCount = paymentRetryCount;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getPaytmToken() {
        return paytmToken;
    }

    public void setPaytmToken(String paytmToken) {
        this.paytmToken = paytmToken;
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
        builder.append("PaymentsBankRequest [savingsAccountBalance=");
        builder.append(savingsAccountBalance);
        builder.append(", accountNumber=");
        builder.append(accountNumber);
        builder.append(", passCode=");
        builder.append(StringUtils.isNotBlank(passCode));
        builder.append(", userMobile=");
        builder.append(StringUtils.isNotBlank(userMobile));
        builder.append(", clientId=");
        builder.append(clientId);
        builder.append(", paymentRetryCount=");
        builder.append(paymentRetryCount);
        builder.append(", bankCode=");
        builder.append(bankCode);
        builder.append(", paytmToken=");
        builder.append(paytmToken);
        builder.append(", accountRefId=");
        builder.append(accountRefId);
        builder.append("]");
        return builder.toString();
    }

}
