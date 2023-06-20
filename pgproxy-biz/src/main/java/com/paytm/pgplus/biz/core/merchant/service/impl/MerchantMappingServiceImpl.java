package com.paytm.pgplus.biz.core.merchant.service.impl;

import com.paytm.pgplus.biz.core.merchant.service.IMerchantMappingService;
import com.paytm.pgplus.cache.model.MerchantExtendedInfoResponse;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class MerchantMappingServiceImpl implements IMerchantMappingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantMappingServiceImpl.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(MerchantMappingServiceImpl.class);

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    IMerchantDataService merchantDataService;

    public MerchantExtendedInfoResponse getMerchantInfoResponse(String merchantId) {
        MerchantExtendedInfoResponse merchantExtendedInfoResponse = null;
        if (StringUtils.isNotBlank(merchantId)) {
            try {
                merchantExtendedInfoResponse = merchantDataService.getMerchantExtendedData(merchantId);
                EXT_LOGGER.customInfo("Mapping response - MerchantExtendedInfoResponse :: {}",
                        merchantExtendedInfoResponse);
            } catch (Exception e) {
                LOGGER.warn("Exception occurred while fetching merchantDetails for merchantId- {}", merchantId);
            }
        }
        return merchantExtendedInfoResponse;
    }

}
