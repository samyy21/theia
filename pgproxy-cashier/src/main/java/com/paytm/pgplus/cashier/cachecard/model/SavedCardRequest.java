/**
 * 
 */
package com.paytm.pgplus.cashier.cachecard.model;

import java.io.Serializable;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.validator.BeanParameterValidator;

/**
 * @author amit.dubey
 *
 */
public class SavedCardRequest implements Serializable {

    /** Serial Version UID */
    private static final long serialVersionUID = 6790521665775424470L;

    /** savedCardId */
    private String savedCardId;

    /** CVV number */
    private String cvv;

    private String cardType;

    private String cardScheme;

    public String getCardType() {
        return cardType;
    }

    public String getCardScheme() {
        return cardScheme;
    }

    /**
     * @param savedCardId
     * @param cvv
     * @param cardType
     * @param cardScheme
     * @throws CashierCheckedException
     */
    public SavedCardRequest(String savedCardId, String cvv, String cardType, String cardScheme)
            throws CashierCheckedException {
        BeanParameterValidator.validateInputStringParam(savedCardId, "savedCardId");
        this.savedCardId = savedCardId;

        this.cvv = cvv;

        this.cardType = cardType;
        this.cardScheme = cardScheme;
    }

    /**
     * @param savedCardId
     * @param cvv
     * @throws CashierCheckedException
     */
    public SavedCardRequest(String savedCardId, String cvv) throws CashierCheckedException {
        BeanParameterValidator.validateInputStringParam(savedCardId, "savedCardId");
        this.savedCardId = savedCardId;

        this.cvv = cvv;
    }

    /**
     * @return the savedCardId
     */
    public String getSavedCardId() {
        return savedCardId;
    }

    /**
     * @return the cvv
     */
    public String getCvv() {
        return cvv;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SavedCardRequest [savedCardId=");
        builder.append(savedCardId);
        builder.append(", cvv=");
        builder.append(cvv);
        builder.append(", cardType=");
        builder.append(cardType);
        builder.append(", cardScheme=");
        builder.append(cardScheme);
        builder.append("]");
        return builder.toString();
    }

}
