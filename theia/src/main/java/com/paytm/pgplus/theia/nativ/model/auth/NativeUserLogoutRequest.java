package com.paytm.pgplus.theia.nativ.model.auth;

import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class NativeUserLogoutRequest implements Serializable {

    private static final long serialVersionUID = 6425982522091786175L;

    @NotNull
    @Valid
    private TokenRequestHeader head;

    private NativeUserLogoutRequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public NativeUserLogoutRequestBody getBody() {
        return body;
    }

    public void setBody(NativeUserLogoutRequestBody body) {
        this.body = body;
    }
}
