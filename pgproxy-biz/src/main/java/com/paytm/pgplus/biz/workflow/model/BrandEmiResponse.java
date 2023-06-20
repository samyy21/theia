package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;
import java.util.List;

public class BrandEmiResponse implements Serializable {

    private static final long serialVersionUID = -3096066221999334033L;

    private BrandEmiResponseBody body;
    private HeadResponse head;

    public BrandEmiResponseBody getBody() {
        return body;
    }

    public void setBody(BrandEmiResponseBody body) {
        this.body = body;
    }

    public HeadResponse getHead() {
        return head;
    }

    public void setHead(HeadResponse head) {
        this.head = head;
    }

    @Override
    public String toString() {
        return "BrandEmiResponse{" + "body=" + body + ", head=" + head + '}';
    }
}
