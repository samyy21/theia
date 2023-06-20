package com.paytm.pgplus.biz.model.aoaorderlookup;

public enum OrderStatus {
    INIT("INIT"), CLOSED("CLOSED");

    String status;

    private OrderStatus(final String orderStatus) {
        this.status = orderStatus;
    }
}
