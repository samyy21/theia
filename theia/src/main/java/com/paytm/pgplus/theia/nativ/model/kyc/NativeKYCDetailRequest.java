package com.paytm.pgplus.theia.nativ.model.kyc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.offline.model.base.BaseRequest;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NativeKYCDetailRequest extends BaseRequest {

    @Valid
    @NotBlank
    private TokenRequestHeader head;

    @Valid
    @NotBlank
    private NativeKYCDetailRequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public NativeKYCDetailRequestBody getBody() {
        return body;
    }

    public void setBody(NativeKYCDetailRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NativeKYCDetailRequestBody{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
