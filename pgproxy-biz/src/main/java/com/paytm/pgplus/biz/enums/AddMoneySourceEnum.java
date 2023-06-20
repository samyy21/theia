package com.paytm.pgplus.biz.enums;

public enum AddMoneySourceEnum {
    PAYTM_WEBSITE("paytm-web"), PAYTM_APP("paytm-app"), THIRD_PARTY("3pm");

    private String value;

    public String getValue() {
        return value;
    }

    private AddMoneySourceEnum(String value) {
        this.value = value;
    }
}
