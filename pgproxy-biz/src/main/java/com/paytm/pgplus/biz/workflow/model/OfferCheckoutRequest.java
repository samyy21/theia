package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;

public class OfferCheckoutRequest implements Serializable {

    private static final long serialVersionUID = -8900324837079806579L;

    private OfferCheckoutRequestHead head;
    private OfferCheckoutRequestBody body;

    public OfferCheckoutRequestHead getHead() {
        return head;
    }

    public void setHead(OfferCheckoutRequestHead head) {
        this.head = head;
    }

    public OfferCheckoutRequestBody getBody() {
        return body;
    }

    public void setBody(OfferCheckoutRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "OfferCheckoutRequest{" + "head=" + head + ", body=" + body + '}';
    }
}
