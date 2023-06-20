package com.paytm.pgplus.theia.emiSubvention.model.request.banks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmiBanksRequest implements Serializable {

    private static final long serialVersionUID = -4659423609882853698L;

    @NotNull
    TokenRequestHeader head;

    @NotNull
    private EmiBanksRequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public EmiBanksRequestBody getBody() {
        return body;
    }

    public void setBody(EmiBanksRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BanksEmiRequest [head=").append(head).append("body").append(body).append("]");
        return builder.toString();
    }

}
