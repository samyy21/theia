package com.paytm.pgplus.theia.cache.impl;

import com.paytm.pgplus.cache.model.MerchantExtendedInfoResponse;
import com.paytm.pgplus.cache.model.MerchantPreferenceInfoResponse;
import com.paytm.pgplus.common.statistics.StatisticsLogger;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.theia.cache.model.IPreRedisCacheService;
import com.paytm.pgplus.theia.cache.model.MerchantPreference;
import com.paytm.pgplus.theia.cache.model.MerchantPreferenceStore;
import com.paytm.pgplus.theia.cache.util.MerchantPreferenceUtil;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.CacheConstant;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * @author soniya goyal
 * @date sep 13, 2018
 */

@Service("preRedisCacheServiceImpl")
public class PreRedisCacheServiceImpl implements IPreRedisCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreRedisCacheServiceImpl.class);

    @Autowired
    @Qualifier(value = "merchantDataServiceImpl")
    private IMerchantDataService merchantDataService;

    @Override
    @Cacheable(value = CacheConstant.REDIS_KEYS_CACHE, cacheManager = "timeoutCacheManager", key = "\"M_PREFERENCE_\"+#merchantid")
    public MerchantPreferenceStore getMerchantPreferenceStoreByCache(String merchantid) {
        return getMerchantPreferenceStore(merchantid);
    }

    @Override
    public MerchantPreferenceStore getMerchantPreferenceStoreWithoutCache(String merchantid) {
        return getMerchantPreferenceStore(merchantid);
    }

    private MerchantPreferenceStore getMerchantPreferenceStore(String merchantid) {
        try {
            MerchantPreferenceInfoResponse merchantPreferenceResponse = null;
            merchantPreferenceResponse = merchantDataService.getMerchantPreference(merchantid);
            if (merchantPreferenceResponse != null) {
                LOGGER.debug("Merchant preference found for merchant id : {} ", merchantid);
                return parseResponse(merchantPreferenceResponse);
            }
            LOGGER.error("Merchant preference not found for merchant id : {} ", merchantid);
        } catch (Exception errorCause) {
            if (StringUtils.isBlank(merchantid)) {
                LOGGER.error("Error while fetching merchantPreference Detail from mapping service as mid is null");
            }
            throw new PaymentRequestValidationException(
                    "Error while fetching merchantPreference Detail from mapping service", errorCause,
                    ResponseConstants.INVALID_MID);
        }
        return null;
    }

    /*
     * Function that returns Merchant preference which is in turn stored inside
     * payment request bean.
     */
    public MerchantPreferenceInfoResponse getMerchantPreferenceInfoResponse(String merchantid) {
        if (merchantid == null) {
            LOGGER.error("Merchant Id is null.");
            return null;
        }
        MerchantPreferenceInfoResponse merchantPreferenceResponse = null;
        try {
            merchantPreferenceResponse = merchantDataService.getMerchantPreference(merchantid);
        } catch (Exception errorCause) {
            throw new PaymentRequestValidationException(
                    "Error while fetching merchantPreference Detail from mapping service", errorCause,
                    ResponseConstants.INVALID_MID);
        }
        return merchantPreferenceResponse;
    }

    public MerchantPreferenceStore parseResponse(MerchantPreferenceInfoResponse merchantPreferenceResponse) {
        if (merchantPreferenceResponse != null && merchantPreferenceResponse.getMerchantPreferenceInfos() != null
                && merchantPreferenceResponse.getMerchantPreferenceInfos().size() > 0) {
            MerchantPreferenceStore store = new MerchantPreferenceStore(merchantPreferenceResponse.getMerchantId());

            boolean enabled;

            for (MerchantPreferenceInfoResponse.MerchantPreferenceInfo merchantPreferenceInfo : merchantPreferenceResponse
                    .getMerchantPreferenceInfos()) {
                enabled = false;

                if (MerchantPreferenceUtil.getMerchantPreferenceMap().containsKey(merchantPreferenceInfo.getPrefType())) {
                    enabled = MerchantPreferenceUtil.getMerchantPreferenceMap()
                            .get(merchantPreferenceInfo.getPrefType()).equals(merchantPreferenceInfo.getPrefValue());
                }

                if (TheiaConstant.MerchantPreference.Status.ACTIVE.toString().equals(
                        merchantPreferenceInfo.getPrefStatus())) {
                    MerchantPreference preference = new MerchantPreference();
                    preference.setEnabled(enabled);
                    preference.setPreferenceName(merchantPreferenceInfo.getPrefType());
                    preference.setPreferenceValue(merchantPreferenceInfo.getPrefValue());
                    store.getPreferences().put(preference.getPreferenceName(), preference);
                }
            }
            return store;
        }
        return null;
    }

    @Override
    @Cacheable(value = CacheConstant.REDIS_KEYS_CACHE, cacheManager = "timeoutCacheManager", key = "\"M_EXTENDED_\"+#merchantid")
    public MerchantExtendedInfoResponse getMerchantExtendedDataByCache(String merchantid) {
        return getMerchantExtendedData(merchantid);
    }

    @Override
    @Cacheable(value = CacheConstant.REDIS_KEYS_CACHE, cacheManager = "timeoutCacheManager", key = "\"M_EXTENDED_\"+#merchantId+#clientId")
    public MerchantExtendedInfoResponse getMerchantExtendedDataFromClientIdByCache(String merchantId, String clientId) {
        return getMerchantExtendedDataFromClientId(merchantId, clientId);
    }

    @Override
    public MerchantExtendedInfoResponse getMerchantExtendedDataWithoutCache(String merchantid) {
        return getMerchantExtendedData(merchantid);
    }

    @Override
    public MerchantExtendedInfoResponse getMerchantExtendedDataFromClientIdWithoutCache(String merchantid,
            String clientId) {
        return getMerchantExtendedDataFromClientId(merchantid, clientId);
    }

    private MerchantExtendedInfoResponse getMerchantExtendedData(String merchantid) {
        try {
            MerchantExtendedInfoResponse merchantExtendedInfoResponse = null;
            merchantExtendedInfoResponse = merchantDataService.getMerchantExtendedData(merchantid);
            return merchantExtendedInfoResponse;
        } catch (Exception errorCause) {
            StatisticsLogger.logForXflush(merchantid, "MappingService", "NONE", "response",
                    "Error while fetching extended data from MS", errorCause.getMessage());
            throw new PaymentRequestValidationException(
                    "Error while fetching merchantContract Detail from mapping service", errorCause);
        }
    }

    private MerchantExtendedInfoResponse getMerchantExtendedDataFromClientId(String merchantId, String clientId) {
        try {
            MerchantExtendedInfoResponse merchantExtendedInfoResponse = merchantDataService
                    .getMerchantExtendedDataFromClientId(merchantId, clientId);
            return merchantExtendedInfoResponse;
        } catch (Exception errorCause) {
            throw new PaymentRequestValidationException(
                    "Error while fetching merchantContract Detail from mapping service using clientId: " + clientId,
                    errorCause);
        }
    }

}
