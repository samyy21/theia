package com.paytm.pgplus.theia.merchant.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author manojpal
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtendedInfo {

    private String entityKey;

    @JsonProperty(value = "entityKey")
    public String getEntityKey() {
        return entityKey;
    }

}
