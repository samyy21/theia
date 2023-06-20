package com.paytm.pgplus.theia.nativ.model.merchantuserinfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.nativ.model.common.TokenTypeRequestHeader;

import java.io.Serializable;

public class FetchMerchantUserInfoRequest implements Serializable {

    @JsonProperty("head")
    private TokenTypeRequestHeader head;

    @JsonProperty("body")
    private FetchMerchantUserInfoRequestBody body;

    private static final long serialVersionUID = 7984550283911348477L;

    public TokenTypeRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenTypeRequestHeader head) {
        this.head = head;
    }

    public FetchMerchantUserInfoRequestBody getBody() {
        return body;
    }

    public void setBody(FetchMerchantUserInfoRequestBody body) {
        this.body = body;
    }

}
