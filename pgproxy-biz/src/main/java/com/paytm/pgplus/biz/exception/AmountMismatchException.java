package com.paytm.pgplus.biz.exception;

public class AmountMismatchException extends RuntimeException {

    public AmountMismatchException(String message) {
        super(message);
    }
}
