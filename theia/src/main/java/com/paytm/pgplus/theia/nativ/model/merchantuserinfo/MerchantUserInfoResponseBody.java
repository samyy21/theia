package com.paytm.pgplus.theia.nativ.model.merchantuserinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.BaseResponseBody;
import io.swagger.annotations.ApiModel;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantUserInfoResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = -3551361706770883483L;

    @JsonProperty("merchantInfoResp")
    private MerchantInfoResp merchantInfoResp;

    @JsonProperty("userInfoResp")
    private UserInfoResp userInfoResp;

    public MerchantUserInfoResponseBody() {
        super();
    }

    public MerchantInfoResp getMerchantInfoResp() {
        return merchantInfoResp;
    }

    public void setMerchantInfoResp(MerchantInfoResp merchantInfoResp) {
        this.merchantInfoResp = merchantInfoResp;
    }

    public UserInfoResp getUserInfoResp() {
        return userInfoResp;
    }

    public void setUserInfoResp(UserInfoResp userInfoResp) {
        this.userInfoResp = userInfoResp;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MerchantUserInfoResponseBody [merchantInfo=").append(merchantInfoResp).append(userInfoResp)
                .append(super.toString()).append("]");
        return builder.toString();
    }

}
