package com.paytm.pgplus.theia.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class CoreSessionExpiredException extends RuntimeException {

    private static final long serialVersionUID = -6557060300144039876L;

    public CoreSessionExpiredException() {
        super();
    }

    public CoreSessionExpiredException(String message) {
        super(message);
    }

}
