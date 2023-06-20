package com.paytm.pgplus.biz.core.model.oauth;

import java.io.Serializable;

/**
 * @author namanjain
 *
 */
public class VerifyLoginResponseBizBean implements Serializable {

    private static final long serialVersionUID = 4L;

    private OAuthTokenBiz accessToken;

    private OAuthTokenBiz paytmToken;

    private OAuthTokenBiz walletToken;

    private UserDetailsBiz userDetails;

    public OAuthTokenBiz getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(OAuthTokenBiz accessToken) {
        this.accessToken = accessToken;
    }

    public OAuthTokenBiz getPaytmToken() {
        return paytmToken;
    }

    public void setPaytmToken(OAuthTokenBiz paytmToken) {
        this.paytmToken = paytmToken;
    }

    public OAuthTokenBiz getWalletToken() {
        return walletToken;
    }

    public void setWalletToken(OAuthTokenBiz walletToken) {
        this.walletToken = walletToken;
    }

    public UserDetailsBiz getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetailsBiz userDetails) {
        this.userDetails = userDetails;
    }

}
