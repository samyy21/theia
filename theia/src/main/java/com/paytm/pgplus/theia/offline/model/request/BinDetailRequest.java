package com.paytm.pgplus.theia.offline.model.request;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.offline.model.base.BaseRequest;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BinDetailRequest extends BaseRequest {

    private static final long serialVersionUID = -5841831427987006973L;

    @Valid
    @NotBlank
    private RequestHeader head;

    @Valid
    @NotBlank
    private BinDetailRequestBody body;

    public BinDetailRequest() {
        super();
    }

    public BinDetailRequest(RequestHeader head, BinDetailRequestBody body) {
        super();
        this.head = head;
        this.body = body;
    }

    public RequestHeader getHead() {
        return head;
    }

    public void setHead(RequestHeader head) {
        this.head = head;
    }

    public BinDetailRequestBody getBody() {
        return body;
    }

    public void setBody(BinDetailRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BinDetailRequest{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
