package com.paytm.pgplus.theia.nativ.model.payview.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.request.TokenRequestHeader;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NativeCashierInfoV4Request implements Serializable {

    @JsonProperty("head")
    private TokenRequestHeader head;

    @JsonProperty("body")
    private NativeCashierInfoV4RequestBody body;

    private final static long serialVersionUID = 3384550283911348477L;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public NativeCashierInfoV4RequestBody getBody() {
        return body;
    }

    public void setBody(NativeCashierInfoV4RequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("head", head).append("body", body).toString();
    }

}
