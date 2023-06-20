package com.paytm.pgplus.theia.nativ.model.common;

import java.io.Serializable;

public class NativeRetryInfo implements Serializable {

    private static final long serialVersionUID = 8563984613862994921L;

    private boolean retryAllowed;
    private String retryMessage;

    public NativeRetryInfo(boolean retryAllowed, String retryMessage) {
        this.retryAllowed = retryAllowed;
        this.retryMessage = retryMessage;
    }

    public boolean isRetryAllowed() {
        return retryAllowed;
    }

    public String getRetryMessage() {
        return retryMessage;
    }

    public void setRetryAllowed(boolean retryAllowed) {
        this.retryAllowed = retryAllowed;
    }

    public void setRetryMessage(String retryMessage) {
        this.retryMessage = retryMessage;
    }
}
