package com.paytm.pgplus.theia.enums;

public enum AgreementEndponts {

    INITIATE_AGREEMENT("/theia/agreement/initiate"), CREATE_AGREEMENT("/theia/agreement/create");

    private String endPoint;

    AgreementEndponts(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getEndPoint() {
        return endPoint;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgreementEndponts{");
        sb.append("endPoint='").append(endPoint).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
