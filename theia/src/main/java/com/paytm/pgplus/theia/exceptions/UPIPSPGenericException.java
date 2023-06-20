package com.paytm.pgplus.theia.exceptions;

/**
 * @author Santosh chourasia
 *
 */
public class UPIPSPGenericException extends Exception {

    /**
	 * 
	 */
    private static final long serialVersionUID = 413706320986566025L;

    public UPIPSPGenericException() {
        super();
    }

    public UPIPSPGenericException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public UPIPSPGenericException(String message, Throwable cause) {
        super(message, cause);
    }

    public UPIPSPGenericException(String message) {
        super(message);
    }

    public UPIPSPGenericException(Throwable cause) {
        super(cause);
    }

}
