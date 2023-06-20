package com.paytm.pgplus.theia.aoa.model.orderpay;

import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.request.SubscriptionTransactionRequestBody;
import com.paytm.pgplus.theia.models.NativeJsonRequestBody;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

public class CreateOrderAndPaymentRequestBody implements Serializable {
    private final static long serialVersionUID = 4514771555532115284L;

    private String mid;
    private String orderId;
    private String requestType;
    private String sgwReferenceId;
    private InitiateTransactionRequestBody orderDetails;
    private SubscriptionTransactionRequestBody subscriptionDetails;
    private NativeJsonRequestBody paymentDetails;
    private transient HttpServletRequest request;

    public CreateOrderAndPaymentRequestBody() {
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

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public InitiateTransactionRequestBody getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(InitiateTransactionRequestBody orderDetails) {
        this.orderDetails = orderDetails;
    }

    public SubscriptionTransactionRequestBody getSubscriptionDetails() {
        return subscriptionDetails;
    }

    public void setSubscriptionDetails(SubscriptionTransactionRequestBody subscriptionDetails) {
        this.subscriptionDetails = subscriptionDetails;
    }

    public NativeJsonRequestBody getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(NativeJsonRequestBody paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public String getSgwReferenceId() {
        return sgwReferenceId;
    }

    public void setSgwReferenceId(String sgwReferenceId) {
        this.sgwReferenceId = sgwReferenceId;
    }

    @Override
    public String toString() {
        return "CreateOrderAndPaymentRequestBody{" + "mid='" + mid + '\'' + ", orderId='" + orderId + '\''
                + ", requestType='" + requestType + '\'' + ", orderDetails=" + orderDetails + ", subscriptionDetails="
                + subscriptionDetails + ", paymentDetails=" + paymentDetails + ",sgwReferenceId=" + sgwReferenceId
                + '}';
    }
}
