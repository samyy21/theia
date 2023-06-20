package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;

import java.io.Serializable;

public class LoginInfo implements Serializable {

    private static final long serialVersionUID = 6983240650203540630L;

    private boolean userLoggedIn;
    private boolean pgAutoLoginEnabled;
    private boolean mobileNumberNonEditable;

    private boolean disableLoginStrip;

    public void setDisableLoginStrip(boolean disableLoginStrip) {
        this.disableLoginStrip = disableLoginStrip;
    }

    public boolean getDisableLoginStrip() {
        return disableLoginStrip;
    }

    public boolean isUserLoggedIn() {
        return userLoggedIn;
    }

    public void setUserLoggedIn(boolean userLoggedIn) {
        this.userLoggedIn = userLoggedIn;
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

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
