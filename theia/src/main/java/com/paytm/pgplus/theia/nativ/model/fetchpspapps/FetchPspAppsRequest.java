package com.paytm.pgplus.theia.nativ.model.fetchpspapps;

import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

public class FetchPspAppsRequest {

    private TokenRequestHeader head;

    private FetchPspAppsRequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHeader(TokenRequestHeader head) {
        this.head = head;
    }

    public FetchPspAppsRequestBody getBody() {
        return body;
    }

    public void setBody(FetchPspAppsRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "FetchPspAppsRequest{" + "header=" + head + ", body=" + body + '}';
    }
}
