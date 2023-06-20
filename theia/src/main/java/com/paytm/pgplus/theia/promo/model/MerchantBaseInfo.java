package com.paytm.pgplus.theia.promo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.offline.model.base.BaseRequest;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class MerchantBaseInfo extends BaseRequest {

    @JsonProperty("mid")
    private String mid;

    @JsonProperty("officialName")
    private String officialName;

    @JsonProperty("isValidMid")
    private boolean isValidMid = false;

    public MerchantBaseInfo() {
        super();
    }

    public MerchantBaseInfo(String mid) {
        super();
        this.mid = mid;
    }

    public MerchantBaseInfo(String mid, String officialName, boolean isValidMid) {
        this.mid = mid;
        this.officialName = officialName;
        this.isValidMid = isValidMid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public void setOfficialName(String officialName) {
        this.officialName = officialName;
    }

    public void setValidMid(boolean validMid) {
        isValidMid = validMid;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
    }
}
