package com.paytm.pgplus.theia.nativ.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;

import java.io.Serializable;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class NativeUserLogoutRequestBody implements Serializable {

    private static final long serialVersionUID = -1246609865412357496L;

    private String referenceId;

    private String mid;

    @JsonIgnore
    private String originalVersion;

    public String getOriginalVersion() {
        return originalVersion;
    }

    public void setOriginalVersion(String originalVersion) {
        this.originalVersion = originalVersion;
    }

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
}
