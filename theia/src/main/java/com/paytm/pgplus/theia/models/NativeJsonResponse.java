package com.paytm.pgplus.theia.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.paytm.pgplus.response.ResponseHeader;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

public class NativeJsonResponse implements Serializable {

    private final static long serialVersionUID = 8755772306676512293L;

    @JsonProperty(value = "head")
    @NotBlank
    private ResponseHeader head;

    @JsonProperty(value = "body")
    @NotBlank
    private NativeJsonResponseBody body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public NativeJsonResponseBody getBody() {
        return body;
    }

    public void setBody(NativeJsonResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "NativeJsonResponse{" + "head=" + head + ", body=" + body + '}';
    }
}
