/**
 * 
 */
package com.paytm.pgplus.cashier.pay.service.model;

import java.io.Serializable;

/**
 * @author amit.dubey
 *
 */
public class CashierUserCard implements Serializable {
    /**
     * serial version id
     */
    private static final long serialVersionUID = 4890924807167928779L;

    private String cardNumber;
    private String cardTypeVal;
    private String expiryDate;
    private String firstSixDigit;
    private String lastFourDigit;
    private String statusVal;
    private String userId;

    /**
     * @return the cardNumber
     */
    public String getCardNumber() {
        return cardNumber;
    }

    /**
     * @param cardNumber
     *            the cardNumber to set
     */
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    /**
     * @return the cardTypeVal
     */
    public String getCardTypeVal() {
        return cardTypeVal;
    }

    /**
     * @param cardTypeVal
     *            the cardTypeVal to set
     */
    public void setCardTypeVal(String cardTypeVal) {
        this.cardTypeVal = cardTypeVal;
    }

    /**
     * @return the expiryDate
     */
    public String getExpiryDate() {
        return expiryDate;
    }

    /**
     * @param expiryDate
     *            the expiryDate to set
     */
    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
     * @return the firstSixDigit
     */
    public String getFirstSixDigit() {
        return firstSixDigit;
    }

    /**
     * @param firstSixDigit
     *            the firstSixDigit to set
     */
    public void setFirstSixDigit(String firstSixDigit) {
        this.firstSixDigit = firstSixDigit;
    }

    /**
     * @return the lastFourDigit
     */
    public String getLastFourDigit() {
        return lastFourDigit;
    }

    /**
     * @param lastFourDigit
     *            the lastFourDigit to set
     */
    public void setLastFourDigit(String lastFourDigit) {
        this.lastFourDigit = lastFourDigit;
    }

    /**
     * @return the statusVal
     */
    public String getStatusVal() {
        return statusVal;
    }

    /**
     * @param statusVal
     *            the statusVal to set
     */
    public void setStatusVal(String statusVal) {
        this.statusVal = statusVal;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId
     *            the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

}
