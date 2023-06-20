package com.paytm.pgplus.biz.enums;

/**
 * @author manojpal
 *
 */
public enum VerifiedByEnum {

    EMAIL("EMAIL"), MOBILE_NO("MOBILE_NO");

    private String value;

    public String getValue() {
        return value;
    }

    VerifiedByEnum(String value) {
        this.value = value;
    }

    public static VerifiedByEnum getEnumByValue(String authMode) {
        for (VerifiedByEnum enumValue : values()) {
            if (enumValue.getValue().equalsIgnoreCase(authMode)) {
                return enumValue;
            }
        }
        return null;
    }

}
