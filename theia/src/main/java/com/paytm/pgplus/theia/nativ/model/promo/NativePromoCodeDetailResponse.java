package com.paytm.pgplus.theia.nativ.model.promo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.ResponseHeader;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NativePromoCodeDetailResponse implements Serializable {

    private static final long serialVersionUID = 7980116847503251178L;

    @JsonProperty("head")
    private ResponseHeader head;

    @JsonProperty("body")
    private NativePromoCodeDetailResponseBody body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public NativePromoCodeDetailResponseBody getBody() {
        return body;
    }

    public void setBody(NativePromoCodeDetailResponseBody body) {
        this.body = body;
    }

    public NativePromoCodeDetailResponse(ResponseHeader head, NativePromoCodeDetailResponseBody body) {
        super();
        this.head = head;
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BinDetailResponse{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
