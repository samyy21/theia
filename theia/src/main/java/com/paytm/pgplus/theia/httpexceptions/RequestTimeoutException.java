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
@ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
public class RequestTimeoutException extends RuntimeException {

    /**
	 * 
	 */
    private static final long serialVersionUID = -6768492122172706926L;

    /**
	 * 
	 */
    public RequestTimeoutException() {
        super();
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public RequestTimeoutException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * @param message
     * @param cause
     */
    public RequestTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public RequestTimeoutException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public RequestTimeoutException(Throwable cause) {
        super(cause);
    }

}
