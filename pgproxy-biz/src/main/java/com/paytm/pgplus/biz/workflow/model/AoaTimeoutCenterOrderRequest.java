package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AoaTimeoutCenterOrderRequest {
    private static final long serialVersionUID = 4462610455192203947L;

    private List<OrderInfo> orderInfo;

    public List<OrderInfo> getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(List<OrderInfo> orderInfo) {
        this.orderInfo = orderInfo;
    }

    @Override
    public String toString() {
        return "AoaTimeoutCenterOrderRequestBody{" + "orderInfo=" + orderInfo + '}';
    }
}
