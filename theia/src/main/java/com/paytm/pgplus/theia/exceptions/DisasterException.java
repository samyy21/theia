package com.paytm.pgplus.theia.exceptions;

/**
 * @author Naman Jain on 05/06/18 This exception is intended to be used on ultra
 *         critical API hits, on which data is Mandatory and if doesn't come,
 *         can lead to bussiness loss.
 */
public class DisasterException extends RuntimeException {

    private static final long serialVersionUID = -6402700046545681931L;

    /**
     * @param message
     */
    public DisasterException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public DisasterException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public DisasterException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public DisasterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
