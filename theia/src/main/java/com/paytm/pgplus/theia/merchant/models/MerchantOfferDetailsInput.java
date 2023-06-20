package com.paytm.pgplus.theia.merchant.models;

import com.paytm.pgplus.theia.merchant.models.MappingMerchantOfferDetails.Channel;

public class MerchantOfferDetailsInput {

    private String merchantId;
    private MappingMerchantOfferDetails.Channel channel;
    private String website;

    /**
     * @param merchantId
     * @param channel
     * @param website
     */
    public MerchantOfferDetailsInput(String merchantId, Channel channel, String website) {
        super();
        this.merchantId = merchantId;
        this.channel = channel;
        this.website = website;
    }

    /**
     * @return the merchantId
     */
    public String getMerchantId() {
        return merchantId;
    }

    /**
     * @return the channel
     */
    public MappingMerchantOfferDetails.Channel getChannel() {
        return channel;
    }

    /**
     * @return the website
     */
    public String getWebsite() {
        return website;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((channel == null) ? 0 : channel.hashCode());
        result = prime * result + ((merchantId == null) ? 0 : merchantId.hashCode());
        result = prime * result + ((website == null) ? 0 : website.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MerchantOfferDetailsInput other = (MerchantOfferDetailsInput) obj;
        if (channel != other.channel)
            return false;
        if (merchantId == null) {
            if (other.merchantId != null)
                return false;
        } else if (!merchantId.equals(other.merchantId))
            return false;
        if (website == null) {
            if (other.website != null)
                return false;
        } else if (!website.equals(other.website))
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MerchantOfferDetailsInput [merchantId=").append(merchantId).append(", channel=")
                .append(channel).append(", website=").append(website).append("]");
        return builder.toString();
    }

}
