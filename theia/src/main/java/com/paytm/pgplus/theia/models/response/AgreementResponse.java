package com.paytm.pgplus.theia.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.SecureResponseHeader;
import com.paytm.pgplus.response.interfaces.SecureResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AgreementResponse implements SecureResponse {

    private static final long serialVersionUID = -4660879241264522681L;

    @JsonProperty("head")
    private SecureResponseHeader head;

    @JsonProperty("body")
    private AgreementResponseBody body;

    public AgreementResponse(SecureResponseHeader head, AgreementResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public AgreementResponse() {

    }

    @Override
    public SecureResponseHeader getHead() {
        return head;
    }

    public void setHead(SecureResponseHeader head) {
        this.head = head;
    }

    @Override
    public AgreementResponseBody getBody() {
        return body;
    }

    public void setBody(AgreementResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgreementResponse{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
