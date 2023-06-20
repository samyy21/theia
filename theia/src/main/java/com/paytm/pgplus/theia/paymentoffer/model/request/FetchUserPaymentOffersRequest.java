package com.paytm.pgplus.theia.paymentoffer.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchUserPaymentOffersRequest implements Serializable {

    private static final long serialVersionUID = -2486579718001802183L;
    @Valid
    @NotNull
    private TokenRequestHeader head;

    @Valid
    @NotNull
    private FetchUserPaymentOffersRequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public FetchUserPaymentOffersRequestBody getBody() {
        return body;
    }

    public void setBody(FetchUserPaymentOffersRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return MaskToStringBuilder.toString(this);
    }
}
