package com.paytm.pgplus.theia.nativ.model.payview.emi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.nativ.model.common.UserInfo;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * Created by charu on 07/10/18.
 */

public class EMIEligibilityRequestBody implements Serializable {

    private static final long serialVersionUID = -2918958487703375931L;

    @JsonProperty("userInfo")
    @NotNull
    private UserInfo userInfo;

    @JsonProperty("mid")
    @NotNull
    private String mid;

    @JsonProperty("payMethod")
    @NotNull
    private String payMethod;

    @JsonProperty("channelCode")
    @NotNull
    private String channelCode;

    private String emiId;

    private List<String> emiTypes;

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public String getMid() {
        return mid;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public String getEmiId() {
        return emiId;
    }

    public List<String> getEmiTypes() {
        return emiTypes;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EMIEligibilityRequestBody{");
        sb.append("userInfo=").append(userInfo);
        sb.append(", mid='").append(mid).append('\'');
        sb.append(", payMethod=").append(payMethod);
        sb.append(", channelCode='").append(channelCode);
        sb.append(", emiId='").append(emiId);
        sb.append(", emiTypes='").append(emiTypes).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
