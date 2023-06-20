/**
 * 
 */
package com.paytm.pgplus.theia.httpexceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @createdOn 21-Mar-2016
 * @author kesari
 */
@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
public class MethodNotAllowedException extends RuntimeException {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1086316178014895913L;

    /**
	 * 
	 */
    public MethodNotAllowedException() {
        super();
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public MethodNotAllowedException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * @param message
     * @param cause
     */
    public MethodNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public MethodNotAllowedException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public MethodNotAllowedException(Throwable cause) {
        super(cause);
    }

}
