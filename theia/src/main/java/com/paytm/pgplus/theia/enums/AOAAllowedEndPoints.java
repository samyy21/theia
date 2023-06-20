package com.paytm.pgplus.theia.enums;

public enum AOAAllowedEndPoints {

    INITIATE_TXN("api/v1/initiateTransaction"), FETCH_PAYVIEW("api/v1/fetchPaymentOptions"), FETCH_BIN_DETAIL(
            "api/v1/fetchBinDetail"), PROCESS_TRANSACTION("/api/v1/processTransaction");

    private String endPoint;

    AOAAllowedEndPoints(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getEndPoint() {
        return endPoint;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AOAAllowedEndPoints{");
        sb.append("endPoint='").append(endPoint).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
