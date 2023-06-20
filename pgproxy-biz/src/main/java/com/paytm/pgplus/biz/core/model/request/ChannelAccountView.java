package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;
import java.util.List;

/**
 * @author charu
 *
 */
public class ChannelAccountView implements Serializable {

    private static final long serialVersionUID = 4493453782303780600L;

    private String payMethod;
    private List<ChannelAccount> channelAccounts;
    private boolean enableStatus;
    private String disableReason;

    private Boolean noInternalAssetAvailable;

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public List<ChannelAccount> getChannelAccounts() {
        return channelAccounts;
    }

    public void setChannelAccounts(List<ChannelAccount> channelAccounts) {
        this.channelAccounts = channelAccounts;
    }

    public boolean isEnableStatus() {
        return enableStatus;
    }

    public void setEnableStatus(boolean enableStatus) {
        this.enableStatus = enableStatus;
    }

    public String getDisableReason() {
        return disableReason;
    }

    public void setDisableReason(String disableReason) {
        this.disableReason = disableReason;
    }

    public Boolean getNoInternalAssetAvailable() {
        return noInternalAssetAvailable;
    }

    public void setNoInternalAssetAvailable(Boolean noInternalAssetAvailable) {
        this.noInternalAssetAvailable = noInternalAssetAvailable;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChannelAccountView{");
        sb.append("payMethod=").append(payMethod);
        sb.append(", channelAccounts=").append(channelAccounts);
        sb.append(", enableStatus=").append(enableStatus);
        sb.append(", disableReason='").append(disableReason).append('\'');
        sb.append(", noInternalAssetAvailable=").append(noInternalAssetAvailable);
        sb.append('}');
        return sb.toString();
    }
}
