package com.paytm.pgplus.biz.enums;

public enum SettlementType {

    DIRECT_SETTLEMENT("DIRECT_SETTLEMENT"), DEFERRED_SETTLEMENT("DEFERRED_SETTLEMENT");

    private String value;

    SettlementType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "SettleType{" + "value='" + value + '\'' + '}';
    }
};
