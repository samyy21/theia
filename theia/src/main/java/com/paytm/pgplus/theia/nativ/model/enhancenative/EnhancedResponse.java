package com.paytm.pgplus.theia.nativ.model.enhancenative;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.offline.model.base.BaseResponse;

public class EnhancedResponse extends BaseResponse {

    private static final long serialVersionUID = -8711881765332301625L;

    ResponseHeader head;
    EnhancedResponseBody body;

    public EnhancedResponse(ResponseHeader head, EnhancedResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public EnhancedResponse(EnhancedResponseBody body) {
        this.body = body;
        this.head = new ResponseHeader();
    }

    public EnhancedResponse() {
        this.head = new ResponseHeader();
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public EnhancedResponseBody getBody() {
        return body;
    }

    public void setBody(EnhancedResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "EnhancedResponse{" + "head=" + head + ", body=" + body + '}';
    }
}
