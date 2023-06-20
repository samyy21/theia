package com.paytm.pgplus.biz.exception;

/**
 * The Class ValidationException. This class is used to throw the exception in
 * the service layer
 */
public class InvalidFacadeResponseException extends BaseException {

    private static final long serialVersionUID = 419809161435462163L;

    public InvalidFacadeResponseException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public InvalidFacadeResponseException(String errorCode, String message) {
        this(errorCode, message, null);
    }

    public InvalidFacadeResponseException(String message) {
        this("1", message, null);
    }

}
