package com.paytm.pgplus.theia.nativ.model.auth;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "ValidateInfoRequest", description = "Request for validate otp")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidateOtpRequest implements Serializable {

    private static final long serialVersionUID = 6221848077250065146L;
    @NotNull
    @Valid
    private TokenRequestHeader head;

    @NotNull
    @Valid
    private ValidateOtpRequestBody body;

    public ValidateOtpRequest(TokenRequestHeader head, ValidateOtpRequestBody body) {
        this.head = head;
        this.body = body;
    }

    public ValidateOtpRequest() {
    }

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public ValidateOtpRequestBody getBody() {
        return body;
    }

    public void setBody(ValidateOtpRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ValidateOtpRequest{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
