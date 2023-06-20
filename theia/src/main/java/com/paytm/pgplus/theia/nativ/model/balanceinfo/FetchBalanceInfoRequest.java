package com.paytm.pgplus.theia.nativ.model.balanceinfo;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

public class FetchBalanceInfoRequest implements Serializable {

    @JsonProperty("head")
    private TokenRequestHeader head;

    @JsonProperty("body")
    private FetchBalanceInfoRequestBody body;

    private final static long serialVersionUID = 7984550283911348477L;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public FetchBalanceInfoRequestBody getBody() {
        return body;
    }

    public void setBody(FetchBalanceInfoRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("head", head).append("body", body).toString();
    }

}
