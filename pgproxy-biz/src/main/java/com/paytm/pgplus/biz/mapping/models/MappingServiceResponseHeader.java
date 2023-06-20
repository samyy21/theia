/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.mapping.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MappingServiceResponseHeader {

    private String clientId;
    private Long respTime;
    private String accessToken;
    private String reserve;

    /**
     * @return the clientId
     */
    @JsonProperty("clientId")
    public String getClientId() {
        return clientId;
    }

    /**
     * @return the respTime
     */
    @JsonProperty("respTime")
    public Long getRespTime() {
        return respTime;
    }

    /**
     * @return the accessToken
     */
    @JsonProperty("accessToken")
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * @return the reserve
     */
    @JsonProperty("reserve")
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
        builder.append("MappingServiceResponseHeader [clientId=").append(clientId).append(", respTime=")
                .append(respTime).append(", accessToken=").append(accessToken).append(", reserve=").append(reserve)
                .append("]");
        return builder.toString();
    }

}
