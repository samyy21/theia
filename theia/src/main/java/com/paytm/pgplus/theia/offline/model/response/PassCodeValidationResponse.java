/*
 * {user}
 * {date}
 */

package com.paytm.pgplus.theia.offline.model.response;

import com.paytm.pgplus.theia.offline.model.base.BaseResponse;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by rahulverma on 13/10/17.
 */
public class PassCodeValidationResponse extends BaseResponse {

    private static final long serialVersionUID = -9209847487373561470L;
    @NotNull
    @Valid
    private ResponseHeader head;

    @NotNull
    @Valid
    private PassCodeValidationResponseBody body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public PassCodeValidationResponseBody getBody() {
        return body;
    }

    public void setBody(PassCodeValidationResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PassCodeValidationResponse [head=");
        builder.append(head);
        builder.append(", body=");
        builder.append(body);
        builder.append("]");
        return builder.toString();
    }

}
