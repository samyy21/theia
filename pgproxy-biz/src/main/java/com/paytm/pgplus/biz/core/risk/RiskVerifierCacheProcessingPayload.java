package com.paytm.pgplus.biz.core.risk;

public class RiskVerifierCacheProcessingPayload {

    private boolean successful;

    private String message;

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
