/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;

/**
 * @author namanjain
 *
 */
public class BizCreateOrderResponse implements Serializable {

    private static final long serialVersionUID = 10L;

    /**
     * The order ID. It is the same as was passed in the request.
     */
    private String orderId;

    /**
     * The transaction ID that was created for this request.
     */
    private String transId;

    /**
     * Whether the order was created successfully.
     */
    /*
     * private boolean isSuccessfullyProcessed;
     *//**
     * Will be populated only and only if
     */
    /*
     * private FailureDescription failureDescription;
     */

    /**
     * @return the orderId
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * @return the transId
     */
    public String getTransId() {
        return transId;
    }

    /**
     * @param orderId
     *            the orderId to set
     */
    public void setOrderId(final String orderId) {
        this.orderId = orderId;
    }

    /**
     * @param transId
     *            the transId to set
     */
    public void setTransId(final String transId) {
        this.transId = transId;
    }

    public BizCreateOrderResponse(String orderId, String transId) {
        this.orderId = orderId;
        this.transId = transId;
    }

}