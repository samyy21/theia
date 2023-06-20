/**
 * 
 */
package com.paytm.pgplus.theia.datamapper.helper;

import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.checksum.exception.SecurityException;
import com.paytm.pgplus.checksum.utils.CryptoUtils;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.TreeMap;

import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.*;

/**
 * @author naman
 *
 */
@Service("bizRequestResponseMapperHelper")
public class BizRequestResponseMapperHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BizRequestResponseMapperHelper.class);

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    private FF4JUtil ff4JUtil;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    /**
     * @param requestData
     */
    public void decryptRequest(PaymentRequestBean requestData) {
        String encryptedString = requestData.getEncParams();
        Map<String, String> decryptedParams = buildRequestMapFromEncParam(encryptedString, requestData.getMid(),
                requestData.getClientId());

        mapDecryptedParams(requestData, decryptedParams);
    }

    private Map<String, String> buildRequestMapFromEncParam(String stringToDecrypt, String miD, String clientId) {

        String encMerchantkey = merchantExtendInfoUtils.getMerchantKey(miD, clientId);
        String rawData = null;
        try {
            String decMerchantkey = CryptoUtils.decrypt(encMerchantkey);
            rawData = CryptoUtils.decrypt(stringToDecrypt, decMerchantkey);
            if (StringUtils.isNotBlank(rawData)) {
                // LOGGER.info("Request Paramters in ENC_DATA request : {}",
                // rawData);
                Map<String, String> params = new TreeMap<String, String>();
                String[] keyValuePair;
                String key;
                String value;
                String[] tokens = rawData.split("\\|");
                for (String token : tokens) {
                    if (StringUtils.isNotBlank(token)) {
                        keyValuePair = token.split("=");
                        if (keyValuePair.length > 1) {
                            key = keyValuePair[0].trim();
                            value = keyValuePair[1].trim();
                            params.put(key, value);
                        } else if (keyValuePair.length > 0) {
                            key = keyValuePair[0].trim();
                            value = "";
                            params.put(key, value);
                        }
                    }
                }
                return params;
            }
        } catch (SecurityException e) {
            LOGGER.error("Error occured while decrypting merhchant request::{}", e);
        }

        return null;
    }

    private void mapDecryptedParams(PaymentRequestBean paymentRequestBean, Map<String, String> decryptedParams) {

        if (decryptedParams != null && decryptedParams.size() > 0) {
            for (Map.Entry<String, String> entry : decryptedParams.entrySet()) {

                switch (entry.getKey()) {
                case REQUEST_TYPE:
                    if (StringUtils.isBlank(entry.getValue())) {
                        paymentRequestBean.setRequestType("DEFAULT");
                    } else {
                        paymentRequestBean.setRequestType(entry.getValue());
                    }
                    break;
                case ORDER_ID:
                    paymentRequestBean.setOrderId(entry.getValue());
                    break;
                case CUST_ID:
                    paymentRequestBean.setCustId(entry.getValue());
                    break;
                case TXN_AMOUNT:
                    paymentRequestBean.setTxnAmount(entry.getValue());
                    break;
                case TIP_AMOUNT:
                    paymentRequestBean.setTipAmount(entry.getValue());
                    break;
                case CHANNEL_ID:
                    paymentRequestBean.setChannelId(entry.getValue());
                    break;
                case INDUSTRY_TYPE_ID:
                    paymentRequestBean.setIndustryTypeId(entry.getValue());
                    break;
                case WEBSITE:
                    paymentRequestBean.setWebsite(entry.getValue());
                    break;
                case MOBILE_NO:
                    paymentRequestBean.setMobileNo(entry.getValue());
                    break;
                case EMAIL:
                    paymentRequestBean.setEmail(entry.getValue());
                    break;
                case AUTH_MODE:
                    paymentRequestBean.setAuthMode(entry.getValue());
                    break;
                case PAYMENT_TYPE_ID:
                    paymentRequestBean.setPaymentTypeId(entry.getValue());
                    break;
                case CARD_TYPE:
                    paymentRequestBean.setCardType(entry.getValue());
                    break;
                case BANK_CODE:
                    paymentRequestBean.setBankCode(entry.getValue());
                    break;
                case PROMO_CAMP_ID:
                    paymentRequestBean.setPromoCampId(entry.getValue());
                    break;
                case ORDER_DETAILS:
                    paymentRequestBean.setOrderDetails(entry.getValue());
                    break;
                case COMMENTS:
                    paymentRequestBean.setComments(entry.getValue());
                    break;
                case DOB:
                    paymentRequestBean.setDob(entry.getValue());
                    break;
                case PAN_CARD:
                    paymentRequestBean.setPan_Card(entry.getValue());
                    break;
                case DL_NUMBER:
                    paymentRequestBean.setDlNumber(entry.getValue());
                    break;
                case MSISDN:
                    paymentRequestBean.setMsisdn(entry.getValue());
                    break;
                case ADDRESS_1:
                    paymentRequestBean.setAddress1(entry.getValue());
                    break;
                case ADDRESS_2:
                    paymentRequestBean.setAddress2(entry.getValue());
                    break;
                case CITY:
                    paymentRequestBean.setCity(entry.getValue());
                    break;
                case STATE:
                    paymentRequestBean.setState(entry.getValue());
                    break;
                case PINCODE:
                    paymentRequestBean.setPincode(entry.getValue());
                    break;
                case LOGIN_THEME:
                    paymentRequestBean.setLoginTheme(entry.getValue());
                    break;
                case CALLBACK_URL:
                    paymentRequestBean.setCallbackUrl(entry.getValue());
                    break;
                case THEME:
                    paymentRequestBean.setTheme(entry.getValue());
                    break;
                case CANCEL_POINT:
                    paymentRequestBean.setCancelPoint(entry.getValue());
                    break;
                case PAYMENT_DETAILS:
                    paymentRequestBean.setPaymentDetails(entry.getValue());
                    break;
                case SSO_TOKEN:
                    paymentRequestBean.setSsoToken(entry.getValue());
                    break;
                case STORE_CARD:
                    paymentRequestBean.setStoreCard(entry.getValue());
                    break;
                case TXN_MODE:
                    paymentRequestBean.setPaymentMode(entry.getValue());
                    break;
                case PAYMENT_MODE_ONLY:
                    paymentRequestBean.setPaymentModeOnly(entry.getValue());
                    break;
                case PAYMENT_MODE_DISABLED:
                    paymentRequestBean.setDisabledPaymentMode(entry.getValue());
                    break;
                case AUTH_CODE:
                    paymentRequestBean.setAuthCode(entry.getValue());
                    break;
                case SAVED_CARD_ID:
                    paymentRequestBean.setSavedCardID(entry.getValue());
                    break;
                case SUBS_PAYMENT_MODE:
                    paymentRequestBean.setSubsPaymentMode(entry.getValue());
                    break;
                case SUBS_PPI_ONLY:
                    paymentRequestBean.setSubsPPIOnly(entry.getValue());
                    break;
                case SUBSCRIPTION_AMOUNT_TYPE:
                    paymentRequestBean.setSubscriptionAmountType(entry.getValue());
                    break;
                case SUBSCRIPTION_SERVICE_ID:
                    paymentRequestBean.setSubscriptionServiceID(entry.getValue());
                    break;
                case SUBSCRIPTION_ID:
                    paymentRequestBean.setSubscriptionID(entry.getValue());
                    break;
                case SUBS_FREQUENCY:
                    paymentRequestBean.setSubscriptionFrequency(entry.getValue());
                    break;
                case SUBS_FREQUENCY_UNIT:
                    paymentRequestBean.setSubscriptionFrequencyUnit(entry.getValue());
                    break;
                case SUBS_EXPIRY_DATE:
                    paymentRequestBean.setSubscriptionExpiryDate(entry.getValue());
                    break;
                case SUBS_ENABLE_RETRY:
                    paymentRequestBean.setSubscriptionEnableRetry(entry.getValue());
                    break;
                case SUBS_GRACE_DAYS:
                    paymentRequestBean.setSubscriptionGraceDays(entry.getValue());
                    break;
                case SUBS_START_DATE:
                    paymentRequestBean.setSubscriptionStartDate(entry.getValue());
                    break;
                case MERCH_UNQ_REF:
                    paymentRequestBean.setMerchUniqueReference(entry.getValue());
                    break;
                case UDF_1:
                    paymentRequestBean.setUdf1(entry.getValue());
                    break;
                case UDF_2:
                    paymentRequestBean.setUdf2(entry.getValue());
                    break;
                case UDF_3:
                    paymentRequestBean.setUdf3(entry.getValue());
                    break;
                case ADDITIONAL_INFO:
                    paymentRequestBean.setAdditionalInfo(entry.getValue());
                    break;
                case CARD_NO:
                    paymentRequestBean.setCardNo(entry.getValue());
                    break;
                case CVV:
                    paymentRequestBean.setCvv(entry.getValue());
                    break;
                case EXPIRY_DATE:
                    paymentRequestBean.setExpiryDate(entry.getValue());
                    break;
                case OTP:
                    paymentRequestBean.setOtp(entry.getValue());
                    break;
                case VIRTUAL_PAYMENT_ADDRESS:
                    paymentRequestBean.setVirtualPaymentAddr(entry.getValue());
                    break;
                case WALLET_AMOUNT:
                    paymentRequestBean.setWalletAmount(entry.getValue());
                    break;
                case TOTAL_AMOUNT:
                    paymentRequestBean.setTotalAmount(entry.getValue());
                    break;
                case PEON_URL:
                    paymentRequestBean.setPeonURL(entry.getValue());
                    break;
                case INVOICE_PAYMENT_TYPE:
                    paymentRequestBean.setInvoicePaymentType(entry.getValue());
                    break;
                case ORDER_TIMEOUT_MILLI_SECOND:
                    paymentRequestBean.setOrderTimeOutMilliSecond(entry.getValue());
                    break;
                case OAUTH_STATE:
                    paymentRequestBean.setoAuthState(entry.getValue());
                    break;
                case SUBS_MAX_AMOUNT:
                    paymentRequestBean.setSubscriptionMaxAmount(entry.getValue());
                    break;
                case CONNECTION_TYPE:
                    paymentRequestBean.setConnectiontype(entry.getValue());
                    break;
                default:
                    break;
                }
            }

        }

    }

    public boolean validateAndCheckCCOnUpi(String mid) {
        if (!merchantPreferenceService.isFullPg2TrafficEnabled(mid)
                && !merchantPreferenceService.isPG2Enabled(mid, false)) {
            return false;
        }
        if (ff4JUtil.isFeatureEnabled(UPI_ON_CC_BLACKLIST, mid)) {
            return false;
        }
        if (merchantPreferenceService.isP2PMMerchantEnabled((mid))) {
            return false;
        }
        if (merchantPreferenceService.convertTxnToAddNPayEnabled(mid, false)) {
            return false;
        }
        if (StringUtils.equals(mid, ConfigurationUtil.getTheiaProperty(BizConstant.MP_ADD_MONEY_MID))) {
            return false;
        }
        if (merchantPreferenceService.isCCOnUPIRailsEnabled(mid)) {
            return true;
        }
        if (merchantExtendInfoUtils.isMerchantOnPaytm(mid)) {
            if (ff4JUtil.isFeatureEnabled(UPI_ON_CC_ONUS, mid)) {
                return true;
            } else {
                return false;
            }
        }
        if (ff4JUtil.isFeatureEnabled(UPI_ON_CC_GLOBAL, mid)) {
            return true;
        }
        return false;
    }
}