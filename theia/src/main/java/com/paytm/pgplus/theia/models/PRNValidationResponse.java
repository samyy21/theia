package com.paytm.pgplus.theia.models;

import java.io.Serializable;

public class PRNValidationResponse implements Serializable {

    private static final long serialVersionUID = -5647788887166623158L;

    private String status;

    private boolean retryAllowed;

    public PRNValidationResponse(String status, boolean retryAllowed) {
        this.status = status;
        this.retryAllowed = retryAllowed;
    }

    public boolean isRetryAllowed() {
        return retryAllowed;
    }

    public void setRetryAllowed(boolean retryAllowed) {
        this.retryAllowed = retryAllowed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PRNValidationResponse{");
        sb.append("retryAllowed=").append(retryAllowed);
        sb.append(", status='").append(status).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
