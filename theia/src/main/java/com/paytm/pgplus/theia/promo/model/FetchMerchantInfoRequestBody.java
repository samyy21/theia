package com.paytm.pgplus.theia.promo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;
import java.util.List;

public class FetchMerchantInfoRequestBody implements Serializable {

    @NotBlank
    @JsonProperty("mids")
    private List<String> mid;

    public List<String> getMid() {
        return mid;
    }

    public void setMid(List<String> mid) {
        this.mid = mid;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
    }
}
