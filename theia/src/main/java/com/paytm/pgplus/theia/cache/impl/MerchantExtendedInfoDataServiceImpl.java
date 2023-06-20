package com.paytm.pgplus.theia.cache.impl;

import com.paytm.pgplus.theia.cache.model.IPreRedisCacheService;
import com.paytm.pgplus.theia.helper.PreRedisCacheHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.cache.model.MerchantExtendedInfoResponse;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.cache.IMerchantExtendedInfoDataService;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.merchant.models.TheiaMerchantExtendedDataResponse;

/**
 * @author manojpal
 *
 */
@Service("merchantExtendedInfoDataService")
public class MerchantExtendedInfoDataServiceImpl implements IMerchantExtendedInfoDataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantExtendedInfoDataServiceImpl.class);

    @Autowired
    private PreRedisCacheHelper preRedisCacheHelper;

    @Override
    public TheiaMerchantExtendedDataResponse getMerchantExtendedInfoData(String merchantid) {
        try {
            MerchantExtendedInfoResponse merchantExtendedInfoResponse = preRedisCacheHelper
                    .getMerchantExtendedData(merchantid);
            LOGGER.debug("MerchantExtendedInfoResponse received : {}", merchantExtendedInfoResponse);
            if (merchantExtendedInfoResponse != null) {
                TheiaMerchantExtendedDataResponse theiaMerchantExtendedDataResponse = JsonMapper.convertValue(
                        merchantExtendedInfoResponse, TheiaMerchantExtendedDataResponse.class);
                return theiaMerchantExtendedDataResponse;
            }
        } catch (Exception errorCause) {
            LOGGER.error(errorCause.getMessage());
            throw new PaymentRequestValidationException(
                    "Error while fetching merchantContract Detail from guava cache", errorCause);
        }
        return null;
    }

    @Override
    public TheiaMerchantExtendedDataResponse getMerchantExtendedInfoDataFromClientId(String merchantId, String clientId) {
        try {
            MerchantExtendedInfoResponse merchantExtendedInfoResponse = preRedisCacheHelper
                    .getMerchantExtendedDataFromClientId(merchantId, clientId);
            LOGGER.debug("MerchantExtendedInfoResponse received : {}", merchantExtendedInfoResponse);
            if (merchantExtendedInfoResponse != null) {
                TheiaMerchantExtendedDataResponse theiaMerchantExtendedDataResponse = JsonMapper.convertValue(
                        merchantExtendedInfoResponse, TheiaMerchantExtendedDataResponse.class);
                return theiaMerchantExtendedDataResponse;
            }
        } catch (Exception errorCause) {
            LOGGER.error(errorCause.getMessage());
            throw new PaymentRequestValidationException(
                    "Errgaor while fetching merchantContract Detail from guava cache using ClientId", errorCause);
        }
        return null;
    }

    @Override
    public GenericCoreResponseBean<TheiaMerchantExtendedDataResponse> getMerchanExtendedDataFromCache(final String mid) {
        try {
            final TheiaMerchantExtendedDataResponse theiaMerchantDataResponse = getMerchantExtendedInfoData(mid);
            if ((theiaMerchantDataResponse != null) && (theiaMerchantDataResponse.getExtendedInfo() != null)) {
                return new GenericCoreResponseBean<>(theiaMerchantDataResponse);
            }
            LOGGER.debug("Merchant details not found for this MID : {}", mid);
            return new GenericCoreResponseBean<>("Merchant details not found for this MID : " + mid);

        } catch (final Exception e) {
            LOGGER.error("Exception occurred while fetching merchant data from cache/mapping service", e);
            return new GenericCoreResponseBean<>(e.getMessage());
        }
    }
}
