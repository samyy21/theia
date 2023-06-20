package com.paytm.pgplus.theia.nativ.model.balanceinfo;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.response.ResponseHeader;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "BalanceInfoResponse", description = "BalanceInfoResponse")
@JsonIgnoreProperties(ignoreUnknown = true)
public class BalanceInfoResponse implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7726104176053530999L;

    @NotNull
    @Valid
    private ResponseHeader head;

    @NotNull
    @Valid
    private BalanceInfoResponseBody body;

    public BalanceInfoResponse() {
    }

    public BalanceInfoResponse(ResponseHeader head, BalanceInfoResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public BalanceInfoResponseBody getBody() {
        return body;
    }

    public void setBody(BalanceInfoResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BalanceInfoResponse{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
