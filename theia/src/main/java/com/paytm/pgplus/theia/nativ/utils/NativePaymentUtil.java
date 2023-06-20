package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.utils.MappingUtil;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.log.EventLogger;
import com.paytm.pgplus.common.model.ConsultDetails;
import com.paytm.pgplus.common.model.nativ.NativeConsultDetails;
import com.paytm.pgplus.enums.EPreAuthType;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.integration.enums.SupportRegion;
import com.paytm.pgplus.facade.payment.models.FeeRateFactors;
import com.paytm.pgplus.facade.payment.models.PayChannelOptionView;
import com.paytm.pgplus.facade.user.models.UserDetails;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.models.PaymentMode;
import com.paytm.pgplus.models.RiskFeeDetails;
import com.paytm.pgplus.models.TwoFADetails;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.request.SubscriptionTransactionRequestBody;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.ELitePayViewDisabledReasonMsg;
import com.paytm.pgplus.theia.nativ.model.auth.GenerateOtpRequest;
import com.paytm.pgplus.theia.nativ.model.auth.NativeUserLogoutRequest;
import com.paytm.pgplus.theia.nativ.model.auth.ValidateOtpRequest;
import com.paytm.pgplus.theia.nativ.model.balanceinfo.BalanceInfoResponse;
import com.paytm.pgplus.theia.nativ.model.balanceinfo.FetchBalanceInfoRequest;
import com.paytm.pgplus.theia.nativ.model.balanceinfo.FetchBalanceInfoRequestBody;
import com.paytm.pgplus.theia.nativ.model.common.NativeCashierInfoContainerRequest;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.model.payview.nb.NativeFetchNBPayChannelRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequestBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.PayChannelBase;
import com.paytm.pgplus.theia.nativ.model.payview.response.PayOption;
import com.paytm.pgplus.theia.nativ.model.pcfDetails.FetchPcfDetailRequest;
import com.paytm.pgplus.theia.nativ.model.pcfDetails.FetchPcfDetailRequestBody;
import com.paytm.pgplus.theia.nativ.model.pcfDetails.FetchPcfDetailResponse;
import com.paytm.pgplus.theia.nativ.model.vpaValidate.VpaValidateRequest;
import com.paytm.pgplus.theia.nativ.processor.impl.NativeFetchBalanceInfoRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.impl.NativeFetchPcfDetailsRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.impl.NativePayviewConsultRequestProcessor;
import com.paytm.pgplus.theia.offline.constants.FrontEndMsgConstant;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.utils.EventUtils;
import com.paytm.pgplus.theia.utils.ProcessTransactionUtil;
import com.paytm.pgplus.theiacommon.response.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RedisKeysConstant.NativeSession.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.CARD_PRE_AUTH_TYPE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.SSO_TOKEN;
import static com.paytm.pgplus.theia.utils.EnvInfoUtil.httpServletRequest;

@Component
public class NativePaymentUtil {

    @Autowired
    private NativePayviewConsultRequestProcessor nativePayviewConsultRequestProcessor;

    @Autowired
    NativeFetchBalanceInfoRequestProcessor nativeFetchBalanceInfoRequestProcessor;

    @Autowired
    NativeFetchPcfDetailsRequestProcessor nativeFetchPcfDetailsRequestProcessor;

    @Autowired
    NativeSessionUtil nativeSessionUtil;

    @Autowired
    ProcessTransactionUtil processTransactionUtil;

    @Autowired
    @Qualifier("mapUtilsBiz")
    private MappingUtil mappingUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(NativePaymentUtil.class);

    public static ResultInfo resultInfo(ResultCode resultCode) {
        if (resultCode == null)
            resultCode = ResultCode.UNKNOWN_ERROR;
        return new ResultInfo(resultCode.getResultStatus(), resultCode.getResultCodeId(), resultCode.getResultMsg());
    }

    public static ResultInfo resultInfoForSuccess() {
        return resultInfo(ResultCode.SUCCESS);
    }

    public static String successRateMsg(boolean successRateFlag, String bankName) {
        return successRateFlag ? FrontEndMsgConstant.BANK_SUCCESS_RATE_LOW_MSG + bankName : "";
    }

    public static String getLastFourDigits(Long lastFourDigit) {

        if (lastFourDigit == null) {
            return StringUtils.EMPTY;
        }
        if (lastFourDigit > 999) {
            return lastFourDigit.toString();
        }
        String lastFourDigitsPadded = new StringBuilder().append("0000").append(lastFourDigit).toString();
        return lastFourDigitsPadded.substring(lastFourDigitsPadded.length() - 4);
    }

    public void fetchPaymentOptions(TokenRequestHeader request, InitiateTransactionRequestBody orderDetail,
            TwoFADetails twoFADetails) throws Exception {
        NativeCashierInfoRequest nativeCashierInfoRequest = new NativeCashierInfoRequest();
        NativeCashierInfoRequestBody nativeCashierInfoRequestBody = new NativeCashierInfoRequestBody();
        nativeCashierInfoRequest.setHead(request);
        nativeCashierInfoRequestBody.setAddMoneyFeeAppliedOnWallet(checkIsAddMoneyFeeApplied(request, orderDetail));
        nativeCashierInfoRequestBody.setInitialAddMoneyAmount(getInitialAmountWithoutFee(orderDetail));
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();

        String preAuthType = (String) httpServletRequest.getAttribute(CARD_PRE_AUTH_TYPE);

        if (Objects.nonNull(preAuthType)) {

            nativeCashierInfoRequestBody.setCardPreAuthType(EPreAuthType.valueOf(preAuthType));

        }
        // setting this field as true since FPO is called internally
        nativeCashierInfoRequestBody.setInternalFetchPaymentOptions(Boolean.TRUE);
        if (iPgpFf4jClient.checkWithdefault(TheiaConstant.FF4J.THEIA_ENHANCED_RETURN_DISABLED_CHANNELS,
                new HashMap<>(), false)) {
            nativeCashierInfoRequestBody.setReturnDisabledChannels(true);
        }
        if (Objects.nonNull(twoFADetails)) {
            nativeCashierInfoRequestBody.setTwoFADetails(twoFADetails);
        }
        nativeCashierInfoRequest.setBody(nativeCashierInfoRequestBody);

        NativeCashierInfoContainerRequest nativeCashierInfoContainerRequest = new NativeCashierInfoContainerRequest(
                nativeCashierInfoRequest);
        nativePayviewConsultRequestProcessor.process(nativeCashierInfoContainerRequest);
    }

    private String getInitialAmountWithoutFee(InitiateTransactionRequestBody orderDetail) {
        return Optional.ofNullable(orderDetail).map(InitiateTransactionRequestBody::getRiskFeeDetails)
                .map(RiskFeeDetails::getInitialAmount).map(Money::getValue).orElse(null);
    }

    private boolean checkIsAddMoneyFeeApplied(TokenRequestHeader request, InitiateTransactionRequestBody orderDetail) {
        try {
            if (orderDetail != null && processTransactionUtil.isAddMoneyMerchant(orderDetail.getMid())
                    && orderDetail.isAddMoneyFeeAppliedOnWallet()) {
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred while checking if addMoneyFeeApplied {} ", e);
            return false;
        }
        return false;
    }

    public NativeCashierInfoResponse fetchPaymentOptionsWithSsoToken(TokenRequestHeader request, String mid,
            boolean isOrderIdNeedToBeGenerated, TwoFADetails twoFADetails) throws Exception {

        NativeCashierInfoRequest nativeCashierInfoRequest = new NativeCashierInfoRequest();
        nativeCashierInfoRequest.setHead(request);
        NativeCashierInfoRequestBody body = new NativeCashierInfoRequestBody();
        body.setMid(mid);
        if (Objects.nonNull(twoFADetails)) {
            body.setTwoFADetails(twoFADetails);
        }
        body.setGenerateOrderId(String.valueOf(isOrderIdNeedToBeGenerated));
        if (iPgpFf4jClient.checkWithdefault(TheiaConstant.FF4J.THEIA_ENHANCED_RETURN_DISABLED_CHANNELS,
                new HashMap<>(), false)) {
            body.setReturnDisabledChannels(true);
        }
        nativeCashierInfoRequest.setBody(body);
        NativeCashierInfoContainerRequest nativeCashierInfoContainerRequest = new NativeCashierInfoContainerRequest(
                nativeCashierInfoRequest);
        return nativePayviewConsultRequestProcessor.process(nativeCashierInfoContainerRequest);
    }

    public NativeCashierInfoResponse fetchPaymentOptionsForGuest(TokenRequestHeader request, String mid,
            boolean isOrderIdNeedToBeGenerated, String referenceId, TwoFADetails twoFADetails) throws Exception {

        NativeCashierInfoRequest nativeCashierInfoRequest = new NativeCashierInfoRequest();
        nativeCashierInfoRequest.setHead(request);
        NativeCashierInfoRequestBody body = new NativeCashierInfoRequestBody();
        body.setMid(mid);
        body.setGenerateOrderId(String.valueOf(isOrderIdNeedToBeGenerated));
        body.setReferenceId(referenceId);
        if (Objects.nonNull(twoFADetails)) {
            body.setTwoFADetails(twoFADetails);
        }
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        String preAuthType = (String) httpServletRequest.getAttribute(CARD_PRE_AUTH_TYPE);
        if (Objects.nonNull(preAuthType)) {
            body.setCardPreAuthType(EPreAuthType.valueOf(preAuthType));
        }
        if (iPgpFf4jClient.checkWithdefault(TheiaConstant.FF4J.THEIA_ENHANCED_RETURN_DISABLED_CHANNELS,
                new HashMap<>(), false)) {
            body.setReturnDisabledChannels(true);
        }
        nativeCashierInfoRequest.setBody(body);
        NativeCashierInfoContainerRequest nativeCashierInfoContainerRequest = new NativeCashierInfoContainerRequest(
                nativeCashierInfoRequest);
        return nativePayviewConsultRequestProcessor.process(nativeCashierInfoContainerRequest);
    }

    public void fetchPaymentOptionsWithPaymentContext(TokenRequestHeader request, String mid, String refId,
            List<PaymentMode> enablePaymentMode, List<PaymentMode> disablePaymentMode, UserDetails userDetails,
            String custId, ERequestType requestType,
            SubscriptionTransactionRequestBody subscriptionTransactionRequestBody) throws Exception {
        NativeCashierInfoRequest nativeCashierInfoRequest = new NativeCashierInfoRequest();
        if (request != null && request.getTokenType() == null) {
            request.setTokenType(TokenType.JWT);
        }
        nativeCashierInfoRequest.setHead(request);
        NativeCashierInfoRequestBody nativeCashierInfoRequestBody = new NativeCashierInfoRequestBody();
        nativeCashierInfoRequestBody.setMid(mid);
        nativeCashierInfoRequestBody.setReferenceId(refId);
        nativeCashierInfoRequestBody.setEnablePaymentMode(enablePaymentMode);
        nativeCashierInfoRequestBody.setDisablePaymentMode(disablePaymentMode);
        nativeCashierInfoRequestBody.setCustId(custId);
        nativeCashierInfoRequestBody.setRequestType(String.valueOf(requestType));
        if (ERequestType.NATIVE_SUBSCRIPTION.equals(requestType)) {
            nativeCashierInfoRequestBody.setSubscriptionTransactionRequestBody(subscriptionTransactionRequestBody);
        }
        if (ff4jUtils.isFeatureEnabled(TheiaConstant.FF4J.THEIA_ENHANCED_RETURN_DISABLED_CHANNELS, false)) {
            nativeCashierInfoRequestBody.setReturnDisabledChannels(true);
        }
        nativeCashierInfoRequest.setBody(nativeCashierInfoRequestBody);
        UserDetailsBiz userDetailsBiz = null;
        if (userDetails != null) {
            userDetailsBiz = getUserDetailsBizForFPO(userDetails);
        }
        NativePersistData nativePersistData = new NativePersistData(userDetailsBiz);
        NativeCashierInfoContainerRequest nativeCashierInfoContainerRequest = new NativeCashierInfoContainerRequest(
                nativeCashierInfoRequest, nativePersistData);
        nativeCashierInfoContainerRequest.setSuperGwApiHit(true);
        nativePayviewConsultRequestProcessor.process(nativeCashierInfoContainerRequest);
    }

    public NativeCashierInfoResponse fetchPaymentOptionsWithPaymentContext(TokenRequestHeader request, String mid,
            String refId, List<PaymentMode> enablePaymentMode, List<PaymentMode> disablePaymentMode,
            UserDetails userDetails, String custId) throws Exception {

        NativeCashierInfoRequest nativeCashierInfoRequest = new NativeCashierInfoRequest();
        nativeCashierInfoRequest.setHead(request);
        NativeCashierInfoRequestBody body = new NativeCashierInfoRequestBody();
        body.setMid(mid);
        body.setReferenceId(refId);
        body.setEnablePaymentMode(enablePaymentMode);
        body.setDisablePaymentMode(disablePaymentMode);
        body.setCustId(custId);
        if (iPgpFf4jClient.checkWithdefault(TheiaConstant.FF4J.THEIA_ENHANCED_RETURN_DISABLED_CHANNELS,
                new HashMap<>(), false)) {
            body.setReturnDisabledChannels(true);
        }
        nativeCashierInfoRequest.setBody(body);
        UserDetailsBiz userDetailsBiz = null;
        if (userDetails != null) {
            userDetailsBiz = getUserDetailsBizForFPO(userDetails);
        }
        NativePersistData nativePersistData = new NativePersistData(userDetailsBiz);
        NativeCashierInfoContainerRequest nativeCashierInfoContainerRequest = new NativeCashierInfoContainerRequest(
                nativeCashierInfoRequest, nativePersistData);
        nativeCashierInfoContainerRequest.setSuperGwApiHit(true);
        return nativePayviewConsultRequestProcessor.process(nativeCashierInfoContainerRequest);
    }

    public BalanceInfoResponse fetchBalance(TokenRequestHeader requestHeader, String payMethod, String mid)
            throws Exception {
        LOGGER.info("Fetching user balance for paymethod {}", payMethod);
        FetchBalanceInfoRequest fetchBalanceInfoRequest = new FetchBalanceInfoRequest();
        FetchBalanceInfoRequestBody fetchBalanceInfoRequestBody = new FetchBalanceInfoRequestBody();
        fetchBalanceInfoRequestBody.setPaymentMode(payMethod);
        fetchBalanceInfoRequestBody.setMid(mid);
        fetchBalanceInfoRequest.setHead(requestHeader);
        fetchBalanceInfoRequest.setBody(fetchBalanceInfoRequestBody);
        return nativeFetchBalanceInfoRequestProcessor.process(fetchBalanceInfoRequest);
    }

    public static ResultInfo resultInfoForFailure() {
        return resultInfo(ResultCode.FAILED);
    }

    public Map<EPayMethod, NativeConsultDetails> consultFeeForGivenPaymode(String token, String paymethod,
            String instId, FeeRateFactors feeRateFactors, String bin) throws Exception {
        FetchPcfDetailRequest fetchPcfDetailRequest = new FetchPcfDetailRequest();
        FetchPcfDetailRequestBody body = new FetchPcfDetailRequestBody();
        TokenRequestHeader head = new TokenRequestHeader();
        head.setTxnToken(token);
        List<PayChannelOptionView> payMethods = new ArrayList<PayChannelOptionView>();
        FetchPcfDetailResponse fetchPcfDetailResponse = null;
        PayMethod paymentMethod;
        if (EPayMethod.PPBL.getMethod().equals(paymethod)) {
            paymentMethod = PayMethod.PPBL;
        } else {
            paymentMethod = PayMethod.getPayMethodByMethod(paymethod);
        }
        PayChannelOptionView payChannelOptionView = new PayChannelOptionView(paymentMethod.getOldName(), paymentMethod,
                true);
        payChannelOptionView.setFeeRateFactors(feeRateFactors);
        payChannelOptionView.setInstId(instId);
        payMethods.add(payChannelOptionView);
        body.setPayMethods(payMethods);
        body.setBin(bin);
        fetchPcfDetailRequest.setHead(head);
        fetchPcfDetailRequest.setBody(body);
        fetchPcfDetailResponse = nativeFetchPcfDetailsRequestProcessor.process(fetchPcfDetailRequest);
        return fetchPcfDetailResponse.getBody().getConsultDetails();

    }

    public void validateInternationalCard(final PayOption payOption, String cardScheme, String cardType)
            throws PaytmValidationException {
        if (payOption.getPayMethods() != null) {
            List<PayChannelBase> channelList;
            com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod paymentMethod;
            if (TheiaConstant.ExtraConstants.DEBIT_CARD.equals(cardType)) {
                paymentMethod = payOption.getPayMethods().stream()
                        .filter(payMethod -> EPayMethod.DEBIT_CARD.getMethod().equals(payMethod.getPayMethod()))
                        .findAny().orElse(null);

            } else {
                paymentMethod = payOption.getPayMethods().stream()
                        .filter(payMethod -> EPayMethod.CREDIT_CARD.getMethod().equals(payMethod.getPayMethod()))
                        .findAny().orElse(null);

            }
            if (paymentMethod != null) {
                channelList = paymentMethod.getPayChannelOptions();
                PayChannelBase channelInfo = null;
                for (PayChannelBase bankInfo : channelList) {
                    if (bankInfo.getIsDisabled() != null
                            && TheiaConstant.ExtraConstants.TRUE.equals(bankInfo.getIsDisabled().getStatus())
                            && ELitePayViewDisabledReasonMsg.CHANNEL_NOT_AVAILABLE.getDisabledReason().equals(
                                    bankInfo.getIsDisabled().getMsg())) {
                        continue;
                    }
                    if (StringUtils.contains(bankInfo.getPayChannelOption(), cardScheme)) {

                        channelInfo = bankInfo;
                        break;
                    }
                }

                if ((channelInfo != null) && !(channelInfo.toString().contains(SupportRegion.INTL.name()))) {
                    LOGGER.info("international card validation exception ");

                    throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_INTERNATIONAL_CARD);

                }
            }
        }
    }

    public Map<EPayMethod, NativeConsultDetails> convertConsultFeeResponse(
            Map<EPayMethod, ConsultDetails> consultDetails) {

        Map<EPayMethod, NativeConsultDetails> finalConsultDetails = new HashMap<>();

        for (Map.Entry<EPayMethod, ConsultDetails> details : consultDetails.entrySet()) {
            ConsultDetails tempConsultDetails = details.getValue();
            if (tempConsultDetails != null) {
                NativeConsultDetails nativeConsultDetails = new NativeConsultDetails(tempConsultDetails.getPayMethod(),
                        tempConsultDetails.getText(), tempConsultDetails.getDisplayText());
                nativeConsultDetails.setBaseTransactionAmount(new Money(tempConsultDetails.getBaseTransactionAmount()
                        .toString()));
                nativeConsultDetails.setFeeAmount(new Money(tempConsultDetails.getFeeAmount().toString()));
                nativeConsultDetails.setTaxAmount(new Money(tempConsultDetails.getTaxAmount().toString()));
                nativeConsultDetails.setTotalConvenienceCharges(new Money(tempConsultDetails
                        .getTotalConvenienceCharges().toString()));
                nativeConsultDetails.setTotalTransactionAmount(new Money(tempConsultDetails.getTotalTransactionAmount()
                        .toString()));
                finalConsultDetails.put(details.getKey(), nativeConsultDetails);
            }

        }

        return finalConsultDetails;
    }

    public boolean isNativeEnhanceFlow(PaymentRequestBean requestData) {
        return ERequestType.NATIVE.name().equals(requestData.getRequestType())
                && requestData.getRequest().getAttribute("NATIVE_ENHANCED_FLOW") != null
                && Boolean.TRUE.equals(requestData.getRequest().getAttribute("NATIVE_ENHANCED_FLOW"));
    }

    public boolean isNativeEnhanceOrNativeEnhanceSubscriptionOrAoaEnhanceFlow(PaymentRequestBean requestData) {
        return (((ERequestType.NATIVE.name().equals(requestData.getRequestType())
                || ERequestType.NATIVE_SUBSCRIPTION.name().equals(requestData.getRequestType())
                || ERequestType.UNI_PAY.name().equals(requestData.getRequestType()) || ERequestType.NATIVE_MF_SIP
                .name().equals(requestData.getRequestType()))
                && requestData.getRequest().getAttribute("NATIVE_ENHANCED_FLOW") != null && Boolean.TRUE
                    .equals(requestData.getRequest().getAttribute("NATIVE_ENHANCED_FLOW"))) || requestData
                .isPaymentCallFromDccPage());
    }

    public void logNativeResponse(LinkedHashMap<String, String> metaData, EventNameEnum eventNameEnum) {
        try {
            String mid = MDC.get("MID") != null ? MDC.get("MID") : "";
            String orderId = MDC.get("ORDER_ID") != null ? MDC.get("ORDER_ID") : "";
            EventUtils.pushTheiaEvents(mid, orderId, eventNameEnum, metaData);
        } catch (Exception e) {
            LOGGER.info("Problem occurred while logging response : {}", e.getMessage());
        }
    }

    public void logNativeResponse(final ResultInfo resultInfo) {
        try {
            String mid = MDC.get("MID") != null ? MDC.get("MID") : "";
            String orderId = MDC.get("ORDER_ID") != null ? MDC.get("ORDER_ID") : "";
            LinkedHashMap<String, String> metaData = new LinkedHashMap<>();
            String resultStatus = "";
            String resultCode = "";
            String resultMessage = "";
            if (resultInfo != null) {
                resultCode = resultInfo.getResultCode();
                resultStatus = resultInfo.getResultStatus();
                resultMessage = resultInfo.getResultMsg();
            }
            HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest();
            metaData.put("api", servletRequest.getRequestURI());
            metaData.put("resultCode", resultCode);
            metaData.put("resultStatus", resultStatus);
            metaData.put("resultMsg", resultMessage);
            EventUtils.pushTheiaEvents(mid, orderId, EventNameEnum.NATIVE_API_RESPONSE, metaData);
            EventUtils.logResponseCode(servletRequest.getRequestURI(), EventNameEnum.RESPONSE_CODE_SENT, resultCode,
                    resultMessage);
        } catch (Exception e) {
            LOGGER.info("Problem occured while logging response : {}", e.getMessage());
        }
    }

    public void logNativeResponse(final ResultInfo resultInfo, String api) {
        try {
            String mid = MDC.get("MID") != null ? MDC.get("MID") : "";
            String orderId = MDC.get("ORDER_ID") != null ? MDC.get("ORDER_ID") : "";
            Map<String, String> metaData = new LinkedHashMap<>();
            String resultStatus = "";
            String resultCode = "";
            String resultMessage = "";
            if (resultInfo != null) {
                resultCode = resultInfo.getResultCode();
                resultStatus = resultInfo.getResultStatus();
                resultMessage = resultInfo.getResultMsg();
            }
            metaData.put("api", api);
            metaData.put("resultCode", resultCode);
            metaData.put("resultStatus", resultStatus);
            metaData.put("resultMsg", resultMessage);
            EventUtils.pushTheiaEvents(mid, orderId, EventNameEnum.NATIVE_API_RESPONSE, metaData);
            EventUtils.logResponseCode(api, EventNameEnum.RESPONSE_CODE_SENT, resultCode, resultMessage);
        } catch (Exception e) {
            LOGGER.info("Problem occured while logging response : {}", e.getMessage());
        }
    }

    public void logNativeRequests(String request) {
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        EventLogger.logNativeRequests(servletRequest.getRequestURI(), request);
    }

    public void logNativeRequests(String request, String api) {
        EventLogger.logNativeRequests(api, request);
    }

    public void logNativeRequests(String request, String api, Map<String, String> metadata) {
        EventLogger.logNativeRequests(api, request, metadata);
    }

    public void logNativeRequests(String request, Map<String, String> metadata) {
        try {
            HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest();
            EventLogger.logNativeRequests(servletRequest.getRequestURI(), request, metadata);
        } catch (Exception e) {
            LOGGER.error("problem while logging event :{}", e.getMessage());
        }
    }

    public ResultInfo getResultInfo(BaseResponseBody baseResponseBody) {
        return baseResponseBody == null ? null : baseResponseBody.getResultInfo();
    }

    public ResultInfo getResultInfo(ResponseBody responseBody) {
        ResultInfo resultInfo = null;
        if (responseBody != null && responseBody.getResultInfo() != null) {
            resultInfo = new ResultInfo();
            resultInfo.setBankRetry(responseBody.getResultInfo().getBankRetry());
            resultInfo.setResultCode(responseBody.getResultInfo().getResultCode());
            resultInfo.setResultMsg(responseBody.getResultInfo().getResultMsg());
            resultInfo.setResultStatus(responseBody.getResultInfo().getResultStatus());
            resultInfo.setUserRetryAllowed(responseBody.getResultInfo().getUserRetryAllowed());
            resultInfo.setRetry(responseBody.getResultInfo().getRetry());
        }
        return resultInfo;
    }

    public void logNativeResponse(final long startTime) {
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        EventLogger.logNativeResponseTime(servletRequest.getRequestURI(), startTime);
    }

    public void logNativeResponse(final long startTime, String api) {
        EventLogger.logNativeResponseTimeWithRequestId(api, startTime);
    }

    public void setSsoTokenInHttpRequest(String ssoToken) {
        HttpServletRequest httpServletRequest = OfflinePaymentUtils.gethttpServletRequest();
        httpServletRequest.setAttribute(SSO_TOKEN, ssoToken);
    }

    public void setReferenceIdInBody(NativeCashierInfoRequest request) {
        if (request != null && request.getBody() != null) {
            request.getBody().setReferenceId(httpServletRequest().getParameter("referenceId"));
        }
    }

    public void setReferenceIdAndMidInBody(NativeUserLogoutRequest request) {
        if (request != null && request.getBody() != null) {
            request.getBody().setReferenceId(httpServletRequest().getParameter("referenceId"));
            request.getBody().setMid(httpServletRequest().getParameter("mid"));
        }
    }

    public void setReferenceIdInBody(FetchPcfDetailRequest request) {
        if (request != null && request.getBody() != null) {
            request.getBody().setReferenceId(httpServletRequest().getParameter("referenceId"));
        }
    }

    public void setReferenceIdInBody(FetchBalanceInfoRequest request) {
        if (request != null && request.getBody() != null) {
            request.getBody().setReferenceId(httpServletRequest().getParameter("referenceId"));
        }
    }

    public void setReferenceIdInBody(NativeFetchNBPayChannelRequest request) {
        if (request != null && request.getBody() != null) {
            request.getBody().setReferenceId(httpServletRequest().getParameter("referenceId"));
        }
    }

    public void setReferenceIdInBody(VpaValidateRequest request) {
        if (request != null && request.getBody() != null) {
            request.getBody().setReferenceId(httpServletRequest().getParameter("referenceId"));
        }
    }

    public void setReferenceIdInBody(GenerateOtpRequest request) {
        if (request != null && request.getBody() != null) {
            request.getBody().setReferenceId(httpServletRequest().getParameter("referenceId"));
        }
    }

    public void setReferenceIdInBody(ValidateOtpRequest request) {
        if (request != null && request.getBody() != null) {
            request.getBody().setReferenceId(httpServletRequest().getParameter("referenceId"));
        }
    }

    public void invalidateNativeSessionData(String txnToken, String mid, String orderId) {
        try {
            if (!ff4jUtils.isFeatureEnabledOnMid(mid, TheiaConstant.FF4J.INVALIDATE_REDIS_KEY_AND_FIELDS_NATIVE, false)) {
                return;
            }

            if (StringUtils.isBlank(txnToken)) {
                txnToken = nativeSessionUtil.getTxnToken(mid, orderId);
            }

            List<String> fieldsToBeDeleted = new ArrayList<>();
            fieldsToBeDeleted.add(CASHIER_INFO);
            fieldsToBeDeleted.add(ENTITY_PAYMENT_OPTION);
            fieldsToBeDeleted.add(USER_DETAILS);
            fieldsToBeDeleted.add(EXTEND_INFO);
            fieldsToBeDeleted.add(INITIATE_TXN_RESPONSE);
            nativeSessionUtil.deleteFields(txnToken, fieldsToBeDeleted);

            LOGGER.info("Removed fields for Native on txnToken");
        } catch (Exception e) {
            LOGGER.error("Exception in invalidateNativeSessionData, ", e);
        }
    }

    public void invalidateNativeJsonRequestSessionData(String txnToken, String mid, String orderId) {
        try {

            if (!ff4jUtils.isFeatureEnabledOnMid(mid,
                    TheiaConstant.FF4J.INVALIDATE_REDIS_KEY_AND_FIELDS_NATIVE_JSON_REQUEST, false)) {
                return;
            }

            if (StringUtils.isBlank(txnToken)) {
                txnToken = nativeSessionUtil.getTxnToken(mid, orderId);
            }

            List<String> fieldsToBeDeleted = new ArrayList<>();
            fieldsToBeDeleted.add(CASHIER_INFO);
            fieldsToBeDeleted.add(ENTITY_PAYMENT_OPTION);
            fieldsToBeDeleted.add(USER_DETAILS);
            fieldsToBeDeleted.add(EXTEND_INFO);
            fieldsToBeDeleted.add(INITIATE_TXN_RESPONSE);
            nativeSessionUtil.deleteFields(txnToken, fieldsToBeDeleted);

            LOGGER.info("Removed fields for NativeJsonRequest on txnToken");
        } catch (Exception e) {
            LOGGER.error("Exception in invalidateNativeJsonRequestSessionData, ", e);
        }
    }

    public UserDetailsBiz getUserDetailsBizForFPO(UserDetails userDetails) throws MappingServiceClientException {
        return mappingUtil.mapUserDetails(userDetails);
    }

    public boolean isAddMoneyFlow(String mid, String txnToken, InitiateTransactionRequestBody orderDetail) {
        if (StringUtils.equals(mid, ConfigurationUtil.getTheiaProperty(BizConstant.MP_ADD_MONEY_MID))) {
            return true;
        }
        if (orderDetail == null && StringUtils.isNotBlank(txnToken)) {
            NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.getNativeInitiateRequest(txnToken);
            if (nativeInitiateRequest != null && nativeInitiateRequest.getInitiateTxnReq() != null) {
                orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
            }
        }
        return (orderDetail != null && orderDetail.isNativeAddMoney());
    }
}
