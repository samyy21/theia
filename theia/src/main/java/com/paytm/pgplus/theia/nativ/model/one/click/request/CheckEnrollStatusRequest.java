package com.paytm.pgplus.theia.nativ.model.one.click.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.nativ.model.common.SsoTokenRequestHeader;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckEnrollStatusRequest implements Serializable {

    private static final long serialVersionUID = 966928229449682053L;

    @JsonProperty("head")
    private SsoTokenRequestHeader head;

    @JsonProperty("body")
    private CheckEnrollStatusRequestBody body;

    public CheckEnrollStatusRequest() {
    }

    public CheckEnrollStatusRequest(SsoTokenRequestHeader head, CheckEnrollStatusRequestBody body) {
        this.head = head;
        this.body = body;
    }

    public SsoTokenRequestHeader getHead() {
        return head;
    }

    public void setHead(SsoTokenRequestHeader head) {
        this.head = head;
    }

    public CheckEnrollStatusRequestBody getBody() {
        return body;
    }

    public void setBody(CheckEnrollStatusRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("EnrollStatusCheckRequest {");
        sb.append(" head='").append(head).append('\'').append(", body='").append(body).append('\'').append('}');
        return sb.toString();
    }

}
