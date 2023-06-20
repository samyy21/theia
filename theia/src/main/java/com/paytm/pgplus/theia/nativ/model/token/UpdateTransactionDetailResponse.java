package com.paytm.pgplus.theia.nativ.model.token;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.response.SecureResponseHeader;
import com.paytm.pgplus.response.interfaces.SecureResponse;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateTransactionDetailResponse implements SecureResponse {

    private static final long serialVersionUID = -9149656158378739982L;
    @NotNull
    @Valid
    private SecureResponseHeader head;

    @NotNull
    @Valid
    private BaseResponseBody body;

    public UpdateTransactionDetailResponse() {
    }

    public UpdateTransactionDetailResponse(SecureResponseHeader head, BaseResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public SecureResponseHeader getHead() {
        return head;
    }

    public void setHead(SecureResponseHeader head) {
        this.head = head;
    }

    public BaseResponseBody getBody() {
        return body;
    }

    public void setBody(BaseResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UpdateTransactionDetailResponse {");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
