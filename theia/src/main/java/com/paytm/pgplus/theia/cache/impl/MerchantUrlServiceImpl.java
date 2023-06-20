/**
 * 
 */
package com.paytm.pgplus.theia.cache.impl;

import com.paytm.pgplus.cache.model.MerchantApiUrlInfo;
import com.paytm.pgplus.cache.model.MerchantApiUrlInfoResponse;
import com.paytm.pgplus.cache.model.MerchantUrlInfo;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.pgproxycommon.models.MappingMerchantUrlInfo;
import com.paytm.pgplus.pgproxycommon.models.MerchantUrlInput;
import com.paytm.pgplus.theia.cache.IMerchantUrlService;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.logging.ExceptionLogUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 
 * @author vaishakhnair
 *
 */
@Service("merchantUrlService")
public class MerchantUrlServiceImpl implements IMerchantUrlService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantUrlServiceImpl.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(MerchantUrlServiceImpl.class);
    private static final PaymentRequestValidationException urlInfoException = new PaymentRequestValidationException(
            "Error while fetching merchant url info from mapping service");

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    private IMerchantDataService merchantDataService;

    @Override
    public MappingMerchantUrlInfo getMerchantUrlInfo(MerchantUrlInput merchantUrlInput) {
        try {
            if (!StringUtils.isBlank(merchantUrlInput.getWebsite())) {
                MerchantUrlInfo merchantUrlInfo = merchantDataService.getMerchantUrlInfo(
                        merchantUrlInput.getMerchantId(), merchantUrlInput.getUrlTypeId().name(),
                        merchantUrlInput.getWebsite());
                EXT_LOGGER.customInfo(
                        "Mapping response - MerchantUrlInfo :: {} for MID : {} UrlTypeId : {}, Website : {}",
                        merchantUrlInfo, merchantUrlInput.getMerchantId(), merchantUrlInput.getUrlTypeId().name(),
                        merchantUrlInput.getWebsite());
                LOGGER.debug("Mapping Service response : {}", merchantUrlInfo);
                if (merchantUrlInfo != null) {
                    MappingMerchantUrlInfo mappingMerchantUrlInfo = JsonMapper.convertValue(merchantUrlInfo,
                            MappingMerchantUrlInfo.class);
                    return mappingMerchantUrlInfo;
                }
                LOGGER.error(
                        "No response returned from mapping service for merchantId : {} , urlTypeId : {} , website : {} ",
                        merchantUrlInput.getMerchantId(), merchantUrlInput.getUrlTypeId().name(),
                        merchantUrlInput.getWebsite());
            }

            LOGGER.warn("Merchant website not available for merchant id : {}", merchantUrlInput.getMerchantId());
            return null;
        } catch (Exception errorCause) {
            // PGP-31371 Logs removal activity for theia
            LOGGER.error("Error while fetching merchant url info from mapping service:{}, Exception : {} ",
                    merchantUrlInput, errorCause.getMessage());
            throw urlInfoException;
        }
    }

    @Override
    public MappingMerchantUrlInfo getMerchantUrlInfoV2(MerchantUrlInput merchantUrlInput) {
        try {
            if (!StringUtils.isBlank(merchantUrlInput.getWebsite())) {
                MerchantUrlInfo merchantUrlInfo = merchantDataService.getMerchantUrlInfoV2(
                        merchantUrlInput.getMerchantId(), merchantUrlInput.getUrlTypeId().name(),
                        merchantUrlInput.getWebsite());
                EXT_LOGGER.customInfo(
                        "Mapping response - MerchantUrlInfo :: {} for MID : {} UrlTypeId : {}, Website : {}",
                        merchantUrlInfo, merchantUrlInput.getMerchantId(), merchantUrlInput.getUrlTypeId().name(),
                        merchantUrlInput.getWebsite());
                LOGGER.debug("Mapping Service response : {}", merchantUrlInfo);
                if (merchantUrlInfo != null) {
                    MappingMerchantUrlInfo mappingMerchantUrlInfo = JsonMapper.convertValue(merchantUrlInfo,
                            MappingMerchantUrlInfo.class);
                    return mappingMerchantUrlInfo;
                }
                LOGGER.error(
                        "No response returned from mapping service for merchantId : {} , urlTypeId : {} , website : {} ",
                        merchantUrlInput.getMerchantId(), merchantUrlInput.getUrlTypeId().name(),
                        merchantUrlInput.getWebsite());
            }

            LOGGER.warn("Merchant website not available for merchant id : {}", merchantUrlInput.getMerchantId());
            return null;
        } catch (Exception errorCause) {
            // PGP-31371 Logs removal activity for theia
            LOGGER.error("Error while fetching merchant url info from mapping service:{}, Exception : {} ",
                    merchantUrlInput, errorCause.getMessage());
            throw urlInfoException;
        }
    }

    @Override
    public List<MerchantApiUrlInfo> getMerchantApiUrlInfo(String midType, String mid) {
        try {
            MerchantApiUrlInfoResponse merchantApiUrlInfoResponse = merchantDataService.getMerchantApiUrlInfo(midType,
                    mid);
            EXT_LOGGER.customInfo("Mapping response - MerchantApiUrlInfoResponse :: {}", merchantApiUrlInfoResponse);
            if (merchantApiUrlInfoResponse == null) {
                LOGGER.error("No response returned for MerchantApiUrlInfo from mapping service for merchantId : {}",
                        mid);
                return null;
            }
            if (!StringUtils.equals(mid, merchantApiUrlInfoResponse.getMerchantId())) {
                LOGGER.error("mid in merchantApiUrlInfoResponse:{} does not match with requested mid:{}",
                        merchantApiUrlInfoResponse.getMerchantId(), mid);
                return null;
            }
            if ((merchantApiUrlInfoResponse.getMerchantApiUrlInfoList() == null || merchantApiUrlInfoResponse
                    .getMerchantApiUrlInfoList().isEmpty())) {
                LOGGER.error("MerchantApiUrlInfoList is null/empty for mid:{}", mid);
                return null;
            }
            LOGGER.info("response received for getMerchantApiUrlInfo for mid:{}, {}", mid, merchantApiUrlInfoResponse);
            return merchantApiUrlInfoResponse.getMerchantApiUrlInfoList();

        } catch (Exception errorCause) {
            // LOGGER.error("Error while fetching merchant api url info from mapping service: ",
            // mid, errorCause);
            LOGGER.error("Error while fetching merchant api url info from mapping service: ", mid,
                    ExceptionLogUtils.limitLengthOfStackTrace(errorCause));
            return null;
        }
    }
}
