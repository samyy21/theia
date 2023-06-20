package com.paytm.pgplus.theia.models.upiAccount.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.ResponseHeader;
import org.hibernate.validator.constraints.NotBlank;

@Deprecated
public class CheckUPIAccountResponse {
    @JsonProperty(value = "head")
    @NotBlank
    private ResponseHeader head;

    @JsonProperty(value = "body")
    @NotBlank
    private CheckUPIAccountResponseBody body;

    public CheckUPIAccountResponse() {
    }

    public CheckUPIAccountResponse(ResponseHeader head, CheckUPIAccountResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public CheckUPIAccountResponse(CheckUPIAccountResponseBody body) {
        this.body = body;
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public CheckUPIAccountResponseBody getBody() {
        return body;
    }

    public void setBody(CheckUPIAccountResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "CheckUPIAccountResponse{" + "head=" + head + ", body=" + body + '}';
    }
}
