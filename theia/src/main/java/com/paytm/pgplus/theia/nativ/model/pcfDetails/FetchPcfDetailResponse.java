package com.paytm.pgplus.theia.nativ.model.pcfDetails;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.ResponseHeader;

import java.io.Serializable;

public class FetchPcfDetailResponse implements Serializable {

    private static final long serialVersionUID = -5734130914877556332L;

    @JsonProperty("head")
    private ResponseHeader head;

    @JsonProperty("body")
    private FetchPcfDetailResponseBody body;

    public FetchPcfDetailResponse(ResponseHeader head, FetchPcfDetailResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public FetchPcfDetailResponseBody getBody() {
        return body;
    }

    public void setBody(FetchPcfDetailResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "FetchPcfDetailResponse{" + "head=" + head + ", body=" + body + '}';
    }

}
