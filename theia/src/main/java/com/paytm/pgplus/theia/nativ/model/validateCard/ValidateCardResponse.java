package com.paytm.pgplus.theia.nativ.model.validateCard;

import com.paytm.pgplus.response.ResponseHeader;

import java.io.Serializable;

public class ValidateCardResponse implements Serializable {

    private ResponseHeader head;

    private ValidateCardResponseBody body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public ValidateCardResponseBody getBody() {
        return body;
    }

    public void setBody(ValidateCardResponseBody body) {
        this.body = body;
    }

    public ValidateCardResponse() {
    }

    public ValidateCardResponse(ResponseHeader head, ValidateCardResponseBody body) {
        this.head = head;
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ValidateCardResponse{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }

}
