package com.paytm.pgplus.theia.offline.model.payview;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by rahulverma on 1/9/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Bank extends PayChannelBase {

    private static final long serialVersionUID = 334126587466226356L;
    private String instId;
    private String instName;

    public Bank() {
    }

    public String getInstId() {
        return this.instId;
    }

    public String getInstName() {
        return this.instName;
    }

    public void setInstId(String instId) {
        this.instId = instId;
    }

    public void setInstName(String instName) {
        this.instName = instName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Bank{");
        sb.append("instId='").append(instId).append('\'');
        sb.append(", instName='").append(instName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
