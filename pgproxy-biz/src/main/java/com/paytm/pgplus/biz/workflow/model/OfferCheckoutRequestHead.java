package com.paytm.pgplus.biz.workflow.model;

import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;

import java.io.Serializable;

public class OfferCheckoutRequestHead implements Serializable {

    private static final long serialVersionUID = -7236917328908430339L;

    private String version;
    private String clientId;
    @Mask(prefixNoMaskLen = 2, suffixNoMaskLen = 2)
    private String clientSecret;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
