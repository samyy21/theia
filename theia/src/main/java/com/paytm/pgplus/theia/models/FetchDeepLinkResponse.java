package com.paytm.pgplus.theia.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.biz.workflow.model.FetchDeepLinkResponseBody;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FetchDeepLinkResponse implements Serializable {

    private static final long serialVersionUID = 7934528011842871891L;

    private FetchDeepLinkResponseHeader head;
    private FetchDeepLinkResponseBody body;

    public FetchDeepLinkResponseHeader getHead() {
        return head;
    }

    public void setHead(FetchDeepLinkResponseHeader head) {
        this.head = head;
    }

    public FetchDeepLinkResponseBody getBody() {
        return body;
    }

    public void setBody(FetchDeepLinkResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FetchDeepLinkResponse{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
