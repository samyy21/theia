package com.paytm.pgplus.biz.fetchBulkCardIndexNumber.model;

import com.paytm.pgplus.facade.user.models.response.CacheCardResponseBody;

import java.io.Serializable;

public class NativeCardIndexNumberResponse implements Serializable {
    private static final long serialVersionUID = 2880978426862109310L;
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
