package com.paytm.pgplus.theia.nativ.model.promo;

import javax.validation.Valid;

import com.paytm.pgplus.theia.nativ.model.promo.NativePromoCodeDetailRequestBody;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.offline.model.base.BaseRequest;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NativePromoCodeDetailRequest extends BaseRequest {

    private static final long serialVersionUID = 5199032505424900108L;

    @Valid
    @NotBlank
    private TokenRequestHeader head;

    @Valid
    @NotBlank
    private NativePromoCodeDetailRequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public NativePromoCodeDetailRequestBody getBody() {
        return body;
    }

    public void setBody(NativePromoCodeDetailRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PromoCodeRequest{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
