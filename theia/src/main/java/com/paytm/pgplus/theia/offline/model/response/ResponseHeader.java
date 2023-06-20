/*
 * {user}
 * {date}
 */

package com.paytm.pgplus.theia.offline.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.offline.model.base.BaseHeader;

/**
 * Created by rahulverma on 1/9/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseHeader extends BaseHeader {

    private static final long serialVersionUID = -4665956794750942617L;

    private String responseTimestamp;

    public ResponseHeader() {

    }

    public ResponseHeader(String mid, String clientId, String version, String requestId, String responseTimestamp) {
        super(mid, clientId, version, requestId);
        this.responseTimestamp = responseTimestamp;
    }

    public ResponseHeader(BaseHeader baseHeader) {
        super(baseHeader.getMid(), baseHeader.getClientId(), baseHeader.getVersion(), baseHeader.getRequestId());
        this.responseTimestamp = String.valueOf(System.currentTimeMillis());
    }

    public String getResponseTimestamp() {
        return responseTimestamp;
    }

    public void setResponseTimestamp(long responseTimestampInMilis) {

        this.responseTimestamp = String.valueOf(responseTimestampInMilis);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ResponseHeader{");
        sb.append("responseTimestamp='").append(responseTimestamp).append('\'');
        sb.append(super.toString());
        sb.append('}');
        return sb.toString();
    }
}
