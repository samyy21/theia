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
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenRequestException extends RuntimeException {

    /**
	 * 
	 */
    private static final long serialVersionUID = -671226596939476462L;

    /**
	 * 
	 */
    public ForbiddenRequestException() {
        super();
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public ForbiddenRequestException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * @param message
     * @param cause
     */
    public ForbiddenRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public ForbiddenRequestException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ForbiddenRequestException(Throwable cause) {
        super(cause);
    }

}
