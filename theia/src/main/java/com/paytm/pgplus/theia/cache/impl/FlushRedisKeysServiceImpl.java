package com.paytm.pgplus.theia.cache.impl;

import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.theia.cache.IFlushRedisKeysService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.cache.model.RedisKeys;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service("flushRedisKeysDataService")
public class FlushRedisKeysServiceImpl implements IFlushRedisKeysService {

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(FlushRedisKeysServiceImpl.class);

    private static final String DIRECT_BANK_DELIMITER = "";

    @Override
    public void flushRedisKeys(HttpServletRequest request) {
        List<String> redisKeyPrefixList = buildRedisKeyPrefixList(ConfigurationUtil
                .getProperty(TheiaConstant.RedisKeysConstant.REDIS_KEY_PREFIX));
        List<String> deleteRedisKeyList = new ArrayList<>();
        List<RedisKeys> redisKeyList = generateKeysToRemove(request);
        addRedisKeysInDeleteList(redisKeyPrefixList, deleteRedisKeyList, redisKeyList);
        String[] deleteRedisKey = deleteRedisKeyList.toArray(new String[0]);
        LOGGER.info("Removing Keys : {} from redis", deleteRedisKeyList);
        if (deleteRedisKey.length > 0) {
            theiaSessionRedisUtil.del(deleteRedisKey);
        }
    }

    private List<RedisKeys> generateKeysToRemove(HttpServletRequest request) {
        List<RedisKeys> redisKeyList = new ArrayList<>();

        String directBankKey = ConfigurationUtil.getProperty(TheiaConstant.RedisKeysConstant.DIRECT_BANK_KEY);
        RedisKeys redisKey = new RedisKeys(directBankKey, DIRECT_BANK_DELIMITER,
                request.getParameter(TheiaConstant.ExtraConstants.CASHIER_REQUEST_ID));
        redisKeyList.add(redisKey);

        redisKey = new RedisKeys(TheiaConstant.RedisKeysConstant.DIRECT_BANK_CARD_PAYMENT, DIRECT_BANK_DELIMITER,
                request.getParameter(TheiaConstant.ExtraConstants.CASHIER_REQUEST_ID));
        redisKeyList.add(redisKey);

        return redisKeyList;
    }

    private void addRedisKeysInDeleteList(List<String> redisKeyPrefixList, List<String> deleteRedisKeyList,
            List<RedisKeys> redisKeyList) {
        for (RedisKeys key : redisKeyList) {
            String redisKey = null;
            try {
                redisKey = key.createRedisKey();
                if (redisKeyPrefixList != null && redisKeyPrefixList.contains(key.getPrefix())
                        && theiaSessionRedisUtil.isExist(redisKey)) {
                    deleteRedisKeyList.add(redisKey);
                }
            } catch (Exception e) {
                LOGGER.error("Exception : {}, occured for redis key : {}", e, redisKey);
            }
        }

    }

    private List<String> buildRedisKeyPrefixList(String redisKeys) {
        if (StringUtils.isBlank(redisKeys)) {
            LOGGER.info("Redis keys recived from property file is null");
            return null;
        }

        List<String> redisKeyPrefixList;
        redisKeyPrefixList = Arrays.asList(redisKeys.trim().split("\\s*,\\s*"));
        return redisKeyPrefixList;
    }
}
