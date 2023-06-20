package com.paytm.pgplus.theia.nativ.model.bin.cardhash;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.ResponseHeader;

import java.io.Serializable;

public class NativeBinCardHashAPIResponse implements Serializable {

    private final static long serialVersionUID = -4820657922897633911L;

    @JsonProperty(value = "head")
    private ResponseHeader head;

    @JsonProperty(value = "body")
    private NativeBinCardHashAPIResponseBody body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public NativeBinCardHashAPIResponseBody getBody() {
        return body;
    }

    public void setBody(NativeBinCardHashAPIResponseBody body) {
        this.body = body;
    }
}
