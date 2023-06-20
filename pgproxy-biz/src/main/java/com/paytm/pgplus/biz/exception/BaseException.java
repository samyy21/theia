package com.paytm.pgplus.biz.exception;

public class BaseException extends Exception {
    /**
   * 
   */
    private static final long serialVersionUID = -6368709531967840935L;
    private String errorCode;
    private String errorMessage;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Instantiates a new ValidationException.
     * 
     * @param errorCode
     *            the error code
     * @param message
     *            the message
     */
    public BaseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
        this.errorMessage = message;
    }

    public BaseException(String errorCode, String message) {
        this(errorCode, message, null);
    }

    public BaseException(String message) {
        super(message);
        this.errorMessage = message;

    }

    public BaseException(Throwable cause) {
        super(cause);
    }
}
