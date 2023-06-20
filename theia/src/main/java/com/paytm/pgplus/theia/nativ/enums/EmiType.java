package com.paytm.pgplus.theia.nativ.enums;

/**
 * Created by charu on 02/10/18.
 */

public enum EmiType {

    DEBIT_CARD("DEBIT_CARD"), CREDIT_CARD("CREDIT_CARD"), NBFC("NBFC");

    String type;

    EmiType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
