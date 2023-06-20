package com.paytm.pgplus.theia.acs.models;

import java.io.Serializable;

public class ACSUrlData implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6090650739847628168L;
    private String mid;
    private String orderId;
    private String acsUrlGenerated;
    private String uniqueRandomId;
    private String webFormContext;
    private long timeOfCreation;

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAcsUrlGenerated() {
        return acsUrlGenerated;
    }

    public void setAcsUrlGenerated(String acsUrlGenerated) {
        this.acsUrlGenerated = acsUrlGenerated;
    }

    public String getUniqueRandomId() {
        return uniqueRandomId;
    }

    public void setUniqueRandomId(String uniqueRandomId) {
        this.uniqueRandomId = uniqueRandomId;
    }

    public String getWebFormContext() {
        return webFormContext;
    }

    public void setWebFormContext(String webFormContext) {
        this.webFormContext = webFormContext;
    }

    public long getTimeOfCreation() {
        return timeOfCreation;
    }

    public void setTimeOfCreation(long timeOfCreation) {
        this.timeOfCreation = timeOfCreation;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ACSUrlData [mid=").append(mid).append(", orderId=").append(orderId)
                .append(", acsUrlGenerated=").append(acsUrlGenerated).append(", uniqueRandomId=")
                .append(uniqueRandomId).append(", timeOfCreation=").append(timeOfCreation).append("]");
        return builder.toString();
    }

}
