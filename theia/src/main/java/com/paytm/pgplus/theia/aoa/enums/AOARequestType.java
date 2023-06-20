package com.paytm.pgplus.theia.aoa.enums;

import org.apache.commons.lang3.StringUtils;

public enum AOARequestType {

    NATIVE_SUBSCRIPTION("NATIVE_SUBSCRIPTION", "RecurringAcquiringProd"), UNI_PAY("UNI_PAY",
            "AOADirectPayAcquiringProd");

    String type;

    String productCode;

    AOARequestType(final String type, final String productCode) {
        this.type = type;
        this.productCode = productCode;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the productCode
     */
    public String getProductCode() {
        return productCode;
    }

    public static AOARequestType getByProductCode(final String productCode) {
        for (final AOARequestType e : values()) {
            if (e.productCode.equals(productCode)) {
                return e;
            }
        }
        return null;
    }

    public static AOARequestType getByRequestType(final String reqType) {
        for (final AOARequestType e : values()) {
            if (e.type.equals(reqType)) {
                return e;
            }
        }
        return null;
    }

    public static Boolean isValidRequestType(final String reqType) {
        if (StringUtils.isBlank(reqType)) {
            return false;
        }
        for (final AOARequestType e : values()) {
            if (e.type.equals(reqType)) {
                return true;
            }
        }
        return false;
    }
}
