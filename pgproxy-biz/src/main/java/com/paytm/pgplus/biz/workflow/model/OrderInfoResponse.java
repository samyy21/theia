package com.paytm.pgplus.biz.workflow.model;

import com.paytm.pgplus.common.model.ResultInfo;

public class OrderInfoResponse {
    private String acquirementId;
    private ResultInfo resultInfo;

    public String getAcquirementId() {
        return acquirementId;
    }

    public void setAcquirementId(String acquirementId) {
        this.acquirementId = acquirementId;
    }

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    @Override
    public String toString() {
        return "OrderInfoResponse{" + "acquirementId='" + acquirementId + '\'' + ", resultInfo=" + resultInfo + '}';
    }
}
