package com.paytm.pgplus.theia.models;

import java.io.Serializable;

public class FetchDeepLinkResponseHeader implements Serializable {

    private static final long serialVersionUID = 7645375557353132181L;

    private Long responseTimestamp;
    private String version;

    public Long getResponseTimestamp() {
        return responseTimestamp;
    }

    public void setResponseTimestamp(Long responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FetchDeepLinkResponseHeader{");
        sb.append("responseTimestamp=").append(responseTimestamp);
        sb.append(", version='").append(version).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
