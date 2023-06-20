package com.paytm.pgplus.theia.accesstoken.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.response.ResultInfo;

public class CreateAccessTokenResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = 7075991833282597294L;

    @JsonProperty("accessToken")
    private String accessToken;

    public CreateAccessTokenResponseBody(ResultInfo resultInfo, String accessToken) {
        super(resultInfo);
        this.accessToken = accessToken;
    }

    public CreateAccessTokenResponseBody() {
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CreateAccessTokenResponseBody [accessToken=").append(accessToken).append(super.toString())
                .append("]");
        return builder.toString();
    }
}
