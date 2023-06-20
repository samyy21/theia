package com.paytm.pgplus.theia.offline.model.request;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.offline.model.base.BaseResponse;
import com.paytm.pgplus.theia.offline.model.response.ResponseHeader;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FastForwardResponse extends BaseResponse {

    private static final long serialVersionUID = -9167702917193734540L;

    @NotNull
    @Valid
    private ResponseHeader head;

    @NotNull
    @Valid
    private FastForwardResponseBody body;

    public FastForwardResponse() {

    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public FastForwardResponseBody getBody() {
        return body;
    }

    public void setBody(FastForwardResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FastForwardResponse{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
