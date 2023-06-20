/**
 * 
 */
package com.paytm.pgplus.cashier.exception;

/**
 * @author amit.dubey
 *
 */
public class CashierInvalidSignatureException extends CashierUncheckedException {

    /**
	 * 
	 */
    private static final long serialVersionUID = 5930975914120020095L;

    /**
	 * 
	 */
    public CashierInvalidSignatureException() {
        super();
    }

    /**
     * @param message
     */
    public CashierInvalidSignatureException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public CashierInvalidSignatureException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public CashierInvalidSignatureException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public CashierInvalidSignatureException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
