package com.paytm.pgplus.theia.emiSubvention.model.request.tenures;

import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class EmiTenuresRequest implements Serializable {

    private static final long serialVersionUID = -2937450451597604397L;

    @NotNull
    TokenRequestHeader head;

    @NotNull
    @Valid
    private EmiTenuresRequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public EmiTenuresRequestBody getBody() {
        return body;
    }

    public void setBody(EmiTenuresRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TenuresEmiRequest [head=").append(head).append("body").append(body).append("]");
        return builder.toString();
    }
}
