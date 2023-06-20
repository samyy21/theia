/**
 * Alipay.com Inc. * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.paytm.pgplus.theia.nativ.model.user;

import com.paytm.pgplus.facade.user.models.UserPreferenceInfo;

import java.io.Serializable;
import java.util.List;

public class SetUserPreferenceServiceRequest implements Serializable {

    private static final long serialVersionUID = -5080314873261892973L;
    private String mid;
    private String orderId;
    private String userId;
    private List<UserPreferenceInfo> userPreferences;

    public SetUserPreferenceServiceRequest() {
    }

    public SetUserPreferenceServiceRequest(String mid, String orderId, String userId,
            List<UserPreferenceInfo> userPreferences) {
        this.mid = mid;
        this.orderId = orderId;
        this.userId = userId;
        this.userPreferences = userPreferences;
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

    public List<UserPreferenceInfo> getUserPreferences() {
        return userPreferences;
    }

    public void setUserPreferences(List<UserPreferenceInfo> userPreferences) {
        this.userPreferences = userPreferences;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}