package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;

import java.io.Serializable;

public class NativePersistData implements Serializable {

    private static final long serialVersionUID = -8077355810987139674L;

    private UserDetailsBiz userDetails;

    public NativePersistData(UserDetailsBiz userDetails) {
        this.userDetails = userDetails;
    }

    public UserDetailsBiz getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetailsBiz userDetails) {
        this.userDetails = userDetails;
    }

}
