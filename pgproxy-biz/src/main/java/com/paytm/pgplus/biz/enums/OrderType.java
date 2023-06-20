package com.paytm.pgplus.biz.enums;

public enum OrderType {

    BRAND_EMI_ORDER("BRAND_EMI_ORDER"), BANK_EMI_ORDER("BANK_EMI_ORDER"), RETAIL_EMI_ORDER("RETAIL_EMI_ORDER");

    private String value;

    OrderType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
