package com.paytm.pgplus.biz.workflow.service.factory;

public enum PayAndCopImplEnum {

    EDL_LINK_PAYMENT_PAY("EDL_LINK_PAYMENT_PAY"), EDL_LINK_PAYMENT_COP("EDL_LINK_PAYMENT_COP"), OLD_GENERIC_PAY(
            "OLD_GENERIC_PAY"), OLD_GENERIC_COP("OLD_GENERIC_COP");

    private String value;

    PayAndCopImplEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
