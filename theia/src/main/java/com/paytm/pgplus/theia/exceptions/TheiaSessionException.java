package com.paytm.pgplus.theia.exceptions;

/**
 * @author amit.dubey on 26/04/18
 */
public class TheiaSessionException extends RuntimeException {

    private static final long serialVersionUID = -6402700046545681931L;

    /**
     * @param message
     */
    public TheiaSessionException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public TheiaSessionException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public TheiaSessionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public TheiaSessionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
