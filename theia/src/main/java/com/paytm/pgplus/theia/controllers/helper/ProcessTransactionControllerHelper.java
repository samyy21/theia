package com.paytm.pgplus.theia.controllers.helper;

//import com.paytm.pgplus.aoaSubscriptionClient.model.response.AoaSubscriptionCreateResponse;

import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowRequestCreationHelper;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.common.enums.SubsPaymentMode;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.models.PaymentMode;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.utils.AdditionalInfoUtil;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.request.SubscriptionTransactionRequestBody;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import com.paytm.pgplus.subscriptionClient.utils.SubscriptionUtil;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.datamapper.impl.BizRequestResponseMapperImpl;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.subscription.impl.NativeSubscriptionHelperImpl;
import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.helper.EnhancedCashierPageServiceHelper;
import com.paytm.pgplus.theia.services.impl.TheiaSessionDataServiceImpl;
import com.paytm.pgplus.theia.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.AUTO_APP_INVOKE;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.DISABLE_ENHANCED_ON_SEAMLESS_NB;
import static com.paytm.pgplus.enums.EChannelId.WAP;
import static com.paytm.pgplus.enums.EChannelId.WEB;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.MID;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.ORDER_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.ORDERID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.TXN_CANCEL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.MANDATE_TYPE;

@Component("processTransactionControllerHelper")
public class ProcessTransactionControllerHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessTransactionControllerHelper.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(ProcessTransactionControllerHelper.class);

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private TheiaSessionDataServiceImpl theiaSessionDataService;

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    @Autowired
    private SubscriptionUtil subscriptionUtil;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private DynamicQRUtil dynamicQRUtil;

    @Autowired
    @Qualifier("merchantPreferenceProvider")
    private MerchantPreferenceProvider merchantPreferenceProvider;

    @Autowired
    EnhancedCashierPageServiceHelper enhancedCashierPageServiceHelper;

    @Autowired
    private ProcessTransactionUtil processTransactionUtil;

    @Autowired
    private WorkFlowRequestCreationHelper workFlowRequestCreationHelper;

    @Autowired
    private FF4JUtil ff4JUtil;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    @Qualifier("aoaUtils")
    private AOAUtils aoaUtils;

    private static final String MODE = "mode";
    private static final String MOBILE_NUMBER = "mobile_number";
    private static final String REQUEST_TYPE = "REQUEST_TYPE";
    private static final String QR_MERCHANT = "QR_MERCHANT";

    public void checkAndSetIfDynamicQRFlow(PaymentRequestBean paymentRequestData) {
        EXT_LOGGER.customInfo("Getting preference for request {}", paymentRequestData);
        boolean isQREnabled = merchantPreferenceProvider.isQRCodePaymentEnabled(paymentRequestData);
        boolean isQRWith2FAEnabledPCF = merchantPreferenceProvider.isDynamicQR2FAEnabledWithPCF(paymentRequestData);
        boolean isQRWith2FAEnabled = merchantPreferenceProvider.isDynamicQR2FAEnabled(paymentRequestData);
        boolean isDynamicQREdcRequest = dynamicQRUtil.isDynamicQREdcRequest(paymentRequestData);
        boolean isOrderAlreadyCreated = dynamicQRUtil.isOrderAlreadyCreated(paymentRequestData);
        boolean isAoaDqrOrder = dynamicQRUtil.isAoaDqrOrder(paymentRequestData);

        LOGGER.info(
                "Found isQRWith2FAEnabledPCF => {} ,isQREnabled => {} , isQRWith2FAEnabled => {} , isDynamicQREdcRequest => {}, isOrderAlreadyCreated => {}",
                isQRWith2FAEnabledPCF, isQREnabled, isQRWith2FAEnabled, isDynamicQREdcRequest, isOrderAlreadyCreated);

        paymentRequestData.setQREnabled(isQREnabled);

        if ((isQREnabled || isQRWith2FAEnabled || isDynamicQREdcRequest || isQRWith2FAEnabledPCF
                || isOrderAlreadyCreated || isAoaDqrOrder)
                && (TheiaConstant.RequestTypes.OFFLINE.equals(paymentRequestData.getRequestType())
                        || TheiaConstant.RequestTypes.NATIVE.equals(paymentRequestData.getRequestType()) || TheiaConstant.RequestTypes.UNI_PAY
                            .equals(paymentRequestData.getRequestType()))
                && StringUtils.isBlank(paymentRequestData.getLinkId())
                && StringUtils.isBlank(paymentRequestData.getInvoiceId())
                && !processTransactionUtil.isEnhancedNativeFlow(paymentRequestData.getRequest())) {

            if (isQRWith2FAEnabled || isQRWith2FAEnabledPCF) {
                LOGGER.info("Changing Request Type to {}", ERequestType.DYNAMIC_QR_2FA.getType());
                paymentRequestData.setRequestType(TheiaConstant.RequestTypes.DYNAMIC_QR_2FA);
            } else {
                LOGGER.info("Changing Request Type to {}", ERequestType.DYNAMIC_QR.getType());
                paymentRequestData.setRequestType(TheiaConstant.RequestTypes.DYNAMIC_QR);
                String website = dynamicQRUtil.getWebsiteName(paymentRequestData);
                if (StringUtils.isNotBlank(website)) {
                    LOGGER.info("Changing Website type to {}", website);
                    paymentRequestData.setWebsite(website);
                }
                String callbackUrl = dynamicQRUtil.getCallbackUrl(paymentRequestData);
                if (StringUtils.isNotBlank(callbackUrl)) {
                    LOGGER.info("Changing CallbckUrl type to {}", callbackUrl);
                    paymentRequestData.setCallbackUrl(callbackUrl);
                }
                String peonUrl = dynamicQRUtil.getPeonUrl(paymentRequestData);
                if (StringUtils.isNotBlank(peonUrl)) {
                    LOGGER.info("Changing peonUrl type to {}", callbackUrl);
                    paymentRequestData.setPeonURL(peonUrl);
                }
            }
        }
    }

    public void checkForSeamlessNBCases(PaymentRequestBean paymentRequestData) {

        if (TheiaConstant.ExtraConstants.NB.equalsIgnoreCase(paymentRequestData.getPaymentTypeId())
                && StringUtils.isNotBlank(paymentRequestData.getBankCode())
                && (TheiaConstant.ExtraConstants.USRPWD.equals(paymentRequestData.getAuthMode())
                        || ERequestType.NATIVE_MF.getType().equals(paymentRequestData.getRequestType()) || ERequestType.NATIVE_MF
                        .getType().equals(paymentRequestData.getSubRequestType()))) {

            // TODO - Need to optimise the logic
            if ("KOTAK".equalsIgnoreCase(paymentRequestData.getBankCode())) {
                paymentRequestData.setBankCode("NKMB");
            }

            if ("ANDHRA".equalsIgnoreCase(paymentRequestData.getBankCode())) {
                paymentRequestData.setBankCode("ANDB");
            }

            if (TheiaConstant.RequestTypes.DEFAULT.equalsIgnoreCase(paymentRequestData.getRequestType())) {
                paymentRequestData.setRequestType(TheiaConstant.RequestTypes.SEAMLESS_NB);
                if (ff4JUtil.isFeatureEnabled(DISABLE_ENHANCED_ON_SEAMLESS_NB, paymentRequestData.getMid())) {
                    LOGGER.info("Setting enhanced Cashier Page Request as false for Seamless NB flow");
                    paymentRequestData.setEnhancedCashierPageRequest(false);
                }
            }
        }
    }

    public boolean checkIfEnhancedCashierFlow(PaymentRequestBean paymentRequestData, HttpServletRequest request) {
        UserAgentInfo userAgentInfo = new UserAgentInfo(request);

        String channelId = getChannelId(request, userAgentInfo);

        if (oAuthRedirect(request, paymentRequestData)) {
            LOGGER.info("oAuthRedirect, returning isEnhancedCashierPage=false");
            return false;
        }

        boolean isAutomaticEnhanceCashierAllowed = Boolean.valueOf(ConfigurationUtil.getProperty(
                "automaticEnhanceCashierAllowed", "false"));

        boolean isWapEnhancedCashierEnabledOnMerchant = merchantPreferenceService.isEnhancedCashierPageEnabled(
                paymentRequestData.getMid(), isAutomaticEnhanceCashierAllowed);
        boolean isWebEnhancedCashierEnabledOnMerchant = merchantPreferenceService.isWebEnhancedCashierPageEnabled(
                paymentRequestData.getMid(), isAutomaticEnhanceCashierAllowed);

        boolean isEnhancedCashierPage = false;
        if (TheiaConstant.RequestTypes.UNI_PAY.equalsIgnoreCase(paymentRequestData.getRequestType())
                && !StringUtils.isNotBlank(paymentRequestData.getPaymentTypeId())) {
            isEnhancedCashierPage = true;
        }

        if (!isEnhancedCashierHtmlTemplatePresent()) {
            return false;
        }

        if (allowForNativeAppInvokeFlow(paymentRequestData, request)) {
            /*
             * allowForAppInvokeFlow is being checked for AppInvoke flow, for
             * which we'll always show enhancedCashierpage
             */
            isEnhancedCashierPage = true;

        } else if (isEnhancedCashierFlowForLinkBased(paymentRequestData)
                || TheiaConstant.RequestTypes.SUBSCRIPTION.equalsIgnoreCase(paymentRequestData.getRequestType())
                || TheiaConstant.RequestTypes.CC_BILL_PAYMENT.equalsIgnoreCase(paymentRequestData.getRequestType())) {
            /*
             * This handles for linkbased Payments
             */
            if (isWapEnhancedCashierEnabledOnMerchant || isWebEnhancedCashierEnabledOnMerchant
                    || isRequestTypeAllowedForEnhance(paymentRequestData.getRequestType())) {
                isEnhancedCashierPage = true;
            }

        } else if ((TheiaConstant.RequestTypes.DEFAULT.equalsIgnoreCase(paymentRequestData.getRequestType())
                || TheiaConstant.RequestTypes.ADD_MONEY.equalsIgnoreCase(paymentRequestData.getRequestType()) || TheiaConstant.RequestTypes.DEFAULT_MF
                    .equalsIgnoreCase(paymentRequestData.getRequestType()))
                && (isWapEnhancedCashierEnabledOnMerchant || isWebEnhancedCashierEnabledOnMerchant)) {

            /*
             * This condition is for WEB calls, also it allows/blocks WEB
             * enhanced theme on midCustId
             */
            if (StringUtils.equals(WEB.getValue(), channelId)) {
                if (isWebEnhancedCashierEnabledOnMerchant) {
                    isEnhancedCashierPage = true;
                }
                if (enhancedCashierPageServiceHelper.isMidCustIdAllowedForWEBEnhanced(paymentRequestData)) {
                    isEnhancedCashierPage = true;
                }
                if (enhancedCashierPageServiceHelper.isMidCustIdBlockedForWEBEnhanced(paymentRequestData)) {
                    isEnhancedCashierPage = false;
                }
            }

            /*
             * This condition is for WAP calls
             */
            if (isWapEnhancedCashierEnabledOnMerchant && StringUtils.equals(WAP.getValue(), channelId)
                    && enhancedCashierPageServiceHelper.isMidCustIdAllowedForWAPEnhanced(paymentRequestData)) {
                isEnhancedCashierPage = true;
            }
        }

        // check for not allowed request-types
        if (isRequestTypeNotAllowedForEnhance(paymentRequestData.getRequestType())
                || (("S2S".equalsIgnoreCase(paymentRequestData.getConnectiontype())) && ERequestType.SUBSCRIBE
                        .getType().equalsIgnoreCase(paymentRequestData.getRequestType()))) {
            isEnhancedCashierPage = false;
        }

        if (paymentRequestData.isRiskVerifiedEnhanceFlow()) {
            isEnhancedCashierPage = false;
        }

        /*
         * "isEnhancedCashierPage" decides if enhanced-cashier page is to be
         * opened or not
         */
        if (isEnhancedCashierPage) {
            paymentRequestData.setEnhancedCashierPageRequest(true);
            paymentRequestData.setChannelId(channelId);
        }

        return isEnhancedCashierPage;
    }

    private String getChannelId(HttpServletRequest request, UserAgentInfo userAgentInfo) {
        if (userAgentInfo != null) {
            if (StringUtils.isBlank(userAgentInfo.getUserAgent())) {
                LOGGER.info("Returning WAP since user-agent is empty");
                return WAP.getValue();
            }
        }
        return theiaSessionDataService.getChannel(request, false);
    }

    private boolean isEnhancedCashierHtmlTemplatePresent() {
        return (StringUtils.isNotBlank(ConfigurationUtil.getHtmlCashierTemplate())
                || StringUtils.isNotBlank(ConfigurationUtil.getWEBHtmlEnhancedCashierTheme()) || StringUtils
                    .isNotBlank(ConfigurationUtil.getWAPHtmlEnhancedCashierTheme()));
    }

    private boolean isRequestTypeAllowedForEnhance(String requestType) {
        String allowedRequestTypes = ConfigurationUtil.getProperty(
                TheiaConstant.PaytmPropertyConstants.ALLOWED_REQUEST_TYPES_FOR_ENHANCED_NATIVE, "");
        if (StringUtils.isNotEmpty(allowedRequestTypes) && allowedRequestTypes.indexOf(requestType) != -1) {
            return true;
        }
        return false;
    }

    private boolean isRequestTypeNotAllowedForEnhance(String requestType) {
        String notAllowedRequestTypes = ConfigurationUtil.getProperty(
                TheiaConstant.PaytmPropertyConstants.NOT_ALLOWED_REQUEST_TYPES_FOR_ENHANCED_NATIVE, "");
        if (StringUtils.isNotEmpty(notAllowedRequestTypes) && notAllowedRequestTypes.indexOf(requestType) != -1) {
            return true;
        }
        return false;
    }

    public boolean isEnhancedCashierFlowForLinkBased(PaymentRequestBean paymentRequestData) {
        if ((TheiaConstant.RequestTypes.LINK_BASED_PAYMENT.equalsIgnoreCase(paymentRequestData.getRequestType()) || TheiaConstant.RequestTypes.LINK_BASED_PAYMENT_INVOICE
                .equalsIgnoreCase(paymentRequestData.getRequestType()))
                && StringUtils.isBlank(paymentRequestData.getPaymentTypeId())) {
            return true;
        }
        return false;
    }

    public boolean allowForNativeAppInvokeFlow(PaymentRequestBean paymentRequestData, HttpServletRequest request) {
        String txnToken = paymentRequestData.getTxnToken();
        if (StringUtils.isNotBlank(txnToken)
                && TheiaConstant.RequestTypes.DEFAULT.equalsIgnoreCase(paymentRequestData.getRequestType())) {
            if (request != null) {
                setMidOrderIdNativeAppInvokeFlow(paymentRequestData, request);
            }
            return true;
        }
        return false;
    }

    private void setMidOrderIdNativeAppInvokeFlow(PaymentRequestBean paymentRequestBean, HttpServletRequest request) {
        /*
         * request type is "DEFAULT" for enhancedNative if cashierPage is to be
         * rendered
         */
        if (paymentRequestBean.getRequestType().equals(TheiaConstant.RequestTypes.DEFAULT)) {
            if (StringUtils.isBlank(paymentRequestBean.getMid())) {
                paymentRequestBean.setMid(request.getParameter(TheiaConstant.RequestParams.Native.MID));
            }
            if (StringUtils.isBlank(paymentRequestBean.getOrderId())) {
                paymentRequestBean.setOrderId(request.getParameter(TheiaConstant.RequestParams.Native.ORDER_ID));
            }
        }
    }

    public void checkAndSetIfNativeSubscriptionFlow(PaymentRequestBean paymentRequestData, HttpServletRequest request)
            throws Exception {
        if (ERequestType.isSubscriptionCreationRequest(paymentRequestData.getRequestType())) {
            disableCcDcInSubscription(paymentRequestData);
            paymentRequestData.setSubscription(true);
            // Fix to check if subs_id is same as created in Subscription create
            // API.
            StringBuilder key = new StringBuilder(paymentRequestData.getRequestType()).append(paymentRequestData
                    .getTxnToken());

            String subscriptionId = "";
            boolean isAoa = aoaUtils.isAOAMerchant(paymentRequestData.getMid());
            if (isAoa) {
                LOGGER.error("AOA subscription client call is being used");
                // AoaSubscriptionCreateResponse aoaSubscriptionResponse =
                // (AoaSubscriptionCreateResponse)
                // theiaTransactionalRedisUtil.get(
                // key.toString());
                // if(aoaSubscriptionResponse != null)
                // subscriptionId = aoaSubscriptionResponse.getSubscriptionId();
                //

            } else {
                SubscriptionResponse subscriptionResponse = (SubscriptionResponse) theiaTransactionalRedisUtil.get(key
                        .toString());
                if (subscriptionResponse != null)
                    subscriptionId = subscriptionResponse.getSubscriptionId();
            }

            if (!subscriptionId.equals(paymentRequestData.getSubscriptionID())) {
                throw new PaytmValidationException("Invalid Subscription Id for the session.");
            }
            //

            SubscriptionTransactionRequestBody subsOrderDetail = (SubscriptionTransactionRequestBody) request
                    .getAttribute("orderDetail");
            String subscriptionPaymentMode = null;
            if (null != subsOrderDetail) {
                subscriptionPaymentMode = subsOrderDetail.getSubscriptionPaymentMode();
                paymentRequestData.setSubscriptionMaxAmount(subsOrderDetail.getSubscriptionMaxAmount());
                if (!SubsPaymentMode.UNKNOWN.name().equals(subscriptionPaymentMode)) {
                    paymentRequestData.setSubsPaymentMode(subscriptionPaymentMode);
                    paymentRequestData.setSubsPPIOnly(subsOrderDetail.getSubsPPIOnly());
                } else {
                    String paymentMode = request.getParameter(TheiaConstant.RequestParams.Native.PAYMENT_MODE);
                    if (EPayMethod.UPI_INTENT.getMethod().equals(paymentMode)) {
                        paymentMode = PayMethod.UPI.getMethod();
                    }
                    PayMethod payMethod = PayMethod.getPayMethodByMethod(paymentMode);
                    if ("1".equals(paymentRequestData.isAddMoney())) {
                        payMethod = PayMethod.BALANCE;
                    }
                    if (payMethod != null) {
                        paymentRequestData.setSubsPaymentMode(payMethod.getOldName());
                        if (payMethod.equals(PayMethod.BALANCE)) {
                            paymentRequestData.setSubsPPIOnly("Y");
                        }
                    }
                }
                paymentRequestData.setTxnAmount(subsOrderDetail.getTxnAmount().getValue());

                if (isAoa && SubsPaymentMode.BANK_MANDATE.name().equals(subscriptionPaymentMode)) {
                    if (StringUtils.isBlank(paymentRequestData.getMandateType()))
                        paymentRequestData.setMandateType(request.getParameter(MANDATE_TYPE));
                }
            }
        }
    }

    private void disableCcDcInSubscription(PaymentRequestBean paymentRequestData) {
        if (TheiaConstant.RequestTypes.NATIVE_SUBSCRIPTION.equals(paymentRequestData.getRequestType())
                && (TheiaConstant.ExtraConstants.CC.equals(paymentRequestData.getPaymentMode()) || TheiaConstant.ExtraConstants.DC
                        .equals(paymentRequestData.getPaymentMode()))) {
            String txnToken = paymentRequestData.getTxnToken();
            if (StringUtils.isNotBlank(txnToken)) {
                InitiateTransactionRequestBody orderDetail = nativeSessionUtil.getOrderDetail(txnToken);
                List<PaymentMode> disablePaymentMode = orderDetail.getDisablePaymentMode();
                if (disablePaymentMode != null) {
                    disablePaymentMode.stream().forEach(
                            paymentMode -> {
                                if (paymentMode.equals(paymentRequestData.getPaymentMode())) {
                                    throw new NativeFlowException.ExceptionBuilder(ResultCode.MAXIMUM_GRACE_DAYS
                                            .getCode(), (String.format(ResultCode.MAXIMUM_GRACE_DAYS.getResultMsg(),
                                            NativeSubscriptionHelperImpl.SUBSCRIPTION_MAXIMUM_GRACE_DAYS_CREDIT_CARD,
                                            NativeSubscriptionHelperImpl.SUBSCRIPTION_MAXIMUM_GRACE_DAYS_DEBIT_CARD)),
                                            ResultCode.MAXIMUM_GRACE_DAYS.getResultStatus()).isHTMLResponse(false)
                                            .isNativeJsonRequest(true).build();
                                }
                            });
                }
            }
        }
    }

    public void removeTagLineForOffline(PaymentRequestBean paymentRequestData) {
        try {
            if (paymentRequestData.getRequestType().equals(ERequestType.OFFLINE.getType())
                    || BizRequestResponseMapperImpl.isQRCodeRequest(paymentRequestData)) {
                String additionalInfo = paymentRequestData.getAdditionalInfo();
                if (StringUtils.isNotBlank(additionalInfo)) {
                    int indexOfTagLine = additionalInfo.indexOf("tagLine");
                    int indexOfPipe = additionalInfo.indexOf("|", indexOfTagLine);
                    int finalIndexOfTagLine = indexOfTagLine == 0 ? 0 : indexOfTagLine;
                    String finalAdditionalInfo = null;
                    if (indexOfTagLine == -1) {
                        finalAdditionalInfo = additionalInfo;
                    } else if (indexOfPipe == -1) {
                        finalAdditionalInfo = additionalInfo.substring(0, (finalIndexOfTagLine - 1));
                    } else {
                        finalAdditionalInfo = (additionalInfo.substring(0, (finalIndexOfTagLine))).trim()
                                + (additionalInfo.substring((indexOfPipe + 1), additionalInfo.length()));
                    }
                    if (null != finalAdditionalInfo)
                        paymentRequestData.setAdditionalInfo(finalAdditionalInfo);
                }
            }
        } catch (Exception e) {
            LOGGER.error("error occured while removing tagLine from additionalInfo {}:", e.getMessage());
        }
    }

    private boolean oAuthRedirect(HttpServletRequest request, PaymentRequestBean paymentRequestBean) {
        if (StringUtils.equals(request.getParameter("oauth"), "true")) {
            String redisKey = nativeSessionUtil.getMidOrderIdKeyForRedis(paymentRequestBean.getMid(),
                    paymentRequestBean.getOrderId());

            Object obj = nativeSessionUtil.getKey(redisKey);
            if (obj == null || StringUtils.isBlank((String) obj)) {
                return true;
            }
        }
        return false;
    }

    public String getAdditionalInfoRequestTypeForSoundBox(String additionalInfo) {

        String resultantAdditionalInfo = additionalInfo;
        Map<String, String> additionalInfoMapFromString = AdditionalInfoUtil
                .generateMapFromAdditionalInfoString(additionalInfo);
        if (null == additionalInfoMapFromString) {
            return resultantAdditionalInfo;
        }
        String mode = additionalInfoMapFromString.get(MODE);
        // If mode is present either in String or in Map && it is
        // 'mobile_number'
        if (!StringUtils.equals(additionalInfoMapFromString.get(REQUEST_TYPE), QR_MERCHANT)
                && StringUtils.equals(mode, MOBILE_NUMBER)) {
            LOGGER.info("SoundBox: mode is 'mobile_number'");
            // Setting REQUEST_TYPE as QR_MERCHANT ONLY in additionalInfo & not
            // in paymentRequestData.AdditoinalInfoMap
            if (StringUtils.isNotBlank(additionalInfo)) {
                resultantAdditionalInfo = additionalInfo.replaceAll("REQUEST_TYPE[^|]*", "REQUEST_TYPE:QR_MERCHANT");
                LOGGER.info("SoundBox: REQUEST_TYPE is set to QR_MERCHANT - in additionalInfo String");
            }
            if (!StringUtils.contains(additionalInfo, QR_MERCHANT)) {
                resultantAdditionalInfo = additionalInfo + "|REQUEST_TYPE:QR_MERCHANT";
            }
        }
        return resultantAdditionalInfo;
    }

    public void setAdditionalInfoRequestTypeForSoundBox(PaymentRequestBean paymentRequestData) {

        String requestType = paymentRequestData.getRequestType();
        if (!(StringUtils.equals(requestType, ERequestType.OFFLINE.getType()) || StringUtils.equals(requestType,
                ERequestType.NATIVE.getType()))) {
            return;
        }
        String resultantAdditionalInfo = getAdditionalInfoRequestTypeForSoundBox(paymentRequestData.getAdditionalInfo());

        paymentRequestData.setAdditionalInfo(resultantAdditionalInfo);
    }

    public void checkAndSetIfScanAndPayFlow(PaymentRequestBean paymentRequestData) {
        String requestType = paymentRequestData.getRequestType();
        if (!(StringUtils.equals(requestType, ERequestType.DYNAMIC_QR.getType()) && !(StringUtils.equals(requestType,
                ERequestType.DYNAMIC_QR_2FA.getType())))) {
            if (BizRequestResponseMapperImpl.isQRCodeRequest(paymentRequestData)) {
                paymentRequestData.setScanAndPayFlow(true);
            }
        }
        if (StringUtils.isNotBlank(paymentRequestData.getTxnToken()))
            nativeSessionUtil
                    .setScanAndPayFlag(paymentRequestData.getTxnToken(), paymentRequestData.isScanAndPayFlow());
    }

    public boolean checkIfAutoAppInvokeAllowed(PaymentRequestBean requestData,
            com.paytm.pgplus.theia.models.UserAgentInfo userAgentInfo, boolean checkBrowserCompat,
            boolean appInvokeV2ff4j, boolean isSdkProcessTxn) {

        if (!ff4JUtil.isFeatureEnabledOnCustId(AUTO_APP_INVOKE, requestData.getCustId())) {
            LOGGER.info("AUTO_APP_INVOKE preference not enabled , autoAppInvoke disabled");
            return false;
        }

        if (appInvokeV2ff4j) {

            if (!merchantPreferenceService.isAutoAppInvokeAllowed(requestData.getMid())
                    || !(StringUtils.equals(ERequestType.DEFAULT.getType(), requestData.getRequestType())
                            || StringUtils.equals(ERequestType.SUBSCRIBE.getType(), requestData.getRequestType())
                            || StringUtils.equals(ERequestType.NATIVE_SUBSCRIPTION.getType(),
                                    requestData.getRequestType())
                            || StringUtils.equals(ERequestType.NATIVE_MF_SIP.getType(), requestData.getRequestType()) || StringUtils
                                .equalsIgnoreCase("Payment", requestData.getRequestType()))) {
                LOGGER.info("Merchant preference not enabled or request type {}", requestData.getRequestType());
                return false;
            }

            // incase of sdk transaction check PCF category merchant. i.e. to
            // disable app invoke for pcf sdk transaction
            if (isSdkProcessTxn && checkPcfCategoryMerchant(requestData)) {
                LOGGER.info("Merchant has PCF applicable , autoAppInvoke disabled");
                return false;
            }

        } else {

            if (!merchantPreferenceService.isAutoAppInvokeAllowed(requestData.getMid())
                    || !(StringUtils.equals(ERequestType.DEFAULT.getType(), requestData.getRequestType()) || StringUtils
                            .equalsIgnoreCase("Payment", requestData.getRequestType()))) {
                LOGGER.info("Merchant preference not enabled or request type {}", requestData.getRequestType());
                return false;
            }

            if (checkPcfCategoryMerchant(requestData)) {
                LOGGER.info("Merchant has PCF applicable , autoAppInvoke disabled");
                return false;
            }

        }

        // App invoke will be handled on ui, explicit check is not required, as
        // ui is sending WEB only.
        // if (!StringUtils.equals(Channel.WAP.getName(),
        // requestData.getChannelId())) {
        // LOGGER.info("Channel is not WAP , autoAppInvoke disabled");
        // return false;
        // }

        if (workFlowRequestCreationHelper.isAddMoneyToWallet(requestData.getMid(), requestData.getRequestType())) {
            LOGGER.info("Add money txn , autoAppInvoke disabled");
            return false;
        }

        if (checkBrowserCompat && !BrowserUtil.isBrowserSupported(userAgentInfo)) {
            LOGGER.info("Browser not supported , autoAppInvoke disabled");
            return false;
        }

        LOGGER.debug("Auto App Invoke allowed is true");
        return true;
    }

    private boolean checkPcfCategoryMerchant(PaymentRequestBean requestData) {
        return merchantPreferenceProvider.isSlabBasedMDREnabled(requestData)
                || merchantPreferenceProvider.isDynamicFeeMerchant(requestData)
                || merchantPreferenceProvider.isPostConvenienceFeesEnabled(requestData);
    }

    public void deleteRedisKeyForCancelledTxns(HttpServletRequest request) {
        if (request != null && TXN_CANCEL.equalsIgnoreCase(request.getParameter("STATUS"))) {
            String mid = request.getParameter(MID);
            String orderid = request.getParameter(ORDERID);
            if (orderid == null) {
                orderid = request.getParameter(ORDER_ID);
            }
            String txnToken = nativeSessionUtil.getTxnToken(mid, orderid);
            String midOrderIdKey = nativeSessionUtil.getMidOrderIdKeyForRedis(mid, orderid);
            if (txnToken != null)
                nativeSessionUtil.deleteKey(txnToken, midOrderIdKey);
        }
    }
}
