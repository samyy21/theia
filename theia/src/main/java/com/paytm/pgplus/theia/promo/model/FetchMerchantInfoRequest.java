package com.paytm.pgplus.theia.promo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

public class FetchMerchantInfoRequest implements Serializable {

    @NotBlank
    @JsonProperty("head")
    private RequestHeader head;

    @NotBlank
    @JsonProperty("body")
    private FetchMerchantInfoRequestBody body;

    public RequestHeader getHead() {
        return head;
    }

    public void setHead(RequestHeader head) {
        this.head = head;
    }

    public FetchMerchantInfoRequestBody getBody() {
        return body;
    }

    public void setBody(FetchMerchantInfoRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
    }
}
