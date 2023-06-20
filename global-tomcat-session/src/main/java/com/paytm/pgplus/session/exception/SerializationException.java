/**
 * 
 */
package com.paytm.pgplus.session.exception;

/**
 * @createdOn 20-Feb-2016
 * @author kesari
 */
public class SerializationException extends RuntimeException {

    /**
	 * 
	 */
    private static final long serialVersionUID = 5363308068203760066L;

    /**
	 * 
	 */
    public SerializationException() {
        super();
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public SerializationException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * @param message
     * @param cause
     */
    public SerializationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public SerializationException(final String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public SerializationException(final Throwable cause) {
        super(cause);
    }

}
