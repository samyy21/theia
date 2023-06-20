package com.paytm.pgplus.theia.s2s.models.request;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentS2SRequestHeader extends SecureS2SRequestHeader {

    private static final long serialVersionUID = 968687291311099310L;

    @NotEmpty(message = "channel cannot be blank")
    private String channelId;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PaymentS2SRequestHeader [channelId=").append(channelId).append(", clientId=").append(clientId)
                .append(", signature=").append(signature).append(", version=").append(version)
                .append(", requestTimestamp=").append(requestTimestamp).append("]");
        return builder.toString();
    }

}
