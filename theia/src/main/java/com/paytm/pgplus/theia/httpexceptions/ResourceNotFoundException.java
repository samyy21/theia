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
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1486936026154229077L;

    /**
	 * 
	 */
    public ResourceNotFoundException() {
        super();
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public ResourceNotFoundException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * @param message
     * @param cause
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ResourceNotFoundException(Throwable cause) {
        super(cause);
    }

}
