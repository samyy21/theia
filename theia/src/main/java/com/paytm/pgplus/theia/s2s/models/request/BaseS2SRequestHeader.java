package com.paytm.pgplus.theia.s2s.models.request;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseS2SRequestHeader implements Serializable {

    private static final long serialVersionUID = 1581467989770388347L;

    @NotEmpty(message = "version cannot be blank")
    protected String version;

    protected String requestTimestamp;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(String requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BaseS2SRequestHeader [version=").append(version).append(", requestTimestamp=")
                .append(requestTimestamp).append("]");
        return builder.toString();
    }

}
