package com.paytm.pgplus.cashier.exception;

import javax.ws.rs.core.MultivaluedMap;

import com.paytm.pgplus.pgproxycommon.utils.ValidationMessage;

public class ValidationException extends Exception {

    private static final long serialVersionUID = 8208727905017613431L;

    private MultivaluedMap<ValidationMessage, String> errors;

    public ValidationException(MultivaluedMap<ValidationMessage, String> errors) {
        super();
        this.errors = errors;
    }

    public MultivaluedMap<ValidationMessage, String> getErrors() {
        return errors;
    }

    public ValidationException(MultivaluedMap<ValidationMessage, String> errors, String message, Throwable cause) {
        super(message, cause);
        this.errors = errors;
    }

    public ValidationException(MultivaluedMap<ValidationMessage, String> errors, String message) {
        super(message);
        this.errors = errors;
    }

}
