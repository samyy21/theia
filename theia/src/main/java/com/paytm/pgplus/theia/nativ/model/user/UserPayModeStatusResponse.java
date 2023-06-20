package com.paytm.pgplus.theia.nativ.model.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.ResponseHeader;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPayModeStatusResponse implements Serializable {

    private static final long serialVersionUID = 3147475625174200074L;

    @JsonProperty("head")
    private ResponseHeader head;

    @JsonProperty("body")
    private UserPayModeStatusResponseBody body;

    public UserPayModeStatusResponse() {
    }

    public UserPayModeStatusResponse(ResponseHeader head, UserPayModeStatusResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public UserPayModeStatusResponse(UserPayModeStatusResponseBody body) {
        this.body = body;
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public UserPayModeStatusResponseBody getBody() {
        return body;
    }

    public void setBody(UserPayModeStatusResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserPayModeStatusResponse{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
