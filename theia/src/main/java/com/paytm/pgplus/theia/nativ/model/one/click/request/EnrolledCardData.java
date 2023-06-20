package com.paytm.pgplus.theia.nativ.model.one.click.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EnrolledCardData {
    @JsonProperty("deviceId")
    private String deviceId;
    @JsonProperty("cardAlias")
    private String cardAlias;
    @JsonProperty("bin")
    private String bin;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getCardAlias() {
        return cardAlias;
    }

    public void setCardAlias(String cardAlias) {
        this.cardAlias = cardAlias;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    @Override
    public String toString() {
        return "EnrolledCardData{" + "deviceId='" + deviceId + '\'' + ", cardAlias='" + cardAlias + '\'' + ", bin='"
                + bin + '\'' + '}';
    }
}
