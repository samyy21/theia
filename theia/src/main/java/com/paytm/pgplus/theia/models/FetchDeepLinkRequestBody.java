package com.paytm.pgplus.theia.models;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;
import java.util.Map;

public class FetchDeepLinkRequestBody {

    private String requestType;

    @NotBlank(message = "txnAmount passed in the request is null")
    @Length(max = 50, message = "Invalid length for txnAmount")
    @Pattern(regexp = "^(\\d{1,9}|\\d{0,6}\\.\\d{1,2})$", message = "Validation regex not matching for txnAmount")
    private String txnAmount;

    @NotBlank(message = "mid passed in the request is null")
    private String mid;

    @Length(max = 50, message = "Invalid length for orderID")
    @Pattern(regexp = "^[a-zA-Z0-9-|_@.-]*$", message = "Validation regex not matching for orderID")
    @NotBlank(message = "orderId passed in the request is null")
    private String orderId;

    @NotBlank(message = "txnToken passed in the request is null")
    private String txnToken;

    @NotBlank(message = "channelId passed in the request is null")
    private String channelId;

    private String aggMid;

    private String accountNumber;

    private String refUrl;

    private String txnNote;

    private Map<String, String> additionalInfo;

    private Map<String, String> extendInfo;

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getAggMid() {
        return aggMid;
    }

    public void setAggMid(String aggMid) {
        this.aggMid = aggMid;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getRefUrl() {
        return refUrl;
    }

    public void setRefUrl(String refUrl) {
        this.refUrl = refUrl;
    }

    public String getTxnNote() {
        return txnNote;
    }

    public void setTxnNote(String txnNote) {
        this.txnNote = txnNote;
    }

    public Map<String, String> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Map<String, String> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getTxnToken() {
        return txnToken;
    }

    public void setTxnToken(String txnToken) {
        this.txnToken = txnToken;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FetchDeepLinkRequestBody{");
        sb.append("requestType='").append(requestType).append('\'');
        sb.append(", txnAmount='").append(txnAmount).append('\'');
        sb.append(", mid='").append(mid).append('\'');
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", txnToken='").append(txnToken).append('\'');
        sb.append(", channelId='").append(channelId).append('\'');
        sb.append(", aggMid='").append(aggMid).append('\'');
        sb.append(", accountNumber='").append(accountNumber).append('\'');
        sb.append(", refUrl='").append(refUrl).append('\'');
        sb.append(", txnNote='").append(txnNote).append('\'');
        sb.append(", additionalInfo=").append(additionalInfo);
        sb.append(", extendInfo=").append(extendInfo);
        sb.append('}');
        return sb.toString();
    }
}
