package com.paytm.pgplus.biz.exception;

import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;

public class AffordabilityCheckoutException extends RuntimeException {

    private String message;
    private ResponseConstants resultCode;

    public AffordabilityCheckoutException() {
        super();
    }

    public AffordabilityCheckoutException(String message) {
        super(message);
        this.message = message;
    }

    public AffordabilityCheckoutException(String message, ResponseConstants resultCode) {
        super(message);
        this.message = message;
        this.setResultCode(resultCode);
    }

    public AffordabilityCheckoutException(ResponseConstants resultCode) {
        super(resultCode.getMessage());
        this.message = resultCode.getMessage();
        this.setResultCode(resultCode);
    }

    public AffordabilityCheckoutException(Throwable cause) {
        super(cause);
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public ResponseConstants getResultCode() {
        return resultCode;
    }

    public void setResultCode(ResponseConstants resultCode) {
        this.resultCode = resultCode;
    }
}
