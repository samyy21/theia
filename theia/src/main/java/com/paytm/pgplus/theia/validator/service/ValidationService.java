/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.theia.validator.service;

import com.paytm.pgplus.biz.enums.AuthModeEnum;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.checksum.ValidateChecksum;
import com.paytm.pgplus.checksum.crypto.EncryptionFactory;
import com.paytm.pgplus.checksum.crypto.IEncryption;
import com.paytm.pgplus.checksum.crypto.impl.AddMoneyExpressDecryption;
import com.paytm.pgplus.checksum.exception.SecurityException;
import com.paytm.pgplus.checksum.model.CheckSumInput;
import com.paytm.pgplus.checksum.utils.CryptoUtils;
import com.paytm.pgplus.checksum.utils.EncryptConstants;
import com.paytm.pgplus.common.enums.Channel;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod;
import com.paytm.pgplus.theia.utils.EmiBinValidationUtil;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import com.paytm.pgplus.theia.utils.MerchantPreferenceProvider;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.TRUE;

/**
 * @author manojpal
 *
 */

@Service
public class ValidationService {

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    @Qualifier("emiBinValidationUtil")
    private EmiBinValidationUtil emiBinValidationUtil;

    @Autowired
    @Qualifier("merchantPreferenceProvider")
    private MerchantPreferenceProvider merchantPreferenceProvider;

    private static final AddMoneyExpressDecryption ADD_MONEY_EXPRESS_DECRYPTION = AddMoneyExpressDecryption
            .getInstance();

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationService.class);

    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(ValidationService.class);

    public boolean checksumValidatation(final PaymentRequestBean requestData) throws PaymentRequestValidationException {

        final CheckSumInput checkSumInput = new CheckSumInput();
        checkSumInput.setMerchantKey(requestData.getMerchantKey());
        checkSumInput.setMerchantChecksumHash(requestData.getChecksumhash());
        try {
            return ValidateChecksum.getInstance().verifyCheckSum(checkSumInput);
        } catch (final Exception e) {
            LOGGER.error("Exception occured while validating checksumhash", e);
            throw new PaymentRequestValidationException("Exception occured while validating checksumhash", e);
        }
    }

    /*
     * Validating Payment_Details ; Mandatory only in case of
     * Seamless/Seamless_Native
     * 
     * For Card payment :If Transaction is coming after entering card details
     * then Seamless/Seamless_Native flow request Format :
     * cardNumber|cvvNumber|expirydate . Also "expirydate" must be in
     * format(MMYYYY). If MM is 7 then it must come like 07.
     * 
     * If Transaction is coming from saved Card then Seamless_Native request
     * Format : savedCardId|cvvNumber
     */
    public void validateAndProcessSeamlessPaymentDetails(final String encryptedPaymentDetails, final String merchantId,
            final boolean isSavedCardTxn, WorkFlowRequestBean workFlowRequestBean, PaymentRequestBean requestData)
            throws PaymentRequestValidationException {

        try {

            if (checkIfWalletRelatedPayMode(workFlowRequestBean)) {
                return;
            }

            if (StringUtils.equalsIgnoreCase(PaymentTypeIdEnum.COD.getValue(), workFlowRequestBean.getPaymentTypeId())
                    || StringUtils.equalsIgnoreCase(EPayMethod.MP_COD.getMethod(),
                            workFlowRequestBean.getPaymentTypeId())) {
                return;
            }

            if (StringUtils.equalsIgnoreCase(PaymentTypeIdEnum.ADVANCE_DEPOSIT_ACCOUNT.getValue(),
                    workFlowRequestBean.getPaymentTypeId())
                    || StringUtils.equalsIgnoreCase(EPayMethod.ADVANCE_DEPOSIT_ACCOUNT.getMethod(),
                            workFlowRequestBean.getPaymentTypeId())) {
                return;
            }

            String paymentDetails = encryptedPaymentDetails;

            if (ERequestType.SEAMLESS.equals(workFlowRequestBean.getRequestType())
                    || ((ERequestType.isNativeOrUniRequest(workFlowRequestBean.getRequestType()) || ERequestType
                            .isSubscriptionCreationRequest(workFlowRequestBean.getRequestType().getType())) && workFlowRequestBean
                            .isEncryptedCardDetail())) {
                String merchantKey = merchantExtendInfoUtils.getMerchantKey(merchantId, requestData.getClientId());
                String decryptMerchantKey = CryptoUtils.decrypt(merchantKey);
                paymentDetails = CryptoUtils.decrypt(encryptedPaymentDetails, decryptMerchantKey);
            }
            String[] actualPaymentDetailsArray = paymentDetails.split(Pattern.quote("|"), -1);
            int paymentDetailsArrayLength = actualPaymentDetailsArray.length;
            if (StringUtils.isBlank(paymentDetails)) {
                LOGGER.error("Empty paymentDetails");
            }
            if (workFlowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.CC.value)
                    || workFlowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.DC.value)
                    || workFlowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.EMI.value)) {
                // Checking if card is MAESTRO & CVV,expiry is not provided
                if (paymentDetailsArrayLength == 1) {
                    if (StringUtils.isBlank(actualPaymentDetailsArray[0])) {
                        throw new PaymentRequestValidationException("Invalid Seamless Payment details",
                                ResponseConstants.INVALID_PAYMENT_DETAILS);
                    } else if (actualPaymentDetailsArray[0].length() == 19) {
                        workFlowRequestBean.setCardNo(actualPaymentDetailsArray[0]);
                        workFlowRequestBean.setCvv2("123");
                        workFlowRequestBean.setExpiryMonth(Short.valueOf("12"));
                        workFlowRequestBean.setExpiryYear(Short.valueOf("2049"));
                    }
                } else if (paymentDetailsArrayLength == 2) {
                    if (StringUtils.isBlank(actualPaymentDetailsArray[0])
                            || (StringUtils.isBlank(actualPaymentDetailsArray[1]) && !isIDebitEnabled(workFlowRequestBean))) {
                        throw new PaymentRequestValidationException("Invalid Seamless Payment details",
                                ResponseConstants.INVALID_PAYMENT_DETAILS);
                    } else {
                        if (!workFlowRequestBean.isCoftTokenTxn()) {
                            workFlowRequestBean.setIsSavedCard(true);
                            workFlowRequestBean.setSavedCardID(actualPaymentDetailsArray[0]); // Setting
                            // SavedCardId
                            workFlowRequestBean.setCvv2(actualPaymentDetailsArray[1]); // Setting
                        }
                    }
                } else if (paymentDetailsArrayLength == 3) {
                    if (StringUtils.isBlank(actualPaymentDetailsArray[0])
                            || (StringUtils.isBlank(actualPaymentDetailsArray[1]) && !isIDebitEnabled(workFlowRequestBean))
                            || StringUtils.isBlank(actualPaymentDetailsArray[2])) {
                        throw new PaymentRequestValidationException("Invalid Seamless Payment details",
                                ResponseConstants.INVALID_PAYMENT_DETAILS);
                    }
                    String cardNo = actualPaymentDetailsArray[0];
                    workFlowRequestBean.setCardNo(cardNo);
                    workFlowRequestBean.setCvv2(actualPaymentDetailsArray[1]);

                    // Verifying expiry date length & format(MMYYYY)
                    final String expiryDate = actualPaymentDetailsArray[2];
                    populateExpiryDate(workFlowRequestBean, expiryDate);
                    if (TRUE.equals(requestData.getFromAOARequest()))
                        workFlowRequestBean.setLastFourDigits(cardNo.substring(cardNo.length() - 4));
                }

            } else if (workFlowRequestBean.getPaymentTypeId().equals(PaymentTypeIdEnum.IMPS.value)) {

                /*
                 * IMPS: For IMPS payment mode following is required a. Mobile
                 * Number - 10 digit b. MMID - 7 digit c. OTP - 6 digit or 8
                 * digit depending on the bank. In case of IMPS details of user
                 * is saved, following is required a. saved card id - This will
                 * include Mobile number and MMID b. OTP - 6 digit
                 */

                if (paymentDetailsArrayLength == 2) {
                    if (StringUtils.isBlank(actualPaymentDetailsArray[0])
                            || StringUtils.isBlank(actualPaymentDetailsArray[1])) {
                        throw new PaymentRequestValidationException("Invalid Seamless Payment details",
                                ResponseConstants.INVALID_PAYMENT_DETAILS);
                    }
                    workFlowRequestBean.setIsSavedCard(true);
                    workFlowRequestBean.setSavedCardID(actualPaymentDetailsArray[0]);
                    workFlowRequestBean.setOtp(actualPaymentDetailsArray[1]);
                } else if (paymentDetailsArrayLength == 3) {
                    if (StringUtils.isBlank(actualPaymentDetailsArray[0])
                            || StringUtils.isBlank(actualPaymentDetailsArray[1])
                            || StringUtils.isBlank(actualPaymentDetailsArray[2])) {
                        throw new PaymentRequestValidationException("Invalid Seamless Payment details",
                                ResponseConstants.INVALID_PAYMENT_DETAILS);
                    }
                    workFlowRequestBean.setMobileNo(actualPaymentDetailsArray[0]);
                    workFlowRequestBean.setMmid(actualPaymentDetailsArray[1]);
                    workFlowRequestBean.setOtp(actualPaymentDetailsArray[2]);
                }
            }

        } catch (final Exception e) {
            EXT_LOGGER.error("Excetion occured while validating payment details in seamless", e);
            EXT_LOGGER.customError("Excetion occured while validating payment details in seamless : {}",
                    ExceptionUtils.getFullStackTrace(e));

            throw new PaymentRequestValidationException(e.getMessage(), ResponseConstants.INVALID_PAYMENT_DETAILS);
        }
    }

    private boolean checkIfWalletRelatedPayMode(WorkFlowRequestBean workFlowRequestBean) {
        return PaymentTypeIdEnum.PPI.value.equals(workFlowRequestBean.getPaymentTypeId())
                || PaymentTypeIdEnum.WALLET.value.equals(workFlowRequestBean.getPaymentTypeId());
    }

    private boolean isIDebitEnabled(WorkFlowRequestBean workFlowRequestBean) {
        return "true".equalsIgnoreCase(workFlowRequestBean.getiDebitEnabled())
                && PaymentTypeIdEnum.DC.value.equals(workFlowRequestBean.getPaymentTypeId());
    }

    public void validateAndProcessAddMoneyExpressPaymentDetails(final WorkFlowRequestBean flowRequestBean,
            PaymentRequestBean paymentRequest) {

        try {
            String encryptedPaymentDetails = flowRequestBean.getPaymentDetails();
            String paymentDetails = encryptedPaymentDetails;
            if (ERequestType.ADDMONEY_EXPRESS.equals(flowRequestBean.getRequestType())) {
                paymentDetails = ADD_MONEY_EXPRESS_DECRYPTION.returnDecryptedValue(paymentDetails);
                paymentRequest.setPaymentDetails(paymentDetails);
            }
            String[] paymentDetailsArray = paymentDetails.split(Pattern.quote("|"));
            int paymentDetailsArrayLength = paymentDetailsArray.length;

            if (flowRequestBean.getIsSavedCard()
                    && TheiaConstant.ExtraConstants.PAYTM_EXPRESS_1.equals(flowRequestBean.getStoreCard())) {
                LOGGER.error("Store card and save card can not be true at same time.");
                throw new PaymentRequestValidationException("Invalid Add Money Express payment details",
                        ResponseConstants.INVALID_PAYMENT_DETAILS);
            }

            if (PaymentTypeIdEnum.CC.value.equals(flowRequestBean.getPaymentTypeId())
                    || PaymentTypeIdEnum.DC.value.equals(flowRequestBean.getPaymentTypeId())) {
                if (paymentDetailsArrayLength < 2 || StringUtils.isBlank(paymentDetailsArray[0])
                        || StringUtils.isBlank(paymentDetailsArray[1])) {
                    throw new PaymentRequestValidationException("Invalid Add Money Express payment details",
                            ResponseConstants.INVALID_PAYMENT_DETAILS);
                }
                if (paymentDetailsArrayLength == 2 && flowRequestBean.getIsSavedCard()) {
                    String savedCardId = paymentDetailsArray[0];
                    if (StringUtils.isNotBlank(savedCardId)) {
                        if (savedCardId.length() > 15 && savedCardId.length() < 45) {
                            paymentRequest.setCoftTokenTxn(true);
                            flowRequestBean.setIsSavedCard(false);
                        }
                        flowRequestBean.setSavedCardID(savedCardId.trim());
                        flowRequestBean.setCvv2(ExtraConstants.XXX.equalsIgnoreCase(paymentDetailsArray[1]) ? "123"
                                : paymentDetailsArray[1]);
                    }
                } else if (paymentDetailsArrayLength == 3) {
                    if (StringUtils.isBlank(paymentDetailsArray[2])) {
                        throw new PaymentRequestValidationException("Invalid Add Money Express payment details",
                                ResponseConstants.INVALID_PAYMENT_DETAILS);
                    }
                    flowRequestBean.setCardNo(paymentDetailsArray[0]);
                    flowRequestBean.setCvv2(ExtraConstants.XXX.equalsIgnoreCase(paymentDetailsArray[1]) ? "123"
                            : paymentDetailsArray[1]);
                    final String expiryDate = paymentDetailsArray[2];
                    if (expiryDate.length() != 6) {
                        throw new PaymentRequestValidationException("Invalid Add Money Express payment details",
                                ResponseConstants.INVALID_PAYMENT_DETAILS);
                    } else {
                        flowRequestBean
                                .setExpiryMonth(ExtraConstants.MM.equalsIgnoreCase(expiryDate.substring(0, 2)) ? Short
                                        .valueOf("12") : Short.valueOf(expiryDate.substring(0, 2)));
                        flowRequestBean
                                .setExpiryYear(ExtraConstants.YYYY.equalsIgnoreCase(expiryDate.substring(2, 6)) ? Short
                                        .valueOf("2049") : Short.valueOf(expiryDate.substring(2, 6)));
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Excetion occured while validating payment details in add money express", ex);
            throw new PaymentRequestValidationException(ex.getMessage(), ResponseConstants.INVALID_PAYMENT_DETAILS);
        }
    }

    public void populateExpiryDate(WorkFlowRequestBean workFlowRequestBean, final String expiryDate)
            throws ParseException {
        final SimpleDateFormat sdf = new SimpleDateFormat("MMyyyy");
        if (!(expiryDate.length() == 6)) {
            throw new PaymentRequestValidationException("Invalid Seamless Payment details",
                    ResponseConstants.INVALID_PAYMENT_DETAILS);
        } else if (!(expiryDate.equals(sdf.format(sdf.parse(expiryDate))))) {
            throw new PaymentRequestValidationException("Invalid Seamless Payment details",
                    ResponseConstants.INVALID_PAYMENT_DETAILS);
        } else {
            workFlowRequestBean.setExpiryMonth(Short.valueOf(expiryDate.substring(0, 2)));
            workFlowRequestBean.setExpiryYear(Short.valueOf(expiryDate.substring(2, 6)));
        }
    }

    public boolean validatePaytmExpressData(PaymentRequestBean requestData) {

        if (!checkForPaytmExpressMandatoryParams(requestData)) {
            return false;
        }

        if (!checkForPaytmExpressConditionalParams(requestData)) {
            return false;
        }

        if (TheiaConstant.ExtraConstants.PAYTM_EXPRESS_1.equals(requestData.isAddMoney())) {

            return isAddMoneyAllowed(requestData);

        } else if (TheiaConstant.ExtraConstants.PAYTM_EXPRESS_0.equals(requestData.isAddMoney())) {

            return isHybridAllowed(requestData.getMid());

        } else if (StringUtils.isNotBlank(requestData.isAddMoney())) {

            return false;

        }

        return true;
    }

    private boolean checkForPaytmExpressConditionalParams(PaymentRequestBean requestData) {

        if (PaymentTypeIdEnum.CC.getValue().equals(requestData.getPaymentTypeId())
                || PaymentTypeIdEnum.DC.getValue().equals(requestData.getPaymentTypeId())
                || PaymentTypeIdEnum.IMPS.getValue().equals(requestData.getPaymentTypeId())
                || PaymentTypeIdEnum.UPI.getValue().equals(requestData.getPaymentTypeId())) {
            if (!validatePaymentDetailsForPaytmExpress(requestData.getPaymentDetails())) {
                return false;
            }
        }

        if (PaymentTypeIdEnum.NB.getValue().equals(requestData.getPaymentTypeId())) {
            if (!validateBankCode(requestData.getBankCode())) {
                return false;
            }
        }

        if (StringUtils.isNotBlank(requestData.isAddMoney())
                || PaymentTypeIdEnum.PPI.getValue().equals(requestData.getPaymentTypeId())) {
            if (!validateSSOToken(requestData.getSsoToken())) {
                return false;
            }

            if (StringUtils.isNotBlank(requestData.isAddMoney()) && StringUtils.isBlank(requestData.getPaytmToken())) {
                LOGGER.error("Paytm token is required in AddAndPay or Hybrid Txns");
                return false;
            }
        }

        return true;
    }

    private boolean checkForPaytmExpressMandatoryParams(PaymentRequestBean requestData) {

        if (!validateMid(requestData.getMid()) || !validateOrderId(requestData.getOrderId())
                || !validateCustId(requestData.getCustId()) || !validateTxnAmount(requestData.getTxnAmount())
                || !validateChannelId(requestData) || !validateIndustryType(requestData.getIndustryTypeId())
                || !validateWebsite(requestData.getWebsite()) || !validateAuthMode(requestData.getAuthMode())
                || !validatePaymentTypeID(requestData)) {
            return false;
        }

        return true;
    }

    public boolean validateMid(String mid) {

        if (StringUtils.isBlank(mid) || mid.length() > TheiaConstant.ExtraConstants.MID_MAX_LENGTH
                || !Pattern.matches(TheiaConstant.ExtraConstants.ALPHA_NUMERIC_PATTERN, mid)) {
            LOGGER.error("Invalid MID ::{}", mid);
            return false;
        }

        return true;
    }

    public boolean validateOrderId(String orderID) {

        if (StringUtils.isBlank(orderID) || orderID.length() > TheiaConstant.ExtraConstants.ORDER_ID_MAX_LENGTH
                || !Pattern.matches(TheiaConstant.ExtraConstants.ALPHA_NUMERIC_PATTERN, orderID)) {
            LOGGER.error("Invalid orderID ::{}", orderID);
            return false;
        }

        return true;
    }

    public boolean validateToken(String token) {
        if (StringUtils.isBlank(token) || !Pattern.matches(ExtraConstants.TOKEN_PATTERN, token)) {
            LOGGER.error("Invalid token ::{}", token);
            return false;
        }
        return true;
    }

    public boolean validateJson(String json) {
        if (StringUtils.isBlank(json) || !Pattern.matches(ExtraConstants.JSON_PATTERN, json)) {
            LOGGER.error("Invalid json ::{}", json);
            return false;
        }
        return true;
    }

    public boolean validateKycMid(String kycMid) {
        if (StringUtils.isBlank(kycMid) || !Pattern.matches(TheiaConstant.ExtraConstants.ALPHA_NUMERIC_PATTERN, kycMid)) {
            LOGGER.error("Invalid KYC MID ::{}", kycMid);
            return false;
        }
        return true;
    }

    public boolean validateTransactionId(String transactionId) {
        if (StringUtils.isBlank(transactionId)
                || !Pattern.matches(ExtraConstants.TRANSACTION_ID_PATTERN, transactionId)) {
            LOGGER.error("Invalid Transaction ID ::{}", transactionId);
            return false;
        }
        return true;
    }

    public boolean validateRespMsg(String respMsg) {
        if (StringUtils.isBlank(respMsg) || !Pattern.matches(ExtraConstants.RESPMSG_PATTERN, respMsg)) {
            LOGGER.error("Invalid RESP MSG ::{}", respMsg);
            return false;
        }
        return true;
    }

    public boolean validateHeaderWorkflow(String headerWorkflow) {
        if (StringUtils.isBlank(headerWorkflow)
                || !Pattern.matches(ExtraConstants.HEADER_WORKFLOW_PATTERN, headerWorkflow)) {
            LOGGER.error("Invalid HEADER_WORKFLOW ::{}", headerWorkflow);
            return false;
        }
        return true;
    }

    public boolean validateTxnToken(String txnToken) {
        if (StringUtils.isBlank(txnToken)
                || !Pattern.matches(TheiaConstant.ExtraConstants.ALPHA_NUMERIC_PATTERN, txnToken)) {
            LOGGER.error("Invalid Txn Token ::{}", txnToken);
            return false;
        }
        return true;
    }

    private boolean validateCustId(String custID) {

        if (StringUtils.isBlank(custID) || custID.length() > TheiaConstant.ExtraConstants.CUST_ID_MAX_LENGTH) {
            LOGGER.error("Invalid custID ::{}", custID);
            return false;
        }

        return true;
    }

    private boolean validateTxnAmount(String txnAmount) {

        if (StringUtils.isBlank(txnAmount) || txnAmount.length() > TheiaConstant.ExtraConstants.TXN_AMOUNT_MAX_LENGTH
                || !NumberUtils.isNumber(txnAmount)) {
            LOGGER.error("Invalid txnAmount ::{}", txnAmount);
            return false;
        }

        return true;
    }

    private boolean validateChannelId(PaymentRequestBean requestData) {

        Channel channel = Channel.getChannelbyName(requestData.getChannelId());
        if (channel == null) {
            LOGGER.error("Invalid channelID ::{}", requestData.getChannelId());
            return false;
        }

        return true;
    }

    private boolean validateIndustryType(String industryType) {

        if (industryType.length() > TheiaConstant.ExtraConstants.INDUSTRY_TYPE_MAX_LENGTH
                || !Pattern.matches(TheiaConstant.ExtraConstants.ALPHA_NUMERIC_PATTERN, industryType)) {
            LOGGER.error("Invalid industryType ::{}", industryType);
            return false;
        }

        return true;
    }

    private boolean validateWebsite(String website) {

        if (StringUtils.isBlank(website) || website.length() > TheiaConstant.ExtraConstants.WEBSITE_MAX_LENGTH
                || !Pattern.matches(TheiaConstant.ExtraConstants.ALPHA_NUMERIC_PATTERN, website)) {
            LOGGER.error("Invalid website ::{}", website);
            return false;
        }

        return true;
    }

    private boolean validatePaymentDetailsForPaytmExpress(String paymentDetails) {

        if (StringUtils.isBlank(paymentDetails)) {
            LOGGER.error("Invalid paymentDetails ::{}", paymentDetails);
            return false;
        }

        return true;
    }

    private boolean validateTheme(String theme) {

        if (StringUtils.isBlank(theme)) {
            LOGGER.error("Invalid theme ::{}", theme);
            return false;
        }

        return true;
    }

    private boolean validateBankCode(String bankCode) {

        if (StringUtils.isBlank(bankCode)) {
            LOGGER.error("Invalid BankCode ::{}", bankCode);
            return false;
        }

        return true;
    }

    private boolean validateSSOToken(String ssoToken) {

        if (StringUtils.isBlank(ssoToken)) {
            LOGGER.error("Invalid SSO Token ::{}", ssoToken);
            return false;
        }

        return true;
    }

    private boolean validateAuthMode(String authMode) {

        AuthModeEnum authModeEnum = AuthModeEnum.getEnumByValue(authMode);
        if (authModeEnum == null) {
            LOGGER.error("Invalid authMode ::{}", authMode);
            return false;
        }

        return true;
    }

    private boolean validatePaymentTypeID(PaymentRequestBean requestData) {

        PaymentTypeIdEnum paymentTypeIdEnum = PaymentTypeIdEnum.getEnumByValue(requestData.getPaymentTypeId());
        if (paymentTypeIdEnum == null) {
            LOGGER.error("Invalid paymentTypeId ::{}", requestData.getPaymentTypeId());
            return false;
        } else if (TheiaConstant.RequestTypes.PAYTM_EXPRESS.equals(requestData.getRequestType())
                && (paymentTypeIdEnum.equals(PaymentTypeIdEnum.COD) || paymentTypeIdEnum
                        .equals(PaymentTypeIdEnum.Telco))) {
            LOGGER.error("Invalid paymentTypeId ::{}", requestData.getPaymentTypeId());
            return false;
        }

        return true;
    }

    private boolean isAddMoneyAllowed(PaymentRequestBean requestData) {
        boolean result = merchantPreferenceProvider.isAddMoneyEnabled(requestData);
        if (!result) {
            LOGGER.error("Add Money not allowed for merchant :: {}", requestData.getMid());
        } else if (StringUtils.isBlank(requestData.getSsoToken())) {
            LOGGER.error("SSO Token is mandatory for Add Money :: {}", requestData.getMid());
            result = false;
        }
        return result;
    }

    private boolean isHybridAllowed(String mid) {
        boolean result = merchantPreferenceService.getMerchantPreferenceStore(mid).getPreferences()
                .get(TheiaConstant.MerchantPreference.PreferenceKeys.HYBRID_ALLOWED).isEnabled();
        if (!result) {
            LOGGER.error("Hybrid Not allowed for merchant ::{}", mid);
        }
        return result;
    }

    private String decryptPrivateKey(String merchantPrivateKey) {
        String decryptedPrivateKey = null;
        try {
            IEncryption encryption = EncryptionFactory.getEncryptionInstance(EncryptConstants.ALGTHM_TYPE_AES);
            decryptedPrivateKey = encryption.decrypt(merchantPrivateKey);
        } catch (SecurityException e) {
            LOGGER.error("Error occured while decrypting the private key : {}", e);
            throw new PaymentRequestValidationException(e.getMessage(), ResponseConstants.INVALID_PAYMENT_DETAILS);
        }
        return decryptedPrivateKey;
    }

    public void validateSeamlessEMIPaymentRequest(PaymentRequestBean paymentRequestBean,
            WorkFlowRequestBean flowRequestBean, EntityPaymentOptionsTO entityPaymentOptions) {
        validateEMIPayment(paymentRequestBean, flowRequestBean);
        if (!StringUtils.isEmpty(flowRequestBean.getCardNo())) {
            BinDetail binDetail = new BinDetail(Long.parseLong(flowRequestBean.getCardNo().substring(0, 6)));
            if ((EPayMode.NONE == flowRequestBean.getPaytmExpressAddOrHybrid() && !emiBinValidationUtil
                    .isValidEmiCardDetailsEntered(entityPaymentOptions.getCompleteEMIInfoList(), binDetail,
                            paymentRequestBean.getEmiPlanID()))
                    || (EPayMode.HYBRID == flowRequestBean.getPaytmExpressAddOrHybrid() && !emiBinValidationUtil
                            .isValidEmiCardDetailsEntered(entityPaymentOptions.getHybridEMIInfoList(), binDetail,
                                    paymentRequestBean.getEmiPlanID()))) {
                throw new TheiaServiceException("Exception occured while builing EMI data for seamless transaction");
            }
        }
    }

    public void validateSeamlessEMIPaymentRequestNative(PaymentRequestBean requestBean,
            WorkFlowRequestBean flowRequestBean, NativeCashierInfoResponse cashierInfoResponse) {
        validateEMIPayment(requestBean, flowRequestBean);
        validateEMIPaymentNative(cashierInfoResponse);

        PayMethod payMethod = cashierInfoResponse.getBody().getMerchantPayOption().getPayMethods().stream()
                .filter(paymethod -> EPayMethod.EMI.getMethod().equals(paymethod.getPayMethod())).findAny()
                .orElse(null);
        if (payMethod == null) {
            throw new TheiaServiceException(
                    "Exception occured while building EMI data for seamless transaction: EMI not configured on merchant");
        }
        if (!StringUtils.isEmpty(flowRequestBean.getCardNo())) {
            BinDetail binDetail = new BinDetail(Long.parseLong(flowRequestBean.getCardNo().substring(0, 6)));
            if (!emiBinValidationUtil.isValidEmiCardDetailsEnteredNative(payMethod.getPayChannelOptions(), binDetail,
                    requestBean.getEmiPlanID(), requestBean.getEmitype())) {
                throw new TheiaServiceException("Exception occured while building EMI data for seamless transaction");
            }

        }

    }

    private void validateEMIPaymentNative(NativeCashierInfoResponse cashierInfoResponse) {
        if (cashierInfoResponse.getBody().getMerchantPayOption() == null) {
            throw new TheiaServiceException(
                    "Exception occured while building EMI data for seamless transaction: No Pay Method on Merchant");
        }
        if (cashierInfoResponse.getBody().getMerchantPayOption().getPayMethods() == null) {
            throw new TheiaServiceException(
                    "Exception occured while building EMI data for seamless transaction: No Pay Method on merchant");
        }
    }

    private void validateEMIPayment(PaymentRequestBean paymentRequestBean, WorkFlowRequestBean flowRequestBean) {
        if (EPayMode.ADDANDPAY == flowRequestBean.getPaytmExpressAddOrHybrid()) {
            throw new TheiaServiceException(
                    "Exception occured while builing EMI data for seamless transaction: AddnPay is not allowed");
        } else if (!PaymentTypeIdEnum.EMI.value.equals(paymentRequestBean.getPaymentTypeId())) {
            throw new TheiaServiceException(
                    "Exception occured while builing EMI data for seamless transaction: Only EMI is allowed");
        } else if (StringUtils.isEmpty(paymentRequestBean.getEmiPlanID())) {
            throw new TheiaServiceException(
                    "Exception occured while builing EMI data for seamless transaction: Invalid PlanId");
        }
    }
}
