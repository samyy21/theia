package com.paytm.pgplus.theia.nativ.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.response.BaseResponseBody;

import io.swagger.annotations.ApiModel;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenerateOtpResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = -3551361706770883483L;

    public GenerateOtpResponseBody() {
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
