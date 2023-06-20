package com.paytm.pgplus.theia.models;

import java.io.Serializable;

public class PRNValidationRequest implements Serializable {

    private static final long serialVersionUID = 5344563881677446706L;

    private String mid;

    private String orderId;

    private String prnCode;

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

    public String getPrnCode() {
        return prnCode;
    }

    public void setPrnCode(String prnCode) {
        this.prnCode = prnCode;
    }

    public PRNValidationRequest() {
    }

    public PRNValidationRequest(String mid, String orderId, String prnCode) {
        this.mid = mid;
        this.orderId = orderId;
        this.prnCode = prnCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PRNValidationRequest{");
        sb.append("mid='").append(mid).append('\'');
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", prnCode='").append(prnCode).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
