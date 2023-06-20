package com.paytm.pgplus.theia.nativ.model.common;

import java.io.Serializable;

public class EnhanceCashierPageOAuthInfo implements Serializable {

    private static final long serialVersionUID = 2528504899586070345L;

    private String webClientId;

    private String wapClientId;

    private String oAuthBaseUrl;

    private String email;

    private String address1;

    private String address2;

    private String msisdn;

    private String orderId;

    private String mid;

    private String oAuthResponseUrl;

    private String loginTheme;

    private boolean autoLoginEnabled;

    private String oAuthLogoutReturnUrl;

    private String oAuthLogoutUrl;

    private String paytmToken;

    private String autoLogin;

    public String getWebClientId() {
        return webClientId;
    }

    public void setWebClientId(String webClientId) {
        this.webClientId = webClientId;
    }

    public String getWapClientId() {
        return wapClientId;
    }

    public void setWapClientId(String wapClientId) {
        this.wapClientId = wapClientId;
    }

    public String getoAuthBaseUrl() {
        return oAuthBaseUrl;
    }

    public void setoAuthBaseUrl(String oAuthBaseUrl) {
        this.oAuthBaseUrl = oAuthBaseUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getoAuthResponseUrl() {
        return oAuthResponseUrl;
    }

    public void setoAuthResponseUrl(String oAuthResponseUrl) {
        this.oAuthResponseUrl = oAuthResponseUrl;
    }

    public String getLoginTheme() {
        return loginTheme;
    }

    public void setLoginTheme(String loginTheme) {
        this.loginTheme = loginTheme;
    }

    public boolean isAutoLoginEnabled() {
        return autoLoginEnabled;
    }

    public void setAutoLoginEnabled(boolean autoLoginEnabled) {
        this.autoLoginEnabled = autoLoginEnabled;
    }

    public String getoAuthLogoutReturnUrl() {
        return oAuthLogoutReturnUrl;
    }

    public void setoAuthLogoutReturnUrl(String oAuthLogoutReturnUrl) {
        this.oAuthLogoutReturnUrl = oAuthLogoutReturnUrl;
    }

    public String getoAuthLogoutUrl() {
        return oAuthLogoutUrl;
    }

    public void setoAuthLogoutUrl(String oAuthLogoutUrl) {
        this.oAuthLogoutUrl = oAuthLogoutUrl;
    }

    public String getPaytmToken() {
        return paytmToken;
    }

    public void setPaytmToken(String paytmToken) {
        this.paytmToken = paytmToken;
    }

    public String getAutoLogin() {
        return autoLogin;
    }

    public void setAutoLogin(String autoLogin) {
        this.autoLogin = autoLogin;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EnhanceCashierPageOAuthInfo{");
        sb.append("address1='").append(address1).append('\'');
        sb.append(", address2='").append(address2).append('\'');
        sb.append(", autoLogin='").append(autoLogin).append('\'');
        sb.append(", autoLoginEnabled=").append(autoLoginEnabled);
        sb.append(", email='").append(email).append('\'');
        sb.append(", loginTheme='").append(loginTheme).append('\'');
        sb.append(", mid='").append(mid).append('\'');
        sb.append(", msisdn='").append(msisdn).append('\'');
        sb.append(", oAuthBaseUrl='").append(oAuthBaseUrl).append('\'');
        sb.append(", oAuthLogoutReturnUrl='").append(oAuthLogoutReturnUrl).append('\'');
        sb.append(", oAuthLogoutUrl='").append(oAuthLogoutUrl).append('\'');
        sb.append(", oAuthResponseUrl='").append(oAuthResponseUrl).append('\'');
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", paytmToken='").append(paytmToken).append('\'');
        sb.append(", wapClientId='").append(wapClientId).append('\'');
        sb.append(", webClientId='").append(webClientId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
