package com.paytm.pgplus.theia.acs.service.impl;

import java.util.UUID;

import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.paytm.pgplus.theia.acs.enums.AcsUrlPlaceholder;
import com.paytm.pgplus.theia.acs.models.ACSUrlData;
import com.paytm.pgplus.theia.acs.service.IAcsUrlService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;

@Service("acsUrlServiceImpl")
public class AcsUrlServiceImpl implements IAcsUrlService {

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(AcsUrlServiceImpl.class);

    @Override
    public String generateACSUrl(String mid, String orderId, String webForm) throws TheiaServiceException {
        Assert.isTrue(StringUtils.isNotBlank(mid), "MID received was empty");
        Assert.isTrue(StringUtils.isNotBlank(orderId), "ORDER_ID received was empty");
        Assert.isTrue(StringUtils.isNotBlank(webForm), "BANK_FORM received was empty");
        ACSUrlData acsUrlData = createAcsUrlData(mid, orderId, webForm);
        String cacheKey = getAcsUrlCacheKey(mid, orderId);
        LOGGER.info("Generated ACS Url Data : {} with Cache Key : {} ", acsUrlData, cacheKey);
        theiaTransactionalRedisUtil.set(cacheKey, acsUrlData, 4 * 60 * 60);
        return acsUrlData.getAcsUrlGenerated();
    }

    @Override
    public String resolveACSUrl(String mid, String orderId, String uniqueId) throws TheiaServiceException {
        Assert.isTrue(StringUtils.isNotBlank(mid), "mid received was empty");
        Assert.isTrue(StringUtils.isNotBlank(orderId), "orderId received was empty");
        Assert.isTrue(StringUtils.isNotBlank(uniqueId), "uniqueId received was empty");
        String cacheKey = getAcsUrlCacheKey(mid, orderId);
        ACSUrlData acsUrlData = (ACSUrlData) theiaTransactionalRedisUtil.get(cacheKey);
        Assert.notNull(acsUrlData, "ACS data has expired for key " + cacheKey);
        LOGGER.info("ACS Url Data for key {} obtained after a delay of {} ms", cacheKey,
                (System.currentTimeMillis() - acsUrlData.getTimeOfCreation()));
        Assert.isTrue(uniqueId.equals(acsUrlData.getUniqueRandomId()), "Invalid URL accessed");
        Assert.isTrue(StringUtils.isNotBlank(acsUrlData.getWebFormContext()), "Empty bank form obtained from cache");
        return acsUrlData.getWebFormContext();
    }

    @Override
    public void purgeAcsUrl(String mid, String orderId) {
        String cacheKey = getAcsUrlCacheKey(mid, orderId);
        theiaTransactionalRedisUtil.del(cacheKey);
        LOGGER.info("Successfully purged entry : {} from cache", cacheKey);
    }

    private String getAcsUrlCacheKey(String mid, String orderId) {
        return new StringBuilder().append(TheiaConstant.ExtraConstants.ACS_URL_DATA_CACHE_KEY_PREFIX).append("_")
                .append(mid).append("_").append(orderId).toString();
    }

    private ACSUrlData createAcsUrlData(String mid, String orderId, String webForm) {
        ACSUrlData acsUrlData = new ACSUrlData();
        acsUrlData.setMid(mid);
        acsUrlData.setOrderId(orderId);
        acsUrlData.setWebFormContext(webForm);
        acsUrlData.setTimeOfCreation(System.currentTimeMillis());
        acsUrlData.setUniqueRandomId(UUID.randomUUID().toString().replaceAll("-", ""));
        setUrl(acsUrlData);
        return acsUrlData;
    }

    private void setUrl(ACSUrlData acsUrlData) {
        String configUrl = ConfigurationUtil.getProperty(TheiaConstant.ExtraConstants.ACS_URL_FORMAT_PROPERTY_KEY);
        Assert.isTrue(StringUtils.isNotBlank(configUrl), "ACS base url is not configured");
        for (AcsUrlPlaceholder acsUrlPlaceholder : AcsUrlPlaceholder.values()) {
            switch (acsUrlPlaceholder) {
            case MID:
                configUrl = configUrl.replace(acsUrlPlaceholder.getPlaceholderValue(), acsUrlData.getMid());
                break;
            case ORDER_ID:
                configUrl = configUrl.replace(acsUrlPlaceholder.getPlaceholderValue(), acsUrlData.getOrderId());
                break;
            case UNIQUE_ID:
                configUrl = configUrl.replace(acsUrlPlaceholder.getPlaceholderValue(), acsUrlData.getUniqueRandomId());
                break;
            }
        }
        acsUrlData.setAcsUrlGenerated(configUrl);
    }

}
