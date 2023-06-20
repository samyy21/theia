package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;

public class BankEmiResponse implements Serializable {
    private static final long serialVersionUID = 7879315696221512425L;

    private BankEmiResponseHead head;
    private BankEmiResponseBody body;

    public BankEmiResponseHead getHead() {
        return head;
    }

    public void setHead(BankEmiResponseHead head) {
        this.head = head;
    }

    public BankEmiResponseBody getBody() {
        return body;
    }

    public void setBody(BankEmiResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "BankEmiResponse{" + "head=" + head + ", body=" + body + '}';
    }

}
