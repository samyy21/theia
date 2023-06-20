package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.EPayMethodGroup;
import com.paytm.pgplus.common.util.PayMethodUtility;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatraV4;
import com.paytm.pgplus.theia.nativ.model.payview.response.*;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.regex.Pattern;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.NATIVE_ONLINE_PAYMODES;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.NATIVE_PAYMODES;

/**
 * Util to provide ordering priority in fetch pay instruments response
 */

@Service
public class PayModeOrderUtil {

    private static Ff4jUtils ff4jUtils;

    @Autowired
    private Ff4jUtils ff4jUtils1;

    @PostConstruct
    private void initStaticBean() {
        ff4jUtils = this.ff4jUtils1;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(PayModeOrderUtil.class);

    private static Map<EPayMethod, Integer> savedCardOrdering = new LinkedHashMap<>();
    private static Map<EPayMethod, Integer> addMoneySavedCardOrdering = new LinkedHashMap<>();

    private PayModeOrderUtil() {
    }

    public static void payModeOrdering(NativeCashierInfoResponse unorderedResponse, String merchantBasedPaymodeStr,
            boolean offlineFlow) {
        List<EPayMethod> merchantPaymodesOrdering = fetchMerchantPaymodesOrdering(merchantBasedPaymodeStr, offlineFlow);
        // List<EPayMethod> addMoneyPaymodesOrdering =
        // fetchAddMoneyPaymodesOrdering();
        List<PayMethod> payMethods;
        List<PayChannelBase> savedCards = null;
        UserProfileSarvatra savedVpa = null;
        UserProfileSarvatraV4 upiProfile = null;
        List<SavedMandateBank> savedMandateBankList = null;
        if (unorderedResponse.getBody().getMerchantPayOption() != null && merchantPaymodesOrdering.get(0) != null) {
            if (unorderedResponse.getBody().getMerchantPayOption().getSavedInstruments() != null) {
                savedCards = unorderedResponse.getBody().getMerchantPayOption().getSavedInstruments();
                savedVpa = unorderedResponse.getBody().getMerchantPayOption().getUserProfileSarvatra();
                upiProfile = unorderedResponse.getBody().getMerchantPayOption().getUpiProfileV4();
            }

            if (unorderedResponse.getBody().getMerchantPayOption().getSavedMandateBanks() != null) {
                savedMandateBankList = unorderedResponse.getBody().getMerchantPayOption().getSavedMandateBanks();
            }
            payMethods = unorderedResponse.getBody().getMerchantPayOption().getPayMethods();
            orderingOfPayModes(savedCards, payMethods, savedVpa, merchantPaymodesOrdering, unorderedResponse.getBody()
                    .getMerchantPayOption().getSavedInstruments(), upiProfile, savedMandateBankList, offlineFlow);
        }
        savedCards = null;
        savedVpa = null;
        upiProfile = null;
        savedMandateBankList = null;
        if (unorderedResponse.getBody().getAddMoneyPayOption() != null && merchantPaymodesOrdering.get(0) != null) {
            if (unorderedResponse.getBody().getAddMoneyPayOption().getSavedInstruments() != null) {
                savedCards = unorderedResponse.getBody().getAddMoneyPayOption().getSavedInstruments();
                savedVpa = unorderedResponse.getBody().getAddMoneyPayOption().getUserProfileSarvatra();
                upiProfile = unorderedResponse.getBody().getAddMoneyPayOption().getUpiProfileV4();
            }
            if (unorderedResponse.getBody().getAddMoneyPayOption().getSavedMandateBanks() != null) {
                savedMandateBankList = unorderedResponse.getBody().getAddMoneyPayOption().getSavedMandateBanks();
            }
            payMethods = unorderedResponse.getBody().getAddMoneyPayOption().getPayMethods();
            orderingOfPayModes(savedCards, payMethods, savedVpa, merchantPaymodesOrdering, unorderedResponse.getBody()
                    .getAddMoneyPayOption().getSavedInstruments(), upiProfile, savedMandateBankList, offlineFlow);
        }
    }

    /*
     * private static List<EPayMethod> fetchAddMoneyPaymodesOrdering() {
     * List<EPayMethod> addMoneyPaymodesOrdering = new ArrayList<>(); String
     * orderedAddMoneyPaymodeStr =
     * ConfigurationUtil.getProperty("offline.addmoney.paymode.order", "");
     * String[] orderedAddMoneyPaymodeList =
     * orderedAddMoneyPaymodeStr.split(Pattern.quote(",")); for (String paymode
     * : orderedAddMoneyPaymodeList) {
     * addMoneyPaymodesOrdering.add(EPayMethod.getPayMethodByMethod
     * (paymode.trim())); } return addMoneyPaymodesOrdering; }
     */
    private static List<EPayMethod> fetchMerchantPaymodesOrdering(String merchantBasedPaymodeStr, boolean offlineflow) {
        List<EPayMethod> merchantPaymodesOrdering = new ArrayList<>();
        if (StringUtils.isNotEmpty(merchantBasedPaymodeStr)) {
            try {
                String[] orderedMerchantPaymodeList = merchantBasedPaymodeStr.split(Pattern.quote(","));
                for (String paymode : orderedMerchantPaymodeList) {
                    EPayMethod ePayMethod = PayMethodUtility.getPayMethodByShortName(paymode.trim());
                    if (ePayMethod != null)
                        merchantPaymodesOrdering.add(ePayMethod);
                }
            } catch (Exception ex) {
                LOGGER.error("Error in Native flow in Merchant based paymodes sequencing for priority : ", ex);
            }
        } else {
            if (offlineflow) {
                merchantBasedPaymodeStr = ff4jUtils.getPropertyAsStringWithDefault(NATIVE_PAYMODES, "");
                if (StringUtils.isEmpty(merchantBasedPaymodeStr))
                    merchantBasedPaymodeStr = ConfigurationUtil.getProperty(NATIVE_PAYMODES, "");
            } else {
                merchantBasedPaymodeStr = ff4jUtils.getPropertyAsStringWithDefault(NATIVE_ONLINE_PAYMODES, "");
                if (StringUtils.isEmpty(merchantBasedPaymodeStr))
                    merchantBasedPaymodeStr = ConfigurationUtil.getProperty(NATIVE_ONLINE_PAYMODES, "");
            }
            try {
                String[] orderedMerchantPaymodeList = merchantBasedPaymodeStr.split(Pattern.quote(","));
                for (String paymode : orderedMerchantPaymodeList) {
                    EPayMethod ePayMethod = PayMethodUtility.getPayMethodByMethod(paymode.trim());
                    if (ePayMethod != null)
                        merchantPaymodesOrdering.add(ePayMethod);
                }
            } catch (Exception ex) {
                LOGGER.error("Error in Native flow in Merchant based paymodes sequencing : ", ex);
            }
        }

        if (!merchantPaymodesOrdering.contains(EPayMethod.UPI_LITE)) {
            if (offlineflow && merchantPaymodesOrdering.contains(EPayMethod.UPI))
                merchantPaymodesOrdering.add(merchantPaymodesOrdering.indexOf(EPayMethod.UPI), EPayMethod.UPI_LITE);
            else if (!offlineflow && merchantPaymodesOrdering.contains(EPayMethod.SAVED_VPA))
                merchantPaymodesOrdering.add(merchantPaymodesOrdering.indexOf(EPayMethod.SAVED_VPA),
                        EPayMethod.UPI_LITE);
            else
                merchantPaymodesOrdering.add(EPayMethod.UPI_LITE);
        }
        return merchantPaymodesOrdering;
    }

    private static void orderingOfPayModes(List<PayChannelBase> savedCards, List<PayMethod> payMethods,
            UserProfileSarvatra savedVpa, List<EPayMethod> merchantPaymodesOrdering, List<PayChannelBase> savedCard,
            UserProfileSarvatraV4 upiProfile, List<SavedMandateBank> savedMandateBankList, boolean offlineFlow) {
        if (savedCards != null)
            Collections.sort(savedCards, SortingComparator.SAVED_CARD_COMPARATOR);
        int merchantMethodCounter = 1;
        Iterator<EPayMethod> methodIterator = merchantPaymodesOrdering.iterator();
        while (methodIterator.hasNext()) {
            EPayMethod method = methodIterator.next();
            if (EPayMethod.SAVED_CARD.equals(method) && CollectionUtils.isNotEmpty(savedCards)) {
                Iterator<PayChannelBase> savedCardIterator = savedCards.iterator();
                while (savedCardIterator.hasNext()) {
                    SavedCard card = (SavedCard) savedCardIterator.next();
                    card.setPriority(String.valueOf(merchantMethodCounter));
                    merchantMethodCounter++;
                }
            } else if (EPayMethod.SAVED_VPA.equals(method)) {
                if (null != savedVpa && savedVpa.getResponse() != null
                        && savedVpa.getResponse().getVpaDetails() != null) {
                    savedVpa.setPriority(String.valueOf(merchantMethodCounter));
                    merchantMethodCounter++;
                } else if (null != upiProfile
                        && ((upiProfile.isUpiOnboarding()) || (upiProfile.getRespDetails() != null && upiProfile
                                .getRespDetails().getProfileDetail() != null))) {
                    upiProfile.setPriority(String.valueOf(merchantMethodCounter));
                    merchantMethodCounter++;
                }
            } else if (EPayMethod.SAVED_MANDATE_BANK.equals(method) && CollectionUtils.isNotEmpty(savedMandateBankList)) {
                for (SavedMandateBank savedMandateBank : savedMandateBankList) {
                    savedMandateBank.setPriority(String.valueOf(merchantMethodCounter));
                    merchantMethodCounter++;
                }
            } else if (payMethods != null) {
                Iterator<PayMethod> payMethodIterator = payMethods.iterator();
                while (payMethodIterator.hasNext()) {
                    PayMethod payMethod = payMethodIterator.next();
                    if (payMethod.getPayMethod().equals(method.getMethod())
                            || payMethod.getPayMethod().equals(method.name())) {
                        payMethod.setPriority(String.valueOf(merchantMethodCounter));
                        merchantMethodCounter++;
                        break;
                    }
                }
            }
        }
        orderingOfLeftOutItems(savedCards, savedVpa, upiProfile, payMethods, savedMandateBankList,
                merchantMethodCounter, offlineFlow);
    }

    private static void orderingOfLeftOutItems(List<PayChannelBase> savedCards, UserProfileSarvatra savedVpa,
            UserProfileSarvatraV4 upiProfile, List<PayMethod> payMethods, List<SavedMandateBank> savedMandateBankList,
            int merchantMethodCounter, boolean offlineFlow) {

        if (savedCards != null) {
            Iterator<PayChannelBase> savedCardIterator = savedCards.iterator();
            while (savedCardIterator.hasNext()) {
                SavedCard card = (SavedCard) savedCardIterator.next();
                if (card.getPriority() == null) {
                    card.setPriority(String.valueOf(merchantMethodCounter));
                    merchantMethodCounter++;
                }
            }
        }

        if (savedVpa != null && savedVpa.getResponse() != null && savedVpa.getResponse().getVpaDetails() != null
                && StringUtils.isBlank(savedVpa.getPriority())) {
            savedVpa.setPriority(String.valueOf(merchantMethodCounter));
            merchantMethodCounter++;
        }

        if (savedMandateBankList != null) {
            for (SavedMandateBank savedMandateBank : savedMandateBankList) {
                if (StringUtils.isBlank(savedMandateBank.getPriority())) {
                    savedMandateBank.setPriority(String.valueOf(merchantMethodCounter));
                    merchantMethodCounter++;
                }
            }
        }
        if (upiProfile != null
                && ((upiProfile.isUpiOnboarding()) || (upiProfile.getRespDetails() != null && upiProfile
                        .getRespDetails().getProfileDetail() != null)) && StringUtils.isBlank(upiProfile.getPriority())) {
            upiProfile.setPriority(String.valueOf(merchantMethodCounter));
            merchantMethodCounter++;
        }

        if (payMethods != null) {
            Iterator<PayMethod> payMethodIterator = payMethods.iterator();
            while (payMethodIterator.hasNext()) {
                PayMethod payMethod = payMethodIterator.next();
                if (payMethod.getPriority() == null) {
                    payMethod.setPriority(String.valueOf(merchantMethodCounter));
                    merchantMethodCounter++;
                }
            }
        }

    }

    enum SortingComparator implements Comparator<PayChannelBase> {
        SAVED_CARD_COMPARATOR {
            @Override
            public int compare(PayChannelBase savedCard1, PayChannelBase savedCard2) {
                return compareSavedCards(savedCardOrdering, savedCard1, savedCard2);
            }
        };

        static {
            String orderedSavedCardStr = ConfigurationUtil.getProperty("native.savedcards.order", "");

            String[] orderedSavedCardList = orderedSavedCardStr.split(Pattern.quote(","));

            int merchantPayOptionPriority = 1;
            for (String savedCardType : orderedSavedCardList) {
                savedCardOrdering.put(EPayMethod.getPayMethodByMethod(savedCardType.trim()),
                        merchantPayOptionPriority++);
            }

        }

        private static int compareSavedCards(Map<EPayMethod, Integer> ordering, PayChannelBase savedCard1,
                PayChannelBase savedCard2) {

            return getPriority(ordering, savedCard1).compareTo(getPriority(ordering, savedCard2));
        }

        private static Integer getPriority(Map<EPayMethod, Integer> ordering, PayChannelBase savedCard) {
            Integer priority = ordering.get(EPayMethod.getPayMethodByMethod(savedCard.getPayMethod()));
            if (priority == null)
                return Integer.MAX_VALUE;
            if (((SavedCard) savedCard).isCardCoft()
                    && StringUtils.containsIgnoreCase(((SavedCard) savedCard).getBankName(),
                            BizConstant.COBRANDED_COFT_CARD_PREFIX)) {
                priority -= ordering.size();
                ((SavedCard) savedCard).setIsCardCoBranded(true);
            }
            return priority;
        }
    }

    public static class PayModeGroupComparator implements Comparator<Map.Entry<String, Object>> {

        Map<String, EPayMethodGroup> displayNameEPayMethodGroupMap;
        Map<String, Integer> groupNameIndexMap;

        public PayModeGroupComparator(Map<String, Integer> groupNameIndexMap) {
            this.displayNameEPayMethodGroupMap = PayMethodUtility.getDisplayNameEPayMethodGroupMap();
            this.groupNameIndexMap = groupNameIndexMap;
        }

        @Override
        public int compare(Map.Entry<String, Object> entry1, Map.Entry<String, Object> entry2) {
            if (!displayNameEPayMethodGroupMap.containsKey(entry1.getKey())
                    || !groupNameIndexMap
                            .containsKey(displayNameEPayMethodGroupMap.get(entry1.getKey()).getGroupName()))
                return 1;
            else if (!displayNameEPayMethodGroupMap.containsKey(entry2.getKey())
                    || !groupNameIndexMap
                            .containsKey(displayNameEPayMethodGroupMap.get(entry2.getKey()).getGroupName()))
                return -1;
            return groupNameIndexMap.get(displayNameEPayMethodGroupMap.get(entry1.getKey()).getGroupName())
                    - groupNameIndexMap.get(displayNameEPayMethodGroupMap.get(entry2.getKey()).getGroupName());
        }
    }
}
