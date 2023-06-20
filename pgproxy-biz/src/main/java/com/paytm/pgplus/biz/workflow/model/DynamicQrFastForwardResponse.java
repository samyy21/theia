package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DynamicQrFastForwardResponse implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 2296008725277354153L;

    @JsonProperty("TxnId")
    private String txnId;
    @JsonProperty("MerchantId")
    private String merchantId;
    @JsonProperty("OrderId")
    private String orderId;
    @JsonProperty("TxnAmount")
    private String txnAmount;
    @JsonProperty("BankTxnId")
    private String bankTxnId;
    @JsonProperty("ResponseCode")
    private String responseCode;
    @JsonProperty("ResponseMessage")
    private String responseMessage;
    @JsonProperty("Status")
    private String status;
    @JsonProperty("PaymentMode")
    private String paymentMode;
    @JsonProperty("BankName")
    private String bankName;
    @JsonProperty("CheckSum")
    private String checkSum;
    @JsonProperty("CustId")
    private String custId;
    @JsonProperty("MBID")
    private String mbid;

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getBankTxnId() {
        return bankTxnId;
    }

    public void setBankTxnId(String bankTxnId) {
        this.bankTxnId = bankTxnId;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getMbid() {
        return mbid;
    }

    public void setMbid(String mbid) {
        this.mbid = mbid;
    }

    @Override
    public String toString() {
        return "DynamicQrFastForwardResponse [txnId=" + txnId + ", merchantId=" + merchantId + ", orderId=" + orderId
                + ", txnAmount=" + txnAmount + ", bankTxnId=" + bankTxnId + ", responseCode=" + responseCode
                + ", responseMessage=" + responseMessage + ", status=" + status + ", paymentMode=" + paymentMode
                + ", bankName=" + bankName + ", checkSum=" + checkSum + ", custId=" + custId + ", mbid=" + mbid + "]";
    }

}
