package com.paytm.pgplus.theia.promo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

public class CardNetworkInfo implements Serializable {

    @JsonProperty("cardNetwork")
    private String cardNetwork;

    @JsonProperty("logoUrl")
    private String logoUrl;

    @JsonProperty("displayName")
    private String displayName;

    public CardNetworkInfo() {
    }

    public String getCardNetwork() {
        return cardNetwork;
    }

    public void setCardNetwork(String cardNetwork) {
        this.cardNetwork = cardNetwork;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
    }
}
