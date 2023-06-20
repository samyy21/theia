/**
 * 
 */
package com.paytm.pgplus.theia.exceptions;

import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @createdOn 27-Mar-2016
 * @author kesari
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class TheiaDataMappingException extends Exception {

    private static final long serialVersionUID = -3426309006160352250L;
    private ResponseConstants responseConstant;

    /**
     * @param responseConstants
     */
    public TheiaDataMappingException(ResponseConstants responseConstants) {
        super();
        this.responseConstant = responseConstants;
    }

    public TheiaDataMappingException() {
        super();
    }

    public TheiaDataMappingException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public TheiaDataMappingException(String message, Throwable cause, ResponseConstants responseConstants) {
        super(message, cause);
        this.responseConstant = responseConstants;
    }

    public TheiaDataMappingException(String message, ResponseConstants responseConstant) {
        super(message);
        this.responseConstant = responseConstant;
    }

    public TheiaDataMappingException(Throwable cause, ResponseConstants responseConstant) {
        super(cause);
        this.responseConstant = responseConstant;
    }

    /**
     * @return the responseConstant
     */
    public ResponseConstants getResponseConstant() {
        return responseConstant;
    }

}
