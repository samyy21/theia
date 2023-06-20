package com.paytm.pgplus.theia.promo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.ResponseHeader;
import io.swagger.annotations.ApiModel;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel(value = "MerchantInfoResponse", description = "MerchantInfoResponse")
@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchMerchantInfoResponse implements Serializable {

    @NotNull
    @JsonProperty("head")
    private ResponseHeader head;

    @NotNull
    @JsonProperty("body")
    private MerchantInfoResponseBody body;

    public FetchMerchantInfoResponse() {
    }

    public FetchMerchantInfoResponse(ResponseHeader head, MerchantInfoResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public MerchantInfoResponseBody getBody() {
        return body;
    }

    public void setBody(MerchantInfoResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
    }
}
