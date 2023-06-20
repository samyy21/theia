package com.paytm.pgplus.theia.merchant.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TheiaMerchantRequest {

    private String id;
    private String type;

    public TheiaMerchantRequest(String id, String type) {
        super();
        this.id = id;
        this.type = type;
    }

    @JsonProperty(value = "id")
    public String getId() {
        return id;
    }

    @JsonProperty(value = "type")
    public String getType() {
        return type;
    }

}
