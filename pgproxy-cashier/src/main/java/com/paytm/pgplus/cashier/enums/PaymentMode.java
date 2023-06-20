/*
 * @Developer AmitDubey
 * @Date 22-12-2917
 */

package com.paytm.pgplus.cashier.enums;

public enum PaymentMode {
    CC("CC"), DC("DC"), EMI("EMI"), NB("NB"), ATM("ATM"), IMPS("IMPS"), UPI("UPI"), PPI("PPI"), COD("COD"), RISK("RISK"), EMI_DC(
            "EMI_DC");

    private String mode;

    PaymentMode(String paymentMode) {
        this.mode = paymentMode;
    }

    public String getMode() {
        return mode;
    }
}
