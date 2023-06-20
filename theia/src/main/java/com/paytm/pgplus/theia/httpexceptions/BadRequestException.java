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
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    /**
	 * 
	 */
    private static final long serialVersionUID = 9217803513190327547L;

    /**
	 * 
	 */
    public BadRequestException() {
        super();
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public BadRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * @param message
     * @param cause
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public BadRequestException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public BadRequestException(Throwable cause) {
        super(cause);
    }

}
