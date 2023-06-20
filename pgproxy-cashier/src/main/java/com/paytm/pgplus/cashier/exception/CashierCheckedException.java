/**
 * 
 */
package com.paytm.pgplus.cashier.exception;

/**
 * @author amit.dubey
 *
 */
public class CashierCheckedException extends Exception {

    private static final long serialVersionUID = -371456915826492352L;

    /**
     * @param message
     */
    public CashierCheckedException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public CashierCheckedException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public CashierCheckedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public CashierCheckedException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
