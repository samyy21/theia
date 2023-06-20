package com.paytm.pgplus.theia.nativ.model.token;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.nativ.model.common.TokenSecureRequestHeader;

import java.io.Serializable;

public class UpdateTransactionDetailRequest implements Serializable {

    @JsonProperty("head")
    private TokenSecureRequestHeader head;

    @JsonProperty("body")
    private UpdateTransactionDetailRequestBody body;

    private final static long serialVersionUID = 6183126127771924331L;

    public TokenSecureRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenSecureRequestHeader head) {
        this.head = head;
    }

    public UpdateTransactionDetailRequestBody getBody() {
        return body;
    }

    public void setBody(UpdateTransactionDetailRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TransactionRequest [head=");
        builder.append(head);
        builder.append(", body=");
        builder.append(body);
        builder.append("]");
        return builder.toString();
    }

}
