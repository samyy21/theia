package com.paytm.pgplus.theia.models.upiAccount.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.ResponseHeader;
import org.hibernate.validator.constraints.NotBlank;

@Deprecated
@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchUpiOptionsResponse {
    @JsonProperty(value = "head")
    @NotBlank
    private ResponseHeader head;

    @JsonProperty(value = "body")
    @NotBlank
    private FetchUpiOptionsResponseBody body;

    public FetchUpiOptionsResponse() {
    }

    public FetchUpiOptionsResponse(ResponseHeader head, FetchUpiOptionsResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public FetchUpiOptionsResponse(FetchUpiOptionsResponseBody body) {
        this.body = body;
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public FetchUpiOptionsResponseBody getBody() {
        return body;
    }

    public void setBody(FetchUpiOptionsResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "FetchUPIOptionsResponse{" + "head=" + head + ", body=" + body + '}';
    }
}
