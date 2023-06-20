package com.paytm.pgplus.theia.nativ.model.directpage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

public class NativeDirectBankPageRequest implements Serializable {

    private static final long serialVersionUID = -4032567369981399682L;

    @NotNull
    @Valid
    private TokenRequestHeader head;

    @NotNull
    @Valid
    private NativeDirectBankPageRequestBody body;

    @JsonIgnore
    private transient HttpServletRequest httpServletRequest;

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    public NativeDirectBankPageRequest() {

    }

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public NativeDirectBankPageRequestBody getBody() {
        return body;
    }

    public void setBody(NativeDirectBankPageRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "NativeDirectBankPageRequest{" + "head=" + head + '}';
    }
}
