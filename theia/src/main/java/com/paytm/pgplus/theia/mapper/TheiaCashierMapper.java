/**
 *
 */
package com.paytm.pgplus.theia.mapper;

import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cache.model.PaytmProperty;
import com.paytm.pgplus.cashier.cachecard.model.*;
import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.cashier.constant.CashierWorkflow;
import com.paytm.pgplus.cashier.enums.PaymentType;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.*;
import com.paytm.pgplus.cashier.models.CashierEnvInfo.CashierEnvInfoBuilder;
import com.paytm.pgplus.cashier.pay.model.PSULimit;
import com.paytm.pgplus.cashier.pay.model.PaymentRequest;
import com.paytm.pgplus.cashier.pay.model.PaymentRequest.PaymentRequestBuilder;
import com.paytm.pgplus.cashier.pay.model.ValidationRequest;
import com.paytm.pgplus.cashier.payoption.PayBillOptions;
import com.paytm.pgplus.cashier.payoption.PayBillOptions.PayBillOptionsBuilder;
import com.paytm.pgplus.common.enums.ETerminalType;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.facade.common.model.EnvInfo.EnvInfoBuilder;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.enums.TerminalType;
import com.paytm.pgplus.facade.enums.TransType;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.user.models.PaytmBanksVpaDefaultDebitCredit;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.utils.RequestIdGenerator;
import com.paytm.pgplus.pgproxycommon.enums.CardScheme;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.SavedCardInfo;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantBankInfoDataService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.merchant.models.BankInfoData;
import com.paytm.pgplus.theia.merchant.models.PaymentInfo;
import com.paytm.pgplus.theia.models.TheiaPaymentRequest;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.session.utils.UpiInfoSessionUtil;
import com.paytm.pgplus.theia.sessiondata.*;
import com.paytm.pgplus.theia.utils.BinUtils;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import com.paytm.pgplus.theia.viewmodel.BankInfo;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static com.paytm.pgplus.biz.utils.BizConstant.PASS_THROUGH_EXTEND_INFO_KEY;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.SAVED_CARD_ENABLE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.SAVED_CARD_ENABLE_ALT;

/**
 * @author amit.dubey
 *
 */
@Service
public class TheiaCashierMapper {

    @Autowired
    @Qualifier("merchantBankInfoDataService")
    private IMerchantBankInfoDataService merchantBankInfoDataService;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    @Qualifier("theiaSessionDataService")
    ITheiaSessionDataService sessionDataService;

    @Autowired
    private UpiInfoSessionUtil upiInfoSessionUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(TheiaCashierMapper.class);

    /**
     * @param txnInfo
     * @param theiaPaymentRequest
     * @param entityPaymentOptionsTO
     * @return
     * @throws PaytmValidationException
     */
    public CashierWorkflow getCashierWorkflow(final TransactionInfo txnInfo, TheiaPaymentRequest theiaPaymentRequest,
            EntityPaymentOptionsTO entityPaymentOptionsTO) throws PaytmValidationException {

        if (theiaPaymentRequest == null) {
            LOGGER.error("TheiaPaymentRequest can't be null");
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE);
        }

        switch (txnInfo.getRequestType()) {
        case RequestTypes.DEFAULT:
        case RequestTypes.DEFAULT_MF:
        case RequestTypes.CC_BILL_PAYMENT:
        case RequestTypes.EMAIL_INVOICE:
        case RequestTypes.SMS_INVOICE:
        case RequestTypes.SUBSCRIPTION:
        case RequestTypes.LINK_BASED_PAYMENT:
        case RequestTypes.LINK_BASED_PAYMENT_INVOICE:
        case RequestTypes.RESELLER:
            return getCashierWorkflowForNonAddMoney(theiaPaymentRequest, entityPaymentOptionsTO);
        case RequestTypes.ADD_MONEY:
            return getCashierWorkflowForAddMoney(theiaPaymentRequest, entityPaymentOptionsTO);
        }

        throw new PaymentRequestValidationException("Cashier workflow not found for this transaction : "
                + txnInfo.getRequestType());
    }

    private CashierWorkflow getCashierWorkflowForNonAddMoney(TheiaPaymentRequest theiaPaymentRequest,
            EntityPaymentOptionsTO entityPaymentOptionsTO) throws PaytmValidationException {
        StringBuilder sb = new StringBuilder();
        CashierWorkflow cashierWorkflow = null;

        if (StringUtils.isBlank(theiaPaymentRequest.getTxnMode())) {
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE);
        }

        switch (theiaPaymentRequest.getTxnMode()) {
        case "PPI":
            cashierWorkflow = CashierWorkflow.WALLET;
            break;
        case "PAYTM_DIGITAL_CREDIT":
            cashierWorkflow = CashierWorkflow.DIGITAL_CREDIT_PAYMENT;
            break;
        case "DC":
            if (entityPaymentOptionsTO.isDcEnabled()
                    || (ExtraConstants.ADD_MONEY_FLAG_VALUE.equals(theiaPaymentRequest.getAddMoneyFlag()) && entityPaymentOptionsTO
                            .isAddDcEnabled())) {
                cashierWorkflow = CashierWorkflow.ISOCARD;
            }
            sb.append("DEBIT_CARD");
            break;
        case "CC":
            if (entityPaymentOptionsTO.isCcEnabled()
                    || (ExtraConstants.ADD_MONEY_FLAG_VALUE.equals(theiaPaymentRequest.getAddMoneyFlag()) && entityPaymentOptionsTO
                            .isAddCcEnabled())) {
                cashierWorkflow = CashierWorkflow.ISOCARD;
            }
            sb.append("CREDIT_CARD");
            break;
        case "EMI":
            cashierWorkflow = CashierWorkflow.ISOCARD;
            break;
        case "ATM":
            if (entityPaymentOptionsTO.isAtmEnabled()
                    || (ExtraConstants.ADD_MONEY_FLAG_VALUE.equals(theiaPaymentRequest.getAddMoneyFlag()) && entityPaymentOptionsTO
                            .isAddAtmEnabled())) {
                cashierWorkflow = CashierWorkflow.ATM;
            }
            sb.append("ATM");
            break;
        case "PPBL":
            checkIfPaymentsBankSupported(theiaPaymentRequest, entityPaymentOptionsTO);
            theiaPaymentRequest.setTxnMode(ExtraConstants.NB);
            theiaPaymentRequest.setPaymentMode(ExtraConstants.NB);
            theiaPaymentRequest.setBankCode(ExtraConstants.PAYMENTS_BANK_CODE);
            cashierWorkflow = CashierWorkflow.NB;
            break;
        case "NB":
            if (entityPaymentOptionsTO.isNetBankingEnabled()
                    || (ExtraConstants.ADD_MONEY_FLAG_VALUE.equals(theiaPaymentRequest.getAddMoneyFlag()) && entityPaymentOptionsTO
                            .isAddNetBankingEnabled())) {
                cashierWorkflow = CashierWorkflow.NB;
            } else if ("PPBL".equals(theiaPaymentRequest.getBankCode())) {
                checkIfPaymentsBankSupported(theiaPaymentRequest, entityPaymentOptionsTO);
                cashierWorkflow = CashierWorkflow.NB;
            }
            sb.append("NET_BANKING");
            break;
        case RequestTypes.ADD_MONEY:
            if (theiaPaymentRequest.getPaymentMode() == null) {
                throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE);

            }
            sb.append("IMPS");
            break;
        case "COD":
            if (entityPaymentOptionsTO.isCodEnabled()) {
                cashierWorkflow = CashierWorkflow.COD;
            }
            sb.append("COD");
            break;
        case "UPI":
            if (entityPaymentOptionsTO.isUpiEnabled()
                    || (ExtraConstants.ADD_MONEY_FLAG_VALUE.equals(theiaPaymentRequest.getAddMoneyFlag()) && entityPaymentOptionsTO
                            .isAddUpiEnabled())) {
                cashierWorkflow = CashierWorkflow.UPI;
            }
            sb.append("UPI");
            break;
        }

        if (cashierWorkflow != null) {
            return cashierWorkflow;
        }
        sb.append(" : payment is not enabled for this transaction");

        throw new PaytmValidationException(sb.toString());
    }

    private CashierWorkflow getCashierWorkflowForAddMoney(TheiaPaymentRequest theiaPaymentRequest,
            EntityPaymentOptionsTO entityPaymentOptionsTO) throws PaytmValidationException {
        StringBuilder sb = new StringBuilder();
        CashierWorkflow cashierWorkflow = null;

        if (StringUtils.isBlank(theiaPaymentRequest.getTxnMode())) {
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE);
        }

        switch (theiaPaymentRequest.getPaymentMode()) {
        case "DC":
            if (entityPaymentOptionsTO.isDcEnabled()) {
                cashierWorkflow = CashierWorkflow.ADD_MONEY_ISOCARD;
            }

            sb.append("DEBIT_CARD");
            break;
        case "CC":
            if (entityPaymentOptionsTO.isCcEnabled()) {
                cashierWorkflow = CashierWorkflow.ADD_MONEY_ISOCARD;
            }

            sb.append("CREDIT_CARD");
            break;
        case "NB":
            if (entityPaymentOptionsTO.isNetBankingEnabled()) {
                cashierWorkflow = CashierWorkflow.ADD_MONEY_NB;
            }

            sb.append("NET BANKING");
            break;
        case "IMPS":
            if (entityPaymentOptionsTO.isNetBankingEnabled()) {
                cashierWorkflow = CashierWorkflow.ADD_MONEY_IMPS;
            }

            sb.append("IMPS");
            break;
        case "ATM":
            if (entityPaymentOptionsTO.isNetBankingEnabled()) {
                cashierWorkflow = CashierWorkflow.ADD_MONEY_ATM;
            }

            sb.append("ATM");
            break;
        case "UPI":
            if (entityPaymentOptionsTO.isUpiEnabled()) {
                return CashierWorkflow.ADD_MONEY_UPI;
            }
            sb.append("UPI");
            break;
        }

        if (cashierWorkflow != null) {
            return cashierWorkflow;
        }
        sb.append(" : payment is not enabled for AddMoney transaction");

        throw new PaytmValidationException(sb.toString());
    }

    private void checkIfPaymentsBankSupported(TheiaPaymentRequest theiaPaymentRequest,
            EntityPaymentOptionsTO entityPaymentOptionsTO) throws PaytmValidationException {

        final SavingsAccountInfo savingsAccountInfo = sessionDataService.getSavingsAccountInfoFromSession(
                theiaPaymentRequest.getRequest(), false);

        if (savingsAccountInfo == null
                || (!ExtraConstants.ADD_MONEY_FLAG_VALUE.equals(theiaPaymentRequest.getAddMoneyFlag()) && !entityPaymentOptionsTO
                        .isPaymentsBankEnabled())
                || (ExtraConstants.ADD_MONEY_FLAG_VALUE.equals(theiaPaymentRequest.getAddMoneyFlag()) && !entityPaymentOptionsTO
                        .isAddPaymentsBankEnabled()) || savingsAccountInfo.isSavingsAccountInactive()) {
            throw new PaytmValidationException("Payments bank is not enabled for this transaction");
        }
    }

    /**
     * @param theiaPaymentRequest
     * @param paymentInfo
     * @param txnInfo
     * @param txnConfig
     * @param userInfo
     * @param extendedInfo
     * @param envInfo
     * @return
     * @throws CashierCheckedException
     */
    public PaymentRequest preparePaymentRequest(final TheiaPaymentRequest theiaPaymentRequest,
            final PaymentInfo paymentInfo, final TransactionInfo txnInfo, final TransactionConfig txnConfig,
            final OAuthUserInfo userInfo, final Map<String, String> extendedInfo, MerchantInfo merchInfo,
            EntityPaymentOptionsTO entityPaymentOptions, EnvInfoRequestBean envInfo,
            final Map<String, String> riskExtendedInfo, final DigitalCreditRequest digitalCreditRequest,
            final UPIPushRequest upiPushRequest, BinDetail binDetail) throws CashierCheckedException,
            PaytmValidationException {

        PaymentType paymentType = paymentInfo.getPaymentType();
        String cardScheme = null;
        if (binDetail != null) {
            cardScheme = binDetail.getCardName();
        }
        String transId = txnInfo.getTxnId();
        TransType transType = TransType.valueOf(txnConfig.getTxnType());
        // Removed this check as part of PGP-750.
        // Was needed earlier to ensure that 2 pay requests are not submitted to
        // A+ for topup.
        // String requestId = TransType.ACQUIRING.equals(transType) ?
        // RequestIdGenerator.generateRequestId() : transId;

        PayBillOptions payBillOptions = preparePayBillOptions(theiaPaymentRequest, paymentInfo, userInfo, txnInfo,
                merchInfo, entityPaymentOptions, extendedInfo, riskExtendedInfo, digitalCreditRequest, upiPushRequest,
                envInfo, cardScheme);

        CashierEnvInfo cashierEnvInfo = prepareCashierEnvInfo(theiaPaymentRequest, paymentInfo, envInfo);

        String securityId = PaymentType.ADDNPAY.equals(paymentType) ? txnInfo.getAddAndPaySecurityId() : txnInfo
                .getSecurityID();

        String payerUserId = null;
        if (null != userInfo) {
            payerUserId = userInfo.getPayerUserID();
            extendedInfo.put(CashierConstant.PAYTM_USER_ID, userInfo.getUserID());
        }
        if (StringUtils.isNotBlank(txnInfo.getCustID())) {
            extendedInfo.put(CashierConstant.CUST_ID, txnInfo.getCustID());
        }

        extendedInfo.put(CashierConstant.STORE_CARD_PREFERENCE, String.valueOf(merchInfo.isMerchantStoreCardPref()));

        putSubWalletAmountInExtendedInfo(txnInfo, extendedInfo);

        if ((TheiaConstant.RequestTypes.ADD_MONEY.equals(txnInfo.getRequestType()) || (RequestTypes.DEFAULT
                .equals(txnInfo.getRequestType()))) && StringUtils.isNotBlank(txnInfo.getAddMoneyDestination())) {
            // Pass destination in extendInfo of pay
            extendedInfo.put(CashierConstant.ADD_MONEY_DESTINATION, txnInfo.getAddMoneyDestination());
        }

        PaymentRequestBuilder builder = new PaymentRequestBuilder(paymentType, transId, transType,
                RequestIdGenerator.generateRequestId(), payBillOptions, cashierEnvInfo).setPayerUserId(payerUserId)
                .setSecurityId(securityId).setExtendInfo(extendedInfo).setRiskExtendInfo(riskExtendedInfo);

        return builder.build();
    }

    private void putSubWalletAmountInExtendedInfo(TransactionInfo txnInfo, Map<String, String> extendedInfo) {

        if (Objects.nonNull(extendedInfo) && Objects.nonNull(txnInfo) && Objects.nonNull(txnInfo.getSubwalletAmount())) {

            try {
                extendedInfo.put(BizConstant.ExtendedInfoKeys.SUBWALLET_AMOUNT_DETAILS,
                        JsonMapper.mapObjectToJson(txnInfo.getSubwalletAmount()));
            } catch (FacadeCheckedException exception) {
                LOGGER.error("Exception Occurred while transforming Subwallet Map to Json :: {}", exception);
            }
        }
    }

    /**
     * @param theiaPaymentRequest
     * @param paymentInfo
     * @param userInfo
     * @param txnInfo
     * @return
     * @throws CashierCheckedException
     */
    private PayBillOptions preparePayBillOptions(final TheiaPaymentRequest theiaPaymentRequest,
            final PaymentInfo paymentInfo, final OAuthUserInfo userInfo, final TransactionInfo txnInfo,
            final MerchantInfo merchInfo, final EntityPaymentOptionsTO entityPaymentOptions,
            final Map<String, String> extendInfo, final Map<String, String> riskExtendInfo,
            final DigitalCreditRequest digitalCreditRequest, final UPIPushRequest upiPushRequest,
            final EnvInfoRequestBean envInfo, String cardScheme) throws CashierCheckedException,
            PaytmValidationException {
        Long serviceAmount = paymentInfo.getServiceAmount();
        boolean topupAndPay = paymentInfo.getTopupAndPay();
        Long chargeFeeAmount = paymentInfo.getChargeFeeAmount();
        String payerAccountNo = userInfo.getPayerAccountNumber();
        boolean isSubscription = false;
        boolean saveChannelInfoAfterPay = SAVED_CARD_ENABLE.equalsIgnoreCase(theiaPaymentRequest.getStoreCardFlag())
                || SAVED_CARD_ENABLE_ALT.equalsIgnoreCase(theiaPaymentRequest.getStoreCardFlag());

        Map<String, String> channelInfo = buildChannelInfo(theiaPaymentRequest, entityPaymentOptions);

        Map<String, String> passThroughChannelExtendedInfo = new HashMap<String, String>();
        passThroughChannelExtendedInfo = setPassThroughChannelExtendedInfo(envInfo, extendInfo, userInfo, merchInfo);
        String channelPassThroughJson = StringUtils.EMPTY;
        try {
            channelPassThroughJson = JsonMapper.mapObjectToJson(passThroughChannelExtendedInfo);
        } catch (Exception e) {
            LOGGER.error("Exception occurred while transforming passThroughChannelInfoMap {}", e);
        }
        String encodedChannelPassThrough = new String(Base64.getEncoder().encode(channelPassThroughJson.getBytes()));
        channelInfo.put(BizConstant.ExtendedInfoKeys.CHANNEL_INFO_PASS_THROUGH_KEY, encodedChannelPassThrough);

        extendInfo.put(TheiaConstant.ExtendedInfoPay.PAYTM_MERCHANT_ID, merchInfo.getMid());
        extendInfo.put(TheiaConstant.ExtendedInfoPay.ALIPAY_MERCHANT_ID, merchInfo.getInternalMid());

        // Emi
        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.CARDHOLDER_ADD1, txnInfo.getAddress1());
        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.CARDHOLDER_ADD2, txnInfo.getPincode());
        // passing merchant dispaly name in channelinfo
        if (merchInfo != null && !StringUtils.isBlank(merchInfo.getMerchantName())) {
            channelInfo.put(BizConstant.ExtendedInfoKeys.MERCHANT_DISPLAY_NAME, merchInfo.getMerchantName());
        }
        if (StringUtils.isNotBlank(theiaPaymentRequest.getVpa())) {
            extendInfo.put(TheiaConstant.ExtendedInfoPay.VIRTUAL_PAYMENT_ADDRESS, theiaPaymentRequest.getVpa());
        }

        // Hack to support UPI PUSH EXPRESS flow
        if (TheiaConstant.BasicPayOption.UPI.equals(theiaPaymentRequest.getTxnMode())) {

            if (upiPushRequest.isUpiPushTxn() && upiPushRequest.isUpiPushExpressSupported()) {
                PaytmBanksVpaDefaultDebitCredit defaultDebit = upiPushRequest.getSarvatraVpaDetails().getDefaultDebit();
                Map<String, String> passThroughExtendInfoMap = new HashMap<String, String>();
                passThroughExtendInfoMap.put(FacadeConstants.DEVICE_ID, upiPushRequest.getDeviceId());
                passThroughExtendInfoMap.put(FacadeConstants.MOBILE_NO, upiPushRequest.getMobile());
                passThroughExtendInfoMap.put(FacadeConstants.SEQUENCE_NUMBER, upiPushRequest.getSeqNo());
                passThroughExtendInfoMap.put(FacadeConstants.IFSC, defaultDebit.getIfsc());
                passThroughExtendInfoMap.put(FacadeConstants.ACCOUNT_NUMBER, defaultDebit.getAccount());
                passThroughExtendInfoMap.put(FacadeConstants.MPIN, upiPushRequest.getMpin());
                passThroughExtendInfoMap.put(FacadeConstants.PAYER_VPA, upiPushRequest.getSarvatraVpaDetails()
                        .getName());
                passThroughExtendInfoMap.put(FacadeConstants.ORDER_ID, upiPushRequest.getOrderId());
                if (StringUtils.isNotBlank(upiPushRequest.getAppId())) {
                    passThroughExtendInfoMap.put(FacadeConstants.APP_ID, upiPushRequest.getAppId());
                }
                String passThroughJson = StringUtils.EMPTY;
                try {
                    passThroughJson = JsonMapper.mapObjectToJson(passThroughExtendInfoMap);
                } catch (Exception e) {
                    LOGGER.error("Exception occurred while transforming passThroughExtendInfoMap {}", e);
                }
                String encodedPassThrough = new String(Base64.getEncoder().encode(passThroughJson.getBytes()));
                channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.PASS_THROUGH_EXTEND_INFO_KEY,
                        encodedPassThrough);
            } else if (!upiPushRequest.isUpiPushTxn() && !upiPushRequest.isUpiPushExpressSupported()) {
                Map<String, String> passThroughExtendedInfoMap = new HashMap<>();
                String passThroughJson = StringUtils.EMPTY;
                passThroughExtendedInfoMap.put(BizConstant.ExtendedInfoKeys.PassThroughKeys.PAYMENT_TIMEOUT_UPI_MINS,
                        upiInfoSessionUtil.getPaymentTimeoutinMinsForUpi(merchInfo.getMid()));
                try {
                    passThroughJson = JsonMapper.mapObjectToJson(passThroughExtendedInfoMap);
                } catch (Exception e) {
                    LOGGER.error("Exception occurred while transforming passThroughChannelInfoMap {}", e);
                }
                String encodedPassThrough = new String(Base64.getEncoder().encode(passThroughJson.getBytes()));
                channelInfo.put(PASS_THROUGH_EXTEND_INFO_KEY, encodedPassThrough);
            }
        }

        if ((digitalCreditRequest != null)
                && PayMethod.PAYTM_DIGITAL_CREDIT.getMethod().equals(theiaPaymentRequest.getTxnMode())) {
            if (StringUtils.isBlank(digitalCreditRequest.getExternalAccountNo())
                    || StringUtils.isBlank(digitalCreditRequest.getLenderId())) {
                throw new CashierCheckedException("Paytm CC account details not found");
            }
            channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.PAYTM_CC_ID,
                    digitalCreditRequest.getExternalAccountNo());
            channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.LENDER_ID,
                    digitalCreditRequest.getLenderId());
            /* Add AUTHORIZATION_TOKEN when pass code gets verified from OAuth */
        }

        channelInfo.put(BizConstant.ExtendedInfoKeys.MERCHANT_TYPE,
                isOnus(merchantExtendInfoUtils, merchInfo) ? BizConstant.ExtendedInfoKeys.MerchantTypeValues.ONUS
                        : BizConstant.ExtendedInfoKeys.MerchantTypeValues.OFFUS);

        /** Saving card data in cache for Subscription */
        if (txnInfo.getRequestType().equals(RequestTypes.SUBSCRIPTION)) {
            saveChannelInfoAfterPay = true;
            isSubscription = true;
        }
        if (isSubscription && extendInfo.containsKey(TheiaConstant.ExtendedInfoPay.SAVED_CARD_ID)
                && isSufficientBalance(paymentInfo)) {
            paymentInfo.setPaymentType(PaymentType.ONLY_WALLET);
        }
        /*
         * Modify payotion for UPI_PUSH/EXPRESS txn
         */
        if (PayMethod.UPI.getOldName().equals(theiaPaymentRequest.getPaymentMode())
                && StringUtils.isNotBlank(theiaPaymentRequest.getMpin())
                && StringUtils.isNotBlank(theiaPaymentRequest.getDeviceId())) {
            if (upiPushRequest.isUpiPushExpressSupported()) {
                theiaPaymentRequest.setPaymentMode(TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS);
            } else {
                theiaPaymentRequest.setPaymentMode(TheiaConstant.BasicPayOption.UPI_PUSH);
            }
        }

        Map<PayMethod, String> payOptions = computePayOptions(paymentInfo.getPaymentType(),
                theiaPaymentRequest.getBankCode(), theiaPaymentRequest.getPaymentMode(), cardScheme);

        PayBillOptionsBuilder builder = new PayBillOptionsBuilder(serviceAmount, chargeFeeAmount, payOptions)
                .setTopAndPay(topupAndPay).setPayerAccountNumber(payerAccountNo)
                .setSaveChannelInfoAfterPay(saveChannelInfoAfterPay).setChannelInfo(channelInfo)
                .setExtendInfo(extendInfo).setWalletBalance(paymentInfo.getWalletBalance());

        return builder.build();
    }

    /**
     * *populating the userdetails, mcc and ipaddr in the channelinfo
     * 
     * @param envInfoRequestBean
     * @param extendInfo
     * @param userInfo
     * @return a map containing usermobile, useremail, mcc, and ipaddress
     */

    private Map<String, String> setPassThroughChannelExtendedInfo(EnvInfoRequestBean envInfoRequestBean,
            Map<String, String> extendInfo, OAuthUserInfo userInfo, MerchantInfo merchInfo) {
        Map<String, String> passThroughChannelExtendedInfo = new HashMap<>();
        if (null != extendInfo && extendInfo.get(BizConstant.ExtendedInfoKeys.MCC_CODE) != null) {
            passThroughChannelExtendedInfo.put(BizConstant.ExtendedInfoKeys.PassThroughKeys.MCC,
                    extendInfo.get(BizConstant.ExtendedInfoKeys.MCC_CODE));
        }
        if (null != userInfo && userInfo.getMobileNumber() != null) {
            passThroughChannelExtendedInfo.put(BizConstant.ExtendedInfoKeys.PassThroughKeys.CUSTOMER_PHONE_NO,
                    userInfo.getMobileNumber());
        }
        if (null != userInfo && userInfo.getEmailId() != null) {
            passThroughChannelExtendedInfo.put(BizConstant.ExtendedInfoKeys.PassThroughKeys.CUSTOMER_EMAIL_ID,
                    userInfo.getEmailId());
        }
        if (null != envInfoRequestBean && envInfoRequestBean.getClientIp() != null) {
            passThroughChannelExtendedInfo.put(BizConstant.ExtendedInfoKeys.PassThroughKeys.IP_ADDRESS,
                    envInfoRequestBean.getClientIp());
        }

        String merchantType = MERCHANT_TYPE_OFF_US;
        boolean isMerchantOnPaytm = merchantExtendInfoUtils.isMerchantOnPaytm(merchInfo.getMid());
        if (isMerchantOnPaytm) {
            merchantType = MERCHANT_TYPE_ON_US;
        }

        passThroughChannelExtendedInfo.put(MERCHANT_TYPE, merchantType);
        passThroughChannelExtendedInfo.put(CUSTOMER_ID, userInfo.getUserID());
        passThroughChannelExtendedInfo.put(MERCHANT_DISPLAY_NAME, merchInfo.getMerchantName());

        return passThroughChannelExtendedInfo;
    }

    public boolean isSufficientBalance(PaymentInfo paymentInfo) {
        return paymentInfo.getServiceAmount() <= paymentInfo.getWalletBalance();
    }

    public Map<String, String> buildChannelInfo(final TheiaPaymentRequest theiaPaymentRequest,
            final EntityPaymentOptionsTO entityPaymentOptions) {
        Map<String, String> channelInfo = new HashMap<>();

        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.BROWSER_USER_AGENT,
                theiaPaymentRequest.getBrowserUserAgent());
        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.IS_EMI, isEmi(theiaPaymentRequest));
        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.CARD_HOLDER_NAME,
                TheiaConstant.ExtendedInfoKeys.ChannelInfoDefaultValues.CARD_HOLDER_NAME);
        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.MOBILE_NO,
                TheiaConstant.ExtendedInfoKeys.ChannelInfoDefaultValues.MOBILE_NO);
        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.SHIPPING_ADDR_1,
                TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.SHIPPING_ADDR_1);
        channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.SHIPPING_ADDR_2,
                TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.SHIPPING_ADDR_2);

        if (StringUtils.isNotBlank(theiaPaymentRequest.getVpa())) {
            channelInfo.put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.VIRTUAL_PAYMENT_ADDRESS,
                    theiaPaymentRequest.getVpa());
        }

        if (!channelInfo.get(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.IS_EMI).equalsIgnoreCase("Y")) {
            return channelInfo;
        }

        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_PLANID, theiaPaymentRequest.getEmiPlanID());

        if (!CollectionUtils.isEmpty(entityPaymentOptions.getCompleteEMIInfoList())) {
            for (BankInfo bankInfo : entityPaymentOptions.getCompleteEMIInfoList()) {
                for (EMIInfo eMIInfo : bankInfo.getEmiInfo()) {
                    if (eMIInfo.getInstId().equals(theiaPaymentRequest.getBankCode())
                            && eMIInfo.getPlanId().equals(theiaPaymentRequest.getEmiPlanID())) {

                        if (eMIInfo.isAggregator()) {
                            channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_PLANID, eMIInfo.getAggregatorPlanId());
                        }

                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_TENUREID, eMIInfo.getTenureId());
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_ONUS, eMIInfo.getCardAcquiringMode());
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_MONTHS, eMIInfo.getOfMonths());
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_INTEREST, eMIInfo.getInterestRate());
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_AMOUNT, eMIInfo.getEmiAmount());
                    }
                }
            }
        } else {
            for (BankInfo bankInfo : entityPaymentOptions.getHybridEMIInfoList()) {
                for (EMIInfo eMIInfo : bankInfo.getEmiInfo()) {
                    if (eMIInfo.getInstId().equals(theiaPaymentRequest.getBankCode())
                            && eMIInfo.getPlanId().equals(theiaPaymentRequest.getEmiPlanID())) {
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_TENUREID, eMIInfo.getTenureId());
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_ONUS, eMIInfo.getCardAcquiringMode());
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_MONTHS, eMIInfo.getOfMonths());
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_INTEREST, eMIInfo.getInterestRate());
                        channelInfo.put(TheiaConstant.ChannelInfoKeys.EMI_AMOUNT, eMIInfo.getEmiAmount());
                    }
                }
            }
        }

        // bases on ui flag set whatever needed in channel info
        return channelInfo;
    }

    /**
     * @param input
     * @param paymentInfo
     * @param envInfoRequestBean
     * @return
     * @throws CashierCheckedException
     * @throws FacadeCheckedException
     */
    private CashierEnvInfo prepareCashierEnvInfo(final TheiaPaymentRequest input, final PaymentInfo paymentInfo,
            EnvInfoRequestBean envInfoRequestBean) throws CashierCheckedException {

        TerminalType terminalType = getTerminalFromChannel(envInfoRequestBean.getTerminalType());

        EnvInfoBuilder envInfoBuilder;
        try {
            envInfoBuilder = new EnvInfoBuilder(envInfoRequestBean.getClientIp(), terminalType.getTerminal());
            envInfoBuilder.osType(envInfoRequestBean.getOsType()).tokenId(envInfoRequestBean.getTokenId()).build();
        } catch (FacadeInvalidParameterException e) {
            throw new CashierCheckedException(e);
        }

        final CashierEnvInfoBuilder builder = new CashierEnvInfoBuilder(envInfoRequestBean.getClientIp(), terminalType)
                .tokenId(envInfoRequestBean.getTokenId()).websiteLanguage(envInfoRequestBean.getWebsiteLanguage())
                .osType(envInfoRequestBean.getOsType()).appVersion(envInfoRequestBean.getAppVersion())
                .sdkVersion(envInfoRequestBean.getSdkVersion()).clientKey(envInfoRequestBean.getClientKey())
                .orderTerminalType(envInfoRequestBean.getOrderTerminalType())
                .orderOsType(envInfoRequestBean.getOrderOsType())
                .merchantAppVersion(envInfoRequestBean.getMerchantAppVersion())
                .sessionId(envInfoRequestBean.getSessionId()).extendInfo(envInfoRequestBean.getExtendInfo());

        return builder.build();
    }

    /**
     * @param input
     * @return
     */
    private static String isEmi(final TheiaPaymentRequest input) {
        return "EMI".equals(input.getPaymentMode()) ? "Y" : "N";
    }

    private static boolean isOnus(final MerchantExtendInfoUtils merchantExtendInfoUtils, final MerchantInfo merchInfo) {
        return merchantExtendInfoUtils.isMerchantOnPaytm(merchInfo.getMid());
    }

    private Map<PayMethod, String> computePayOptions(final PaymentType paymentType, String bank,
            final String paymentMode, String cardScheme) throws PaytmValidationException {

        Map<PayMethod, String> payOptions = new HashMap<>();

        if (null == paymentType) {
            LOGGER.error("Getting paymentType as Null in  computePayOptions");
            return payOptions;
        }

        switch (paymentType) {
        case ONLY_PG:
            payOptions = getSecondaryPayOption(bank, paymentMode, payOptions, cardScheme);
            break;
        case ONLY_COD:
            payOptions.put(PayMethod.MP_COD, TheiaConstant.BasicPayOption.MP_COD);
            break;
        case ONLY_WALLET:
            payOptions.put(PayMethod.BALANCE, PayMethod.BALANCE.getMethod());
            break;
        case HYBRID:
        case ADDNPAY:
            payOptions.put(PayMethod.BALANCE, PayMethod.BALANCE.getMethod());
            payOptions = getSecondaryPayOption(bank, paymentMode, payOptions, cardScheme);
            break;
        case HYBRID_COD:
            payOptions.put(PayMethod.BALANCE, PayMethod.BALANCE.getMethod());
            payOptions.put(PayMethod.MP_COD, TheiaConstant.BasicPayOption.MP_COD);
            break;
        case OTHER:
        default:
            break;
        }
        return payOptions;
    }

    private Map<PayMethod, String> getSecondaryPayOption(final String bank, final String paymentMode,
            final Map<PayMethod, String> input, String cardScheme) throws PaytmValidationException {
        switch (paymentMode) {
        case "DC":
            input.put(PayMethod.DEBIT_CARD, TheiaConstant.BasicPayOption.DEBIT_CARD);
            break;
        case "EMI":
            validateBankCode(bank);
            if (StringUtils.isNotBlank(cardScheme) && cardScheme.equals(CardScheme.AMEX.name())) {
                input.put(PayMethod.EMI, TheiaConstant.BasicPayOption.EMI.concat(CardScheme.AMEX.name()));
            } else {
                input.put(PayMethod.EMI, TheiaConstant.BasicPayOption.EMI.concat(bank));
            }
            break;
        case "CC":
            input.put(PayMethod.CREDIT_CARD, TheiaConstant.BasicPayOption.CREDIT_CARD);
            break;
        case "ATM":
            validateBankCode(bank);
            input.put(PayMethod.ATM, TheiaConstant.BasicPayOption.ATM.concat(bank));
            break;
        case "NB":
            validateBankCode(bank);
            input.put(PayMethod.NET_BANKING, TheiaConstant.BasicPayOption.NET_BANKING.concat(bank));
            break;
        case "IMPS":
            input.put(PayMethod.IMPS, TheiaConstant.BasicPayOption.IMPS);
            break;
        case "MP_COD":
            input.put(PayMethod.MP_COD, TheiaConstant.BasicPayOption.MP_COD);
            break;
        case "UPI":
            // on bases of flag set it as upi push
            input.put(PayMethod.UPI, TheiaConstant.BasicPayOption.UPI);
            break;
        case "UPI_PUSH":
            input.put(PayMethod.UPI, TheiaConstant.BasicPayOption.UPI_PUSH);
            break;
        case "UPI_PUSH_EXPRESS":
            input.put(PayMethod.UPI, TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS);
            break;
        case "PAYTM_DIGITAL_CREDIT":
            input.put(PayMethod.PAYTM_DIGITAL_CREDIT, TheiaConstant.BasicPayOption.PAYTM_DIGITAL_CREDIT);
            break;
        }
        return input;
    }

    private void validateBankCode(String bank) throws PaytmValidationException {
        if (StringUtils.isBlank(bank)) {
            throw new PaytmValidationException("Invalid Bank Code");
        }
    }

    /**
     * @param eTerminalType
     * @return
     */
    private TerminalType getTerminalFromChannel(final ETerminalType eTerminalType) {
        switch (eTerminalType) {
        case WAP:
        case APP:
            return TerminalType.WAP;
        case SYSTEM:
            return TerminalType.SYSTEM;
        case WEB:
        default:
            return TerminalType.WEB;
        }
    }

    /**
     * @param input
     * @param txnConfig
     * @param merchInfo
     * @return
     * @throws CashierCheckedException
     */
    public CashierMerchant prepareCashierMerchant(final TheiaPaymentRequest input, final TransactionConfig txnConfig,
            final MerchantInfo merchInfo) throws CashierCheckedException {
        String merchantId = merchInfo.getMid();
        boolean retryConfiguredForPayment = merchInfo.isMerchantRetryEnabled();

        int maxRetryCount = txnConfig.getRetryCount();
        return new CashierMerchant(merchantId, merchInfo.getInternalMid(), retryConfiguredForPayment, maxRetryCount);
    }

    /**
     * @param cardInfo
     * @param theiaPaymentRequest
     * @return
     * @throws CashierCheckedException
     */
    public CardRequest prepareCardRequest(CardInfo cardInfo, TheiaPaymentRequest theiaPaymentRequest)
            throws CashierCheckedException, PaytmValidationException {

        String cvv = theiaPaymentRequest.getCvv();
        CardRequest cardRequest;

        if (StringUtils.isNotBlank(theiaPaymentRequest.getSavedCardId())) {

            Map<String, SavedCardInfo> cardMap = TheiaConstant.ExtraConstants.ADD_MONEY_FLAG_VALUE
                    .equals(theiaPaymentRequest.getAddMoneyFlag()) ? cardInfo.getAddAnPaySavedCardMap() : cardInfo
                    .getSavedCardMap();

            SavedCardRequest savedCardRequest;

            if (!cardMap.isEmpty() && (cardMap.get(theiaPaymentRequest.getSavedCardId()) != null)) {
                SavedCardInfo savedCardInfo = cardMap.get(theiaPaymentRequest.getSavedCardId());
                String cardType = savedCardInfo.getCardType();
                String cardScheme = savedCardInfo.getCardScheme();

                savedCardRequest = new SavedCardRequest(theiaPaymentRequest.getSavedCardId(), cvv, cardType, cardScheme);
            } else {
                savedCardRequest = new SavedCardRequest(theiaPaymentRequest.getSavedCardId(), cvv);
            }

            cardRequest = new CardRequest(savedCardRequest);
        } else {

            String cardNumber = theiaPaymentRequest.getCardNo();
            if (StringUtils.isBlank(cardNumber) || cardNumber.length() < ExtraConstants.BIN_LENGTH
                    || cardNumber.length() > ExtraConstants.MAX_CARD_LENGTH) {
                throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_CARD_LENGTH);
            }
            String binNumber = cardNumber.substring(0, 6);
            String expiryYear = theiaPaymentRequest.getExpiryYear();
            String expiryMonth = theiaPaymentRequest.getExpiryMonth();
            String txnMode = theiaPaymentRequest.getTxnMode();

            String cardType = null;

            if (PayMethod.CREDIT_CARD.getOldName().equals(txnMode) || PayMethod.EMI.getOldName().equals(txnMode)) {
                cardType = PayMethod.CREDIT_CARD.name();
            } else if (PayMethod.DEBIT_CARD.getOldName().equals(txnMode)) {
                cardType = PayMethod.DEBIT_CARD.name();
            }

            BankCardRequest bankCardRequest;

            BinUtils.logSixDigitBinLength(binNumber);
            String cardScheme = cardUtils.fetchCardSchemeUsingCustomLogic(binNumber);

            if (StringUtils.isBlank(cardType)) {
                bankCardRequest = new BankCardRequest(cardNumber, cvv, expiryYear, expiryMonth, cardScheme);
            } else {
                bankCardRequest = new BankCardRequest(cardNumber, cvv, expiryYear, expiryMonth, cardType, cardScheme);
            }

            cardRequest = new CardRequest(binNumber, bankCardRequest);
        }
        return cardRequest;

    }

    /**
     * @param theiaPaymentRequest
     * @return
     * @throws CashierCheckedException
     */
    public CardRequest preapreImpsCardRequest(final TheiaPaymentRequest theiaPaymentRequest)
            throws CashierCheckedException {
        CardRequest cardRequest;

        if (StringUtils.isNotBlank(theiaPaymentRequest.getSavedCardId())) {

            SavedImpsCardRequest savedImpsCardRequest = new SavedImpsCardRequest(theiaPaymentRequest.getSavedCardId(),
                    theiaPaymentRequest.getOtp(), "IMPS");
            cardRequest = new CardRequest(savedImpsCardRequest);

        } else {

            IMPSCardRequest impsCardRequest = new IMPSCardRequest(theiaPaymentRequest.getMmid(),
                    theiaPaymentRequest.getOtp(), "IMPS", theiaPaymentRequest.getHolderMobileNo());
            cardRequest = new CardRequest(impsCardRequest);
        }
        return cardRequest;
    }

    public ValidationRequest prepareValidationRequest(TheiaPaymentRequest theiaPaymentRequest, MerchantInfo merchInfo,
            CashierWorkflow cashierWorkflow, BinDetail binDetail) throws PaytmValidationException {
        boolean isAddMoney = false;

        if (ExtraConstants.ADD_MONEY_FLAG_VALUE.equals(theiaPaymentRequest.getAddMoneyFlag())) {
            isAddMoney = true;
        }

        String entityId = merchantExtendInfoUtils.getEntityIDCorrespodingToMerchant(merchInfo.getMid());
        String bankId = null;
        String bankCode = null;

        if (null != theiaPaymentRequest.getBankCode()) {
            if (theiaPaymentRequest.getBankCode().equalsIgnoreCase("-1")) {
                throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE_NB);
            }

            BankInfoData bankInfo = merchantBankInfoDataService.getBankInfo(theiaPaymentRequest.getBankCode());

            if (bankInfo == null) {
                LOGGER.warn("Bank Details not found : {}", theiaPaymentRequest.getBankCode());
                throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE_NB);
            }

            bankId = String.valueOf(bankInfo.getBankId());
            bankCode = theiaPaymentRequest.getBankCode();
        } else {
            if (StringUtils.isNotBlank(theiaPaymentRequest.getCardNo())) {

                // Added as part of PGP-565
                // Request is being submitted via a Card. This Needs to be
                // validated to prevent malformed inputs by User.
                if (!StringUtils.isNumeric(theiaPaymentRequest.getCardNo())
                        || theiaPaymentRequest.getCardNo().length() < ExtraConstants.MIN_CARD_LENGTH
                        || theiaPaymentRequest.getCardNo().length() > ExtraConstants.MAX_CARD_LENGTH) {
                    throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_CARD_LENGTH);
                }
                // Added as part of PGP-565

                if ((binDetail != null) && StringUtils.isNotBlank(binDetail.getBankCode())) {
                    BankInfoData bankInfo = merchantBankInfoDataService.getBankInfo(binDetail.getBankCode());
                    bankId = String.valueOf(bankInfo.getBankId());
                    bankCode = binDetail.getBankCode();
                } else {
                    if (((binDetail != null) && StringUtils.isBlank(binDetail.getBankCode()) && !binDetail
                            .getIsIndian()) || (binDetail == null)) {
                        bankId = ExtraConstants.INTERNATIONAL_BANK_ID;
                        bankCode = ExtraConstants.INTERNATIONAL_BANK_CODE;
                    }
                }
            }

            // Added as part of PGP-565
            if (StringUtils.isNotBlank(theiaPaymentRequest.getCvv())
                    && !StringUtils.isNumeric(theiaPaymentRequest.getCvv())) {
                throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_CVV);
            }
            // Added as part of PGP-565
        }

        if (CashierWorkflow.UPI.equals(cashierWorkflow) || CashierWorkflow.ADD_MONEY_UPI.equals(cashierWorkflow)) {
            if (StringUtils.isBlank(theiaPaymentRequest.getSavedCardId())) {
                validateVPA(theiaPaymentRequest.getVpa());
            }
        }

        return new ValidationRequest(theiaPaymentRequest.getTxnMode(), null, bankCode, isAddMoney, entityId, bankId,
                getPSULimitInfo());
    }

    private void validateVPA(String vpa) throws PaytmValidationException {
        boolean isValid = false;
        if (StringUtils.isNotBlank(vpa) && vpa.length() <= 255) {
            String[] vpaSplit = vpa.split("@");
            if (vpaSplit.length == 2) {
                String handle = vpaSplit[0];
                String psp = vpaSplit[1];
                if (StringUtils.isNotBlank(psp) && StringUtils.isNotBlank(handle)) {
                    isValid = psp.matches(ExtraConstants.VPA_REGEX) && handle.matches(ExtraConstants.VPA_REGEX);
                }
            }
        }
        if (!isValid) {
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_VPA);
        }
    }

    private String getPaytmProperty(String propertyName) {
        PaytmProperty paytmProperty = configurationDataService.getPaytmProperty(propertyName);
        if (paytmProperty != null) {
            return paytmProperty.getValue();
        }
        return StringUtils.EMPTY;
    }

    private static List<String> stringToList(String toSplit, String delimiter) {
        String[] arr = toSplit.split(delimiter);
        return Arrays.asList(arr);
    }

    private PSULimit getPSULimitInfo() {

        String nbCapBankList = getPaytmProperty(TheiaConstant.PSULimit.NB_LIMIT_BANK_LIST);
        String nbCapMaxAmount = getPaytmProperty(TheiaConstant.PSULimit.NB_LIMIT_MAX_AMOUNT);
        String capMerchantList = getPaytmProperty(TheiaConstant.PSULimit.NB_LIMIT_MERCHANT_LIST);
        String nbCapApplicable = getPaytmProperty(TheiaConstant.PSULimit.NB_LIMITS_APPLICABLE);
        String cardCapMaxAmount = getPaytmProperty(TheiaConstant.PSULimit.CARD_LIMIT_MAX_AMOUNT);
        String cardCapApplicable = getPaytmProperty(TheiaConstant.PSULimit.CARD_LIMITS_APPLICABLE);
        String cardCapBankList = getPaytmProperty(TheiaConstant.PSULimit.CARD_LIMIT_BANK_LIST);
        String sbiCardBlocked = getPaytmProperty(TheiaConstant.PSULimit.SBI_CARD_BLOCKED);

        PSULimit psuLimitInfo = new PSULimit();
        psuLimitInfo.setCapMerchantList(stringToList(capMerchantList, ","));
        psuLimitInfo.setCardCapApplicable(cardCapApplicable);
        psuLimitInfo.setCardCapMaxAmount(cardCapMaxAmount);
        psuLimitInfo.setNbCapApplicable(nbCapApplicable);
        psuLimitInfo.setNbCapBankList(stringToList(nbCapBankList, ","));
        psuLimitInfo.setNbCapMaxAmount(nbCapMaxAmount);
        psuLimitInfo.setCardCapBankList(stringToList(cardCapBankList, ","));
        psuLimitInfo.setSbiCardEnabled(sbiCardBlocked);

        return psuLimitInfo;
    }

    public CardRequest prepareUPIRequest(final TheiaPaymentRequest theiaPaymentRequest) throws CashierCheckedException {
        CardRequest cardRequest;
        if (StringUtils.isNotBlank(theiaPaymentRequest.getSavedCardId())) {
            SavedVPACardRequest savedVpaCardRequest = new SavedVPACardRequest(theiaPaymentRequest.getSavedCardId(),
                    theiaPaymentRequest.getVpa());
            cardRequest = new CardRequest(savedVpaCardRequest);
        } else {
            VPACardRequest vpaCardRequest = new VPACardRequest(theiaPaymentRequest.getVpa());
            cardRequest = new CardRequest(vpaCardRequest);
        }
        return cardRequest;
    }
}
