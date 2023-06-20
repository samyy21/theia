/*
 * @Developer AmitDubey
 * @Date 27-Dec-17
 */

package com.paytm.pgplus.theia.enums;

public enum SavedCardType {
    CC("CREDIT_CARD"), DC("DEBIT_CARD"), UPI("UPI"), IMPS("IMPS");

    private String cardType;

    SavedCardType(String savedCardType) {
        this.cardType = savedCardType;
    }

    public String getCardType() {
        return cardType;
    }
}
