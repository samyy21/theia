package com.paytm.pgplus.biz.utils;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.paytm.pgplus.logging.ExtendedLogger;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.COFT_GLOBAL_VAULT_ENABLED;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.USER_ID;

@Service
public class FF4JUtil {

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(FF4JUtil.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(FF4JUtil.class);

    public boolean fetchSavedCardFromPlatform(WorkFlowRequestBean requestBean, String userId) {
        boolean isFeatureEnabledOnMidCustId = false;
        boolean isFeatureEnabledOnUserId = false;
        Map<String, Object> context = new HashMap<>();
        if (StringUtils.isNotEmpty(requestBean.getCustID())) {
            context.put(TheiaConstant.ExtraConstants.KEY_ARGS2, requestBean.getCustID());
            context.put(TheiaConstant.ExtraConstants.KEY_ARGS1, requestBean.getPaytmMID());
            // make one feature
            isFeatureEnabledOnMidCustId = iPgpFf4jClient.checkWithdefault("fetchSavedcardFromPlatformForMidCustId",
                    context, false);
        }
        if (!isFeatureEnabledOnMidCustId && StringUtils.isNotEmpty(userId)) {
            context.remove(TheiaConstant.ExtraConstants.KEY_ARGS2, requestBean.getCustID());
            context.remove(TheiaConstant.ExtraConstants.KEY_ARGS1, requestBean.getPaytmMID());
            context.put(USER_ID, userId);
            isFeatureEnabledOnUserId = iPgpFf4jClient.checkWithdefault("fetchSavedcardFromPlatformForUserId", context,
                    false);

        }
        return isFeatureEnabledOnMidCustId || isFeatureEnabledOnUserId;
    }

    public boolean saveCardAtPlatformOnUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            return false;
        }
        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        boolean isFeatureEnabledOnUserId = iPgpFf4jClient
                .checkWithdefault("saveCardAtPlatformOnUserId", context, false);
        return isFeatureEnabledOnUserId;
    }

    public boolean saveCardAtPlatformOnMidCustId(String mid, String custId) {
        if (StringUtils.isBlank(mid) || StringUtils.isBlank(custId)) {
            return false;
        }
        Map<String, Object> context = new HashMap<>();
        context.put(TheiaConstant.ExtraConstants.KEY_ARGS2, custId);
        context.put(TheiaConstant.ExtraConstants.KEY_ARGS1, mid);
        boolean isFeatureEnabledOnMidCustId = iPgpFf4jClient.checkWithdefault("saveCardAtPlatformOnMidCustId", context,
                false);
        return isFeatureEnabledOnMidCustId;
    }

    public boolean fetchSavedCardFromService(String userId, String mid, String custId) {
        return fetchSavedCardFromServiceOnMidCustid(mid, custId);
    }

    public boolean fetchSavedCardFromServiceOnUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            return false;
        }
        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        boolean isFeatureDisableddOnUserId = iPgpFf4jClient.checkWithdefault(
                "shortCircuitSavedCardServiceReadForUserId", context, false);
        return !isFeatureDisableddOnUserId;
    }

    public boolean fetchSavedCardFromServiceOnMidCustid(String mid, String custId) {

        if (StringUtils.isBlank(mid)) {
            return false;
        }

        if (StringUtils.isBlank(custId)) {
            custId = "NONE";
        }

        Map<String, Object> context = new HashMap<>();
        context.put(TheiaConstant.ExtraConstants.KEY_ARGS2, custId);
        context.put(TheiaConstant.ExtraConstants.KEY_ARGS1, mid);
        boolean isFeatureDisabledOnMidCustId = iPgpFf4jClient.checkWithdefault(
                "shortCircuitSavedCardServiceReadForMidCustId", context, false);
        return !isFeatureDisabledOnMidCustId;
    }

    public boolean returnSavedCardsFromPlatform(String userId, String mid, String custId) {

        return !blackListFeature("theia.blackListReturnSavedCardsFromPlatformForMidCustId", mid)
                && (returnSavedCardsFromPlatformForMidCustId(mid, custId) || returnSavedCardsFromPlatformForUserId(userId));
    }

    public boolean returnSavedCardsFromPlatformForMidCustId(String mid, String custId) {
        if (StringUtils.isBlank(mid) || StringUtils.isBlank(custId)) {
            return false;

        }
        Map<String, Object> context = new HashMap<>();
        context.put(TheiaConstant.ExtraConstants.KEY_ARGS1, mid);
        context.put(TheiaConstant.ExtraConstants.KEY_ARGS2, custId);
        return iPgpFf4jClient.checkWithdefault("returnSavedCardsFromPlatformForMidCustId", context, false);
    }

    public boolean returnSavedCardsFromPlatformForUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            return false;
        }
        Map<String, Object> context = new HashMap<>();
        context.put(USER_ID, userId);
        return iPgpFf4jClient.checkWithdefault("returnSavedCardsFromPlatformForUserId", context, false);

    }

    public boolean queryNonSensitiveForCCBillPayment(String mid, String custId) {
        if (StringUtils.isBlank(mid) || StringUtils.isBlank(custId)) {
            return false;
        }
        Map<String, Object> context = new HashMap<>();
        context.put(TheiaConstant.ExtraConstants.KEY_ARGS1, mid);
        context.put(TheiaConstant.ExtraConstants.KEY_ARGS2, custId);
        return iPgpFf4jClient.checkWithdefault("queryNonSensitiveForCCBillPayment", context, false);
    }

    public boolean useCINForSubsRenewal(String subsId) {
        if (StringUtils.isBlank(subsId)) {
            return false;
        }
        Map<String, Object> context = new HashMap<>();
        context.put(TheiaConstant.RequestParams.NATIVE_MID, subsId);
        return iPgpFf4jClient.checkWithdefault("useCINForSubsRenewal", context, false);
    }

    public boolean blockingFilterPlatformSavedAssetsForSubs(String mid) {
        if (StringUtils.isBlank(mid)) {
            return false;
        }
        Map<String, Object> context = new HashMap<>();
        context.put(TheiaConstant.RequestParams.NATIVE_MID, mid);
        return !iPgpFf4jClient.checkWithdefault("blockingFilterSavedAssetsFromPlatformForSubs", context, false);
    }

    public boolean isFeatureEnabled(String Feature, String mid) {
        if (StringUtils.isBlank(mid)) {
            return false;
        }
        Map<String, Object> context = new HashMap<>();
        context.put(TheiaConstant.RequestParams.NATIVE_MID, mid);
        return iPgpFf4jClient.checkWithdefault(Feature, context, false);
    }

    public boolean filterPlatformSavedAssets() {
        Map<String, Object> context = new HashMap<>();
        return iPgpFf4jClient.checkWithdefault("theia.filterPlatformSavedAssets", context, false);
    }

    public boolean isFeatureEnabledOnCustId(String Feature, String custId) {
        if (StringUtils.isBlank(custId)) {
            return false;
        }
        Map<String, Object> context = new HashMap<>();
        context.put(USER_ID, custId);
        return iPgpFf4jClient.checkWithdefault(Feature, context, false);
    }

    public boolean isFeatureEnabledForPromo(String mid) {
        return !blackListFeature(BizConstant.Ff4jFeature.BLACKLIST_THEIA_SEND_CIN_AND_8BINHASH_PROMO, mid)
                && isFeatureEnabled(
                        com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.THEIA_SEND_CIN_AND_8BINHASH_PROMO,
                        mid);
    }

    public boolean blackListFeature(String Feature, String mid) {
        if (StringUtils.isBlank(mid)) {
            return true;
        }
        Map<String, Object> context = new HashMap<>();
        context.put(TheiaConstant.RequestParams.NATIVE_MID, mid);
        return iPgpFf4jClient.checkWithdefault(Feature, context, true);
    }

    public boolean isMigrateBankOffersPromo(String mid) {
        if (StringUtils.isBlank(mid)) {
            return false;
        }
        Map<String, Object> context = new HashMap<>();
        context.put(TheiaConstant.RequestParams.NATIVE_MID, mid);
        return iPgpFf4jClient.checkWithdefault(BizConstant.Ff4jFeature.MIGRATE_BANK_OFFERS_PROMO, context, false);
    }

    public boolean isGlobalVaultEnabled(WorkFlowTransactionBean transBean, String mid) {
        if (transBean.getUserDetails() != null && transBean.getUserDetails().getInternalUserId() != null
                && !StringUtils.isBlank(mid)) {
            // TODO coft- remove later
            return checkFf4jFeature(mid);
        }
        return false;
    }

    public boolean checkFf4jFeature(String mid) {
        Map<String, Object> context = new HashMap<>();
        context.put(TheiaConstant.RequestParams.NATIVE_MID, mid);
        EXT_LOGGER.customInfo("Checking for global vault feature.");
        return iPgpFf4jClient.checkWithdefault(COFT_GLOBAL_VAULT_ENABLED, context, false);
    }

    public boolean isSuperCashEnabledMid(String mid) {
        if (StringUtils.isBlank(mid)) {
            return false;
        }
        Map<String, Object> context = new HashMap<>();
        context.put(TheiaConstant.RequestParams.NATIVE_MID, mid);
        return iPgpFf4jClient.checkWithdefault(BizConstant.Ff4jFeature.SUPERCASH_MID, context, false);
    }

    public boolean isSuperCashEnabledForOffline(String mid) {
        if (StringUtils.isBlank(mid)) {
            return false;
        }
        Map<String, Object> context = new HashMap<>();
        context.put(TheiaConstant.RequestParams.NATIVE_MID, mid);
        return iPgpFf4jClient.checkWithdefault(BizConstant.Ff4jFeature.SUPERCASH_OFFLINE, context, false);
    }

    public boolean isSuperCashEnabledForOfflineMlv(String mid) {
        if (StringUtils.isBlank(mid)) {
            return false;
        }
        Map<String, Object> context = new HashMap<>();
        context.put(TheiaConstant.RequestParams.NATIVE_MID, mid);
        return iPgpFf4jClient.checkWithdefault(BizConstant.Ff4jFeature.SUPERCASH_OFFLINE_MLV, context, false);

    }

}
