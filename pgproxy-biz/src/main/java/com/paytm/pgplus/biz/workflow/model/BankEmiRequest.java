package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;

public class BankEmiRequest implements Serializable {

    private static final long serialVersionUID = 6896525285286849671L;

    private BankEmiRequestHead head;
    private BankEmiRequestBody body;

    public BankEmiRequestHead getHead() {
        return head;
    }

    public void setHead(BankEmiRequestHead head) {
        this.head = head;
    }

    public BankEmiRequestBody getBody() {
        return body;
    }

    public void setBody(BankEmiRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "BankEmiRequest{" + "head=" + head + ", body=" + body + '}';
    }

}
