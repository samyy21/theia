package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.facade.wallet.models.UserWalletFreezeDetails;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleField;

import java.io.Serializable;

public class ActiveInfo implements Serializable {

    private static final long serialVersionUID = 1745019573708449216L;

    private boolean status;
    @LocaleField
    private String msg;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("isNewWalletUser")
    private Boolean newWalletUser;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("isLimitApplicable")
    private Boolean walletLimitBreached;

    private UserWalletFreezeDetails userWalletFreezeDetails;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Boolean getNewWalletUser() {
        return newWalletUser;
    }

    public void setNewWalletUser(Boolean newWalletUser) {
        this.newWalletUser = newWalletUser;
    }

    public Boolean getWalletLimitBreached() {
        return walletLimitBreached;
    }

    public void setWalletLimitBreached(Boolean walletLimitBreached) {
        this.walletLimitBreached = walletLimitBreached;
    }

    public UserWalletFreezeDetails getUserWalletFreezeDetails() {
        return userWalletFreezeDetails;
    }

    public void setUserWalletFreezeDetails(UserWalletFreezeDetails userWalletFreezeDetails) {
        this.userWalletFreezeDetails = userWalletFreezeDetails;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("isActive{");
        sb.append("status='").append(status).append('\'');
        sb.append(", msg='").append(msg).append('\'');
        sb.append(", isNewWalletUser='").append(newWalletUser).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
