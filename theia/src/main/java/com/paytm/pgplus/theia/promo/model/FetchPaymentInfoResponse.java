package com.paytm.pgplus.theia.promo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.ResponseHeader;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class FetchPaymentInfoResponse implements Serializable {

    @NotNull
    @JsonProperty("head")
    private ResponseHeader head;

    @NotNull
    @JsonProperty("body")
    private FetchPaymentInfoResponseBody body;

    public FetchPaymentInfoResponse() {
    }

    public FetchPaymentInfoResponse(@NotNull ResponseHeader head, @NotNull FetchPaymentInfoResponseBody body) {
        this.head = head;
        this.body = body;
    }

    @NotNull
    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(@NotNull ResponseHeader head) {
        this.head = head;
    }

    @NotNull
    public FetchPaymentInfoResponseBody getBody() {
        return body;
    }

    public void setBody(@NotNull FetchPaymentInfoResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
    }
}
