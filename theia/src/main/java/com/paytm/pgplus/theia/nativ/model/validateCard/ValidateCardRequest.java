package com.paytm.pgplus.theia.nativ.model.validateCard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.common.model.nativ.SecuredRequestHeader;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.model.prn.NativeValidatePRNRequestBody;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidateCardRequest implements Serializable {

    private static final long serialVersionUID = 1064203839799033350L;

    @NotNull
    @Valid
    private TokenRequestHeader head;

    @NotNull
    @Valid
    private ValidateCardRequestBody body;

    public ValidateCardRequest() {
    }

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public ValidateCardRequestBody getBody() {
        return body;
    }

    public void setBody(ValidateCardRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ValidateCardRequest{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
