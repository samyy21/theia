package com.paytm.pgplus.theia.nativ.enums;

public enum AuthMode {

    PIN("pin"), OTP("otp");

    private String type;

    AuthMode(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
