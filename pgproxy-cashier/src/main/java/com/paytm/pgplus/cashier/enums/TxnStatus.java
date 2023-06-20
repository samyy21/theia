package com.paytm.pgplus.cashier.enums;

/**
 * @author amit.dubey
 */
public enum TxnStatus {

    TXN_SUCCESS("TXN_SUCCESS"), TXN_FAILURE("TXN_FAILURE");

    private String value;

    /**
     * @param value
     */
    TxnStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /*
     * @JsonCreator public static TxnStatus create(String value) {
     * System.out.println(" Inside txnStatus "); if (value == null) { throw new
     * EnumValidationException(); } for (TxnStatus v : values()) { if
     * (value.equals(v.getValue())) { return v; } }
     * System.out.println(" Calling TxnStatus."); throw new
     * EnumValidationException(); }
     */

}