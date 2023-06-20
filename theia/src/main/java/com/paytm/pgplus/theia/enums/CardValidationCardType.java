package com.paytm.pgplus.theia.enums;

public enum CardValidationCardType {
    EXPIRED("EXPIRED"), RUPAY("RUPAY");
    private String cardType;

    private CardValidationCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCardType() {
        return cardType;
    }
}
