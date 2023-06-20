package com.paytm.pgplus.theia.offline.utils;

import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.theia.offline.model.payview.PayMethod;
import com.paytm.pgplus.theia.offline.model.payview.SavedCard;
import com.paytm.pgplus.theia.offline.model.payview.SavedInstruments;
import com.paytm.pgplus.theia.offline.model.response.CashierInfoResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.OFFLINE_ADD_MONEY_PAYMODES;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.OFFLINE_PAYMODES;

/**
 * Util to provide ordering priority in fetch pay instruments response
 */

public class PayModeOrderUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PayModeOrderUtil.class);

    private static Map<EPayMethod, Integer> savedCardOrdering = new LinkedHashMap<>();

    private PayModeOrderUtil() {
    }

    public static void payModeOrdering(CashierInfoResponse unorderedResponse, String merchantBasedPaymodeStr) {
        List<EPayMethod> merchantPaymodesOrdering = fetchMerchantPaymodesOrdering(merchantBasedPaymodeStr);
        List<EPayMethod> addMoneyPaymodesOrdering = fetchAddMoneyPaymodesOrdering(merchantBasedPaymodeStr);
        List<PayMethod> payMethods;
        List<SavedCard> savedCards = null;
        List<String> sarvatraList = null;
        if (unorderedResponse.getBody().getPayMethodViews() != null && merchantPaymodesOrdering.get(0) != null) {
            if (unorderedResponse.getBody().getPayMethodViews().getMerchantSavedInstruments() != null) {
                savedCards = unorderedResponse.getBody().getPayMethodViews().getMerchantSavedInstruments()
                        .getSavedCards();
                sarvatraList = unorderedResponse.getBody().getPayMethodViews().getMerchantSavedInstruments()
                        .getSarvatraVpa();
            }
            payMethods = unorderedResponse.getBody().getPayMethodViews().getMerchantPayMethods();
            orderingOfPayModes(savedCards, payMethods, sarvatraList, merchantPaymodesOrdering, unorderedResponse
                    .getBody().getPayMethodViews().getMerchantSavedInstruments());
        }
        savedCards = null;
        sarvatraList = null;
        if (unorderedResponse.getBody().getPayMethodViews() != null && merchantPaymodesOrdering.get(0) != null) {
            if (unorderedResponse.getBody().getPayMethodViews().getAddMoneySavedInstruments() != null) {
                savedCards = unorderedResponse.getBody().getPayMethodViews().getAddMoneySavedInstruments()
                        .getSavedCards();
                sarvatraList = unorderedResponse.getBody().getPayMethodViews().getAddMoneySavedInstruments()
                        .getSarvatraVpa();
            }
            payMethods = unorderedResponse.getBody().getPayMethodViews().getAddMoneyPayMethods();
            orderingOfPayModes(savedCards, payMethods, sarvatraList, addMoneyPaymodesOrdering, unorderedResponse
                    .getBody().getPayMethodViews().getAddMoneySavedInstruments());
        }
    }

    private static List<EPayMethod> fetchAddMoneyPaymodesOrdering(String merchantBasedPaymodeStr) {
        return getListOfEPayMethods(merchantBasedPaymodeStr, OFFLINE_ADD_MONEY_PAYMODES);
    }

    private static List<EPayMethod> fetchMerchantPaymodesOrdering(String merchantBasedPaymodeStr) {
        return getListOfEPayMethods(merchantBasedPaymodeStr, OFFLINE_PAYMODES);
    }

    private static List<EPayMethod> getListOfEPayMethods(String merchantBasedPaymodeStr, String offLineFlow) {
        List<EPayMethod> merchantPaymodesOrdering = new ArrayList<>();
        if (StringUtils.isNotEmpty(merchantBasedPaymodeStr)) {
            try {
                String[] orderedMerchantPaymodeList = merchantBasedPaymodeStr.split(Pattern.quote(","));
                for (String paymode : orderedMerchantPaymodeList) {
                    if (StringUtils.isNotBlank(paymode.trim())) {
                        merchantPaymodesOrdering.add(EPayMethod.getPayMethodByShortName(paymode.trim()));
                    }
                }
                return merchantPaymodesOrdering;
            } catch (Exception ex) {
                LOGGER.error("Error in Offline flow in Merchant based paymodes sequencing: {}", ex);
                merchantPaymodesOrdering.clear();
            }
        }
        String orderedMerchantPaymodeStr = ConfigurationUtil.getProperty(offLineFlow, "");
        String[] orderedMerchantPaymodeList = orderedMerchantPaymodeStr.split(Pattern.quote(","));
        for (String paymode : orderedMerchantPaymodeList) {
            if (StringUtils.isNotBlank(paymode.trim())) {
                merchantPaymodesOrdering.add(EPayMethod.getPayMethodByMethod(paymode.trim()));
            }
        }
        return merchantPaymodesOrdering;
    }

    private static void orderingOfPayModes(List<SavedCard> savedCards, List<PayMethod> payMethods,
            List<String> sarvatraList, List<EPayMethod> merchantPaymodesOrdering, SavedInstruments savedInstruments) {
        if (savedCards != null)
            Collections.sort(savedCards, SortingComparator.SAVED_CARD_COMPARATOR);
        int merchantMethodCounter = 1;
        Iterator<EPayMethod> methodIterator = merchantPaymodesOrdering.iterator();
        while (methodIterator.hasNext()) {
            EPayMethod method = methodIterator.next();
            if (EPayMethod.SAVED_CARD.equals(method) && CollectionUtils.isNotEmpty(savedCards)) {
                Iterator<SavedCard> savedCardIterator = savedCards.iterator();
                while (savedCardIterator.hasNext()) {
                    SavedCard card = savedCardIterator.next();
                    card.setPriority(merchantMethodCounter);
                    merchantMethodCounter++;
                }
            } else if (EPayMethod.SAVED_VPA.equals(method) && sarvatraList != null && !sarvatraList.isEmpty()) {
                savedInstruments.setSarvatraVpaPriority(merchantMethodCounter);
                merchantMethodCounter++;
            } else if (payMethods != null) {
                Iterator<PayMethod> payMethodIterator = payMethods.iterator();
                while (payMethodIterator.hasNext()) {
                    PayMethod payMethod = payMethodIterator.next();
                    if (payMethod.getPayMethod().equals(method.getMethod())) {
                        payMethod.setPriority(merchantMethodCounter);
                        merchantMethodCounter++;
                        break;
                    }
                }
            }
        }
        orderingOfLeftOutItems(savedCards, savedInstruments, sarvatraList, payMethods, merchantMethodCounter);
    }

    private static void orderingOfLeftOutItems(List<SavedCard> savedCards, SavedInstruments savedInstruments,
            List<String> sarvatraList, List<PayMethod> payMethods, int merchantMethodCounter) {
        if (savedCards != null) {
            Iterator<SavedCard> savedCardIterator = savedCards.iterator();
            while (savedCardIterator.hasNext()) {
                SavedCard card = savedCardIterator.next();
                if (card.getPriority() == 0) {
                    card.setPriority(merchantMethodCounter);
                    merchantMethodCounter++;
                }
            }
        }
        if (CollectionUtils.isNotEmpty(sarvatraList) && savedInstruments != null
                && savedInstruments.getSarvatraVpaPriority() == 0) {
            savedInstruments.setSarvatraVpaPriority(merchantMethodCounter);
            merchantMethodCounter++;
        }
        if (payMethods != null) {
            Iterator<PayMethod> payMethodIterator = payMethods.iterator();
            while (payMethodIterator.hasNext()) {
                PayMethod payMethod = payMethodIterator.next();
                if (payMethod.getPriority() == 0) {
                    payMethod.setPriority(merchantMethodCounter);
                    merchantMethodCounter++;
                }
            }
        }

    }

    enum SortingComparator implements Comparator<SavedCard> {
        SAVED_CARD_COMPARATOR {
            @Override
            public int compare(SavedCard savedCard1, SavedCard savedCard2) {
                return compareSavedCards(savedCardOrdering, savedCard1, savedCard2);
            }
        };

        private static int compareSavedCards(Map<EPayMethod, Integer> ordering, SavedCard savedCard1,
                SavedCard savedCard2) {
            return getOrderValue(ordering, EPayMethod.getPayMethodByMethod(savedCard1.getCardDetails().getCardType()))
                    .compareTo(
                            getOrderValue(ordering,
                                    EPayMethod.getPayMethodByMethod(savedCard2.getCardDetails().getCardType())));
        }

        static {
            String orderedSavedCardStr = ConfigurationUtil.getProperty("offline.savedcards.order", "");

            String[] orderedSavedCardList = orderedSavedCardStr.split(Pattern.quote(","));

            int i = 1;
            for (String savedCardType : orderedSavedCardList) {
                savedCardOrdering.put(EPayMethod.getPayMethodByMethod(savedCardType.trim()), i++);
            }
        }

        private static Integer getOrderValue(Map<EPayMethod, Integer> ordering, EPayMethod key) {
            Integer i = ordering.get(key);
            return i == null ? 100 : i;
        }
    }
}
