package com.paytm.pgplus.theia.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;
import com.paytm.pgplus.theia.enums.CybersourceResponseEnum;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VisaCyberSourceResponse implements Serializable {

    private static final long serialVersionUID = 213086685600036703L;

    private String resultCode;
    private String resultCodeId;
    private String resultMsg;
    private String cardType;
    private String status;
    private CybersourceResponseEnum responseEnum;

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultCodeId() {
        return resultCodeId;
    }

    public void setResultCodeId(String resultCodeId) {
        this.resultCodeId = resultCodeId;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public CybersourceResponseEnum getResponseEnum() {
        return responseEnum;
    }

    public void setResponseEnum(CybersourceResponseEnum responseEnum) {
        this.responseEnum = responseEnum;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
