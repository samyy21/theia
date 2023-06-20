package com.paytm.pgplus.theia.nativ.model.prn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.response.ResponseHeader;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NativeValidatePRNResponse implements Serializable {

    private static final long serialVersionUID = 3913592129606449033L;

    private ResponseHeader head;

    private NativeValidatePRNResponseBody body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public NativeValidatePRNResponseBody getBody() {
        return body;
    }

    public void setBody(NativeValidatePRNResponseBody body) {
        this.body = body;
    }

    public NativeValidatePRNResponse() {
    }

    public NativeValidatePRNResponse(ResponseHeader head, NativeValidatePRNResponseBody body) {
        this.head = head;
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NativeValidatePRNResponse{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
