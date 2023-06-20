package com.paytm.pgplus.cashier.exception;

import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;

/**
 * @createdOn 25-Nov-2016
 * @author Santosh
 */
public class SaveCardValidationException extends PaytmValidationException {

    private static final long serialVersionUID = 4198091614354633382L;

    private String saveCardID;

    public SaveCardValidationException(String message, PaytmValidationExceptionType type, String CardId) {
        super(message, type);
        this.setSaveCardID(CardId);
    }

    public String getSaveCardID() {
        return saveCardID;
    }

    public void setSaveCardID(String saveCardID) {
        this.saveCardID = saveCardID;
    }

}
