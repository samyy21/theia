package com.paytm.pgplus.theia.s2s.models.request;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentS2SRequest implements Serializable {

    private static final long serialVersionUID = -4353482323280113957L;

    @NotNull(message = "request header cannot be null")
    @Valid
    private PaymentS2SRequestHeader header;

    @NotNull(message = "request body cannot be null")
    @Valid
    private PaymentS2SRequestBody body;

    public PaymentS2SRequestHeader getHeader() {
        return header;
    }

    public void setHeader(PaymentS2SRequestHeader header) {
        this.header = header;
    }

    public PaymentS2SRequestBody getBody() {
        return body;
    }

    public void setBody(PaymentS2SRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PaymentS2SRequest [header=").append(header).append(", body=").append(body).append("]");
        return builder.toString();
    }

}
