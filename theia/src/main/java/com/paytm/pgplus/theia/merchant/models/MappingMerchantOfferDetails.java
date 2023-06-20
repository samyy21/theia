package com.paytm.pgplus.theia.merchant.models;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MappingMerchantOfferDetails implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -2014839322654631813L;
    private String merchantId;
    private Channel channel;
    private String website;
    private Status status;
    private String message;
    private String mid;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private Date createdDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private Date modifiedDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private Date validFrom;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private Date validTo;

    public static enum Channel {
        WEB, WAP, SYSTEM;
    }

    public static enum Status {
        ACTIVE("9376503"), INACTIVE("9376504");

        private String code;

        Status(String code) {
            this.code = code;
        }

        private static Status getStatusByCode(String code) {
            for (Status status : Status.values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            return null;
        }
    }

    public MappingMerchantOfferDetails(String merchantId, String channel, String website, String status,
            String message, String mid, Date createdDate, Date modifiedDate, Date validFrom, Date validTo) {
        this.merchantId = merchantId;
        this.channel = getChannelbyName(channel);
        this.website = website;
        this.status = Status.getStatusByCode(status);
        this.message = message;
        this.mid = mid;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.validFrom = validFrom;
        this.validTo = validTo;
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
    public Channel getChannel() {
        return channel;
    }

    /**
     * @return the website
     */
    public String getWebsite() {
        return website;
    }

    /**
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the mid
     */
    public String getMid() {
        return mid;
    }

    /**
     * @return the createdDate
     */
    public Date getCreatedDate() {
        return createdDate;
    }

    /**
     * @return the modifiedDate
     */
    public Date getModifiedDate() {
        return modifiedDate;
    }

    /**
     * @return the validFrom
     */
    public Date getValidFrom() {
        return validFrom;
    }

    /**
     * @return the validTo
     */
    public Date getValidTo() {
        return validTo;
    }

    public Channel getChannelbyName(String name) {
        for (Channel ct : Channel.values()) {
            if (ct.name().equalsIgnoreCase(name)) {
                return ct;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "MappingMerchantOfferDetails [merchantId=" + merchantId + ", channel=" + channel + ", website="
                + website + ", status=" + status + ", message=" + message + ", mid=" + mid + ", createdDate="
                + createdDate + ", modifiedDate=" + modifiedDate + ", validFrom=" + validFrom + ", validTo=" + validTo
                + "]";
    }
}
