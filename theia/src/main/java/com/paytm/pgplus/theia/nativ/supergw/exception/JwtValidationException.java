package com.paytm.pgplus.theia.nativ.supergw.exception;

public class JwtValidationException extends RuntimeException {

    private static final long serialVersionUID = -1413749389322942042L;

    public JwtValidationException() {
    }

    public JwtValidationException(String message) {
        super(message);
    }
}
