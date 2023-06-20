package com.paytm.pgplus.theia.nativ.model.payview.emi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.common.model.nativ.SecuredRequestHeader;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantEmiDetailRequest implements Serializable {

    private static final long serialVersionUID = -3864807953692481649L;

    @JsonProperty("head")
    private SecuredRequestHeader head;

    @JsonProperty("body")
    private MerchantEmiDetailRequestBody body;

    public SecuredRequestHeader getHead() {
        return head;
    }

    public void setHead(SecuredRequestHeader head) {
        this.head = head;
    }

    public MerchantEmiDetailRequestBody getBody() {
        return body;
    }

    public void setBody(MerchantEmiDetailRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
