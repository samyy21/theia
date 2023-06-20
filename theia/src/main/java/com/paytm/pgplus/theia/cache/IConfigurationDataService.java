/**
 * 
 */
package com.paytm.pgplus.theia.cache;

import com.paytm.pgplus.biz.mapping.models.EMIValidBinsData;
import com.paytm.pgplus.biz.mapping.models.EntityIgnoreParamsDataResponse;
import com.paytm.pgplus.biz.mapping.models.PaytmDefaultValuesData;
import com.paytm.pgplus.cache.model.PaytmProperty;

import java.util.List;
import java.util.Optional;

/**
 * @author riteshkumarsharma
 *
 */
public interface IConfigurationDataService {

    PaytmProperty getPaytmProperty(String propertyName);

    String getPaytmProperty(String key, String defaultValue);

    String getPaytmPropertyValue(String oauthClientId);

    EMIValidBinsData getEmiValidBins(String key);

    PaytmDefaultValuesData getPaytmDefaultValues(String fieldName);

    Optional<List<PaytmDefaultValuesData>> getPaytmDefaultValues(List<String> fieldNameList);

    EntityIgnoreParamsDataResponse getEntityIgnoreParams(String entityId);

}
