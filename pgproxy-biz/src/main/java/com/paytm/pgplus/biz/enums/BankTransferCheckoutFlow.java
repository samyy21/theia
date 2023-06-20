package com.paytm.pgplus.biz.enums;

public enum BankTransferCheckoutFlow {
    MERCHANT_CONTROLLED("MERCHANT_CONTROLLED"), PAYTM_CONTROLLED("PAYTM_CONTROLLED"), DISABLED("DISABLED");

    private String value;

    BankTransferCheckoutFlow(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
