package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;

public class BrandEmiRequest implements Serializable {

    private static final long serialVersionUID = -1300357317809739359L;

    private BrandEmiRequestHead head;
    private BrandEmiRequestBody body;

    public BrandEmiRequestHead getHead() {
        return head;
    }

    public void setHead(BrandEmiRequestHead head) {
        this.head = head;
    }

    public BrandEmiRequestBody getBody() {
        return body;
    }

    public void setBody(BrandEmiRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "BrandEmiRequest{" + "head=" + head + ", body=" + body + '}';
    }
}
