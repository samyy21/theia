package com.paytm.pgplus.theia.nativ.model.auth;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "GenerateOtpRequest", description = "Request for send otp to user mobile")
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenerateOtpRequest implements Serializable {

    private static final long serialVersionUID = 6221848077250065146L;
    @NotNull
    @Valid
    private TokenRequestHeader head;

    @NotNull
    @Valid
    private GenerateOtpRequestBody body;

    public GenerateOtpRequest(TokenRequestHeader head, GenerateOtpRequestBody body) {
        this.head = head;
        this.body = body;
    }

    public GenerateOtpRequest() {
    }

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public GenerateOtpRequestBody getBody() {
        return body;
    }

    public void setBody(GenerateOtpRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GenerateOtpRequest{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
