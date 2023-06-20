package com.paytm.pgplus.theia.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.request.SecureRequestHeader;

import java.io.Serializable;

public class ImeiValidateRequest implements Serializable {
    @JsonProperty("head")
    private SecureRequestHeader head;
    @JsonProperty("body")
    private ImeiValidateRequestBody body;

    public SecureRequestHeader getHead() {
        return head;
    }

    public void setHead(SecureRequestHeader head) {
        this.head = head;
    }

    public ImeiValidateRequestBody getBody() {
        return body;
    }

    public void setBody(ImeiValidateRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ImeiValidationRequest [head=");
        builder.append(head);
        builder.append(", body=");
        builder.append(body);
        builder.append("]");
        return builder.toString();
    }
}