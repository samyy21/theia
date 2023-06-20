/*
 * {user}
 * {date}
 */

package com.paytm.pgplus.theia.offline.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.offline.model.base.BaseResponse;
import io.swagger.annotations.ApiModel;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by rahulverma on 1/9/17.
 */
@ApiModel(value = "CashierInfoResponse", description = "Response for cashier page rendering")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CashierInfoResponse extends BaseResponse {

    private static final long serialVersionUID = -9149656158378739982L;
    @NotNull
    @Valid
    private ResponseHeader head;

    @NotNull
    @Valid
    private CashierInfoResponseBody body;

    public CashierInfoResponse() {
    }

    public CashierInfoResponse(ResponseHeader head, CashierInfoResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public CashierInfoResponseBody getBody() {
        return body;
    }

    public void setBody(CashierInfoResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CashierInfoResponse{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
