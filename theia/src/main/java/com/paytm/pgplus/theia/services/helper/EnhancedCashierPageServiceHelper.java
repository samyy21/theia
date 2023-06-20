package com.paytm.pgplus.theia.services.helper;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.EPayMethodGroup;
import com.paytm.pgplus.common.util.PayMethodUtility;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.enums.PayModeGroupSequenceEnum;
import com.paytm.pgplus.theia.nativ.model.common.EnhancedCashierPage;
import com.paytm.pgplus.theia.nativ.model.common.EnhancedCashierPagePayModeBase;
import com.paytm.pgplus.theia.nativ.model.common.PaymodeSequenceEnum;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.nativ.utils.PayModeOrderUtil;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.paytm.pgplus.common.util.AllowedMidCustidPropertyUtil.isMidCustIdEligible;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.DEFAULT_TXN_TOKEN_EXPIRY_FOR_NATIVE_PAYMENT_IN_SECONDS;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RedisKeysConstant.NativeSession.*;

@Service
public class EnhancedCashierPageServiceHelper {

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    public static final Logger LOGGER = LoggerFactory.getLogger(EnhancedCashierPageServiceHelper.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(EnhancedCashierPageServiceHelper.class);

    public String fetchRedisKey(String mid, String orderId) {

        return new StringBuilder(mid).append("_").append(orderId).append("_").append("EnhancedCashierPagePayload")
                .toString();
    }

    public int getTokenExpiryTime() {

        int txnTokenExpiryInSeconds = 900;

        String txnTokenExpiryInSecondsString = ConfigurationUtil.getProperty("txn.token.expiry.native.payment.seconds",
                DEFAULT_TXN_TOKEN_EXPIRY_FOR_NATIVE_PAYMENT_IN_SECONDS);

        if (txnTokenExpiryInSecondsString != null && !txnTokenExpiryInSecondsString.isEmpty()) {
            txnTokenExpiryInSeconds = Integer.parseInt(txnTokenExpiryInSecondsString);
        }

        return txnTokenExpiryInSeconds;
    }

    public String getEnhancedCashierTheme(String channelId) {

        EChannelId eChannelId = EChannelId.getEChannelIdByValue(channelId);
        if (eChannelId == null) {
            eChannelId = EChannelId.WEB;
        }
        String theme = null;

        switch (eChannelId) {
        case WEB:
            theme = com.paytm.pgplus.common.config.ConfigurationUtil.getWEBHtmlEnhancedCashierTheme();
            LOGGER.info("Received channelId:{}, using Enhanced-WEB theme", eChannelId);
            break;
        case WAP:
            theme = com.paytm.pgplus.common.config.ConfigurationUtil.getWAPHtmlEnhancedCashierTheme();
            LOGGER.info("Received channelId:{}, using Enhanced-WAP theme", eChannelId);
            break;
        default:
            theme = com.paytm.pgplus.common.config.ConfigurationUtil.getHtmlCashierTemplate();
            LOGGER.info("Received channelId:{}, using Enhanced-Default theme", eChannelId);
        }

        if (StringUtils.isBlank(theme)) {
            LOGGER.error("Enhanced-WEB/Enhanced-WAP theme is blank!, using default old Enhanced theme");
            theme = com.paytm.pgplus.common.config.ConfigurationUtil.getHtmlCashierTemplate();
        }
        return theme;
    }

    public String getEnhancedCheckoutJSTheme() {
        return com.paytm.pgplus.common.config.ConfigurationUtil.getHtmlEnhancedCheckoutTheme();
    }

    public boolean isMidCustIdAllowedForWEBEnhanced(PaymentRequestBean paymentRequestBean) {
        return isMidCustIdEligible(paymentRequestBean.getMid(), paymentRequestBean.getCustId(),
                TheiaConstant.ExtraConstants.ENHANCED_WEB_THEME_ALLOWED_CUSTID_LIST_KEY,
                TheiaConstant.ExtraConstants.ALL, Boolean.FALSE);
    }

    public boolean isMidCustIdBlockedForWEBEnhanced(PaymentRequestBean paymentRequestBean) {
        return isMidCustIdEligible(paymentRequestBean.getMid(), paymentRequestBean.getCustId(),
                TheiaConstant.ExtraConstants.ENHANCED_WEB_THEME_BLOCKED_CUSTID_LIST_KEY,
                TheiaConstant.ExtraConstants.NONE, Boolean.FALSE);
    }

    public boolean isMidCustIdAllowedForWAPEnhanced(PaymentRequestBean paymentRequestBean) {
        return isMidCustIdEligible(paymentRequestBean.getMid(), paymentRequestBean.getCustId(),
                TheiaConstant.ExtraConstants.ENHANCED_ALLOWED_CUSTID_LIST_KEY, TheiaConstant.ExtraConstants.ALL,
                Boolean.TRUE);
    }

    public void invalidateEnhancedNativeData(String txnToken, String mid, String orderId) {
        try {
            if (!ff4jUtils.isFeatureEnabledOnMid(mid,
                    TheiaConstant.FF4J.INVALIDATE_REDIS_KEY_AND_FIELDS_ENHANCED_NATIVE, false)) {
                return;
            }

            if (StringUtils.isBlank(txnToken)) {
                txnToken = nativeSessionUtil.getTxnToken(mid, orderId);
            }

            nativeSessionUtil.deleteKey(fetchRedisKey(mid, orderId));

            List<String> fieldsToBeDeleted = new ArrayList<>();
            fieldsToBeDeleted.add(CASHIER_INFO);
            fieldsToBeDeleted.add(ENTITY_PAYMENT_OPTION);
            fieldsToBeDeleted.add(USER_DETAILS);
            fieldsToBeDeleted.add(EXTEND_INFO);
            fieldsToBeDeleted.add(INITIATE_TXN_RESPONSE);
            nativeSessionUtil.deleteFields(txnToken, fieldsToBeDeleted);

            EXT_LOGGER.customInfo("Removed EnhancedNative Payload and few fields on txnToken");
        } catch (Exception e) {
            LOGGER.error("Exception in invalidatingEnhancedNativeData, ", e);
        }
    }

    public void settxnTokenTTL(EnhancedCashierPage enhancedCashierPage) {
        long ttl = nativeSessionUtil.fetchTTL(enhancedCashierPage.getTxnToken());
        enhancedCashierPage.settxnTokenTTL(ttl);
    }

    public Map<String, Object> getGroupedPayModes(PaymentRequestBean requestData,
            List<EnhancedCashierPagePayModeBase> payModes, String payMode) {
        String merchantPayModeGroupOrdering = getMerchantPayModeGroupSequence(
                requestData.getMid(),
                requestData.isSubscription() ? PayModeGroupSequenceEnum.SUBSCRIPTION : PayModeGroupSequenceEnum.ENHANCE,
                payMode);
        LOGGER.info("Grouped PayMode Ordering received for paymode {} : {}", payMode, merchantPayModeGroupOrdering);
        if (StringUtils.isNotBlank(merchantPayModeGroupOrdering)) {
            Map<String, Integer> groupNameIndexMap = new HashMap<>();
            String[] payModeGroups = merchantPayModeGroupOrdering.split(",");
            for (int index = 0; index < payModeGroups.length; index++)
                groupNameIndexMap.put(payModeGroups[index], index);
            return getGroupedPayModes(payModes, groupNameIndexMap);
        }
        return null;
    }

    private Map<String, Object> getGroupedPayModes(List<EnhancedCashierPagePayModeBase> payModes,
            Map<String, Integer> groupNameIndexMap) {
        if (payModes == null || payModes.isEmpty())
            return null;
        Map<String, EPayMethod> ePayMethodMap = PayMethodUtility.getEPayMethodMap();
        Map<String, Object> groupedPayModes = new HashMap<>();

        List<EnhancedCashierPagePayModeBase> payModeBases = new ArrayList<>(payModes);
        payModeBases.sort(Comparator.comparingInt(EnhancedCashierPagePayModeBase::getId));
        for (EnhancedCashierPagePayModeBase payMode : payModeBases) {
            EPayMethod ePayMethod = ePayMethodMap.get(payMode.getPayMethod());
            if (ePayMethod == null || ePayMethod.getMethodGroup() == null
                    || !groupNameIndexMap.containsKey(ePayMethod.getMethodGroup().getGroupName()))
                continue;
            switch (ePayMethod.getMethodGroup()) {
            case SAVED_VPA:
            case SAVED_MANDATE_BANK:
            case SAVED_CARD:
            case UPI_PROFILE:
                putInMap(ePayMethod.getMethodGroup(), payMode, groupNameIndexMap, groupedPayModes);
                break;
            case OTHER_OPTIONS:
            case PAYTM_FEATURED:
                List<EnhancedCashierPagePayModeBase> payModeBaseList = (List<EnhancedCashierPagePayModeBase>) groupedPayModes
                        .getOrDefault(ePayMethod.getMethodGroup().getDisplayName(), new ArrayList<>());
                payModeBaseList.add(payMode);
                putInMap(ePayMethod.getMethodGroup(), payModeBaseList, groupNameIndexMap, groupedPayModes);
                break;
            default:
                LOGGER.warn("PayModeGroup invalid for payMethod {}", ePayMethod);
            }
        }

        groupedPayModes = groupedPayModes.entrySet().stream()
                .sorted(new PayModeOrderUtil.PayModeGroupComparator(groupNameIndexMap))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
        return groupedPayModes;
    }

    private void putInMap(EPayMethodGroup ePayMethodGroup, Object value, Map<String, Integer> groupNameIndexMap,
            Map<String, Object> groupedPayOptions) {
        if (!groupNameIndexMap.containsKey(ePayMethodGroup.getGroupName()))
            return;
        groupedPayOptions.put(ePayMethodGroup.getDisplayName(), value);
    }

    public String getMerchantPayModeGroupSequence(String mid, PayModeGroupSequenceEnum payModeGroupSequenceEnum,
            String payMode) {
        String merchantPayModeGroupShortNameOrdering = merchantPreferenceService.getMerchantPaymodeGrpSequence(mid,
                payModeGroupSequenceEnum);
        LOGGER.info("merchantPayModeGroupShortNameOrdering preference received {}",
                merchantPayModeGroupShortNameOrdering);
        if (StringUtils.isNotBlank(merchantPayModeGroupShortNameOrdering)) {
            Map<String, EPayMethodGroup> shortNameEPayMethodGroupMap = PayMethodUtility
                    .getShortNameEPayMethodGroupMap();
            String[] merchantPayModeShortNames = merchantPayModeGroupShortNameOrdering.split(",");
            List<String> payModeGroups = new ArrayList<>();
            for (String shortName : merchantPayModeShortNames)
                if (shortNameEPayMethodGroupMap.containsKey(shortName))
                    payModeGroups.add(shortNameEPayMethodGroupMap.get(shortName).getGroupName());
            return StringUtils.join(payModeGroups, ",");
        }
        return ConfigurationUtil.getProperty(payMode, "");
    }

    /***
     *
     * @param mid
     * @param paymodeSequenceEnum
     * @param payModeSequence
     *            It get the group Priority based on PayMode Priority. The logic
     *            for this is implemented in this function
     * @return
     */
    public Map<String, Integer> getGroupPrioritybyPayMethodPriority(String mid,
            PaymodeSequenceEnum paymodeSequenceEnum, String payModeSequence) {

        LOGGER.info("PayMode Sequence :: {}",
                paymodeSequenceEnum == null ? "null" : paymodeSequenceEnum.getPreferenceName());
        Map<String, Integer> groupPriorities = new HashMap<>();
        String merchantBasedPaymodeStr = StringUtils.isNotBlank(payModeSequence) ? payModeSequence
                : merchantPreferenceService.getMerchantPaymodeSequence(mid, paymodeSequenceEnum);

        LOGGER.info("mapping pref got:: {}", merchantBasedPaymodeStr);

        List<EPayMethod> merchantPaymodesOrdering = new ArrayList<>();
        if (StringUtils.isNotEmpty(merchantBasedPaymodeStr)) {
            try {
                String[] orderedMerchantPaymodeList = merchantBasedPaymodeStr.split(",");
                for (String paymode : orderedMerchantPaymodeList) {
                    merchantPaymodesOrdering.add(EPayMethod.getPayMethodByShortName(paymode.trim()));
                }
            } catch (Exception ex) {
                LOGGER.error("Error in Merchant based paymodes sequencing: {}", ex);
                merchantPaymodesOrdering.clear();
            }
        }

        List<String> merchantPayMethodGroupOrdering = new ArrayList<>();
        if (merchantPaymodesOrdering.isEmpty()) {
            List<EPayMethodGroup> groupPayModesOrdering = new ArrayList<>();
            try {
                String orderedGroupPaymodeStr = ConfigurationUtil.getProperty("default.paymode.group.order", "");
                String[] orderedGroupPayModeList = orderedGroupPaymodeStr.split(",");
                for (String groupPayMode : orderedGroupPayModeList) {
                    groupPayModesOrdering.add(EPayMethodGroup.getEpayMethodGroupFromGroupName(groupPayMode.trim()));
                }
                if (!groupPayModesOrdering.isEmpty()) {
                    merchantPayMethodGroupOrdering = groupPayModesOrdering.stream().distinct()
                            .map(EPayMethodGroup::getDisplayName).filter(StringUtils::isNotBlank)
                            .collect(Collectors.toList());
                }
            } catch (Exception ex) {
                LOGGER.error("Error in Merchant based paymodes sequencing: {}", ex);
                groupPayModesOrdering.clear();
                merchantPayMethodGroupOrdering.clear();
            }
        } else {
            merchantPayMethodGroupOrdering = merchantPaymodesOrdering.stream().map(EPayMethod::getMethodGroup)
                    .distinct().map(EPayMethodGroup::getDisplayName).filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
        }

        if (merchantPayMethodGroupOrdering.isEmpty()) {
            LOGGER.error("Group priority sequence is null");
        }

        for (int index = 0; index < merchantPayMethodGroupOrdering.size(); index++) {
            if (merchantPayMethodGroupOrdering.get(index) != null) {
                groupPriorities.put(merchantPayMethodGroupOrdering.get(index), index + 1);
            }
        }

        // Add UPI_PROFILE also with same priority as SAVED_VPA
        if (groupPriorities.containsKey(EPayMethodGroup.SAVED_VPA.getDisplayName())) {
            int upiPriority = groupPriorities.get(EPayMethodGroup.SAVED_VPA.getDisplayName());
            groupPriorities.put(EPayMethodGroup.UPI_PROFILE.getDisplayName(), upiPriority);
        }
        return groupPriorities;
    }
}
