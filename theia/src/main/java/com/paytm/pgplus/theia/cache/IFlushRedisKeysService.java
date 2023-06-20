package com.paytm.pgplus.theia.cache;

import javax.servlet.http.HttpServletRequest;

public interface IFlushRedisKeysService {

    void flushRedisKeys(HttpServletRequest request);
}
