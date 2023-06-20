package com.paytm.pgplus.theia.nativ.model.validateCard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardNumberValidationRequest implements Serializable {

    private static final long serialVersionUID = 8443401006623221315L;

    private TokenRequestHeader head;

    private CardNumberValidationRequestBody body;

    public CardNumberValidationRequest() {
    }

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public CardNumberValidationRequestBody getBody() {
        return body;
    }

    public void setBody(CardNumberValidationRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CardNumberValidationRequest{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
