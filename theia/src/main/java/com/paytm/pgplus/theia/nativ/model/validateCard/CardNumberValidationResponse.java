package com.paytm.pgplus.theia.nativ.model.validateCard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.response.ResponseHeader;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardNumberValidationResponse implements Serializable {

    private static final long serialVersionUID = -6650118153447216880L;

    private ResponseHeader head;

    private CardNumberValidationResponseBody body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public CardNumberValidationResponseBody getBody() {
        return body;
    }

    public void setBody(CardNumberValidationResponseBody body) {
        this.body = body;
    }

    public CardNumberValidationResponse() {
    }

    public CardNumberValidationResponse(ResponseHeader head, CardNumberValidationResponseBody body) {
        this.head = head;
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CardNumberValidationResponse{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }

}
