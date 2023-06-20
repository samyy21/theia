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
@ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
public class UnsupportedMediaTypeException extends RuntimeException {

    /**
	 * 
	 */
    private static final long serialVersionUID = 5673081120022586054L;

    /**
	 * 
	 */
    public UnsupportedMediaTypeException() {
        super();
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public UnsupportedMediaTypeException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * @param message
     * @param cause
     */
    public UnsupportedMediaTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public UnsupportedMediaTypeException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public UnsupportedMediaTypeException(Throwable cause) {
        super(cause);
    }

}
