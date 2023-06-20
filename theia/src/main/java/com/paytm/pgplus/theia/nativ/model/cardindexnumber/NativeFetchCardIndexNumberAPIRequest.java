package com.paytm.pgplus.theia.nativ.model.cardindexnumber;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.request.RequestHeader;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.offline.model.base.BaseRequest;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;

public class NativeFetchCardIndexNumberAPIRequest extends BaseRequest {

    private static final long serialVersionUID = 2951033047048406304L;

    @JsonProperty(value = "head")
    @Valid
    @NotBlank
    private TokenRequestHeader head;

    @JsonProperty(value = "body")
    @Valid
    @NotBlank
    private NativeFetchCardIndexNumberAPIRequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public NativeFetchCardIndexNumberAPIRequestBody getBody() {
        return body;
    }

    public void setBody(NativeFetchCardIndexNumberAPIRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "NativeFetchCardIndexNumberAPIRequest{" + "head=" + head + ", body=" + body + '}';
    }
}
