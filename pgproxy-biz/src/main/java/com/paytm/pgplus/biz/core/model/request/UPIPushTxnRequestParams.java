package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;

/**
 * @author vivek kumar
 *
 */
public class UPIPushTxnRequestParams implements Serializable {

    private static final long serialVersionUID = -6079694431111515016L;

    private String mpin;
    private String bankName;
    private String accountNumber;
    private String credBlock;
    private String mobileNo;
    private String deviceId;
    private String payerVpa;
    private String ifsc;
    private String seqNo;
    private String externalSrNo;
    private String appId;

    public String getMpin() {
        return mpin;
    }

    public void setMpin(String mpin) {
        this.mpin = mpin;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCredBlock() {
        return credBlock;
    }

    public void setCredBlock(String credBlock) {
        this.credBlock = credBlock;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPayerVpa() {
        return payerVpa;
    }

    public void setPayerVpa(String payerVpa) {
        this.payerVpa = payerVpa;
    }

    public String getIfsc() {
        return ifsc;
    }

    public void setIfsc(String ifsc) {
        this.ifsc = ifsc;
    }

    public String getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
    }

    public String getExternalSrNo() {
        return externalSrNo;
    }

    public void setExternalSrNo(String externalSrNo) {
        this.externalSrNo = externalSrNo;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UPIPushTxnRequestParams [mpin=").append(mpin).append(", bankName=").append(bankName)
                .append(", accountNumber=").append(accountNumber).append(", credBlock=").append(credBlock)
                .append(", mobileNo=").append(mobileNo).append(", deviceId=").append(deviceId).append(", payerVpa=")
                .append(payerVpa).append(", ifsc=").append(ifsc).append(", seqNo=").append(seqNo)
                .append(", externalSrNo=").append(externalSrNo).append(", appId=").append(appId).append("]");
        return builder.toString();
    }

}
