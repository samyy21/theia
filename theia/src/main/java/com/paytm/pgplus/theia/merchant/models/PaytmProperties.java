/**
 * 
 */
package com.paytm.pgplus.theia.merchant.models;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author namanjain
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaytmProperties {

    private Map<String, String> paytmProperties;

    public Map<String, String> getPaytmProperties() {
        return paytmProperties;
    }

    public void setPaytmProperties(Map<String, String> paytmProperties) {
        this.paytmProperties = paytmProperties;
    }

}
