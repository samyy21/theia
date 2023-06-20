package com.paytm.pgplus.theia.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.BaseResponseBody;

public class AgreementResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = 5614005866893570163L;

    @JsonProperty("redirectUri")
    private String redirectUri;

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AgreementResponseBody [redirectUri=").append(redirectUri).append("]");
        return builder.toString();
    }
}
