package com.paytm.pgplus.theia.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

public class NativeJsonRequest implements Serializable {

    private final static long serialVersionUID = -7690186460349884989L;

    @JsonProperty(value = "head")
    @NotBlank
    private TokenRequestHeader head;

    @JsonProperty(value = "body")
    @NotBlank
    private NativeJsonRequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public NativeJsonRequestBody getBody() {
        return body;
    }

    public void setBody(NativeJsonRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "NativeJsonRequest{" + "head=" + head + ", body=" + body + '}';
    }
}
