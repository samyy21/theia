package com.paytm.pgplus.biz.exception;

public class AccountNotExistsException extends RuntimeException {

    public AccountNotExistsException(String message) {
        super(message);
    }
}
