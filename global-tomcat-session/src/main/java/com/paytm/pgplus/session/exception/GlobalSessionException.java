/**
 * 
 */
package com.paytm.pgplus.session.exception;

/**
 * @createdOn 12-Mar-2016
 * @author kesari
 */
public class GlobalSessionException extends Exception {

    /**
     * Serial version uid
     */
    private static final long serialVersionUID = 3505789329960598398L;

    /**
	 * 
	 */
    public GlobalSessionException() {
        super();
    }

    /**
     * @param message
     */
    public GlobalSessionException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public GlobalSessionException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public GlobalSessionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public GlobalSessionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
