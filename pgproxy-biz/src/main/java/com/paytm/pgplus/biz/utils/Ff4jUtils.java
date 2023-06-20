package com.paytm.pgplus.biz.utils;

import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgpff4jstrategy.MidPercentageStrategy;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.IS_COFT_SUPPORT_ENABLED_ON_AOA;
import static com.paytm.pgplus.pgpff4jstrategy.MidPercentageStrategy.IDENTIFIER;

@Component
public class Ff4jUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Ff4jUtils.class);
    private static final String IDENTIFIER = "uniqueHash";
    private static final String MID = "mid";

    public static final String DUMMY_BRAND_LIST = "theia.dummy.brand.id.list";

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    /**
     *
     * @param mid
     * @return if v2/user/mid is allowed to hit on given mid
     */
    public boolean isFeatureEnabledOnMid(String mid) {
        Map<String, Object> context = new HashMap<>();
        context.put("mid", mid);
        Boolean enabled = iPgpFf4jClient.checkWithdefault("v2UserMid", context, false);

        Boolean disabled = iPgpFf4jClient.checkWithdefault("v2UserMidBlocked", context, false);

        LOGGER.debug("Feature enabled {}, disabled {}", enabled, disabled);
        return disabled ? false : enabled;
    }

    public boolean isFeatureEnabledOnMid(String mid, String feature, boolean defaultReturnValue) {
        try {
            Map<String, Object> context = new HashMap<>();
            context.put("mid", mid);
            return iPgpFf4jClient.checkWithdefault(feature, context, defaultReturnValue);

        } catch (Exception e) {
            LOGGER.error("Exception getting feature status from ff4j for mid", feature, mid);
            return defaultReturnValue;
        }
    }

    /**
     * to check whether a particular feature is enabled or not
     * 
     * @param feature
     * @param defaultReturnValue
     * @return
     */
    public boolean isFeatureEnabled(String feature, boolean defaultReturnValue) {
        try {
            return iPgpFf4jClient.checkWithdefault(feature, null, defaultReturnValue);

        } catch (Exception e) {
            LOGGER.error("Exception getting feature {} from ff4j", feature);
            return defaultReturnValue;
        }
    }

    public boolean featureEnabledOnMultipleKeys(String mid, String custId, String feature, boolean defaultReturnValue) {
        try {
            Map<String, Object> context = new HashMap<>();
            context.put("keyArg1", mid);
            context.put("keyArg2", custId);
            return iPgpFf4jClient.checkWithdefault(feature, context, defaultReturnValue);

        } catch (Exception e) {
            LOGGER.error("Exception getting feature status from ff4j for mid", feature, mid);
            return defaultReturnValue;
        }
    }

    public boolean isFeatureEnabledOnMidAndCustIdOrUserId(String Feature, String mid, String custId, String userId) {
        Map<String, Object> context = new HashMap<>();
        context.put("mid", mid);
        context.put("custId", custId);
        context.put("userId", userId);
        return iPgpFf4jClient.checkWithdefault(Feature, context, true);
    }

    public boolean isFeatureEnabledOnMidAndCustIdOrUserId(String feature, String mid, String custId, String userId,
            boolean defaultValue) {
        Map<String, Object> context = new HashMap<>();
        context.put("mid", mid);
        context.put("custId", custId);
        context.put("userId", userId);
        return iPgpFf4jClient.checkWithdefault(feature, context, defaultValue);
    }

    // Commenting Unused code, will be removed in future
    // public boolean isFeatureEnabledOnMidPayModeAndPercentage(String feature,
    // String mid, String payMode,
    // String payOption, String userId) {
    // Map<String, Object> context = new HashMap<>();
    // context.put("mid", mid);
    // context.put("payMode", payMode);
    // context.put("payOption", payOption);
    // context.put("userId", userId);
    // return iPgpFf4jClient.checkWithdefault(feature, context, false);
    // }

    public boolean isFeatureEnabledOnMidPayModeAndPercentage(String feature, String mid, String payMode,
            String payOption, String userId, String orderId) {
        Map<String, Object> context = new HashMap<>();
        String midOrderIdKey = mid + orderId;
        context.put("mid", mid);
        context.put("payMode", payMode);
        context.put("userId", userId);
        context.put("midOrderIdHash", Integer.toString(midOrderIdKey.hashCode()));

        // First we will check if for a particular payMode, "ALL" payOption is
        // configured,
        // if this is false then only we will check if feature is enabled for
        // specific payOption.
        context.put("payOption", "ALL");
        if (BooleanUtils.isFalse(iPgpFf4jClient.checkWithdefault(feature, context, false))) {
            context.put("payOption", payOption);
            return iPgpFf4jClient.checkWithdefault(feature, context, false);
        }
        return true;
    }

    public String getPropertyAsStringWithDefault(String propertyName, String defaultValue) {
        return iPgpFf4jClient.getPropertyAsStringWithDefault(propertyName, defaultValue);
    }

    public boolean isCOFTEnabledOnAOA(String mid) {
        return isFeatureEnabledOnMid(mid, IS_COFT_SUPPORT_ENABLED_ON_AOA, false);
    }

    public boolean isEnabledOnMidAndPercentage(String mid, String featureName, boolean defaultValue) {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put(MID, mid);
        ctx.put(IDENTIFIER, Integer.toString(UUID.randomUUID().hashCode()));
        return iPgpFf4jClient.checkWithdefault(featureName, ctx, defaultValue);
    }
}
