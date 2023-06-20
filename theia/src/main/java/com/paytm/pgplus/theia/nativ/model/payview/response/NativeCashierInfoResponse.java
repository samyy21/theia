package com.paytm.pgplus.theia.nativ.model.payview.response;

import java.io.Serializable;

import com.paytm.pgplus.response.ResponseHeader;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NativeCashierInfoResponse implements Serializable {

    @JsonProperty("head")
    private ResponseHeader head;

    @JsonProperty("body")
    private NativeCashierInfoResponseBody body;

    private final static long serialVersionUID = 7984550283911348477L;

    public NativeCashierInfoResponse() {
        super();
    }

    public NativeCashierInfoResponse(ResponseHeader head, NativeCashierInfoResponseBody body) {
        super();
        this.head = head;
        this.body = body;
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public NativeCashierInfoResponseBody getBody() {
        return body;
    }

    public void setBody(NativeCashierInfoResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("head", head).append("body", body).toString();
    }

}
