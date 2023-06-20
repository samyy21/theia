package com.paytm.pgplus.theia.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.response.SecureResponseHeader;
import com.paytm.pgplus.response.interfaces.SecureResponse;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by anamika on 24/10/18.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class VpaValidateResponse implements SecureResponse {

    @NotNull
    @Valid
    private SecureResponseHeader head;

    @NotNull
    @Valid
    private PPBLUPICollectData body;

    public VpaValidateResponse() {
    }

    public VpaValidateResponse(SecureResponseHeader head, PPBLUPICollectData body) {
        this.head = head;
        this.body = body;
    }

    public SecureResponseHeader getHead() {
        return head;
    }

    public void setHead(SecureResponseHeader head) {
        this.head = head;
    }

    @Override
    @NotNull
    public PPBLUPICollectData getBody() {
        return body;
    }

    public void setBody(PPBLUPICollectData body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PPBLUPICollectData{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}