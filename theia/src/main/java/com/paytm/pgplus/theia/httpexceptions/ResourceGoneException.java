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
@ResponseStatus(HttpStatus.GONE)
public class ResourceGoneException extends RuntimeException {

    /**
	 * 
	 */
    private static final long serialVersionUID = 995761252819621274L;

    /**
	 * 
	 */
    public ResourceGoneException() {
        super();
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public ResourceGoneException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * @param message
     * @param cause
     */
    public ResourceGoneException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public ResourceGoneException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ResourceGoneException(Throwable cause) {
        super(cause);
    }

}
