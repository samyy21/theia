package com.paytm.pgplus.theia.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.request.SecureRequestHeader;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TheiaCreateAgreementRequest implements Serializable {

    private static final long serialVersionUID = -4748048701962609366L;

    @JsonProperty("head")
    private SecureRequestHeader head;

    @JsonProperty("body")
    private TheiaCreateAgreementRequestBody body;

    public SecureRequestHeader getHead() {
        return head;
    }

    public void setHead(SecureRequestHeader head) {
        this.head = head;
    }

    public TheiaCreateAgreementRequestBody getBody() {
        return body;
    }

    public void setBody(TheiaCreateAgreementRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TheiaCreateAgreementRequest [head=");
        builder.append(head);
        builder.append(", body=");
        builder.append(body);
        builder.append("]");
        return builder.toString();
    }
}
