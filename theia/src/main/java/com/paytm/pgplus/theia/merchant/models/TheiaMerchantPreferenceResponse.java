package com.paytm.pgplus.theia.merchant.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author manojpal
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TheiaMerchantPreferenceResponse {
    private String merchantId;
    private List<MerchantPreferences> merchantPreferenceInfos;

    @JsonProperty(value = "merchantId")
    public String getMerchantId() {
        return merchantId;
    }

    @JsonProperty(value = "merchantPreferenceInfos")
    public List<MerchantPreferences> getMerchantPreferenceInfos() {
        return merchantPreferenceInfos;
    }

}
