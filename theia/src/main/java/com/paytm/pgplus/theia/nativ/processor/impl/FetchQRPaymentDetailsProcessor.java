package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.wallet.service.IWalletQRCodeService;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.cart.interfaces.IChannelPaymentDetailsService;
import com.paytm.pgplus.facade.cart.model.ChannelPaymentDetailsHeaderRequest;
import com.paytm.pgplus.facade.cart.model.ChannelPaymentDetailsRequest;
import com.paytm.pgplus.facade.wallet.enums.OperationType;
import com.paytm.pgplus.facade.wallet.enums.PlatformType;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoBaseRequest;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoBaseResponse;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoRequestData;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoResponseData;
import com.paytm.pgplus.facade.wallet.services.IWalletQRCodeDetailsService;
import com.paytm.pgplus.http.client.utils.HttpUtils;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.models.PaymentMode;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.common.NativeCashierInfoContainerRequest;
import com.paytm.pgplus.theia.nativ.model.common.PaymodeSequenceEnum;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.model.payview.request.FetchQRPaymentDetailsRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequestBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.FetchQRPaymentDetailsResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.FetchQRPaymentDetailsResponseBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponseBody;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.*;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.services.helper.EnhancedCashierPageServiceHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.ws.rs.core.MediaType;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_GLOBAL_VAULT_ON_STATIC_QR;
import static com.paytm.pgplus.theia.constants.TheiaConstant.AdditionalCallBackParam.LOGO_URL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.REMOVE_HDFC_EMI_IN_RESPONSE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.SUPPORT_ADD_MONEY_PAY_OPTION_IN_OFFLINE_FLOW;

@Service("fetchQRPaymentDetailsRequestProcessor")
public class FetchQRPaymentDetailsProcessor
        extends
        AbstractRequestProcessor<FetchQRPaymentDetailsRequest, FetchQRPaymentDetailsResponse, QRCodeInfoBaseRequest, FetchQRPaymentDetailsResponse> {

    public static final Logger LOGGER = LoggerFactory.getLogger(FetchQRPaymentDetailsProcessor.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(FetchQRPaymentDetailsProcessor.class);

    @Autowired
    @Qualifier("walletQRCodeServiceImpl")
    private IWalletQRCodeService walletQRCodeService;

    @Autowired
    @Qualifier("walletQRCodeDetailsServiceImpl")
    IWalletQRCodeDetailsService walletQrCodeDetailsService;

    @Autowired
    NativeValidationService nativeValidationService;

    @Autowired
    NativePayviewConsultRequestProcessor nativePayviewConsultRequestProcessor;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    EnhancedCashierPageServiceHelper enhancedCashierPageServiceHelper;

    @Autowired
    private IChannelPaymentDetailsService channelPaymentDetailsService;

    @Autowired
    private FF4JUtil ff4JUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("aoaUtils")
    private AOAUtils aoaUtils;

    @Override
    protected QRCodeInfoBaseRequest preProcess(FetchQRPaymentDetailsRequest request) {
        validateRequest(request);
        String qrCodeId = request.getBody().getQrCodeId();
        String orderId = request.getBody().getOrderId();

        if (StringUtils.isBlank(qrCodeId)) {
            LOGGER.error("qrCodeId is null or empty");
            throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                    .isNativeJsonRequest(true).isRetryAllowed(true).setRetryMsg("qrCodeId entered is empty.").build();
        }
        QRCodeInfoBaseRequest qrDetailsRequest = createQRCodeDetailsRequest(request, qrCodeId, orderId);
        return qrDetailsRequest;

    }

    public FetchQRPaymentDetailsProcessor() {
    }

    @Override
    protected FetchQRPaymentDetailsResponse onProcess(FetchQRPaymentDetailsRequest request,
            QRCodeInfoBaseRequest serviceRequest) throws Exception {
        QRCodeInfoBaseResponse response = fetchQRDetailsResponse(serviceRequest);
        String gstBrkUp = response.getResponse().getGstBrkUp();
        if (StringUtils.isNotBlank(gstBrkUp)) {
            // encoding gstBrkUp information
            try {
                response.getResponse().setGstBrkUp(new String(Base64.getEncoder().encode(gstBrkUp.getBytes())));
            } catch (Exception e) {
                LOGGER.error("Exception occured while encoding the gst-break-up info", e);
            }
        }
        NativeCashierInfoResponse nativeCashierInfoResponse = null;
        Object channelPaymentDetails = null;
        String fpoUrl = NATIV_FETCH_PAYMENTOPTIONS_URL;
        String fetchQrDetailsUrl = FETCH_QR_PAYMENT_DETAILS;
        boolean isFetchMLVInfo = false;
        boolean isMLVInfoPresent = false;

        if (response.getResponse() != null
                && TheiaConstant.ExtraConstants.SUCCESS.equalsIgnoreCase(response.getStatus())
                && TheiaConstant.ExtraConstants.P2M.equalsIgnoreCase(response.getResponse().getService())) {
            UserDetailsBiz userDetails = nativeValidationService.validateSSOToken(request.getHead().getToken(),
                    response.getResponse().getMappingId());
            if (userDetails != null && StringUtils.isNotBlank(userDetails.getUserId())
                    && request.getBody().getQueryParams() != null) {
                request.getBody().getQueryParams()
                        .put(TheiaConstant.EventLogConstants.CUST_ID, userDetails.getUserId());
            }

            String mid = response.getResponse().getMappingId();
            if (merchantPreferenceService.isAOAsPaytmPgMID(mid, false)) {
                mid = aoaUtils.getAOAMidForPGMid(mid);
                response.getResponse().setMappingId(mid);
            }
            OfflinePaymentUtils.setMDC(mid, null, null);
            saveLogoUrl(request, response.getResponse());

            if (TheiaConstant.RequestHeaders.Version_V2.equals(MDC.get(VERSION))) {
                fpoUrl = NATIV_FETCH_PAYMENTOPTIONS_URL_V2;
                fetchQrDetailsUrl = FETCH_QR_PAYMENT_DETAILS_V2;
            }

            long fetchPaymentOptionsStartTime = System.currentTimeMillis();
            try {
                // PGP-20304 Only show payment modes available on Enhanced
                // Cashier Page
                List<PaymentMode> disablePayModesList = null;
                List<PaymentMode> enablePayModesList = null;
                String txnToken = (String) nativeSessionUtil.getKey(nativeSessionUtil.getMidOrderIdKeyForRedis(mid,
                        response.getResponse().getOrderId()));
                if (StringUtils.isNotBlank(txnToken)) {
                    InitiateTransactionRequestBody initiateTransactionRequestBody = nativeSessionUtil
                            .getOrderDetail(txnToken);
                    if (initiateTransactionRequestBody != null) {
                        if (CollectionUtils.isNotEmpty(initiateTransactionRequestBody.getDisablePaymentMode()))
                            disablePayModesList = new ArrayList<>(
                                    initiateTransactionRequestBody.getDisablePaymentMode());
                        if (CollectionUtils.isNotEmpty(initiateTransactionRequestBody.getEnablePaymentMode()))
                            enablePayModesList = new ArrayList<>(initiateTransactionRequestBody.getEnablePaymentMode());

                    }
                }

                if (ff4JUtil.isFeatureEnabled(REMOVE_HDFC_EMI_IN_RESPONSE, mid)) {
                    if (isEdcPaymentRequest(response) || !isDynamicQRPayment(response)) {

                        PaymentMode paymentMode = new PaymentMode();
                        paymentMode.setMode(EPayMethod.EMI.getMethod());
                        List<String> channels = new ArrayList<>();
                        channels.add("HDFC");
                        paymentMode.setChannels(channels);

                        if (disablePayModesList == null) {
                            disablePayModesList = new ArrayList<>();
                        }
                        disablePayModesList.add(paymentMode);
                    }
                }

                NativeCashierInfoContainerRequest nativeCashierInfoContainerRequest = createFetchPayOptionsRequest(

                request, mid, response, userDetails, disablePayModesList, enablePayModesList);
                LOGGER.info("Native request received for API: {} is: {}", fpoUrl,
                        nativeCashierInfoContainerRequest.getNativeCashierInfoRequest());
                nativePaymentUtil.logNativeRequests(nativeCashierInfoContainerRequest.getNativeCashierInfoRequest()
                        .getHead().toString(), fpoUrl);
                nativeCashierInfoResponse = nativePayviewConsultRequestProcessor
                        .process(nativeCashierInfoContainerRequest);

                if (nativeCashierInfoResponse != null && nativeCashierInfoResponse.getBody() != null
                        && nativeCashierInfoResponse.getBody().getAddMoneyPayOption() != null) {
                    if ((request.getBody() != null
                            && StringUtils.isBlank(request.getBody().getSupportedPayModesForAddNPay()) || !ff4jUtils
                                .isFeatureEnabledOnMid(mid, SUPPORT_ADD_MONEY_PAY_OPTION_IN_OFFLINE_FLOW, false))
                            && ff4JUtil.isFeatureEnabled(
                                    BizConstant.Ff4jFeature.DISABLE_ADD_MONEY_PAY_OPTION_IN_OFFLINE_FLOW, mid)) {
                        EXT_LOGGER.customInfo("Disabling add money pay option in offline flow for mid: {}", mid);
                        nativeCashierInfoResponse.getBody().setAddMoneyPayOption(null);
                    }
                }

                nativePaymentUtil
                        .logNativeResponse(
                                (response == null ? null : nativePaymentUtil.getResultInfo(nativeCashierInfoResponse
                                        .getBody())), fpoUrl);
                setOrderIdforDynamicQRMerchant(nativeCashierInfoResponse, response);

            } finally {
                nativePaymentUtil.logNativeResponse(fetchPaymentOptionsStartTime, fpoUrl);
            }
            // Code for setting MLV information
            if (nativeCashierInfoResponse != null && nativeCashierInfoResponse.getBody() != null) {
                channelPaymentDetails = nativeCashierInfoResponse.getBody().getChannelDetails();
            }
            isFetchMLVInfo = (request.getBody() != null && request.getBody().isMlvSupported()
                    && StringUtils.isNotBlank(response.getResponse().getKybId()) && StringUtils.isNotBlank(response
                    .getResponse().getShopId())) ? true : false;
        }

        FetchQRPaymentDetailsResponse fetchQRPaymentDetailsResponse = new FetchQRPaymentDetailsResponse();
        FetchQRPaymentDetailsResponseBody fetchQRPaymentDetailsResponseBody = new FetchQRPaymentDetailsResponseBody();
        fetchQRPaymentDetailsResponseBody.setChannelPaymentDetails(channelPaymentDetails);

        if (nativeCashierInfoResponse != null && nativeCashierInfoResponse.getBody() != null) {
            fetchQRPaymentDetailsResponseBody.setNativeCashierInfoResponse(nativeCashierInfoResponse.getBody());
            fetchQRPaymentDetailsResponse.setHead(nativeCashierInfoResponse.getHead());
        }
        fetchQRPaymentDetailsResponseBody.setQrCodeInfoBaseResponse(response);
        fetchQRPaymentDetailsResponse.setBody(fetchQRPaymentDetailsResponseBody);

        request.getBody().setOrderId(MDC.get(TheiaConstant.EventLogConstants.ORDER_ID));
        Map<String, String> metadata = createEventLogMetadata(request.getBody().getQueryParams());
        nativePaymentUtil.logNativeRequests(request.toString(), fetchQrDetailsUrl, metadata);
        isMLVInfoPresent = (fetchQRPaymentDetailsResponseBody.getChannelPaymentDetails()) != null ? true : false;
        String mlvInfoEventLoggerRequest = createEventLogForMLV(isFetchMLVInfo, isMLVInfoPresent);
        nativePaymentUtil.logNativeRequests(mlvInfoEventLoggerRequest);
        return fetchQRPaymentDetailsResponse;
    }

    private boolean isEdcPaymentRequest(QRCodeInfoBaseResponse qrCodeInfoBaseResponse) {
        if (qrCodeInfoBaseResponse != null) {
            if (qrCodeInfoBaseResponse.getResponse() != null) {
                QRCodeInfoResponseData qrCodeInfoResponseData = qrCodeInfoBaseResponse.getResponse();
                if (BooleanUtils.toBoolean(qrCodeInfoResponseData.getIsEdcRequest())
                        || StringUtils.equals(qrCodeInfoResponseData.getProductCode(), "51051000100000000047")) {
                    return true;
                }
            }
        }
        return false;
    }

    private String createEventLogForMLV(boolean isFetchMLVInfo, boolean isMLVInfoPresent) {
        StringBuilder sb = new StringBuilder(TheiaConstant.EventLogConstants.FETCH_MLV_INFO);
        sb.append(":").append(isFetchMLVInfo).append(",").append(TheiaConstant.EventLogConstants.MLV_INFO_PRESENT)
                .append(":").append(isMLVInfoPresent);
        return sb.toString();
    }

    private Object fetchChannelDetails(FetchQRPaymentDetailsRequest request, QRCodeInfoBaseResponse response,
            Object channelPaymentDetails) {
        // fetching MLV details
        // TODO : Create Executor and make call parallel to
        // fetchPaymentOption
        try {
            if (request.getBody().isMlvSupported() && StringUtils.isNotEmpty(response.getResponse().getKybId())
                    && StringUtils.isNotEmpty(response.getResponse().getShopId())) {
                ChannelPaymentDetailsRequest channelRequest = createChannelPaymentDetailsRequest(response.getResponse()
                        .getKybId(), response.getResponse().getShopId(), request.getBody().getAppVersion());
                ChannelPaymentDetailsHeaderRequest headerRequest = createChannelPaymentDetailsHeaderRequest(request
                        .getHead().getToken());
                // LOGGER.info("fetching channel details for trace id :{}",
                // headerRequest.getTraceId());
                channelRequest.setPgMid(response.getResponse().getMappingId());
                channelPaymentDetails = channelPaymentDetailsService.getChannelPaymentDetails(channelRequest,
                        headerRequest, null);
                LOGGER.info("successfully fetched channel details for trace id :{}", headerRequest.getTraceId());
            }
        } catch (Exception e) {
            LOGGER.error("error while fetching channel payment details request data", e);
        }
        return channelPaymentDetails;
    }

    private void saveLogoUrl(FetchQRPaymentDetailsRequest request, QRCodeInfoResponseData qRCodeData) {

        String ssoToken = request.getHead().getToken();
        if (null == qRCodeData.getExtendedInfo()) {
            LOGGER.info("Cannot set Logo URL - ExtendInfo is null");
            return;
        }

        String mid = qRCodeData.getMappingId();
        String logoURL = (String) (qRCodeData.getExtendedInfo().get(LOGO_URL));

        if (StringUtils.isBlank(logoURL)) {
            LOGGER.info("Logo URL is Blank");
            return;
        }
        String keyForMidSSO = nativeSessionUtil.createTokenForMidSSOFlow(ssoToken, mid);
        nativeSessionUtil.setField(keyForMidSSO, LOGO_URL, logoURL);
        LOGGER.info("Logo URL: {} is set in session", logoURL);
    }

    private ChannelPaymentDetailsHeaderRequest createChannelPaymentDetailsHeaderRequest(String token) {
        ChannelPaymentDetailsHeaderRequest request = new ChannelPaymentDetailsHeaderRequest();
        request.setContentType(MediaType.APPLICATION_JSON);
        request.setSessionToken(token);
        request.setTraceId(UUID.randomUUID().toString());
        try {
            request.setUserAgent(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getHeader(TheiaConstant.RequestHeaders.USER_AGENT));
        } catch (Exception e) {
            LOGGER.warn("error while getting user agent for mlv request :{}", e.getMessage());
        }
        return request;
    }

    private ChannelPaymentDetailsRequest createChannelPaymentDetailsRequest(String kybId, String shopId,
            String appVersion) {
        ChannelPaymentDetailsRequest detailsRequest = new ChannelPaymentDetailsRequest();
        detailsRequest.setKybId(kybId);
        detailsRequest.setShopId(shopId);
        detailsRequest.setVersion_id(appVersion);
        return detailsRequest;
    }

    @Override
    protected FetchQRPaymentDetailsResponse postProcess(FetchQRPaymentDetailsRequest request,
            QRCodeInfoBaseRequest serviceRequest, FetchQRPaymentDetailsResponse fetchQRPaymentDetailsResponse) {

        QRCodeInfoBaseResponse qrResponse = fetchQRPaymentDetailsResponse.getBody().getQrCodeInfoBaseResponse();
        NativeCashierInfoResponseBody nativeCashierInfoResponseBody = fetchQRPaymentDetailsResponse.getBody()
                .getNativeCashierInfoResponse();

        String resultStatus = (StringUtils.equals("FAILURE", qrResponse.getStatus())) ? "F" : "S";
        String qrStatusCode = qrResponse.getStatusCode();
        String qrStatusMessage = qrResponse.getStatusMessage();

        if (StringUtils.equals("F", resultStatus) && qrStatusCode == null) {
            qrStatusCode = ResultCode.UNKNOWN_ERROR.getResultCodeId();
            qrStatusMessage = ResultCode.UNKNOWN_ERROR.getResultMsg();

        }

        ResultInfo resultInfo = new ResultInfo(resultStatus, qrStatusCode, qrStatusMessage);

        if (nativeCashierInfoResponseBody != null) {
            resultInfo = nativeCashierInfoResponseBody.getResultInfo();
            /*
             * setting it null as sending the common result info for both the
             * API's
             */
            nativeCashierInfoResponseBody.setResultInfo(null);
            EXT_LOGGER.customInfo("Setting the final result Info of QRPaymentDetails as of Fetch Payment Options");
        }

        fetchQRPaymentDetailsResponse.getBody().setResultInfo(resultInfo);
        if (fetchQRPaymentDetailsResponse.getHead() != null) {
            fetchQRPaymentDetailsResponse.getHead().setRequestId(request.getHead().getRequestId());
        }
        return fetchQRPaymentDetailsResponse;
    }

    public TokenRequestHeader getTokenRequestHeader(String channel) {
        TokenRequestHeader tokenRequestHeader = new TokenRequestHeader();
        tokenRequestHeader.setVersion("v1");
        tokenRequestHeader.setRequestTimestamp(Long.toString(System.currentTimeMillis()));
        if (StringUtils.isNotBlank(channel)) {
            tokenRequestHeader.setChannelId(EChannelId.valueOf(channel.toUpperCase()));
        } else {
            tokenRequestHeader.setChannelId(EChannelId.WEB);
        }

        return tokenRequestHeader;

    }

    private QRCodeInfoBaseRequest createQRCodeDetailsRequest(FetchQRPaymentDetailsRequest request, String qrCodeId,
            String orderId) {
        QRCodeInfoRequestData requestData = new QRCodeInfoRequestData(qrCodeId);
        requestData.setOrderId(orderId);
        QRCodeInfoBaseRequest qrDetailsRequest = new QRCodeInfoBaseRequest();
        qrDetailsRequest.setRequest(requestData);
        qrDetailsRequest.setOperationType(OperationType.QR_CODE.getValue());
        qrDetailsRequest.setPlatformName(PlatformType.PAYTM.getValue());
        qrDetailsRequest.setIpAddress(forwardIPDetails());
        qrDetailsRequest.setSsoToken(request.getHead().getToken());

        return qrDetailsRequest;
    }

    private String forwardIPDetails() {
        try {
            InetAddress ia = HttpUtils.getLocalHostLANAddress();
            if (ia != null) {
                return ia.getHostAddress();
            }
        } catch (UnknownHostException e) {
            LOGGER.debug("Unable to get ip address", e);
        }
        return null;
    }

    private NativeCashierInfoContainerRequest createFetchPayOptionsRequest(FetchQRPaymentDetailsRequest request,
            String mid, QRCodeInfoBaseResponse qrCodeInfoBaseResponse, UserDetailsBiz userDetails,
            List<PaymentMode> disablePayModesList, List<PaymentMode> enablePayModesList) throws Exception {
        TokenRequestHeader tokenRequestHeader = getTokenRequestHeader(request.getHead().getChannelId().toString());
        tokenRequestHeader.setTokenType(TokenType.SSO);
        tokenRequestHeader.setToken(request.getHead().getToken());
        tokenRequestHeader.setVersion(MDC.get(VERSION));

        NativeCashierInfoRequest nativeCashierInfoRequest = new NativeCashierInfoRequest();
        NativeCashierInfoRequestBody nativeCashierInfoRequestBody = new NativeCashierInfoRequestBody();
        nativeCashierInfoRequestBody.setMid(mid);
        nativeCashierInfoRequestBody.setUpiLiteEligible(request.getBody().getIsLiteEligible());

        if (!isDynamicQRPayment(qrCodeInfoBaseResponse)
                && ff4jUtils.isFeatureEnabledOnMid(mid, ENABLE_GLOBAL_VAULT_ON_STATIC_QR, false)) {
            nativeCashierInfoRequestBody.setStaticQrCode(Boolean.TRUE);
        }

        if (StringUtils.isNotBlank(request.getBody().getOrderId())) {
            nativeCashierInfoRequestBody.setOrderId(request.getBody().getOrderId());
        } else {
            nativeCashierInfoRequestBody.setGenerateOrderId(String.valueOf(true));
        }

        if (CollectionUtils.isNotEmpty(disablePayModesList)) {
            if (CollectionUtils.isNotEmpty(nativeCashierInfoRequestBody.getDisablePaymentMode())) {
                nativeCashierInfoRequestBody.getDisablePaymentMode().addAll(disablePayModesList);
            } else {
                nativeCashierInfoRequestBody.setDisablePaymentMode(disablePayModesList);
            }
        }

        if (CollectionUtils.isNotEmpty(enablePayModesList)) {
            if (CollectionUtils.isNotEmpty(nativeCashierInfoRequestBody.getEnablePaymentMode())) {
                nativeCashierInfoRequestBody.getEnablePaymentMode().addAll(enablePayModesList);
            } else {
                nativeCashierInfoRequestBody.setEnablePaymentMode(enablePayModesList);
            }
        }

        if (qrCodeInfoBaseResponse.getResponse() != null)
            nativeCashierInfoRequestBody.setProductCode(qrCodeInfoBaseResponse.getResponse().getProductCode());
        nativeCashierInfoRequest.setHead(tokenRequestHeader);

        if (isDynamicQRPayment(qrCodeInfoBaseResponse)) {
            LOGGER.info("Dynamic QR FetchPayOptions with OrderId {}", qrCodeInfoBaseResponse.getResponse()
                    .getMerchantTransId());
            OfflinePaymentUtils.updateOrderIdInMDC(qrCodeInfoBaseResponse.getResponse().getMerchantTransId());
            nativeCashierInfoRequestBody.setGenerateOrderId(String.valueOf(false));
            nativeCashierInfoRequestBody.setSubwalletAmount(qrCodeInfoBaseResponse.getResponse()
                    .getSubwalletWithdrawMaxAmountDetails());

            /*
             * if(enhancePageDynamicQR(mid,qrCodeInfoBaseResponse.getResponse().
             * getMerchantTransId())){
             * LOGGER.info("Enhance Page Dynamic QR with OrderId {} "
             * ,qrCodeInfoBaseResponse.getResponse().getMerchantTransId());
             * List<PaymentMode> paymentModes=new ArrayList<>(); List<String>
             * channels= Arrays.asList("UPIPUSH","UPIPUSHEXPRESS"); PaymentMode
             * paymentMode=new PaymentMode(); paymentMode.setChannels(channels);
             * paymentModes.add(paymentMode);
             * nativeCashierInfoRequestBody.setEnablePaymentMode(paymentModes);
             * }
             */
        }

        // For Channels API in FetchChannelDetailsTask
        if (request.getBody() != null) {
            QRCodeInfoResponseData qrCodeData = qrCodeInfoBaseResponse.getResponse();
            QRCodeInfoResponseData qrCodeInfo = new QRCodeInfoResponseData();
            if (request.getBody().isMlvSupported()) {

                // mlvSupported & appVersion
                nativeCashierInfoRequestBody.setMlvSupported(request.getBody().isMlvSupported());
                nativeCashierInfoRequestBody.setAppVersion(request.getBody().getAppVersion());

                // kybId, shopId
                qrCodeInfo.setKybId(qrCodeData.getKybId());
                qrCodeInfo.setShopId(qrCodeData.getShopId());
            }
            nativeCashierInfoRequestBody.setFetchPaytmInstrumentsBalance(request.getBody()
                    .isFetchPaytmInstrumentsBalance());

            qrCodeInfo.setTxnAmount(qrCodeData.getTxnAmount());
            qrCodeInfo.setQrCodeId(qrCodeData.getQrCodeId());
            nativeCashierInfoRequestBody.setqRCodeInfo(qrCodeInfo);
            nativeCashierInfoRequestBody.setSupportedPayModesForAddNPay(request.getBody()
                    .getSupportedPayModesForAddNPay());
        }
        nativeCashierInfoRequestBody.setPaymodeSequenceEnum(PaymodeSequenceEnum.OFFLINE);
        nativeCashierInfoRequestBody.setOfflineFlow(Boolean.TRUE);
        nativeCashierInfoRequestBody.setTwoFADetails(request.getBody().getTwoFADetails());
        nativeCashierInfoRequest.setBody(nativeCashierInfoRequestBody);
        NativePersistData nativePersistData = new NativePersistData(userDetails);

        NativeCashierInfoContainerRequest nativeCashierInfoContainerRequest = new NativeCashierInfoContainerRequest(
                nativeCashierInfoRequest, nativePersistData);
        nativeCashierInfoContainerRequest.setFetchQRDetailsRequest(true);
        return nativeCashierInfoContainerRequest;
    }

    /*
     * private boolean enhancePageDynamicQR(String mid, String orderId) {
     * EnhanceCashierPageCachePayload enhanceCashierPageCachePayload =
     * (EnhanceCashierPageCachePayload) nativeSessionUtil
     * .getKey(enhancedCashierPageServiceHelper.fetchRedisKey(mid, orderId));
     * return enhanceCashierPageCachePayload!=null; }
     */

    private void validateRequest(FetchQRPaymentDetailsRequest request) {
        String tokenType = request.getHead().getTokenType().getType();
        if (StringUtils.isBlank(tokenType) || !StringUtils.equals(TokenType.SSO.getType(), tokenType)
                || StringUtils.isBlank(request.getHead().getToken())) {
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }

    }

    private QRCodeInfoBaseResponse fetchQRDetailsResponse(QRCodeInfoBaseRequest serviceRequest) throws Exception {
        long fetchQRDetailsStartTime = System.currentTimeMillis();
        try {
            QRCodeInfoBaseResponse qrResponse = walletQrCodeDetailsService.getQRCodeInfoByQrCodeId(serviceRequest);
            return qrResponse;
        } finally {
            nativePaymentUtil.logNativeResponse(fetchQRDetailsStartTime, WALLET_QR_CODE);

        }

    }

    private void setOrderIdforDynamicQRMerchant(NativeCashierInfoResponse nativeCashierInfoResponse,
            QRCodeInfoBaseResponse qrResponse) {
        String merchantTransId = qrResponse.getResponse().getMerchantTransId();
        if (nativeCashierInfoResponse != null && StringUtils.isNotBlank(merchantTransId)) {
            nativeCashierInfoResponse.getBody().setOrderId(merchantTransId);
        }

    }

    private boolean isDynamicQRPayment(QRCodeInfoBaseResponse response) {
        if (StringUtils.isNotBlank(response.getResponse().getMerchantTransId())) {
            return true;
        }
        return false;
    }

    private Map<String, String> createEventLogMetadata(Map<String, String> queryParams) {
        Map<String, String> metadata = new HashMap<>();

        if (queryParams != null) {
            metadata.put(TheiaConstant.EventLogConstants.DEVICE_ID,
                    queryParams.get(TheiaConstant.OfflineQueryParamConstrants.DEVICE_IDENTIFIER));
            metadata.put(TheiaConstant.EventLogConstants.DEVICE_CATEGORY,
                    queryParams.get(TheiaConstant.SarvatraV4QueryParamConstrants.CLIENT));
            metadata.put(TheiaConstant.EventLogConstants.OS_VERSION,
                    queryParams.get(TheiaConstant.OfflineQueryParamConstrants.OS_VERSION));
            metadata.put(TheiaConstant.EventLogConstants.DEVICE_NAME,
                    queryParams.get(TheiaConstant.OfflineQueryParamConstrants.DEVICE_NAME));
            metadata.put(TheiaConstant.EventLogConstants.NETWORK_TYPE,
                    queryParams.get(TheiaConstant.OfflineQueryParamConstrants.NETWORK_TYPE));
            metadata.put(TheiaConstant.EventLogConstants.CUST_ID,
                    queryParams.get(TheiaConstant.EventLogConstants.CUST_ID));
        }

        metadata.put(TheiaConstant.EventLogConstants.MID, MDC.get(TheiaConstant.EventLogConstants.MID));
        metadata.put(TheiaConstant.EventLogConstants.ORDER_ID, MDC.get(TheiaConstant.EventLogConstants.ORDER_ID));

        return metadata;
    }

}
