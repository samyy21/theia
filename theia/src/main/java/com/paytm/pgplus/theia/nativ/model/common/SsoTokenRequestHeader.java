package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ToStringBuilder;

public class SsoTokenRequestHeader extends TokenRequestHeader {

    @JsonProperty("ssoToken")
    private String ssoToken;

    private final static long serialVersionUID = -7807095168614314271L;

    public String getSsoToken() {
        return ssoToken;
    }

    public void setSsoToken(String ssoToken) {
        this.ssoToken = ssoToken;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("channelId", channelId).append("ssoToken", ssoToken).toString();
    }

}
