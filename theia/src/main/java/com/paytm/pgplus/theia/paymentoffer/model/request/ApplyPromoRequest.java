package com.paytm.pgplus.theia.paymentoffer.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplyPromoRequest implements Serializable {

    private static final long serialVersionUID = -2284447185409620484L;
    @NotNull
    @Valid
    private TokenRequestHeader head;
    @NotNull
    @Valid
    private ApplyPromoRequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public ApplyPromoRequestBody getBody() {
        return body;
    }

    public void setBody(ApplyPromoRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return MaskToStringBuilder.toString(this);
    }
}
