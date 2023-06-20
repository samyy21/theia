package com.paytm.pgplus.theia.nativ.model.user;

import java.io.Serializable;

public class UserPayModeStatus implements Serializable {

    private static final long serialVersionUID = 6146624738343979434L;

    private String paymentMode;
    private String status;

    public UserPayModeStatus() {
    }

    public UserPayModeStatus(String paymentMode, String status) {
        this.paymentMode = paymentMode;
        this.status = status;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserPayModeStatus{");
        sb.append("paymentMode='").append(paymentMode).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
