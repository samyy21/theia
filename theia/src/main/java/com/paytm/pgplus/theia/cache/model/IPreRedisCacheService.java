package com.paytm.pgplus.theia.cache.model;

import com.paytm.pgplus.cache.model.MerchantExtendedInfoResponse;
import com.paytm.pgplus.cache.model.MerchantPreferenceInfoResponse;

/**
 * @author soniya goyal
 * @date sept 3, 2018
 */

public interface IPreRedisCacheService {

    MerchantPreferenceStore getMerchantPreferenceStoreByCache(String id);

    MerchantPreferenceStore getMerchantPreferenceStoreWithoutCache(String id);

    MerchantPreferenceInfoResponse getMerchantPreferenceInfoResponse(String merchantid);

    MerchantPreferenceStore parseResponse(MerchantPreferenceInfoResponse merchantPreferenceResponse);

    MerchantExtendedInfoResponse getMerchantExtendedDataByCache(String id);

    MerchantExtendedInfoResponse getMerchantExtendedDataWithoutCache(String id);

    MerchantExtendedInfoResponse getMerchantExtendedDataFromClientIdByCache(String id, String clientId);

    MerchantExtendedInfoResponse getMerchantExtendedDataFromClientIdWithoutCache(String id, String clientId);
}
