package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * @author Santosh chourasia
 *
 */

public class UPIPSPResponseBody implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 255904260034291233L;

    String resultCode;
    String resultCodeId;
    String resultMsg;
    String externalSerialNo;
    String orderId;
    String requestMsgId;
    String txnAmount;
    String mid;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String subscriptionId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String debitAmount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String payerAccountDetails;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String callbackUrl;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Boolean txnAllowed;

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultCodeId() {
        return resultCodeId;
    }

    public void setResultCodeId(String resultCodeId) {
        this.resultCodeId = resultCodeId;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public String getExternalSerialNo() {
        return externalSerialNo;
    }

    public void setExternalSerialNo(String externalSerialNo) {
        this.externalSerialNo = externalSerialNo;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getRequestMsgId() {
        return requestMsgId;
    }

    public void setRequestMsgId(String requestMsgId) {
        this.requestMsgId = requestMsgId;
    }

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

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getDebitAmount() {
        return debitAmount;
    }

    public void setDebitAmount(String debitAmount) {
        this.debitAmount = debitAmount;
    }

    public String getPayerAccountDetails() {
        return payerAccountDetails;
    }

    public void setPayerAccountDetails(String payerAccountDetails) {
        this.payerAccountDetails = payerAccountDetails;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public Boolean getTxnAllowed() {
        return txnAllowed;
    }

    public void setTxnAllowed(Boolean txnAllowed) {
        this.txnAllowed = txnAllowed;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UPIPSPResponse [resultCode=").append(resultCode).append(", resultCodeId=").append(resultCodeId)
                .append(", resultMsg=").append(resultMsg).append(", externalSerialNo=").append(externalSerialNo)
                .append(", orderId=").append(orderId).append(", requestMsgId=").append(requestMsgId)
                .append(", txnAmount=").append(txnAmount).append(", mid=").append(mid).append(", subscriptionId=")
                .append(subscriptionId).append(", debitAmount=").append(debitAmount).append(", payerAccountDetails=")
                .append(payerAccountDetails).append(", callbackUrl=").append(callbackUrl).append(", txnAllowed=")
                .append(txnAllowed).append("]");
        return builder.toString();
    }

}
