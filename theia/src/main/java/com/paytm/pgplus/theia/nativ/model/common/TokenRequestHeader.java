package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.paytm.pgplus.customaspects.annotations.Mask;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;
import com.paytm.pgplus.request.RequestHeader;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import org.apache.commons.lang3.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenRequestHeader extends RequestHeader {

    private final static long serialVersionUID = -7807095168614314271L;

    @JsonProperty("txnToken")
    private String txnToken;

    @JsonProperty("tokenType")
    private TokenType tokenType;

    @Mask(prefixNoMaskLen = 6, suffixNoMaskLen = 4)
    @JsonProperty("token")
    private String token;

    @JsonProperty("clientId")
    private String clientId;

    @JsonProperty("workFlow")
    private String workFlow;

    public String getTxnToken() {
        if (TokenType.TXN_TOKEN.equals(this.tokenType) && StringUtils.isEmpty(this.txnToken)) {
            return this.token;
        }
        return txnToken;
    }

    public void setTxnToken(String txnToken) {
        this.txnToken = txnToken;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String getWorkFlow() {
        return workFlow;
    }

    @Override
    public void setWorkFlow(String workFlow) {
        this.workFlow = workFlow;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
