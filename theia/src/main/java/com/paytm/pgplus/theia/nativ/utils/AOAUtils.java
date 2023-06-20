package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.cache.model.AoaGatewayConfigInfoResponse;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.IAoaGatewayService;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.cache.IMerchantExtendedInfoDataService;
import com.paytm.pgplus.theia.merchant.models.TheiaMerchantExtendedDataResponse;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("aoaUtils")
public class AOAUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AOAUtils.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(AOAUtils.class);

    @Autowired
    @Qualifier("merchantExtendInfoProvider")
    private MerchantExtendInfoProvider merchantExtendInfoProvider;

    @Autowired
    @Qualifier("merchantExtendedInfoDataService")
    IMerchantExtendedInfoDataService merchantExtendedInfoDataService;

    @Autowired
    @Qualifier("aoaGatewayServiceImpl")
    IAoaGatewayService aoaGatewayService;

    private boolean isAOAMerchant(TheiaMerchantExtendedDataResponse extendedDataResponse) {
        if (extendedDataResponse == null || extendedDataResponse.getExtendedInfo() == null
                || extendedDataResponse.getExtendedInfo().get("platformType") == null) {
            return false;
        }
        LOGGER.debug("PlatformType is : {}", extendedDataResponse.getExtendedInfo().get("platformType"));
        return extendedDataResponse.getExtendedInfo().get("platformType").equals("AOA");
    }

    public boolean isAOAMerchant(String paytmMID) {
        LOGGER.debug("validating AOA merchant for mid : {}", paytmMID);

        if (StringUtils.isBlank(paytmMID)) {
            // LOGGER.warn("MID is blank");
            return false;
        }
        LOGGER.info("MID : {} is not empty", paytmMID);
        TheiaMerchantExtendedDataResponse extendedDataResponse = merchantExtendedInfoDataService
                .getMerchantExtendedInfoData(paytmMID);

        return isAOAMerchant(extendedDataResponse);
    }

    public boolean isAOAMerchant(PaymentRequestBean paymentRequestBean) {

        if (paymentRequestBean == null) {
            LOGGER.error("PaymentRequestBean is null.");
            return false;
        }
        return isAOAMerchant(paymentRequestBean.getMid());
    }

    public String getPgMidForAoaMid(String aoaMid) {
        String pgMid = null;
        try {
            AoaGatewayConfigInfoResponse aoaGatewayConfigInfoResponse = aoaGatewayService
                    .getGatewayConfigViaAoaMidGatewayName(aoaMid, "PAYTMPG");
            EXT_LOGGER
                    .customInfo("Mapping response - AoaGatewayConfigInfoResponse :: {}", aoaGatewayConfigInfoResponse);
            if (aoaGatewayConfigInfoResponse != null
                    && StringUtils.isNotBlank(aoaGatewayConfigInfoResponse.getGatewayMerchantId()))
                pgMid = aoaGatewayConfigInfoResponse.getGatewayMerchantId();
        } catch (Exception e) {
            LOGGER.error("Exception occured while fetching pg mid against aoa mid");
        }
        return pgMid;
    }

    public String getAOAMidForPGMid(String pgMid) {
        String aoaMid = null;
        try {
            AoaGatewayConfigInfoResponse aoaGatewayConfigInfoResponse = aoaGatewayService
                    .getGatewayConfigViaGatewayMid(pgMid);
            EXT_LOGGER
                    .customInfo("Mapping response - AoaGatewayConfigInfoResponse :: {}", aoaGatewayConfigInfoResponse);
            if (aoaGatewayConfigInfoResponse != null)
                aoaMid = aoaGatewayConfigInfoResponse.getAoaMerchantId();
        } catch (Exception e) {
            LOGGER.error("Exception occured while fetching aoa mid against pg mid {}", pgMid);
        }
        return aoaMid;
    }

}