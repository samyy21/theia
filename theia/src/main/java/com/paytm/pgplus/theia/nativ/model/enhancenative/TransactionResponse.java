package com.paytm.pgplus.theia.nativ.model.enhancenative;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.offline.model.base.BaseResponse;

/**
 * Created by rahulverma on 7/4/18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionResponse extends BaseResponse {

    private static final long serialVersionUID = -83128644233368062L;

    ResponseHeader head;
    TransactionResponseBody body;

    public TransactionResponse() {
        this.head = new ResponseHeader();
    }

    public TransactionResponse(TransactionResponseBody body) {
        this.body = body;
        this.head = new ResponseHeader();
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public TransactionResponseBody getBody() {
        return body;
    }

    public void setBody(TransactionResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "TransactionResponse{" + "head=" + head + ", body=" + body + '}';
    }
}
