/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.mapping.models;

public class MappingServiceRequestHeader {

    private String clientId;
    private Long reqTime;
    private String accessToken;
    private String reserve;

    /**
     * @return the clientId
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * @return the respTime
     */
    public Long getReqTime() {
        return reqTime;
    }

    /**
     * @return the accessToken
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * @return the reserve
     */
    public String getReserve() {
        return reserve;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("MappingServiceRequestHeader [clientId=").append(clientId).append(", reqTime=").append(reqTime)
                .append(", accessToken=").append(accessToken).append(", reserve=").append(reserve).append("]");
        return builder.toString();
    }

    /**
     * @param clientId
     *            the clientId to set
     */
    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    /**
     * @param reqTime
     *            the reqTime to set
     */
    public void setReqTime(final Long reqTime) {
        this.reqTime = reqTime;
    }

    /**
     * @param accessToken
     *            the accessToken to set
     */
    public void setAccessToken(final String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * @param reserve
     *            the reserve to set
     */
    public void setReserve(final String reserve) {
        this.reserve = reserve;
    }

}
