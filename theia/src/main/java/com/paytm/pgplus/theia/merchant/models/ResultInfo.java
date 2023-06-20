package com.paytm.pgplus.theia.merchant.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author manojpal
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultInfo {

    private String resultCode;
    private String resultStatus;
    private String message;

    @JsonProperty(value = "resultCode")
    public String getResultCode() {
        return resultCode;
    }

    @JsonProperty(value = "resultStatus")
    public String getResultStatus() {
        return resultStatus;
    }

    @JsonProperty(value = "messaage")
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "BizResultInfo{" + "resultCode='" + resultCode + '\'' + ", resultStatus='" + resultStatus + '\''
                + ", message='" + message + '\'' + '}';
    }

}
