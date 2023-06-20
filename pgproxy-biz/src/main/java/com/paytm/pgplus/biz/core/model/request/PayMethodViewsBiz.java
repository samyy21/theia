/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.model.request;

import com.paytm.pgplus.facade.payment.models.PayOptionRemainingLimits;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author namanjain
 *
 */
public class PayMethodViewsBiz implements Serializable {
    private static final long serialVersionUID = -8630485188176006985L;

    private String payMethod;
    private List<PayChannelOptionViewBiz> payChannelOptionViews;
    private List<PayCardOptionViewBiz> payCardOptionViews;
    private RiskResultBiz riskResult;
    private String remainingLimit;
    private List<PayOptionRemainingLimits> payOptionRemainingLimits;

    private Boolean newUser;

    public void setRiskResult(final RiskResultBiz riskResult) {
        this.riskResult = riskResult;
    }

    public RiskResultBiz getRiskResult() {
        return riskResult;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(final String payMethod) {
        this.payMethod = payMethod;
    }

    public List<PayChannelOptionViewBiz> getPayChannelOptionViews() {
        return payChannelOptionViews != null ? payChannelOptionViews : Collections.emptyList();
    }

    public void setPayChannelOptionViews(final List<PayChannelOptionViewBiz> payChannelOptionViews) {
        this.payChannelOptionViews = payChannelOptionViews;
    }

    public List<PayCardOptionViewBiz> getPayCardOptionViews() {
        return payCardOptionViews != null ? payCardOptionViews : Collections.emptyList();
    }

    public void setPayCardOptionViews(final List<PayCardOptionViewBiz> payCardOptionViews) {
        this.payCardOptionViews = payCardOptionViews;
    }

    public Boolean getNewUser() {
        return newUser;
    }

    public void setNewUser(Boolean newUser) {
        this.newUser = newUser;
    }

    public String getRemainingLimit() {
        return remainingLimit;
    }

    public void setRemainingLimit(String remainingLimit) {
        this.remainingLimit = remainingLimit;
    }

    public List<PayOptionRemainingLimits> getPayOptionRemainingLimits() {
        return payOptionRemainingLimits;
    }

    public void setPayOptionRemainingLimits(List<PayOptionRemainingLimits> payOptionRemainingLimits) {
        this.payOptionRemainingLimits = payOptionRemainingLimits;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PayMethodViewsBiz [payMethod=").append(payMethod).append(", payChannelOptionViews=")
                .append(payChannelOptionViews).append(", payCardOptionViews=").append(payCardOptionViews)
                .append(", riskResult=").append(riskResult).append(", newUser=").append(newUser)
                .append(", remainingLimit=").append(remainingLimit).append(", payOptionRemainingLimits=")
                .append(payOptionRemainingLimits).append("]");
        return builder.toString();
    }
}