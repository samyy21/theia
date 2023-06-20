/*
 * {user}
 * {date}
 */

package com.paytm.pgplus.theia.offline.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.offline.model.base.BaseResponse;

import javax.validation.constraints.NotNull;

/**
 * Created by rahulverma on 1/9/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse extends BaseResponse {

    private static final long serialVersionUID = -2539622335258632774L;
    @NotNull
    private ResponseHeader head;

    @NotNull
    private ResponseBody body;

    public ErrorResponse() {
    }

    public ResponseHeader getHead() {
        return this.head;
    }

    public ResponseBody getBody() {
        return this.body;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public void setBody(ResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ErrorResponse{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
