package com.paytm.pgplus.biz.exception;

public class InvalidParameterException extends BaseException {

    private static final long serialVersionUID = 419809161435462163L;

    public InvalidParameterException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public InvalidParameterException(String errorCode, String message) {
        this(errorCode, message, null);
    }

    public InvalidParameterException(String message) {
        this("1", message, null);
    }
}
