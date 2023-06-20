package com.paytm.pgplus.theia.paymentoffer.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.common.model.nativ.ResponseHeader;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchAllPaymentOffersResponse implements Serializable {

    private static final long serialVersionUID = -4169624318799184703L;
    private ResponseHeader head;
    private FetchPaymentOffersResponseBody body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public FetchPaymentOffersResponseBody getBody() {
        return body;
    }

    public void setBody(FetchPaymentOffersResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return MaskToStringBuilder.toString(this);
    }
}
