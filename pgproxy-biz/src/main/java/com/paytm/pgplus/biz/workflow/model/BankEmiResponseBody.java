package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class BankEmiResponseBody implements Serializable {

    private static final long serialVersionUID = -8456345908048320876L;

    private String resultStatus;
    private String resultCode;
    private String resultMsg;
    private String mid;
    private String tid;
    private String cardType;
    private String message;
    private String emiMinAmount;
    private List<String> details;
    private EmiDetails emiDetail;

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

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmiMinAmount() {
        return emiMinAmount;
    }

    public void setEmiMinAmount(String emiMinAmount) {
        this.emiMinAmount = emiMinAmount;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }

    public EmiDetails getEmiDetail() {
        return emiDetail;
    }

    public void setEmiDetail(EmiDetails emiDetail) {
        this.emiDetail = emiDetail;
    }

    @Override
    public String toString() {
        return "BankEmiResponseBody{" + "resultStatus='" + resultStatus + '\'' + ", resultCode='" + resultCode + '\''
                + ", resultMsg='" + resultMsg + '\'' + ", mid='" + mid + '\'' + ", tid='" + tid + '\'' + ", cardType='"
                + cardType + '\'' + ", message='" + message + '\'' + ", emiMinAmount='" + emiMinAmount + '\''
                + ", details=" + details + ", emiDetail=" + emiDetail + '}';
    }
}
