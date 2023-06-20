package com.paytm.pgplus.theia.nativ.enums;

public enum PayModeType {

    DEBIT_CARD("card"), CREDIT_CARD("card"), NET_BANKING("nb"), PPBL("ppb"), EMI("emi"), UPI("upi"), COD("cod"), PAYTM_DIGITAL_CREDIT(
            "pdc"), SAVED_CARDS("savedCards"), SAVED_VPAS("savedVPAs"), ADVANCE_DEPOSIT_ACCOUNT("ada"), WALLET("aoa"), GIFT_VOUCHER(
            "mgv"), BANK_MANDATE("bm"), SAVED_MANDATE_BANKS("savedMandateBanks"), BALANCE("wallet");

    private String type;

    PayModeType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
