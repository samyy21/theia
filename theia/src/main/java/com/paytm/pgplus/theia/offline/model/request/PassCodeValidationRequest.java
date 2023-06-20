package com.paytm.pgplus.theia.offline.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.offline.model.base.BaseRequest;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by rahulverma on 13/10/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PassCodeValidationRequest extends BaseRequest {

    private static final long serialVersionUID = 1246025601354387041L;
    @NotNull
    @Valid
    private RequestHeader head;

    @NotNull
    @Valid
    PassCodeValidationRequestBody body;

    public RequestHeader getHead() {
        return head;
    }

    public void setHead(RequestHeader head) {
        this.head = head;
    }

    public PassCodeValidationRequestBody getBody() {
        return body;
    }

    public void setBody(PassCodeValidationRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PassCodeValidationRequest [head=");
        builder.append(head);
        builder.append(", body=");
        builder.append(body);
        builder.append("]");
        return builder.toString();
    }

}
