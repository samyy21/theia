package com.paytm.pgplus.biz.enums;

import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum AoaTimeoutCenterUrl {
    AOA_TIMEOUT_CENTER_ORDER_URL("/timeoutcenter/orders");

    private static final Logger LOGGER = LoggerFactory.getLogger(AoaTimeoutCenterUrl.class);
    private String url;

    AoaTimeoutCenterUrl(final String url) {
        this.url = url;
    }

    public String getUrl() {
        return ConfigurationUtil.getTheiaProperty("aoa.timeoutcenter.service.aws.base.url") + url;
    }
}
