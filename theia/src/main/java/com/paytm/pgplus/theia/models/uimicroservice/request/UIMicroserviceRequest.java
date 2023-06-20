package com.paytm.pgplus.theia.models.uimicroservice.request;

import com.paytm.pgplus.facade.enums.UIMicroserviceUrl;

public class UIMicroserviceRequest {

    private String jsonPayload;
    private String channelId;
    private String isDataEncoded;
    private UIMicroserviceUrl uiMicroServiceUrl;

    public UIMicroserviceRequest() {
    }

    public UIMicroserviceRequest(String jsonPayload, String channelId, String isDataEncoded,
            UIMicroserviceUrl uiMicroServiceUrl) {
        this.jsonPayload = jsonPayload;
        this.channelId = channelId;
        this.isDataEncoded = isDataEncoded;
        this.uiMicroServiceUrl = uiMicroServiceUrl;
    }

    public UIMicroserviceRequest(String jsonPayload, UIMicroserviceUrl uiMicroServiceUrl) {
        this.jsonPayload = jsonPayload;
        this.uiMicroServiceUrl = uiMicroServiceUrl;
    }

    public String getJsonPayload() {
        return jsonPayload;
    }

    public void setJsonPayload(String jsonPayload) {
        this.jsonPayload = jsonPayload;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getIsDataEncoded() {
        return isDataEncoded;
    }

    public void setIsDataEncoded(String isDataEncoded) {
        this.isDataEncoded = isDataEncoded;
    }

    public UIMicroserviceUrl getUiMicroServiceUrl() {
        return uiMicroServiceUrl;
    }

    public void setUiMicroServiceUrl(UIMicroserviceUrl uiMicroServiceUrl) {
        this.uiMicroServiceUrl = uiMicroServiceUrl;
    }

}
