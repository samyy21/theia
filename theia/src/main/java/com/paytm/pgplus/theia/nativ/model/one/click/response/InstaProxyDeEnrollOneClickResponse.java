package com.paytm.pgplus.theia.nativ.model.one.click.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class InstaProxyDeEnrollOneClickResponse implements Serializable {

    private static final long serialVersionUID = 3938830900525177326L;

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

    public InstaProxyDeEnrollOneClickResponse() {
    }

    public InstaProxyDeEnrollOneClickResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InstaProxyDeEnrollOneClickResponse{");
        sb.append("status='").append(status).append('\'');
        sb.append(", message='").append(message).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
