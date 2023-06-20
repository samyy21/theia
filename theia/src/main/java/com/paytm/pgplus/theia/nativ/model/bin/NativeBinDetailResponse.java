package com.paytm.pgplus.theia.nativ.model.bin;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.ResponseHeader;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NativeBinDetailResponse implements Serializable {

    private static final long serialVersionUID = -83475041679782968L;

    @JsonProperty("head")
    private ResponseHeader head;

    @JsonProperty("body")
    private NativeBinDetailResponseBody body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public NativeBinDetailResponseBody getBody() {
        return body;
    }

    public void setBody(NativeBinDetailResponseBody body) {
        this.body = body;
    }

    public NativeBinDetailResponse(ResponseHeader head, NativeBinDetailResponseBody body) {
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
