/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.mapping.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MappingResultInfo {

    private String resultCode;
    private String resultStatus;
    private String messaage;

    /**
     * @return the resultCode
     */
    @JsonProperty("resultCode")
    public String getResultCode() {
        return resultCode;
    }

    /**
     * @return the resultStatus
     */
    @JsonProperty("resultStatus")
    public String getResultStatus() {
        return resultStatus;
    }

    /**
     * @return the messaage
     */
    @JsonProperty("messaage")
    public String getMessaage() {
        return messaage;
    }

}
