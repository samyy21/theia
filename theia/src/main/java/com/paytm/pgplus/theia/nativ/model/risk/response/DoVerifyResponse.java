package com.paytm.pgplus.theia.nativ.model.risk.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.ResponseHeader;

import java.io.Serializable;

/**
 * @author Ravi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DoVerifyResponse implements Serializable {

    private static final long serialVersionUID = 1002886141690737814L;

    @JsonProperty("head")
    private ResponseHeader head;

    @JsonProperty("body")
    private DoVerifyResponseBody body;

    public DoVerifyResponse(ResponseHeader head, DoVerifyResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public ResponseHeader getHead() {
        return head;
    }

    public DoVerifyResponseBody getBody() {
        return body;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public void setBody(DoVerifyResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "DoVerifyResponse{" + "head=" + head + ", body=" + body + '}';
    }
}
