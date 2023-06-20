package com.paytm.pgplus.biz.enums;

/**
 * @author manojpal
 *
 */
public enum AuthModeEnum {

    USRPWD("USRPWD"), THREE_D("3D");

    private String value;

    AuthModeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AuthModeEnum getEnumByValue(String authMode) {
        for (AuthModeEnum enumValue : values()) {
            if (enumValue.getValue().equalsIgnoreCase(authMode)) {
                return enumValue;
            }
        }
        return null;
    }
}
