package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;

public class BaseS2SResponseBody implements Serializable {

    private static final long serialVersionUID = -8312421456542284131L;

    protected BizResultInfo resultInfo;

    public BaseS2SResponseBody() {
    }

    public BaseS2SResponseBody(BizResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    public BizResultInfo getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(BizResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BaseS2SResponseBody [resultInfo=").append(resultInfo).append("]");
        return builder.toString();
    }

}
