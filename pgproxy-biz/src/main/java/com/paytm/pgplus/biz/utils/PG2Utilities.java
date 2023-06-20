package com.paytm.pgplus.biz.utils;

import com.paytm.pgplus.cache.model.PerfernceInfo;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("pg2Utilities")
public class PG2Utilities {

    @Autowired
    private IMerchantDataService merchantDataService;

    @Autowired
    Ff4jUtils ff4jUtils;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(PG2Utilities.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(PG2Utilities.class);
    private static final String DISABLE_PG2_PHASE3_RETRY = "theia.DISABLE_PG2_PHASE3_RETRY";
    private static final String DISABLE_PG2_NON_PHASE3_RETRY = "theia.DISABLE_PG2_NON_PHASE3_RETRY";

    public boolean disablePg2Retry(String mid, boolean isFullPg2TrafficEnabled) {
        if (isFullPg2TrafficEnabled && ff4jUtils.isFeatureEnabledOnMid(mid, DISABLE_PG2_PHASE3_RETRY, false)) {
            return true;
        } else if (!isFullPg2TrafficEnabled
                && ff4jUtils.isFeatureEnabledOnMid(mid, DISABLE_PG2_NON_PHASE3_RETRY, false)) {
            return true;
        }
        return false;
    }

    public void setPg2PaymentCount(String txnToken) {
        if (txnToken == null) {
            LOGGER.info("Received txnToken null while setting pg2PaymentCount");
        }
        try {
            theiaSessionRedisUtil.hsetIfExist(txnToken, "pg2PaymentCount", 1);
        } catch (Exception e) {
            LOGGER.info("Exception while setting pg2PaymentCount in redis: {}", e.getMessage());
        }
    }
}
