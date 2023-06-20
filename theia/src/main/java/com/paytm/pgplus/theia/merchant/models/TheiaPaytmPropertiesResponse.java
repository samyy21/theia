/**
 * 
 */
package com.paytm.pgplus.theia.merchant.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author namanjain
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TheiaPaytmPropertiesResponse {

    @JsonProperty(value = "paytmProperties")
    private PaytmProperties paytmProperties;

    @JsonProperty(value = "resultInfo")
    private ResultInfo resultInfo;

    public PaytmProperties getPaytmProperties() {
        return paytmProperties;
    }

    public void setPaytmProperties(PaytmProperties paytmProperties) {
        this.paytmProperties = paytmProperties;
    }

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

}
