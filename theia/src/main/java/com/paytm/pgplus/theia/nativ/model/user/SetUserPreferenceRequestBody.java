/**
 * Alipay.com Inc. * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.paytm.pgplus.theia.nativ.model.user;

import com.paytm.pgplus.facade.user.models.UserPreferenceInfo;

import java.io.Serializable;
import java.util.List;

public class SetUserPreferenceRequestBody implements Serializable {

    private static final long serialVersionUID = -4189297799917792222L;

    private List<UserPreferenceInfo> userPreferenceInfoDetails;

    private String mid;
    private String orderId;

    public SetUserPreferenceRequestBody() {
    }

    public SetUserPreferenceRequestBody(List<UserPreferenceInfo> userPreferenceInfoDetails) {
        this.userPreferenceInfoDetails = userPreferenceInfoDetails;
    }

    public List<UserPreferenceInfo> getUserPreferenceDetails() {
        return userPreferenceInfoDetails;
    }

    public void setUserPreferenceDetails(List<UserPreferenceInfo> userPreferenceInfoDetails) {
        this.userPreferenceInfoDetails = userPreferenceInfoDetails;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SetUserPreferenceRequestBody{");
        sb.append("userPreferenceDetails=").append(userPreferenceInfoDetails);
        sb.append('}');
        return sb.toString();
    }
}