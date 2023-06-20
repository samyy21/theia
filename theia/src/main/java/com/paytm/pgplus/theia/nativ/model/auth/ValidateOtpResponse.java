package com.paytm.pgplus.theia.nativ.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.response.ResponseHeader;
import io.swagger.annotations.ApiModel;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel(value = "ValidateOtpResponse", description = "Response for validate otp request")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidateOtpResponse implements Serializable {

    private static final long serialVersionUID = 324282992104952365L;

    @NotNull
    @Valid
    private ResponseHeader head;

    @NotNull
    @Valid
    private ValidateOtpResponseBody body;

    public ValidateOtpResponse() {
    }

    public ValidateOtpResponse(ResponseHeader head, ValidateOtpResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public ValidateOtpResponseBody getBody() {
        return body;
    }

    public void setBody(ValidateOtpResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ValidateOtpResponse{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
