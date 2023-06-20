/**
 * Alipay.com Inc. * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.paytm.pgplus.theia.nativ.model.user;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

public class SetUserPreferenceRequest implements Serializable {

    private static final long serialVersionUID = -858420693830255617L;

    @NotNull
    @Valid
    private TokenRequestHeader head;

    @NotNull
    @Valid
    private SetUserPreferenceRequestBody body;

    public SetUserPreferenceRequest(TokenRequestHeader head, SetUserPreferenceRequestBody body) {
        this.head = head;
        this.body = body;
    }

    public SetUserPreferenceRequest() {
    }

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public SetUserPreferenceRequestBody getBody() {
        return body;
    }

    public void setBody(SetUserPreferenceRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SetUserPreferenceRequest{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}