package com.paytm.pgplus.theia.nativ.model.vpa.details;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.paytm.pgplus.response.ResponseHeader;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "vpaDetailsResponse", description = "vpaDetailsResponse")
@JsonIgnoreProperties(ignoreUnknown = true)
public class VpaDetailsResponse implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7726104176053530999L;

    @NotNull
    @Valid
    private ResponseHeader head;

    @NotNull
    @Valid
    private VpaDetailsResponseBody body;

    public VpaDetailsResponse() {
    }

    public VpaDetailsResponse(ResponseHeader head, VpaDetailsResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public VpaDetailsResponseBody getBody() {
        return body;
    }

    public void setBody(VpaDetailsResponseBody body) {
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
