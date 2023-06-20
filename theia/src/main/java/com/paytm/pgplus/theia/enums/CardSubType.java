package com.paytm.pgplus.theia.enums;

public enum CardSubType {
    CORPORATE_CARD("CORPORATE_CARD");

    private String cardSubType;

    CardSubType(String cardSubType) {
        this.cardSubType = cardSubType;
    }

    public String getCardSubType() {
        return cardSubType;
    }

    public void setCardSubType(String cardSubType) {
        this.cardSubType = cardSubType;
    }

    public static CardSubType getCardSubTypeByName(String cardSubType) {
        for (CardSubType x : values()) {
            if (x.getCardSubType().equalsIgnoreCase(cardSubType)) {
                return x;
            }
        }
        return null;
    }

}
