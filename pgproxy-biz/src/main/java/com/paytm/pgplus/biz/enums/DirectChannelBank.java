package com.paytm.pgplus.biz.enums;

public enum DirectChannelBank {

    ICICIIDEBIT("ICICI"), ICICIDIRECT("ICICI"), HDFCIDEBIT("HDFC"), HDFCDIRECT("HDFC"), CITIDIRECT("CITI"), AXSD("AXIS"), ICIO(
            "ICICI"), HDFO("HDFC");
    private String bankCode;

    private DirectChannelBank(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankCode() {
        return bankCode;
    }

}
