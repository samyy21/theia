package com.paytm.pgplus.theia.csrf;

/**
 * Created by ankitsinghal on 18/05/17. Updated By Amit Dubey.
 */
public class CSRFInvalidException extends RuntimeException {

    private static final long serialVersionUID = 7601152027226097420L;

    public CSRFInvalidException(CSRFToken tokenInRequest, CSRFToken tokenInSession) {
        super("Invalid CSRF Token detected. Token in request:" + tokenInRequest + ", token in session:"
                + tokenInSession);
    }

    public CSRFInvalidException(String cause) {
        super(cause);
    }

}
