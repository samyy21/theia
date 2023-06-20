package com.paytm.pgplus.cashier.validator.model;

import java.util.Map;

public class CacheCardBinInfo {

    Long bin;
    String category;
    String cardType;
    String cardName;
    String bankName;
    boolean isBlocked;

    public CacheCardBinInfo(Long bin) {

    }

    public CacheCardBinInfo(Map<String, Object> row) {
        this.bin = (Long) row.get("BIN_NUMBER");
        this.bankName = (null != row.get("BANK_NAME") ? row.get("BANK_NAME").toString() : "");
        this.category = (null != row.get("CATEGORY") ? row.get("CATEGORY").toString() : "");
        this.cardName = (null != row.get("CARD_NAME") ? row.get("CARD_NAME").toString() : "");
        this.cardType = (null != row.get("CARD_TYPE") ? row.get("CARD_TYPE").toString() : "");
        this.isBlocked = (row.get("IS_BLOCKED") != null ? Boolean.parseBoolean(row.get("IS_BLOCKED").toString())
                : false);
    }

    public CacheCardBinInfo(Long bin, String category, String cardType, String cardName, String bankName,
            boolean isBlocked) {
        super();
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

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public Long getBin() {
        return bin;
    }

    public void setBin(Long bin) {
        this.bin = bin;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean isBlocked) {
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
