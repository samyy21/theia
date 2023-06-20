package com.paytm.pgplus.theia.nativ.model.bin;

import javax.validation.Valid;

import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.offline.model.base.BaseRequest;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NativeBinDetailRequest extends BaseRequest {

    private static final long serialVersionUID = -5841831427987006973L;

    @Valid
    @NotBlank
    private TokenRequestHeader head;

    @Valid
    @NotBlank
    private NativeBinDetailRequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public NativeBinDetailRequestBody getBody() {
        return body;
    }

    public void setBody(NativeBinDetailRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
