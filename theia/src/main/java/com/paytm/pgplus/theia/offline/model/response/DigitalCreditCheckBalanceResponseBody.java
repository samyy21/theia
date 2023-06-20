package com.paytm.pgplus.theia.offline.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.offline.model.payview.DigitalCreditBalanceInfo;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by rahulverma on 17/4/18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DigitalCreditCheckBalanceResponseBody extends ResponseBody {

    private static final long serialVersionUID = 3634157137036877827L;
    @NotNull
    @Valid
    private DigitalCreditBalanceInfo balanceInfo;

    private DigitalCreditOnboardingInfo onboardingInfo;

    private String infoButtonMessage;

    private String displayMessage;

    private String accountStatus;

    private boolean enable;

    public DigitalCreditBalanceInfo getBalanceInfo() {
        return balanceInfo;
    }

    public void setBalanceInfo(DigitalCreditBalanceInfo balanceInfo) {
        this.balanceInfo = balanceInfo;
    }

    public DigitalCreditOnboardingInfo getOnboardingInfo() {
        return onboardingInfo;
    }

    public void setOnboardingInfo(DigitalCreditOnboardingInfo onboardingInfo) {
        this.onboardingInfo = onboardingInfo;
    }

    public String getInfoButtonMessage() {
        return infoButtonMessage;
    }

    public void setInfoButtonMessage(String infoButtonMessage) {
        this.infoButtonMessage = infoButtonMessage;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
