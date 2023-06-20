package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;

public class BizPreferenceValue implements Serializable {

    private static final long serialVersionUID = 8360278686723528073L;

    private String serviceInstId;
    private Integer score;

    public BizPreferenceValue() {
    }

    public BizPreferenceValue(String serviceInstId, Integer score) {
        this.serviceInstId = serviceInstId;
        this.score = score;
    }

    public String getServiceInstId() {
        return serviceInstId;
    }

    public void setServiceInstId(String serviceInstId) {
        this.serviceInstId = serviceInstId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BizPreferenceValue{");
        sb.append("serviceInstId='").append(serviceInstId).append('\'');
        sb.append(", score=").append(score);
        sb.append('}');
        return sb.toString();
    }
}
