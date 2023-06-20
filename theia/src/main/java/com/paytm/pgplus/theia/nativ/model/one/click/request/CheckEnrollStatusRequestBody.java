package com.paytm.pgplus.theia.nativ.model.one.click.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckEnrollStatusRequestBody implements Serializable {
    private static final long serialVersionUID = 966928229449682093L;
    @JsonProperty("appId")
    private String appId;

    @JsonProperty("custId")
    private String custId;

    @JsonProperty("accountDataList")
    private List<EnrolledCardData> enrolledCardDataList;
    @JsonIgnore
    private String mid;
    @JsonIgnore
    private String referenceId;

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public List<EnrolledCardData> getEnrolledCardDataList() {
        return enrolledCardDataList;
    }

    public void setEnrolledCardDataList(List<EnrolledCardData> enrolledCardDataList) {
        this.enrolledCardDataList = enrolledCardDataList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CheckEnrollStatusRequestBody {");
        sb.append(" appId='").append(appId).append('\'').append(", custId='").append(custId).append('\'')
                .append(", enrolledCardDataList='").append(enrolledCardDataList).append('\'').append('}');
        return sb.toString();
    }
}
