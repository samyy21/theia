package com.paytm.pgplus.theia.models;

import java.io.Serializable;

/**
 * @author Santosh chourasia
 *
 */
public class UPIPSPResponseHeader implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 9034875557353132181L;

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
        StringBuilder builder = new StringBuilder();
        builder.append("UPIPSPResponseHeader [responseTimestamp=").append(responseTimestamp).append(", version=")
                .append(version).append("]");
        return builder.toString();
    }

}
