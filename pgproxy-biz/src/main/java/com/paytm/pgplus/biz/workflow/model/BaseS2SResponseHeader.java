package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class BaseS2SResponseHeader implements Serializable {

    private static final long serialVersionUID = -8915005939453222549L;

    protected String version;

    protected String responseTimestamp;

    public BaseS2SResponseHeader() {
    }

    public BaseS2SResponseHeader(String version, String responseTimestamp) {
        this.version = version;
        this.responseTimestamp = responseTimestamp;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getResponseTimestamp() {
        return responseTimestamp;
    }

    public void setResponseTimestamp(String responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BaseS2SResponseHeader [version=").append(version).append(", responseTimestamp=")
                .append(responseTimestamp).append("]");
        return builder.toString();
    }

}
