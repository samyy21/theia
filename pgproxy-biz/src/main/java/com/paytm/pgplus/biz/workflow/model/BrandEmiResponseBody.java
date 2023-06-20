package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class BrandEmiResponseBody implements Serializable {

    private static final long serialVersionUID = 2779539393957453484L;

    private String resultCodeId;
    private String resultStatus;
    private String resultCode;
    private String resultMsg;
    private String mid;
    private String tid;
    private BrandEmiDetail emiDetail;

    public String getResultCodeId() {
        return resultCodeId;
    }

    public void setResultCodeId(String resultCodeId) {
        this.resultCodeId = resultCodeId;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
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

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public BrandEmiDetail getEmiDetail() {
        return emiDetail;
    }

    public void setEmiDetail(BrandEmiDetail emiDetail) {
        this.emiDetail = emiDetail;
    }

    @Override
    public String toString() {
        return "BrandEmiResponseBody{" + "resultCodeId='" + resultCodeId + '\'' + ", resultStatus='" + resultStatus
                + '\'' + ", resultCode='" + resultCode + '\'' + ", resultMsg='" + resultMsg + '\'' + ", mid='" + mid
                + '\'' + ", tid='" + tid + '\'' + ", emiDetail=" + emiDetail + '}';
    }
}