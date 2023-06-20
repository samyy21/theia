/**
 * 
 */
package com.paytm.pgplus.theia.cache;

import com.paytm.pgplus.cache.model.MerchantApiUrlInfo;
import com.paytm.pgplus.pgproxycommon.models.MappingMerchantUrlInfo;
import com.paytm.pgplus.pgproxycommon.models.MerchantUrlInput;

import java.util.List;

/**
 * @author amitdubey
 * @date Jan 13, 2017
 */
public interface IMerchantUrlService {

    MappingMerchantUrlInfo getMerchantUrlInfo(MerchantUrlInput merchantUrlInput);

    MappingMerchantUrlInfo getMerchantUrlInfoV2(MerchantUrlInput merchantUrlInput);

    List<MerchantApiUrlInfo> getMerchantApiUrlInfo(String midType, String mid);
}
