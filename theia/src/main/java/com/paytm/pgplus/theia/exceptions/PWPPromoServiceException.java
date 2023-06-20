package com.paytm.pgplus.theia.exceptions;

import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;

public class PWPPromoServiceException extends RuntimeException {
    private static final long serialVersionUID = -1848653375161427782L;
    private ResponseConstants responseConstants;

    public PWPPromoServiceException(ResponseConstants responseConstants) {
        super(responseConstants.getMessage());
        this.responseConstants = responseConstants;
    }

    public PWPPromoServiceException() {
        super(ResponseConstants.SYSTEM_ERROR.getMessage());
        this.responseConstants = ResponseConstants.SYSTEM_ERROR;
    }

    public PWPPromoServiceException(Throwable throwable, ResponseConstants responseConstants) {
        super(throwable);
        this.responseConstants = responseConstants;
    }

    public ResponseConstants getResponseConstants() {
        return responseConstants;
    }

}
