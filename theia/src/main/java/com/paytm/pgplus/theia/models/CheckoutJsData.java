package com.paytm.pgplus.theia.models;

import com.paytm.pgplus.theia.nativ.model.payment.request.NativePaymentRequestBody;

public class CheckoutJsData {

    String orderId;

    String mid;

    String txnToken;

    NativePaymentRequestBody retryData;

    public CheckoutJsData() {
    }

    public CheckoutJsData(String orderId, String mid, String txnToken, NativePaymentRequestBody nativePaymentRequestBody) {
        this.orderId = orderId;
        this.mid = mid;
        this.txnToken = txnToken;
        this.retryData = nativePaymentRequestBody;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getTxnToken() {
        return txnToken;
    }

    public void setTxnToken(String txnToken) {
        this.txnToken = txnToken;
    }

    public NativePaymentRequestBody getRetryData() {
        return retryData;
    }

    public void setRetryData(NativePaymentRequestBody retryData) {
        this.retryData = retryData;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CheckoutJsData [orderId=").append(orderId).append(", mid=").append(mid).append(", txnToken=")
                .append(txnToken).append(", retryData=").append(retryData).append("]");
        return builder.toString();
    }
}
