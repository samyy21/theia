package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpiPspOptions implements Serializable {
    @JsonProperty("name")
    private String name;
    @JsonProperty("iconUrl")
    private String iconUrl;

    public UpiPspOptions() {
    }

    public UpiPspOptions(String name, String iconUrl) {
        this.name = name;
        this.iconUrl = iconUrl;
    }

    @Override
    public String toString() {
        return "UPIPspOptions{" + "name='" + name + '\'' + ", iconUrl='" + iconUrl + '\'' + '}';
    }
}
