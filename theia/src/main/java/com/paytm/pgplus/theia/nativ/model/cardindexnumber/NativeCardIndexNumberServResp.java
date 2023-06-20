package com.paytm.pgplus.theia.nativ.model.cardindexnumber;

import com.paytm.pgplus.facade.user.models.response.CacheCardResponseBody;

import java.io.Serializable;

public class NativeCardIndexNumberServResp implements Serializable {

    private final static long serialVersionUID = -7565152268975544046L;

    private CacheCardResponseBody cacheCardResponseBody;

    public CacheCardResponseBody getCacheCardResponseBody() {
        return cacheCardResponseBody;
    }

    public void setCacheCardResponseBody(CacheCardResponseBody cacheCardResponseBody) {
        this.cacheCardResponseBody = cacheCardResponseBody;
    }

    @Override
    public String toString() {
        return "NativeCardIndexNumberServResp{" + "cacheCardResponseBody=" + cacheCardResponseBody + '}';
    }
}
