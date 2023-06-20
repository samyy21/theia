/**
 * 
 */
package com.paytm.pgplus.theia.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @createdOn 27-Mar-2016
 * @author kesari
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class TheiaServiceException extends RuntimeException {

    private static final long serialVersionUID = -3426309006160352250L;

    public TheiaServiceException() {
        super();
    }

    public TheiaServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public TheiaServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public TheiaServiceException(String message) {
        super(message);
    }

    public TheiaServiceException(Throwable cause) {
        super(cause);
    }

}
