package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FetchDeepLinkResponseBody implements Serializable {

    private static final long serialVersionUID = 217234260034291233L;

    String resultCode;
    String resultCodeId;
    String resultMsg;
    String deepLink;
    String orderId;
    String transId;
    String cashierRequestId;

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

    public String getDeepLink() {
        return deepLink;
    }

    public void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public FetchDeepLinkResponseBody() {
    }

    public FetchDeepLinkResponseBody(String resultCode, String resultCodeId, String resultMsg, String deepLink,
            String orderId) {
        this.resultCode = resultCode;
        this.resultCodeId = resultCodeId;
        this.resultMsg = resultMsg;
        this.deepLink = deepLink;
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FetchDeepLinkResponseBody{");
        sb.append("resultCode='").append(resultCode).append('\'');
        sb.append(", resultCodeId='").append(resultCodeId).append('\'');
        sb.append(", resultMsg='").append(resultMsg).append('\'');
        sb.append(", deepLink='").append(deepLink).append('\'');
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", transId='").append(transId).append('\'');
        sb.append(", cashierRequestId='").append(cashierRequestId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
