package com.paytm.pgplus.theia.exceptions;

import com.paytm.pgplus.dynamicwrapper.exceptions.WrapperServiceException;

public class CustomInitTxnException extends RuntimeException {

    public CustomInitTxnException(WrapperServiceException e) {
        super(e);
    }

    public CustomInitTxnException(Exception e) {
        super(e);
    }
}
