package com.paytm.pgplus.theia.nativ.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class UserPayModeStatusRequest implements Serializable {

    private static final long serialVersionUID = 7180261597525491896L;

    @JsonProperty("head")
    private TokenRequestHeader head;

    @JsonProperty("body")
    private UserPayModeStatusRequestBody body;

    public UserPayModeStatusRequest() {
    }

    public UserPayModeStatusRequest(TokenRequestHeader head, UserPayModeStatusRequestBody body) {
        this.head = head;
        this.body = body;
    }

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public UserPayModeStatusRequestBody getBody() {
        return body;
    }

    public void setBody(UserPayModeStatusRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserPayModeStatusRequest{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
