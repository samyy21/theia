package com.paytm.pgplus.theia.nativ.model.payment.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NativePaymentRequest implements Serializable {

    private static final long serialVersionUID = 4462610455192203947L;

    private TokenRequestHeader head;

    private NativePaymentRequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public NativePaymentRequestBody getBody() {
        return body;
    }

    public void setBody(NativePaymentRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NativePaymentRequest [head=").append(head).append(", body=").append(body).append("]");
        return builder.toString();
    }
}
