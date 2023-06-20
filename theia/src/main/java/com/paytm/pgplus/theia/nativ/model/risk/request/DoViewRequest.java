package com.paytm.pgplus.theia.nativ.model.risk.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

import java.io.Serializable;

/**
 * @author Ravi
 *
 */
public class DoViewRequest implements Serializable {

    private static final long serialVersionUID = -4584173398509889764L;

    @JsonProperty("head")
    private TokenRequestHeader head;

    @JsonProperty("body")
    private DoViewRequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public DoViewRequestBody getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "DoViewRequest{" + "head=" + head + ", body=" + body + '}';
    }
}
