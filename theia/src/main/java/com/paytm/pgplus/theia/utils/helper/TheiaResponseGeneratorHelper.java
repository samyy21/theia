/**
 * 
 */
package com.paytm.pgplus.theia.utils.helper;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.checksum.ValidateChecksum;
import com.paytm.pgplus.checksum.exception.SecurityException;
import com.paytm.pgplus.checksum.utils.AESMerchantService;
import com.paytm.pgplus.checksum.utils.CryptoUtils;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants;
import com.paytm.pgplus.theia.services.helper.EncryptedParamsRequestServiceHelper;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.TreeMap;

/**
 * @author naman
 *
 */
@Component
public class TheiaResponseGeneratorHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheiaResponseGeneratorHelper.class);
    @Autowired
    EncryptedParamsRequestServiceHelper encParamRequestService;

    @Autowired
    Environment env;

    @Autowired
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    private AESMerchantService aesMerchantService;

    @Autowired
    private Ff4jUtils ff4jUtils;

    private final String INPUT_FORM_TEMPLATE = "<input type='hidden' name='{name}' value='{value}'>\n";
    private final String NAME_TEMPLATE = "{name}";
    private final String VALUE_TEMPLATE = "{value}";

    public StringBuilder encryptedResponse(TransactionResponse response, boolean isAES256Encrypted,
            boolean isNativeDirectBankPageFlow) {
        StringBuilder strBuilder = new StringBuilder();
        StringBuilder finalResponse = new StringBuilder();
        TreeMap<String, String> paramMap = new TreeMap<>();
        if (StringUtils.isNotBlank(response.getOrderId())) {
            strBuilder.append(ResponseConstants.ORDER_ID).append("=").append(response.getOrderId()).append("|");
            paramMap.put(ResponseConstants.ORDER_ID, response.getOrderId());
        }

        if (StringUtils.isNotBlank(response.getMid())) {
            strBuilder.append(ResponseConstants.M_ID).append("=").append(response.getMid()).append("|");
            paramMap.put(ResponseConstants.M_ID, response.getMid());
            if (!isNativeDirectBankPageFlow) {
                finalResponse.append(replace(ResponseConstants.M_ID, response.getMid()));
            }
        }

        if (StringUtils.isNotBlank(response.getTxnId())) {
            strBuilder.append(ResponseConstants.TRANSACTION_ID).append("=").append(response.getTxnId()).append("|");
            paramMap.put(ResponseConstants.TRANSACTION_ID, response.getTxnId());
        }

        if (StringUtils.isNotBlank(response.getTxnAmount())) {
            strBuilder.append(ResponseConstants.AMOUNT).append("=").append(response.getTxnAmount()).append("|");
            paramMap.put(ResponseConstants.AMOUNT, response.getTxnAmount());
        }

        if (StringUtils.isNotBlank(response.getPaymentMode())) {
            strBuilder.append(ResponseConstants.PAYMENT_MODE).append("=").append(response.getPaymentMode()).append("|");
            paramMap.put(ResponseConstants.PAYMENT_MODE, response.getPaymentMode());
        }

        if (StringUtils.isNotBlank(response.getCurrency())) {
            strBuilder.append(ResponseConstants.CURRENCY).append("=").append(response.getCurrency()).append("|");
            paramMap.put(ResponseConstants.CURRENCY, response.getCurrency());
        }

        if (StringUtils.isNotBlank(response.getTxnDate())) {
            strBuilder.append(ResponseConstants.TRANSACTION_DATE).append("=").append(response.getTxnDate()).append("|");
            paramMap.put(ResponseConstants.TRANSACTION_DATE, response.getTxnDate());
        }

        if (StringUtils.isNotBlank(response.getCustId())) {
            strBuilder.append(ResponseConstants.CUSTOMER_ID).append("=").append(response.getCustId()).append("|");
            paramMap.put(ResponseConstants.CUSTOMER_ID, response.getCustId());
        }

        if (StringUtils.isNotBlank(response.getTransactionStatus())) {
            strBuilder.append(ResponseConstants.STATUS).append("=").append(response.getTransactionStatus()).append("|");
            paramMap.put(ResponseConstants.STATUS, response.getTransactionStatus());
        }

        if (StringUtils.isNotBlank(response.getResponseCode())) {
            strBuilder.append(ResponseConstants.RESPONSE_CODE).append("=").append(response.getResponseCode())
                    .append("|");
            paramMap.put(ResponseConstants.RESPONSE_CODE, response.getResponseCode());
        }

        if (StringUtils.isNotBlank(response.getMerchUniqueReference())) {
            strBuilder.append(ResponseConstants.MERCH_UNQ_REF).append("=").append(response.getMerchUniqueReference())
                    .append("|");
            paramMap.put(ResponseConstants.MERCH_UNQ_REF, response.getMerchUniqueReference());
        }

        if (StringUtils.isNotBlank(response.getUdf1())) {
            strBuilder.append(ResponseConstants.UDF_1).append("=").append(response.getUdf1()).append("|");
            paramMap.put(ResponseConstants.UDF_1, response.getUdf1());
        }

        if (StringUtils.isNotBlank(response.getUdf2())) {
            strBuilder.append(ResponseConstants.UDF_2).append("=").append(response.getUdf2()).append("|");
            paramMap.put(ResponseConstants.UDF_2, response.getUdf2());
        }

        if (StringUtils.isNotBlank(response.getUdf3())) {
            strBuilder.append(ResponseConstants.UDF_3).append("=").append(response.getUdf3()).append("|");
            paramMap.put(ResponseConstants.UDF_3, response.getUdf3());
        }

        if (StringUtils.isNotBlank(response.getAdditionalInfo())) {
            strBuilder.append(ResponseConstants.ADDITIONAL_INFO).append("=").append(response.getAdditionalInfo())
                    .append("|");
            paramMap.put(ResponseConstants.ADDITIONAL_INFO, response.getAdditionalInfo());
        }

        if (StringUtils.isNotBlank(response.getGateway())) {
            strBuilder.append(ResponseConstants.GATEWAY).append("=").append(response.getGateway()).append("|");
            paramMap.put(ResponseConstants.GATEWAY, response.getGateway());
        }

        if (StringUtils.isNotBlank(response.getBankTxnId())) {
            strBuilder.append(ResponseConstants.BANKTXNID).append("=").append(response.getBankTxnId()).append("|");
            paramMap.put(ResponseConstants.BANKTXNID, response.getBankTxnId());
        }

        if (StringUtils.isNotBlank(response.getBinNumber())) {
            strBuilder.append(ResponseConstants.BIN).append("=").append(response.getBinNumber()).append("|");
            paramMap.put(ResponseConstants.BIN, response.getBinNumber());
        }

        if (StringUtils.isNotBlank(response.getLastFourDigits())) {
            strBuilder.append(ResponseConstants.LASTFOURDIGITS).append("=").append(response.getLastFourDigits())
                    .append("|");
            paramMap.put(ResponseConstants.LASTFOURDIGITS, response.getLastFourDigits());
        }

        if (StringUtils.isNotBlank(response.getPrepaidCard())
                && merchantPreferenceService.isReturnPrepaidEnabled(response.getMid())) {
            strBuilder.append(ResponseConstants.PREPAID_CARD).append("=").append(response.getPrepaidCard()).append("|");
            paramMap.put(ResponseConstants.PREPAID_CARD, response.getPrepaidCard());
        }

        if (StringUtils.isNotBlank(response.getResponseMsg())) {
            strBuilder.append(ResponseConstants.RESPONSE_MSG).append("=").append(response.getResponseMsg()).append("|");
            paramMap.put(ResponseConstants.RESPONSE_MSG, response.getResponseMsg());
        }

        if (response.getChildTxnList() != null && response.getChildTxnList().size() > 0) {
            try {
                String childTxnListString = JsonMapper.mapObjectToJson(response.getChildTxnList());
                strBuilder.append(ResponseConstants.CHILDTXNLIST).append("=").append(childTxnListString).append("|");
                paramMap.put(ResponseConstants.CHILDTXNLIST, childTxnListString);
            } catch (FacadeCheckedException e) {
                LOGGER.error("Error occured while fetching childTxnList:::{}", e);
            }

        }

        if (StringUtils.isNotBlank(response.getPromoCode())) {
            strBuilder.append(ResponseConstants.PROMO_CAMP_ID).append("=").append(response.getPromoCode()).append("|");
            paramMap.put(ResponseConstants.PROMO_CAMP_ID, response.getPromoCode());
        }
        if (StringUtils.isNotBlank(response.getPromoResponseCode())) {
            strBuilder.append(ResponseConstants.PROMO_RESPCODE).append("=").append(response.getPromoResponseCode())
                    .append("|");
            paramMap.put(ResponseConstants.PROMO_RESPCODE, response.getPromoResponseCode());
        }
        if (StringUtils.isNotBlank(response.getPromoApplyResultStatus())) {
            strBuilder.append(ResponseConstants.PROMO_STATUS).append("=").append(response.getPromoApplyResultStatus())
                    .append("|");
            paramMap.put(ResponseConstants.PROMO_STATUS, response.getPromoApplyResultStatus());
        }

        if (StringUtils.isNotBlank(response.getCardScheme())
                && merchantPreferenceService.isSendCardSchemeEncryptedParamEnabled(response.getMid())) {
            strBuilder.append(ResponseConstants.CARD_SCHEME).append("=").append(response.getCardScheme()).append("|");
            paramMap.put(ResponseConstants.CARD_SCHEME, response.getCardScheme());
        }

        // TODO - pcf charges to be added based on pref (needs to be created)
        if (StringUtils.isNotBlank(response.getChargeAmount())
                && merchantPreferenceService.isSendPCFDetailsEncryptedParamEnabled(response.getMid())) {
            strBuilder.append(ResponseConstants.PCF_DETAILS).append("=").append(response.getChargeAmount()).append("|");
            paramMap.put(ResponseConstants.PCF_DETAILS, response.getChargeAmount());
        }

        if (response.isQrIdFlowOnly() && StringUtils.isNotBlank(response.getChargeAmount())
                && ff4jUtils.isFeatureEnabled(TheiaConstant.FF4J.THEIA_CHARGE_AMOUNT_IN_PTC_RESPONSE, false)) {
            strBuilder.append(ResponseConstants.CHARGE_AMOUNT).append("=").append(response.getChargeAmount())
                    .append("|");
            paramMap.put(ResponseConstants.CHARGE_AMOUNT, response.getChargeAmount());
        }

        if (merchantPreferenceService.isSendCountryCode(response.getMid())) {
            strBuilder.append(ResponseConstants.COUNTRY_CODE).append("=").append(ResponseConstants.COUNTRY_CODE_INDIA)
                    .append("|");
            paramMap.put(ResponseConstants.COUNTRY_CODE, ResponseConstants.COUNTRY_CODE_INDIA);
        }

        if (!isAES256Encrypted) {
            String encryptedData = encryptResponse(response.getMid(), strBuilder, response.getClientId(),
                    isAES256Encrypted);
            if (isNativeDirectBankPageFlow) {
                finalResponse.append(encryptedData);
            } else
                finalResponse.append(replace(ResponseConstants.ENC_PARAMS, encryptedData));
        }

        if (merchantPreferenceService.isChecksumEnabled(response.getMid())
                && !(ResponseConstants.ResponseCodes.CHECKSUM_FAILURE_CODE.equals(response.getResponseCode()))) {
            try {
                String checksum;
                String merchantKey;
                if (isAES256Encrypted) {
                    merchantKey = aesMerchantService.fetchAesChecksumKey(response.getMid());
                    checksum = ValidateChecksum.getInstance().getSpecialRespCheckSumValue(merchantKey, paramMap)
                            .get("CHECKSUMHASH");
                } else {
                    merchantKey = merchantExtendInfoUtils.getMerchantKey(response.getMid(), response.getClientId());
                    checksum = ValidateChecksum.getInstance().getRespCheckSumValue(merchantKey, paramMap)
                            .get("CHECKSUMHASH");
                }
                if (StringUtils.isNotBlank(checksum)) {
                    if (!isAES256Encrypted) {
                        if (isNativeDirectBankPageFlow) {
                            finalResponse.append("|").append(checksum);
                        } else
                            finalResponse.append(replace(ResponseConstants.CHECKSUM, checksum));
                    }
                    strBuilder.append(ResponseConstants.CHECKSUM).append("=").append(checksum).append("|");
                }
            } catch (Exception e) {
                LOGGER.error("Exception Occurred while calculating response checksum", e);
            }
        }

        if (isAES256Encrypted) {
            String encryptedData = encryptResponse(response.getMid(), strBuilder, response.getClientId(),
                    isAES256Encrypted);
            if (isNativeDirectBankPageFlow) {
                finalResponse.append(encryptedData);
            } else {
                finalResponse.append(replace(ResponseConstants.ENC_PARAMS, encryptedData));
            }
        }

        return finalResponse;
    }

    public String encryptResponse(String merchantID, StringBuilder stringBuilder, String clientId,
            boolean isAES256Encrypted) {

        String merchantKey = null;
        if (isAES256Encrypted) {
            merchantKey = aesMerchantService.fetchAesEncDecKey(merchantID);
        } else {
            merchantKey = merchantExtendInfoUtils.getMerchantKey(merchantID, clientId);
        }

        String rawData = null;
        try {
            if (isAES256Encrypted) {
                rawData = CryptoUtils.encrypt(stringBuilder.toString(), merchantKey);
            } else {
                String decMerchantkey = CryptoUtils.decrypt(merchantKey);
                rawData = CryptoUtils.encrypt(stringBuilder.toString(), decMerchantkey);
            }
        } catch (SecurityException e) {
            LOGGER.error("Exception Occurred while encrypting response  ::{}", e);
        }

        return rawData;
    }

    private String replace(String name, String value) {
        StringBuilder str = new StringBuilder(INPUT_FORM_TEMPLATE);
        if ((name != null) && (value != null)) {
            int index = str.indexOf(NAME_TEMPLATE);
            if (index >= 0) {
                str.replace(index, index + NAME_TEMPLATE.length(), name);
            }
            index = str.indexOf(VALUE_TEMPLATE);
            if (index >= 0) {
                str.replace(index, index + VALUE_TEMPLATE.length(), value);
            }
            return str.toString();
        }
        return "";
    }
}