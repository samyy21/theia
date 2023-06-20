package com.paytm.pgplus.theia.nativ.model.payview.emi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.response.ResponseHeader;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantEmiDetailResponse implements Serializable {

    private static final long serialVersionUID = 7987921320176950911L;

    @JsonProperty("head")
    private ResponseHeader head;

    @JsonProperty("body")
    private MerchantEmiDetailResponseBody body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public MerchantEmiDetailResponseBody getBody() {
        return body;
    }

    public void setBody(MerchantEmiDetailResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
