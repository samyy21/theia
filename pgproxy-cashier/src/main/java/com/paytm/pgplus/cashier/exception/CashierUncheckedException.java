/**
 * 
 */
package com.paytm.pgplus.cashier.exception;

/**
 * @author amit.dubey
 *
 */
public class CashierUncheckedException extends RuntimeException {

    /**
	 * 
	 */
    private static final long serialVersionUID = 5062907084905226480L;

    /**
	 * 
	 */
    public CashierUncheckedException() {
        super();
    }

    /**
     * @param message
     */
    public CashierUncheckedException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public CashierUncheckedException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public CashierUncheckedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public CashierUncheckedException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
