package com.paytm.pgplus.biz.exception;

public class AccountMismatchException extends RuntimeException {

    public AccountMismatchException(String message) {
        super(message);
    }
}
