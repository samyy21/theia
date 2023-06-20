package com.paytm.pgplus.theia.nativ.model.payview.emi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.ResponseHeader;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

public class FetchEmiPayChannelResponse implements Serializable {

    @JsonProperty("head")
    private ResponseHeader head;

    @JsonProperty("body")
    private FetchEmiPayChannelResponseBody body;

    private final static long serialVersionUID = 7984550283911348477L;

    public FetchEmiPayChannelResponse() {
        super();
    }

    public FetchEmiPayChannelResponse(ResponseHeader head, FetchEmiPayChannelResponseBody body) {
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

    public FetchEmiPayChannelResponseBody getBody() {
        return body;
    }

    public void setBody(FetchEmiPayChannelResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("head", head).append("body", body).toString();
    }

}
