/*
 * {user}
 * {date}
 */

package com.paytm.pgplus.theia.offline.model.base;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by rahulverma on 30/8/17.
 */

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseHeader implements Serializable {

    private static final long serialVersionUID = 304117746744759743L;
    @ApiModelProperty(required = true)
    @NotBlank
    private String mid;
    @ApiModelProperty(required = true)
    @NotBlank
    private String clientId;
    @ApiModelProperty(required = true)
    @NotBlank
    private String version;

    @ApiModelProperty(required = true)
    @NotBlank
    private String requestId;

    public BaseHeader() {

    }

    public BaseHeader(String mid, String clientId, String version, String requestId) {
        this.mid = mid;
        this.clientId = clientId;
        this.version = version;
        this.requestId = requestId;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BaseHeader{");
        sb.append("mid='").append(mid).append('\'');
        sb.append(", clientId='").append(clientId).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append(", requestId='").append(requestId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
