/**
 * 
 */
package com.paytm.pgplus.cashier.exception;

/**
 * @author amit.dubey
 *
 */
public class CashierInvalidParameterException extends CashierCheckedException {

    private static final long serialVersionUID = -3714349158265926392L;

    /**
     * @param message
     */
    public CashierInvalidParameterException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public CashierInvalidParameterException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public CashierInvalidParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public CashierInvalidParameterException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
