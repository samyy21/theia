package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;

public class HeadResponse implements Serializable {

    private static final long serialVersionUID = -6716184569418164216L;

    private String responseTimestamp;

    public String getResponseTimestamp() {
        return responseTimestamp;
    }

    @Override
    public String toString() {
        return "HeadResponse{" + "responseTimestamp='" + responseTimestamp + '\'' + '}';
    }

    public void setResponseTimestamp(String responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }
}
