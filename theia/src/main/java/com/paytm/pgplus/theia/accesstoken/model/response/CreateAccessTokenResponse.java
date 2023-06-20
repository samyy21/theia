package com.paytm.pgplus.theia.accesstoken.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.common.model.nativ.ResponseHeader;

import java.io.Serializable;

public class CreateAccessTokenResponse implements Serializable {

    private static final long serialVersionUID = 5806229648814801061L;

    @JsonProperty(value = "head")
    private ResponseHeader head;

    @JsonProperty(value = "body")
    private CreateAccessTokenResponseBody body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public CreateAccessTokenResponseBody getBody() {
        return body;
    }

    public void setBody(CreateAccessTokenResponseBody body) {
        this.body = body;
    }

    public CreateAccessTokenResponse() {
        this.head = new ResponseHeader();
        this.body = new CreateAccessTokenResponseBody();
    }

}
