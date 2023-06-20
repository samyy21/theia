package com.paytm.pgplus.theia.models.banktransfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.request.SecureRequestHeader;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class InternalTransactionRequest implements Serializable {

    private final static long serialVersionUID = 1216672408562032165L;

    @JsonProperty(value = "head")
    @NotBlank
    private SecureRequestHeader head;

    @JsonProperty(value = "body")
    @NotBlank
    private InternalTransactionRequestBody body;

    public SecureRequestHeader getHead() {
        return head;
    }

    public void setHead(SecureRequestHeader head) {
        this.head = head;
    }

    public InternalTransactionRequestBody getBody() {
        return body;
    }

    public void setBody(InternalTransactionRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("InternalTransactionRequest{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
