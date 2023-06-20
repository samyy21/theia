package com.paytm.pgplus.theia.offline.model.request;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.offline.model.base.BaseRequest;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FastForwardRequest extends BaseRequest {

    private static final long serialVersionUID = -9165702917193734540L;

    @NotNull
    @Valid
    private RequestHeader head;

    @NotNull
    @Valid
    private FastForwardRequestBody body;

    public FastForwardRequest() {

    }

    public RequestHeader getHead() {
        return head;
    }

    public void setHead(RequestHeader head) {
        this.head = head;
    }

    public FastForwardRequestBody getBody() {
        return body;
    }

    public void setBody(FastForwardRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FastForwardRequest{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
