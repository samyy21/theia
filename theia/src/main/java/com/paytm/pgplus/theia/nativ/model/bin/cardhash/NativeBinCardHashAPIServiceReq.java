package com.paytm.pgplus.theia.nativ.model.bin.cardhash;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;

public class NativeBinCardHashAPIServiceReq {
    UserDetailsBiz userDetailsBiz;

    public UserDetailsBiz getUserDetailsBiz() {
        return userDetailsBiz;
    }

    public void setUserDetailsBiz(UserDetailsBiz userDetailsBiz) {
        this.userDetailsBiz = userDetailsBiz;
    }
}
