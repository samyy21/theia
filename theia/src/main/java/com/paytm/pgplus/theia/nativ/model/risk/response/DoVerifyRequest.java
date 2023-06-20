package com.paytm.pgplus.theia.nativ.model.risk.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

import java.io.Serializable;

/**
 * @author Ravi
 *
 */
public class DoVerifyRequest implements Serializable {

    private static final long serialVersionUID = -670791404454634974L;

    @JsonProperty("head")
    private TokenRequestHeader head;

    @JsonProperty("body")
    private DoVerifyRequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public DoVerifyRequestBody getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "DoVerifyRequest{" + "head=" + head + ", body=" + body + '}';
    }
}
