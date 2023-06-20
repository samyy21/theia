package com.paytm.pgplus.theia.enums;

public enum PaymentRequestParam {

    TXN_AMOUNT("TXN_AMOUNT"), AUTH_MODE("AUTH_MODE"), CARD_TYPE("CARD_TYPE"), BANK_CODE("bankCode"), EMI_BANK_CODE(
            "emiBankName"), EMI_PLAN_ID("emiPlanId"), MERCHANT_CALLBACK_URL("CALLBACK_URL"), TXN_MODE("txnMode"), TXN_MDE(
            "txnMde"), CARD_NUMBER("cardNumber"), CVV("cvvNumber"), WALLET_AMOUNT("walletAmount"), SAVED_CARD_ID(
            "savedCardId"), EXPIRY_YEAR("expiryYear"), EXPIRY_MONTH("expiryMonth"), STORE_CARD_FLAG("storeCardFlag"), ADD_MONEY(
            "addMoney"), SAVED_CARD_TYPE("saved_card_type"), VIRTUAL_PAYMENT_ADDRESS("VIRTUAL_PAYMENT_ADDRESS"), ICICI_IDEBIT(
            "isIciciIDebit"), PASS_CODE("PASS_CODE"), MPIN("mpin"), DEVICE_ID("deviceId"), SEQ_NO("sequenceNumber"), CC_DIRECT(
            "ccDirect"), APP_ID("appId");

    private String value;

    private PaymentRequestParam(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
