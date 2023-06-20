package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class PaymentS2SResponse implements Serializable {

    private static final long serialVersionUID = 3242825354531643693L;

    @NotNull
    @Valid
    private PaymentS2SResponseHeader header;

    @NotNull
    @Valid
    private PaymentS2SResponseBody body;

    public PaymentS2SResponse() {
    }

    public PaymentS2SResponse(PaymentS2SResponseHeader header, PaymentS2SResponseBody body) {
        this.header = header;
        this.body = body;
    }

    public PaymentS2SResponseHeader getHeader() {
        return header;
    }

    public void setHeader(PaymentS2SResponseHeader header) {
        this.header = header;
    }

    public PaymentS2SResponseBody getBody() {
        return body;
    }

    public void setBody(PaymentS2SResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PaymentS2SResponse [header=").append(header).append(", body=").append(body).append("]");
        return builder.toString();
    }

}
