package com.paytm.pgplus.biz.ppbl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.paytm.pgplus.biz.core.model.ppb.AccountBalanceResponse;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.exception.AccountMismatchException;
import com.paytm.pgplus.biz.exception.AccountNotExistsException;
import com.paytm.pgplus.biz.model.GstInformation;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.NativeCoreService;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowRequestCreationHelper;
import com.paytm.pgplus.checksum.crypto.impl.RSAEncryption;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.log.EventLogger;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeUncheckedException;
import com.paytm.pgplus.facade.user.models.request.ValidatePassCodeRequest;
import com.paytm.pgplus.facade.user.models.response.PassCodeVerificationResponse;
import com.paytm.pgplus.facade.user.services.IAuthentication;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.payloadvault.theia.utils.AdditionalInfoUtil;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.logging.ExtendedLogger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.paytm.pgplus.biz.utils.BizConstant.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.TRUE;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.USER_INVESTMENT_CONSENT_FLAG;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.INVOICE_DATE;

/**
 * Created by rahulverma on 5/10/17.
 */

@Service("bizAccessTokenPaymentRequestBuilder")
public class BizAccessTokenPaymentRequestBuilder implements IBizAccessTokenPaymentRequestBuilder {
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(BizAccessTokenPaymentRequestBuilder.class);

    @Autowired
    @Qualifier("authenticationImpl")
    private IAuthentication authenticationImpl;

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("nativeCoreService")
    private NativeCoreService nativeCoreService;

    @Autowired
    @Qualifier("commonFlowHelper")
    private WorkFlowRequestCreationHelper workRequestCreator;

    private static final Logger LOGGER = LoggerFactory.getLogger(BizAccessTokenPaymentRequestBuilder.class);

    private static final RSAEncryption RSA_ENCRYPTION = RSAEncryption.getInstance();
    private static final String PAYMENTS_BANK_CODE = "PPBL";
    private static final String BANK_SCOPE_TOKEN_NAME = "ppblToken";
    private static final String PASS_THROUGH_EXTEND_INFO_KEY = "passThroughExtendInfo";
    private static final String PAYTM_CC_ID = "paytmCCId";
    private static final String LENDER_ID = "lenderId";
    private static final String AUTHORIZATION_TOKEN = "authorizationToken";
    private static final String POSTPAID_SCOPE = "dc_txn";
    private static final String PPBL_SCOPE = "bank_txn";
    private static final int MPIN_LENGTH = 10;

    private static final int DIGITAL_CREDIT_PAYMENT_DETAILS_ARRAY_LENGTH = 3;

    public void buildPPBPaymentRequestParameters(WorkFlowTransactionBean workFlowTransactionBean,
            Map<String, String> channelInfo) throws PaytmValidationException {

        if (workFlowTransactionBean != null && workFlowTransactionBean.getWorkFlowBean() != null
                && workFlowTransactionBean.getUserDetails() != null) {
            try {
                WorkFlowRequestBean workFlowRequestBean = workFlowTransactionBean.getWorkFlowBean();

                if ((ERequestType.NATIVE_MF.equals(workFlowRequestBean.getRequestType())
                        || ERequestType.NATIVE_MF.equals(workFlowRequestBean.getSubRequestType())
                        || ERequestType.NATIVE_ST.equals(workFlowRequestBean.getRequestType()) || ERequestType.NATIVE_ST
                            .equals(workFlowRequestBean.getSubRequestType()))
                        && Boolean.valueOf(workFlowRequestBean.getValidateAccountNumber())) {
                    if (isPaymentsBankAllowed(workFlowTransactionBean)) {
                        LOGGER.info("Payment Bank is allowed for this transaction {}, Going to Match Account No",
                                workFlowRequestBean.getRequestType().getType());
                        // Invoke BMW API
                        GenericCoreResponseBean<AccountBalanceResponse> bankResponse = workFlowHelper
                                .fetchAccountBalance(workFlowTransactionBean);
                        if (bankResponse.isSuccessfullyProcessed() && bankResponse.getResponse() != null) {
                            if (!StringUtils.equals(bankResponse.getResponse().getAccountNumber(),
                                    workFlowRequestBean.getAccountNumber())) {
                                LOGGER.error("Account Number Mismatch For {}, Given {},found {}", workFlowRequestBean
                                        .getRequestType().getType(), workFlowRequestBean.getAccountNumber(),
                                        bankResponse.getResponse().getAccountNumber());
                                throw new AccountMismatchException("Account Number Mismatch");
                            }
                        } else {
                            LOGGER.error("Account Number Not Exist For {} Payment Given {}", workFlowRequestBean
                                    .getRequestType().getType(), workFlowRequestBean.getAccountNumber());
                            throw new AccountNotExistsException("Account Not Exists");
                        }
                    } else {
                        LOGGER.error("Account Number Not Exist For {} Payment Given {}", workFlowRequestBean
                                .getRequestType().getType(), workFlowRequestBean.getAccountNumber());
                        throw new AccountNotExistsException("Account Not Exists");
                    }
                }

                String accessToken = workFlowRequestBean.getPaymentDetails();
                if (StringUtils.isBlank(accessToken)) {
                    LOGGER.error("Blank accessToken for Paytm payments bank");
                    throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PASS_CODE_BLANK);
                }
                if ((ERequestType.NATIVE.equals(workFlowRequestBean.getRequestType())
                        || ERequestType.NATIVE_MF.equals(workFlowRequestBean.getRequestType())
                        || ERequestType.NATIVE_MF.equals(workFlowRequestBean.getSubRequestType())
                        || ERequestType.NATIVE_ST.equals(workFlowRequestBean.getSubRequestType())
                        || ERequestType.NATIVE_ST.equals(workFlowRequestBean.getRequestType()) || ERequestType.NATIVE_SUBSCRIPTION
                            .equals(workFlowRequestBean.getRequestType())) && accessToken.length() <= MPIN_LENGTH) {

                    PassCodeVerificationResponse authVerificationResponse = authenticationImpl
                            .validatePassCode(buildValidatePassCodeRequest(workFlowRequestBean.getPaymentDetails(),
                                    workFlowRequestBean.getOauthClientId(), workFlowRequestBean.getOauthSecretKey(),
                                    workFlowTransactionBean.getUserDetails().getMobileNo(),
                                    workFlowRequestBean.getToken(), PPBL_SCOPE));

                    if (authVerificationResponse != null
                            && authVerificationResponse.isSuccessfullyProcessed()
                            && StringUtils
                                    .isNotBlank(authVerificationResponse.getAuthorizationToken().getAccessToken())) {

                        accessToken = authVerificationResponse.getAuthorizationToken().getAccessToken();
                    } else if (authVerificationResponse != null) {
                        LOGGER.warn("Pass Code validation failed for PPBL : {}",
                                authVerificationResponse.getResponseMessage());
                        throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PASS_CODE,
                                authVerificationResponse.getResponseMessage());
                    }
                }
                Map<String, String> passThroughExtendInfoMap = new HashMap<String, String>();
                passThroughExtendInfoMap.put(BANK_SCOPE_TOKEN_NAME, accessToken);
                if (StringUtils.isNotBlank(workFlowRequestBean.getPpblAccountType())) {
                    passThroughExtendInfoMap.put(accountType, workFlowRequestBean.getPpblAccountType());
                }
                // For setting passthroughinfo for NativeJson Request
                workRequestCreator
                        .setChannelInfoForNativeJsonRequest(workFlowTransactionBean, passThroughExtendInfoMap);
                if (workFlowRequestBean.getUseInvestmentAsFundingSource()) {
                    passThroughExtendInfoMap.put(USER_INVESTMENT_CONSENT_FLAG,
                            String.valueOf(workFlowRequestBean.getUseInvestmentAsFundingSource()));
                    EventLogger.pushEventLog(MDC.get("MID"), MDC.get("ORDER_ID"),
                            EventNameEnum.USE_INVESTMENT_AS_FUNDING_SOURCE, new HashMap<String, String>() {
                                {
                                    put(USER_INVESTMENT_CONSENT_FLAG, TRUE);
                                }
                            });

                }
                setDeepIntegrationFieldsToPassThroughExtendedInfo(workFlowTransactionBean, passThroughExtendInfoMap);
                String passThroughJson = JsonMapper.mapObjectToJson(passThroughExtendInfoMap);
                String encodedPassThrough = new String(Base64.getEncoder().encode(passThroughJson.getBytes()));
                channelInfo.put(PASS_THROUGH_EXTEND_INFO_KEY, encodedPassThrough);
                return;

            } catch (FacadeUncheckedException | FacadeCheckedException e) {
                throw new PaytmValidationException(
                        PaytmValidationExceptionType.INVALID_PAYMODE_PAYTM_PAYMENTS_BANK.getValidationFailedMsg(), e);
            }
        }
        throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE_PAYTM_PAYMENTS_BANK);

    }

    private boolean isPaymentsBankAllowed(WorkFlowTransactionBean workFlowTransactionBean) {
        return workFlowTransactionBean.getUserDetails() != null
                && workFlowTransactionBean.getUserDetails().isSavingsAccountRegistered();
    }

    public void buildDigitalCreditRequestParameters(WorkFlowTransactionBean workFlowTransactionBean,
            Map<String, String> channelInfo) throws PaytmValidationException {

        if (workFlowTransactionBean != null && workFlowTransactionBean.getWorkFlowBean() != null
                && workFlowTransactionBean.getUserDetails() != null) {
            try {
                WorkFlowRequestBean workFlowRequestBean = workFlowTransactionBean.getWorkFlowBean();
                String paymentDetails = workFlowRequestBean.getPaymentDetails();
                String accessToken = null;
                if (StringUtils.isBlank(paymentDetails)) {
                    LOGGER.error("Blank accessToken and other payment details for Digital credit");
                    throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PASS_CODE_BLANK);
                }
                String[] paymentDetailsArr = paymentDetails.split(Pattern.quote("|"));

                if (ERequestType.NATIVE.equals(workFlowRequestBean.getRequestType())) {

                    if (StringUtils.isEmpty(paymentDetailsArr[0]) || StringUtils.isEmpty(paymentDetailsArr[1])) {

                        LOGGER.error("Blank accessToken and other payment details for Digital credit");
                        throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PASS_CODE_BLANK);
                    }

                } else if (StringUtils.isEmpty(paymentDetailsArr[0]) || StringUtils.isEmpty(paymentDetailsArr[1])) {

                    LOGGER.error("Blank accessToken and other payment details for Digital credit");
                    throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PASS_CODE_BLANK);
                }

                if (paymentDetailsArr.length == DIGITAL_CREDIT_PAYMENT_DETAILS_ARRAY_LENGTH) {
                    accessToken = paymentDetailsArr[2];
                }

                if (StringUtils.isNotBlank(accessToken)
                        && ERequestType.NATIVE.equals(workFlowRequestBean.getRequestType())
                        && accessToken.length() <= MPIN_LENGTH) {

                    PassCodeVerificationResponse authVerificationResponse = authenticationImpl
                            .validatePassCode(buildValidatePassCodeRequest(accessToken,
                                    workFlowRequestBean.getOauthClientId(), workFlowRequestBean.getOauthSecretKey(),
                                    workFlowTransactionBean.getUserDetails().getMobileNo(),
                                    workFlowRequestBean.getToken(), POSTPAID_SCOPE));

                    if (authVerificationResponse != null
                            && authVerificationResponse.isSuccessfullyProcessed()
                            && StringUtils
                                    .isNotBlank(authVerificationResponse.getAuthorizationToken().getAccessToken())) {

                        accessToken = authVerificationResponse.getAuthorizationToken().getAccessToken();
                    } else {
                        LOGGER.warn("Pass Code validation failed for Digital Credit : {}",
                                authVerificationResponse.getResponseMessage());
                        throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PASS_CODE,
                                authVerificationResponse.getResponseMessage());
                    }
                }

                channelInfo.put(PAYTM_CC_ID, paymentDetailsArr[0]);
                channelInfo.put(LENDER_ID, paymentDetailsArr[1]);

                if (StringUtils.isNotBlank(accessToken)) {
                    channelInfo.put(AUTHORIZATION_TOKEN, accessToken);
                } else {
                    channelInfo.put(AUTHORIZATION_TOKEN, "SSO_TOKEN|" + workFlowRequestBean.getToken());
                }

                return;

            } catch (FacadeUncheckedException | FacadeCheckedException e) {
                throw new PaytmValidationException(
                        PaytmValidationExceptionType.INVALID_PAYMODE_PAYTMCC.getValidationFailedMsg(), e);
            }
        }
        throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE_PAYTMCC);
    }

    @Override
    public void buildUPIRequestParameters(WorkFlowTransactionBean workFlowTransactionBean,
            Map<String, String> channelInfo) throws PaytmValidationException {

        if (workFlowTransactionBean != null && workFlowTransactionBean.getWorkFlowBean() != null) {
            try {
                channelInfo.put(BizConstant.VIRTUAL_PAYMENT_ADDRESS, workFlowTransactionBean.getWorkFlowBean()
                        .getVirtualPaymentAddress());

                channelInfo.put(BizConstant.UPI_ACC_REF_ID, workFlowTransactionBean.getWorkFlowBean().getUpiAccRefId());

                if (ERequestType.NATIVE_MF_SIP.equals(workFlowTransactionBean.getWorkFlowBean().getRequestType())) {
                    workFlowHelper.setAccountInfoInChannelInfo(channelInfo, workFlowTransactionBean.getWorkFlowBean());
                }
                if (ERequestType.isSubscriptionCreationRequest(workFlowTransactionBean.getWorkFlowBean()
                        .getRequestType().getType())
                        && !EPayMode.ADDANDPAY.equals(workFlowTransactionBean.getWorkFlowBean()
                                .getPaytmExpressAddOrHybrid())) {
                    channelInfo.put(UPI_MANDATE, "true");

                }

                if (workFlowTransactionBean.getWorkFlowBean().isNativeDeepLinkReqd()) {
                    channelInfo.put(TheiaConstant.ChannelInfoKeys.IS_DEEP_LINK_REQ, "true");
                }
                Map<String, String> extendedInfo = workFlowTransactionBean.getExtendInfo();

                if (extendedInfo == null) {
                    LOGGER.warn("No extended info available so creating new one");
                    extendedInfo = new HashMap<String, String>();
                }
                extendedInfo
                        .put(BizConstant.UPI_ACC_REF_ID, workFlowTransactionBean.getWorkFlowBean().getUpiAccRefId());

                extendedInfo.put(BizConstant.VIRTUAL_PAYMENT_ADDRESS, workFlowTransactionBean.getWorkFlowBean()
                        .getVirtualPaymentAddress());

                workFlowTransactionBean.setExtendInfo(extendedInfo);

                workFlowTransactionBean.getWorkFlowBean().getExtendInfo()
                        .setVirtualPaymentAddr(workFlowTransactionBean.getWorkFlowBean().getVirtualPaymentAddress());

                if (ERequestType.DYNAMIC_QR.equals(workFlowTransactionBean.getWorkFlowBean().getRequestType())
                        && workFlowTransactionBean.getWorkFlowBean().isUpiPushExpressSupported()) {
                    createPassThroughExtendedInfoNativeUPI(workFlowTransactionBean, channelInfo);
                } else if (workFlowTransactionBean.getWorkFlowBean().isUpiPushExpressSupported()) {
                    nativeCoreService.ceatePassThroughExtendedInfoNativeUPI(workFlowTransactionBean, channelInfo);
                } else if (workFlowTransactionBean.getWorkFlowBean().isNativeDeepLinkReqd()) {
                    nativeCoreService.ceatePassThroughExtendedInfoNativeUPIIntent(workFlowTransactionBean, channelInfo);
                }

                workRequestCreator.setZeroSubsInfoInPassThroughInfo(channelInfo,
                        workFlowTransactionBean.getWorkFlowBean());
            } catch (FacadeUncheckedException e) {
                throw new PaytmValidationException(
                        PaytmValidationExceptionType.INVALID_PAYMODE_UPI.getValidationFailedMsg(), e);
            }
        } else {
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE_UPI);
        }
    }

    private ValidatePassCodeRequest buildValidatePassCodeRequest(String passCode, String oAuthClientId,
            String oAuthSecretKey, String mobileNo, String token, String scope) throws PaytmValidationException {

        if (StringUtils.isBlank(passCode) || StringUtils.isBlank(oAuthClientId) || StringUtils.isBlank(oAuthSecretKey)
                || StringUtils.isBlank(mobileNo) || StringUtils.isBlank(token)) {
            LOGGER.error("Blank passcode|oAuthClientId|oAuthSecretKey|mobileNo for Paytm payments bank");
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PASS_CODE_BLANK);
        }
        String encPassCode = RSA_ENCRYPTION.encrypt(passCode);
        ValidatePassCodeRequest validatePassCodeRequest = new ValidatePassCodeRequest(mobileNo, encPassCode,
                oAuthClientId, oAuthSecretKey);
        validatePassCodeRequest.setScope(scope);
        validatePassCodeRequest.setPaytmToken(token);
        return validatePassCodeRequest;
    }

    private void createPassThroughExtendedInfoNativeUPI(WorkFlowTransactionBean workFlowTransactionBean,
            Map<String, String> channelInfo) {

        Map<String, String> passThroughExtendInfo = new HashMap<String, String>();
        if (StringUtils.isNotBlank(workFlowTransactionBean.getWorkFlowBean().getUpiAccRefId())) {
            EXT_LOGGER.customInfo("Setting AccRefId for UPI PUSH Flow");
            passThroughExtendInfo.put(FacadeConstants.UPI_ACC_REF_ID, workFlowTransactionBean.getWorkFlowBean()
                    .getUpiAccRefId());
            passThroughExtendInfo.put(FacadeConstants.ORIGIN_CHANNEL, workFlowTransactionBean.getWorkFlowBean()
                    .getOriginChannel());
        } else {
            EXT_LOGGER.customInfo("Setting IFSC and Account Number for UPI PUSH flow");
            String ifsc = StringUtils.isBlank(workFlowTransactionBean.getWorkFlowBean().getIfsc()) ? ""
                    : workFlowTransactionBean.getWorkFlowBean().getIfsc();
            passThroughExtendInfo.put(FacadeConstants.IFSC, ifsc);
            passThroughExtendInfo.put(FacadeConstants.ACCOUNT_NUMBER, workFlowTransactionBean.getWorkFlowBean()
                    .getAccountNumber());
        }

        passThroughExtendInfo.put(FacadeConstants.DEVICE_ID, workFlowTransactionBean.getWorkFlowBean().getDeviceId());
        passThroughExtendInfo.put(FacadeConstants.MOBILE_NO, workFlowTransactionBean.getUserDetails().getMobileNo());
        passThroughExtendInfo
                .put(FacadeConstants.SEQUENCE_NUMBER, workFlowTransactionBean.getWorkFlowBean().getSeqNo());
        passThroughExtendInfo.put(FacadeConstants.MPIN, workFlowTransactionBean.getWorkFlowBean().getMpin());
        passThroughExtendInfo.put(FacadeConstants.PAYER_VPA, workFlowTransactionBean.getWorkFlowBean()
                .getVirtualPaymentAddress());
        passThroughExtendInfo.put(FacadeConstants.ORDER_ID, workFlowTransactionBean.getWorkFlowBean().getOrderID());
        if (org.apache.commons.lang.StringUtils.isNotBlank(workFlowTransactionBean.getWorkFlowBean().getAppId())) {
            passThroughExtendInfo.put(FacadeConstants.APP_ID, workFlowTransactionBean.getWorkFlowBean().getAppId());
        }
        if (StringUtils.isNotBlank(workFlowTransactionBean.getWorkFlowBean().getSubscriptionID())) {
            passThroughExtendInfo.put(FacadeConstants.UPI_RECURRING_KEY, "UPI_SUBSCRIPTION_"
                    + workFlowTransactionBean.getWorkFlowBean().getPaytmMID() + "_"
                    + workFlowTransactionBean.getWorkFlowBean().getOrderID());
        }
        setGstInfoInPassThroughExtendInfo(workFlowTransactionBean.getWorkFlowBean(), passThroughExtendInfo);
        passThroughExtendInfo.put(ExtendedInfoKeys.MERCHANT_VPA, workFlowTransactionBean.getWorkFlowBean()
                .getPaymentRequestBean().getMerchantVpa());
        String passThroughJson = org.apache.commons.lang.StringUtils.EMPTY;
        try {
            passThroughJson = JsonMapper.mapObjectToJson(passThroughExtendInfo);
        } catch (Exception e) {
            LOGGER.error("Exception occurred while transforming passThroughExtendInfoMap {}", e);
        }
        String encodedPassThrough = new String(Base64.getEncoder().encode(passThroughJson.getBytes()));
        channelInfo
                .put(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.PASS_THROUGH_EXTEND_INFO_KEY, encodedPassThrough);

    }

    private void setGstInfoInPassThroughExtendInfo(WorkFlowRequestBean workFlowBean,
            Map<String, String> passThroughExtendInfo) {

        String REQUEST_TYPE = AdditionalInfoUtil.getValueFromAdditionalInfo(workFlowBean.getPaymentRequestBean(),
                "REQUEST_TYPE");

        if (UPI_POS_ORDER.equals(REQUEST_TYPE)) {
            /**
             * This is for send payment Request
             */
            Map<String, String> posOrderData = workFlowBean.getPosOrderData();
            if (posOrderData == null) {
                LOGGER.info("posOrderData is null");
                return;
            }

            String gstInformationString = posOrderData.get("gstInformation");
            LOGGER.info("GST Infromation or POS ORDER");
            if (gstInformationString != null) {
                GstInformation gstInformation = null;
                try {
                    gstInformation = JsonMapper.readValue(gstInformationString, new TypeReference<GstInformation>() {
                    });
                    passThroughExtendInfo.put(GST_IN, gstInformation.getGstIn());
                    passThroughExtendInfo.put(GST_BRKUP, gstInformation.getGstBrkUp());
                    passThroughExtendInfo.put(INVOICE_NO, gstInformation.getInvoiceNo());
                    passThroughExtendInfo.put(INVOICE_DATE, gstInformation.getInvoiceDate());
                    passThroughExtendInfo.put(EN_TIPS, gstInformation.getEnTips());
                    passThroughExtendInfo.put(PAYER_CONSENT, gstInformation.getPayerConsent());
                    passThroughExtendInfo.put(TIPS, workFlowBean.getPaymentRequestBean().getTipAmount());
                } catch (IOException e) {
                    LOGGER.error("Error occured while convering GstInformation String to Object ", e);
                }
            }

        } else {
            /**
             * This is for dynamic QR Code
             */
            passThroughExtendInfo.put(GST_IN,
                    AdditionalInfoUtil.getValueFromAdditionalInfo(workFlowBean.getPaymentRequestBean(), GST_IN));

            String gstBrkUp = AdditionalInfoUtil.getValueFromAdditionalInfo(workFlowBean.getPaymentRequestBean(),
                    GST_BRKUP);
            if (org.apache.commons.lang.StringUtils.isNotBlank(gstBrkUp)) {
                try {
                    String decodedGstBrkUp = new String(Base64.getDecoder().decode(gstBrkUp));
                    passThroughExtendInfo.put(GST_BRKUP, decodedGstBrkUp);
                } catch (Exception e) {
                    LOGGER.error("Exception occured while decoding gst_brk_up info ", e);
                }
            }

            passThroughExtendInfo.put(INVOICE_NO,
                    AdditionalInfoUtil.getValueFromAdditionalInfo(workFlowBean.getPaymentRequestBean(), INVOICE_NO));

            passThroughExtendInfo.put(INVOICE_DATE,
                    AdditionalInfoUtil.getValueFromAdditionalInfo(workFlowBean.getPaymentRequestBean(), INVOICE_DATE));

            passThroughExtendInfo.put(EN_TIPS,
                    AdditionalInfoUtil.getValueFromAdditionalInfo(workFlowBean.getPaymentRequestBean(), EN_TIPS));

            passThroughExtendInfo.put(PAYER_CONSENT,
                    AdditionalInfoUtil.getValueFromAdditionalInfo(workFlowBean.getPaymentRequestBean(), PAYER_CONSENT));

            passThroughExtendInfo.put(TIPS, workFlowBean.getPaymentRequestBean().getTipAmount());
        }

    }

    private void setDeepIntegrationFieldsToPassThroughExtendedInfo(WorkFlowTransactionBean flowTransBean,
            Map<String, String> passThroughExtendInfoMap) {
        try {
            final Map<String, String> extendedInfo = flowTransBean.getWorkFlowBean().getEnvInfoReqBean()
                    .getExtendInfo();
            final Map<String, String> riskExtendInfo = flowTransBean.getWorkFlowBean().getRiskExtendedInfo();
            if (extendedInfo == null) {
                LOGGER.info("ExtendInfo is null");
            } else {
                if (extendedInfo.containsKey(LATITUDE))
                    passThroughExtendInfoMap.put(TheiaConstant.RequestParams.CUSTOMER_LAT, extendedInfo.get(LATITUDE));
                if (extendedInfo.containsKey(LONGITUDE))
                    passThroughExtendInfoMap
                            .put(TheiaConstant.RequestParams.CUSTOMER_LONG, extendedInfo.get(LONGITUDE));
                if (extendedInfo.containsKey(CLIENT_IP))
                    passThroughExtendInfoMap.put(TheiaConstant.RequestParams.SOURCE_IP, extendedInfo.get(CLIENT_IP));
                if (extendedInfo.containsKey(OS_VERSION))
                    passThroughExtendInfoMap.put(OS_VERSION, extendedInfo.get(TheiaConstant.RequestParams.OS_VERSION));
                if (extendedInfo.containsKey(TheiaConstant.RequestParams.DEVICE_ID_FOR_RISK))
                    passThroughExtendInfoMap.put(DEVICE_ID_FOR_RISK, extendedInfo.get(DEVICE_ID_FOR_RISK));
                if (extendedInfo.containsKey(OS_TYPE))
                    passThroughExtendInfoMap.put(TheiaConstant.RequestParams.CLIENT_2, extendedInfo.get(OS_TYPE));
                if (extendedInfo.containsKey(PLATFORM))
                    passThroughExtendInfoMap.put(TheiaConstant.RequestParams.CHANNEL_2, extendedInfo.get(PLATFORM));
                if (extendedInfo.containsKey(APP_VERSION))
                    passThroughExtendInfoMap.put(APP_VERSION, extendedInfo.get(APP_VERSION));
            }

            if (riskExtendInfo == null) {
                LOGGER.info("riskExtendInfo is null");
            } else {
                if (riskExtendInfo.containsKey(DEVICE_IDENTIFIER))
                    passThroughExtendInfoMap.put(DEVICE_IDENTIFIER,
                            riskExtendInfo.get(TheiaConstant.RequestParams.DEVICE_IDENTIFIER));
                if (riskExtendInfo.containsKey(NETWORK_TYPE))
                    passThroughExtendInfoMap.put(NETWORK_TYPE,
                            riskExtendInfo.get(TheiaConstant.RequestParams.NETWORK_TYPE));
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred while setting values to passThroughChannelInfoMap {}", e);
        }
    }
}
