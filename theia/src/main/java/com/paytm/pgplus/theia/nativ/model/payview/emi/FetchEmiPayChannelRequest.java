package com.paytm.pgplus.theia.nativ.model.payview.emi;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

public class FetchEmiPayChannelRequest implements Serializable {

    @JsonProperty("head")
    private TokenRequestHeader head;

    @JsonProperty("body")
    private FetchEmiPayChannelRequestBody body;

    private final static long serialVersionUID = 984550283911348477L;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public FetchEmiPayChannelRequestBody getBody() {
        return body;
    }

    public void setBody(FetchEmiPayChannelRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("head", head).append("body", body).toString();
    }

}
