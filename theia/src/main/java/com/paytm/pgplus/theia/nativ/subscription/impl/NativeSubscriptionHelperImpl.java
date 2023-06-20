package com.paytm.pgplus.theia.nativ.subscription.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.paytm.pgplus.biz.core.validator.BizParamValidator;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.cashier.enums.PaymentMode;
import com.paytm.pgplus.common.enums.AmountType;
import com.paytm.pgplus.common.enums.FrequencyUnit;
import com.paytm.pgplus.common.enums.SubsPaymentMode;
import com.paytm.pgplus.common.enums.SubscriptionRequestType;
import com.paytm.pgplus.facade.user.models.response.ValidateLoginOtpResponse;
import com.paytm.pgplus.models.ExtendInfo;
import com.paytm.pgplus.payloadvault.subscription.request.AuthorizeSubscriptionRequest;
import com.paytm.pgplus.payloadvault.subscription.response.AuthorizeSubscriptionResponse;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.request.SubscriptionTransactionRequest;
import com.paytm.pgplus.subscription.model.response.SubscriptionCheckStatusRequestBody;
import com.paytm.pgplus.subscription.model.response.SubscriptionCheckStatusResponseBody;
import com.paytm.pgplus.subscriptionClient.model.request.DeeplinkQRRequest;
import com.paytm.pgplus.subscriptionClient.model.request.FreshSubscriptionRequest;
import com.paytm.pgplus.subscriptionClient.model.response.DeeplinkQRResponse;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import com.paytm.pgplus.subscriptionClient.service.ISubscriptionService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.MandateException;
import com.paytm.pgplus.theia.nativ.model.payview.response.QrDetail;
import com.paytm.pgplus.theia.nativ.subscription.INativeSubscriptionHelper;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.paytm.pgplus.common.enums.FrequencyUnit.getFrequencyUnitbyName;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.SUBSCRIPTION_AMOUNT_LIMIT_ON_PPI;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.SUBSCRIPTION_AMOUNT_LIMIT_ON_UPI;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.SUCCESS;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.VERSION;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.DISABLE_2FA_FOR_SUBSONWALLET_IN_APPHOSTEDFLOW;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.CLIENT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants.ResponseCodes.SUBSCRIPTION_SUCCESS_RESPONSE_CODE;

@Service
@Qualifier("nativeSubscriptionHelper")
public class NativeSubscriptionHelperImpl implements INativeSubscriptionHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeSubscriptionHelperImpl.class);

    public static final String SUBSCRIPTION_MAXIMUM_GRACE_DAYS_CREDIT_CARD = ConfigurationUtil.getProperty(
            TheiaConstant.MerchantPreference.PreferenceKeys.SUBSCRIPTION_MAXIMUM_GRACE_DAYS_CREDIT_CARD, "3");
    public static final String SUBSCRIPTION_MAXIMUM_GRACE_DAYS_DEBIT_CARD = ConfigurationUtil.getProperty(
            TheiaConstant.MerchantPreference.PreferenceKeys.SUBSCRIPTION_MAXIMUM_GRACE_DAYS_DEBIT_CARD, "3");
    static Map<EncodeHintType, Object> hintMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
    private static List<String> listOfAOASubscriptionPaymodesConfigured = Arrays
            .asList(ConfigurationUtil.getProperty(TheiaConstant.ExtraConstants.AOA_SUBSCRIPTION_PAYMODES, "")
                    .split(",")).stream().map(s -> s.trim()).filter(s -> s.length() > 0).collect(Collectors.toList());;
    static String fileConstant = "qrCode";
    static int width = 480;
    static int height = 480;
    static String charset = "UTF-8";
    static String fileExt = "png";

    static {
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hintMap.put(EncodeHintType.MARGIN, 0); /* default = 4 */
    }

    @Autowired
    @Qualifier(value = "merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private ISubscriptionService subscriptionService;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private Ff4jUtils ff4JUtil;

    public FreshSubscriptionRequest createFreshSubscriptionRequest(SubscriptionTransactionRequest request) {
        FreshSubscriptionRequest freshSubscriptionRequest = new FreshSubscriptionRequest();

        String retryCount = request.getBody().getSubscriptionRetryCount();
        if (!"1".equals(request.getBody().getSubscriptionEnableRetry())) {
            retryCount = "1";
        }
        String graceDays = "0";
        if (StringUtils.isNotBlank(request.getBody().getSubscriptionGraceDays())) {
            graceDays = request.getBody().getSubscriptionGraceDays();
        }
        freshSubscriptionRequest.setSubscriptionAmountType(AmountType.getEnumByName(
                request.getBody().getSubscriptionAmountType()).getName());
        freshSubscriptionRequest.setSubscriptionEnableRetry(request.getBody().getSubscriptionEnableRetry());
        freshSubscriptionRequest.setSubscriptionExpiryDate(request.getBody().getSubscriptionExpiryDate());
        freshSubscriptionRequest
                .setSubscriptionFrequency(request.getBody().getSubscriptionFrequency() != null ? request.getBody()
                        .getSubscriptionFrequency() : "1");
        freshSubscriptionRequest.setSubscriptionFrequencyUnit(FrequencyUnit.getFrequencyUnitbyName(
                request.getBody().getSubscriptionFrequencyUnit()).getName());
        freshSubscriptionRequest.setGraceDays(graceDays);
        freshSubscriptionRequest.setSubscriptionStartDate(calculateSubsStartDate(request.getBody()
                .getSubscriptionStartDate(), request.getBody().getSubscriptionFrequency(), request.getBody()
                .getSubscriptionFrequencyUnit()));

        freshSubscriptionRequest.setRetryCount(retryCount);
        freshSubscriptionRequest.setStartDateFlow("true");
        if (StringUtils.isBlank(request.getBody().getSubscriptionStartDate())) {
            freshSubscriptionRequest.setStartDateFlow("false");
        }

        freshSubscriptionRequest.setSubscriptionMaxAmount(AmountUtils.getTransactionAmountInPaise(request.getBody()
                .getSubscriptionMaxAmount()));
        freshSubscriptionRequest.setPaymentMode(evaluateSubsPayMode(request));
        freshSubscriptionRequest.setSubsPPIonly(request.getBody().getSubsPPIOnly());
        freshSubscriptionRequest.setCustomerId(request.getBody().getUserInfo().getCustId());
        freshSubscriptionRequest.setChannelId(request.getHead().getChannelId().getValue());
        freshSubscriptionRequest.setMid(request.getBody().getMid());
        freshSubscriptionRequest.setRequestType(SubscriptionRequestType.SUBSCRIBE.getName());
        freshSubscriptionRequest.setTxnAmount(AmountUtils.getTransactionAmountInPaise(request.getBody().getTxnAmount()
                .getValue()));
        freshSubscriptionRequest.setOrderId(request.getBody().getOrderId());
        freshSubscriptionRequest.setWebsite(request.getBody().getWebsiteName());
        freshSubscriptionRequest.setIndustryType("NA");
        freshSubscriptionRequest.setSsoId(request.getBody().getPaytmSsoToken());
        freshSubscriptionRequest.setAutoRetry(request.getBody().isAutoRetry());
        freshSubscriptionRequest.setAutoRenewal(request.getBody().isAutoRenewal());
        freshSubscriptionRequest.setCommunicationManager(request.getBody().isCommunicationManager());
        freshSubscriptionRequest.setSubsGoodsInfo(request.getBody().getSubsGoodsInfo());
        freshSubscriptionRequest.setRenewalAmount(AmountUtils.getTransactionAmountInPaise(request.getBody()
                .getRenewalAmount()));
        freshSubscriptionRequest.setUserInfo(request.getBody().getUserInfo());
        freshSubscriptionRequest.setSubsPurpose(request.getBody().getSubscriptionPurpose());
        if (request.isSuperGwHit() && request.getBody().isAuthorized()) {
            freshSubscriptionRequest.setAuthorized(true);
        }
        freshSubscriptionRequest.setFlexiSubscription(request.getBody().isFlexiSubscription());
        if (StringUtils.isNotBlank(request.getBody().getMandateId())) {
            freshSubscriptionRequest.setMandateId(request.getBody().getMandateId());
        }
        freshSubscriptionRequest.setExtendInfo(getExtendedInfo(request));
        return freshSubscriptionRequest;
    }

    private ExtendInfo getExtendedInfo(SubscriptionTransactionRequest request) {
        ExtendInfo extendInfo = new ExtendInfo();
        if (Objects.nonNull(request) && Objects.nonNull(request.getBody().getExtendInfo())) {
            extendInfo.setUdf1(request.getBody().getExtendInfo().getUdf1());
            extendInfo.setUdf2(request.getBody().getExtendInfo().getUdf2());
            extendInfo.setUdf3(request.getBody().getExtendInfo().getUdf3());
            extendInfo.setMercUnqRef(request.getBody().getExtendInfo().getMercUnqRef());
            extendInfo.setComments(request.getBody().getExtendInfo().getComments());
            return extendInfo;
        }
        return extendInfo;
    }

    // public AoaSubscriptionCreateRequest
    // createFreshAOASubscriptionRequest(SubscriptionTransactionRequest request)
    // {
    // AoaSubscriptionCreateRequest freshSubscriptionRequest = new
    // AoaSubscriptionCreateRequest();
    //
    // String retryCount = request.getBody().getSubscriptionRetryCount();
    // if (!"1".equals(request.getBody().getSubscriptionEnableRetry())) {
    // retryCount = "1";
    // }
    // String graceDays = "0";
    // if (StringUtils.isNotBlank(request.getBody().getSubscriptionGraceDays()))
    // {
    // graceDays = request.getBody().getSubscriptionGraceDays();
    // }
    // freshSubscriptionRequest.setSubscriptionAmountType(AmountType.getEnumByName(
    // request.getBody().getSubscriptionAmountType()).getName());
    // freshSubscriptionRequest.setSubscriptionEnableRetry(request.getBody().getSubscriptionEnableRetry());
    // freshSubscriptionRequest.setSubscriptionExpiryDate(request.getBody().getSubscriptionExpiryDate());
    // freshSubscriptionRequest
    // .setSubscriptionFrequency(request.getBody().getSubscriptionFrequency() !=
    // null ? request.getBody()
    // .getSubscriptionFrequency() : "1");
    // freshSubscriptionRequest.setSubscriptionFrequencyUnit(FrequencyUnit.getFrequencyUnitbyName(
    // request.getBody().getSubscriptionFrequencyUnit()).getName());
    // freshSubscriptionRequest.setGraceDays(graceDays);
    // freshSubscriptionRequest.setSubscriptionStartDate(calculateSubsStartDate(request.getBody()
    // .getSubscriptionStartDate(),
    // request.getBody().getSubscriptionFrequency(), request.getBody()
    // .getSubscriptionFrequencyUnit()));
    //
    // freshSubscriptionRequest.setRetryCount(retryCount);
    // freshSubscriptionRequest.setStartDateFlow("true");
    // if (StringUtils.isBlank(request.getBody().getSubscriptionStartDate())) {
    // freshSubscriptionRequest.setStartDateFlow("false");
    // }
    //
    // freshSubscriptionRequest.setSubscriptionMaxAmount(AmountUtils.getTransactionAmountInPaise(request.getBody()
    // .getSubscriptionMaxAmount()));
    // freshSubscriptionRequest.setPaymentMode(evaluateSubsPayMode(request));
    // freshSubscriptionRequest.setSubsPPIonly(request.getBody().getSubsPPIOnly());
    // freshSubscriptionRequest.setCustomerId(request.getBody().getUserInfo().getCustId());
    // freshSubscriptionRequest.setChannelId(request.getHead().getChannelId().getValue());
    // freshSubscriptionRequest.setMid(request.getBody().getMid());
    // freshSubscriptionRequest.setRequestType(SubscriptionRequestType.SUBSCRIBE.getName());
    // freshSubscriptionRequest.setTxnAmount(AmountUtils.getTransactionAmountInPaise(request.getBody().getTxnAmount()
    // .getValue()));
    // freshSubscriptionRequest.setOrderId(request.getBody().getOrderId());
    // freshSubscriptionRequest.setWebsite(request.getBody().getWebsiteName());
    // freshSubscriptionRequest.setIndustryType("NA");
    // freshSubscriptionRequest.setSsoId(request.getBody().getPaytmSsoToken());
    // freshSubscriptionRequest.setAutoRetry(request.getBody().isAutoRetry());
    // freshSubscriptionRequest.setAutoRenewal(request.getBody().isAutoRenewal());
    // freshSubscriptionRequest.setCommunicationManager(request.getBody().isCommunicationManager());
    // freshSubscriptionRequest.setSubsGoodsInfo(request.getBody().getSubsGoodsInfo());
    // freshSubscriptionRequest.setRenewalAmount(AmountUtils.getTransactionAmountInPaise(request.getBody()
    // .getRenewalAmount()));
    // freshSubscriptionRequest.setUserInfo(request.getBody().getUserInfo());
    // freshSubscriptionRequest.setSubsPurpose(request.getBody().getSubscriptionPurpose());
    // if (request.isSuperGwHit() && request.getBody().isAuthorized()) {
    // freshSubscriptionRequest.setAuthorized(true);
    // }
    // freshSubscriptionRequest.setFlexiSubscription(request.getBody().isFlexiSubscription());
    // String extendInfoJsonString = null;
    // try {
    // extendInfoJsonString =
    // JsonMapper.mapObjectToJson(request.getBody().getExtendInfo());
    // } catch (Exception e) {
    // LOGGER.error("Exception Occurred while extendInfo to Json :: {}", e);
    // }
    // freshSubscriptionRequest.setExtendedInfo(extendInfoJsonString);
    // return freshSubscriptionRequest;
    // }

    public SubsPaymentMode evaluateSubsPayMode(SubscriptionTransactionRequest request) {
        SubsPaymentMode subsPayMode = null;
        String inputPaymentMode = request.getBody().getSubscriptionPaymentMode();
        if (SubsPaymentMode.UNKNOWN.name().equalsIgnoreCase(inputPaymentMode) || StringUtils.isBlank(inputPaymentMode)) {
            subsPayMode = SubsPaymentMode.UNKNOWN;
        } else if (SubsPaymentMode.NORMAL.name().equalsIgnoreCase(inputPaymentMode)) {
            subsPayMode = SubsPaymentMode.NORMAL;
        } else if (SubsPaymentMode.PPI.name().equalsIgnoreCase(inputPaymentMode)) {
            subsPayMode = SubsPaymentMode.PPI;
            request.getBody().setSubsPPIOnly("Y");
        } else if (SubsPaymentMode.CC.name().equalsIgnoreCase(inputPaymentMode)) {
            subsPayMode = SubsPaymentMode.CC;
        } else if (SubsPaymentMode.DC.name().equalsIgnoreCase(inputPaymentMode)) {
            subsPayMode = SubsPaymentMode.DC;
        } else if (SubsPaymentMode.PPBL.name().equalsIgnoreCase(inputPaymentMode)) {
            subsPayMode = SubsPaymentMode.PPBL;
        } else if (SubsPaymentMode.BANK_MANDATE.name().equalsIgnoreCase(inputPaymentMode)) {
            subsPayMode = SubsPaymentMode.BANK_MANDATE;
        } else if (SubsPaymentMode.UPI.name().equalsIgnoreCase(inputPaymentMode))
            subsPayMode = SubsPaymentMode.UPI;
        return subsPayMode;
    }

    public String calculateSubsStartDate(String startDate, String frequency, String frequencyUnit) {
        if (StringUtils.isBlank(startDate)) {
            Calendar today = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (frequencyUnit.equals(FrequencyUnit.MONTH.getName())) {
                today.add(Calendar.MONTH, Integer.parseInt(frequency));
                return sdf.format(today.getTime());
            } else if (frequencyUnit.equals(FrequencyUnit.YEAR.getName())) {
                today.add(Calendar.YEAR, Integer.parseInt(frequency));
                return sdf.format(today.getTime());
            } else {
                FrequencyUnit frequencyUnitbyName = FrequencyUnit.getFrequencyUnitbyName(frequencyUnit);
                Integer multiplier = 0;
                if (frequencyUnitbyName != null) {
                    multiplier = frequencyUnitbyName.getMultiplier();
                }
                today.add(Calendar.DATE, Integer.parseInt(frequency) * multiplier);
                return sdf.format(today.getTime());
            }
        }
        return startDate;
    }

    @Override
    public boolean subsPPIAmountLimitBreached(String subscriptionPaymentMode, String subscriptionMaxAmount, String mid) {
        boolean limitApplicable = merchantPreferenceService.isSubscriptionLimitOnWalletEnabled(mid);
        Double thresholdValue = Double.parseDouble(com.paytm.pgplus.biz.utils.ConfigurationUtil.getTheiaProperty(
                SUBSCRIPTION_AMOUNT_LIMIT_ON_PPI, "2000"));
        if (limitApplicable && PaymentMode.PPI.getMode().equalsIgnoreCase(subscriptionPaymentMode)
                && thresholdValue.compareTo(Double.parseDouble(subscriptionMaxAmount)) < 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isSubscriptionNotAuthorized(PaymentRequestBean paymentRequestData) {
        boolean limitApplicable = merchantPreferenceService.isSubscriptionLimitOnWalletEnabled(paymentRequestData
                .getMid());
        if (PaymentMode.PPI.getMode().equalsIgnoreCase(paymentRequestData.getSubsPaymentMode())) {
            if (isAppHostedFlow(paymentRequestData)
                    && ff4JUtil.isFeatureEnabledOnMid(paymentRequestData.getMid(),
                            DISABLE_2FA_FOR_SUBSONWALLET_IN_APPHOSTEDFLOW, false)) {
                return false;
            }
            if (limitApplicable
                    && BooleanUtils.isNotTrue(nativeSessionUtil.isSubscriptionAuthorized(paymentRequestData
                            .getTxnToken()))) {
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public void markSubscriptionAuthorized(String txnToken, InitiateTransactionRequestBody orderDetail,
            ValidateLoginOtpResponse validateLoginOtpResponse) {
        nativeSessionUtil.markSubscriptionAuthorized(txnToken);
        StringBuilder key = new StringBuilder(orderDetail.getRequestType()).append(txnToken);
        SubscriptionResponse subscriptionResponse = (SubscriptionResponse) theiaTransactionalRedisUtil.get(key
                .toString());
        AuthorizeSubscriptionRequest subscriptionRequest = new AuthorizeSubscriptionRequest(
                subscriptionResponse.getSubscriptionId(), validateLoginOtpResponse.getResponseData().getBasicInfo()
                        .getPhone(), validateLoginOtpResponse.getResponseData().getUserId());
        AuthorizeSubscriptionResponse response = subscriptionService.authorizeSubscription(subscriptionRequest);
        LOGGER.info("Subscription Authorize Response : {}", response);
    }

    public boolean invalidUpifrequencyCycle(String frequencyCycle) {

        FrequencyUnit frequencyUnit = FrequencyUnit.getFrequencyUnitbyName(frequencyCycle);
        if (StringUtils.isEmpty(frequencyUnit.getUpiFreq())) {
            return true;
        }
        return false;
    }

    public boolean subsUpiMonthlyFrequencyBreach(String frequencyCycle, String subsFrequency) {
        FrequencyUnit frequencyUnit = FrequencyUnit.getFrequencyUnitbyName(frequencyCycle);
        if (FrequencyUnit.MONTH == frequencyUnit) {
            if (Integer.parseInt(subsFrequency) > 2 || Integer.parseInt(subsFrequency) < 0)
                return true;
        }
        return false;
    }

    public boolean subsUPIAmountLimitBreached(String subsAmount) {
        Double thresholdValue = Double.parseDouble(com.paytm.pgplus.biz.utils.ConfigurationUtil.getTheiaProperty(
                SUBSCRIPTION_AMOUNT_LIMIT_ON_UPI, "2000"));
        if (thresholdValue.compareTo(Double.parseDouble(subsAmount)) < 0)
            return true;
        return false;
    }

    public boolean invalidUpiGraceDays(String frequencyCycle, String graceDays, String frequency) {
        FrequencyUnit frequencyUnit = getFrequencyUnitbyName(frequencyCycle);
        int freq = Integer.parseInt(frequency);

        if (frequencyUnit != null && StringUtils.isNotBlank(graceDays) && StringUtils.isNumeric(graceDays)
                && Integer.valueOf(graceDays) > (frequencyUnit.getMultiplier() * freq))
            return true;
        return false;
    }

    public boolean invalidUpiSubsStartDate(String subsStartDate, String frequencyUnit) {
        try {
            if (!BizParamValidator.validateSubscritpionStartDate(subsStartDate)
                    || !validUPIFutureStartDate(subsStartDate, frequencyUnit)) {
                return true;
            }
        } catch (ParseException e) {
            return true;
        }
        return false;
    }

    @Override
    public boolean invalidSubsRetryCount(String subscriptionEnableRetry, String subscriptionRetryCount) {
        if ("1".equals(subscriptionEnableRetry) && StringUtils.isNumeric(subscriptionRetryCount)
                && Integer.parseInt(subscriptionRetryCount) > 2) {
            return true;
        }
        return false;
    }

    @Override
    public boolean invalidUpiSubsFrequency(String subsFrequency) {
        if (StringUtils.isNotBlank(subsFrequency) && StringUtils.isNumeric(subsFrequency)
                && (Integer.parseInt(subsFrequency) > 1 || Integer.parseInt(subsFrequency) <= 0))
            return true;
        return false;
    }

    public boolean validUPIFutureStartDate(String subsStartDate, String frequencyUnit) throws ParseException {
        Date todayDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(todayDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date subsDate = sdf.parse(subsStartDate);
        if (DateUtils.isSameDay(subsDate, todayDate)) {
            return true;
        }
        FrequencyUnit frequency = FrequencyUnit.getFrequencyUnitbyName(frequencyUnit);
        boolean isvalid = false;
        switch (frequency) {
        case DAY:
            cal.add(Calendar.DATE, 1);
            isvalid = DateUtils.isSameDay(cal.getTime(), subsDate);
            break;
        case WEEK:
            cal.add(Calendar.WEEK_OF_YEAR, 1);
            isvalid = DateUtils.isSameDay(cal.getTime(), subsDate);
            break;
        case FORTNIGHT:
            cal.add(Calendar.DATE, 15);
            isvalid = DateUtils.isSameDay(cal.getTime(), subsDate);
            break;
        case MONTH:
            cal.add(Calendar.MONTH, 1);
            isvalid = DateUtils.isSameDay(cal.getTime(), subsDate);
            break;
        case BI_MONTHLY:
            cal.add(Calendar.MONTH, 2);
            isvalid = DateUtils.isSameDay(cal.getTime(), subsDate);
            break;
        case QUARTER:
            cal.add(Calendar.MONTH, 3);
            if (DateUtils.isSameDay(cal.getTime(), subsDate))
                return true;
        case SEMI_ANNUALLY:
            cal.add(Calendar.MONTH, 6);
            isvalid = DateUtils.isSameDay(cal.getTime(), subsDate);
            break;
        case YEAR:
            cal.add(Calendar.YEAR, 1);
            isvalid = DateUtils.isSameDay(cal.getTime(), subsDate);
            break;
        }
        return isvalid;
    }

    @Override
    public SubscriptionCheckStatusResponseBody getSubscriptionStatus(String subsId, String mid, String custId,
            String orderId) {
        SubscriptionCheckStatusRequestBody request = new SubscriptionCheckStatusRequestBody(subsId, mid, custId,
                orderId);
        SubscriptionCheckStatusResponseBody response = subscriptionService.checkSubscriptionStatus(request);

        if (null != response && SUBSCRIPTION_SUCCESS_RESPONSE_CODE.equals(response.getResultInfo().getCode())) {
            LOGGER.info("Response received for subscription status api {}", response);

            return response;
        } else {
            LOGGER.error("Erroneous response received from subscription check status response api {}", response);
            throw new MandateException.ExceptionBuilder(null,
                    OfflinePaymentUtils.resultInfo(ResultCode.MERCHANT_REDIRECT_REQUEST_EXCEPTION), true).build();
        }
    }

    @Override
    public boolean invalidSubsFrequencyUnitForBankMandate(String subscriptionFrequencyUnit,
            String subscriptionPaymentMode) {
        if (SubsPaymentMode.BANK_MANDATE.name().equalsIgnoreCase(subscriptionPaymentMode)
                && FrequencyUnit.FORTNIGHT.getName().equalsIgnoreCase(subscriptionFrequencyUnit)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean invalidGraceDaysForCard(String graceDays) {
        if (StringUtils.isBlank(graceDays) || !StringUtils.isNumeric(graceDays) || Integer.valueOf(graceDays) < 0) {
            return true;
        }
        return false;
    }

    @Override
    public String fetchDeepLink(String orderId, String mid) {
        LOGGER.info("fetching deepLink....");
        DeeplinkQRRequest deeplinkQRRequest = new DeeplinkQRRequest();
        deeplinkQRRequest.setMid(mid);
        deeplinkQRRequest.setOrderId(orderId);
        DeeplinkQRResponse deeplinkQRResponse = subscriptionService.generateDeeplinkForSubscription(deeplinkQRRequest);

        if (deeplinkQRResponse != null && null != deeplinkQRResponse.getResultInfo()
                && SUCCESS.equalsIgnoreCase(deeplinkQRResponse.getResultInfo().getStatus())) {
            return deeplinkQRResponse.getDeeplink();
        } else {
            LOGGER.error("Failure while fetching deeplink response {}", deeplinkQRResponse);
        }
        return null;
    }

    @Override
    public QrDetail fetchQrDetails(String orderId, String mid, String deepLink) {
        LOGGER.info("fetching qrDetail.....");
        QrDetail qrDetail = null;

        if (StringUtils.isBlank(deepLink)) {
            LOGGER.error("deeplink from subscription is null");
            return null;
        }
        qrDetail = getQrDetailForSubscription(mid, deepLink);

        return qrDetail;
    }

    private boolean isAppHostedFlow(PaymentRequestBean paymentRequestData) {
        return StringUtils.isNotBlank(paymentRequestData.getRequest().getParameter(CLIENT))
                && StringUtils.isNotBlank(paymentRequestData.getRequest().getParameter(VERSION));
    }

    public static String createQRCode(String qrCodeData) throws WriterException, IOException {

        return createQRCodePngFormat(qrCodeData);
    }

    private static String createQRCodePngFormat(String qrCodeData) throws WriterException, IOException {

        /* logger.info("value of qrcode data ="+qrCodeData); */
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // ZXing QR-code encoding
        BitMatrix bitMatrix = new QRCodeWriter().encode(qrCodeData, BarcodeFormat.QR_CODE, width, height, hintMap);
        // Convert to PNG image and write to stream
        MatrixToImageWriter.writeToStream(bitMatrix, fileExt, outputStream);
        // Encode to Base 64
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    private QrDetail getQrDetailForSubscription(String mID, String deepLink) {
        QrDetail qrDetail = null;
        try {
            String qr = null;
            if (StringUtils.isNotBlank(deepLink)) {
                qr = createQRCode(deepLink);
            }

            String pageTimeout = com.paytm.pgplus.common.config.ConfigurationUtil.getProperty(
                    TheiaConstant.ExtraConstants.ENHANCE_DYNAMIC_QR_PAGE_TIMEOUT, "4800000");
            boolean isPRN = merchantPreferenceService.isPRNEnabled(mID);

            String displayMessage;

            displayMessage = com.paytm.pgplus.common.config.ConfigurationUtil
                    .getProperty(TheiaConstant.ExtraConstants.SUBSCRIPTION_UPI_QR_DISPLAY_MESSAGE);

            qrDetail = new QrDetail(qr, Long.valueOf(pageTimeout), displayMessage, true, isPRN, true);

        } catch (Exception e) {
            LOGGER.error("Exception occured while fetching getQrDetailForSubscription :", e);
        }
        return qrDetail;

    }

    @Override
    public List<String> getAOASubscriptionPaymodesConfigured() {
        return listOfAOASubscriptionPaymodesConfigured;
    }

}