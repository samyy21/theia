package com.paytm.pgplus.theia.utils.helper;

import com.paytm.pgplus.cache.model.PaytmProperty;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.SuccessRateBean;
import com.paytm.pgplus.theia.models.SuccessRateCacheModel;
import com.paytm.pgplus.theia.models.response.SuccessRateQueryResponseBean;
import com.paytm.pgplus.theia.services.ISuccessRateQueryService;
import com.paytm.pgplus.theia.utils.MapperUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Naman on 18/04/17.
 */
@Service("successRateUtils")
public class SuccessRateUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SuccessRateUtils.class);

    @Autowired
    @Qualifier("successRateQueryServiceImpl")
    private ISuccessRateQueryService successRateQueryService;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    public SuccessRateCacheModel fetchSuccessRateAndSetToCache() {
        SuccessRateCacheModel successRateCacheModel = successRateQueryService.getSuccessRateModelFromCache();

        if (successRateCacheModel != null) {
            return successRateCacheModel;
        }

        SuccessRateQueryResponseBean successRateResponseBean = null;
        try {
            successRateResponseBean = successRateQueryService.getSuccessRatesForPayMethod();
        } catch (TheiaServiceException e) {
            LOGGER.error("Exception occurred while fetching success rates : ", e);
        }

        if ((successRateResponseBean != null) && (successRateResponseBean.getSuccessRates() != null)
                && !successRateResponseBean.getSuccessRates().isEmpty()) {
            Map<String, String> successRatesMap = new HashMap<>();

            for (SuccessRateBean successRateBean : successRateResponseBean.getSuccessRates()) {

                if (StringUtils.isBlank(successRateBean.getBankAbbr())
                        || StringUtils.isBlank(successRateBean.getBizPattern())) {
                    continue;
                }
                String cardType = null;
                StringBuilder sb = null;
                String bankcard = TheiaConstant.ExtraConstants.BANKCARD;

                if (successRateBean.getBizPattern().equals(bankcard)) {
                    sb = checkSuccessForBankCard(successRatesMap, successRateBean, cardType, sb,
                            TheiaConstant.THREAD_NAME_SEPERATOR);
                } else {
                    sb = new StringBuilder(successRateBean.getBizPattern());
                }

                if ((successRateBean.getBizPattern().equals(bankcard) && StringUtils.isNotBlank(successRateBean
                        .getCardType())) || !successRateBean.getBizPattern().equals(bankcard)) {

                    if (null != sb) {
                        sb.append(TheiaConstant.THREAD_NAME_SEPERATOR).append(successRateBean.getBankAbbr());
                        successRatesMap.put(sb.toString(), successRateBean.getRate());
                    } else {
                        LOGGER.error("Invalid response parameters obtained via APlus in Success Rate Query API");
                    }
                }
            }

            successRateCacheModel = new SuccessRateCacheModel(successRatesMap);
            successRateQueryService.setSuccessRateModelInCache(successRateCacheModel);
        }

        return successRateCacheModel;
    }

    private StringBuilder checkSuccessForBankCard(Map<String, String> successRatesMap, SuccessRateBean successRateBean,
            String cardType, StringBuilder sb, String threadSeperator) {
        if (StringUtils.isNotBlank(successRateBean.getCardType())) {
            if (ExtraConstants.CREDIT_CARD.equals(successRateBean.getCardType()))
                cardType = ExtraConstants.CC;
            if (ExtraConstants.DEBIT_CARD.equals(successRateBean.getCardType()))
                cardType = ExtraConstants.DC;
            sb = new StringBuilder(cardType);
        } else {
            StringBuilder sbCC = new StringBuilder(ExtraConstants.CC);
            sbCC.append(threadSeperator).append(successRateBean.getBankAbbr());
            successRatesMap.put(sbCC.toString(), successRateBean.getRate());

            StringBuilder sbDC = new StringBuilder(ExtraConstants.DC);
            sbDC.append(threadSeperator).append(successRateBean.getBankAbbr());
            successRatesMap.put(sbDC.toString(), successRateBean.getRate());
        }
        return sb;
    }

    public boolean checkIfLowSuccessRate(final String bankName, final PayMethod payMethod) {
        PaytmProperty fetchSuccessRateFlag = configurationDataService
                .getPaytmProperty(ExtraConstants.FETCH_SUCCESS_RATE);

        if ((fetchSuccessRateFlag == null) || !NumberUtils.isNumber(fetchSuccessRateFlag.getValue())
                || !ExtraConstants.ENABLE_FLAG_SUCCESS_RATE.equals(fetchSuccessRateFlag.getValue())) {
            return false;
        }

        String bizPattern = MapperUtils.getBizPatternForPayMethod(payMethod);

        if (StringUtils.EMPTY.equals(bizPattern)) {
            return false;
        }

        SuccessRateCacheModel successRateCacheModel = fetchSuccessRateAndSetToCache();
        String successRateMapKey = new StringBuilder(bizPattern).append(TheiaConstant.THREAD_NAME_SEPERATOR)
                .append(bankName).toString();

        String bankThresholdPropertyKey = new StringBuilder(TheiaConstant.ExtraConstants.BANK_SUCCESS_RATE_THRESHOLD)
                .append(bizPattern).append(".").append(bankName).toString();

        if ((successRateCacheModel != null)
                && successRateCacheModel.getSuccessRatesMap().containsKey(successRateMapKey)
                && StringUtils.isNotBlank(ConfigurationUtil.getProperty(bankThresholdPropertyKey))) {

            return checkBankSuccessFlag(successRateCacheModel, successRateMapKey, bankThresholdPropertyKey);
        }

        return false;
    }

    public boolean checkIfLowSuccessRate(final String bankName, final PayMethod payMethod,
            SuccessRateCacheModel successRateCacheModel) {
        if (null == successRateCacheModel) {
            return false;
        }

        String bizPattern = MapperUtils.getBizPatternForPayMethod(payMethod);

        if (StringUtils.EMPTY.equals(bizPattern)) {
            return false;
        }
        String successRateMapKey = new StringBuilder(bizPattern).append(TheiaConstant.THREAD_NAME_SEPERATOR)
                .append(bankName).toString();

        String bankThresholdPropertyKey = new StringBuilder(TheiaConstant.ExtraConstants.BANK_SUCCESS_RATE_THRESHOLD)
                .append(bizPattern).append(".").append(bankName).toString();

        if ((successRateCacheModel.getSuccessRatesMap().containsKey(successRateMapKey) && StringUtils
                .isNotBlank(ConfigurationUtil.getProperty(bankThresholdPropertyKey)))) {

            return checkBankSuccessFlag(successRateCacheModel, successRateMapKey, bankThresholdPropertyKey);
        }

        return false;
    }

    private boolean checkBankSuccessFlag(SuccessRateCacheModel successRateCacheModel, String successRateMapKey,
            String bankThresholdPropertyKey) {
        try {
            float bankSuccessRate = Float.parseFloat(successRateCacheModel.getSuccessRatesMap().get(successRateMapKey));
            float bankSuccessRateThreshold = Float.parseFloat(ConfigurationUtil.getProperty(bankThresholdPropertyKey));

            if (bankSuccessRate < bankSuccessRateThreshold) {
                return true;
            }
        } catch (NumberFormatException e) {
            LOGGER.warn("Unable to compare low success rate checks", e);
            return false;
        }
        return false;
    }

    public SuccessRateCacheModel getSuccessRateCacheModel() {
        SuccessRateCacheModel successRateCacheModel = null;
        try {
            boolean isFetchSuccessRateEnable = true;
            PaytmProperty fetchSuccessRateFlag = configurationDataService
                    .getPaytmProperty(ExtraConstants.FETCH_SUCCESS_RATE);

            if ((fetchSuccessRateFlag == null) || !NumberUtils.isNumber(fetchSuccessRateFlag.getValue())
                    || !ExtraConstants.ENABLE_FLAG_SUCCESS_RATE.equals(fetchSuccessRateFlag.getValue())) {
                isFetchSuccessRateEnable = false;
            }
            if (isFetchSuccessRateEnable) {
                successRateCacheModel = fetchSuccessRateAndSetToCache();
            }
        } catch (Exception e) {
            LOGGER.error("Unable to fetch success rates {}", e);
        }
        return successRateCacheModel;
    }
}
