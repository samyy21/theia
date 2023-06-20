package com.paytm.pgplus.biz.enums;

/**
 * @author manojpal
 *
 */
public enum PaymentTypeIdEnum {

    CC("CC"), DC("DC"), PPI("PPI"), NB("NB"), Telco("Telco"), COD("COD"), IMPS("IMPS"), UPI("UPI"), PAYTM_DIGITAL_CREDIT(
            "PAYTM_DIGITAL_CREDIT"), EMI("EMI"), WALLET("WALLET"), LOYALTY_POINT("LOYALTY_POINT"), ADVANCE_DEPOSIT_ACCOUNT(
            "ADVANCE_DEPOSIT_ACCOUNT"), BANK_MANDATE("BANK_MANDATE"), GIFT_VOUCHER("GIFT_VOUCHER"), BANK_TRANSFER(
            "BANK_TRANSFER"), UPI_LITE("UPI_LITE");

    public String value;

    public String getValue() {
        return value;
    }

    PaymentTypeIdEnum(String value) {
        this.value = value;
    }

    public static PaymentTypeIdEnum getEnumByValue(String value) {
        for (PaymentTypeIdEnum enumValue : values()) {
            if (enumValue.getValue().equalsIgnoreCase(value)) {
                return enumValue;
            }
        }
        return null;
    }
}
