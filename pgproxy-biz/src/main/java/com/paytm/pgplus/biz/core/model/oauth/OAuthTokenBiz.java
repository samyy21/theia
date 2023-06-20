package com.paytm.pgplus.biz.core.model.oauth;

import java.io.Serializable;

public class OAuthTokenBiz implements Serializable {

    private static final long serialVersionUID = 420273421464702125L;

    private String token;
    private String scope;
    private String resourceOwnerId;
    private Long expiryTime;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getResourceOwnerId() {
        return resourceOwnerId;
    }

    public void setResourceOwnerId(String resourceOwnerId) {
        this.resourceOwnerId = resourceOwnerId;
    }

    public Long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public String toString() {
        return "OAuthTokenBiz [token=" + token + ", scope=" + scope + ", resourceOwnerId=" + resourceOwnerId
                + ", expiryTime=" + expiryTime + "]";
    }

}
