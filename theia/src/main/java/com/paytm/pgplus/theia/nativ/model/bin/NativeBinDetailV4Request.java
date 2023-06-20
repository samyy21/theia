package com.paytm.pgplus.theia.nativ.model.bin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;
import com.paytm.pgplus.request.TokenRequestHeader;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NativeBinDetailV4Request implements Serializable {

    private static final long serialVersionUID = -6189288811781624643L;

    @Valid
    @NotBlank
    private TokenRequestHeader head;

    @Valid
    @NotBlank
    private NativeBinDetailV4RequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public NativeBinDetailV4RequestBody getBody() {
        return body;
    }

    public void setBody(NativeBinDetailV4RequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
