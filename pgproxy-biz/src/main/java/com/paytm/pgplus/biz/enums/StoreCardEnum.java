package com.paytm.pgplus.biz.enums;

/**
 * @author manojpal
 *
 */
public enum StoreCardEnum {

    ZERO("0"), ONE("1");

    public String value;

    StoreCardEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static StoreCardEnum getEnumByValue(String storeCard) {
        for (StoreCardEnum enumValue : values()) {
            if (enumValue.getValue().equals(storeCard)) {
                return enumValue;
            }
        }
        return null;
    }

}
