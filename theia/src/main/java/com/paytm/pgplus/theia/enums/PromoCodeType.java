package com.paytm.pgplus.theia.enums;

public enum PromoCodeType {

    CASHBACK("CASHBACK"), DISCOUNT("DISCOUNT");

    private String value;

    private PromoCodeType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
