package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.ResponseHeader;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class EnhancedNativeErrorResponse implements Serializable {

    private static final long serialVersionUID = -2184096277945079749L;

    @NotNull
    @JsonProperty(value = "head")
    private ResponseHeader head;

    @NotNull
    @JsonProperty(value = "body")
    private EnhancedNativeErrorResponseBody body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public EnhancedNativeErrorResponseBody getBody() {
        return body;
    }

    public void setBody(EnhancedNativeErrorResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ErrorResponse{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
