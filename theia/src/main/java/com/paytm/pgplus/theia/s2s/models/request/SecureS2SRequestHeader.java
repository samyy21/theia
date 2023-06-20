package com.paytm.pgplus.theia.s2s.models.request;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecureS2SRequestHeader extends BaseS2SRequestHeader {

    private static final long serialVersionUID = 2292692611570250946L;

    @NotEmpty(message = "client id cannot be blank")
    protected String clientId;

    @NotEmpty(message = "signature cannot be blank")
    protected String signature;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SecureS2SRequestHeader [clientId=").append(clientId).append(", signature=").append(signature)
                .append(", version=").append(version).append(", requestTimestamp=").append(requestTimestamp)
                .append("]");
        return builder.toString();
    }

}
