package com.paytm.pgplus.theia.nativ.model.payview.nb;

import java.io.Serializable;

import com.paytm.pgplus.response.ResponseHeader;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NativeFetchNBPayChannelResponse implements Serializable {

    @JsonProperty("head")
    private ResponseHeader head;

    @JsonProperty("body")
    private NativeFetchNBPayChannelResponseBody body;

    private final static long serialVersionUID = 7984550283911348477L;

    public NativeFetchNBPayChannelResponse() {
        super();
    }

    public NativeFetchNBPayChannelResponse(ResponseHeader head, NativeFetchNBPayChannelResponseBody body) {
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

    public NativeFetchNBPayChannelResponseBody getBody() {
        return body;
    }

    public void setBody(NativeFetchNBPayChannelResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("head", head).append("body", body).toString();
    }

}
