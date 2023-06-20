package com.paytm.pgplus.theia.nativ.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.response.ResponseHeader;
import io.swagger.annotations.ApiModel;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel(value = "EnhancedValidateOtpResponse", description = "Response for validate otp request")
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedValidateOtpResponse implements Serializable {

    private static final long serialVersionUID = -8625627074532505940L;

    @NotNull
    @Valid
    private ResponseHeader head;

    @NotNull
    @Valid
    private EnhancedValidateOtpResponseBody body;

    public EnhancedValidateOtpResponse() {
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public EnhancedValidateOtpResponseBody getBody() {
        return body;
    }

    public void setBody(EnhancedValidateOtpResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(
                "com.paytm.pgplus.theia.nativ.model.auth.EnhancedValidateOtpResponse{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
