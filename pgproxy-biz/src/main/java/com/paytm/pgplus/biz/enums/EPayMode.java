package com.paytm.pgplus.biz.enums;

public enum EPayMode {

    NONE("NONE"), ADDANDPAY("ADDANDPAY"), HYBRID("HYBRID"), ADDANDPAY_KYC("KYC"), NONE_KYC("NONE_KYC"), LIMIT_REJECT(
            "LIMIT_REJECT"), ADDANDPAY_GV("GV");

    private String value;

    EPayMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
