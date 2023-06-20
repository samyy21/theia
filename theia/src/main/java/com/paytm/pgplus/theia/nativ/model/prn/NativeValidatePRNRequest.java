package com.paytm.pgplus.theia.nativ.model.prn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NativeValidatePRNRequest implements Serializable {

    private static final long serialVersionUID = 1837161071913695881L;
    @NotNull
    @Valid
    private TokenRequestHeader head;

    @NotNull
    @Valid
    private NativeValidatePRNRequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public NativeValidatePRNRequestBody getBody() {
        return body;
    }

    public void setBody(NativeValidatePRNRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NativeValidatePRNRequest{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
