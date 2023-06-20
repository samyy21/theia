package com.paytm.pgplus.theia.nativ.model.vpa.details;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

public class FetchVpaDetailsRequest implements Serializable {

    @JsonProperty("head")
    private TokenRequestHeader head;

    @JsonProperty("body")
    private FetchVpaDetailsRequestBody body;

    private static final long serialVersionUID = 7984550283911348477L;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public FetchVpaDetailsRequestBody getBody() {
        return body;
    }

    public void setBody(FetchVpaDetailsRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("head", head).append("body", body).toString();
    }

}
