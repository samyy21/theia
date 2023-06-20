package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BizResultInfo implements Serializable {

    private static final long serialVersionUID = -5593088024706291469L;

    private String resultStatus;
    private String resultCodeId;
    private String resultCode;
    private String resultMsg;

    public BizResultInfo() {

    }

    public BizResultInfo(String resultStatus, String resultCodeId, String resultCode, String resultMsg) {
        this.resultCode = resultCode;
        this.resultCodeId = resultCodeId;
        this.resultMsg = resultMsg;
        this.resultStatus = resultStatus;

    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getResultCodeId() {
        return resultCodeId;
    }

    public void setResultCodeId(String resultCodeId) {
        this.resultCodeId = resultCodeId;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BizResultInfo [resultStatus=").append(resultStatus).append(", resultCodeId=")
                .append(resultCodeId).append(", resultCode=").append(resultCode).append(", resultMsg=")
                .append(resultMsg).append("]");
        return builder.toString();
    }

}
