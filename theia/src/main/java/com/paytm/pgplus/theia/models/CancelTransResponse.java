package com.paytm.pgplus.theia.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author kartik
 * @date 05-07-2017
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CancelTransResponse implements Serializable {

    private static final long serialVersionUID = 6582168332989322644L;

    private String status;
    private String statusCode;
    private String statusMessage;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CancelTransResponse [status=");
        builder.append(status);
        builder.append(", statusCode=");
        builder.append(statusCode);
        builder.append(", statusMessage=");
        builder.append(statusMessage);
        builder.append("]");
        return builder.toString();
    }

}
