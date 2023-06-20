package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.cache.model.PaytmProperty;
import com.paytm.pgplus.facade.common.model.AlipayExternalRequestHeader;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.fluxnet.models.SuccessRate;
import com.paytm.pgplus.facade.fluxnet.models.request.SuccessRateQueryRequest;
import com.paytm.pgplus.facade.fluxnet.models.request.SuccessRateQueryRequestBody;
import com.paytm.pgplus.facade.fluxnet.models.response.SuccessRateQueryResponse;
import com.paytm.pgplus.facade.fluxnet.services.IQuerySuccessRates;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.SuccessRateBean;
import com.paytm.pgplus.theia.models.SuccessRateCacheModel;
import com.paytm.pgplus.theia.models.SuccessRateQueryRequestBean;
import com.paytm.pgplus.theia.models.response.SuccessRateQueryResponseBean;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.ISuccessRateQueryService;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.THEIA_STATIC_REDIS_MIGRATION_SESSION_REDIS;

/**
 * @author kartik
 * @date 09-03-2017
 */
@Service("successRateQueryServiceImpl")
public class SuccessRateQueryServiceImpl implements ISuccessRateQueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuccessRateQueryServiceImpl.class);

    @Autowired
    @Qualifier("querySuccessRatesImpl")
    private IQuerySuccessRates querySuccessRateImpl;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Override
    public SuccessRateQueryResponseBean getSuccessRatesForPayMethod(
            SuccessRateQueryRequestBean successRateQueryRequestBean) {
        try {
            final SuccessRateQueryRequest successRateQueryRequest = mapRequestBeanToFacadeRequest(successRateQueryRequestBean);
            SuccessRateQueryResponse facadeSuccessRateResponse = null;
            if (ff4jUtils.isFeatureEnabled("theia.circuitBreaker", false)) {
                facadeSuccessRateResponse = querySuccessRateImpl.querySuccessRatesCircuitBreak(successRateQueryRequest);
            } else {
                facadeSuccessRateResponse = querySuccessRateImpl.querySuccessRates(successRateQueryRequest);
            }
            return mapFacadeResponseToTheiaResponseModel(facadeSuccessRateResponse);
        } catch (Exception e) {
            throw new TheiaServiceException("Exception occurred while fetching success rates", e);
        }
    }

    @Override
    public SuccessRateQueryResponseBean getSuccessRatesForPayMethod() {
        SuccessRateQueryRequestBean successRateQueryRequestBean = new SuccessRateQueryRequestBean();
        return getSuccessRatesForPayMethod(successRateQueryRequestBean);
    }

    @Override
    public SuccessRateQueryResponseBean mapFacadeResponseToTheiaResponseModel(
            SuccessRateQueryResponse facadeSuccessRateResponse) {

        if ((facadeSuccessRateResponse != null) && (facadeSuccessRateResponse.getBody() != null)
                && facadeSuccessRateResponse.getBody().getResultInfo().getResultCode().equals("SUCCESS")) {

            List<SuccessRate> facadeLowSuccessRateChannels = facadeSuccessRateResponse.getBody().getSuccessRates();
            List<SuccessRateBean> lowSuccessRateChannels = new ArrayList<SuccessRateBean>();

            if ((facadeLowSuccessRateChannels != null) && !facadeLowSuccessRateChannels.isEmpty()) {
                for (SuccessRate lowSuccessRatePayChannel : facadeLowSuccessRateChannels) {
                    SuccessRateBean successRateBean = new SuccessRateBean(lowSuccessRatePayChannel.getBankAbbr(),
                            lowSuccessRatePayChannel.getBizPattern(), lowSuccessRatePayChannel.getRate(),
                            lowSuccessRatePayChannel.getCardType());
                    lowSuccessRateChannels.add(successRateBean);
                }
            }
            SuccessRateQueryResponseBean successRateQueryResponseBean = new SuccessRateQueryResponseBean(
                    lowSuccessRateChannels);

            return successRateQueryResponseBean;
        }
        throw new TheiaServiceException("Invalid Response received from Alipay for Success Rate Query");
    }

    @Override
    public SuccessRateQueryRequest mapRequestBeanToFacadeRequest(SuccessRateQueryRequestBean successRateQueryRequestBean) {
        SuccessRateQueryRequest facadeSuccessRateQueryRequest;
        try {
            AlipayExternalRequestHeader requestHeader = RequestHeaderGenerator
                    .getHeader(ApiFunctions.SUCCESS_RATE_QUERY);
            SuccessRateQueryRequestBody successRateQueryRequestBody = new SuccessRateQueryRequestBody(
                    successRateQueryRequestBean.getSuccessRateType());
            facadeSuccessRateQueryRequest = new SuccessRateQueryRequest(requestHeader, successRateQueryRequestBody);
        } catch (FacadeCheckedException e) {
            throw new TheiaServiceException("Failed to create SuccessRateQueryRequest");
        }
        return facadeSuccessRateQueryRequest;
    }

    @Override
    public SuccessRateCacheModel getSuccessRateModelFromCache() {
        SuccessRateCacheModel successRateCacheModel = null;

        successRateCacheModel = (SuccessRateCacheModel) theiaSessionRedisUtil.get(ExtraConstants.SUCCESS_RATE_KEY);

        if (successRateCacheModel == null) {
            if (!ff4jUtils.isFeatureEnabledOnMid(ExtraConstants.SUCCESS_RATE_KEY,
                    THEIA_STATIC_REDIS_MIGRATION_SESSION_REDIS, false)) {
                LOGGER.info("operation on static redis, {}", ExtraConstants.SUCCESS_RATE_KEY);
                successRateCacheModel = (SuccessRateCacheModel) theiaTransactionalRedisUtil
                        .get(ExtraConstants.SUCCESS_RATE_KEY);
            }
        }

        return successRateCacheModel;
    }

    @Override
    public void setSuccessRateModelInCache(SuccessRateCacheModel successRatesCacheModel) {
        long expiry = Long.parseLong(ConfigurationUtil.getProperty(ExtraConstants.BANK_SUCCESS_RATE_KEY_EXPIRY, "900"));

        theiaSessionRedisUtil.set(ExtraConstants.SUCCESS_RATE_KEY, successRatesCacheModel, expiry);

        if (!ff4jUtils.isFeatureEnabledOnMid(ExtraConstants.SUCCESS_RATE_KEY,
                THEIA_STATIC_REDIS_MIGRATION_SESSION_REDIS, false)) {
            LOGGER.info("operation on static redis, {}", ExtraConstants.SUCCESS_RATE_KEY);
            theiaTransactionalRedisUtil.set(ExtraConstants.SUCCESS_RATE_KEY, successRatesCacheModel, expiry);
        }

    }

    @Override
    public SuccessRateCacheModel checkAndFetchSuccessRateModel() {

        SuccessRateCacheModel successRateCacheModel = null;
        PaytmProperty fetchSuccessRateFlag = configurationDataService
                .getPaytmProperty(ExtraConstants.FETCH_SUCCESS_RATE);

        if ((fetchSuccessRateFlag != null) && NumberUtils.isNumber(fetchSuccessRateFlag.getValue())
                && ExtraConstants.ENABLE_FLAG_SUCCESS_RATE.equals(fetchSuccessRateFlag.getValue())) {

            successRateCacheModel = getSuccessRateModelFromCache();
            if (successRateCacheModel == null) {
                SuccessRateQueryRequestBean successRateQueryRequestBean = new SuccessRateQueryRequestBean();
                SuccessRateQueryResponseBean successRateResponseBean = null;
                try {
                    successRateResponseBean = getSuccessRatesForPayMethod(successRateQueryRequestBean);
                } catch (TheiaServiceException e) {
                    LOGGER.error("Exception occured while fetching success rates : ", e);
                }
                if ((successRateResponseBean != null) && (successRateResponseBean.getSuccessRates() != null)
                        && !successRateResponseBean.getSuccessRates().isEmpty()) {
                    successRateCacheModel = new SuccessRateCacheModel();
                    Map<String, String> successRatesMap = new HashMap<String, String>();
                    for (SuccessRateBean successRateBean : successRateResponseBean.getSuccessRates()) {
                        successRatesMap.put(successRateBean.getBizPattern() + TheiaConstant.THREAD_NAME_SEPERATOR
                                + successRateBean.getBankAbbr(), successRateBean.getRate());
                    }
                    successRateCacheModel.setSuccessRatesMap(successRatesMap);
                    setSuccessRateModelInCache(successRateCacheModel);
                }
            }
        }
        return successRateCacheModel;
    }

}