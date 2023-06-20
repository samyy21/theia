package com.paytm.pgplus.theia.oltpu.models;

import java.io.Serializable;

/**
 * Created by anamika on 10/9/18.
 */
public class MerchantRequest implements Serializable {

    private static final long serialVersionUID = -1951450756133191786L;
    private String mid;
    private String vpaAddress;
    private String orderId;

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getVpaAddress() {
        return vpaAddress;
    }

    public void setVpaAddress(String vpaAddress) {
        this.vpaAddress = vpaAddress;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MerchantRequest{");
        sb.append("mid='").append(mid).append('\'');
        sb.append(", vpaAddress='").append(vpaAddress).append('\'');
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
