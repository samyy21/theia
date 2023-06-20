package com.paytm.pgplus.theia.test.enums;

/**
 * @author kartik
 * @date 26-05-2017
 */
public enum ETestResponseValidation {
    BLANK_MERCHANT_RESPONSE("BLANK_MERCHANT_RESPONSE"), SUCCESSFULLY_PROCESSED("SUCCESSFULLY_PROCESSED"), LOGIN_FLAG_ENABLED(
            "LOGIN_FLAG_ENABLED"), SAVED_CARD_ENABLED("SAVED_CARD_ENABLED"), ADDNPAY_SAVED_CARD_ENABLED(
            "ADDNPAY_SAVED_CARD_ENABLED"), REDIRECT_PAGE_IN_SESSION("REDIRECT_PAGE_IN_SESSION"), DIGITAL_CREDIT_ENABLED(
            "DIGITAL_CREDIT_ENABLED"), PROMO_CODE_ENABLED("PROMO_CODE_ENABLED");

    private String name;

    private ETestResponseValidation(String name) {
        this.name = name;
    }

    public static ETestResponseValidation getResponseValidationByName(String name) {
        for (ETestResponseValidation e : ETestResponseValidation.values()) {
            if (e.getName().equals(name)) {
                return e;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
}
