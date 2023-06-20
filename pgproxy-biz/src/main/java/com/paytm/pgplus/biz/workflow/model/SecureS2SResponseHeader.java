package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class SecureS2SResponseHeader extends BaseS2SResponseHeader {

    private static final long serialVersionUID = -6680266438614143806L;

    @JsonIgnore
    protected String clientId;

    @JsonIgnore
    protected String signature;

    public SecureS2SResponseHeader() {
    }

    public SecureS2SResponseHeader(String clientId, String version, String responseTimestamp, String signature) {
        super(version, responseTimestamp);
        this.clientId = clientId;
        this.signature = signature;
    }

    public SecureS2SResponseHeader(String version, String responseTimestamp) {
        super(version, responseTimestamp);
    }

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
        builder.append("SecureS2SResponseHeader [clientId=").append(clientId).append(", signature=").append(signature)
                .append(", version=").append(version).append(", responseTimestamp=").append(responseTimestamp)
                .append("]");
        return builder.toString();
    }

}
