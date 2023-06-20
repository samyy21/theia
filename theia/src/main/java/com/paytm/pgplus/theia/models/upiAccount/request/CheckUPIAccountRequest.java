package com.paytm.pgplus.theia.models.upiAccount.request;

import org.hibernate.validator.constraints.NotBlank;

@Deprecated
public class CheckUPIAccountRequest {

    @NotBlank
    private CheckUPIAccountRequestHeader head;

    @NotBlank
    private CheckUPIAccountRequestBody body;

    public CheckUPIAccountRequest() {
    }

    public CheckUPIAccountRequestHeader getHead() {
        return head;
    }

    public void setHead(CheckUPIAccountRequestHeader head) {
        this.head = head;
    }

    public CheckUPIAccountRequestBody getBody() {
        return body;
    }

    public void setBody(CheckUPIAccountRequestBody body) {
        this.body = body;
    }
}
