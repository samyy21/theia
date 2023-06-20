package com.paytm.pgplus.theia.utils;

public class StagingParamValidatorResponse {

    private String respAttribute;

    private String respStatus;

    private String respMsg;

    public StagingParamValidatorResponse(String respAttribute, String respStatus, String respMsg) {
        this.respAttribute = respAttribute;
        this.respStatus = respStatus;
        this.respMsg = respMsg;
    }

    public String getRespAttribute() {
        return respAttribute;
    }

    public void setRespAttribute(String respAttribute) {
        this.respAttribute = respAttribute;
    }

    public String getRespStatus() {
        return respStatus;
    }

    public void setRespStatus(String respStatus) {
        this.respStatus = respStatus;
    }

    public String getRespMsg() {
        return respMsg;
    }

    public void setRespMsg(String respMsg) {
        this.respMsg = respMsg;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StagingParamValidatorResponse{");
        sb.append("respAttribute='").append(respAttribute).append('\'');
        sb.append(", respStatus='").append(respStatus).append('\'');
        sb.append(", respMsg='").append(respMsg).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
