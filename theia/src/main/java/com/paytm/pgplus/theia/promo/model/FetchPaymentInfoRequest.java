package com.paytm.pgplus.theia.promo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class FetchPaymentInfoRequest implements Serializable {

    @NotNull
    @JsonProperty("head")
    private RequestHeader head;

    @JsonProperty("body")
    private FetchPaymentInfoRequestBody body;

    public FetchPaymentInfoRequest() {
    }

    public FetchPaymentInfoRequest(RequestHeader head, FetchPaymentInfoRequestBody body) {
        this.head = head;
        this.body = body;
    }

    public RequestHeader getHead() {
        return head;
    }

    public void setHead(RequestHeader head) {
        this.head = head;
    }

    public FetchPaymentInfoRequestBody getBody() {
        return body;
    }

    public void setBody(FetchPaymentInfoRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
    }
}
