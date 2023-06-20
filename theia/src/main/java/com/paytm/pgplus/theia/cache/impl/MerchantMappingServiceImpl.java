package com.paytm.pgplus.theia.cache.impl;

import com.paytm.pgplus.common.statistics.StatisticsLogger;
import com.paytm.pgplus.logging.ExtendedLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.cache.model.MerchantInfo;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

/**
 * @author manojpal, Amit D
 *
 */
@Service("merchantMappingService")
public class MerchantMappingServiceImpl implements IMerchantMappingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantMappingServiceImpl.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(MerchantMappingServiceImpl.class);

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    private IMerchantDataService merchantDataService;

    @Override
    public MappingMerchantData getMappingMerchantData(String id) throws PaymentRequestValidationException {
        try {
            MerchantInfo merchantInfo = merchantDataService.getMerchantMappingData(id);
            EXT_LOGGER.customInfo("Mapping response - MerchantInfo :: {}", merchantInfo);
            LOGGER.debug("Merchant info response :: {}", merchantInfo);
            if (null != merchantInfo) {
                MappingMerchantData merchantData = JsonMapper.convertValue(merchantInfo, MappingMerchantData.class);
                LOGGER.debug("Mapped Merchant Data is :: {} ", merchantData);
                return merchantData;
            }
        } catch (Exception errorCause) {
            StatisticsLogger.logForXflush(MDC.get("MID"), "MappingService", "NONE", "response",
                    "Error in fetching merchant detail from MS", errorCause.getMessage());
            throw new PaymentRequestValidationException(
                    "Error while fetching merchantMapping Detail from mapping service for MID:" + id, errorCause);
        }
        return null;
    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CACHE_FETCH_MERCHANTDATA)
    public GenericCoreResponseBean<MappingMerchantData> fetchMerchanData(final String mId) {
        try {
            MappingMerchantData mappingMerchantData = getMappingMerchantData(mId);

            LOGGER.debug("Response from mapping is : {}", mappingMerchantData);
            if (mappingMerchantData != null) {
                return new GenericCoreResponseBean<>(mappingMerchantData);
            }
            LOGGER.debug("Merchant details not found for this MID : {}", mId);
            return new GenericCoreResponseBean<>("Merchant details not found for this MID : " + mId);
        } catch (final Exception e) {
            LOGGER.error("Exception occurred while fetching merchant data from cache/mapping service", e);
            return new GenericCoreResponseBean<>(e.getMessage());
        }
    }
}