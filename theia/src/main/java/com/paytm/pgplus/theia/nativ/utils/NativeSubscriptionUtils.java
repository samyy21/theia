package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.common.enums.AmountType;
import com.paytm.pgplus.common.enums.FrequencyUnit;
import com.paytm.pgplus.common.enums.SubsPaymentMode;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.request.SubscriptionTransactionRequestBody;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.model.subscription.SubscriptionDetail;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class NativeSubscriptionUtils {

    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeSubscriptionUtils.class);

    // subscription-message properties
    private static final String SUBSDETAIL_FREQUENCY_ON_DEMAND = "subscription.detail.frequency.message.ondemand";
    private static final String SUBSDETAIL_FREQUENCY_MONTH = "subscription.detail.frequency.message.month";
    private static final String SUBSDETAIL_FREQUENCY_MONTHS = "subscription.detail.frequency.message.months";
    private static final String SUBSDETAIL_FREQUENCY_DAY = "subscription.detail.frequency.message.day";
    private static final String SUBSDETAIL_FREQUENCY_DAYS = "subscription.detail.frequency.message.days";
    private static final String SUBSDETAIL_FREQUENCY_WEEK = "subscription.detail.frequency.message.week";
    private static final String SUBSDETAIL_FREQUENCY_WEEKS = "subscription.detail.frequency.message.weeks";
    private static final String SUBSDETAIL_FREQUENCY_YEAR = "subscription.detail.frequency.message.year";
    private static final String SUBSDETAIL_FREQUENCY_YEARS = "subscription.detail.frequency.message.years";
    private static final String SUBSDETAIL_FREQUENCY_QUARTER = "subscription.detail.frequency.message.quarter";
    private static final String SUBSDETAIL_FREQUENCY_BIMONTH = "subscription.detail.frequency.message.bimonth";
    private static final String SUBSDETAIL_FREQUENCY_SEMIANNUAL = "subscription.detail.frequency.message.semiannual";
    private static final String SUBSDETAIL_FREQUENCY_FORTNIGHT = "subscription.detail.frequency.message.fortnight";
    private static final String SUBSDETAIL_DUEDATE_MESSAGE = "subscription.detail.paymentdue.message";
    private static final String SUBSDETAIL_WALLET_RENEW_MESSAGE = "subscription.detail.wallet.renew.message";
    private static final String SUBSDETAIL_NON_WALLET_RENEW_MESSAGE = "subscription.detail.nonwallet.renew.message";
    private static final String SUBSDETAIL_INFO_MESSAGE = "subscription.detail.info.message";
    private static final String SUBSDETAIL_AMOUNT_MESSAGE = "subscription.detail.amount.message";

    private static final String FREQUENCY_KEY = "frequencyKey";
    private static final String FREQUENCY_VALUE = "frequencyValue";
    private static final String NEXT_PAYMENT_KEY = "nextPaymentKey";
    private static final String NEXT_PAYMENT_VALUE = "nextPaymentValue";

    // default values
    private static final String SUBSDETAIL_DUEDATE_MESSAGE_DEFAULT_VALUE = "Next payment due on";
    private static final String BLANK = " ";

    // fetch subcription-details using requestdata and subscription-response
    public static SubscriptionDetail getSubscriptionDetail(SubscriptionTransactionRequestBody requestData,
            SubscriptionResponse subscriptionResponse) {
        SubscriptionDetail subscriptionDetail = new SubscriptionDetail();
        if (subscriptionResponse != null) {
            subscriptionDetail.setSubsId(subscriptionResponse.getSubscriptionId());
            subscriptionDetail.setSubsType(requestData.getSubscriptionAmountType());
            subscriptionDetail.setSubsMaxAmount(requestData.getSubscriptionMaxAmount());
            subscriptionDetail.setEnabled(true);
            boolean showSubsDueDate = showSubsDueDate(subscriptionResponse, requestData);
            boolean showSubsDisplayAmount = showSubsDisplayAmount(subscriptionResponse, requestData);
            if (showSubsDueDate || showSubsDisplayAmount) {
                subscriptionDetail.setDetails(createSubscriptionDetail(requestData, subscriptionResponse,
                        showSubsDueDate, showSubsDisplayAmount));
                if (MapUtils.isNotEmpty(subscriptionDetail.getDetails())) {
                    subscriptionDetail.setShowDetails(true);
                }
            }
            subscriptionDetail.setSubscriptionPurpose(subscriptionResponse.getSubsPurpose());
            subscriptionDetail.setStartDate(subscriptionResponse.getSubsStartDate());
            subscriptionDetail.setEndDate(subscriptionResponse.getSubscriptionExpiryDate());
            subscriptionDetail.setMaxAmount(AmountUtils.getTransactionAmountInRupee(subscriptionResponse
                    .getSubsMaxAmount()));
            subscriptionDetail.setSubsfrequency(getSubsFrequency(subscriptionResponse));
            if (StringUtils.isNotBlank(subscriptionResponse.getPaymentMid())
                    && StringUtils.isNotBlank(subscriptionResponse.getPaymentOrderId())) {
                subscriptionDetail.setAutoRefund(true);
            }
        }
        LOGGER.info("SubscriptionDetails returned: {}", subscriptionDetail);
        return subscriptionDetail;
    }

    private static String getSubsFrequency(SubscriptionResponse subscriptionResponse) {
        FrequencyUnit frequencyUnit;
        try {
            frequencyUnit = FrequencyUnit.getFrequencyUnitbyValue(Integer.parseInt(subscriptionResponse
                    .getSubsFreqUnit()));
        } catch (Exception e) {
            return subscriptionResponse.getSubsFreqUnit();
        }
        if (FrequencyUnit.ONDEMAND.equals(frequencyUnit)) {
            return frequencyUnit.name();
        } else {
            return subscriptionResponse.getSubsFreq() + BLANK + frequencyUnit.name();
        }

    }

    public static Map<String, String> createSubscriptionDetail(SubscriptionTransactionRequestBody requestData,
            SubscriptionResponse subscriptionResponse, boolean showSubsDueDate, boolean showSubsDisplayAmount) {

        // Subscription details map
        Map<String, String> details = new HashMap<>();

        // Add messages in subscriptiondetail array
        if (showSubsDisplayAmount) {
            setFrequencyMessageDetail(requestData, details);
        }
        if (showSubsDueDate) {
            setDueDateMessageDetail(subscriptionResponse, details);
        }

        return (details.isEmpty() ? null : details);
    }

    public static void setFrequencyMessageDetail(SubscriptionTransactionRequestBody requestData,
            Map<String, String> details) {

        String freqMessageKey = getFreqMessagePropertyValue(requestData.getSubscriptionFrequency(),
                requestData.getSubscriptionFrequencyUnit());
        String subsMaxAmountString = getSubscriptionDisplayAmount(requestData);
        if (freqMessageKey != null && StringUtils.isNotEmpty(subsMaxAmountString)) {
            details.put(FREQUENCY_KEY, freqMessageKey);
            details.put(FREQUENCY_VALUE, subsMaxAmountString);
        }
    }

    public static void setDueDateMessageDetail(SubscriptionResponse subscriptionResponse, Map<String, String> details) {
        if (StringUtils.isNotBlank(subscriptionResponse.getNextDueDate())) {
            String dueDateMessageKey = ConfigurationUtil.getMessageProperty(SUBSDETAIL_DUEDATE_MESSAGE,
                    SUBSDETAIL_DUEDATE_MESSAGE_DEFAULT_VALUE);
            details.put(NEXT_PAYMENT_KEY, dueDateMessageKey);
            details.put(NEXT_PAYMENT_VALUE, getSubscriptionDueDate(subscriptionResponse.getNextDueDate()));
        }
    }

    public static String getFreqMessagePropertyValue(String subsFreq, String subsFreqUnit) {
        String key = null;
        if (StringUtils.isNumeric(subsFreq) && StringUtils.isNotBlank(subsFreqUnit)) {
            if (FrequencyUnit.MONTH.getName().equalsIgnoreCase(subsFreqUnit)) {
                if (Integer.parseInt(subsFreq) > 1) {
                    key = MessageFormat.format(ConfigurationUtil.getMessageProperty(SUBSDETAIL_FREQUENCY_MONTHS),
                            subsFreq);
                } else if (Integer.parseInt(subsFreq) == 1) {
                    key = ConfigurationUtil.getMessageProperty(SUBSDETAIL_FREQUENCY_MONTH);
                }
            } else if (FrequencyUnit.DAY.getName().equalsIgnoreCase(subsFreqUnit)) {
                if (Integer.parseInt(subsFreq) > 1) {
                    key = String.valueOf(MessageFormat.format(
                            ConfigurationUtil.getMessageProperty(SUBSDETAIL_FREQUENCY_DAYS), subsFreq));
                } else if (Integer.parseInt(subsFreq) == 1) {
                    key = ConfigurationUtil.getMessageProperty(SUBSDETAIL_FREQUENCY_DAY);
                }
            } else if (FrequencyUnit.WEEK.getName().equalsIgnoreCase(subsFreqUnit)) {
                if (Integer.parseInt(subsFreq) > 1) {
                    key = String.valueOf(MessageFormat.format(
                            ConfigurationUtil.getMessageProperty(SUBSDETAIL_FREQUENCY_WEEKS), subsFreq));
                } else if (Integer.parseInt(subsFreq) == 1) {
                    key = ConfigurationUtil.getMessageProperty(SUBSDETAIL_FREQUENCY_WEEK);
                }
            } else if (FrequencyUnit.YEAR.getName().equalsIgnoreCase(subsFreqUnit)) {
                if (Integer.parseInt(subsFreq) > 1) {
                    key = String.valueOf(MessageFormat.format(
                            ConfigurationUtil.getMessageProperty(SUBSDETAIL_FREQUENCY_YEARS), subsFreq));
                } else if (Integer.parseInt(subsFreq) == 1) {
                    key = ConfigurationUtil.getMessageProperty(SUBSDETAIL_FREQUENCY_YEAR);
                }
            } else if (FrequencyUnit.QUARTER.getName().equalsIgnoreCase(subsFreqUnit)) {
                if (Integer.parseInt(subsFreq) == 1) {
                    key = ConfigurationUtil.getMessageProperty(SUBSDETAIL_FREQUENCY_QUARTER);
                }
            } else if (FrequencyUnit.BI_MONTHLY.getName().equalsIgnoreCase(subsFreqUnit)) {
                if (Integer.parseInt(subsFreq) == 1) {
                    key = ConfigurationUtil.getMessageProperty(SUBSDETAIL_FREQUENCY_BIMONTH);
                }
            } else if (FrequencyUnit.SEMI_ANNUALLY.getName().equalsIgnoreCase(subsFreqUnit)) {
                if (Integer.parseInt(subsFreq) == 1) {
                    key = ConfigurationUtil.getMessageProperty(SUBSDETAIL_FREQUENCY_SEMIANNUAL);
                }
            } else if (FrequencyUnit.FORTNIGHT.getName().equalsIgnoreCase(subsFreqUnit)) {
                if (Integer.parseInt(subsFreq) == 1) {
                    key = ConfigurationUtil.getMessageProperty(SUBSDETAIL_FREQUENCY_FORTNIGHT);
                }
            } else if (FrequencyUnit.ONDEMAND.getName().equalsIgnoreCase(subsFreqUnit)) {
                if (Integer.parseInt(subsFreq) == 1) {
                    key = ConfigurationUtil.getMessageProperty(SUBSDETAIL_FREQUENCY_ON_DEMAND);
                }
            }
        }
        return key;
    }

    // get subscription-amount to be displayed on ui
    public static String getSubscriptionDisplayAmount(SubscriptionTransactionRequestBody requestBody) {
        String subsMaxAmountString = null;
        try {
            Double amount = Double.parseDouble(requestBody.getSubscriptionMaxAmount());
            if (StringUtils.equals(requestBody.getSubscriptionAmountType(), AmountType.FIX.getName())
                    && StringUtils.isNotBlank(requestBody.getRenewalAmount())) {
                amount = Double.parseDouble(requestBody.getRenewalAmount());
            }
            subsMaxAmountString = String.format("%.02f", amount);
        } catch (Exception ex) {
            LOGGER.info("Invalid subsMaxAmount");
            subsMaxAmountString = null;
        }
        return subsMaxAmountString;
    }

    // get Subscription-duedate to be displayed on ui
    public static String getSubscriptionDueDate(String subsDueDate) {
        if (StringUtils.isNotBlank(subsDueDate)) {
            try {
                Date date = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy").parse(subsDueDate);
                return new SimpleDateFormat("dd MMMM yyyy").format(date);
            } catch (Exception e) {
                LOGGER.info("Invalid SubscriptionDueDate");
            }
        }
        return null;
    }

    public static boolean showSubsDueDate(SubscriptionResponse subscriptionResponse,
            SubscriptionTransactionRequestBody requestData) {
        boolean showDueDate = false;
        if (!(FrequencyUnit.DAY.getName().equalsIgnoreCase(requestData.getSubscriptionFrequencyUnit()) && "1"
                .equalsIgnoreCase(requestData.getSubscriptionFrequency()))) {

            // check for subscription-start-date same as creation-date
            try {
                Date startDate = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy").parse(subscriptionResponse
                        .getSubsStartDate());
                Date today = new Date();
                if (!DateUtils.isSameDay(today, startDate)) {
                    showDueDate = true;
                }
            } catch (Exception ex) {
                LOGGER.info("Invalid SubscriptionStartDate");
            }
        }
        return showDueDate;
    }

    public static boolean showSubsDisplayAmount(SubscriptionResponse subscriptionResponse,
            SubscriptionTransactionRequestBody requestData) {
        return !(FrequencyUnit.DAY.getName().equalsIgnoreCase(requestData.getSubscriptionFrequencyUnit()) && "1"
                .equalsIgnoreCase(subscriptionResponse.getSubsFreq()));
    }

    public static String calculateDisplayButtonAmount(String txnAmountStr, Double walletAmount, String subsPaymentMode,
            boolean isInSufficientBalance) {
        if (SubsPaymentMode.NORMAL.name().equalsIgnoreCase(subsPaymentMode) && !isInSufficientBalance) {
            return String.valueOf(1);
        } else if (isInSufficientBalance && walletAmount != null && StringUtils.isNotBlank(txnAmountStr)) {
            try {
                return String.valueOf(Double.parseDouble(txnAmountStr) - walletAmount);
            } catch (Exception ex) {
                LOGGER.info("Invalid Transaction Amount or Wallet Amount");
                return StringUtils.EMPTY;
            }
        }
        return StringUtils.EMPTY;
    }

    public static String getWalletRenewMessage() {
        return ConfigurationUtil.getMessageProperty(SUBSDETAIL_WALLET_RENEW_MESSAGE);
    }

    public static String getNonWalletRenewMessage() {
        return ConfigurationUtil.getMessageProperty(SUBSDETAIL_NON_WALLET_RENEW_MESSAGE);
    }

    public static String getInfoMessage() {
        return ConfigurationUtil.getMessageProperty(SUBSDETAIL_INFO_MESSAGE);
    }

    public static String getAmountInfoMessage() {
        return ConfigurationUtil.getMessageProperty(SUBSDETAIL_AMOUNT_MESSAGE);
    }

    public static boolean isZeroRupeesSubscription(String txnAmountString) {
        try {
            Double txnAmount = Double.parseDouble(txnAmountString);
            return txnAmount.equals(0d);
        } catch (Exception ex) {
            LOGGER.info("Invalid Txn Amount");
        }
        return false;
    }

    public static List<String> getInfoMessage(String txnAmount) {
        List<String> infoMessageList = new ArrayList<>();
        if (StringUtils.isNotBlank(txnAmount)) {
            String amountMessage = NativeSubscriptionUtils.getAmountInfoMessage();
            amountMessage = amountMessage.replace(TheiaConstant.RequestParams.TXN_AMOUNT, txnAmount);
            infoMessageList.add(amountMessage);
        }
        String infoMessage = NativeSubscriptionUtils.getInfoMessage();
        infoMessageList.add(infoMessage);
        return infoMessageList;
    }
}
