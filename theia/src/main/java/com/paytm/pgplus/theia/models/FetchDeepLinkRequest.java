package com.paytm.pgplus.theia.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@JsonIgnoreProperties
public class FetchDeepLinkRequest {

    @NotNull(message = "Head passed in the request is null")
    @Valid
    private FetchDeepLinkRequestHeader head;
    @NotNull(message = "Body passed in the request is null")
    @Valid
    private FetchDeepLinkRequestBody body;

    @NotNull
    public FetchDeepLinkRequestHeader getHead() {
        return head;
    }

    public void setHead(@NotNull FetchDeepLinkRequestHeader head) {
        this.head = head;
    }

    @NotNull
    public FetchDeepLinkRequestBody getBody() {
        return body;
    }

    public void setBody(@NotNull FetchDeepLinkRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FetchDeepLinkRequest{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
