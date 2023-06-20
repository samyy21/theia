package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DccPaymentDetailRequest implements Serializable {

    private static final long serialVersionUID = 6175763888655098142L;
    private String version;
    private String requestTimeStamp;
    private String client;
    private String bankCode;
    private String mid;
    private String amount;
    private String bin;
    private String orderId;
    private String extendedInfo;
    private String payMode;
    private String pcfAmount;

    public DccPaymentDetailRequest() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRequestTimeStamp() {
        return requestTimeStamp;
    }

    public void setRequestTimeStamp(String requestTimeStamp) {
        this.requestTimeStamp = requestTimeStamp;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getExtendedInfo() {
        return extendedInfo;
    }

    public void setExtendedInfo(String extendedInfo) {
        this.extendedInfo = extendedInfo;
    }

    public String getPayMode() {
        return payMode;
    }

    public void setPayMode(String payMode) {
        this.payMode = payMode;
    }

    public String getPcfAmount() {
        return pcfAmount;
    }

    public void setPcfAmount(String pcfAmount) {
        this.pcfAmount = pcfAmount;
    }

    @Override
    public String toString() {
        return "DccPaymentDetailRequest{" + "version='" + version + '\'' + ", requestTimeStamp='" + requestTimeStamp
                + '\'' + ", client='" + client + '\'' + ", bankCode='" + bankCode + '\'' + ", mid='" + mid + '\''
                + ", amount='" + amount + '\'' + ", bin='" + bin + '\'' + ", orderId='" + orderId + '\''
                + ", extendedInfo='" + extendedInfo + '\'' + ", payMode='" + payMode + '\'' + ", pcfAmount='"
                + pcfAmount + '\'' + '}';
    }
}
