package com.paytm.pgplus.theia.nativ.model.cardindexnumber;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.SecureResponseHeader;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import java.io.Serializable;

public class NativeFetchCardIndexNumberAPIResponse implements Serializable {

    private final static long serialVersionUID = -8577403610903555955L;

    @JsonProperty(value = "head")
    @Valid
    @NotBlank
    private ResponseHeader head;

    @JsonProperty(value = "body")
    @Valid
    @NotBlank
    private NativeFetchCardIndexNumberAPIResponseBody body;

    public NativeFetchCardIndexNumberAPIResponse(ResponseHeader head, NativeFetchCardIndexNumberAPIResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(SecureResponseHeader head) {
        this.head = head;
    }

    public NativeFetchCardIndexNumberAPIResponseBody getBody() {
        return body;
    }

    public void setBody(NativeFetchCardIndexNumberAPIResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "NativeFetchCardIndexNumberAPIResponse{" + "head=" + head + ", body=" + body + '}';
    }
}
