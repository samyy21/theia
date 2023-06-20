package com.paytm.pgplus.biz.enums;

public enum EFundType {

    SEND_MONEY("SEND_MONEY"), SEND_MONEY_FOR_NOT_ACCOUNT("SEND_MONEY_FOR_NOT_ACCOUNT"), REQUEST_MONEY("REQUEST_MONEY"), REQUEST_MONEY_FOR_NOT_ACCOUNT(
            "REQUEST_MONEY_FOR_NOT_ACCOUNT"), FACE_TO_FACE_SEND_MONEY("FACE_TO_FACE_SEND_MONEY"), SNS_SEND_MONEY(
            "SNS_SEND_MONEY"), TOPUP("TOPUP"), WITHDRAW("WITHDRAW"), MERCHANT_INNER_TRANSFER("MERCHANT_INNER_TRANSFER"), AGENT_TOPUP_FOR_USER_SETTLE(
            "AGENT_TOPUP_FOR_USER_SETTLE"), AGENT_TOPUP_FOR_USER_CLEARING("AGENT_TOPUP_FOR_USER_CLEARING"), AGENT_TOPUP_FOR_MERCHANT(
            "AGENT_TOPUP_FOR_MERCHANT"), TOPUP_FROM_MERCHANT("TOPUP_FROM_MERCHANT"), TOPUP_MULTIPAY_MODE(
            "TOPUP_MULTIPAY_MODE"), ;

    String value = "";

    EFundType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}