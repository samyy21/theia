package com.paytm.pgplus.theia.nativ.model.bin.cardhash;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

import java.io.Serializable;

public class NativeBinCardHashAPIRequest implements Serializable {

    private final static long serialVersionUID = 1199312927072103672L;

    @JsonProperty("head")
    private TokenRequestHeader head;

    @JsonProperty("body")
    private NativeBinCardHashAPIRequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public NativeBinCardHashAPIRequestBody getBody() {
        return body;
    }

    public void setBody(NativeBinCardHashAPIRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return MaskToStringBuilder.toString(this);
    }
}
