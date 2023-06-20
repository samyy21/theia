package com.paytm.pgplus.theia.nativ.supergw.enums;

public enum PaymentType {

    RECURRING("RECURRING"), OTM("OTM"), OTHERS("OTHERS");

    private String value;

    PaymentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
