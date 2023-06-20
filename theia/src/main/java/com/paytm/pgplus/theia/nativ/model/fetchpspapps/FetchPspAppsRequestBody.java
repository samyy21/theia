package com.paytm.pgplus.theia.nativ.model.fetchpspapps;

import java.io.Serializable;

public class FetchPspAppsRequestBody implements Serializable {

    private String mid;

    private String referenceId;

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    @Override
    public String toString() {
        return "FetchPspAppsrequestBody{" + "mid='" + mid + '\'' + ", referenceId='" + referenceId + '\'' + '}';
    }
}
