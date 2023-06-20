package com.paytm.pgplus.theia.utils.helper;

import com.paytm.pgplus.applicationcache.CacheManager;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.management.InstanceAlreadyExistsException;

import static com.paytm.pgplus.theia.constants.TheiaConstant.CacheName.*;

@Component
public class MerchantThemeLoadBalancer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantThemeLoadBalancer.class);

    private static Long counterWeb = Long.valueOf(-1);
    private static Long counterWap = Long.valueOf(-1);
    private static String[] allWebThemes;
    private static String[] allWapThemes;
    private static Long sizeWebChannel;
    private static Long sizeWapChannel;
    private static boolean isRedisEnabledForTheme;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    static {
        try {
            CacheManager.getCacheManager().registerExpirableCache(THEMECOUNTER_CACHE, String.class, Long.class);
            CacheManager.getCacheManager().putValueToExpirableCache(THEMECOUNTER_WEB, counterWeb, THEMECOUNTER_CACHE);
            CacheManager.getCacheManager().putValueToExpirableCache(THEMECOUNTER_WAP, counterWap, THEMECOUNTER_CACHE);
        } catch (InstanceAlreadyExistsException e) {
            LOGGER.error("ThemeCounter ExpirableCache instance already exits.");
            System.exit(1);
        }
    }

    public String getTheme(String channel, String mid) {
        initialize(channel, mid);
        Long count = Long.valueOf("0");
        switch (channel) {
        case CHANNEL_WAP:
            int indexWap;
            if (isRedisEnabledForTheme) {
                count = (Long) theiaTransactionalRedisUtil.increment(THEMECOUNTER_WAP);
            } else if (CacheManager.getCacheManager().getValueFromExpirableCache(THEMECOUNTER_WAP, Long.class,
                    THEMECOUNTER_CACHE) != null) {
                count = CacheManager.getCacheManager().increment(THEMECOUNTER_CACHE, THEMECOUNTER_WAP);
            }
            indexWap = (int) (count % sizeWapChannel);
            if (StringUtils.isBlank(allWapThemes[indexWap])) {
                LOGGER.info("No WAP theme found in the properties file for {}", mid);
                return TheiaConstant.ThemeConstants.DEFAULT_WAP_THEME;
            } else {

                LOGGER.info("Total WAP theme counter is {} and Current Theme is {}:{} ", count, mid,
                        allWapThemes[indexWap]);
            }
            return allWapThemes[indexWap];
        case CHANNEL_WEB:
            int indexWeb;
            if (isRedisEnabledForTheme) {
                count = (Long) theiaTransactionalRedisUtil.increment(THEMECOUNTER_WEB);
            } else if (CacheManager.getCacheManager().getValueFromExpirableCache(THEMECOUNTER_WEB, Long.class,
                    THEMECOUNTER_CACHE) != null) {
                count = CacheManager.getCacheManager().increment(THEMECOUNTER_CACHE, THEMECOUNTER_WEB);
            }
            indexWeb = (int) (count % sizeWebChannel);
            if (StringUtils.isBlank(allWebThemes[indexWeb])) {
                LOGGER.info("No WEB theme found in the properties file for {}", mid);
                return TheiaConstant.ThemeConstants.DEFAULT_WEB_THEME;
            } else {
                LOGGER.info("Total WEB theme counter is {} and Current Theme is {}:{} ", count, mid,
                        allWebThemes[indexWeb]);
            }
            return allWebThemes[indexWeb];
        default:
            LOGGER.info("Channel {} not supported for theme, returning theme null", channel);
            return null;
        }
    }

    private void initialize(String channel, String mid) {
        if (ConfigurationUtil.getProperty(TheiaConstant.PaytmPropertyConstants.MERCHANT_THEME_LB_REDIS_FLAG).equals(
                "true")) {
            isRedisEnabledForTheme = true;
        }
        LOGGER.info("Redis flag for theme load balancer is {}", isRedisEnabledForTheme);
        if (StringUtils.equals(channel, CHANNEL_WEB)) {
            String propertyName = mid.concat(".web.theme.list");
            allWebThemes = ConfigurationUtil.getProperty(propertyName).split(",");
            sizeWebChannel = Long.valueOf(allWebThemes.length);
        } else {
            String propertyName = mid.concat(".wap.theme.list");
            allWapThemes = ConfigurationUtil.getProperty(propertyName).split(",");
            sizeWapChannel = Long.valueOf(allWapThemes.length);
        }
    }
}
