package com.paytm.pgplus.biz.core.payment.utils;

/**
 * @author utkarshsrivastava on 02/03/22
 */

import com.paytm.pgplus.cache.model.AoaGatewayConfigInfoResponse;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.IAoaGatewayService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("aoaUtil")
public class AOAUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AOAUtils.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(AOAUtils.class);

    @Autowired
    @Qualifier("aoaGatewayServiceImpl")
    IAoaGatewayService aoaGatewayService;

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

}
