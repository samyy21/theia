/**
 * 
 */
package com.paytm.pgplus.theia.sessiondata;

import com.dyuproject.protostuff.Tag;

import java.io.Serializable;

/**
 * @createdOn 03-Apr-2016
 * @author kesari
 */
public class ChannelInfo implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -2745388165269097682L;
    @Tag(value = 1)
    private String authMode;
    @Tag(value = 2)
    private String paymentType;

    // Added By Naman
    @Tag(value = 3)
    private String channelID;

    public String getChannelID() {
        return channelID;
    }

    public void setChannelID(String channelID) {
        this.channelID = channelID;
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ChannelInfo [authMode=").append(authMode).append(", paymentType=").append(paymentType)
                .append("]");
        return builder.toString();
    }

}
