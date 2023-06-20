package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.ResponseHeader;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

public class FetchQRPaymentDetailsResponse implements Serializable {

    private final static long serialVersionUID = 2976528794579317770L;

    @JsonProperty("head")
    private ResponseHeader head;

    @JsonProperty("body")
    private FetchQRPaymentDetailsResponseBody body;

    public FetchQRPaymentDetailsResponse() {
        super();
    }

    public FetchQRPaymentDetailsResponse(ResponseHeader head, FetchQRPaymentDetailsResponseBody body) {
        super();
        this.head = head;
        this.body = body;
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public FetchQRPaymentDetailsResponseBody getBody() {
        return body;
    }

    public void setBody(FetchQRPaymentDetailsResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("head", head).append("body", body).toString();
    }

}
