package com.paytm.pgplus.theia.nativ.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.response.ResponseHeader;
import io.swagger.annotations.ApiModel;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel(value = "GenerateOtpResponse", description = "Response for generate otp request")
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenerateOtpResponse implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7726104176053530999L;

    @NotNull
    @Valid
    private ResponseHeader head;

    @NotNull
    @Valid
    private GenerateOtpResponseBody body;

    public GenerateOtpResponse() {
    }

    public GenerateOtpResponse(ResponseHeader head, GenerateOtpResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public GenerateOtpResponseBody getBody() {
        return body;
    }

    public void setBody(GenerateOtpResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GenerateOtpResponse{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
