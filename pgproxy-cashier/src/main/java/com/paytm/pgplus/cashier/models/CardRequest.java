/**
 * 
 */
package com.paytm.pgplus.cashier.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.paytm.pgplus.cashier.cachecard.model.*;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.exception.CashierInvalidParameterException;
import com.paytm.pgplus.cashier.validator.BeanParameterValidator;
import com.paytm.pgplus.pgproxycommon.models.SavedCardInfo;

/**
 * @author amit.dubey
 *
 */
public class CardRequest implements Serializable {

    /** Serial version UID */
    private static final long serialVersionUID = -4956399037948349291L;

    /** bin number for card */
    private String binNumber;

    /** saved card request */
    private SavedCardRequest savedCardRequest;

    /** Bank card request */
    private BankCardRequest bankCardRequest;

    /** IMPS card request */
    private IMPSCardRequest impsCardRequest;

    /** saved imps card request */
    private SavedImpsCardRequest savedImpsCardRequest;

    private boolean internationalCard;

    private VPACardRequest vpaCardRequest;

    private SavedVPACardRequest savedVpaCardRequest;

    private List<SavedCardInfo> merchantViewSavedCardsList = new ArrayList<>();

    public List<SavedCardInfo> getMerchantViewSavedCardsList() {
        return merchantViewSavedCardsList;
    }

    public void setMerchantViewSavedCardsList(List<SavedCardInfo> merchantViewSavedCardsList) {
        this.merchantViewSavedCardsList = merchantViewSavedCardsList;
    }

    /**
     * @param binNumber
     * @param bankCardRequest
     * @throws CashierInvalidParameterException
     */
    public CardRequest(String binNumber, BankCardRequest bankCardRequest) throws CashierInvalidParameterException {
        BeanParameterValidator.validateInputStringParam(binNumber, "biNumber");
        this.binNumber = binNumber;

        BeanParameterValidator.validateInputObjectParam(bankCardRequest, "bankCardRequest");
        this.bankCardRequest = bankCardRequest;
    }

    /**
     * @param impsCardRequest
     * @throws CashierCheckedException
     */
    public CardRequest(IMPSCardRequest impsCardRequest) throws CashierCheckedException {
        BeanParameterValidator.validateInputObjectParam(impsCardRequest, "impsCardRequest");
        this.impsCardRequest = impsCardRequest;
    }

    /**
     * @param savedCardId
     * @throws CashierCheckedException
     */
    public CardRequest(SavedCardRequest savedCardRequest) throws CashierCheckedException {
        BeanParameterValidator.validateInputObjectParam(savedCardRequest, "savedCardRequest");
        this.savedCardRequest = savedCardRequest;
    }

    /**
     * @param savedImpsCardRequest
     * @throws CashierCheckedException
     */
    public CardRequest(SavedImpsCardRequest savedImpsCardRequest) throws CashierCheckedException {
        BeanParameterValidator.validateInputObjectParam(savedImpsCardRequest, "savedImpsCardRequest");
        this.savedImpsCardRequest = savedImpsCardRequest;
    }

    public CardRequest(VPACardRequest vpaCardRequest) throws CashierCheckedException {
        BeanParameterValidator.validateInputObjectParam(vpaCardRequest, "vpaCardRequest");
        this.vpaCardRequest = vpaCardRequest;
    }

    public CardRequest(SavedVPACardRequest vpaCardRequest) throws CashierCheckedException {
        BeanParameterValidator.validateInputObjectParam(vpaCardRequest, "vpaCardRequest");
        this.savedVpaCardRequest = vpaCardRequest;
    }

    /**
     * @return the binNumber
     */
    public String getBinNumber() {
        return binNumber;
    }

    /**
     * @return the bankCardRequest
     */
    public BankCardRequest getBankCardRequest() {
        return bankCardRequest;
    }

    /**
     * @return the impsCardRequest
     */
    public IMPSCardRequest getImpsCardRequest() {
        return impsCardRequest;
    }

    /**
     * @return the savedCardRequest
     */
    public SavedCardRequest getSavedCardRequest() {
        return savedCardRequest;
    }

    /**
     * @return the savedImpsCardRequest
     */
    public SavedImpsCardRequest getSavedImpsCardRequest() {
        return savedImpsCardRequest;
    }

    public boolean isInternationalCard() {
        return internationalCard;
    }

    public void setInternationalCard(boolean internationalCard) {
        this.internationalCard = internationalCard;
    }

    public VPACardRequest getVpaCardRequest() {
        return vpaCardRequest;
    }

    public SavedVPACardRequest getSavedVpaCardRequest() {
        return savedVpaCardRequest;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CardRequest [binNumber=").append(binNumber).append(", savedCardRequest=")
                .append(savedCardRequest).append(", bankCardRequest=").append(bankCardRequest)
                .append(", impsCardRequest=").append(impsCardRequest).append(", savedImpsCardRequest=")
                .append(savedImpsCardRequest).append(", internationalCard=").append(internationalCard)
                .append(", vpaCardRequest").append(vpaCardRequest).append(", savedVpaCardRequest")
                .append(savedVpaCardRequest).append("]");
        return builder.toString();
    }

}