package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.request.RequestHeader;

public class TokenTypeRequestHeader extends RequestHeader {

    @JsonProperty("tokenType")
    private String tokenType;

    @JsonProperty("token")
    private String token;

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TokenTypeRequestHeader [tokenType=").append(tokenType).append("token=")
                .append(super.toString()).append("]");
        return builder.toString();
    }
}
