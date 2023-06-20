package com.paytm.pgplus.theia.nativ.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.ResponseHeader;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class ValidateVpaResponse implements Serializable {

    private static final long serialVersionUID = 5997003545853030143L;
    @JsonProperty("head")
    @NotNull
    @Valid
    private ResponseHeader head;

    @JsonProperty("body")
    @NotNull
    @Valid
    private NativeValidateVpaResponse body;

    public ValidateVpaResponse() {
    }

    public ValidateVpaResponse(ResponseHeader head, NativeValidateVpaResponse body) {
        this.head = head;
        this.body = body;
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public NativeValidateVpaResponse getBody() {
        return body;
    }

    public void setBody(NativeValidateVpaResponse body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "ValidateVpaResponse{" + "head=" + head + ", body=" + body + '}';
    }
}
