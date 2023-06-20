package com.paytm.pgplus.theia.offline.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.models.TwoFAConfig;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FastForwardRequestBody extends RequestBody {

    private static final long serialVersionUID = -3271229420660696189L;

    private String reqType;
    private String txnAmount;
    private String customerId;
    private String industryType;
    private String currency;
    private String deviceId;
    private String paymentMode;
    private String appIP;
    private String authMode;
    private String channel;
    private String orderId;
    private String refId;
    private Money tipAmount;
    private TwoFAConfig twoFAConfig;
    private String simSubscriptionId;

    public FastForwardRequestBody() {

    }

    public String getReqType() {
        return reqType;
    }

    public void setReqType(String reqType) {
        this.reqType = reqType;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getIndustryType() {
        return industryType;
    }

    public void setIndustryType(String industryType) {
        this.industryType = industryType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getAppIP() {
        return appIP;
    }

    public void setAppIP(String appIP) {
        this.appIP = appIP;
    }

    public String getAuthMode() {
        return authMode;
    }

    public void setAuthMode(String authMode) {
        this.authMode = authMode;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getRefId() {
        return refId;
    }

    public Money getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(Money tipAmount) {
        this.tipAmount = tipAmount;
    }

    public TwoFAConfig getTwoFAConfig() {
        return twoFAConfig;
    }

    public void setTwoFAConfigObj(TwoFAConfig twoFAConfig) {
        this.twoFAConfig = twoFAConfig;
    }

    public String getSimSubscriptionId() {
        return simSubscriptionId;
    }

    public void setSimSubscriptionId(String simSubscriptionId) {
        this.simSubscriptionId = simSubscriptionId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FastForwardRequestBody{");
        sb.append("reqType='").append(reqType).append('\'');
        sb.append(", txnAmount='").append(txnAmount).append('\'');
        sb.append(", customerId='").append(customerId).append('\'');
        sb.append(", industryType='").append(industryType).append('\'');
        sb.append(", currency='").append(currency).append('\'');
        sb.append(", deviceId='").append(deviceId).append('\'');
        sb.append(", paymentMode='").append(paymentMode).append('\'');
        sb.append(", appIP='").append(appIP).append('\'');
        sb.append(", authMode='").append(authMode).append('\'');
        sb.append(", channel='").append(channel).append('\'');
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", refId=").append(refId).append('\'');
        sb.append(", twoFAConfig=").append(twoFAConfig).append('\'');
        sb.append(", simSubscriptionId=").append(simSubscriptionId).append('\'');
        sb.append(super.toString());
        sb.append('}');
        return sb.toString();
    }
}
