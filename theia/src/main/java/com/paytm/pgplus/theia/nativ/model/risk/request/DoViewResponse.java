package com.paytm.pgplus.theia.nativ.model.risk.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.ResponseHeader;

import java.io.Serializable;

/**
 * @author Ravi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoViewResponse implements Serializable {

    private static final long serialVersionUID = -2924532865113453504L;

    @JsonProperty("head")
    private ResponseHeader head;

    @JsonProperty("body")
    private DoViewResponseBody body;

    public DoViewResponse(ResponseHeader head, DoViewResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public ResponseHeader getHead() {
        return head;
    }

    public DoViewResponseBody getBody() {
        return body;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public void setBody(DoViewResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "DoViewResponse{" + "head=" + head + ", body=" + body + '}';
    }
}
