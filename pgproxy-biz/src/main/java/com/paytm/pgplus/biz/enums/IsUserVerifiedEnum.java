package com.paytm.pgplus.biz.enums;

/**
 * @author manojpal
 *
 */
public enum IsUserVerifiedEnum {

    YES("YES"), NO("NO");

    private String value;

    public String getValue() {
        return value;
    }

    IsUserVerifiedEnum(String value) {
        this.value = value;
    }

    public static IsUserVerifiedEnum getEnumByValue(String authMode) {
        for (IsUserVerifiedEnum enumValue : values()) {
            if (enumValue.getValue().equalsIgnoreCase(authMode)) {
                return enumValue;
            }
        }
        return null;
    }

}
