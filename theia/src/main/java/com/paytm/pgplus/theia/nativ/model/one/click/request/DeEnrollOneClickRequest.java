package com.paytm.pgplus.theia.nativ.model.one.click.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.nativ.model.common.SsoTokenRequestHeader;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeEnrollOneClickRequest implements Serializable {

    private static final long serialVersionUID = 7366928229449682053L;

    @JsonProperty("head")
    private SsoTokenRequestHeader head;

    @JsonProperty("body")
    private DeEnrollOneClickRequestBody body;

    public DeEnrollOneClickRequest(SsoTokenRequestHeader head, DeEnrollOneClickRequestBody body) {
        this.head = head;
        this.body = body;
    }

    public DeEnrollOneClickRequest() {
    }

    public SsoTokenRequestHeader getHead() {
        return head;
    }

    public void setHead(SsoTokenRequestHeader head) {
        this.head = head;
    }

    public DeEnrollOneClickRequestBody getBody() {
        return body;
    }

    public void setBody(DeEnrollOneClickRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("DeEnrollOneClickRequest {");
        sb.append(" head='").append(head.toString()).append('\'').append(", body='").append(body.toString())
                .append('\'').append('}');
        return sb.toString();
    }

}