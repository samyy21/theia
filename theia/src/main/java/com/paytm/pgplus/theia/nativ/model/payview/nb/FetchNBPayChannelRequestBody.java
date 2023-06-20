package com.paytm.pgplus.theia.nativ.model.payview.nb;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.facade.user.models.UserDetails;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

public class FetchNBPayChannelRequestBody implements Serializable {

    @JsonProperty("type")
    private String type;

    private final static long serialVersionUID = 958338953026217847L;

    @JsonProperty("mid")
    private String mid;

    private String referenceId;

    @JsonIgnore
    private boolean superGwApiHit;

    @JsonIgnore
    private UserDetails userDetails;

    @JsonIgnore
    private String custId;

    public FetchNBPayChannelRequestBody() {
    }

    public FetchNBPayChannelRequestBody(
            com.paytm.pgplus.theiacommon.supergw.payview.nb.NativeFetchNBPayChannelRequestBody body) {
        this.mid = body.getMid();
        this.type = body.getType();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public boolean isSuperGwApiHit() {
        return superGwApiHit;
    }

    public void setSuperGwApiHit(boolean superGwApiHit) {
        this.superGwApiHit = superGwApiHit;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("type", type).append("mid", mid).toString();
    }

}
