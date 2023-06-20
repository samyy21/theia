/**
 * Alipay.com Inc. * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.paytm.pgplus.theia.nativ.model.user;

import com.paytm.pgplus.response.ResponseHeader;

import java.io.Serializable;

public class SetUserPreferenceResponse implements Serializable {

    private static final long serialVersionUID = 2958399740621400833L;

    private ResponseHeader head;

    private SetUserPreferenceResponseBody body;

    public SetUserPreferenceResponse() {
    }

    public SetUserPreferenceResponse(ResponseHeader head, SetUserPreferenceResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public SetUserPreferenceResponseBody getBody() {
        return body;
    }

    public void setBody(SetUserPreferenceResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SetUserPreferenceResponse{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}