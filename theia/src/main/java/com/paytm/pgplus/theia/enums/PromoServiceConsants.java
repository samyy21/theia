package com.paytm.pgplus.theia.enums;

public enum PromoServiceConsants {

    PROMO_MSG_MAX_COUNT_EXCEEDED("promo.max.card.count.crossed"), PROMO_MSG_OLD_CARD_TRANSACTED("promo.old.card.txn"), PROMO_MSG_MAX_AMOUNT_EXCEEDED(
            "promo.max.card.amount.crossed"), PROMO_MSG_OLD_CUST_TRANSACTED("promo.old.cust.transacted"), PROMO_ERROR_MSG_DEFAULT(
            "Invalid promocode details"), PROMO_CHECK_VALIDITY_PATH("/checkPromoValidity");

    private String value;

    private PromoServiceConsants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
