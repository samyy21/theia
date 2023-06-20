package com.paytm.pgplus.biz.enums;

public enum DirectPaymentVerificationMethod {

    ATM("ATM"), OTP("OTP");

    String value;

    private DirectPaymentVerificationMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
