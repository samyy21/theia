package com.paytm.pgplus.theia.enums;

/**
 * Created by prashant on 1/31/17.
 */
public enum UPIPollStatus {

    STOP_POLLING("STOP_POLLING"), POLL_AGAIN("POLL_AGAIN");

    private String message;

    UPIPollStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
