package com.paytm.pgplus.theia.merchant.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author manojpal
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TheiaMerchantMappingResponse {

    private ResultInfo resultInfo;
    private String sourceId;
    private String type;
    // private String userData;
    private MerchantData merchantData;

    @JsonProperty(value = "bizResultInfo")
    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    @JsonProperty(value = "sourceId")
    public String getSourceId() {
        return sourceId;
    }

    @JsonProperty(value = "type")
    public String getType() {
        return type;
    }

    /*
     * @JsonProperty(value = "userData") public String getUserData() { return
     * userData; }
     */

    @JsonProperty(value = "merchantData")
    public MerchantData getMerchantData() {
        return merchantData;
    }

}
