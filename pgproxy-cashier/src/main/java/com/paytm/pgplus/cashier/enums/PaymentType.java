/**
 * 
 */
package com.paytm.pgplus.cashier.enums;

/**
 * @author amit.dubey
 *
 */
public enum PaymentType {
    ONLY_PG("ONLY_PG", "onlyPg"), ONLY_WALLET("ONLY_WALLET", "walletOnly"), HYBRID("HYBRID", "Hybrid"), ADDNPAY(
            "ADDNPAY", "AddAndPay"), ONLY_COD("ONLY_COD", "cod"), HYBRID_COD("HYBRID_COD", "Hybrid"), OTHER("OTHER",
            "other");

    private String value;
    private String alternate;

    private PaymentType(String value, String alternate) {
        this.value = value;
        this.alternate = alternate;
    }

    public String getValue() {
        return value;
    }

    public String getAlternate() {
        return alternate;
    }
}
