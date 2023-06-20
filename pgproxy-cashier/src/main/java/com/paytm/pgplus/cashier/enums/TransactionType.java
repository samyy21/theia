package com.paytm.pgplus.cashier.enums;

/**
 * Created by prashant on 9/3/16.
 */
public enum TransactionType {
    REFUND("REFUND"), CANCEL("CANCEL");

    private String value;

    /**
     * @param value
     */
    TransactionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
