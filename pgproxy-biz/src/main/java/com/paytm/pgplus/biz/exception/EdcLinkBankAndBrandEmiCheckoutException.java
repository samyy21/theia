package com.paytm.pgplus.biz.exception;

import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;

public class EdcLinkBankAndBrandEmiCheckoutException extends RuntimeException {

    private String message;
    private ResponseConstants resultCode;

    public EdcLinkBankAndBrandEmiCheckoutException() {
        super();
    }

    public EdcLinkBankAndBrandEmiCheckoutException(String message, ResponseConstants resultCode) {
        super(message);
        this.message = message;
        this.setResultCode(resultCode);
    }

    public EdcLinkBankAndBrandEmiCheckoutException(ResponseConstants resultCode) {
        super(resultCode.getMessage());
        this.message = resultCode.getMessage();
        this.setResultCode(resultCode);
    }

    public EdcLinkBankAndBrandEmiCheckoutException(Throwable cause) {
        super(cause);
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public ResponseConstants getResultCode() {
        return resultCode;
    }

    public void setResultCode(ResponseConstants resultCode) {
        this.resultCode = resultCode;
    }
}
