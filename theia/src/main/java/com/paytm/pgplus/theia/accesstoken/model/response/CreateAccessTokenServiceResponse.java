package com.paytm.pgplus.theia.accesstoken.model.response;

public class CreateAccessTokenServiceResponse {

    private String token;
    private boolean idempotent;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isIdempotent() {
        return idempotent;
    }

    public void setIdempotent(boolean idempotent) {
        this.idempotent = idempotent;
    }
}
