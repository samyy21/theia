/**
 * 
 */
package com.paytm.pgplus.theia.cache.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.paytm.pgplus.biz.mapping.models.EMIValidBinsData;
import com.paytm.pgplus.biz.mapping.models.EntityIgnoreParamsData;
import com.paytm.pgplus.biz.mapping.models.EntityIgnoreParamsDataResponse;
import com.paytm.pgplus.biz.mapping.models.PaytmDefaultValuesData;
import com.paytm.pgplus.cache.model.*;
import com.paytm.pgplus.cache.util.vault.VaultReadUtil;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.IConfigurationService;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.exceptions.DisasterException;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author riteshkumarsharma
 *
 */
@Service("configurationDataService")
public class ConfigurationDataServiceImpl implements IConfigurationDataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationDataServiceImpl.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(ConfigurationDataServiceImpl.class);

    @Autowired
    @Qualifier("configurationService")
    IConfigurationService configurationService;

    @Autowired
    @Qualifier("vaultReadUtil")
    VaultReadUtil vaultReadUtil;

    @Override
    public PaytmProperty getPaytmProperty(String propertyName) {
        PaytmProperty properties = null;
        try {
            properties = getPaytmProp(propertyName);
        } catch (Exception errorCause) {
            throw new PaymentRequestValidationException("Error while fetching paytm properties from mapping service",
                    errorCause);
        }
        return properties;
    }

    @Override
    public String getPaytmProperty(String key, String defaultValue) {
        PaytmProperty properties = null;
        properties = getPaytmProp(key);
        if (properties != null) {
            return properties.getValue();
        }
        return defaultValue;
    }

    @Override
    public String getPaytmPropertyValue(String key) {
        PaytmProperty properties = null;
        properties = getPaytmProp(key);
        if (properties != null) {
            return properties.getValue();
        }
        return StringUtils.EMPTY;
    }

    private PaytmProperty getPaytmProp(String key) {
        PaytmProperty paytmProperty = null;
        try {

            paytmProperty = vaultReadUtil.getPaytmProperty(key);

            if (paytmProperty != null) {
                return paytmProperty;
            }
            paytmProperty = configurationService.getPaytmProperty(key);
            EXT_LOGGER.customInfo("Mapping response - PaytmProperty :: {}", paytmProperty);

        } catch (Exception e) {
            LOGGER.info("Error in getting paytm property from vault for key:{}", key);
        }
        return paytmProperty;
    }

    @Override
    public PaytmDefaultValuesData getPaytmDefaultValues(String fieldName) {
        try {
            PaytmDefaultValues paytmDefaultValues = configurationService.getPaytmDefaultValues(fieldName);
            EXT_LOGGER.customInfo("Mapping response - PaytmDefaultValues :: {}", paytmDefaultValues);
            if (paytmDefaultValues == null) {
                return null;
            }
            PaytmDefaultValuesData paytmDefaultValuesData = new PaytmDefaultValuesData(
                    paytmDefaultValues.getFieldName(), paytmDefaultValues.getFieldType(),
                    paytmDefaultValues.getStatus());
            return paytmDefaultValuesData;
        } catch (Exception errorCause) {
            LOGGER.error("Error while fetching Paytm Default Values for {} from mapping service", fieldName);
        }
        return null;
    }

    @Override
    public Optional<List<PaytmDefaultValuesData>> getPaytmDefaultValues(List<String> fieldNameList) {
        PaytmDefaultValuesList paytmDefaultValuesList;
        List<PaytmDefaultValuesData> paytmDefaultValuesDataList = new ArrayList<>();
        try {

            paytmDefaultValuesList = configurationService.getPaytmDefaultValuesListV2(fieldNameList);
            EXT_LOGGER.customInfo("Mapping response - PaytmDefaultValuesList :: {}", paytmDefaultValuesList);

            if (paytmDefaultValuesList == null
                    || CollectionUtils.isEmpty(paytmDefaultValuesList.getPaytmDefaultValuesList())) {
                throw new DisasterException("Paytm Default Values Cannot Be Null");
            }
            paytmDefaultValuesList.getPaytmDefaultValuesList().forEach(
                    paytmDefaultValues -> {
                        PaytmDefaultValuesData paytmDefaultValuesData = new PaytmDefaultValuesData(paytmDefaultValues
                                .getFieldName(), paytmDefaultValues.getFieldType(), paytmDefaultValues.getStatus());
                        paytmDefaultValuesDataList.add(paytmDefaultValuesData);
                    });
            return Optional.of(paytmDefaultValuesDataList);
        } catch (Exception errorCause) {
            LOGGER.error("Error while fetching Paytm Default Values from mapping service");
        }
        return Optional.empty();
    }

    @Override
    public EMIValidBinsData getEmiValidBins(String key) {
        try {
            EMIValidBins emiValidBins = configurationService.getEmiValidBins(key);
            EXT_LOGGER.customInfo("Mapping response - EMIValidBins :: {} for key :: {}", emiValidBins, key);
            if (emiValidBins == null || CollectionUtils.isEmpty(emiValidBins.getValidBins())) {
                return null;
            }
            EMIValidBinsData emiValidBinsData = new EMIValidBinsData(emiValidBins.getValidBins());
            return emiValidBinsData;
        } catch (Exception errorCause) {
            LOGGER.error("Error while fetching EMI Valid Bins for {} from mapping service", key, errorCause);
        }
        return null;
    }

    @Override
    public EntityIgnoreParamsDataResponse getEntityIgnoreParams(String entityId) {
        try {
            Long value = Long.parseLong(entityId);
            EntityIgnoreParamsResponse entityIgnoreParamsResponse = configurationService.getEntityIgnoreParams(value);
            EXT_LOGGER.customInfo("Mapping response - EntityIgnoreParamsResponse :: {} for entity ID : {}",
                    entityIgnoreParamsResponse, value);
            if (entityIgnoreParamsResponse != null
                    && CollectionUtils.isNotEmpty(entityIgnoreParamsResponse.getParamsList())) {
                List<EntityIgnoreParamsData> paramList = JsonMapper.convertValue(
                        entityIgnoreParamsResponse.getParamsList(), new TypeReference<List<EntityIgnoreParamsData>>() {
                        });
                EntityIgnoreParamsDataResponse data = new EntityIgnoreParamsDataResponse(paramList);
                return data;
            }
        } catch (Exception errorCause) {
            LOGGER.error("Error while fetching Entity Ignore Params for {} from MS", entityId);
        }
        return null;

    }
}
