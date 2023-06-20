package com.paytm.pgplus.theia.nativ.model.payview.response;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.models.TwoFARespData;

/**
 * Created by rahulverma on 1/9/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BalanceChannel extends PayChannelBase {

    private static final long serialVersionUID = -8837876317528333510L;
    @NotNull
    @Valid
    private AccountInfo balanceInfo;

    private TwoFARespData twoFAConfig;

    public BalanceChannel() {
    }

    public BalanceChannel(AccountInfo balanceInfo) {
        this.balanceInfo = balanceInfo;
    }

    public BalanceChannel(String payMethod, String payChannelOption, StatusInfo isDisabled, StatusInfo hasLowSuccess,
            String iconUrl, AccountInfo balanceInfo) {
        super(payMethod, payChannelOption, isDisabled, hasLowSuccess, iconUrl);
        this.balanceInfo = balanceInfo;
    }

    public AccountInfo getBalanceInfo() {
        return balanceInfo;
    }

    public void setBalanceInfo(AccountInfo balanceInfo) {
        this.balanceInfo = balanceInfo;
    }

    public TwoFARespData getTwoFAConfig() {
        return twoFAConfig;
    }

    public void setTwoFAConfig(TwoFARespData twoFAConfig) {
        this.twoFAConfig = twoFAConfig;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BalanceChannel{");
        sb.append("balanceInfo=").append(balanceInfo);
        sb.append("twoFAConfig=").append(twoFAConfig);
        sb.append('}');
        return sb.toString();
    }
}
