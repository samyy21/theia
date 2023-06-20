package com.paytm.pgplus.theia.aoa.model.orderpay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.request.SecureRequestHeader;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

public class CreateOrderAndPaymentRequest implements Serializable {

    private final static long serialVersionUID = 2649442500378392750L;

    @JsonProperty(value = "head")
    @NotBlank
    private SecureRequestHeader head;

    @JsonProperty(value = "body")
    @NotBlank
    private CreateOrderAndPaymentRequestBody body;

    public SecureRequestHeader getHead() {
        return head;
    }

    public void setHead(SecureRequestHeader head) {
        this.head = head;
    }

    public CreateOrderAndPaymentRequestBody getBody() {
        return body;
    }

    public void setBody(CreateOrderAndPaymentRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "CreateOrderAndPaymentRequest{" + "head=" + head + ", body=" + body + '}';
    }
}
