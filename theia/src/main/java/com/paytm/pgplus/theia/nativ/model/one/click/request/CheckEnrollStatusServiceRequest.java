package com.paytm.pgplus.theia.nativ.model.one.click.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class CheckEnrollStatusServiceRequest implements Serializable {
    private static final long serialVersionUID = 966928229449682093L;
    @JsonProperty("appId")
    private String appId;
    @JsonProperty("custId")
    private String custId;
    @JsonProperty("accountDataList")
    private List<EnrolledCardData> enrolledCardDataList;

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
        final StringBuilder sb = new StringBuilder("CheckEnrollStatusServiceRequest {");
        sb.append(" appId='").append(appId.toString()).append('\'').append(", custId='").append(custId.toString())
                .append('\'').append(", accountDataList='").append(enrolledCardDataList.toString()).append('\'')
                .append('}');
        return sb.toString();
    }
}
