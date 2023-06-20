package com.paytm.pgplus.theia.offline.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by rahulverma on 1/9/17.
 */
public enum TokenType {
    SSO("SSO"), TXN_TOKEN("TXN_TOKEN"), CHECKSUM("CHECKSUM"), JWT("JWT"), GUEST("GUEST"), ACCESS("ACCESS");

    private String type;

    TokenType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static TokenType getType(String type) {
        TokenType[] var1 = values();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            TokenType tokenType = var1[var3];
            if (StringUtils.isNotBlank(type) && type.equals(tokenType.getType())) {
                return tokenType;
            }
        }

        return null;
    }
}
