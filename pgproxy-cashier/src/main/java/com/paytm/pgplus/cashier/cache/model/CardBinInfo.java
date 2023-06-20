/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.cashier.cache.model;

import java.io.Serializable;

/**
 * @author Lalit Mehra
 * @since March 30, 2016
 *
 */
public class CardBinInfo implements Serializable {

    private static final long serialVersionUID = 1539525705855599936L;

    // private Long id;
    private String bin;
    private String category;
    private String cardType;
    private String cardName;
    private String bankName;
    boolean isBlocked;

    public CardBinInfo(final String bin) {
        this.bin = bin;
    }

    public CardBinInfo(final Long id, final String bin, final String category, final String cardType,
            final String cardName, final String bankName, final boolean isBlocked) {
        super();
        // this.id = id;
        this.bin = bin;
        this.category = category;
        this.cardType = cardType;
        this.cardName = cardName;
        this.bankName = bankName;
        this.isBlocked = isBlocked;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(final String cardType) {
        this.cardType = cardType;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(final String cardName) {
        this.cardName = cardName;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(final String bankName) {
        this.bankName = bankName;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(final String bin) {
        this.bin = bin;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(final boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    public String getKey() {
        return String.valueOf(bin);
    }

    @Override
    public String toString() {
        return "CacheCardBinInfoOutTO [bin=" + bin + ", category=" + category + ", cardType=" + cardType
                + ", cardName=" + cardName + ", bankName=" + bankName + ",isBlocked" + isBlocked + "]";
    }
}
