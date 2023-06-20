package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.request.SecureRequestHeader;
import org.apache.commons.lang.builder.ToStringBuilder;

public class TokenSecureRequestHeader extends SecureRequestHeader {

    private static final long serialVersionUID = 7097079225077220033L;

    @JsonProperty("txnToken")
    private String txnToken;

    public String getTxnToken() {
        return txnToken;
    }

    public void setTxnToken(String txnToken) {
        this.txnToken = txnToken;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TokenSecureRequestHeader{");
        sb.append("txnToken='").append(txnToken).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
