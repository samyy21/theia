package com.paytm.pgplus.theia.offline.validation;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.merchant.models.BankInfoData;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;

/**
 * Created by rahulverma on 5/9/17.
 */
public class ResponseValidationUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(ResponseValidationUtils.class);

    public static void validateResponse(GenericCoreResponseBean genericCoreResponseBean, BaseException ex) {
        LOGGER.debug("Validating Response {}", genericCoreResponseBean.getResponse().getClass().getSimpleName());
        if (genericCoreResponseBean == null || !genericCoreResponseBean.isSuccessfullyProcessed()) {
            LOGGER.error("Validation failed for Response {}", genericCoreResponseBean.getResponse().getClass()
                    .getSimpleName());
            throw ex;
        }
    }

    public static void validateResponse(BinDetail binDetail, BaseException ex) {
        LOGGER.debug("Validating bin details");
        if (binDetail == null) {
            LOGGER.error("Validation failed : unable to fetch bin details");
            throw ex;
        }
    }

    public static void validateResponse(String bin, BaseException ex) {
        LOGGER.debug("Validating bin ");
        if (StringUtils.isBlank(bin) || (bin.length() < 6) || !StringUtils.isNumeric(bin)) {
            LOGGER.error("Validation failed : invalid bin");
            throw ex;
        }
    }

    public static void validateResponse(BankInfoData bankInfo, BaseException ex) {
        LOGGER.debug("Validating bank info  details");
        if (bankInfo == null) {
            LOGGER.error("Validation failed : unable to fetch bank info");
            throw ex;
        }
    }
}
