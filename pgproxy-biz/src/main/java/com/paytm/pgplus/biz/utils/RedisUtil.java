package com.paytm.pgplus.biz.utils;

import com.paytm.pgplus.models.CoftConsent;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.COFT_CONSENT_CACHE_KEY;

@Service
public class RedisUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(RedisUtil.class);
    public static final String CASHIER_REQUEST_DATA_CACHE_KEY_PREFIX = "CASHIER_REQUEST_";

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    /**
     * Adds the given cashierId in redis against the given acquirement id.
     *
     * @param acquirementId
     * @param cashierId
     */
    public void pushCashierIdForAcquirementId(String acquirementId, String cashierId) {
        try {
            long expiry = Long.parseLong(ConfigurationUtil.getProperty(BizConstant.CASHIER_REQUEST_DATA_KEY_EXPIRY,
                    "900"));
            theiaTransactionalRedisUtil.set(CASHIER_REQUEST_DATA_CACHE_KEY_PREFIX + acquirementId, cashierId, expiry);
        } catch (Exception e) {
            LOGGER.warn("Error occurred while adding cashier Id to Redis : {}", e.getMessage());
        }
    }

    public void pushOneClickInfoForPaymentRequest(String oneClickKey, String oneClickObject) {
        try {
            long expiry = Long.parseLong(ConfigurationUtil.getProperty(BizConstant.ONE_CLICK_DATA_KEY_EXPIRY, "900"));
            theiaTransactionalRedisUtil.set(oneClickKey, oneClickObject, expiry);
        } catch (Exception e) {
            LOGGER.warn("Error occurred while adding cashier Id to Redis : {}", e.getMessage());
        }
    }

    public String getCashierIdForAcquirementId(String acquirementId) {
        String cashierId = null;
        Object cashierIdObject = theiaTransactionalRedisUtil.get(CASHIER_REQUEST_DATA_CACHE_KEY_PREFIX + acquirementId);
        if (cashierIdObject != null) {
            cashierId = (String) cashierIdObject;
            if (StringUtils.isBlank(cashierId)) {
                cashierId = null;
            }
        }
        return cashierId;
    }

    public void putAcquirementIdTxnTokenNative(String txnToken, String acqId) {
        try {
            if (StringUtils.isNotBlank(txnToken) && StringUtils.isNotBlank(acqId)) {
                theiaTransactionalRedisUtil.hsetIfExist(txnToken, "acquirementId", acqId);
            }
        } catch (Exception e) {
            LOGGER.error("Error adding <acquirementId: {}> to Redis for <txnToken: {}> : {}", acqId, txnToken,
                    e.getMessage());
        }
    }

    public void pushQueryParams(String key, String queryParams) {
        try {
            long expiry = Long
                    .parseLong(ConfigurationUtil.getProperty(BizConstant.QUERY_PARAMS_DATA_KEY_EXPIRY, "900"));
            theiaTransactionalRedisUtil.set(key, queryParams, expiry);
        } catch (Exception e) {
            LOGGER.warn("Error occurred while adding query params to Redis : {}", e.getMessage());
        }
    }

    public void pushCoftConsentForPaymentRequest(String coftConsentKey, CoftConsent coftConsentValue) {
        try {
            long expiry = Long.parseLong(ConfigurationUtil
                    .getProperty(BizConstant.COFT_CONSENT_CACHE_KEY_EXPIRY, "900"));
            theiaTransactionalRedisUtil.set(coftConsentKey, coftConsentValue, expiry);
        } catch (Exception e) {
            LOGGER.warn("Error occurred while adding Coft Consent to Redis : {}", e.getMessage());
        }
    }

}
