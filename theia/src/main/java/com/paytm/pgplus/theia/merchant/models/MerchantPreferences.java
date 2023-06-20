package com.paytm.pgplus.theia.merchant.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author manojpal
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantPreferences {

    private String prefType;

    private String prefStatus;

    private String prefValue;

    @JsonProperty(value = "prefType")
    public String getPrefType() {
        return prefType;
    }

    @JsonProperty(value = "prefStatus")
    public String getPrefStatus() {
        return prefStatus;
    }

    @JsonProperty(value = "prefValue")
    public String getPrefValue() {
        return prefValue;
    }

}
