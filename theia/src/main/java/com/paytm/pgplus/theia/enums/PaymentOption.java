package com.paytm.pgplus.theia.enums;

public enum PaymentOption {
    UPI("UPI"), UPI_INTENT("UPI_PUSH"), UPI_PUSH("UPI_PUSH_EXPRESS"), UPI_LITE("UPI_LITE");
    private String paymentOption;

    PaymentOption(String paymentOption) {
        this.paymentOption = paymentOption;
    }

    public String getPaymentOption() {
        return paymentOption;
    }
}
