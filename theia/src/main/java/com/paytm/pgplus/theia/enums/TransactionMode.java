/*
 * @Developer AmitDubey
 * @Date 22-12-2917
 */

package com.paytm.pgplus.theia.enums;

public enum TransactionMode {
    CC("CC"), DC("DC"), UPI("UPI"), SC("SC"), IMPS("IMPS"), NB("NB"), PPBL("PPBL");

    private String mode;

    TransactionMode(String transactionMode) {
        this.mode = transactionMode;
    }

    public String getMode() {
        return mode;
    }
}
