package com.paytm.pgplus.theia.nativ.model.pcfDetails;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.model.payview.emi.FetchEmiDetailRequestBody;

import java.io.Serializable;

public class FetchPcfDetailRequest implements Serializable {

    private static final long serialVersionUID = -6618259342584926509L;
    @JsonProperty("head")
    private TokenRequestHeader head;

    @JsonProperty("body")
    private FetchPcfDetailRequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public FetchPcfDetailRequestBody getBody() {
        return body;
    }

    public void setBody(FetchPcfDetailRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "FetchPcfDetailRequest{" + "head=" + head + ", body=" + body + '}';
    }

}
