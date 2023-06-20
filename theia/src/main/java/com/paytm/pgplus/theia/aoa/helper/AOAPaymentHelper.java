package com.paytm.pgplus.theia.aoa.helper;

import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cache.model.MerchantProfile;
import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.checksum.crypto.nativ.NativePaymentDetailsCryptoFactory;
import com.paytm.pgplus.checksum.crypto.nativ.enums.NativeCryptoType;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.model.CardTokenInfo;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.enums.PreferredOtpPage;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.utils.BinHelper;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.pgproxycommon.utils.EcomTokenUtils;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.request.SubscriptionTransactionRequestBody;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import com.paytm.pgplus.theia.aoa.model.orderpay.CreateOrderAndPaymentRequest;
import com.paytm.pgplus.theia.aoa.model.orderpay.CreateOrderAndPaymentRequestBody;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.models.NativeJsonRequestBody;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.nativ.enums.AuthMode;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.payment.request.NativePaymentRequestBody;
import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.helper.RetryServiceHelper;
import com.paytm.pgplus.theia.services.impl.SeamlessPaymentServiceImpl;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import com.paytm.pgplus.theia.utils.EventUtils;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import com.paytm.pgplus.theia.utils.ProcessTransactionUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.paytm.pgplus.pgproxycommon.enums.ResponseConstants.RISK_REJECT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;

@Service
public class AOAPaymentHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(AOAPaymentHelper.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(AOAPaymentHelper.class);
    private static final String MAESTRO_CVV = "|123";
    private static final String BAJAJFN_CVV = "|1111";
    private static final String MAESTRO_EXPIRY = "|122049";
    private static final String MAESTRO = "MAESTRO";

    private static final String ADDITIONAL_INFO_DELIMITER = "|";
    private static final int ETM_ENCRYPTION_CONSTANT = 7;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier(value = "seamlessPaymentService")
    private SeamlessPaymentServiceImpl seamlessPaymentService;

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    private IMerchantDataService merchantDataService;

    @Autowired
    BinHelper binHelper;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private AOAUtils aoaUtils;

    @Autowired
    private EcomTokenUtils ecomTokenUtils;

    @Autowired
    MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    private NativePaymentDetailsCryptoFactory nativePaymentDetailsCryptoFactory;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    private ProcessTransactionUtil processTransactionUtil;

    @Autowired
    private RetryServiceHelper retryServiceHelper;

    public String processNativeJsonRequest(CreateOrderAndPaymentRequest request, String txnToken, String subsId) {
        PaymentRequestBean paymentRequestData = null;
        try {
            paymentRequestData = getPaymentRequestBean(request, txnToken, subsId);
        } catch (final Exception e) {
            LOGGER.error("SYSTEM_ERROR : ", e);
        }
        enrichPaymentRequestBean(request, paymentRequestData);
        LOGGER.info("PaymentRequestBean received : {}", paymentRequestData);
        setCheckoutJsConfigDataInRedisForWebView(request, paymentRequestData);
        PageDetailsResponse pageDetailsResponse = seamlessPaymentService
                .processNativeJsonRequestSeamlessWorkflow(paymentRequestData);
        if (StringUtils.isNotBlank(pageDetailsResponse.getS2sResponse())
                && pageDetailsResponse.isSuccessfullyProcessed()) {
            return pageDetailsResponse.getS2sResponse();
        } else {
            throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isHTMLResponse(false)
                    .isRedirectEnhanceFlow(false).build();
        }
    }

    private void enrichPaymentRequestBean(CreateOrderAndPaymentRequest request, PaymentRequestBean paymentRequestData) {
        String txnToken = paymentRequestData.getTxnToken();
        checkPaymentCountBreached(txnToken);
        checkIfMerchantBlocked(request);

        final long startTime = System.currentTimeMillis();
        PageDetailsResponse pageResponse = null;

        // Below code for native subscription payment support.
        try {
            checkAndSetIfNativeSubscriptionFlow(paymentRequestData, request);
        } catch (Exception e) {
            HttpServletRequest servletRequest = EnvInfoUtil.httpServletRequest();
            servletRequest.setAttribute(TheiaConstant.RequestParams.NATIVE_SUBSCRIPTION_RESULT_ERROR_MESSAGE,
                    e.getMessage());
            throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isHTMLResponse(false)
                    .setMsg(e.getMessage()).build();
        }
    }

    public void checkAndSetIfNativeSubscriptionFlow(PaymentRequestBean paymentRequestData,
            CreateOrderAndPaymentRequest request) throws Exception {
        // TODO: need to check redundancy in this code snippet
        if (ERequestType.isSubscriptionCreationRequest(paymentRequestData.getRequestType())) {
            paymentRequestData.setSubscription(true);
            if (StringUtils.isNotBlank(request.getBody().getSgwReferenceId())
                    && !aoaUtils.isAOAMerchant(request.getBody().getMid())) {
                paymentRequestData.setSubsAoaPgMidTxn(true);
            }
            SubscriptionTransactionRequestBody subsOrderDetail = request.getBody().getSubscriptionDetails();
            if (null != subsOrderDetail) {
                paymentRequestData.setSubscriptionMaxAmount(subsOrderDetail.getSubscriptionMaxAmount());
                paymentRequestData.setTxnAmount(subsOrderDetail.getTxnAmount().getValue());
            }
        }
    }

    void checkIfMerchantBlocked(final CreateOrderAndPaymentRequest request) throws NativeFlowException {
        if (isBlockedMerchant(request)) {
            LOGGER.error("Merchant is either blocked or Inactive");
            throw new NativeFlowException.ExceptionBuilder(ResponseConstants.MERCHANT_BLOCKED).build();
        }
    }

    private boolean isBlockedMerchant(CreateOrderAndPaymentRequest request) {
        if (StringUtils.isNotBlank(request.getBody().getMid())) {
            String mid = request.getBody().getMid();
            return merchantExtendInfoUtils.isMerchantActiveOrBlocked(mid);
        }
        return false;
    }

    public void checkPaymentCountBreached(String txnToken) throws NativeFlowException {
        if (isMaxPaymentRetryBreached(txnToken)) {
            throw new NativeFlowException.ExceptionBuilder(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED)
                    .isHTMLResponse(false).isRetryAllowed(false)
                    .setMsg(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED.getMessage()).build();
        }
    }

    public boolean isMaxPaymentRetryBreached(String txntoken) {
        try {
            int maxPaymentCount = Integer.valueOf(ConfigurationUtil.getProperty(MAX_PAYMENT_COUNT, "10"));
            if (StringUtils.isNotBlank(txntoken)) {
                Integer totalPaymentCount = nativeSessionUtil.getTotalPaymenCount(txntoken);
                if (totalPaymentCount == null) {
                    totalPaymentCount = 0;
                }
                totalPaymentCount = totalPaymentCount + 1;
                if (totalPaymentCount > maxPaymentCount) {
                    return true;
                }
                nativeSessionUtil.setTotalPaymentCount(txntoken, totalPaymentCount);
            }
        } catch (Exception e) {
            LOGGER.warn("something went wrong while checking isMaxPaymentRetryBreached");
        }
        return false;
    }

    private PaymentRequestBean getPaymentRequestBean(CreateOrderAndPaymentRequest request, String txnToken,
            String subsId) throws Exception {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        NativeJsonRequestBody paymentDetails = request.getBody().getPaymentDetails();
        InitiateTransactionRequestBody orderDetail = null;

        boolean isNativeJsonRequest = true;
        String mid = request.getBody().getMid();
        String orderId = request.getBody().getOrderId();

        paymentRequestBean.setMid(mid);
        paymentRequestBean.setOrderId(orderId);
        paymentRequestBean.setTxnToken(txnToken);
        paymentRequestBean.setSubscriptionID(subsId);
        paymentRequestBean.setCoftConsent(paymentDetails.getCoftConsent());
        paymentRequestBean.setRiskExtendedInfo(paymentDetails.getRiskExtendInfo());
        paymentRequestBean.setRequestType(request.getBody().getRequestType());
        paymentRequestBean.setRequest(request.getBody().getRequest());
        paymentRequestBean.setNativeJsonRequest(isNativeJsonRequest);

        if (request.getBody().getSubscriptionDetails() != null) {
            orderDetail = request.getBody().getSubscriptionDetails();
            if (orderDetail.isAutoRefund()
                    && Double.parseDouble(orderDetail.getTxnAmount().getValue()) <= Double
                            .parseDouble(ConfigurationUtil.getProperty("max.autoRefund.amount.for.aoa.subs", "1"))) {
                paymentRequestBean.setAutoRefund(true);
            }
        } else {
            orderDetail = request.getBody().getOrderDetails();
        }

        String flowType = EventNameEnum.ONLINE_NATIVEPLUS_PAYMENT_REQUEST.getEventName();
        nativeSessionUtil.setFlowType(txnToken, flowType);

        paymentRequestBean.setWorkflow(request.getHead().getWorkFlow());
        paymentRequestBean.setPeonURL(orderDetail.getPEON_URL());
        if (orderDetail.getOrderPricingInfo() != null) {
            paymentRequestBean.setOrderPricingInfo(orderDetail.getOrderPricingInfo());
        }

        if (orderDetail.getSplitSettlementInfoData() != null) {
            paymentRequestBean.setSplitSettlementInfo(orderDetail.getSplitSettlementInfo());
        }

        String channelID = getChannelId(request);
        paymentRequestBean.setChannelId(channelID);

        setNativeTxnInProcessFlagInCache(txnToken);

        String paymentMode = paymentDetails.getPaymentMode();
        paymentRequestBean.setPaymentMode(paymentMode);
        EventUtils.pushTheiaEvents(EventNameEnum.REQUEST_WITH_PAYMENT_MODE, new ImmutablePair<>("PAYMENT_MODE",
                paymentMode));

        if (paymentDetails.getCoftConsent() != null) {
            if (paymentDetails.getCoftConsent().getUserConsent() == null
                    || (paymentDetails.getCoftConsent().getUserConsent() != 1 && paymentDetails.getCoftConsent()
                            .getUserConsent() != 0)) {
                LOGGER.error("Invalid coftConsentInfo : {}", paymentDetails.getCoftConsent());
                throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                        .isRetryAllowed(true)
                        .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
            }
            paymentRequestBean.setCoftConsent(paymentDetails.getCoftConsent());
        }

        String paymentInfo = null;

        if (EPayMethod.DEBIT_CARD.getMethod().equalsIgnoreCase(paymentMode)
                || EPayMethod.CREDIT_CARD.getMethod().equalsIgnoreCase(paymentMode)) {
            String cardInfo = paymentDetails.getCardInfo();

            String requiredCardDetails = "";
            CardTokenInfo cardTokenInfoObj = paymentDetails.getCardTokenInfo();

            if (cardTokenInfoObj != null) {
                // Added this to supported encryped card info for payments
                if (merchantPreferenceService.isEncryptedCardMerchant(mid) && isEncryptedCardDetails(cardInfo)) {
                    cardInfo = processTransactionUtil.getDecryptedCardInfo(cardInfo.trim());
                }

                String[] cardDetails = cardInfo.split(Pattern.quote("|"), -1);
                if (cardDetails.length != 4 && cardDetails[2].trim().length() == 0) {
                    LOGGER.error("Invalid cardDetails length or CVV is missing: {}", cardDetails.length);
                    throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                            .isRetryAllowed(true)
                            .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
                }
                requiredCardDetails = parseCVV(cardDetails[2]);
                paymentRequestBean.setPaymentDetails(requiredCardDetails);
                paymentRequestBean.setCardTokenInfo(cardTokenInfoObj);
                paymentRequestBean.setCoftTokenTxn(true);

            } else {

                // Added this to supported encryped card info for payments
                if (merchantPreferenceService.isEncryptedCardMerchant(mid) && isEncryptedCardDetails(cardInfo)) {
                    cardInfo = processTransactionUtil.getDecryptedCardInfo(cardInfo.trim());
                }

                String[] cardDetails = cardInfo.split(Pattern.quote("|"), -1);
                if (cardDetails.length != 4) {
                    LOGGER.error("Invalid cardDetails length: {}", cardDetails.length);
                    throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                            .isRetryAllowed(true)
                            .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
                }

                String cvv = parseCVV(cardDetails[2]);
                cardDetails[2] = cvv;
                if (cardDetails[0].trim().length() != 0) {
                    if (cardDetails[2].trim().length() != 0) {
                        requiredCardDetails = cardDetails[0].trim() + "|" + cardDetails[2].trim();
                        if (cardDetails[0].trim().length() > 15 && cardDetails[0].trim().length() < 45) {
                            paymentRequestBean.setCoftTokenTxn(true);
                        }
                    } else {
                        requiredCardDetails = cardDetails[0].trim() + MAESTRO_CVV;
                    }

                } else if (cardDetails[1].trim().length() != 0
                        && (isMaestroCardScheme(cardDetails[1].trim()) || isBajajCardScheme(cardDetails[1].trim()))) {
                    requiredCardDetails = cardDetails[1].trim() + MAESTRO_CVV + getExpiry(paymentDetails, cardDetails);
                } else if (cardDetails[1].trim().length() != 0 && isBajajFnScheme(cardDetails[1].trim())) {
                    requiredCardDetails = cardDetails[1].trim() + BAJAJFN_CVV + "|" + cardDetails[3].trim();
                } else {
                    requiredCardDetails = cardDetails[1].trim() + "|" + cardDetails[2].trim() + "|"
                            + cardDetails[3].trim();
                }

                if (cardDetails[1].trim().length() != 0) {
                    paymentInfo = requiredCardDetails;
                }

                /*
                 * update paymode as the bin cardtype, this is for cardnumbers,
                 * not savedCardId
                 */
                if ((EPayMethod.DEBIT_CARD.getMethod().equalsIgnoreCase(paymentMode) || EPayMethod.CREDIT_CARD
                        .getMethod().equalsIgnoreCase(paymentMode)) && cardDetails[1].trim().length() != 0) {
                    String cardType = getPaymodeForCardBin(requiredCardDetails);
                    if (StringUtils.isNotBlank(cardType)) {
                        EPayMethod payMeth = EPayMethod.getPayMethodByMethod(cardType);
                        if (payMeth != null) {
                            paymentMode = payMeth.getMethod();
                        }
                    }
                }
                paymentRequestBean.setPaymentDetails(requiredCardDetails);
            }
        }
        if (StringUtils.isNotBlank(paymentDetails.getAuthMode())
                && (EPayMethod.DEBIT_CARD.getMethod().equalsIgnoreCase(paymentMode))) {
            String authMode = paymentDetails.getAuthMode();
            if (AuthMode.OTP.getType().equals(authMode)) {
                paymentRequestBean.setiDebitOption("false");
            } else if (AuthMode.PIN.getType().equals(authMode)) {
                paymentRequestBean.setiDebitOption("true");
            } else {
                LOGGER.error("Invalid authMode: {}", authMode);
                throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                        .isRetryAllowed(true)
                        .setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg()).build();
            }
        }

        if (EPayMethod.DEBIT_CARD.getMethod().equalsIgnoreCase(paymentMode)) {
            paymentMode = PaymentTypeIdEnum.DC.value;
        } else if (EPayMethod.CREDIT_CARD.getMethod().equalsIgnoreCase(paymentMode)) {
            paymentMode = PaymentTypeIdEnum.CC.value;
        }
        paymentRequestBean.setPaymentMode(paymentMode);

        String saveForFuture = paymentDetails.getStoreInstrument();
        if ("1".equals(saveForFuture)) {
            paymentRequestBean.setStoreCard("1");
        }
        String industryTypeId = getIndustryTypeId(request.getBody().getMid());
        paymentRequestBean.setIndustryTypeId(industryTypeId);
        paymentRequestBean.setAuthMode("3D");

        if (ERequestType.isSubscriptionRequest(request.getBody().getRequestType())) {
            paymentRequestBean.setSubsPaymentMode(paymentRequestBean.getPaymentMode());
            StringBuilder key = new StringBuilder(orderDetail.getRequestType()).append(txnToken);
            if (!aoaUtils.isAOAMerchant(request.getBody().getMid())) {
                SubscriptionResponse subscriptionResponse = (SubscriptionResponse) theiaTransactionalRedisUtil.get(key
                        .toString());
                if (subscriptionResponse != null) {
                    if (StringUtils.isNotBlank(subscriptionResponse.getPaymentMid())
                            && StringUtils.isNotBlank(subscriptionResponse.getPaymentOrderId())) {
                        paymentRequestBean.setPaymentMid(subscriptionResponse.getPaymentMid());
                        paymentRequestBean.setPaymentOrderId(subscriptionResponse.getOrderId());
                        paymentRequestBean.setAutoRefund(true);
                    }
                }
            }

            SubscriptionTransactionRequestBody subscriptionTransactionRequestBody = (SubscriptionTransactionRequestBody) orderDetail;
            if (subscriptionTransactionRequestBody != null) {
                paymentRequestBean.setSubscriptionFrequencyUnit(subscriptionTransactionRequestBody
                        .getSubscriptionFrequencyUnit());
                paymentRequestBean.setSubscriptionExpiryDate(subscriptionTransactionRequestBody
                        .getSubscriptionExpiryDate());
                paymentRequestBean.setSubscriptionStartDate(subscriptionTransactionRequestBody
                        .getSubscriptionStartDate());
                paymentRequestBean.setSubscriptionGraceDays(subscriptionTransactionRequestBody
                        .getSubscriptionGraceDays());
                paymentRequestBean.setValidateAccountNumber(subscriptionTransactionRequestBody
                        .getValidateAccountNumber());
                paymentRequestBean.setAccountNumber(subscriptionTransactionRequestBody.getAccountNumber());
                paymentRequestBean.setAllowUnverifiedAccount(subscriptionTransactionRequestBody
                        .getAllowUnverifiedAccount());
                paymentRequestBean.setSubscriptionFrequency(subscriptionTransactionRequestBody
                        .getSubscriptionFrequency());
                paymentRequestBean.setSubscriptionEnableRetry(subscriptionTransactionRequestBody
                        .getSubscriptionEnableRetry());
                paymentRequestBean.setFlexiSubscription(subscriptionTransactionRequestBody.isFlexiSubscription());
            }
            if (StringUtils.isNotBlank(subscriptionTransactionRequestBody.getSubscriptionPaymentMode())
                    && subscriptionTransactionRequestBody.getSubscriptionPaymentMode().equals("UPI")
                    && StringUtils.isNotBlank(request.getBody().getPaymentDetails().getPayerAccount())) {
                validateUpiSubsTxnAmount(request.getBody());
                paymentRequestBean.setPaymentDetails(request.getBody().getPaymentDetails().getPayerAccount());
            }

        }

        paymentRequestBean.setTokenType(TheiaConstant.RequestParams.Native.TXN_TOKEN);
        paymentRequestBean.setTxnAmount(orderDetail.getTxnAmount().getValue());
        paymentRequestBean.setSsoToken(orderDetail.getPaytmSsoToken());
        paymentRequestBean.setCallbackUrl(orderDetail.getCallbackUrl());
        paymentRequestBean.setWebsite(orderDetail.getWebsiteName());
        paymentRequestBean.setPaymentTypeId(paymentMode);
        paymentRequestBean.setDeviceId(paymentDetails.getDeviceId());

        if (orderDetail.getExtendInfo() != null) {
            paymentRequestBean.setMerchUniqueReference(orderDetail.getExtendInfo().getMercUnqRef());
            paymentRequestBean.setUdf1(orderDetail.getExtendInfo().getUdf1());
            paymentRequestBean.setUdf2(orderDetail.getExtendInfo().getUdf2());
            paymentRequestBean.setUdf3(orderDetail.getExtendInfo().getUdf3());
            paymentRequestBean.setSubwalletAmount(orderDetail.getExtendInfo().getSubwalletAmount());
            paymentRequestBean.setAdditionalInfo(orderDetail.getExtendInfo().getComments());
            if (StringUtils.isNotBlank(orderDetail.getExtendInfo().getAmountToBeRefunded())) {
                paymentRequestBean.setAmountToBeRefunded(orderDetail.getExtendInfo().getAmountToBeRefunded());
            }
        }

        if (null != paymentDetails.getAdditionalInfo()) {
            paymentRequestBean.setUdf1(paymentDetails.getAdditionalInfo().get("UDF_1"));
        }

        if (orderDetail.getUserInfo() != null) {
            paymentRequestBean.setCustId(orderDetail.getUserInfo().getCustId());
            paymentRequestBean.setMobileNo(orderDetail.getUserInfo().getMobile());
            paymentRequestBean.setAddress1(orderDetail.getUserInfo().getAddress());
            paymentRequestBean.setPincode(orderDetail.getUserInfo().getPincode());

        }
        if (null != orderDetail.getShippingInfo()) {
            paymentRequestBean.setShippingInfo(JsonMapper.mapObjectToJson(orderDetail.getShippingInfo()));
        }

        if (null != orderDetail.getExtendInfo()) {
            paymentRequestBean.setExtendInfo(orderDetail.getExtendInfo());
        }

        if (!StringUtils.isEmpty(orderDetail.getCardTokenRequired())) {
            paymentRequestBean.setCardTokenRequired(orderDetail.getCardTokenRequired().equalsIgnoreCase("true") ? true
                    : false);
        }

        // cart validation is to be done by backend
        if (StringUtils.isNotBlank(orderDetail.getCartValidationRequired())) {
            paymentRequestBean.setCartValidationRequired(orderDetail.getCartValidationRequired().equalsIgnoreCase(
                    "true") ? true : false);
        }

        if (orderDetail.getAdditionalInfo() != null) {
            paymentRequestBean.setAdditionalInfo(JsonMapper.mapObjectToJson(orderDetail.getAdditionalInfo()));
        }

        paymentRequestBean.setAppId(paymentDetails.getAppId());
        paymentRequestBean.setNativeJsonRequest(true);
        paymentRequestBean.setCorporateCustId(orderDetail.getCorporateCustId());
        paymentRequestBean.setCardHash(orderDetail.getCardHash());
        String preferredOtpPage = paymentDetails.getPreferredOtpPage();
        if (StringUtils.isNotBlank(preferredOtpPage)
                && !(PreferredOtpPage.MERCHANT.getValue().equals(preferredOtpPage) || PreferredOtpPage.BANK.getValue()
                        .equals(preferredOtpPage))) {
            LOGGER.error("PreferredOtpPage Value found {} Value Could only be {} {}", preferredOtpPage,
                    PreferredOtpPage.MERCHANT.getValue(), PreferredOtpPage.BANK.getValue());
            throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                    .isRetryAllowed(true).setRetryMsg(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg())
                    .build();
        }
        paymentRequestBean.setPreferredOtpPage(preferredOtpPage);
        paymentRequestBean.setSgwReferenceId(request.getBody().getSgwReferenceId());
        paymentRequestBean.setAoaSubsOnPgMid(orderDetail.isAoaSubsOnPgMid());
        return paymentRequestBean;
    }

    private String getChannelId(CreateOrderAndPaymentRequest request) {

        String merchantRequestedChannelId = request.getHead().getChannelId().getValue();
        String mid = request.getBody().getMid();
        if (isMidAllowedForForcedChannelId(mid)) {
            if (StringUtils.equals(EChannelId.WEB.getValue(), merchantRequestedChannelId)
                    || StringUtils.equals(EChannelId.WAP.getValue(), merchantRequestedChannelId)) {
                LOGGER.debug("using merchantRequestedChannelId as it matches");
                return merchantRequestedChannelId;
            }
            LOGGER.info("using WAP, as merchant has sent something other channelId");
            return EChannelId.WAP.getValue();
        }

        if (EChannelId.APP.getValue().equals(merchantRequestedChannelId)) {
            /*
             * This is done so that native+ plus integration with offline works
             * well (apparently!)
             */
            LOGGER.info("merchantRequestedChannelId:{}, changing it to {}", merchantRequestedChannelId,
                    EChannelId.WAP.getValue());
            return EChannelId.WAP.getValue();
        }
        return merchantRequestedChannelId;
    }

    private boolean isMidAllowedForForcedChannelId(String mid) {
        String mids = com.paytm.pgplus.theia.utils.ConfigurationUtil.getProperty(MID_ALLOWED_FORCED_CHANNEL_ID);
        return StringUtils.contains(mids, mid);
    }

    private void setNativeTxnInProcessFlagInCache(String txnToken) {
        nativeSessionUtil.setNativeTxnInProcessFlag(txnToken, true);
    }

    private boolean isEncryptedCardDetails(String cardInfo) {

        String[] split = cardInfo.trim().split("\\$");
        return (split.length == ETM_ENCRYPTION_CONSTANT);
    }

    private String parseCVV(String cvv) throws Exception {
        if (StringUtils.length(cvv) < 6) {
            return cvv;
        }
        EXT_LOGGER.customInfo("Getting decrypted CVV");
        return getDecryptedCVV(cvv.trim());
    }

    private String getDecryptedCVV(String encryptedCVV) throws Exception {
        LOGGER.info("encryptedCVV payload : {}", encryptedCVV);

        String decryptedCVV = null;

        try {

            decryptedCVV = nativePaymentDetailsCryptoFactory.getCryptoUtil(NativeCryptoType.CVV).decrypt(encryptedCVV);

        } catch (Exception e) {
            LOGGER.info("cvv decryption failed");
            throw e;
        }
        EXT_LOGGER.customInfo("cvv decrypted successfully");
        return decryptedCVV;
    }

    private boolean isMaestroCardScheme(String cardBin) {
        BinDetail binDetail = null;
        try {
            binDetail = cardUtils.fetchBinDetails(cardBin);
            if (MAESTRO.equalsIgnoreCase(binDetail.getCardName())) {
                return true;
            }
        } catch (PaytmValidationException exception) {
            LOGGER.error("Error occurred while fetching bin details for bin {}, reason {}", cardBin, exception);
        }
        return false;
    }

    private boolean isBajajCardScheme(String cardBin) {
        BinDetail binDetail = null;
        try {

            binDetail = cardUtils.fetchBinDetails(cardBin);
            if (CashierConstant.BAJAJ_CARD.equalsIgnoreCase(binDetail.getCardName())) {
                return true;
            }
        } catch (PaytmValidationException exception) {
            LOGGER.error("Error occurred while fetching bin details for bin {}, reason {}", cardBin, exception);
        }
        return false;
    }

    private String getExpiry(NativeJsonRequestBody paymentDetails, String[] cardDetails) {

        String expiry = MAESTRO_EXPIRY;
        String savedCardId = cardDetails[0].trim();
        String cardExpiry = cardDetails[3].trim();

        if (StringUtils.isBlank(savedCardId) && StringUtils.isNotBlank(cardExpiry)) {
            expiry = "|" + cardExpiry;
        }
        return expiry;
    }

    private boolean isBajajFnScheme(String cardBin) {
        BinDetail binDetail = null;
        try {
            binDetail = cardUtils.fetchBinDetails(cardBin);
            if (CashierConstant.BAJAJFN.equalsIgnoreCase(binDetail.getCardName())) {
                return true;
            }
        } catch (PaytmValidationException exception) {
            LOGGER.error("Error occurred while fetching bin details for bin {}, reason {}", cardBin, exception);
        }
        return false;
    }

    public String getIndustryTypeId(String mid) {
        String industryTypeId = "NA";
        try {
            MerchantProfile merchantProfileInfo = merchantDataService.getMerchantProfileInfo(mid);
            EXT_LOGGER.customInfo("Mapping response - MerchantProfile :: {}", merchantProfileInfo);
            if (null != merchantProfileInfo && null != merchantProfileInfo.getMccCodes()) {
                industryTypeId = merchantProfileInfo.getMccCodes().get(0);
            }
        } catch (Exception e) {
            LOGGER.warn("Exception occurred on calling merchant profile api");
        }
        return industryTypeId;
    }

    private String getPaymodeForCardBin(final String requiredCardDetails) {
        BinDetail binDetails = getBinDetailForCardBin(requiredCardDetails);
        if (binDetails != null && StringUtils.isNotBlank(binDetails.getCardType())) {
            return binDetails.getCardType();
        }
        return null;
    }

    public BinDetail getBinDetailForCardBin(final String requiredCardDetails) {
        /*
         * update paymode as the bin cardtype
         */
        BinDetail binDetails = null;
        try {
            binDetails = cardUtils.fetchBinDetails(requiredCardDetails);

        } catch (Exception e) {
            LOGGER.error("failed fetching binDetails for bin {}, : {}",
                    binHelper.logMaskedBinnumber(requiredCardDetails), e);
        }
        return binDetails;
    }

    private String getPaymodeForEcomToken(final String requiredCardDetails) {
        BinDetail binDetails = getBinDetailForEcomToken(requiredCardDetails);
        if (binDetails != null && StringUtils.isNotBlank(binDetails.getCardType())) {
            return binDetails.getCardType();
        }
        return null;
    }

    public BinDetail getBinDetailForEcomToken(final String requiredCardDetails) {

        final String binNumber = requiredCardDetails.substring(0, 9);
        BinDetail binDetails = null;

        try {
            binDetails = ecomTokenUtils.fetchTokenDetails(binNumber);
        } catch (Exception e) {
            LOGGER.error("failed fetching binDetails for bin {}, : {}", binNumber, e);
        }
        return binDetails;
    }

    public Map<String, String> getAdditionalInfoMap(Map<String, String> additionalInfo, String txnToken, String subsId) {
        if (additionalInfo == null) {
            additionalInfo = new HashMap<>();
        }
        additionalInfo.put(TheiaConstant.ExtraConstants.TXN_TOKEN, txnToken);
        additionalInfo.put(TheiaConstant.ExtraConstants.SUBSCRIPTION_ID, subsId);
        return additionalInfo;
    }

    private void validateUpiSubsTxnAmount(CreateOrderAndPaymentRequestBody requestBody) {
        Double txnAmount = Double.parseDouble(requestBody.getSubscriptionDetails().getTxnAmount().getValue());
        Double upiCollectLimitUnverified = Double.parseDouble(com.paytm.pgplus.theia.utils.ConfigurationUtil
                .getProperty(UPI_COLLECT_LIMIT_UNVERIFIED, "2000"));
        boolean blockPaymentForUPICollect = txnAmount > upiCollectLimitUnverified
                && !merchantPreferenceService.isUpiCollectWhitelisted(requestBody.getMid(), true);
        if (blockPaymentForUPICollect) {
            LOGGER.error("Merchant is not verified and UPI collect txn is > {} : {}", upiCollectLimitUnverified,
                    requestBody.getOrderDetails());
            throw new NativeFlowException.ExceptionBuilder(RISK_REJECT).isRetryAllowed(false)
                    .isRedirectEnhanceFlow(true).setRetryMsg(RISK_REJECT.getMessage()).build();
        }
    }

    /**
     * Method to set checkoutJs config data in redis, this is specifically done
     * to handle web view cases.
     *
     * @param request
     * @param paymentRequestData
     */
    private void setCheckoutJsConfigDataInRedisForWebView(CreateOrderAndPaymentRequest request,
            PaymentRequestBean paymentRequestData) {
        try {
            if (request.getBody() != null && request.getBody().getPaymentDetails() != null) {
                NativePaymentRequestBody nativePaymentRequestBody = new NativePaymentRequestBody();
                nativePaymentRequestBody.setCheckoutJsConfig(request.getBody().getPaymentDetails()
                        .getCheckoutJsConfig());
                if (seamlessPaymentService.isCheckoutJsWebviewRequest(nativePaymentRequestBody, paymentRequestData)) {
                    retryServiceHelper.setNativeCheckOutJsPaymentsDataForRetry(paymentRequestData,
                            nativePaymentRequestBody);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while setting checkoutJsConfig data in redis {}", e);
        }
    }
}
