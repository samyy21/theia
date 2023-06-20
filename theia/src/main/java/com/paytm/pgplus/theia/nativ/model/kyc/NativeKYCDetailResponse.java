package com.paytm.pgplus.theia.nativ.model.kyc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.nativ.model.kyc.NativeKYCDetailResponseBody;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NativeKYCDetailResponse implements Serializable {

    private static final long serialVersionUID = -3090264475979849946L;

    @JsonProperty("head")
    private ResponseHeader head;

    @JsonProperty("body")
    private NativeKYCDetailResponseBody body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public NativeKYCDetailResponseBody getBody() {
        return body;
    }

    public void setBody(NativeKYCDetailResponseBody body) {
        this.body = body;
    }

    public NativeKYCDetailResponse(ResponseHeader head, NativeKYCDetailResponseBody body) {
        super();
        this.head = head;
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NativeKYCDetailResponse{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
