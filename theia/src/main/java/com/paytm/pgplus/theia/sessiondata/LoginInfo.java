/**
 *
 */
package com.paytm.pgplus.theia.sessiondata;

import com.dyuproject.protostuff.Tag;

import java.io.Serializable;

/**
 * @author kesari
 * @createdOn 28-Mar-2016
 */
public class LoginInfo implements Serializable {

    /**
	 *
	 */
    private static final long serialVersionUID = 5061144923889884823L;

    @Tag(value = 1)
    private boolean loginFlag;
    @Tag(value = 2)
    private boolean showLoginSuccess;
    @Tag(value = 3)
    private String oauthInfo;
    @Tag(value = 4)
    private boolean loginMandatory;
    @Tag(value = 5)
    private OAuthUserInfo user = new OAuthUserInfo();
    @Tag(value = 6)
    private boolean autoLoginCreate;
    @Tag(value = 7)
    private boolean logoutAllowed;
    @Tag(value = 8)
    private int loginRetryCount;
    @Tag(value = 9)
    private String loginTheme;

    @Tag(value = 10)
    private String oAuthInfo;
    @Tag(value = 11)
    private String oAuthInfoHost;
    @Tag(value = 12)
    private String oAuthInfoClientID;
    @Tag(value = 13)
    private String oAuthInfoWAPClientID;
    @Tag(value = 14)
    private String oAuthInfoReturnURL;
    @Tag(value = 15)
    private boolean loginWithOtp;
    @Tag(value = 16)
    private boolean autoLoginAttempt = Boolean.TRUE;

    @Tag(value = 17)
    private boolean loginDisabled = Boolean.FALSE;

    @Tag(value = 18)
    private String loginStripText;

    public String getoAuthInfo() {
        return oAuthInfo;
    }

    public void setoAuthInfo(String oAuthInfo) {
        this.oAuthInfo = oAuthInfo;
        this.oauthInfo = oAuthInfo;
    }

    public String getoAuthInfoHost() {
        return oAuthInfoHost;
    }

    /**
     * @return the loginWithOtp
     */
    public boolean isLoginWithOtp() {
        return loginWithOtp;
    }

    /**
     * @param loginWithOtp
     *            the loginWithOtp to set
     */
    public void setLoginWithOtp(boolean loginWithOtp) {
        this.loginWithOtp = loginWithOtp;
    }

    public void setoAuthInfoHost(String oAuthInfoHost) {
        this.oAuthInfoHost = oAuthInfoHost;
    }

    public String getoAuthInfoClientID() {
        return oAuthInfoClientID;
    }

    public void setoAuthInfoClientID(String oAuthInfoClientID) {
        this.oAuthInfoClientID = oAuthInfoClientID;
    }

    public String getoAuthInfoReturnURL() {
        return oAuthInfoReturnURL;
    }

    public void setoAuthInfoReturnURL(String oAuthInfoReturnURL) {
        this.oAuthInfoReturnURL = oAuthInfoReturnURL;
    }

    public boolean isAutoLoginCreate() {
        return autoLoginCreate;
    }

    public void setAutoLoginCreate(boolean autoLoginCreate) {
        this.autoLoginCreate = autoLoginCreate;
    }

    public boolean isLoginMandatory() {
        return loginMandatory;
    }

    public void setLoginMandatory(boolean loginMandatory) {
        this.loginMandatory = loginMandatory;
    }

    public boolean isShowLoginSuccess() {
        return showLoginSuccess;
    }

    public void setShowLoginSuccess(boolean showLoginSuccess) {
        this.showLoginSuccess = showLoginSuccess;
    }

    public OAuthUserInfo getUser() {
        return user;
    }

    public void setUser(OAuthUserInfo user) {
        this.user = user;
    }

    public String getOauthInfo() {
        return oauthInfo;
    }

    public void setOauthInfo(String oauthInfo) {
        this.oauthInfo = oauthInfo;
        this.oAuthInfo = oauthInfo;
    }

    public boolean isLoginFlag() {
        return loginFlag;
    }

    public void setLoginFlag(boolean loginFlag) {
        this.loginFlag = loginFlag;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    /**
     * @return the logoutAllowed
     */
    public boolean isLogoutAllowed() {
        return logoutAllowed;
    }

    /**
     * @param logoutAllowed
     *            the logoutAllowed to set
     */
    public void setLogoutAllowed(boolean logoutAllowed) {
        this.logoutAllowed = logoutAllowed;
    }

    /**
     * @return the loginRetryCount
     */
    public int getLoginRetryCount() {
        return loginRetryCount;
    }

    /**
     * @param loginRetryCount
     *            the loginRetryCount to set
     */
    public void setLoginRetryCount(int loginRetryCount) {
        this.loginRetryCount = loginRetryCount;
    }

    /**
     * @return the loginTheme
     */
    public String getLoginTheme() {
        return loginTheme;
    }

    /**
     * @param loginTheme
     *            the loginTheme to set
     */
    public void setLoginTheme(String loginTheme) {
        this.loginTheme = loginTheme;
    }

    /**
     * @return the oAuthInfoWAPClientID
     */
    public String getoAuthInfoWAPClientID() {
        return oAuthInfoWAPClientID;
    }

    /**
     * @param oAuthInfoWAPClientID
     *            the oAuthInfoWAPClientID to set
     */
    public void setoAuthInfoWAPClientID(String oAuthInfoWAPClientID) {
        this.oAuthInfoWAPClientID = oAuthInfoWAPClientID;
    }

    public boolean isAutoLoginAttempt() {
        return autoLoginAttempt;
    }

    public void setAutoLoginAttempt(boolean autoLoginAttempt) {
        this.autoLoginAttempt = autoLoginAttempt;
    }

    public boolean isLoginDisabled() {
        return loginDisabled;
    }

    public void setLoginDisabled(boolean loginDisabled) {
        this.loginDisabled = loginDisabled;
    }

    public String getLoginStripText() {
        return loginStripText;
    }

    public void setLoginStripText(String loginStripText) {
        this.loginStripText = loginStripText;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LoginInfo [loginFlag=").append(loginFlag).append(", showLoginSuccess=")
                .append(showLoginSuccess).append(", oauthInfo=").append(oauthInfo).append(", loginMandatory=")
                .append(loginMandatory).append(", user=").append(user).append(", autoLoginCreate=")
                .append(autoLoginCreate).append(", logoutAllowed=").append(logoutAllowed).append(", loginRetryCount=")
                .append(loginRetryCount).append(", loginTheme=").append(loginTheme).append(", oAuthInfo=")
                .append(oAuthInfo).append(", oAuthInfoHost=").append(oAuthInfoHost).append(", oAuthInfoClientID=")
                .append(oAuthInfoClientID).append(", oAuthInfoWAPClientID=").append(oAuthInfoWAPClientID)
                .append(", oAuthInfoReturnURL=").append(oAuthInfoReturnURL).append(", autoLoginAttempt=")
                .append(autoLoginAttempt).append(", loginDisabled=").append(loginDisabled).append(", loginStripText=")
                .append(loginStripText).append("]");
        return builder.toString();
    }

}