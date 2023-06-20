package com.paytm.pgplus.cashier.refund.model;

import java.io.Serializable;

/**
 * @author amit.dubey
 */
public class BaseRequest implements Serializable {

    /**
     * serila cerison id
     */
    private static final long serialVersionUID = 4137207547999069707L;

    private final String mid;
    private final String orderId;
    private final String refId;

    public BaseRequest(String mid, String orderId, String refId) {
        this.mid = mid;
        this.orderId = orderId;
        this.refId = refId;
    }

    public String getMid() {
        return mid;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getRefId() {
        return refId;
    }

    @Override
    public String toString() {
        return "BaseRequest{" + "mid='" + mid + '\'' + ", orderId='" + orderId + '\'' + ", refId='" + refId + '\''
                + '}';
    }
}
