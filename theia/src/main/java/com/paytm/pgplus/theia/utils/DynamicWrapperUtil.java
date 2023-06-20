package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.dynamicwrapper.core.config.impl.CacheService;
import com.paytm.pgplus.dynamicwrapper.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.payloadvault.dynamicwrapper.enums.API;
import com.paytm.pgplus.payloadvault.dynamicwrapper.enums.PayloadType;

/**
 * Created by prashant on 6/18/17.
 */
@Component
public class DynamicWrapperUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicWrapperUtil.class);

    @Autowired
    @Qualifier("cacheService")
    private CacheService cacheService;

    public boolean isDynamicWrapperEnabled() {
        String dynamicWrapper = ConfigurationUtil.getProperty("dynamicwrapper.enable", "false");
        LOGGER.info("dynamicwrapper.enable = {}", dynamicWrapper);
        return Boolean.valueOf(dynamicWrapper);
    }

    public boolean isDynamicWrapperConfigPresent(String mid, API api, PayloadType payloadType) {
        String key = CommonUtils.getKey(mid, api, payloadType);
        LOGGER.info("key = {}", key);
        return cacheService.isDynamicWrapperConfigPresent(key);
    }

}
