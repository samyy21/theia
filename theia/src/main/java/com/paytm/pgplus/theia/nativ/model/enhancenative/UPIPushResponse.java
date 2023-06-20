package com.paytm.pgplus.theia.nativ.model.enhancenative;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.offline.model.base.BaseResponse;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UPIPushResponse extends BaseResponse {
    private static final long serialVersionUID = 3751397070832852448L;

    private ResponseHeader head;
    private UPIPushResponseBody body;

    public UPIPushResponse(ResponseHeader head, UPIPushResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public UPIPushResponse(UPIPushResponseBody body) {
        this.body = body;
        this.head = new ResponseHeader();
    }

    public UPIPushResponse() {
        this.head = new ResponseHeader();
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public UPIPushResponseBody getBody() {
        return body;
    }

    public void setBody(UPIPushResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "UPIPushResponse{" + "head=" + head + ", body=" + body + '}';
    }
}
