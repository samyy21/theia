package com.paytm.pgplus.theia.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.customaspects.annotations.Mask;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;
import java.util.Map;

/**
 * @author Santosh chourasia
 *
 */
@JsonIgnoreProperties
public class UPIPSPBody {

    @NotBlank(message = "RequestType passed in the request is null")
    private String requestType;
    @NotBlank(message = "txnAmount passed in the request is null")
    @Length(max = 50, message = "Invalid length for txnAmount")
    @Pattern(regexp = "^(\\d{1,9}|\\d{0,6}\\.\\d{1,2})$", message = "Validation regex not matching for txnAmount")
    private String txnAmount;
    @NotBlank(message = "payerVpa passed in the request is null")
    private String payerVpa;
    @NotBlank(message = "mid passed in the request is null")
    private String mid;
    @Length(max = 50, message = "Invalid length for orderID")
    @Pattern(regexp = "^[a-zA-Z0-9-|_@.-]*$", message = "Validation regex not matching for orderID")
    private String orderId;
    @Mask(prefixNoMaskLen = 3, suffixNoMaskLen = 3)
    @Length(max = 15, message = "Invalid length for mobileNo")
    private String mobileNo;
    private Map<String, String> extendInfo;
    private String custID;
    private String payeeVpa;

    private String payerName;
    private String payerPSP;
    private String upiOrderTimeOutInSeconds;
    private String settlementType;
    private String type;
    private String subscriptionId;
    private String orderAmount;
    private String payerPaymentInstrument;

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getPayerVpa() {
        return payerVpa;
    }

    public void setPayerVpa(String payerVpa) {
        this.payerVpa = payerVpa;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
    }

    public String getCustID() {
        return custID;
    }

    public void setCustID(String custID) {
        this.custID = custID;
    }

    public String getPayeeVpa() {
        return payeeVpa;
    }

    public void setPayeeVpa(String payeeVpa) {
        this.payeeVpa = payeeVpa;
    }

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public String getPayerPSP() {
        return payerPSP;
    }

    public void setPayerPSP(String payerPSP) {
        this.payerPSP = payerPSP;
    }

    public String getUpiOrderTimeOutInSeconds() {
        return upiOrderTimeOutInSeconds;
    }

    public void setUpiOrderTimeOutInSeconds(String upiOrderTimeOutInSeconds) {
        this.upiOrderTimeOutInSeconds = upiOrderTimeOutInSeconds;
    }

    public String getSettlementType() {
        return settlementType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(String orderAmount) {
        this.orderAmount = orderAmount;
    }

    public String getPayerPaymentInstrument() {
        return payerPaymentInstrument;
    }

    public void setPayerPaymentInstrument(String payerPaymentInstrument) {
        this.payerPaymentInstrument = payerPaymentInstrument;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }

}
