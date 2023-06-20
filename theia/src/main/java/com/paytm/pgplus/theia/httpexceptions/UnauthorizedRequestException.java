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
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedRequestException extends RuntimeException {

    /**
	 * 
	 */
    private static final long serialVersionUID = 7441786919752186408L;

    /**
	 * 
	 */
    public UnauthorizedRequestException() {
        super();
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public UnauthorizedRequestException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * @param message
     * @param cause
     */
    public UnauthorizedRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public UnauthorizedRequestException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public UnauthorizedRequestException(Throwable cause) {
        super(cause);
    }

}
