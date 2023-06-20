/**
 *
 */
package com.paytm.pgplus.theia.utils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.enums.SubsTypes;
import com.paytm.pgplus.facade.integration.enums.PPBLAccountType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.paytm.pgplus.cashier.constant.CashierWorkflow;
import com.paytm.pgplus.cashier.enums.PaymentType;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.FrequencyUnit;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.promo.service.client.model.PromoCodeResponse;
import com.paytm.pgplus.theia.constants.ResponseCodeConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtendedInfoPay;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes;
import com.paytm.pgplus.theia.merchant.models.PaymentInfo;
import com.paytm.pgplus.theia.models.TheiaPaymentRequest;
import com.paytm.pgplus.theia.sessiondata.LoginInfo;
import com.paytm.pgplus.theia.sessiondata.MerchantInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionConfig;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;

/**
 * @author amit.dubey
 */
public class ExtendedInfoUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(ExtendedInfoUtil.class);

    /**
     * @param paymentRequest
     * @param merchantInfo
     * @param txnInfo
     * @param paymentInfo
     * @param cashierWorkflow
     * @param txnConfig
     * @return
     */
    public static Map<String, String> selectExtendedInfo(final TheiaPaymentRequest paymentRequest,
            final MerchantInfo merchantInfo, final TransactionInfo txnInfo, final PaymentInfo paymentInfo,
            final CashierWorkflow cashierWorkflow, final TransactionConfig txnConfig, final LoginInfo loginInfo,
            final PromoCodeResponse applyPromoCodeResponse, ExtendedInfoRequestBean extendedInfoRequestBean) {

        Map<String, String> extendedInfo;

        if (txnInfo.getRequestType().equals(RequestTypes.SUBSCRIPTION)) {
            extendedInfo = fillSubscriptionExtendedInfo(paymentRequest, merchantInfo, txnInfo, paymentInfo, txnConfig,
                    loginInfo, extendedInfoRequestBean);
        } else {
            extendedInfo = fillExtendedInfo(paymentRequest, merchantInfo, txnInfo, paymentInfo, txnConfig,
                    applyPromoCodeResponse, loginInfo, extendedInfoRequestBean);
        }

        return extendedInfo;
    }

    /**
     * @param paymentRequest
     * @param merchantInfo
     * @param txnInfo
     * @param paymentInfo
     * @param txnConfig
     * @param loginInfo
     * @return
     */
    private static Map<String, String> fillExtendedInfo(final TheiaPaymentRequest paymentRequest,
            final MerchantInfo merchantInfo, final TransactionInfo txnInfo, final PaymentInfo paymentInfo,
            final TransactionConfig txnConfig, final PromoCodeResponse applyPromoCodeResponse, LoginInfo loginInfo,
            ExtendedInfoRequestBean extendedInfoRequestBean) {
        final String savedCardId = paymentRequest.getSavedCardId();
        final String paytmMerchantId = merchantInfo.getMid();
        final String ssoToken = txnInfo.getSsoToken();
        final String mccCode = merchantInfo.getMerchantCategoryCode();
        final String merchantTransId = txnInfo.getOrderId();
        final String txnType = getTxnType(paymentInfo);
        final String alipayMerchantId = merchantInfo.getInternalMid();
        final String productCode = txnConfig.getProductCode();
        final String totalTxnAmount = txnInfo.getTxnAmount();
        final String merchantName = merchantInfo.getMerchantName();
        final String virtualPaymentAddr = paymentRequest.getVpa();
        final String clientId = extendedInfoRequestBean.getClientId();

        final String isMerchantLimitEnabledForPay = Boolean.valueOf(extendedInfoRequestBean.isMerchantLimitEnabled())
                .toString();
        final String isMerchantLimitUpdatedForPay = Boolean.valueOf(extendedInfoRequestBean.isMerchantLimitUpdated())
                .toString();
        // TODO to fill
        String promoCode = null;
        boolean isPromoCodeValid = false;
        final String promoResponseCode = applyPromoCodeResponse == null ? null : applyPromoCodeResponse
                .getPromoResponseCode();

        if (null != applyPromoCodeResponse && null != applyPromoCodeResponse.getPromoCodeDetail()) {
            promoCode = applyPromoCodeResponse.getPromoCodeDetail().getPromoCode();

            LOGGER.info("PROMO_CODE_RESPONSE : {}", promoResponseCode);

            if (ResponseCodeConstant.PROMO_SUCCESS.equals(promoResponseCode)
                    || (ResponseCodeConstant.PROMO_APPLIED.equals(promoResponseCode) && (ExtendedInfoPay.TXN_TYPE_ONLY_WALLET
                            .equals(txnType) || ExtendedInfoPay.TXN_TYPE_ADDNPAY.equals(txnType)))) {
                isPromoCodeValid = true;
            }
        }

        final String promoApplyResultStatus = applyPromoCodeResponse == null ? null : applyPromoCodeResponse
                .getResultStatus();

        final Map<String, String> extendedInfo = new HashMap<String, String>();
        LOGGER.info("default isMerchantLimitEnabled = {} , isMerchantLimitUpdated= {} ", isMerchantLimitEnabledForPay,
                isMerchantLimitUpdatedForPay);
        extendedInfo.put(ExtendedInfoPay.IS_MERCHANT_LIMIT_ENABLED_FOR_PAY, isMerchantLimitEnabledForPay);
        extendedInfo.put(ExtendedInfoPay.IS_MERCHANT_LIMIT_UPDATED_FOR_PAY, isMerchantLimitUpdatedForPay);

        if ((loginInfo != null) && (loginInfo.getUser() != null)) {
            final String userEmail = loginInfo.getUser().getEmailId();
            final String userMobile = loginInfo.getUser().getMobileNumber();
            extendedInfo.put(TheiaConstant.ExtendedInfoSubscription.USER_EMAIL, userEmail);
            extendedInfo.put(TheiaConstant.ExtendedInfoSubscription.USER_MOBILE, userMobile);
        }

        if (isValidString(savedCardId)) {
            extendedInfo.put(ExtendedInfoPay.SAVED_CARD_ID, savedCardId);
        }

        if (isValidString(paytmMerchantId)) {
            extendedInfo.put(ExtendedInfoPay.PAYTM_MERCHANT_ID, paytmMerchantId);
        }

        if (isValidString(mccCode)) {
            extendedInfo.put(ExtendedInfoPay.MCC_CODE, mccCode);
        }

        if (isValidString(merchantTransId)) {
            extendedInfo.put(ExtendedInfoPay.MERCHANT_TRANS_ID, merchantTransId);
        }

        if (isValidString(txnType)) {
            extendedInfo.put(ExtendedInfoPay.TXN_TYPE, txnType);
        }
        if (PaymentType.HYBRID.getValue().equals(txnType)) {
            extendedInfo.put("otherPayMethod", getOtherPayMethod(paymentRequest));
        }

        if (isValidString(promoResponseCode)) {
            extendedInfo.put(ExtendedInfoPay.PROMO_RESPONSE_CODE, promoResponseCode);
        }

        if (isValidString(promoApplyResultStatus)) {
            extendedInfo.put(ExtendedInfoPay.PROMO_CODE_APPLY_RESULT_STATUS, promoApplyResultStatus);
        }

        if (isValidString(alipayMerchantId)) {
            extendedInfo.put(ExtendedInfoPay.ALIPAY_MERCHANT_ID, alipayMerchantId);
        }

        if (isValidString(productCode)) {
            extendedInfo.put(ExtendedInfoPay.PRODUCT_CODE, productCode);
        }

        if (isValidString(totalTxnAmount)) {
            extendedInfo.put(ExtendedInfoPay.TOTAL_TXN_AMOUNT, totalTxnAmount);
        }

        if (isValidString(merchantName)) {
            extendedInfo.put(ExtendedInfoPay.MERCHANT_NAME, merchantName);
        }

        if (isValidString(virtualPaymentAddr)) {
            extendedInfo.put(ExtendedInfoPay.VIRTUAL_PAYMENT_ADDRESS, virtualPaymentAddr);
        }

        if (isValidString(promoCode)) {
            if (!isPromoCodeValid) {
                extendedInfo.put(ExtendedInfoPay.INAVLID_PROMOCODE, promoCode);
            } else {
                extendedInfo.put(ExtendedInfoPay.PROMO_CODE, promoCode);
            }

        }
        /*
         * Passing merchant ClientId in extendedInfo of pay which is required
         * for checksum calculation
         */
        if (isValidString(clientId)) {
            extendedInfo.put(ExtendedInfoPay.CLIENT_ID, clientId);
        }

        return extendedInfo;
    }

    private static String getOtherPayMethod(final TheiaPaymentRequest paymentRequest) {
        switch (paymentRequest.getTxnMode()) {
        case "PPI":
            return EPayMethod.BALANCE.toString();
        case "DC":
            return EPayMethod.DEBIT_CARD.toString();
        case "CC":
            return EPayMethod.CREDIT_CARD.toString();
        case "EMI":
            return EPayMethod.EMI.toString();
        case "ATM":
            return EPayMethod.ATM.toString();
        case "NB":
            return EPayMethod.NET_BANKING.toString();
        case "IMPS":
            return EPayMethod.IMPS.toString();
        case "COD":
            return EPayMethod.COD.toString();
        case "UPI":
            return EPayMethod.UPI.toString();
        default:
            break;
        }
        return "";
    }

    private static String getTxnType(final PaymentInfo paymentInfo) {

        if (paymentInfo == null || paymentInfo.getPaymentType() == null) {
            return null;
        }

        return PaymentType.HYBRID_COD.equals(paymentInfo.getPaymentType()) ? PaymentType.HYBRID.getValue()
                : paymentInfo.getPaymentType().getValue();
    }

    /**
     * @param paymentRequest
     * @param merchantInfo
     * @param txnInfo
     * @param paymentInfo
     * @param txnConfig
     * @return
     */
    private static Map<String, String> fillSubscriptionExtendedInfo(final TheiaPaymentRequest paymentRequest,
            final MerchantInfo merchantInfo, final TransactionInfo txnInfo, final PaymentInfo paymentInfo,
            final TransactionConfig txnConfig, final LoginInfo loginInfo,
            ExtendedInfoRequestBean extendedInfoRequestBean) {

        final Map<String, String> extendedInfo = fillExtendedInfo(paymentRequest, merchantInfo, txnInfo, paymentInfo,
                txnConfig, null, loginInfo, extendedInfoRequestBean);

        Assert.notNull(loginInfo.getUser(), "User Needs to be Logged in for a Subscription Transaction");

        final String payerUserID = loginInfo.getUser().getPayerUserID();
        final String payerAccountNumber = loginInfo.getUser().getPayerAccountNumber();

        extendedInfo.put(TheiaConstant.ExtendedInfoSubscription.SUBS_ID, txnInfo.getSubscriptionServiceID());
        extendedInfo.put(TheiaConstant.ExtendedInfoSubscription.CURRENT_SUBS_ID, txnInfo.getSubscriptionServiceID());
        extendedInfo.put(TheiaConstant.ExtendedInfoSubscription.SUBS_EXPIRY, txnInfo.getSubscriptionExpiryDate());
        extendedInfo.put(TheiaConstant.ExtendedInfoSubscription.SUBS_PAYER_USER_ID, payerUserID);
        extendedInfo.put(TheiaConstant.ExtendedInfoSubscription.SUBS_ACCOUNT_NUMBER, payerAccountNumber);
        extendedInfo.put(TheiaConstant.ExtendedInfoSubscription.SUBS_REQUEST_TYPE, txnInfo.getRequestType());
        extendedInfo.put(TheiaConstant.ExtendedInfoSubscription.SUBS_CUST_ID, txnInfo.getCustID());
        extendedInfo.put(TheiaConstant.ExtendedInfoSubscription.SUBS_ORDER_ID, txnInfo.getOrderId());
        extendedInfo.put(TheiaConstant.ExtendedInfoSubscription.SUBS_PPI_ONLY, txnInfo.getSubscriptionPPIOnly());
        extendedInfo.put(TheiaConstant.ExtendedInfoSubscription.SUBS_PAY_MODE, txnInfo.getSubscriptionPaymentMode());
        extendedInfo.put(TheiaConstant.ExtendedInfoSubscription.SUBS_FREQUENCY, txnInfo.getSubscriptionFrequency());
        extendedInfo.put(TheiaConstant.ExtendedInfoSubscription.SUBS_FREQUENCY_UNIT, FrequencyUnit
                .getFrequencyUnitbyName(txnInfo.getSubscriptionFrequencyUnit()).getValue().toString());
        BigDecimal subsMinAmount = new BigDecimal(ConfigurationUtil.getProperty(ExtraConstants.SUBSCRIPTION_MIN_AMOUNT,
                "1"));
        BigDecimal txnAmount = new BigDecimal(txnInfo.getTxnAmount());
        String amountToBeRefunded = AmountUtils.getTransactionAmountInPaise(subsMinAmount.subtract(txnAmount)
                .toPlainString());
        if (Double.parseDouble(amountToBeRefunded) > 0) {
            extendedInfo.put(TheiaConstant.ExtendedInfoSubscription.AMOUNT_TO_REFUNDED, amountToBeRefunded);
        }

        if (TheiaConstant.SubscriptionAmountType.FIX.name().equals(txnInfo.getSubscriptionAmountType())
                || TheiaConstant.SubscriptionAmountType.FIXED.name().equals(txnInfo.getSubscriptionAmountType())) {
            extendedInfo.put(TheiaConstant.ExtendedInfoSubscription.SUBS_CAPPED_AMOUNT, txnInfo.getTxnAmount());
        } else {
            extendedInfo.put(TheiaConstant.ExtendedInfoSubscription.SUBS_CAPPED_AMOUNT,
                    txnInfo.getSubscriptionMaxAmount());
        }
        extendedInfo.put(TheiaConstant.ExtendedInfoSubscription.SUBS_TYPE, txnInfo.getRequestType());
        if (txnConfig.getSubsTypes().equals(SubsTypes.PPBL_ONLY)) {
            extendedInfo.put(TheiaConstant.ExtendedInfoSubscription.ACCOUNT_TYPE,
                    PPBLAccountType.SAVING_ACCOUNT.getType());
        }
        return extendedInfo;
    }

    private static boolean isValidString(final String input) {
        if (StringUtils.isBlank(input)) {
            return false;
        }
        return true;
    }
}