package com.paytm.pgplus.theia.emiSubvention.model.request.validate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidateEmiRequest implements Serializable {

    private static final long serialVersionUID = -2462418142769624239L;

    @NotNull
    TokenRequestHeader head;

    @NotNull
    @Valid
    private ValidateEmiRequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public ValidateEmiRequestBody getBody() {
        return body;
    }

    public void setBody(ValidateEmiRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ValidateEmiRequest [head=").append(head).append("body").append(body).append("]");
        return builder.toString();
    }

}
