package com.paytm.pgplus.theia.nativ.model.auth;

import com.paytm.pgplus.response.ResponseHeader;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class NativeUserLogoutResponse implements Serializable {

    private static final long serialVersionUID = 2941375963235356035L;

    @NotNull
    @Valid
    private ResponseHeader head;

    @NotNull
    @Valid
    private NativeUserLogoutResponseBody body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public NativeUserLogoutResponseBody getBody() {
        return body;
    }

    public void setBody(NativeUserLogoutResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "NativeUserLogoutResponse{" + "head=" + head + ", body=" + body + '}';
    }
}
