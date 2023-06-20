package com.paytm.pgplus.theia.nativ.model.common;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedCashierPageLoginInfo implements Serializable {

    private static final long serialVersionUID = 2548293311863064268L;

    private boolean loginFlag;

    private boolean pgAutoLoginEnabled;
    private boolean mobileNumberNonEditable = false;
    private boolean disableLoginStrip = false;

    public boolean isLoginFlag() {
        return loginFlag;
    }

    public void setLoginFlag(boolean loginFlag) {
        this.loginFlag = loginFlag;
    }

    public boolean isPgAutoLoginEnabled() {
        return pgAutoLoginEnabled;
    }

    public void setPgAutoLoginEnabled(boolean pgAutoLoginEnabled) {
        this.pgAutoLoginEnabled = pgAutoLoginEnabled;
    }

    public boolean isMobileNumberNonEditable() {
        return mobileNumberNonEditable;
    }

    public void setMobileNumberNonEditable(boolean mobileNumberNonEditable) {
        this.mobileNumberNonEditable = mobileNumberNonEditable;
    }

    public boolean isDisableLoginStrip() {
        return disableLoginStrip;
    }

    public void setDisableLoginStrip(boolean disableLoginStrip) {
        this.disableLoginStrip = disableLoginStrip;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EnhancedCashierPageLoginInfo{");
        sb.append("loginFlag=").append(loginFlag);
        sb.append(", pgAutoLoginEnabled=").append(pgAutoLoginEnabled);
        sb.append(", mobileNumberNonEditable=").append(mobileNumberNonEditable);
        sb.append(", disableLoginStrip=").append(disableLoginStrip);
        sb.append('}');
        return sb.toString();
    }
}
