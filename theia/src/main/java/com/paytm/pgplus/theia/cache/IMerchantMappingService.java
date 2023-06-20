/**
 * 
 */
package com.paytm.pgplus.theia.cache;

import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;

/**
 * @author amitdubey
 * @date Jan 13, 2017
 */
public interface IMerchantMappingService {

    MappingMerchantData getMappingMerchantData(String id) throws PaymentRequestValidationException;

    GenericCoreResponseBean<MappingMerchantData> fetchMerchanData(String mId);

}
