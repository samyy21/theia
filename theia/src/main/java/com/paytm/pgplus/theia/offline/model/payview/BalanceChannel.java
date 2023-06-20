package com.paytm.pgplus.theia.offline.model.payview;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by rahulverma on 1/9/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BalanceChannel extends PayChannelBase {

    private static final long serialVersionUID = -8837876317528333510L;
    @NotNull
    @Valid
    private BalanceInfo balanceInfo;

    public BalanceChannel() {
    }

    public BalanceChannel(BalanceInfo balanceInfo) {
        this.balanceInfo = balanceInfo;
    }

    public BalanceChannel(String payMethod, String payChannelOption, StatusInfo isDisabled, StatusInfo hasLowSuccess,
            String iconUrl, BalanceInfo balanceInfo) {
        super(payMethod, payChannelOption, isDisabled, hasLowSuccess, iconUrl);
        this.balanceInfo = balanceInfo;
    }

    public BalanceInfo getBalanceInfo() {
        return balanceInfo;
    }

    public void setBalanceInfo(BalanceInfo balanceInfo) {
        this.balanceInfo = balanceInfo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BalanceChannel{");
        sb.append("balanceInfo=").append(balanceInfo);
        sb.append('}');
        return sb.toString();
    }
}
