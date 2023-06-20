package com.paytm.pgplus.theia.paymentoffer.enums;

import java.util.Optional;

public enum RedemptionType {
    CASHBACK("cashback"), DISCOUNT("discount"), PAYTM_CASHBACK("paytm_cashback");

    private String type;

    RedemptionType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static Optional<RedemptionType> fromString(String s) {
        for (RedemptionType val : values()) {
            if (val.getType().equals(s)) {
                return Optional.ofNullable(val);
            }
        }
        return Optional.empty();
    }
}
