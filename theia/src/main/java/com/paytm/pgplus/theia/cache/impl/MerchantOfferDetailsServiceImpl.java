/**
 * 
 */
package com.paytm.pgplus.theia.cache.impl;

import com.paytm.pgplus.cache.model.MerchantOfferDetails;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.theia.cache.IMerchantOfferDetailsService;
import com.paytm.pgplus.theia.merchant.models.MappingMerchantOfferDetails;
import com.paytm.pgplus.theia.merchant.models.MerchantOfferDetailsInput;
import com.paytm.pgplus.theia.merchant.models.MerchantOfferDetailsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 
 * @author vaishakhnair
 *
 */
@Service("merchantOfferDetailsService")
public class MerchantOfferDetailsServiceImpl implements IMerchantOfferDetailsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantOfferDetailsServiceImpl.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(MerchantOfferDetailsServiceImpl.class);

    @Autowired
    @Qualifier(value = "merchantDataServiceImpl")
    private IMerchantDataService merchantDataService;

    @Override
    public MerchantOfferDetailsResponse getMerchantOfferDetails(MerchantOfferDetailsInput merchantUrlInput) {
        try {
            MerchantOfferDetails merchantOfferDetails = null;
            if (merchantUrlInput != null && !merchantUrlInput.getWebsite().isEmpty()
                    && !merchantUrlInput.getChannel().name().isEmpty() && !merchantUrlInput.getMerchantId().isEmpty()) {
                merchantOfferDetails = merchantDataService.getMerchantOfferDetails(merchantUrlInput.getMerchantId(),
                        merchantUrlInput.getChannel().name(), merchantUrlInput.getWebsite());
                EXT_LOGGER.customInfo(
                        "Mapping response - MerchantOfferDetails :: {} for MID :: {} Channel :: {} Website :: {}",
                        merchantOfferDetails, merchantUrlInput.getMerchantId(), merchantUrlInput.getChannel().name(),
                        merchantUrlInput.getWebsite());
            }
            if (null != merchantOfferDetails) {
                LOGGER.info("Merchant offer details found for merchant id : {}", merchantUrlInput.getMerchantId());
                MappingMerchantOfferDetails mappingMerchantUrlInfo = new MappingMerchantOfferDetails(
                        merchantOfferDetails.getMerchantId(), merchantOfferDetails.getChannel(),
                        merchantOfferDetails.getWebsite(), merchantOfferDetails.getStatus(),
                        merchantOfferDetails.getMessage(), merchantOfferDetails.getMid(),
                        merchantOfferDetails.getCreatedDate(), merchantOfferDetails.getModifiedDate(),
                        merchantOfferDetails.getValidFrom(), merchantOfferDetails.getValidTo());

                MerchantOfferDetailsResponse merchantOfferDetailsResponse = new MerchantOfferDetailsResponse();

                merchantOfferDetailsResponse.setMappingMerchantOfferDetails(mappingMerchantUrlInfo);
                merchantOfferDetailsResponse.setSuccessfullyProcessed(true);

                LOGGER.debug("Response received : {}", merchantOfferDetailsResponse);
                return merchantOfferDetailsResponse;
            }
        } catch (Exception errorCause) {
            LOGGER.error("Error while fetching MerchantOffer Detail from mapping service : {}",
                    merchantUrlInput.getMerchantId());
        }
        return null;
    }

}
