/**
 * 
 */
package com.paytm.pgplus.theia.cache;

import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.merchant.models.TheiaMerchantExtendedDataResponse;

/**
 * @author amitdubey
 * @date Jan 13, 2017
 */
public interface IMerchantExtendedInfoDataService {

    TheiaMerchantExtendedDataResponse getMerchantExtendedInfoData(String merchantid);

    TheiaMerchantExtendedDataResponse getMerchantExtendedInfoDataFromClientId(String merchantid, String clientId);

    GenericCoreResponseBean<TheiaMerchantExtendedDataResponse> getMerchanExtendedDataFromCache(String mid);

}
