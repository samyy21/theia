package com.paytm.pgplus.theia.nativ.model.payview.emi;

import java.io.Serializable;

import com.paytm.pgplus.response.ResponseHeader;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FetchEmiDetailResponse implements Serializable {

    @JsonProperty("head")
    private ResponseHeader head;

    @JsonProperty("body")
    private FetchEmiDetailResponseBody body;

    private final static long serialVersionUID = 7984550283911348477L;

    public FetchEmiDetailResponse() {
        super();
    }

    public FetchEmiDetailResponse(ResponseHeader head, FetchEmiDetailResponseBody body) {
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

    public FetchEmiDetailResponseBody getBody() {
        return body;
    }

    public void setBody(FetchEmiDetailResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("head", head).append("body", body).toString();
    }

}
