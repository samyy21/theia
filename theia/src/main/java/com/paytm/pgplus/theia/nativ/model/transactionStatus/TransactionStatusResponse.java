package com.paytm.pgplus.theia.nativ.model.transactionStatus;

import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.models.NativeJsonResponseBody;
import com.paytm.pgplus.theia.nativ.model.vpa.details.VpaDetailsResponseBody;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class TransactionStatusResponse implements Serializable {

    private static final long serialVersionUID = -7726104176053530999L;

    @NotNull
    @Valid
    private ResponseHeader head;

    @NotNull
    @Valid
    private TransactionStatusResponseBody body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public TransactionStatusResponseBody getBody() {
        return body;
    }

    public void setBody(TransactionStatusResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransactionStatusResponse{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
