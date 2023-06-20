package com.paytm.pgplus.theia.nativ.model.payview.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchQRPaymentDetailsRequest implements Serializable {

    private final static long serialVersionUID = -592094969894062494L;

    @JsonProperty("head")
    private TokenRequestHeader head;

    @JsonProperty("body")
    private FetchQRPaymentDetailsRequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public FetchQRPaymentDetailsRequestBody getBody() {
        return body;
    }

    public void setBody(FetchQRPaymentDetailsRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("head", head).append("body", body).toString();
    }
}
