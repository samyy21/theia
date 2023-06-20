package com.paytm.pgplus.theia.nativ.model.transactionStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionStatusRequestBody implements Serializable {

    private static final long serialVersionUID = 3234004890876829302L;

    @JsonProperty("mid")
    @NotNull
    private String merchantId;

    @JsonProperty("orderId")
    @NotNull
    private String orderId;

    @JsonIgnore
    private transient String paymentMode;

    @JsonIgnore
    private transient String transId;

    @JsonIgnore
    private transient String cashierRequestId;

    @JsonProperty("isCallbackUrlRequired")
    private boolean isCallbackUrlRequired;

    @JsonProperty("isFinalTxnStatusRequired")
    private boolean isFinalTxnStatusRequired;

    public boolean getFinalTxnStatusRequired() {
        return isFinalTxnStatusRequired;
    }

    public void setFinalTxnStatusRequired(boolean finalTxnStatusRequired) {
        isFinalTxnStatusRequired = finalTxnStatusRequired;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public String getCashierRequestId() {
        return cashierRequestId;
    }

    public void setCashierRequestId(String cashierRequestId) {
        this.cashierRequestId = cashierRequestId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public boolean isCallbackUrlRequired() {
        return isCallbackUrlRequired;
    }

    public void setCallbackUrlRequired(boolean callbackUrlRequired) {
        isCallbackUrlRequired = callbackUrlRequired;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransactionStatusRequestBody{");
        sb.append("merchantId='").append(merchantId).append('\'');
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", paymentMode='").append(paymentMode).append('\'');
        sb.append(", transId='").append(transId).append('\'');
        sb.append(", cashierRequestId='").append(cashierRequestId).append('\'');
        sb.append(", isCallbackUrlRequired=").append(isCallbackUrlRequired);
        sb.append('}');
        return sb.toString();
    }
}
