package com.paytm.pgplus.theia.viewmodel;

import java.io.Serializable;

public class EntityChannelInfo implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1093033310218401200L;
    private String channelId = null;
    private String comMode = null;
    private String authMode = null;
    private String paymentType = null;
    private String bankName = null;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getComMode() {
        return comMode;
    }

    public void setComMode(String comMode) {
        this.comMode = comMode;
    }

    public String getAuthMode() {
        return authMode;
    }

    public void setAuthMode(String authMode) {
        this.authMode = authMode;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((channelId == null) ? 0 : channelId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EntityChannelInfo other = (EntityChannelInfo) obj;
        if (channelId == null) {
            if (other.channelId != null)
                return false;
        } else if (!channelId.equals(other.channelId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "EntityChannelInfo [channelId=" + channelId + ", comMode=" + comMode + ", authMode=" + authMode
                + ", paymentType=" + paymentType + ", bankName=" + bankName + "]";
    }

}
