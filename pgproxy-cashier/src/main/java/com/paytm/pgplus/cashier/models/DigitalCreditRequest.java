package com.paytm.pgplus.cashier.models;

import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;

import java.io.Serializable;

/**
 * @author kartik
 * @date 11-05-2017
 */
public class DigitalCreditRequest implements Serializable {

    private static final long serialVersionUID = 5175512350530815481L;

    private String accountBalance;
    private String externalAccountNo;
    private String lenderId;
    private String passCode;
    private String userMobile;
    private String clientId;
    private String clientSecret;
    private int paymentRetryCount;
    private String paytmToken;
    private boolean passcodeRequired;
    @Mask(prefixNoMaskLen = 6, maskStr = "*", suffixNoMaskLen = 4)
    private String ssoToken;

    public DigitalCreditRequest(String accountBalance, String externalAccountNo, String lenderId, String passCode,
            int paymentRetryCount) {
        this.accountBalance = accountBalance;
        this.externalAccountNo = externalAccountNo;
        this.lenderId = lenderId;
        this.passCode = passCode;
        this.paymentRetryCount = paymentRetryCount;
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

    public int getPaymentRetryCount() {
        return paymentRetryCount;
    }

    public void setPaymentRetryCount(int paymentRetryCount) {
        this.paymentRetryCount = paymentRetryCount;
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

    public String getPaytmToken() {
        return paytmToken;
    }

    public void setPaytmToken(String paytmToken) {
        this.paytmToken = paytmToken;
    }

    public boolean isPasscodeRequired() {
        return passcodeRequired;
    }

    public void setPasscodeRequired(boolean passcodeRequired) {
        this.passcodeRequired = passcodeRequired;
    }

    public String getSsoToken() {
        return ssoToken;
    }

    public void setSsoToken(String ssoToken) {
        this.ssoToken = ssoToken;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }

}
