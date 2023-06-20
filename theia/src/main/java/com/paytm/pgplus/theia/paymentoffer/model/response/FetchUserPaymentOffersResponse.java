package com.paytm.pgplus.theia.paymentoffer.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.common.model.nativ.ResponseHeader;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchUserPaymentOffersResponse implements Serializable {

    private static final long serialVersionUID = 2418258384342078153L;
    private ResponseHeader head;
    private FetchUserPaymentOffersResponseBody body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public FetchUserPaymentOffersResponseBody getBody() {
        return body;
    }

    public void setBody(FetchUserPaymentOffersResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return MaskToStringBuilder.toString(this);
    }
}
