package com.paytm.pgplus.biz.enums;

public enum LitePayviewConsultType {

    AddnPayLitePayViewConsult("AddnPayLitePayViewConsult"), MerchantLitePayViewConsult("MerchantLitePayViewConsult");

    private String value;

    LitePayviewConsultType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
