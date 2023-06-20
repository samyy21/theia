/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.model.request;

import com.paytm.pgplus.biz.workflow.model.BizResultInfo;

import java.io.Serializable;

public class BizCancelOrderResponse implements Serializable {
    private static final long serialVersionUID = 8L;
    private boolean successfullyProcessed;
    private String description;
    private BizResultInfo bizResultInfo;

    public BizCancelOrderResponse() {

    }

    public BizCancelOrderResponse(final boolean isSuccessfullyProcessed, final String description) {
        this.successfullyProcessed = isSuccessfullyProcessed;
        this.description = description;
    }

    public BizCancelOrderResponse(final boolean isSuccessfullyProcessed, BizResultInfo bizResultInfo) {
        this.successfullyProcessed = isSuccessfullyProcessed;
        this.description = bizResultInfo.getResultMsg();
        this.bizResultInfo = bizResultInfo;
    }

    public boolean isSuccessfullyProcessed() {
        return successfullyProcessed;
    }

    public void setSuccessfullyProcessed(boolean successfullyProcessed) {
        this.successfullyProcessed = successfullyProcessed;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BizResultInfo getBizResultInfo() {
        return bizResultInfo;
    }

    public void setBizResultInfo(BizResultInfo bizResultInfo) {
        this.bizResultInfo = bizResultInfo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BizCancelOrderResponse{");
        sb.append("successfullyProcessed=").append(successfullyProcessed);
        sb.append(", description='").append(description).append('\'');
        sb.append(", bizResultInfo=").append(bizResultInfo);
        sb.append('}');
        return sb.toString();
    }
}