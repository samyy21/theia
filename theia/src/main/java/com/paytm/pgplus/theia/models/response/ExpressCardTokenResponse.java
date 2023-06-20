package com.paytm.pgplus.theia.models.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExpressCardTokenResponse implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -4864192887754658745L;

    @JsonProperty("STATUS")
    private String status;

    @JsonProperty("TOKEN")
    private String token;

    @JsonProperty("ErrorCode")
    private String errorCode;

    @JsonProperty("ErrorMsg")
    private String errorMessage;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ExpressCardTokenResponse [status=").append(status).append(", token=").append(token)
                .append(", errorCode=").append(errorCode).append(", errorMessage=").append(errorMessage).append("]");
        return builder.toString();
    }

}
