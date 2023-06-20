package com.paytm.pgplus.theia.nativ.model.fetchpspapps;

import com.paytm.pgplus.theia.offline.model.response.ResponseHeader;

public class FetchPspAppsResponse {

    private ResponseHeader head;

    private FetchPspAppsResponseBody body;

    public FetchPspAppsResponse() {

    }

    public FetchPspAppsResponse(ResponseHeader head, FetchPspAppsResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public FetchPspAppsResponseBody getBody() {
        return body;
    }

    public void setBody(FetchPspAppsResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "FetchPspAppsResponse{" + "head=" + head + ", body=" + body + '}';
    }
}
