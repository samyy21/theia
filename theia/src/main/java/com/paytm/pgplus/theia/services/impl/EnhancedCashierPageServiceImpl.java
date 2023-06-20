package com.paytm.pgplus.theia.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.wallet.QRCodeDetailsResponse;
import com.paytm.pgplus.biz.core.validator.BizParamValidator;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.exception.BizMerchantVelocityBreachedException;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.utils.NpciHealthUtil;
import com.paytm.pgplus.biz.utils.ObjectMapperUtil;
import com.paytm.pgplus.cache.model.*;
import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.checksum.ValidateChecksum;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.constant.CommonConstant;
import com.paytm.pgplus.common.enums.*;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.common.model.link.PaymentFormDetails;
import com.paytm.pgplus.common.util.MaskingUtil;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.enums.QrType;
import com.paytm.pgplus.facade.enums.UIMicroserviceUrl;
import com.paytm.pgplus.facade.payment.models.FeeRateFactors;
import com.paytm.pgplus.facade.user.models.*;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatraV4;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.IConfigurationService;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.models.UserInfo;
import com.paytm.pgplus.models.*;
import com.paytm.pgplus.payloadvault.theia.constant.CommonConstants;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.EnhancedCashierPageKeys;
import com.paytm.pgplus.payloadvault.theia.enums.UserSubWalletType;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.MappingMerchantUrlInfo;
import com.paytm.pgplus.pgproxycommon.models.MerchantUrlInput;
import com.paytm.pgplus.pgproxycommon.utils.MobileNumberUtils;
import com.paytm.pgplus.pgproxycommon.utils.TaskFlowUtils;
import com.paytm.pgplus.request.*;
import com.paytm.pgplus.response.InitiateTransactionResponse;
import com.paytm.pgplus.response.InitiateTransactionResponseBody;
import com.paytm.pgplus.response.SubscriptionTransactionResponse;
import com.paytm.pgplus.response.SubscriptionTransactionResponseBody;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import com.paytm.pgplus.theia.aspects.LocaleFieldAspect;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.cache.IMerchantUrlService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.controllers.helper.ProcessTransactionControllerHelper;
import com.paytm.pgplus.theia.enums.PayModeOrder;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.KybServiceException;
import com.paytm.pgplus.theia.exceptions.PWPPromoServiceException;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.helper.MobileMaskHelper;
import com.paytm.pgplus.theia.helper.UIMicroserviceHelper;
import com.paytm.pgplus.theia.models.ModifiableHttpServletRequest;
import com.paytm.pgplus.theia.models.UserAgentInfo;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.models.uimicroservice.request.UIMicroserviceRequest;
import com.paytm.pgplus.theia.models.uimicroservice.response.UIMicroserviceResponse;
import com.paytm.pgplus.theia.nativ.enums.AuthMode;
import com.paytm.pgplus.theia.nativ.enums.PayModeType;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.PCF.PCFFeeCharges;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.model.common.*;
import com.paytm.pgplus.theia.nativ.model.enhancenative.AppInvokeRedirectionUrlData;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.*;
import com.paytm.pgplus.theia.nativ.model.payment.request.NativePaymentRequest;
import com.paytm.pgplus.theia.nativ.model.payment.request.NativePaymentRequestBody;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequestBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod;
import com.paytm.pgplus.theia.nativ.model.payview.response.UPI;
import com.paytm.pgplus.theia.nativ.model.payview.response.*;
import com.paytm.pgplus.theia.nativ.model.preauth.PreAuthDetails;
import com.paytm.pgplus.theia.nativ.model.vpa.details.FetchVpaDetailsRequest;
import com.paytm.pgplus.theia.nativ.model.vpa.details.VpaDetailsResponse;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory.RequestType;
import com.paytm.pgplus.theia.nativ.service.IMerchantStaticConfigService;
import com.paytm.pgplus.theia.nativ.subscription.INativeSubscriptionHelper;
import com.paytm.pgplus.theia.nativ.utils.*;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.exceptions.PaymentRequestProcessingException;
import com.paytm.pgplus.theia.offline.facade.ICommonFacade;
import com.paytm.pgplus.theia.offline.utils.CustomObjectMapperUtil;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.AbstractPaymentService;
import com.paytm.pgplus.theia.services.helper.EnhancedCashierPageDisplayIdGenerator;
import com.paytm.pgplus.theia.services.helper.EnhancedCashierPageServiceHelper;
import com.paytm.pgplus.theia.services.helper.FF4JHelper;
import com.paytm.pgplus.theia.session.utils.OAuthInfoSessionUtil;
import com.paytm.pgplus.theia.utils.*;
import com.paytm.pgplus.theia.utils.helper.ProcessTransactionHelper;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.EnhancedCashierPageKeys.ZEST;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.EnhancedCashierPageKeys.ZESTMONEY;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.GvConsent.PTR_URL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.MerchantPreference.PreferenceKeys.MASK_MOBILE_ON_CASHIER_PAGE_ENABLED;
import static com.paytm.pgplus.theia.constants.TheiaConstant.MerchantPreference.PreferenceKeys.UN_GROUPED_PAYMODES_DISABLED;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.REFERER;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.CHECKSUMHASH;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.LINK_BASED_KEY;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.CHECKOUT;
import static com.paytm.pgplus.theia.offline.enums.ResultCode.SUCCESS_IDEMPOTENT_ERROR;

@Service("enhancedCashierPageService")
public class EnhancedCashierPageServiceImpl extends AbstractPaymentService {

    private static final long serialVersionUID = -3059884766173716079L;
    private static final String PAYMENT_MODE_ONLY_FLAG = "YES";
    private static final String PAYMENT_MODE_SPLIT_REGEX = "\\s*,\\s*";
    private static final String EMI_OPTIONS_BANK_SPLIT_REGEX = "\\s*;\\s*";
    private static final String EMI_CHANNEL_DISABLE_SUFIX = "-DROP_ALL";
    private static final String EMI_CHANNEL_ENABLE_SUFIX = "-SHOW_ALL";
    private static final String SUCCESS_RESPONSE_CODE = "0000";
    private static final String SUCCESS = "SUCCESS";
    private static final String VPA_TYPE = "upipush";
    private static final String VPA_AUTH_MODE = "USRPWD";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(EnhancedCashierPageServiceImpl.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(EnhancedCashierPageServiceImpl.class);
    @Autowired
    EnhancedCashierPageServiceHelper enhancedCashierPageServiceHelper;
    @Autowired
    OAuthInfoSessionUtil oAuthInfoSessionUtil;
    @Autowired
    private RequestProcessorFactory requestProcessorFactory;
    @Autowired
    private LocalizationUtil localizationUtil;
    @Autowired
    private LocaleFieldAspect localeFieldAspect;
    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;
    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;
    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;
    @Autowired
    @Qualifier("merchantUrlService")
    private IMerchantUrlService merchantUrlService;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    @Qualifier("nativeInitiateUtil")
    private NativeInitiateUtil nativeInitiateUtil;

    @Autowired
    @Qualifier("hybridDisablingUtil")
    private HybridDisablingUtil hybridDisablingUtil;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    @Qualifier("configurationService")
    private IConfigurationService configurationServiceImpl;

    @Autowired
    private ResponseCodeUtil responseCodeUtil;

    @Autowired
    @Qualifier("aoaUtils")
    private AOAUtils aoaUtils;

    @Autowired
    private LinkPaymentUtil linkPaymentUtil;

    @Autowired
    private UPIHandleUtil upiHandleUtil;

    @Autowired
    private UPIRegionalNameUtil upiRegionalNameUtil;

    @Autowired
    private AddMoneyToGvConsentUtil addMoneyToGvConsentUtil;

    @Autowired
    @Qualifier("processTransactionControllerHelper")
    ProcessTransactionControllerHelper processTransactionControllerHelper;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    private IMerchantDataService merchantDataService;

    @Autowired
    @Qualifier("commonFacade")
    private ICommonFacade commonFacade;

    @Autowired
    private NpciHealthUtil npciHealthUtil;

    @Autowired
    @Qualifier("processTransactionUtil")
    private ProcessTransactionUtil processTransactionUtil;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    private FF4JUtil ff4JUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    private UIMicroserviceHelper uiMicroserviceHelper;

    @Autowired
    private FF4JHelper ff4JHelper;

    @Autowired
    private NativeRetryUtil nativeRetryUtil;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private MobileMaskHelper mobileMaskHelper;

    @Autowired
    private IMerchantStaticConfigService merchantStaticConfigService;

    @Autowired
    @Qualifier("nativeSubscriptionHelper")
    private INativeSubscriptionHelper nativeSubscriptionHelper;

    public static void setMerchantAcceptForPostpaid(NativeCashierInfoResponse nativeCashierInfoResponse,
            EnhancedCashierPage enhancedCashierPage) {
        PayMethod payMethod = nativeCashierInfoResponse.getBody().getMerchantPayOption().getPayMethods().stream()
                .filter(s -> EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod().equals(s.getPayMethod())).findAny()
                .orElse(null);
        if (payMethod != null && payMethod.getIsDisabled() != null
                && Boolean.valueOf(((BalanceStatusInfo) payMethod.getIsDisabled()).getMerchantAccept())) {
            enhancedCashierPage.setMerchantAcceptPostpaid(true);
        }
    }

    private void setRefererURLInCache(String txnToken) {
        if (StringUtils.isNotBlank(txnToken)) {

            HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest();
            String refererURL = servletRequest.getHeader(REFERER);

            if (isAPPInvoke()) {
                LOGGER.info("EnhancedCashierPage render | appinvoke case - setting in cache | refererURL: {}",
                        refererURL);
            } else {
                LOGGER.info("EnhancedCashierPage render | setting in cache | refererURL: {}", refererURL);
            }
            nativeSessionUtil.setRefererURL(txnToken, refererURL);
        }
    }

    @Override
    public PageDetailsResponse processPaymentRequest(PaymentRequestBean requestData, Model responseModel)
            throws TheiaServiceException {

        InitiateTransactionRequest initiateTransactionRequest = null;
        InitiateTransactionResponse initiateTransactionResponse = null;

        InitiateTransactionRequestBody orderDetail = null;

        boolean appInvokeV2ff4j = ff4JUtil.isFeatureEnabled(THEIA_AUTO_APP_INVOKE_PHASE2, requestData.getMid());

        try {

            /*
             * This is when API /api/v1/showPaymentPage receives request with
             * "mid", "orderId" and "txnToken"
             */
            if (isNativeAppInvokeRequest(requestData)) {

                orderDetail = nativeInitiateUtil.fetchInitiateReqBodyFromCache(requestData.getTxnToken());
                requestData.setPaymodeSequence(orderDetail.getPaymodeSequence());

                nativeInitiateUtil.validateMidOrderIdAppInvoke(orderDetail.getMid(), orderDetail.getOrderId());
                nativeInitiateUtil.transformInitReqBodyToPaymentReqBean(orderDetail, requestData);

                initiateTransactionResponse = createInitTxnResponse(requestData.getTxnToken(), orderDetail);

                ERequestType requestType = ERequestType.getByRequestType(orderDetail.getRequestType());
                if (requestType != null && ERequestType.isSubscriptionCreationRequest(requestType.getType())) {
                    requestData.setRequestType(requestType.getType());
                    LOGGER.info("setting requestType:{} sent in orderDetail", requestType.getType());
                    setSubscriptionDataForAppInvokeFlow(orderDetail, initiateTransactionResponse, requestData);
                }

            } else {

                boolean isSdkProcessTxn = BooleanUtils.isTrue(requestData.isSdkProcessTxnFlow());

                try {
                    if (ERequestType.SUBSCRIBE.getType().equals(requestData.getRequestType())) {
                        try {
                            // Create Subscription request
                            SubscriptionTransactionRequest subscriptionTransactionRequest = cerateSubscriptionTransactionRequest(requestData);
                            LOGGER.info("CreateSubscription Request: {}", subscriptionTransactionRequest);

                            IRequestProcessor<SubscriptionTransactionRequest, SubscriptionTransactionResponse> requestProcessor = requestProcessorFactory
                                    .getRequestProcessor(RequestProcessorFactory.RequestType.NATIVE_SUBSCRIPTION);
                            SubscriptionTransactionResponse subscriptionTransactionResponse = requestProcessor
                                    .process(subscriptionTransactionRequest);
                            initiateTransactionResponse = new InitiateTransactionResponse();
                            initiateTransactionResponse.setHead(subscriptionTransactionResponse.getHead());
                            initiateTransactionResponse.setBody(subscriptionTransactionResponse.getBody());
                            initiateTransactionRequest = subscriptionTransactionRequest;
                            requestData.setSubscriptionID(initiateTransactionResponse.getBody().getSubscriptionId());
                            requestData.setSubsPaymentMode(subscriptionTransactionRequest.getBody()
                                    .getSubscriptionPaymentMode());

                            StringBuilder key = new StringBuilder(subscriptionTransactionRequest.getBody()
                                    .getRequestType()).append(initiateTransactionResponse.getBody().getTxnToken());
                            SubscriptionResponse subscriptionResponse = (SubscriptionResponse) theiaTransactionalRedisUtil
                                    .get(key.toString());
                            if (StringUtils.isNotBlank(subscriptionResponse.getPaymentMid())
                                    && StringUtils.isNotBlank(subscriptionResponse.getPaymentOrderId())) {
                                requestData.setAutoRefund(true);
                                requestData.setPaymentMid(subscriptionResponse.getPaymentMid());
                                requestData.setPaymentOrderId(subscriptionResponse.getPaymentOrderId());
                            }

                            LOGGER.info("CreateSubscription response returned for enhancedCashier flow is: {}",
                                    subscriptionTransactionResponse);
                            if (isSdkProcessTxn) {
                                // setting it false by default as auto-app
                                // invoke is not allowed for subs
                                initiateTransactionResponse.getBody().setAutoAppInvokeAllowed(false);
                                PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
                                pageDetailsResponse.setSuccessfullyProcessed(true);
                                pageDetailsResponse.setS2sResponse(ObjectMapperUtil.getObjectMapper()
                                        .writeValueAsString(initiateTransactionResponse));
                                return pageDetailsResponse;
                            }
                        } catch (BaseException e) {
                            if (!isSdkProcessTxn) {
                                throw e;
                            }
                            LOGGER.error("Exception occured in initiate txn request  : {} ", e);
                            // It is used to return json response for
                            // sdk/processTransaction using
                            // NativeRestExceptionHandler
                            return fetchPageDetailsResponseForSdkPtc(e);
                        } catch (Exception e) {
                            if (!isSdkProcessTxn) {
                                throw e;
                            }
                            LOGGER.error("Exception occured in initiate txn request  : {} ", e);
                            return new PageDetailsResponse(false);
                        }
                    } else if (isSdkProcessTxn) {
                        try {
                            initiateTransactionRequest = createInitiateTransactionRequest(requestData);
                            initiateTransactionResponse = processInitiateTransaction(initiateTransactionRequest, false);

                            if (null != initiateTransactionResponse) {
                                UserAgentInfo userAgentInfo = BrowserUtil.getUserAgentInfo();
                                boolean isAutoAppInvokeAllowed = processTransactionControllerHelper
                                        .checkIfAutoAppInvokeAllowed(requestData, userAgentInfo, false,
                                                appInvokeV2ff4j, isSdkProcessTxn);
                                initiateTransactionResponse.getBody().setAutoAppInvokeAllowed(isAutoAppInvokeAllowed);
                                PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
                                pageDetailsResponse.setSuccessfullyProcessed(true);
                                pageDetailsResponse.setS2sResponse(ObjectMapperUtil.getObjectMapper()
                                        .writeValueAsString(initiateTransactionResponse));
                                return pageDetailsResponse;

                            }
                        } catch (BaseException e) {
                            LOGGER.error("Exception occured in initiate txn request  : {} ", e);
                            // It is used to return json response for
                            // sdk/processTransaction using
                            // NativeRestExceptionHandler
                            return fetchPageDetailsResponseForSdkPtc(e);
                        } catch (Exception e) {
                            LOGGER.error("Exception occured in initiate txn request  : {} ", e);
                            return new PageDetailsResponse(false);
                        }
                    } else {
                        initiateTransactionRequest = createInitiateTransactionRequest(requestData);
                        /*
                         * boolean createorderRequiredInInitiate = true; if
                         * (StringUtils.isNotBlank(requestData.getLinkId()) ||
                         * StringUtils.isNotBlank(requestData.getInvoiceId())) {
                         * createorderRequiredInInitiate = false; }
                         */

                        if (!appInvokeV2ff4j) {
                            UserAgentInfo userAgentInfo = BrowserUtil.getUserAgentInfo();
                            boolean isAutoAppInvokeAllowed = processTransactionControllerHelper
                                    .checkIfAutoAppInvokeAllowed(requestData, userAgentInfo, true, appInvokeV2ff4j,
                                            isSdkProcessTxn);
                            if (isAutoAppInvokeAllowed) {
                                initiateTransactionResponse = processInitiateTransaction(initiateTransactionRequest,
                                        true);
                                String redirectionUrl = getRedirectUrlForAppInvoke(
                                        initiateTransactionRequest.getBody(), initiateTransactionResponse,
                                        userAgentInfo);
                                if (StringUtils.isNotBlank(redirectionUrl)) {
                                    com.paytm.pgplus.biz.utils.EventUtils
                                            .pushTheiaEvents(EventNameEnum.AUTO_APP_INVOKE);
                                    PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
                                    pageDetailsResponse.setSuccessfullyProcessed(true);
                                    pageDetailsResponse.setRedirectionUrl(redirectionUrl);
                                    return pageDetailsResponse;
                                }
                            } else {
                                initiateTransactionResponse = processInitiateTransaction(initiateTransactionRequest,
                                        false);
                            }
                        } else {
                            initiateTransactionResponse = processInitiateTransaction(initiateTransactionRequest, false);
                        }
                        if (requestData.isNeedAppIntentEndpoint()) {
                            initiateTransactionResponse.getHead().setSignature(
                                    generateCheckSum(requestData.getMid(), initiateTransactionResponse));
                            PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
                            pageDetailsResponse.setSuccessfullyProcessed(true);
                            pageDetailsResponse.setS2sResponse(ObjectMapperUtil.getObjectMapper().writeValueAsString(
                                    initiateTransactionResponse));
                            return pageDetailsResponse;
                        }
                    }

                } catch (BaseException ex) {
                    LOGGER.error("Error while validating initiateTransactionRequest due to : {}", ex);
                    String htmlPage;
                    if (ex.getResultInfo() != null) {
                        htmlPage = merchantResponseService.processMerchantFailResponse(requestData, ex.getResultInfo());
                    } else {
                        htmlPage = merchantResponseService.processMerchantFailResponse(requestData,
                                ResponseConstants.SYSTEM_ERROR);
                    }

                    PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
                    pageDetailsResponse.setSuccessfullyProcessed(false);
                    pageDetailsResponse.setHtmlPage(htmlPage);

                    if (StringUtils.isNotBlank(requestData.getLinkId())
                            || StringUtils.isNotBlank(requestData.getInvoiceId())) {
                        linkPaymentUtil.setpageDetailsResponseForLinkBasedPayment(pageDetailsResponse, requestData, ex
                                .getResultInfo().getResultMsg(), ex.getResultInfo().getResultCode());
                    }

                    return pageDetailsResponse;

                }

                if (initiateTransactionResponse == null
                        || StringUtils.isBlank(initiateTransactionResponse.getBody().getTxnToken())) {
                    throw new TheiaServiceException("initiateTransactionResponse is null");
                }

                initiateTransactionResponse.getBody().setCallbackUrl(getCallBackUrl(initiateTransactionRequest));
                if (StringUtils.isBlank(requestData.getCallbackUrl())) {
                    requestData.setCallbackUrl(getCallBackUrl(initiateTransactionRequest));
                }
            }

            // check if not appInvoke request, then only -> create and send
            // appLink
            if (appInvokeV2ff4j && null != initiateTransactionResponse && isNotAppLink()) {
                UserAgentInfo userAgentInfo = BrowserUtil.getUserAgentInfo();
                InitiateTransactionRequestBody initiateTransactionRequestBody = Optional
                        .ofNullable(initiateTransactionRequest).map(InitiateTransactionRequest::getBody)
                        .orElse(orderDetail);
                boolean isAutoAppInvokeAllowed = processTransactionControllerHelper.checkIfAutoAppInvokeAllowed(
                        requestData, userAgentInfo, true, appInvokeV2ff4j, false);
                if (isAutoAppInvokeAllowed) {
                    boolean isV3AppInvokeRequired = processTransactionUtil.isV3AppInvokeRequired(requestData.getMid());
                    String redirectionUrl = isV3AppInvokeRequired ? getRedirectUrlV3ForAppInvoke(
                            initiateTransactionRequestBody, initiateTransactionResponse, userAgentInfo)
                            : getRedirectUrlForAppInvoke(initiateTransactionRequestBody, initiateTransactionResponse,
                                    userAgentInfo);
                    if (StringUtils.isNotBlank(redirectionUrl)) {
                        com.paytm.pgplus.biz.utils.EventUtils.pushTheiaEvents(EventNameEnum.AUTO_APP_INVOKE);
                        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
                        pageDetailsResponse.setSuccessfullyProcessed(true);
                        pageDetailsResponse.setRedirectionUrl(redirectionUrl);
                        return pageDetailsResponse;
                    }
                }
            }

            requestData.setTxnToken(initiateTransactionResponse.getBody().getTxnToken());

            setRefererURLInCache(initiateTransactionResponse.getBody().getTxnToken());

            // checks if createOrder is Required
            if (isCreateOrderOrTopupRequired(initiateTransactionResponse, requestData.getMid())) {
                // For cc bill payment setting flag
                if (StringUtils.isNotBlank(requestData.getCreditCardBillNo())) {
                    requestData.setCcBillPaymentRequest(Boolean.TRUE);
                }
                // create order
                if (isParallelizedFlowForEnhancedOnly(requestData)) {
                    if (TheiaConstant.RequestTypes.ADD_MONEY.equalsIgnoreCase(requestData.getRequestType())) {
                        LOGGER.info("CREATE TOPUP REQUIRED,PARALLEL EXECUTION");
                        requestData.setCreateTopupRequired(true);
                    } else if (requestData.isPreAuth()) {
                        LOGGER.info("PREAUTH REQUEST, NO CREATE ORDER REQUIRED");
                        requestData.setCreateOrderRequired(false);
                    } else if (orderDetail != null
                            && orderDetail.getLinkDetailsData() != null
                            && (TheiaConstant.RequestTypes.NATIVE_MF.equals(orderDetail.getLinkDetailsData()
                                    .getSubRequestType()) || TheiaConstant.RequestTypes.NATIVE_ST.equals(orderDetail
                                    .getLinkDetailsData().getSubRequestType()))) {
                        LOGGER.info("CREATE ORDER NOT REQUIRED IN LINK BASED PAYMENT WITH SUBREQUEST TYPE MF,ST,PARALLEL EXECUTION");
                        requestData.setCreateOrderRequired(false);
                    } else {
                        LOGGER.info("CREATE ORDER REQUIRED,PARALLEL EXECUTION");
                        requestData.setCreateOrderRequired(true);
                    }
                } else if (TheiaConstant.RequestTypes.ADD_MONEY.equalsIgnoreCase(requestData.getRequestType())) {
                    nativeInitiateUtil.createTopup(initiateTransactionResponse.getBody().getTxnToken(), requestData);
                } else {
                    /*
                     * Do Create order for: No parallelization
                     */
                    LOGGER.info("CREATE ORDER REQUIRED,DOING SEQUENTIALLY");
                    nativeInitiateUtil.createOrder(initiateTransactionResponse, requestData);
                }
            }

            return fetchPageDetailsResponse(requestData, initiateTransactionResponse);

        } catch (NativeFlowException nfe) {
            if (orderDetail != null) {
                nfe.setOrderDetail(orderDetail);
            }
            throw nfe;
        } catch (BizMerchantVelocityBreachedException e) {
            throw e;
        } catch (PaymentRequestProcessingException ex) {
            LOGGER.error("Error while getting initiateTransactionResponse due to : {}", ex);
            String htmlPage = "";
            if (requestData.isCcBillPaymentRequest()) {
                htmlPage = merchantResponseService.processMerchantFailResponse(requestData, ex.getResultInfo());
            } else if (ex.getResponseConstants() != null) {
                htmlPage = merchantResponseService.processMerchantFailResponse(requestData, ex.getResponseConstants());
            } else {
                htmlPage = merchantResponseService.processMerchantFailResponse(requestData,
                        ResponseConstants.SYSTEM_ERROR);
            }
            PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
            pageDetailsResponse.setSuccessfullyProcessed(false);
            pageDetailsResponse.setHtmlPage(htmlPage);

            if (StringUtils.isNotBlank(requestData.getLinkId()) || StringUtils.isNotBlank(requestData.getInvoiceId())) {

                ResultInfo resultInfo = ex.getResultInfo();
                ResponseConstants responseConstants = ResponseConstants.fetchResponseConstantByName(resultInfo
                        .getResultCode());
                if (responseConstants != null) {
                    linkPaymentUtil.setpageDetailsResponseForLinkBasedPayment(pageDetailsResponse, requestData,
                            responseConstants.getMessage(), responseConstants.getAlipayResultMsg());
                } else {
                    linkPaymentUtil.setpageDetailsResponseForLinkBasedPayment(pageDetailsResponse, requestData, "", "");
                }

            }
            return pageDetailsResponse;
        } catch (KybServiceException ex) {
            LOGGER.error("Error while validating user for Advance deposite due to : {}", ex);
            String htmlPage;
            if (ex.getResponseConstants() != null) {
                htmlPage = merchantResponseService.processMerchantFailResponse(requestData, ex.getResponseConstants());
            } else {
                htmlPage = merchantResponseService.processMerchantFailResponse(requestData,
                        ResponseConstants.SYSTEM_ERROR);
            }
            return generateExceptionResponse(htmlPage);

        } catch (PWPPromoServiceException ex) {
            LOGGER.error("Error while applying promo for PWP Merchant : {}", ex);
            String htmlPage;
            if (ex.getResponseConstants() != null) {
                htmlPage = merchantResponseService.processMerchantFailResponse(requestData, ex.getResponseConstants());
            } else {
                htmlPage = merchantResponseService.processMerchantFailResponse(requestData,
                        ResponseConstants.SYSTEM_ERROR);
            }
            return generateExceptionResponse(htmlPage);
        } catch (Exception ex) {
            LOGGER.error("Error while getting initiateTransactionResponse due to : {}", ex);
            String htmlPage = merchantResponseService.processMerchantFailResponse(requestData,
                    ResponseConstants.SYSTEM_ERROR);
            PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
            pageDetailsResponse.setSuccessfullyProcessed(false);
            pageDetailsResponse.setHtmlPage(htmlPage);

            if (StringUtils.isNotBlank(requestData.getLinkId()) || StringUtils.isNotBlank(requestData.getInvoiceId())) {
                linkPaymentUtil.setpageDetailsResponseForLinkBasedPayment(pageDetailsResponse, requestData, "", "");
            }
            return pageDetailsResponse;
        }
    }

    private InitiateTransactionResponse processInitiateTransaction(
            InitiateTransactionRequest initiateTransactionRequest, boolean orderCreationRequiredInInitiate)
            throws Exception {
        IRequestProcessor<NativeInitiateRequest, InitiateTransactionResponse> requestProcessor = requestProcessorFactory
                .getRequestProcessor(RequestType.INITIATE_TRANSACTION_REQUEST);
        NativeInitiateRequest nativeInitiateRequest = new NativeInitiateRequest();
        nativeInitiateRequest.setInitiateTxnReq(initiateTransactionRequest);
        nativeInitiateRequest.setOrderCreateInInitiate(orderCreationRequiredInInitiate);
        return requestProcessor.process(nativeInitiateRequest);
    }

    private PageDetailsResponse fetchPageDetailsResponseForSdkPtc(BaseException ex) {
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        pageDetailsResponse.setSuccessfullyProcessed(false);
        pageDetailsResponse.setException(ex);
        return pageDetailsResponse;
    }

    private String getRedirectUrlForAppInvoke(InitiateTransactionRequestBody initiateTransactionRequestBody,
            InitiateTransactionResponse initiateTransactionResponse, UserAgentInfo userAgentInfo)
            throws MalformedURLException {
        String sourceUrl = AppInvokeRedirectionUrlData.getBaseUrl().toString();
        AppInvokeRedirectionUrlData url = processTransactionUtil.getUrlDataProvidedSourceUrlForAppInvoke(sourceUrl,
                initiateTransactionRequestBody, initiateTransactionResponse, userAgentInfo);
        return (url == null) ? null : url.getRedirectionUrl();
    }

    private String getRedirectUrlV3ForAppInvoke(InitiateTransactionRequestBody initiateTransactionRequestBody,
            InitiateTransactionResponse initiateTransactionResponse, UserAgentInfo userAgentInfo)
            throws MalformedURLException {
        String sourceUrl = AppInvokeRedirectionUrlData.getV3BaseUrl().toString();
        AppInvokeRedirectionUrlData url = processTransactionUtil.getUrlDataProvidedSourceUrlForAppInvoke(sourceUrl,
                initiateTransactionRequestBody, initiateTransactionResponse, userAgentInfo);
        return (url == null) ? null : url.getV3RedirectionUrl();
    }

    private PageDetailsResponse generateExceptionResponse(String htmlPage) {
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        pageDetailsResponse.setSuccessfullyProcessed(false);
        pageDetailsResponse.setHtmlPage(htmlPage);
        return pageDetailsResponse;
    }

    private void setSubscriptionDataForAppInvokeFlow(InitiateTransactionRequestBody orderDetail,
            InitiateTransactionResponse initiateTransactionResponse, PaymentRequestBean requestData) {
        SubscriptionTransactionRequestBody subsTxnReqBody = (SubscriptionTransactionRequestBody) orderDetail;

        requestData.setSubsPaymentMode(subsTxnReqBody.getSubscriptionPaymentMode());
        requestData.setSubscriptionAmountType(subsTxnReqBody.getSubscriptionAmountType());
        requestData.setSubscriptionFrequencyUnit(subsTxnReqBody.getSubscriptionFrequencyUnit());
        requestData.setSubscriptionFrequency(subsTxnReqBody.getSubscriptionFrequency());
        requestData.setSubscriptionFrequencyUnit(subsTxnReqBody.getSubscriptionFrequencyUnit());
        requestData.setSubscriptionMaxAmount(subsTxnReqBody.getSubscriptionMaxAmount());
        requestData.setSubsPPIOnly(subsTxnReqBody.getSubsPPIOnly());
        requestData.setSubscriptionID(initiateTransactionResponse.getBody().getSubscriptionId());
        requestData.setMandateType(StringUtils.isNotBlank(subsTxnReqBody.getMandateType()) ? subsTxnReqBody
                .getMandateType() : MandateMode.E_MANDATE.name());
        requestData.setAccountNumber(orderDetail.getAccountNumber());

    }

    private boolean isParallelizedFlowForEnhancedOnly(PaymentRequestBean requestData) {
        String nativeEnhanceTaskFlowMids = "ALL";
        return (TaskFlowUtils.isMidEligibleForTaskFlow(requestData.getMid(), nativeEnhanceTaskFlowMids));
    }

    private boolean isNativeAppInvokeRequest(PaymentRequestBean paymentRequestBean) {
        return StringUtils.isNotBlank(paymentRequestBean.getTxnToken())
                && StringUtils.isNotBlank(paymentRequestBean.getMid())
                && StringUtils.isNotBlank(paymentRequestBean.getOrderId());
    }

    private String generateCheckSum(String mid, InitiateTransactionResponse initiateTransactionResponse)
            throws Exception {
        String requestBody = JsonMapper.mapObjectToJson(initiateTransactionResponse.getBody());
        StringBuilder sb = new StringBuilder(requestBody).append("|");
        String merchantKey = merchantExtendInfoUtils.getMerchantKey(mid, initiateTransactionResponse.getHead()
                .getClientId());
        return ValidateChecksum.getInstance().getRespCheckSumValue(merchantKey, sb).get(CHECKSUMHASH);
    }

    private void checkAndSetOldPgUrl(EnhancedCashierPage enhancedCashierPage) {
        try {
            HttpServletRequest httpServletRequest = EnvInfoUtil.httpServletRequest();

            LOGGER.info(" host:" + httpServletRequest.getHeader(HttpHeaders.HOST) + " remoteHost:"
                    + httpServletRequest.getRemoteHost() + " address :" + httpServletRequest.getRemoteAddr() + " "
                    + httpServletRequest.getRequestURL());

            String oldPGBaseUrl = ConfigurationUtil.getProperty(TheiaConstant.ExtraConstants.OLD_PG_BASE_URL_SKIP);
            String newPGURl = ConfigurationUtil.getProperty(TheiaConstant.ExtraConstants.NEW_PG_BASE_URL);

            if (StringUtils.isNotBlank(oldPGBaseUrl) && StringUtils.isNotBlank(newPGURl)) {

                if (oldPGBaseUrl.equalsIgnoreCase(httpServletRequest.getHeader(HttpHeaders.HOST))) {

                    enhancedCashierPage.setOldPGBaseUrl(oldPGBaseUrl.trim());
                    enhancedCashierPage.setNewPGBaseUrl(newPGURl.trim());

                }
            }
        } catch (Exception e) {
            LOGGER.warn("something unexpected happedned while setting old pg variables ", e);
        }
    }

    private boolean isCreateOrderOrTopupRequired(InitiateTransactionResponse initTxnResp, String mid) {
        /*
         * This checks whether acqId is present in cache, if present,
         * createOrder is not done
         */
        Map<String, Object> context = new HashMap<>();
        context.put(TheiaConstant.RequestParams.Native.MID, mid);
        if (iPgpFf4jClient.checkWithdefault(TheiaConstant.ExtraConstants.DISABLE_CREATE_ORDER_ENHANCED_TXN, context,
                false)) {
            if (iPgpFf4jClient.checkWithdefault(TheiaConstant.ExtraConstants.BLACKLIST_CREATE_ORDER_INT_TXN, context,
                    false)) {
                LOGGER.info("not creating order for enhanced flow");
                return false;
            }
        }
        String acqIdFromCache = nativeSessionUtil.getTxnId(initTxnResp.getBody().getTxnToken());

        if (StringUtils.isNotBlank(acqIdFromCache)) {
            if (isIdempotentInitiateTxnRequest(initTxnResp)) {

                /* This is when createOrder has been already done */
                LOGGER.info("SUCCESS_IDEMPOTENT, createOrder already done acqId: {}", acqIdFromCache);
            }

            if (StringUtils.equals(ResultCode.SUCCESS.getResultCodeId(), initTxnResp.getBody().getResultInfo()
                    .getResultCode())) {
                /*
                 * This is the case when appInvoke is called again
                 */
                LOGGER.info("<AppInvoke>, createOrder already done acqId: {}", acqIdFromCache);
            }
            return false;
        }
        return true;
    }

    public PageDetailsResponse fetchPageDetailsResponse(PaymentRequestBean requestData,
            InitiateTransactionResponse initiateTransactionResponse) throws Exception {
        EnhancedCashierPage enhancedCashierPage = null;
        // There will always login skip so create a property if in next release
        // we want to restrict skip login and want user to land on login page
        String disableMandatoryLoginInLinkBasedMFOrSTFlow = ConfigurationUtil.getProperty(
                DISABLE_MANDATORY_LOGIN_IN_LINK_BASED_MF_ST_FLOW, "true");
        if (!Boolean.valueOf(disableMandatoryLoginInLinkBasedMFOrSTFlow)
                && (requestData.getLinkDetailsData() != null && StringUtils.isEmpty(requestData.getSsoToken()) && (requestData
                        .getLinkDetailsData().getPaymentFormDetails() == null
                        || requestData.getLinkDetailsData().getPaymentFormDetails().getSkipLoginEnabled() == null || requestData
                        .getLinkDetailsData().getPaymentFormDetails().getSkipLoginEnabled() == false))) {
            enhancedCashierPage = getEnhancedCashierPageForLinkBasedPaymentOfMTOrST(requestData,
                    initiateTransactionResponse);
        } else {
            try { // enhanced-checkout flow
                if (merchantPreferenceService.isCheckoutJsOnEnhancedFlowEnabled(requestData.getMid())) {
                    // LOGGER.info(String.format("CheckoutJs on EnhancedFlow enabled for MID %s and order ID %s",
                    // requestData.getMid(), requestData.getOrderId()));
                    String htmlPage = enhancedCashierPageServiceHelper.getEnhancedCheckoutJSTheme();
                    if (StringUtils.isNotEmpty(htmlPage)) {
                        String fpoJsonResponse = getCheckoutFpoResponse(requestData, initiateTransactionResponse);
                        LOGGER.info("Checkout Response to UI - " + fpoJsonResponse);
                        setLinkBasedPaymentDetailsInSessionRedis(initiateTransactionResponse, requestData, null);
                        if (StringUtils.isNotEmpty(fpoJsonResponse)) {
                            return checkoutJSPageResponse(fpoJsonResponse, htmlPage);
                        }
                    } else {
                        LOGGER.error("Unable to fetch checkout JS HTML template from local resources");
                    }
                }
            } catch (Exception e) {
                LOGGER.error(String.format("Error in enhanced checkout flow for order ID %s and MID %s - %s",
                        requestData.getOrderId(), requestData.getMid(), e));
            }
            enhancedCashierPage = getEnhancedCashierPage(requestData, initiateTransactionResponse);
            checkAndSetOldPgUrl(enhancedCashierPage);
            setLinkBasedPaymentDetailsInSessionRedis(initiateTransactionResponse, requestData,
                    enhancedCashierPage.getMerchant());
        }

        enhancedCashierPageServiceHelper.settxnTokenTTL(enhancedCashierPage);

        String htmlPage = null;

        String enhancedCashierPageJson = JsonMapper.mapObjectToJson(enhancedCashierPage);

        String isPushAppDataEncoded = ConfigurationUtil.getProperty(ENHANCE_PUSH_APP_DATA_ENCODED, "false");

        // if encodedPropertyTrue then encode the enhancedCashierPAgeJson
        if (!ff4JUtil.isFeatureEnabled(TheiaConstant.FF4J.FEATURE_UI_SERVER_CONFIG, requestData.getMid())
                && Boolean.valueOf(isPushAppDataEncoded)) {
            enhancedCashierPageJson = new String(Base64.getEncoder()
                    .encode((enhancedCashierPageJson).getBytes("UTF-8")));
        }

        /*
         * ff4j code to be added if mid is configured in ff4j get the html page
         * from ui-microservice with thisenhancedCashierPageJson
         */

        UIMicroserviceRequest uiMicroserviceRequest = new UIMicroserviceRequest(enhancedCashierPageJson,
                requestData.getChannelId(), isPushAppDataEncoded, UIMicroserviceUrl.ENHANCED_CASHIER_URL);
        UIMicroserviceResponse uiMicroserviceResponse = uiMicroserviceHelper.getHtmlPageFromUI(uiMicroserviceRequest,
                FEATURE_UI_MICROSERVICE_ENHANCED, requestData.getMid());
        htmlPage = uiMicroserviceResponse.getHtmlPage();

        // if html page is blank try with the old method
        if (StringUtils.isBlank(htmlPage)) {

            htmlPage = enhancedCashierPageServiceHelper.getEnhancedCashierTheme(requestData.getChannelId());
            LOGGER.info("PUSH_APP_DATA for enhanced cashier flow {}", enhancedCashierPage);

            if (Boolean.valueOf(isPushAppDataEncoded)) {
                htmlPage = htmlPage.replace(EnhancedCashierPageKeys.ENCODE_FLAG, "true");
            }

            if (ff4JUtil.isFeatureEnabled(TheiaConstant.FF4J.FEATURE_UI_SERVER_CONFIG, requestData.getMid())) {
                enhancedCashierPageJson = new String(Base64.getEncoder().encode((enhancedCashierPageJson).getBytes()));
            }

            if (StringUtils.isNotBlank(htmlPage)) {
                htmlPage = htmlPage.replace(EnhancedCashierPageKeys.REPLACE_STRING, enhancedCashierPageJson);
            } else {
                throw new TheiaServiceException("Unable to fetch html template from redis");
            }
        }

        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        pageDetailsResponse.setSuccessfullyProcessed(true);
        pageDetailsResponse.setHtmlPage(htmlPage);

        setEnhancedCashierPageInCache(requestData, initiateTransactionResponse, enhancedCashierPage);

        return pageDetailsResponse;
    }

    private void populateGroupedPayModes(EnhancedCashierPage enhancedCashierPage, PaymentRequestBean requestData) {
        boolean groupedPayModesEnabled = merchantPreferenceService
                .isGroupedPayModesEnabled(requestData.getMid(), false);
        if (groupedPayModesEnabled) {
            // LOGGER.info("grouped paymodes enabled for merchant {}",requestData.getMid());
            enhancedCashierPage.setMerchantGroupedPayModes(enhancedCashierPageServiceHelper.getGroupedPayModes(
                    requestData, enhancedCashierPage.getMerchantPayModes(), UIREVAMP_PAYMODE_GROUPS));
            enhancedCashierPage.setAddMoneyGroupedPayModes(enhancedCashierPageServiceHelper.getGroupedPayModes(
                    requestData, enhancedCashierPage.getAddMoneyPayModes(), UIREVAMP_ADDMONEY_PAYMODE_GROUPS));

            LOGGER.info("Enhanced PayModeSequence Enum flow");
            Map<String, Integer> groupPriorities = enhancedCashierPageServiceHelper
                    .getGroupPrioritybyPayMethodPriority(requestData.getMid(),
                            requestData.isSubscription() ? PaymodeSequenceEnum.SUBSCRIPTION
                                    : PaymodeSequenceEnum.ENHANCE, requestData.getPaymodeSequence());
            enhancedCashierPage.setGroupPayOptionsPriorities(groupPriorities);

            if (ff4JUtil.isFeatureEnabled(UN_GROUPED_PAYMODES_DISABLED, requestData.getMid())) {
                LOGGER.info("Disabling old payModes for mid {}", requestData.getMid());
                enhancedCashierPage.setMerchantPayModes(null);
                enhancedCashierPage.setAddMoneyPayModes(null);
            }
        }
    }

    private String getCheckoutFpoResponse(PaymentRequestBean requestData,
            InitiateTransactionResponse initiateTransactionResponse) throws Exception {
        // calling fetchPaymentDetails native API
        LOGGER.info(String.format("Making FPO call for mid %s and order ID %s", requestData.getMid(),
                requestData.getOrderId()));
        NativeCashierInfoResponse nativeCashierInfoResponse = getNativeCashierInfoResponse(requestData,
                initiateTransactionResponse, true);
        if (nativeCashierInfoResponse != null && nativeCashierInfoResponse.getBody() != null) {
            return getEnhancedCheckoutResponse(nativeCashierInfoResponse.getBody(), requestData);
        }
        return null;
    }

    private PageDetailsResponse checkoutJSPageResponse(String enhancedCheckoutJsonResponse, String htmlPage) {
        String checkoutJsDecodeResponse = new String(Base64.getEncoder().encode(
                (enhancedCheckoutJsonResponse).getBytes()));
        htmlPage = htmlPage.replace(EnhancedCashierPageKeys.REPLACE_STRING, checkoutJsDecodeResponse);
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        pageDetailsResponse.setSuccessfullyProcessed(true);
        pageDetailsResponse.setHtmlPage(htmlPage);
        return pageDetailsResponse;
    }

    private boolean isAPPInvoke() {
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        if (StringUtils.equals(NATIVE_APP_INVOKE_URL, servletRequest.getRequestURI())
                || StringUtils.equals(NATIVE_APP_INVOKE_URL_V2, servletRequest.getRequestURI())
                || StringUtils.equals(NATIVE_APP_INVOKE_URL_V3, servletRequest.getRequestURI())) {
            return true;
        }
        return false;
    }

    private boolean isShowLinkPaymentPageFlow() {
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        if (StringUtils.equals(NATIVE_SHOW_LINK_PAYMENT_PAGE, servletRequest.getRequestURI())) {
            return true;
        }
        return false;
    }

    private void setLinkBasedPaymentDetailsInSessionRedis(InitiateTransactionResponse initiateTransactionResponse,
            PaymentRequestBean requestData, EnhancedCashierPageMerchantInfo merchant) {
        if ((StringUtils.isNotEmpty(requestData.getLinkId()) || StringUtils.isNotEmpty(requestData.getInvoiceId()))
                && requestData.getLinkDetailsData() == null) {
            if (StringUtils.isNotEmpty(requestData.getLinkId())) {
                EXT_LOGGER.customInfo("LinkId : {} stored in redis against : {}", requestData.getLinkId(),
                        initiateTransactionResponse.getBody().getTxnToken());
                nativeSessionUtil.setLinkId(initiateTransactionResponse.getBody().getTxnToken(),
                        requestData.getLinkId());
                if (ff4jUtils.isFeatureEnabledOnMid(requestData.getMid(), THEIA_ENABLE_LINK_FLOW_ON_DQR, false)) {
                    String token = nativeSessionUtil.getMidOrderIdToken(requestData.getMid(), requestData.getOrderId());
                    nativeSessionUtil.setLinkIdForQR(token, requestData.getLinkId());
                    EXT_LOGGER.customInfo("LinkId :{} stored in redis against : {}", requestData.getLinkId(), token);
                }
            }

            if (StringUtils.isNotEmpty(requestData.getInvoiceId())) {
                nativeSessionUtil.setInvoiceId(initiateTransactionResponse.getBody().getTxnToken(),
                        requestData.getInvoiceId());
            }

            if (merchant != null) {
                LOGGER.info("Setting merchant info in redis for Link based payment for MID = {}", merchant.getMid());
                LinkBasedMerchantInfo linkBasedMerchantInfo = new LinkBasedMerchantInfo(merchant.getMid(),
                        merchant.getName(), merchant.getLogo());
                theiaTransactionalRedisUtil.set(
                        LINK_BASED_KEY
                                + nativeSessionUtil.getTxnId(initiateTransactionResponse.getBody().getTxnToken()),
                        linkBasedMerchantInfo);
            }
        }
    }

    public EnhancedCashierPage getEnhancedCashierPage(PaymentRequestBean requestData,
            InitiateTransactionResponse initiateTransactionResponse) throws Exception {

        NativeInitiateRequest nativeInitiateRequest = null;
        if (initiateTransactionResponse != null && initiateTransactionResponse.getBody() != null
                && initiateTransactionResponse.getBody().getTxnToken() != null) {
            nativeInitiateRequest = nativeSessionUtil.getNativeInitiateRequest(initiateTransactionResponse.getBody()
                    .getTxnToken());
        }

        /*
         * If request is Idempotent, returns EnhancedCashierPage if already
         * present in Cache
         */
        if (isIdempotentInitiateTxnRequest(initiateTransactionResponse) || isAPPInvoke() || isShowLinkPaymentPageFlow()) {
            EnhancedCashierPage enhancedCashierPage = getEnhancedCashierPageFromCache(requestData);
            if (enhancedCashierPage != null) {
                // PGP-10766 | Remove retry data while showing the cashier page
                // in enhanced.

                // setting accountNumber if present
                if (StringUtils.isNotBlank(requestData.getAccountNumber()))
                    enhancedCashierPage.setCashierAccountNumber(MaskingUtil.getMaskedBankAccountNumber(
                            requestData.getAccountNumber(), 0, 4));

                if (StringUtils.equalsIgnoreCase(requestData.getPaymentMode(), "UPI")) {
                    enhancedCashierPage.setRetryData(null);
                }
                setRetryInfoForDcc(enhancedCashierPage, requestData);

                /**
                 * This is done to show only UPI Intent in m-web for addnpay for
                 * Flipkart
                 */
                if (isNotV1AppInvoke() && enhancedCashierPage.getCollectAppInvoke() != null
                        && enhancedCashierPage.getCollectAppInvoke()) {
                    processTransactionUtil.pushNativePaymentEvent(requestData.getMid(), requestData.getOrderId(),
                            "M-WEB Opened after sms click");
                    enhancedCashierPage.setAddnPayWithUPIIntentOnly(true);
                }

                LOGGER.info("Returning enhancedCashierPage from cache!");
                return enhancedCashierPage;
            }
        }

        // calling fetchPaymentDetails native API
        NativeCashierInfoResponse nativeCashierInfoResponse = getNativeCashierInfoResponse(requestData,
                initiateTransactionResponse, false);
        nativeCashierInfoResponse = setVPANull(nativeCashierInfoResponse);

        updateAuthenticatedFlagInInitiateTransactionResponse(initiateTransactionResponse, nativeCashierInfoResponse);

        // fetching Wallet info from pay methods
        requestData.setTxnToken(initiateTransactionResponse.getBody().getTxnToken());
        EnhancedCashierPageWalletInfo walletInfo = processTransactionUtil.getEnhancedCashierPageWalletInfo(requestData,
                nativeCashierInfoResponse, initiateTransactionResponse.getBody().getTxnToken());

        EnhancedCashierPageDisplayIdGenerator displayIdGenerator = new EnhancedCashierPageDisplayIdGenerator();

        // get merchantPayModes
        List<EnhancedCashierPagePayModeBase> merchantPayModes = getEnhancedCashierPageMerchantPayModes(requestData,
                initiateTransactionResponse, nativeCashierInfoResponse, displayIdGenerator, walletInfo);

        // get addMoneyPayModes
        List<EnhancedCashierPagePayModeBase> addMoneyPayModes = getEnhancedCashierPageAddMoneyPayModes(requestData,
                initiateTransactionResponse, nativeCashierInfoResponse, displayIdGenerator, walletInfo);

        // filtering all pay modes except cc dc for penny drop add money case
        filterCCDCForPennyDropAddMoney(requestData, merchantPayModes);

        checkAndSetIfNotPayModesConfiguredForPromoCode(addMoneyPayModes, nativeCashierInfoResponse);

        boolean onUsMerchant = merchantExtendInfoUtils.isMerchantOnPaytm(requestData.getMid());
        // fetch merchant info
        EnhancedCashierPageMerchantInfo merchantUserInfo = new EnhancedCashierPageMerchantInfo();
        merchantUserInfo.setOnus(onUsMerchant);

        // LOGIN INFO
        EnhancedCashierPageLoginInfo loginInfo = getLoginInfo(initiateTransactionResponse, nativeCashierInfoResponse,
                requestData.getMid());

        if (aoaUtils.isAOAMerchant(requestData)) {
            merchantUserInfo.setMid(requestData.getMid());
            loginInfo.setLoginFlag(true);
        } else {
            merchantUserInfo = getMerchantUserInfoResponse(requestData, initiateTransactionResponse,
                    nativeCashierInfoResponse);
        }
        merchantUserInfo.setOnus(onUsMerchant);
        boolean appInvokeAllowed = merchantPreferenceService.isAppInvokeAllowed(requestData.getMid(), false);
        /*
         * This is to support v3 app invoke (for PCF merchants), untill UI
         * provides support for the same. enabling
         * 'theia.v3.appinvoke.support.feature' will force the code to check &
         * disable 'payment with Paytm button' for v3 app invoke allowed
         * merchants i.e. PCF merchants. disable this ff4j flag once UI releases
         * the support.
         */
        if (appInvokeAllowed
                && ff4JUtil.isFeatureEnabled(TheiaConstant.FF4J.THEIA_V3_APP_INVOKE_SUPPORT, requestData.getMid())
                && processTransactionUtil.isV3AppInvokeRequired(requestData.getMid())) {
            appInvokeAllowed = false;
        }
        merchantUserInfo.setAppInvokeAllowed(appInvokeAllowed);
        /**
         * PGP-UI team will create a local storage (in a cache) based on
         * LocalStorageForLastPayMode where the last 3 payment modes of the user
         * will be stored (for which the txn was successful).Next time, when the
         * user lands on the redirection page for selecting the paymode, the
         * last paymode stored will be selected for the user by default and the
         * page will scroll at the selected paymode
         */
        merchantUserInfo.setLocalStorageAllowedForLastPayMode(ff4JUtil.isFeatureEnabled(
                TheiaConstant.FF4J.THEIA_LOCAL_STORAGE_ALLOWED_ON_MERCHANT, requestData.getMid()));

        EnhancedCashierPageDynamicQR dynamicQR = null;
        if (requestData.getLinkDetailsData() == null) {
            dynamicQR = getEnhancedCashierPageDynamicQR(requestData, nativeCashierInfoResponse);
        }
        // set flag to enable customer feedback option on UI based on merchant
        // preference
        merchantUserInfo.setCustomerFeedbackEnabled(merchantPreferenceService.isCustomerFeedbackEnabled(
                requestData.getMid(), false));

        dynamicQR = getEnhancedCashierPageDynamicQR(requestData, nativeCashierInfoResponse);

        PreAuthDetails preAuthDetails = getPreAuthDetails(requestData);

        // TXN INFO , PCF TO BE ADDED
        EnhancedCashierTxnInfo txnInfo = getTxnInfo(requestData, nativeCashierInfoResponse, walletInfo,
                (initiateTransactionResponse.getBody() instanceof SubscriptionTransactionResponseBody));

        // updateWalletInfoForMgv(merchantPayModes, walletInfo);

        // link data
        LinkAppPushData linkPushData = new LinkAppPushData();

        boolean isSkipLoginFlow = false;
        boolean isMFOrSTSkipLoginFlow = false;

        if (StringUtils.isNotEmpty(requestData.getPaymentFormId())) {

            PaymentFormDetails paymentFormDetails = linkPaymentUtil.getPaymentFormDetails(requestData);

            if (paymentFormDetails != null && paymentFormDetails.getSkipLoginEnabled() != null
                    && paymentFormDetails.getSkipLoginEnabled()) {
                isSkipLoginFlow = true;
            }
        }
        if (requestData.getLinkDetailsData() != null
                && requestData.getLinkDetailsData().getPaymentFormDetails() != null
                && Boolean.TRUE.equals(requestData.getLinkDetailsData().getPaymentFormDetails().getSkipLoginEnabled())) {
            isMFOrSTSkipLoginFlow = true;
        }

        if (StringUtils.isNotEmpty(requestData.getLinkId()) || StringUtils.isNotEmpty(requestData.getInvoiceId())) {

            String linkType = null;
            if (ERequestType.LINK_BASED_PAYMENT_INVOICE.getType().equalsIgnoreCase(requestData.getRequestType())) {
                linkType = TheiaConstant.LinkRiskParams.LINK_INVOICE_TYPE;
            } else if (ERequestType.LINK_BASED_PAYMENT.getType().equalsIgnoreCase(requestData.getRequestType())) {
                linkType = TheiaConstant.LinkRiskParams.LINK_TYPE_NON_INVOICE;
            }

            if (!StringUtils.isEmpty(requestData.getPaymentFormId()) && isSkipLoginFlow) {
                String linkId = requestData.getPaymentFormId().replace(CommonConstant.PAYMENT_FORM_REDIS_PREFIX, "");
                String redirectUrl = ConfigurationUtil.getProperty("link.service.base.url") + "/ptcRedirect/" + linkId;
                linkPushData = new LinkAppPushData(ProcessTransactionHelper.replaceApostrophe(requestData
                        .getLinkDescription()), redirectUrl, linkType);
            } else if (isMFOrSTSkipLoginFlow) {
                String linkId = requestData.getLinkDetailsData().getLinkId();
                String redirectUrl = ConfigurationUtil.getProperty("link.service.base.url") + "/ptcRedirect/" + linkId;
                linkPushData = new LinkAppPushData(ProcessTransactionHelper.replaceApostrophe(requestData
                        .getLinkDescription()), redirectUrl, linkType);
            } else {
                linkPushData = new LinkAppPushData(ProcessTransactionHelper.replaceApostrophe(requestData
                        .getLinkDescription()), requestData.getLongUrl()
                        + TheiaConstant.LinkBasedParams.LINK_INVOICE_CHANGE_NUMBER, linkType);
            }
            if (StringUtils.isNotBlank(requestData.getAccountNumber())) {
                linkPushData.setAccountNumber(MaskingUtil.getMaskedBankAccountNumber(requestData.getAccountNumber(), 0,
                        4));
            }
            linkPushData.setDisplayWarningMessage(requestData.isDisplayWarningMessageForLink());

            linkPushData.setLinkId(requestData.getLinkId());
            linkPushData.setLinkName(requestData.getLinkName());
            if (merchantUserInfo.isAppInvokeAllowed() && nativeCashierInfoResponse != null
                    && nativeCashierInfoResponse.getBody() != null
                    && nativeCashierInfoResponse.getBody().getMerchantDetails() != null) {
                merchantUserInfo.setAppInvokeAllowed(merchantUserInfo.isAppInvokeAllowed()
                        && nativeCashierInfoResponse.getBody().getMerchantDetails().getIsAppInvokeAllowed());
            }

        }

        // Localization info
        EnhancedCashierLocalalizedText i18n = new EnhancedCashierLocalalizedText();
        i18n.setLang("en");

        // Remove PPBL saved VPA if PPBL NB is a valid pay method for onus
        // merchants
        try {
            if (onUsMerchant) {
                removePPBLVPAForPPBLNB(merchantPayModes);
            }
        } catch (Exception e) {
            // TODO : Handle/Correct the exception
            LOGGER.error("Exception occurred while removing PPBL VPA in enhanced");
        }

        /*
         * // Set selected paymodes for insufficient balance for user logged in
         * if (txnInfo.isInsufficientBalance() &&
         * (initiateTransactionResponse.getBody().isAuthenticated() ||
         * (StringUtils.isNotBlank(requestData .getPaytmToken()) ||
         * StringUtils.isNotBlank(requestData.getSsoToken())))) {
         * 
         * setSelectedFlagOnPayModes(merchantPayModes,
         * nativeCashierInfoResponse.getBody().getMerchantPayOption(),
         * initiateTransactionResponse.getBody().getTxnToken(),
         * nativeCashierInfoResponse.getBody() .getPaymentFlow(), true,
         * txnInfo.isPcfEnabled(), false);
         * setSelectedFlagOnPayModes(addMoneyPayModes,
         * nativeCashierInfoResponse.getBody().getAddMoneyPayOption(), null,
         * null, false, false, true); }
         */
        if (initiateTransactionResponse.getBody().isAuthenticated()
                || StringUtils.isNotBlank(requestData.getPaytmToken())
                || StringUtils.isNotBlank(requestData.getSsoToken())) {
            setSelectedFlagOnPayModes(merchantPayModes, nativeCashierInfoResponse.getBody().getMerchantPayOption(),
                    initiateTransactionResponse.getBody().getTxnToken(), nativeCashierInfoResponse.getBody()
                            .getPaymentFlow(), true, txnInfo.isPcfEnabled(), false, walletInfo);
            setSelectedFlagOnPayModes(addMoneyPayModes, nativeCashierInfoResponse.getBody().getAddMoneyPayOption(),
                    null, null, false, false, true, walletInfo);
        }

        // Set first paymode as selected if login is false
        /*
         * if (loginInfo != null && !loginInfo.isLoginFlag()) { if
         * (merchantPayModes != null && merchantPayModes.size() == 1 &&
         * merchantPayModes.get(0) != null) {
         * merchantPayModes.get(0).setSelected(true); } }
         */
        if (walletInfo != null && walletInfo.getIsActive() != null && !walletInfo.getIsActive().isStatus()) {
            walletInfo.setUsed(false);
        }
        checkWalletSelectionForNonePaymode(nativeCashierInfoResponse, walletInfo, txnInfo);

        // Set EMI as selected for Zero Cost EMI
        if (requestData.getEmiOption() != null
                && !StringUtils.isEmpty(requestData.getEmiOption())
                && requestData.getEmiOption().startsWith(TheiaConstant.ExtraConstants.ZERO_COST_EMI)
                && merchantPayModes != null
                && !merchantPayModes.isEmpty()
                && merchantPayModes.get(0) != null
                && (StringUtils.isNotBlank(requestData.getPaytmToken()) || StringUtils.isNotBlank(requestData
                        .getSsoToken()))) {
            merchantPayModes.get(0).setSelected(true);
            if (txnInfo.isPcfEnabled()) {
                merchantPayModes.get(0).setPcfFeeCharges(
                        processTransactionUtil.getPCFFeecharges(initiateTransactionResponse.getBody().getTxnToken(),
                                EPayMethod.EMI, null, null));
            }
        }

        UserInfo userInfo = getUserInfoForEnhancedCashier(initiateTransactionResponse, requestData,
                nativeCashierInfoResponse);

        // mobile is unditable but userInfo doesn't have mobile number field.
        if (mobileMaskHelper.isValidMaskedMobileNumber(userInfo.getMobile())
                || (StringUtils.isBlank(userInfo.getMobile()) && loginInfo.isMobileNumberNonEditable())) {
            loginInfo.setMobileNumberNonEditable(false);
        }

        // getting merchant theme preferences from mapping
        PerfernceInfo merchantPreferenceInfoExt = null;
        if (merchantPreferenceService.isThemeticCustomizationEnabled(requestData.getMid(), false)) {
            merchantPreferenceInfoExt = merchantDataService.getMerchantPreferenceInfoExt(requestData.getMid(),
                    THEME_PREFERENCE_DETAILS);
            EXT_LOGGER.customInfo("Mapping response - PerfernceInfoExt :: {}", merchantPreferenceInfoExt);
        }

        // NOTIFICATION INFO
        EnhancedCashierPage enhancedCashierPage = new EnhancedCashierPage();
        enhancedCashierPage.setTxnToken(initiateTransactionResponse.getBody().getTxnToken());

        enhancedCashierPage.setIsPostpaidEnabledOnMerchantAndDisabledOnUser(nativeCashierInfoResponse.getBody()
                .getIsPostpaidEnabledOnMerchantAndDisabledOnUser());
        if (StringUtils.isNotBlank(requestData.getAccountNumber()))
            enhancedCashierPage.setCashierAccountNumber(MaskingUtil.getMaskedBankAccountNumber(
                    requestData.getAccountNumber(), 0, 4));

        enhancedCashierPage.setMerchant(merchantUserInfo);
        enhancedCashierPage.setWallet(walletInfo);
        enhancedCashierPage.setMerchantPayModes(merchantPayModes);
        enhancedCashierPage.setAddMoneyPayModes(addMoneyPayModes);
        enhancedCashierPage.setLoginInfo(loginInfo);
        enhancedCashierPage.setTxn(txnInfo);
        enhancedCashierPage.setI18n(i18n);
        enhancedCashierPage.setShowStoreCardEnabled(requestData.isShowSavecard());
        enhancedCashierPage.setPromoCodeData(nativeCashierInfoResponse.getBody().getPromoCodeData());
        enhancedCashierPage.setZeroCostEmi(nativeCashierInfoResponse.getBody().isZeroCostEmi());

        enhancedCashierPage.setCallbackUrl(initiateTransactionResponse.getBody().getCallbackUrl());

        enhancedCashierPage.setUserInfo(userInfo);
        enhancedCashierPage.setLink(linkPushData);
        enhancedCashierPage.setQr(dynamicQR);
        enhancedCashierPage.setPreAuthDetails(preAuthDetails);
        enhancedCashierPage.setUpiHandleMap(upiHandleUtil.getUpiHandleMap());
        enhancedCashierPage.setUltimateBeneficiaryDetails(requestData.getUltimateBeneficiaryDetails());
        enhancedCashierPage.setConvertToAddNPayOfferDetails(nativeCashierInfoResponse.getBody()
                .getConvertToAddNPayOfferDetails());

        if (localizationUtil.isLocaleEnabled()
                && (processTransactionUtil.isRequestOfType(PTR_URL) || processTransactionUtil.isRequestOfType(V1_PTC) || processTransactionUtil
                        .isRequestOfType(V1_ENHANCED_VALIDATE_OTP))) {
            String locale = localizationUtil.getLanguageCodeFromRequest();
            enhancedCashierPage.setUpiAppNamesRegional(upiRegionalNameUtil.getUpiAppNamesRegional().get(locale));
        }
        if (StringUtils.equals(ConfigurationUtil.getProperty("enable.fetchpayoption.v2.enhance", StringUtils.EMPTY),
                "true")) {
            UserProfileSarvatraV4 userProfileSarvatraV4 = getUserProfileSarvatraV4(nativeCashierInfoResponse);
            NpciHealthData npciHealthData = getNpciHealth(userProfileSarvatraV4);
            UpiMetaData upiMetaData = getUpiMetaData(userProfileSarvatraV4);
            List<String> upiLinkedBanks = getUpiLinkedBanks(userProfileSarvatraV4);

            enhancedCashierPage.setUpiMetaData(upiMetaData);
            enhancedCashierPage.setNpciHealth(npciHealthData);
            enhancedCashierPage.setUpiLinkedBanks(upiLinkedBanks);
        }
        // Set Subscription Info
        updateWalletInfoFromSubscriptionDetails(enhancedCashierPage, nativeCashierInfoResponse, requestData);
        // set mandate account details for bank mandate creation
        setMandateAccountDetailsForBankMandates(enhancedCashierPage, nativeCashierInfoResponse, requestData);

        if (merchantPreferenceInfoExt != null
                && CollectionUtils.isNotEmpty(merchantPreferenceInfoExt.getMerchantPreferenceInfos())) {
            MerchantPreferenceInfoV2 merchantPreferenceInfoV2 = new MerchantPreferenceInfoV2();
            PerfernceInfo.MerchantPreferenceInfo merchantPreferenceInfo = merchantPreferenceInfoExt
                    .getMerchantPreferenceInfos().get(0);
            merchantPreferenceInfoV2.setPrefStatus(merchantPreferenceInfo.getPrefStatus());
            merchantPreferenceInfoV2.setPrefType(merchantPreferenceInfo.getPrefType());
            merchantPreferenceInfoV2.setPrefValue(ObjectMapperUtil.getObjectMapper().readValue(
                    merchantPreferenceInfo.getPrefValue(), Object.class));
            enhancedCashierPage.setMerchantPreferenceInfoExt(merchantPreferenceInfoV2);
        }
        enhancedCashierPage.setPreLoginTheme(isFeatureEnabledOnCustId(requestData));

        // setting simplifiedPaymentOffers in APP_DATA
        if (nativeCashierInfoResponse.getBody() != null
                && nativeCashierInfoResponse.getBody().getSimplifiedPaymentOffers() != null) {
            enhancedCashierPage.setSimplifiedPaymentOffers(nativeCashierInfoResponse.getBody()
                    .getSimplifiedPaymentOffers());
        }

        // setting paymentOffers in APP_DATA
        if (nativeCashierInfoResponse.getBody() != null
                && CollectionUtils.isNotEmpty(nativeCashierInfoResponse.getBody().getPaymentOffers())) {
            enhancedCashierPage.setPaymentOffers(nativeCashierInfoResponse.getBody().getPaymentOffers());
        }

        // setting pwp enabled in APP_DATA
        if (nativeCashierInfoResponse.getBody() != null && nativeCashierInfoResponse.getBody().getPwpEnabled() != null) {
            enhancedCashierPage.setPwpEnabled(nativeCashierInfoResponse.getBody().getPwpEnabled());
        }

        if (nativeInitiateRequest != null && nativeInitiateRequest.getNativePersistData() != null
                && nativeInitiateRequest.getNativePersistData().getUserDetails() != null) {
            enhancedCashierPage.setCollectAppInvoke(ff4jUtils.isFeatureEnabledOnMidAndCustIdOrUserId(
                    FEATURE_THEIA_APP_INVOKE_AS_COLLECT, requestData.getMid(), requestData.getCustId(),
                    nativeInitiateRequest.getNativePersistData().getUserDetails().getUserId()));
            if (enhancedCashierPage.getCollectAppInvoke()) {
                processTransactionUtil.pushNativePaymentEvent(requestData.getMid(), requestData.getOrderId(),
                        "COLLECT_APP_INVOKE");
            }
        }

        // EMI Subvention All in one SDK Flow setting Subvention details in
        // APP_DATA
        enhancedCashierPage.setSubventionDetails(nativeCashierInfoResponse.getBody().getSubventionDetails());

        // setting merchant retry count in APP_DATA
        enhancedCashierPage.setAllowedRetryCountsForMerchant(nativeRetryUtil
                .getAllowedRetryCountsOnMerchant(requestData.getMid()));
        if (processTransactionUtil.isRequestOfType(PTR_URL)
                || processTransactionUtil.isRequestOfType(V1_ENHANCED_VALIDATE_OTP)
                || processTransactionUtil.isRequestOfType(V1_ENHANCED_LOGOUT_USER)) {
            localeFieldAspect.addLocaleFieldsInObject(enhancedCashierPage, PTR_URL);
            localizationUtil.addLocaleAppData(enhancedCashierPage);
        }
        if (isLinkBasedMFOrSTTxn(requestData)) {
            enhancedCashierPage.setDisableBackButton(true);
        }

        if (nativeCashierInfoResponse.getBody() != null) {
            enhancedCashierPage.setDisableCustomVPAInUPICollect(nativeCashierInfoResponse.getBody()
                    .getDisableCustomVPAInUPICollect());
        }

        setMerchantAcceptForPostpaid(nativeCashierInfoResponse, enhancedCashierPage);
        String uiConfiguration = ConfigurationUtil.getProperty(UI_SERVER_CONFIG);
        Map<String, String> uiConfig = OBJECT_MAPPER.readValue(uiConfiguration, Map.class);
        enhancedCashierPage.setUiConfig(uiConfig);
        HttpServletRequest servletRequest = EnvInfoUtil.httpServletRequest();
        enhancedCashierPage.setServerName(servletRequest.getServerName());
        enhancedCashierPage.setLocationPermission(merchantPreferenceService.isLocationPermission(requestData.getMid()));
        populateGroupedPayModes(enhancedCashierPage, requestData);
        enhancedCashierPage.setPostpaidOnlyMerchant(nativeCashierInfoResponse.getBody().isPostpaidOnlyMerchant());
        enhancedCashierPage
                .setPostpaidOnlyUserMessage(nativeCashierInfoResponse.getBody().getPostpaidOnlyUserMessage());
        enhancedCashierPage.setMerchantUPIVerified(nativeCashierInfoResponse.getBody().getMerchantUPIVerified());
        enhancedCashierPage.setSuperCashOffers(nativeCashierInfoResponse.getBody().getSuperCashOffers());
        enhancedCashierPage.setDeepLink(nativeCashierInfoResponse.getBody().getDeepLink());
        enhancedCashierPage.setQrDetail(nativeCashierInfoResponse.getBody().getQrDetail());
        enhancedCashierPage.setMerchantStaticConfig(getMerchantStaticConfig(requestData.getMid()));
        enhancedCashierPage.setMerchantLimitDetail(nativeCashierInfoResponse.getBody().getMerchantLimitDetail());
        enhancedCashierPage.setCcOnUPIAllowed(nativeCashierInfoResponse.getBody().getCcOnUPIAllowed());

        // setting emiSubventionBanks in APP_DATA
        enhancedCashierPage.setEmiSubventionBanks(nativeCashierInfoResponse.getBody().getEmiSubventionBanks());
        enhancedCashierPage.setNativeAddMoney(nativeCashierInfoResponse.getBody().getNativeAddMoney());
        enhancedCashierPage.setProductCode(nativeCashierInfoResponse.getBody().getProductCode());
        return enhancedCashierPage;
    }

    private MerchantStaticConfig getMerchantStaticConfig(String mid) {
        MerchantStaticConfigResponse merchantStaticConfigResponse = merchantStaticConfigService
                .getMerchantStaticConfig(new MerchantStaticConfigServiceRequest(mid));
        return merchantStaticConfigResponse != null ? merchantStaticConfigResponse.getBody().getMerchantStaticConfig()
                : null;
    }

    // Setting selection of wallet to false having insufficient balance
    // for Non AddnPay & Non Hybrid flow
    public void checkWalletSelectionForNonePaymode(NativeCashierInfoResponse nativeCashierInfoResponse,
            EnhancedCashierPageWalletInfo walletInfo, EnhancedCashierTxnInfo txnInfo) {
        if (walletInfo != null
                && walletInfo.isUsed()
                && walletInfo.getWalletBalance() > 0.0
                && walletInfo.getWalletBalance() < Double.parseDouble(txnInfo.getTxnAmount())
                && walletInfo.isWalletOnly()
                && nativeCashierInfoResponse.getBody().getPaymentFlow() != null
                && EPayMode.NONE.getValue().equalsIgnoreCase(
                        nativeCashierInfoResponse.getBody().getPaymentFlow().getValue())) {
            walletInfo.setUsed(false);
        }
    }

    private String getEnhancedCheckoutResponse(NativeCashierInfoResponseBody nativeCashierInfoResponseBody,
            PaymentRequestBean requestData) throws Exception {
        EnhancedCheckoutResponse res = EnhancedCheckoutResponse.builder().fpoResponse(nativeCashierInfoResponseBody)
                .mid(requestData.getMid()).txnToken(requestData.getTxnToken()).orderId(requestData.getOrderId())
                .txnAmount(requestData.getTxnAmount())
                .merchantStaticConfig(getMerchantStaticConfig(requestData.getMid()))
                .callbackUrl(requestData.getCallbackUrl()).build();
        return CustomObjectMapperUtil.convertToString(res);
    }

    public EnhancedCashierPage getEnhancedCashierPageForLinkBasedPaymentOfMTOrST(PaymentRequestBean requestData,
            InitiateTransactionResponse initiateTransactionResponse) throws Exception {

        EnhancedCashierPage enhancedCashierPage = new EnhancedCashierPage();
        enhancedCashierPage.setUserInfo(requestData.getUserInfo());
        boolean onUsMerchant = merchantExtendInfoUtils.isMerchantOnPaytm(requestData.getMid());
        // fetch merchant info
        EnhancedCashierPageMerchantInfo merchantUserInfo = new EnhancedCashierPageMerchantInfo();
        merchantUserInfo.setOnus(onUsMerchant);
        merchantUserInfo = getMerchantUserInfoResponse(requestData, initiateTransactionResponse, null);
        // LOGIN INFO
        EnhancedCashierPageLoginInfo loginInfo = getLoginInfo(initiateTransactionResponse, null, requestData.getMid());
        EnhancedCashierTxnInfo txnInfo = new EnhancedCashierTxnInfo();
        txnInfo.setTxnAmount(requestData.getTxnAmount());
        txnInfo.setId(requestData.getOrderId());
        enhancedCashierPage.setTxnToken(initiateTransactionResponse.getBody().getTxnToken());
        enhancedCashierPage.setMerchant(merchantUserInfo);
        enhancedCashierPage.setLoginInfo(loginInfo);
        enhancedCashierPage.setTxn(txnInfo);
        if (isLinkBasedMFOrSTTxn(requestData)) {
            enhancedCashierPage.setDisableBackButton(true);
        }
        // link data
        LinkAppPushData linkPushData;
        String linkType = null;
        if (StringUtils.isNotEmpty(requestData.getInvoiceId())) {
            linkType = TheiaConstant.LinkRiskParams.LINK_INVOICE_TYPE;
        } else if (StringUtils.isNotEmpty(requestData.getLinkId())) {
            linkType = TheiaConstant.LinkRiskParams.LINK_TYPE_NON_INVOICE;
        }
        linkPushData = new LinkAppPushData(
                ProcessTransactionHelper.replaceApostrophe(requestData.getLinkDescription()), requestData.getLongUrl()
                        + TheiaConstant.LinkBasedParams.LINK_INVOICE_CHANGE_NUMBER, linkType);

        if (StringUtils.isNotBlank(requestData.getAccountNumber())) {
            linkPushData.setAccountNumber(MaskingUtil.getMaskedBankAccountNumber(requestData.getAccountNumber(), 0, 4));
        }
        enhancedCashierPage.setLink(linkPushData);
        populateGroupedPayModes(enhancedCashierPage, requestData);
        return enhancedCashierPage;
    }

    private boolean v2AppInvoke() {
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        return StringUtils.equals(NATIVE_APP_INVOKE_URL_V2, servletRequest.getRequestURI());
    }

    private boolean isNotV1AppInvoke() {
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        return StringUtils.equals(NATIVE_APP_INVOKE_URL_V2, servletRequest.getRequestURI())
                || StringUtils.equals(NATIVE_APP_INVOKE_URL_V3, servletRequest.getRequestURI());
    }

    private PreAuthDetails getPreAuthDetails(PaymentRequestBean requestData) {
        if (requestData.isPreAuth()) {
            return new PreAuthDetails(true);
        }
        return null;
    }

    public boolean isFeatureEnabledOnCustId(PaymentRequestBean requestData) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", requestData.getCustId());
        if (iPgpFf4jClient.checkWithdefault("preLoginTheme", context, false)) {
            EXT_LOGGER.customInfo("Pre-Login theme is enabled");
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private void updateWalletInfoForMgv(List<EnhancedCashierPagePayModeBase> merchantPayModes,
            EnhancedCashierPageWalletInfo walletInfo) {
        // unchecking wallet by default if enough balance in mgv , see jira
        // PGPUI-289
        if (CollectionUtils.isEmpty(merchantPayModes))
            return;

        List payModes = merchantPayModes.stream().filter(p -> {
            if (StringUtils.equalsIgnoreCase(PayModeType.GIFT_VOUCHER.getType(), p.getType())) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(payModes))
            return;

        EnhancedCashierMgvPayMode mgvPayMode = (EnhancedCashierMgvPayMode) payModes.get(0);

        if (StringUtils.equalsIgnoreCase(PayModeType.GIFT_VOUCHER.getType(), mgvPayMode.getType())) {
            if (walletInfo != null && mgvPayMode.isSelected()) {
                walletInfo.setUsed(false);
            }
        }
    }

    void filterCCDCForPennyDropAddMoney(PaymentRequestBean requestData, List<EnhancedCashierPagePayModeBase> payModes) {
        if (requestData != null && CollectionUtils.isNotEmpty(payModes)) {
            if (StringUtils.equals(requestData.getTheme(), ADD_CARD_THEME)) {
                List<EnhancedCashierPagePayModeBase> ccdcpayModeList = payModes
                        .stream()
                        .filter(paymode -> (StringUtils.equalsIgnoreCase(EPayMethod.CREDIT_CARD.getNewDisplayName(),
                                paymode.getName()) || StringUtils.equalsIgnoreCase(
                                EPayMethod.DEBIT_CARD.getNewDisplayName(), paymode.getName())))
                        .collect(Collectors.toList());

                if (CollectionUtils.isNotEmpty(ccdcpayModeList)) {
                    payModes.clear();
                    payModes.addAll(ccdcpayModeList);
                }
            }
        }
    }

    private List<String> getUpiLinkedBanks(UserProfileSarvatraV4 userProfileSarvatraV4) {
        if (userProfileSarvatraV4 != null && userProfileSarvatraV4.getRespDetails() != null
                && userProfileSarvatraV4.getRespDetails().getProfileDetail() != null
                && userProfileSarvatraV4.getRespDetails().getProfileDetail().getBankAccounts() != null) {
            List<UpiBankAccountV4> bankAccounts = userProfileSarvatraV4.getRespDetails().getProfileDetail()
                    .getBankAccounts();
            return bankAccounts.stream().map(b -> b.getBank()).collect(Collectors.toList());
        }
        return null;
    }

    private UserProfileSarvatraV4 getUserProfileSarvatraV4(NativeCashierInfoResponse nativeCashierInfoResponse) {
        UserProfileSarvatraV4 userProfileSarvatraV4 = null;
        if (nativeCashierInfoResponse.getBody() != null
                && nativeCashierInfoResponse.getBody().getMerchantPayOption() != null
                && nativeCashierInfoResponse.getBody().getMerchantPayOption().getUpiProfileV4() != null) {
            userProfileSarvatraV4 = nativeCashierInfoResponse.getBody().getMerchantPayOption().getUpiProfileV4();
        } else if (nativeCashierInfoResponse.getBody() != null
                && nativeCashierInfoResponse.getBody().getAddMoneyPayOption() != null
                && nativeCashierInfoResponse.getBody().getAddMoneyPayOption().getUpiProfileV4() != null) {
            userProfileSarvatraV4 = nativeCashierInfoResponse.getBody().getAddMoneyPayOption().getUpiProfileV4();
        }

        return userProfileSarvatraV4;
    }

    private NpciHealthData getNpciHealth(UserProfileSarvatraV4 userProfileSarvatraV4) {

        if (userProfileSarvatraV4 != null && userProfileSarvatraV4.getRespDetails() != null
                && userProfileSarvatraV4.getRespDetails().getMetaDetails() != null) {
            return userProfileSarvatraV4.getRespDetails().getMetaDetails();
        } else {
            LOGGER.info("UserprofileV4 is empty , fetching npci health from cache");
            return npciHealthUtil.getNpciHealthViaCache();
        }
    }

    private UpiMetaData getUpiMetaData(UserProfileSarvatraV4 userProfileSarvatraV4) {
        UpiMetaData upiMetaData = new UpiMetaData();

        if (userProfileSarvatraV4 != null && userProfileSarvatraV4.getRespDetails() != null
                && userProfileSarvatraV4.getRespDetails().getProfileDetail() != null) {
            upiMetaData.setDeviceBinded(userProfileSarvatraV4.getRespDetails().getProfileDetail().isDeviceBinded());
            upiMetaData.setProfileStatus(userProfileSarvatraV4.getRespDetails().getProfileDetail().getProfileStatus());
            upiMetaData.setUpiLinkedMobileNumber(userProfileSarvatraV4.getRespDetails().getProfileDetail()
                    .getUpiLinkedMobileNumber());
            return upiMetaData;
        }

        return null;
    }

    public void updateWalletInfoFromSubscriptionDetails(EnhancedCashierPage enhancedCashierPage,
            NativeCashierInfoResponse nativeCashierInfoResponse, PaymentRequestBean requestData) {

        if (requestData.isSubscription()) {

            enhancedCashierPage.setSubscriptionDetail(nativeCashierInfoResponse.getBody().getSubscriptionDetail());

            if (SubsPaymentMode.NORMAL.name().equalsIgnoreCase(requestData.getSubsPaymentMode())) {

                // set ui-related flags
                enhancedCashierPage.getWallet().setEnabled(false);
                enhancedCashierPage.getTxn().setInsufficientBalance(true);

                // set credit-card as default paymode in case of NORMAL
                // subspaymode
                if (enhancedCashierPage.getAddMoneyPayModes() != null) {
                    List<EnhancedCashierPagePayModeBase> savedCardPayMode = enhancedCashierPage.getAddMoneyPayModes()
                            .stream().filter(object -> object instanceof EnhancedCashierSavedCard)
                            .collect(Collectors.toList());
                    if (savedCardPayMode.size() > 0) {
                        savedCardPayMode.get(0).setSelected(true);
                    } else {
                        List<EnhancedCashierPagePayModeBase> creditCardPayMode = enhancedCashierPage
                                .getAddMoneyPayModes()
                                .stream()
                                .filter(object -> EPayMethod.CREDIT_CARD.getDisplayName().equalsIgnoreCase(
                                        object.getName())).collect(Collectors.toList());
                        if (creditCardPayMode.size() > 0) {
                            creditCardPayMode.get(0).setSelected(true);
                        }
                    }
                }
            } else if (SubsPaymentMode.PPI.name().equalsIgnoreCase(requestData.getSubsPaymentMode())
                    && "Y".equalsIgnoreCase(requestData.getSubsPPIOnly())) {
                enhancedCashierPage.getWallet().setEnabled(false);
            }
        }
    }

    private void setMandateAccountDetailsForBankMandates(EnhancedCashierPage enhancedCashierPage,
            NativeCashierInfoResponse nativeCashierInfoResponse, PaymentRequestBean requestData) {
        if (nativeCashierInfoResponse.getBody().getMandateAccountDetails() != null) {
            enhancedCashierPage
                    .setMandateAccountDetails(nativeCashierInfoResponse.getBody().getMandateAccountDetails());
        }
    }

    private boolean isZeroRupeesSubscription(String txnAmountString) {
        try {
            Double txnAmount = Double.parseDouble(txnAmountString);
            return txnAmount.equals(0d);
        } catch (Exception ex) {
            LOGGER.error("Invalid Txn Amount");
        }
        return false;
    }

    private void checkAndSetIfNotPayModesConfiguredForPromoCode(List<EnhancedCashierPagePayModeBase> addMoneyPayModes,
            NativeCashierInfoResponse nativeCashierInfoResponse) {

        HttpServletRequest httpServletRequest = EnvInfoUtil.httpServletRequest();
        if (CollectionUtils.isEmpty(addMoneyPayModes) && nativeCashierInfoResponse != null
                && nativeCashierInfoResponse.getBody() != null
                && nativeCashierInfoResponse.getBody().getMerchantPayOption() != null
                && CollectionUtils.isEmpty(nativeCashierInfoResponse.getBody().getMerchantPayOption().getPayMethods())
                && nativeCashierInfoResponse.getBody().getPromoCodeData() != null) {

            httpServletRequest.setAttribute(TheiaConstant.RequestTypes.PAY_MODE_NOT_PRESENT_FOR_PROMO, true);

        }
    }

    private EnhancedCashierPageDynamicQR getEnhancedCashierPageDynamicQR(PaymentRequestBean requestData,
            NativeCashierInfoResponse nativeCashierInfoResponse) {
        QRCodeDetailsResponse qrCodeDetailsResponse = nativeCashierInfoResponse.getBody().getQrCodeDetailsResponse();
        EnhancedCashierPageDynamicQR dynamicQR = null;
        if (qrCodeDetailsResponse != null) {
            String pageTimeout = ConfigurationUtil.getProperty(
                    TheiaConstant.ExtraConstants.ENHANCE_DYNAMIC_QR_PAGE_TIMEOUT, "4800000");
            boolean isPRN = merchantPreferenceService.isPRNEnabled(requestData.getMid());
            boolean isUPIQR = QrType.UPI_QR == qrCodeDetailsResponse.getQrType();
            String displayMessage;
            if (isUPIQR)
                displayMessage = ConfigurationUtil
                        .getProperty(TheiaConstant.ExtraConstants.ENHANCE_DYNAMIC_UPI_QR_DISPLAY_MESSAGE);
            else
                displayMessage = ConfigurationUtil
                        .getProperty(TheiaConstant.ExtraConstants.ENHANCE_DYNAMIC_PAYTM_QR_DISPLAY_MESSAGE);
            dynamicQR = new EnhancedCashierPageDynamicQR(qrCodeDetailsResponse.getPath(), Long.valueOf(pageTimeout),
                    displayMessage, true, isPRN, isUPIQR);
        } else if (nativeCashierInfoResponse.getBody().getQrDetail() != null) {
            QrDetail qrDetail = nativeCashierInfoResponse.getBody().getQrDetail();
            dynamicQR = new EnhancedCashierPageDynamicQR(qrDetail.getDataUrl(), qrDetail.getPageTimeout(),
                    qrDetail.getDisplayMessage(), true, qrDetail.isPrn(), qrDetail.isUpiQR());
        }
        return dynamicQR;
    }

    private NativeCashierInfoResponse setVPANull(NativeCashierInfoResponse nativeCashierInfoResponse) {
        if (nativeCashierInfoResponse != null) {
            NativeCashierInfoResponseBody body = nativeCashierInfoResponse.getBody();
            if (body != null) {
                maskPayOption(body.getAddMoneyPayOption());
                maskPayOption(body.getMerchantPayOption());
            }
        }
        return nativeCashierInfoResponse;
    }

    private PayOption maskPayOption(PayOption payOption) {
        if (payOption != null) {
            UserProfileSarvatra userProfileSarvatra = payOption.getUserProfileSarvatra();
            if (userProfileSarvatra != null) {
                PaytmVpaDetails vpaDetails = userProfileSarvatra.getResponse();
                if (vpaDetails != null) {
                    List<SarvatraVpaDetails> sarvatraVpaDetails = vpaDetails.getVpaDetails();
                    if (sarvatraVpaDetails != null) {
                        for (SarvatraVpaDetails s : sarvatraVpaDetails) {
                            if (s.getDefaultCredit() != null) {
                                s.getDefaultCredit().setName(null);
                            }
                            if (s.getDefaultDebit() != null) {
                                s.getDefaultDebit().setName(null);
                            }
                        }
                    }
                }
            }
        }
        return payOption;
    }

    public void setEnhancedCashierPageInCache(final PaymentRequestBean paymentRequestBean,
            final InitiateTransactionResponse initiateTransactionResponse, final EnhancedCashierPage enhancedCashierPage) {

        String mid = paymentRequestBean.getMid();
        String orderId = paymentRequestBean.getOrderId();

        int expiryTime = enhancedCashierPageServiceHelper.getTokenExpiryTime();

        String key = enhancedCashierPageServiceHelper.fetchRedisKey(mid, orderId);

        EnhanceCashierPageCachePayload payload = new EnhanceCashierPageCachePayload(paymentRequestBean,
                initiateTransactionResponse, enhancedCashierPage);

        nativeSessionUtil.setKey(key, payload, expiryTime);
    }

    private void updateAuthenticatedFlagInInitiateTransactionResponse(
            InitiateTransactionResponse initiateTransactionResponse, NativeCashierInfoResponse nativeCashierInfoResponse) {
        try {
            if (initiateTransactionResponse.getBody().isAuthenticated() == false
                    && nativeCashierInfoResponse.getBody().getUserDetails() != null) {
                initiateTransactionResponse.getBody().setAuthenticated(true);
            }
        } catch (Exception ex) {
            LOGGER.error("Exception occured while updating isAuthenticated in InitiateTransactionResponse : {}", ex);
        }
    }

    private NativeCashierInfoResponse getNativeCashierInfoResponse(PaymentRequestBean requestData,
            InitiateTransactionResponse initiateTransactionResponse, boolean payViewConsultV2Required) throws Exception {
        NativeCashierInfoRequest nativeCashierInfoRequest = createPayViewConsultRequest(requestData,
                initiateTransactionResponse);
        NativeCashierInfoContainerRequest nativeCashierInfoContainerRequest = new NativeCashierInfoContainerRequest(
                nativeCashierInfoRequest, requestData);
        IRequestProcessor<NativeCashierInfoContainerRequest, NativeCashierInfoResponse> nativePayViewConsultRequestProcessor = requestProcessorFactory
                .getRequestProcessor(RequestType.NATIVE_PAY_VIEW_CONSULT);

        if (payViewConsultV2Required) {
            boolean groupedPayModesEnabled = merchantPreferenceService.isGroupedPayModesEnabled(requestData.getMid(),
                    false);
            if (groupedPayModesEnabled)
                nativePayViewConsultRequestProcessor = requestProcessorFactory
                        .getRequestProcessor(RequestType.NATIVE_PAY_VIEW_CONSULT_V5);
        }

        if (ff4jUtils.isFeatureEnabled(TheiaConstant.FF4J.THEIA_SUBSCRIPTION_QR_ENABLED, false)
                && merchantPreferenceService.isSubscriptionQrEnabled(requestData.getMid())) {
            nativeCashierInfoContainerRequest.getNativeCashierInfoRequest().getBody().setDeepLinkRequired(true);
        }

        NativeCashierInfoResponse nativeCashierInfoResponse = nativePayViewConsultRequestProcessor
                .process(nativeCashierInfoContainerRequest);

        if (nativeCashierInfoResponse == null) {
            throw new TheiaServiceException("Fetch Payment Options response is null");
        }
        return nativeCashierInfoResponse;
    }

    /*
     * private EnhancedCashierPageLoginInfo getLoginInfo(PaymentRequestBean
     * requestData, InitiateTransactionResponse initiateTransactionResponse) {
     * EnhancedCashierPageLoginInfo loginInfo = new
     * EnhancedCashierPageLoginInfo(); boolean isPgAutoLoginEnabled =
     * merchantPreferenceService.isPgAutoLoginEnabled(requestData.getMid(),
     * true);
     * 
     * boolean isMobileNumberNonEditable =
     * merchantPreferenceService.isMobileNumberNonEditable(requestData.getMid(),
     * false); loginInfo.setMobileNumberNonEditable(isMobileNumberNonEditable);
     * loginInfo
     * .setLoginFlag(initiateTransactionResponse.getBody().isAuthenticated());
     * loginInfo.setPgAutoLoginEnabled(isPgAutoLoginEnabled); return loginInfo;
     * }
     */

    private UserInfo getUserInfoForEnhancedCashier(InitiateTransactionResponse initiateTransactionResponse,
            PaymentRequestBean paymentRequestBean, NativeCashierInfoResponse nativeCashierInfoResponse) {
        UserInfo userInfo = new UserInfo();

        String mobNo = paymentRequestBean.getMobileNo();
        String userName = paymentRequestBean.getUserName();

        UserDetailsBiz userDetailsBiz = null;

        if ((StringUtils.isBlank(userName) && StringUtils.isNotBlank(paymentRequestBean.getPaytmToken()))
                || initiateTransactionResponse.getBody().isAuthenticated()) {
            /*
             * This is case of login on cashierPage, we need to fetch
             * userDetails from cache
             */

            userDetailsBiz = nativeSessionUtil.getUserDetails(initiateTransactionResponse.getBody().getTxnToken());
            if (userDetailsBiz != null) {
                userName = userDetailsBiz.getUserName();
                mobNo = userDetailsBiz.getMobileNo();
                userInfo.setConsentForAutoDebitPref(userDetailsBiz.getConsentForAutoDebitPref());
                userInfo.setShowConsentSheetAutoDebit(userDetailsBiz.getShowConsentSheetAutoDebit());
                userInfo.setCapturePostpaidConsentForWalletTopUp(userDetailsBiz
                        .getCapturePostpaidConsentForWalletTopUp());

            }
        }

        if (mobNo != null && mobNo.length() > 10) {
            mobNo = mobNo.trim();
            mobNo = mobNo.substring(mobNo.length() - 10);
        }
        if (MobileNumberUtils.isValidMobile(mobNo)) {
            userInfo.setMobile(mobNo);
        }

        // mask merchant passed mobile no.
        if (userDetailsBiz == null && StringUtils.isNotBlank(userInfo.getMobile())
                && ff4JUtil.isFeatureEnabled(MASK_MOBILE_ON_CASHIER_PAGE_ENABLED, paymentRequestBean.getMid())) {
            userInfo.setMobile(mobileMaskHelper.getMaskedNumber(userInfo.getMobile()));
        }

        if (nativeCashierInfoResponse != null && nativeCashierInfoResponse.getBody() != null
                && nativeCashierInfoResponse.getBody().getUserDetails() != null) {

            UserDetails userDetails = nativeCashierInfoResponse.getBody().getUserDetails();
            userInfo.setPostpaidOnboardingStageMsg(userDetails.getPostpaidOnboardingStageMsg());
            userInfo.setUserEligibileForPostPaidOnboarding(userDetails.getUserEligibileForPostPaidOnboarding());
            userInfo.setPostpaidCreditLimit(userDetails.getPostpaidCreditLimit());

        }

        /*
         * displayName is to be set irrespective of onus or offus merchant
         */
        userInfo.setDisplayName(userName);
        userInfo.setCustId(paymentRequestBean.getCustId());
        return userInfo;
    }

    private EnhancedCashierTxnInfo getTxnInfo(PaymentRequestBean requestData,
            NativeCashierInfoResponse nativeCashierInfoResponse, EnhancedCashierPageWalletInfo walletInfo,
            boolean isSubscriptionRequest) throws Exception {
        EnhancedCashierTxnInfo txnInfo = new EnhancedCashierTxnInfo();
        txnInfo.setTxnAmount(requestData.getTxnAmount());
        txnInfo.setId(requestData.getOrderId());
        txnInfo.setType(nativeCashierInfoResponse.getBody().getPaymentFlow().getValue());
        txnInfo.setInsufficientBalance(false);
        txnInfo.setAddMoney(requestData.isNativeAddMoney());
        if (UserSubWalletType.GIFT_VOUCHER.getType().equals(
                nativeCashierInfoResponse.getBody().getAddMoneyDestination())
                && !addMoneyToGvConsentUtil.bypassGvConsentPage()) {
            txnInfo.setAddMoneyDestination(nativeCashierInfoResponse.getBody().getAddMoneyDestination());
            txnInfo.setGvConsentMsg(ConfigurationUtil.getProperty(TheiaConstant.GvConsent.GV_CONSENT_ENHANCE_MSK_KEY,
                    TheiaConstant.GvConsent.GV_CONSENT_ENHANCE_MSG));
        }
        txnInfo.setRedirectFlow("");
        if (StringUtils.isNotEmpty(requestData.getLinkId()) || StringUtils.isNotEmpty(requestData.getInvoiceId())) {
            txnInfo.setRedirectFlow(CommonConstants.RedirectionFlow.LINK);
        }

        txnInfo.setInsufficientBalance(processTransactionUtil.checkUserWalletSufficient(walletInfo,
                requestData.getTxnAmount(), nativeCashierInfoResponse, isSubscriptionRequest));

        txnInfo.setPcfEnabled(nativeCashierInfoResponse.getBody().isPcfEnabled());
        txnInfo.setAddMoneyPcfEnabled(nativeCashierInfoResponse.getBody().getAddMoneyPcfEnabled());

        /*
         * Handle wallet info display for wallet only merchant UI needs this
         * flag to stop allowing deselection of wallet
         */
        /*
         * if (walletInfo.isWalletOnly()) { walletInfo.setEnabled(false); }
         */
        return txnInfo;
    }

    private List<SavedVPAInstrument> getSavedVPAInfo(PaymentRequestBean requestData,
            InitiateTransactionResponse initiateTransactionResponse, PayOption payOption,
            EnhancedCashierPageDisplayIdGenerator displayIdGenerator) {

        PayMethod payMethod = payOption.getPayMethods().stream()
                .filter(s -> s.getPayMethod().equals(EPayMethod.UPI.getMethod())).findAny().orElse(null);
        if (payMethod != null && !Boolean.valueOf(payMethod.getIsDisabled().getStatus())) {
            /*
             * When doing any changes , change both functions and maintain
             * backward-compatibility
             */
            if (requestData != null && !StringUtils.equalsIgnoreCase("v2", requestData.getVersion())) {
                return getSavedVPAList(requestData, initiateTransactionResponse, displayIdGenerator,
                        payOption.getUserProfileSarvatra());
            } else {
                return getSavedVPAListV2(requestData, initiateTransactionResponse, displayIdGenerator,
                        payOption.getUpiProfileV4());
            }
        }

        return null;

    }

    private List<SavedVPAInstrument> getSavedVPAList(PaymentRequestBean requestData,
            InitiateTransactionResponse initiateTransactionResponse,
            EnhancedCashierPageDisplayIdGenerator displayIdGenerator, UserProfileSarvatra userprofile) {
        List<SavedVPAInstrument> SavedVPAInstrumentsList = new ArrayList<>();
        try {
            if (userprofile != null) {
                if (SUCCESS.equalsIgnoreCase(userprofile.getStatus()) && userprofile.getResponse() != null
                        && userprofile.getResponse().getVpaDetails() != null) {
                    List<SarvatraVpaDetails> payeeDataList = userprofile.getResponse().getVpaDetails();
                    Iterator<SarvatraVpaDetails> payeeDataIterator = payeeDataList.iterator();
                    while (payeeDataIterator.hasNext()) {
                        SarvatraVpaDetails sarvatraVpaDetail = payeeDataIterator.next();
                        if (sarvatraVpaDetail.isPrimary()) {
                            sarvatraVpaDetail.setDefaultCredit(null);
                            SavedVPAInstrument savedVPAInstrument = new SavedVPAInstrument();
                            savedVPAInstrument.setId(String.valueOf(displayIdGenerator.generateDisplayId()));
                            savedVPAInstrument.setVpaId(sarvatraVpaDetail.getName());
                            if (sarvatraVpaDetail.getDefaultDebit() == null
                                    || sarvatraVpaDetail.getDefaultDebit().getBank() == null) {
                                continue;
                            }
                            savedVPAInstrument.setBankName(sarvatraVpaDetail.getDefaultDebit().getBank());
                            savedVPAInstrument.setBankAccountNo(StringUtils.right(sarvatraVpaDetail.getDefaultDebit()
                                    .getAccount(), 4));

                            /*
                             * PGP-10766 "Name" in defaultCredit and
                             * defaultDebit of sarvatraVpaDetail is set to null
                             * because JSON parsing at UI Side caused
                             * parsingError(due to presence of special
                             * characters) resulting in white screen
                             */
                            if (sarvatraVpaDetail.getDefaultCredit() != null) {
                                sarvatraVpaDetail.getDefaultCredit().setName(null);
                            }
                            if (sarvatraVpaDetail.getDefaultDebit() != null) {
                                sarvatraVpaDetail.getDefaultDebit().setName(null);
                            }

                            savedVPAInstrument.setPayeeData(sarvatraVpaDetail);
                            savedVPAInstrument.setAuthMode(VPA_AUTH_MODE);
                            savedVPAInstrument.setType(VPA_TYPE);
                            savedVPAInstrument.setHybridDisabled(hybridDisablingUtil
                                    .isHybridDisabledForBank(savedVPAInstrument.getBankName()));
                            savedVPAInstrument.setDisplayName(savedVPAInstrument.getBankName() + " - "
                                    + EPayMethod.getPayMethodByMethod(EPayMethod.UPI.getMethod()));
                            SavedVPAInstrumentsList.add(savedVPAInstrument);
                        }
                    }
                }
            }

        } catch (Exception ex) {
            LOGGER.error("Error while fetching VPA detail for enhanced flow", ex);
        }
        return SavedVPAInstrumentsList;
    }

    private EnhancedCashierPageLoginInfo getLoginInfo(InitiateTransactionResponse initiateTransactionResponse,
            NativeCashierInfoResponse nativeCashierInfoResponse, String mid) {
        EnhancedCashierPageLoginInfo loginInfo = new EnhancedCashierPageLoginInfo();
        if (nativeCashierInfoResponse != null && nativeCashierInfoResponse.getBody().getLoginInfo() != null) {
            loginInfo.setPgAutoLoginEnabled(nativeCashierInfoResponse.getBody().getLoginInfo().isPgAutoLoginEnabled());
            loginInfo.setMobileNumberNonEditable(nativeCashierInfoResponse.getBody().getLoginInfo()
                    .isMobileNumberNonEditable());
        }
        loginInfo.setLoginFlag(initiateTransactionResponse.getBody().isAuthenticated());
        loginInfo.setLoginFlag(initiateTransactionResponse.getBody().isAuthenticated());
        if (nativeCashierInfoResponse.getBody().getLoginInfo() != null) {
            loginInfo.setDisableLoginStrip(nativeCashierInfoResponse.getBody().getLoginInfo().getDisableLoginStrip());
        }
        return loginInfo;
    }

    private String getVpaFromUpiProfileV4(List<VpaDetailV4> vpaDetails) {
        if (vpaDetails == null || CollectionUtils.isEmpty(vpaDetails)) {
            return null;
        }

        VpaDetailV4 vpaDetail = vpaDetails.stream().filter(s -> s.isPrimary()).findAny().orElse(null);
        if (vpaDetail != null && StringUtils.isNotEmpty(vpaDetail.getDefaultDebitAccRefId())) {
            return vpaDetail.getName();
        }

        return null;
    }

    private SavedVPAInstrument getCopy(SavedVPAInstrument savedVPAInstrument,
            EnhancedCashierPageDisplayIdGenerator displayIdGenerator, PaytmBanksVpaDefaultDebitCredit bankAccount,
            SarvatraVpaDetails sarvatraVpaDetail) {
        SarvatraVpaDetails sarvatraVpaDetails = new SarvatraVpaDetails();
        sarvatraVpaDetails.setDefaultCredit(null);
        sarvatraVpaDetails.setPrimary(true);
        sarvatraVpaDetails.setName(savedVPAInstrument.getPayeeData().getName());
        bankAccount.setName(null);
        sarvatraVpaDetails.setDefaultDebit(bankAccount);

        SavedVPAInstrument savedVPAInstrument1 = new SavedVPAInstrument();
        savedVPAInstrument1.setId(String.valueOf(displayIdGenerator.generateDisplayId()));
        savedVPAInstrument1.setVpaId(savedVPAInstrument.getVpaId());
        savedVPAInstrument1.setAuthMode(savedVPAInstrument.getAuthMode());
        savedVPAInstrument1.setType(savedVPAInstrument.getType());
        savedVPAInstrument1.setHybridDisabled(savedVPAInstrument.isHybridDisabled());
        sarvatraVpaDetails.setDefaultDebit(bankAccount);
        sarvatraVpaDetails.setDefaultCredit(null);

        savedVPAInstrument1.setPayeeData(sarvatraVpaDetails);
        savedVPAInstrument1.setBankAccountNo(StringUtils.right(bankAccount.getAccount(), 4));
        savedVPAInstrument1.setBankName(bankAccount.getBank());
        savedVPAInstrument1.setDisplayName(savedVPAInstrument1.getBankName() + " - "
                + EPayMethod.getPayMethodByMethod(EPayMethod.UPI.getMethod()));
        return savedVPAInstrument1;
    }

    private List<SavedInstrument> getSavedCardInfo(PayOption payOption,
            EnhancedCashierPageDisplayIdGenerator displayIdGenerator) {
        List<SavedInstrument> merchantSavedInstrumentsList = new ArrayList<>();
        if (payOption != null && BizParamValidator.validateInputListParam(payOption.getSavedInstruments())) {
            List<PayChannelBase> savedCards = payOption.getSavedInstruments();
            PayMethod creditCard = null;
            PayMethod debitCard = null;
            if (payOption.getPayMethods() != null && !payOption.getPayMethods().isEmpty()) {
                List<PayMethod> payMethods = payOption.getPayMethods();
                creditCard = payMethods.stream()
                        .filter(s -> EPayMethod.CREDIT_CARD.getMethod().equals(s.getPayMethod())).findAny()
                        .orElse(null);

                debitCard = payMethods.stream().filter(s -> EPayMethod.DEBIT_CARD.getMethod().equals(s.getPayMethod()))
                        .findAny().orElse(null);
            }
            for (PayChannelBase saved : savedCards) {
                if (saved instanceof SavedCard && creditCard != null
                        && ((SavedCard) saved).getCardDetails().getCardType().equals(creditCard.getPayMethod())) {
                    getSavedCardData(saved, merchantSavedInstrumentsList, displayIdGenerator);
                } else if (saved instanceof SavedCard && debitCard != null
                        && ((SavedCard) saved).getCardDetails().getCardType().equals(debitCard.getPayMethod())) {
                    getSavedCardData(saved, merchantSavedInstrumentsList, displayIdGenerator);
                }
            }
        }
        return merchantSavedInstrumentsList;
    }

    private EnhancedCashierPageMerchantInfo getMerchantUserInfoResponse(PaymentRequestBean requestData,
            InitiateTransactionResponse initiateTransactionResponse, NativeCashierInfoResponse nativeCashierInfoResponse)
            throws Exception {
        FetchMerchantUserInfoRequest merchantUserInfoRequest = createFetchMerchantUserInfoRequest(requestData,
                initiateTransactionResponse);
        IRequestProcessor<FetchMerchantUserInfoRequest, MerchantUserInfoResponse> merchantUserInfoRequestProcessor = requestProcessorFactory
                .getRequestProcessor(RequestType.FETCH_MERCHANT_USER_INFO);
        MerchantUserInfoResponse merchantUserInfoResponse = merchantUserInfoRequestProcessor
                .process(merchantUserInfoRequest);

        EnhancedCashierPageMerchantInfo merchantInfo = null;

        if (nativeCashierInfoResponse != null && nativeCashierInfoResponse.getBody().getMerchantDetails() != null
                && nativeCashierInfoResponse.getBody().getMerchantDetails().getMerchantVpa() != null) {

            merchantInfo = new EnhancedCashierPageMerchantInfo(requestData.getMid(),
                    ProcessTransactionHelper.replaceApostrophe(merchantUserInfoResponse.getBody().getMerchantInfoResp()
                            .getMerDispname()), merchantUserInfoResponse.getBody().getMerchantInfoResp()
                            .getMerLogoUrl(), nativeCashierInfoResponse.getBody().getMerchantDetails().getMerchantVpa());
        } else {
            merchantInfo = new EnhancedCashierPageMerchantInfo(requestData.getMid(),
                    ProcessTransactionHelper.replaceApostrophe(merchantUserInfoResponse.getBody().getMerchantInfoResp()
                            .getMerDispname()), merchantUserInfoResponse.getBody().getMerchantInfoResp()
                            .getMerLogoUrl());
        }

        /*
         * put addMoneyMerchantVpa for addNPay
         */
        if (nativeCashierInfoResponse != null
                && StringUtils.equals(EPayMode.ADDANDPAY.getValue(), nativeCashierInfoResponse.getBody()
                        .getPaymentFlow().getValue())) {
            if (nativeCashierInfoResponse.getBody().getAddMoneyPayOption() != null
                    && nativeCashierInfoResponse.getBody().getAddMoneyMerchantDetails() != null
                    && StringUtils.isNotBlank(nativeCashierInfoResponse.getBody().getAddMoneyMerchantDetails()
                            .getMerchantVpa())) {
                merchantInfo.setAddMoneyMerchantVpa(nativeCashierInfoResponse.getBody().getAddMoneyMerchantDetails()
                        .getMerchantVpa());
            }
        }

        return merchantInfo;
    }

    private List<SavedVPAInstrument> getSavedVPAListV2(PaymentRequestBean requestData,
            InitiateTransactionResponse initiateTransactionResponse,
            EnhancedCashierPageDisplayIdGenerator displayIdGenerator, UserProfileSarvatraV4 userProfileSarvatraV4) {
        List<SavedVPAInstrument> SavedVPAInstrumentsList = new ArrayList<>();
        try {
            if (userProfileSarvatraV4 != null) {
                if (SUCCESS.equalsIgnoreCase(userProfileSarvatraV4.getStatus())
                        && userProfileSarvatraV4.getRespDetails() != null
                        && userProfileSarvatraV4.getRespDetails().getProfileDetail() != null) {
                    List<UpiBankAccountV4> payeeDataList = userProfileSarvatraV4.getRespDetails().getProfileDetail()
                            .getBankAccounts();
                    Iterator<UpiBankAccountV4> payeeDataIterator = payeeDataList.iterator();
                    while (payeeDataIterator.hasNext()) {
                        UpiBankAccountV4 sarvatraVpaDetail = payeeDataIterator.next();
                        SavedVPAInstrument savedVPAInstrument = new SavedVPAInstrument();
                        savedVPAInstrument.setId(String.valueOf(displayIdGenerator.generateDisplayId()));
                        savedVPAInstrument.setVpaId(getVpaFromUpiProfileV4(userProfileSarvatraV4.getRespDetails()
                                .getProfileDetail().getVpaDetails()));
                        if (sarvatraVpaDetail.getBank() == null || savedVPAInstrument.getVpaId() == null) {
                            continue;
                        }
                        savedVPAInstrument.setBankName(sarvatraVpaDetail.getBank());
                        savedVPAInstrument.setBankAccountNo(StringUtils.right(
                                sarvatraVpaDetail.getMaskedAccountNumber(), 4));
                        sarvatraVpaDetail.setName(savedVPAInstrument.getVpaId());

                        savedVPAInstrument.setPayeeDataV2(sarvatraVpaDetail);
                        savedVPAInstrument.setAuthMode(VPA_AUTH_MODE);
                        savedVPAInstrument.setType(VPA_TYPE);
                        savedVPAInstrument.setHybridDisabled(hybridDisablingUtil
                                .isHybridDisabledForBank(savedVPAInstrument.getBankName()));
                        savedVPAInstrument.setDisplayName(savedVPAInstrument.getBankName() + " - "
                                + EPayMethod.getPayMethodByMethod(EPayMethod.UPI.getMethod()));

                        if (savedVPAInstrument.getPayeeDataV2() != null) {
                            if (CREDIT.equalsIgnoreCase(savedVPAInstrument.getPayeeDataV2().getAccountType())) {
                                savedVPAInstrument.setBankLogoUrl(savedVPAInstrument.getPayeeDataV2().getLogoUrl());
                            } else {
                                savedVPAInstrument.setBankLogoUrl(commonFacade.getBankLogo(savedVPAInstrument
                                        .getPayeeDataV2().getPgBankCode()));
                            }
                        }

                        SavedVPAInstrumentsList.add(savedVPAInstrument);
                    }
                }
            }

        } catch (Exception ex) {
            LOGGER.error("Error while fetching VPA detail for enhanced flow", ex);
        }
        return SavedVPAInstrumentsList;
    }

    private boolean isMandateAccountReceivedInCreateSubscription(NativeCashierInfoResponse nativeCashierInfoResponse) {
        return nativeCashierInfoResponse.getBody().getMandateAccountDetails() != null
                && StringUtils.isNotBlank(nativeCashierInfoResponse.getBody().getMandateAccountDetails()
                        .getAccountNumber());
    }

    private List<EnhancedCashierPagePayModeBase> getEnhancedCashierPageMerchantPayModes(PaymentRequestBean requestData,
            InitiateTransactionResponse initiateTransactionResponse,
            NativeCashierInfoResponse nativeCashierInfoResponse,
            EnhancedCashierPageDisplayIdGenerator displayIdGenerator, EnhancedCashierPageWalletInfo walletInfo)
            throws Exception {

        List<EnhancedCashierPagePayModeBase> merchantPayModes = new ArrayList<>();

        if (walletInfo != null
                && (requestData != null && requestData.getMid() != null && !merchantPreferenceService
                        .isPostpaidOnlyMerchant(requestData.getMid(), false))) {
            walletInfo.setId(displayIdGenerator.generateDisplayId());
            walletInfo.setPayMethod(EPayMethod.WALLET.getMethod());
            merchantPayModes.add(walletInfo);
        }

        EnhancedCashierMgvPayMode mgvPayMode = buildMGVPayMode(requestData, nativeCashierInfoResponse.getBody()
                .getMerchantPayOption(), nativeCashierInfoResponse.getBody().isPcfEnabled(), displayIdGenerator);
        if (mgvPayMode != null) {
            mgvPayMode.setPayMethod(EPayMethod.GIFT_VOUCHER.getMethod());
            merchantPayModes.add(mgvPayMode);
        }

        // Add Saved VPA in MerchantPayModes
        EnhancedCashierSavedVPA savedVPAPayMode = buildEnhancedCashierSavedVpa(requestData,
                initiateTransactionResponse, nativeCashierInfoResponse.getBody().getMerchantPayOption(),
                displayIdGenerator);
        if (savedVPAPayMode != null) {
            savedVPAPayMode.setPayMethod(EPayMethod.SAVED_VPA.getMethod());
            merchantPayModes.add(savedVPAPayMode);
        }

        // Add Saved cards in MerchantPayModes
        EnhancedCashierPagePayModeBase savedCardPaymode = buildEnhancedCashierSavedCard(nativeCashierInfoResponse
                .getBody().getMerchantPayOption(), displayIdGenerator);
        if (savedCardPaymode != null) {
            savedCardPaymode.setPayMethod(EPayMethod.SAVED_CARD.getMethod());
            merchantPayModes.add(savedCardPaymode);
        }

        // Add other payModes CC,DC,NB etc in MerchantPayModes
        for (PayMethod payMethodIterator : nativeCashierInfoResponse.getBody().getMerchantPayOption().getPayMethods()) {

            EnhancedCashierPagePayModeBase payMode = buildEnhancedCashierPagePaymentMode(payMethodIterator,
                    nativeCashierInfoResponse, displayIdGenerator, requestData, false, walletInfo);
            if (payMode != null) {
                payMode.setPayMethod(payMethodIterator.getPayMethod());
                merchantPayModes.add(payMode);
            }
        }

        /*
         * Add Saved Mandate Banks in MerchantPayModes if MandateType received
         * is E-mandate
         */
        if (CollectionUtils.isNotEmpty(nativeCashierInfoResponse.getBody().getMerchantPayOption()
                .getSavedMandateBanks())
                && MandateMode.E_MANDATE.name().equals(requestData.getMandateType())
                && !isMandateAccountReceivedInCreateSubscription(nativeCashierInfoResponse)) {
            EnhancedCashierSavedMandateBank savedMandateBank = buildSavedBankMandatePayMode(
                    nativeCashierInfoResponse.getBody(), displayIdGenerator);
            if (savedMandateBank != null) {
                savedMandateBank.setPayMethod(EPayMethod.SAVED_MANDATE_BANK.getMethod());
                merchantPayModes.add(savedMandateBank);
            }

        }

        sortMerchantPaymode(requestData.getMid(), merchantPayModes, PayModeOrder.MERCHANT_PAYMODES_ORDERING,
                requestData.isSubscription() ? PaymodeSequenceEnum.SUBSCRIPTION : PaymodeSequenceEnum.ENHANCE,
                requestData.getPaymodeSequence());
        return merchantPayModes;
    }

    private String getPaytmProperty(String propertyName) {
        PaytmProperty paytmProperty = configurationDataService.getPaytmProperty(propertyName);
        if (paytmProperty != null) {
            return paytmProperty.getValue();
        }
        return StringUtils.EMPTY;
    }

    private List<EnhancedCashierPagePayModeBase> getEnhancedCashierPageAddMoneyPayModes(PaymentRequestBean requestData,
            InitiateTransactionResponse initiateTransactionResponse,
            NativeCashierInfoResponse nativeCashierInfoResponse,
            EnhancedCashierPageDisplayIdGenerator displayIdGenerator, EnhancedCashierPageWalletInfo walletInfo)
            throws Exception {

        // to fetch addMoneyPayModes, paymentFlow must be ADDANDPAY
        if (!EPayMode.ADDANDPAY.getValue().equals(nativeCashierInfoResponse.getBody().getPaymentFlow().getValue())) {
            return null;
        }

        List<EnhancedCashierPagePayModeBase> addMoneyPayModes = null;

        if (nativeCashierInfoResponse != null
                && nativeCashierInfoResponse.getBody() != null
                && BizParamValidator.validateInputObjectParam(nativeCashierInfoResponse.getBody()
                        .getAddMoneyPayOption())
                && nativeCashierInfoResponse.getBody().getAddMoneyPayOption().getPayMethods() != null) {

            addMoneyPayModes = new ArrayList<>();

            // Add Saved VPA in AddMoneyPayModes
            EnhancedCashierSavedVPA savedVPAPayMode = buildEnhancedCashierSavedVpa(requestData,
                    initiateTransactionResponse, nativeCashierInfoResponse.getBody().getAddMoneyPayOption(),
                    displayIdGenerator);
            if (savedVPAPayMode != null) {
                savedVPAPayMode.setPayMethod(EPayMethod.SAVED_VPA.getMethod());
                addMoneyPayModes.add(savedVPAPayMode);
            }

            // Add Saved cards in AddMoneyPayModes
            EnhancedCashierPagePayModeBase savedCardPaymode = buildEnhancedCashierSavedCard(nativeCashierInfoResponse
                    .getBody().getAddMoneyPayOption(), displayIdGenerator);
            if (savedCardPaymode != null) {
                savedCardPaymode.setPayMethod(EPayMethod.SAVED_CARD.getMethod());
                addMoneyPayModes.add(savedCardPaymode);
            }

            // Add other payModes CC,DC,NB etc in AddMoneyPayModes
            for (PayMethod payMethodIterator : nativeCashierInfoResponse.getBody().getAddMoneyPayOption()
                    .getPayMethods()) {
                EnhancedCashierPagePayModeBase payMode = buildEnhancedCashierPagePaymentMode(payMethodIterator,
                        nativeCashierInfoResponse, displayIdGenerator, requestData, true, walletInfo);
                if (payMode != null) {
                    payMode.setPayMethod(payMethodIterator.getPayMethod());
                    addMoneyPayModes.add(payMode);
                }
            }
            sortMerchantPaymode(requestData.getMid(), addMoneyPayModes, PayModeOrder.ADD_MONEY_PAYMODE,
                    requestData.isSubscription() ? PaymodeSequenceEnum.SUBSCRIPTION : PaymodeSequenceEnum.ENHANCE,
                    requestData.getPaymodeSequence());
        }
        return addMoneyPayModes;
    }

    private NativeCashierInfoRequest createPayViewConsultRequest(PaymentRequestBean requestData,
            InitiateTransactionResponse initiateTransactionResponse) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss");
        NativeCashierInfoRequest nativeCashierInfoRequest = new NativeCashierInfoRequest();
        TokenRequestHeader requestHeader = new TokenRequestHeader();
        NativeCashierInfoRequestBody body = new NativeCashierInfoRequestBody();
        requestHeader.setRequestTimestamp(sdf.format(new Timestamp(System.currentTimeMillis())).toString());
        requestHeader.setChannelId(EChannelId.valueOf(requestData.getChannelId()));
        requestHeader.setTxnToken(initiateTransactionResponse.getBody().getTxnToken());
        decideAndSetFetchPayOptionVersion(requestData);
        requestHeader.setVersion(requestData.getVersion());
        nativeCashierInfoRequest.setHead(requestHeader);
        body.setPostpaidOnboardingSupported(requestData.isPostpaidOnboardingSupported());
        body.setOriginChannel(requestData.getOriginChannel());
        if (merchantPreferenceService.isCheckoutJsOnEnhancedFlowEnabled(requestData.getMid())) {
            requestHeader.setWorkFlow(CHECKOUT);
        }
        if (StringUtils.isBlank(body.getApplyPaymentOffer())) {
            body.setApplyPaymentOffer(requestData.getApplyPaymentOffer());
        }
        if (StringUtils.isBlank(body.getFetchAllPaymentOffers())) {
            body.setFetchAllPaymentOffers(requestData.getFetchAllPaymentOffers());
        }
        /**
         * This is set to true coz enhance needs no backward compatibilty
         */
        body.setUpiRecurringSupport(true);
        body.setMid(requestData.getMid());
        body.setOrderId(requestData.getOrderId());
        body.setPaymodeSequenceEnum(PaymodeSequenceEnum.ENHANCE);
        if (iPgpFf4jClient.checkWithdefault(TheiaConstant.FF4J.THEIA_ENHANCED_RETURN_DISABLED_CHANNELS,
                new HashMap<>(), false)) {
            body.setReturnDisabledChannels(true);
        }
        nativeCashierInfoRequest.setBody(body);
        return nativeCashierInfoRequest;
    }

    private void decideAndSetFetchPayOptionVersion(PaymentRequestBean requestData) {
        if (StringUtils.equals(ConfigurationUtil.getProperty("enable.fetchpayoption.v2.enhance", StringUtils.EMPTY),
                "true")) {
            /*
             * set to 'v2' to fetch UPI saved accounts (v4/profile) for
             * subscription request types in case of web and m-web PGP-24145
             */
            if (ERequestType.isSubscriptionCreationRequest(requestData.getRequestType())
                    || ERequestType.SUBSCRIBE.getType().equals(requestData.getRequestType())) {
                requestData.setVersion("v2");
            } else if (merchantPreferenceService.isCheckoutJsOnEnhancedFlowEnabled(requestData.getMid())) {
                requestData.setVersion("v2");
            } else if (StringUtils.isBlank(requestData.getAppVersion())
                    && StringUtils.equals(EChannelId.WAP.getValue(), requestData.getChannelId())) {
                requestData.setVersion("v1");
            } else {
                requestData.setVersion("v2");
            }
        } else {
            requestData.setVersion("v1");
        }
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.REQUEST_VALIDATION)
    @Override
    public ValidationResults validatePaymentRequest(final PaymentRequestBean requestData) {
        final boolean validateChecksum = validateChecksum(requestData);

        if (!validateChecksum) {
            return ValidationResults.CHECKSUM_VALIDATION_FAILURE;
        }

        return ValidationResults.VALIDATION_SUCCESS;
    }

    /*
     * private List<ShippingInfo> getShippingInfo(PaymentRequestBean
     * requestData) { List<ShippingInfo> shippingInfoList = new
     * ArrayList<ShippingInfo>(); ShippingInfo shippingInfo = new
     * ShippingInfo(); shippingInfo.setAddress1(requestData.getAddress1());
     * shippingInfo.setAddress2(requestData.getAddress2());
     * shippingInfo.setEmail(requestData.getEmail());
     * shippingInfo.setMobileNo(requestData.getMobileNo());
     * shippingInfo.setStateName(requestData.getState());
     * shippingInfo.setCityName(requestData.getCity());
     * shippingInfo.setZipCode(requestData.getPincode());
     * shippingInfoList.add(shippingInfo); return shippingInfoList; }
     */

    private UserInfo getUserInfo(PaymentRequestBean requestData) {
        UserInfo userInfo = new UserInfo();
        userInfo.setCustId(requestData.getCustId());
        userInfo.setEmail(requestData.getEmail());
        userInfo.setMobile(requestData.getMobileNo());
        userInfo.setAddress(requestData.getAddress1());
        userInfo.setPincode(requestData.getPincode());
        return userInfo;
    }

    private ExtendInfo getExtendedInfo(PaymentRequestBean requestData) {
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setUdf1(requestData.getUdf1());
        extendInfo.setUdf2(requestData.getUdf2());
        extendInfo.setUdf3(requestData.getUdf3());
        extendInfo.setMercUnqRef(requestData.getMerchUniqueReference());
        extendInfo.setComments(requestData.getComments());
        extendInfo.setSubwalletAmount(getSubwalletAmount(requestData.getSubwalletAmount()));
        return extendInfo;
    }

    public Map<String, String> getSubwalletAmount(Map<UserSubWalletType, BigDecimal> requestDataSubwallets) {

        Map<String, String> mappedSubwallets = null;

        if (requestDataSubwallets != null) {
            mappedSubwallets = new HashMap<>();
            for (Map.Entry<UserSubWalletType, BigDecimal> subwallet : requestDataSubwallets.entrySet()) {
                UserSubWalletType walletType = subwallet.getKey();
                BigDecimal amount = subwallet.getValue();

                mappedSubwallets.put(walletType.getType(), amount.toString());
            }
        }

        return mappedSubwallets;
    }

    private List<PaymentMode> getDisablePaymentModes(PaymentRequestBean requestData) {

        List<PaymentMode> disablePaymentModeList = new ArrayList<PaymentMode>();

        if (StringUtils.isNotBlank(requestData.getDisabledPaymentMode())) {

            List<String> payModeList = Arrays.asList(requestData.getDisabledPaymentMode().split(
                    PAYMENT_MODE_SPLIT_REGEX));

            for (String payMode : payModeList) {
                PaymentMode paymentMode = new PaymentMode();
                paymentMode.setMode(EPayMethod.getPayMethodByOldName(payMode).toString());

                if (EPayMethod.EMI == EPayMethod.getPayMethodByOldName(payMode)) {
                    paymentMode.setChannels(getEmiOptionChannelList(requestData.getEmiOption(),
                            EMI_CHANNEL_DISABLE_SUFIX, PAYMENT_MODE_SPLIT_REGEX));
                }

                disablePaymentModeList.add(paymentMode);
            }

        }
        if (StringUtils.isNotBlank(requestData.getEmiOption())) {
            List<String> channelList = getEmiOptionChannelList(requestData.getEmiOption(), EMI_CHANNEL_DISABLE_SUFIX,
                    EMI_OPTIONS_BANK_SPLIT_REGEX);
            if (!channelList.isEmpty()) {
                PaymentMode paymentMode = new PaymentMode();
                paymentMode.setMode(EPayMethod.getPayMethodByOldName(EPayMethod.EMI.toString()).toString());

                if (EPayMethod.EMI == EPayMethod.getPayMethodByOldName(EPayMethod.EMI.toString())) {
                    paymentMode.setChannels(getEmiOptionChannelList(requestData.getEmiOption(),
                            EMI_CHANNEL_DISABLE_SUFIX, EMI_OPTIONS_BANK_SPLIT_REGEX));
                }
                disablePaymentModeList.add(paymentMode);
            }

        }

        // Disable COD if paymentTypeId does not contain COD
        if (!StringUtils.containsIgnoreCase(requestData.getPaymentTypeId(), EPayMethod.MP_COD.getOldName())) {
            disablePaymentModeList.add(new PaymentMode(EPayMethod.MP_COD.getOldName()));
        }

        return disablePaymentModeList;
    }

    private List<PaymentMode> getEnablePaymentModes(PaymentRequestBean requestData) {

        List<PaymentMode> enablePaymentModeList = new ArrayList<PaymentMode>();

        if (PAYMENT_MODE_ONLY_FLAG.equalsIgnoreCase(requestData.getPaymentModeOnly())
                && StringUtils.isNotBlank(requestData.getPaymentTypeId())) {

            List<String> payModeList = Arrays.asList(requestData.getPaymentTypeId().split(PAYMENT_MODE_SPLIT_REGEX));
            for (String payMode : payModeList) {
                PaymentMode paymentMode = new PaymentMode();
                if (null != EPayMethod.getPayMethodByOldName(payMode)) {
                    paymentMode.setMode(EPayMethod.getPayMethodByOldName(payMode).toString());
                    if (EPayMethod.EMI == EPayMethod.getPayMethodByOldName(payMode)) {
                        paymentMode.setChannels(getEmiOptionChannelList(requestData.getEmiOption(),
                                EMI_CHANNEL_ENABLE_SUFIX, PAYMENT_MODE_SPLIT_REGEX));
                    }
                    enablePaymentModeList.add(paymentMode);
                }

            }
        }
        return enablePaymentModeList;
    }

    private List<String> getEmiOptionChannelList(String emiOptions, String sufix, String separator) {

        List<String> channelList = new ArrayList<String>();

        if (StringUtils.isNotBlank(emiOptions)) {
            List<String> emiOptionList = Arrays.asList(emiOptions.split(separator));
            for (String emiOpton : emiOptionList) {
                if (emiOpton.toUpperCase().endsWith(sufix)) {
                    channelList.add(emiOpton.replaceAll(sufix, ""));
                }

            }
        }
        return channelList;
    }

    private InitiateTransactionRequest createInitiateTransactionRequest(PaymentRequestBean requestData)
            throws IOException {

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss");

        InitiateTransactionRequest initiateTransactionRequest = new InitiateTransactionRequest();

        SecureRequestHeader requestHeader = new SecureRequestHeader();
        requestHeader.setRequestTimestamp(sdf.format(new Timestamp(System.currentTimeMillis())).toString());
        requestHeader.setChannelId(EChannelId.valueOf(requestData.getChannelId()));
        requestHeader.setSignature(requestData.getChecksumhash());
        requestHeader.setWorkFlow(TheiaConstant.EnhancedCashierFlow.ENHANCED_CASHIER_FLOW);
        initiateTransactionRequest.setHead(requestHeader);

        InitiateTransactionRequestBody requestBody = new InitiateTransactionRequestBody();

        requestBody.setRequestType(requestData.getRequestType());
        requestBody.setMid(requestData.getMid());
        requestBody.setOrderId(requestData.getOrderId());
        requestBody.setWebsiteName(requestData.getWebsite());
        requestBody.setTxnAmount(new Money(requestData.getTxnAmount()));
        requestBody.setUserInfo(getUserInfo(requestData));
        requestBody.setPaytmSsoToken(requestData.getSsoToken());
        requestBody.setPromoCode(requestData.getPromoCampId());
        // Setting emiOption in initiate request
        requestBody.setEmiOption(requestData.getEmiOption());

        requestBody.setCartValidationRequired(BooleanUtils.toStringTrueFalse(requestData.isCartValidationRequired()));
        requestBody.setCardTokenRequired(BooleanUtils.toStringTrueFalse(requestData.getCardTokenRequired()));
        requestBody.setCardHash(requestData.getCardHash());

        requestBody.setExtendInfo(getExtendedInfo(requestData));

        requestBody.setDisablePaymentMode(getDisablePaymentModes(requestData));

        requestBody.setEnablePaymentMode(getEnablePaymentModes(requestData));

        if (!StringUtils.isEmpty(requestData.getCallbackUrl())) {
            requestBody.setCallbackUrl(requestData.getCallbackUrl());
        }
        // fetch call back from website if website is not empty
        else if (StringUtils.isNotEmpty(requestData.getWebsite())) {
            initiateTransactionRequest.setBody(requestBody);
            requestBody.setCallbackUrl(getCallBackUrl(initiateTransactionRequest));
        }

        if (StringUtils.isNotBlank(requestData.getCreditCardBillNo())) {
            requestBody.setCcBillPayment(new CCBillPayment(requestData.getCreditCardBillNo()));
        }

        requestBody.setChannelId(requestData.getChannelId());

        if (TheiaConstant.RequestTypes.ADD_MONEY.equalsIgnoreCase(requestData.getRequestType())) {
            requestBody.setNativeAddMoney(true);
        }
        // setting splitSettlementInfo
        requestBody.setSplitSettlementInfoData(requestData.getSplitSettlementInfoData());

        requestBody.setCorporateCustId(requestData.getCorporateCustId());
        requestBody.setbId(requestData.getbId());

        if (StringUtils.isNotBlank(requestData.getPeonURL())) {
            requestBody.setPEON_URL(requestData.getPeonURL());
        }
        requestBody.setAccountNumber(requestData.getAccountNumber());
        requestBody.setNeedAppIntentEndpoint(requestData.isNeedAppIntentEndpoint());
        requestBody.setAppCallbackUrl(requestData.getAppCallbackUrl());
        requestBody.setBrowserName(requestData.getBrowserName());
        requestBody.setDeviceId(requestData.getDeviceId());

        if (ObjectUtils.notEqual(requestData.getUltimateBeneficiaryDetails(), null)
                && StringUtils.isNotBlank(requestData.getUltimateBeneficiaryDetails().getUltimateBeneficiaryName())) {
            requestBody.setUltimateBeneficiaryDetails(new UltimateBeneficiaryDetails(requestData
                    .getUltimateBeneficiaryDetails().getUltimateBeneficiaryName()));
        }

        initiateTransactionRequest.setBody(requestBody);
        return initiateTransactionRequest;
    }

    private EnhancedCashierMgvPayMode buildMGVPayMode(PaymentRequestBean requestData, PayOption payOption,
            boolean isPcfEnabled, EnhancedCashierPageDisplayIdGenerator displayIdGenerator) throws Exception {
        if (payOption == null || CollectionUtils.isEmpty(payOption.getPayMethods())) {
            return null;
        }
        List<PayMethod> payMethods = Optional
                .ofNullable(payOption)
                .map(PayOption::getPayMethods)
                .map(pm -> pm
                        .stream()
                        .filter(paymentMethod -> paymentMethod != null
                                && EPayMethod.GIFT_VOUCHER.getMethod().equals(paymentMethod.getPayMethod()))
                        .collect(Collectors.toList())).orElse(null);

        if (CollectionUtils.isEmpty(payMethods)) {
            return null;
        }
        PayMethod payMethod = payMethods.get(0);

        EnhancedCashierMgvPayMode mgvPaymode = new EnhancedCashierMgvPayMode(displayIdGenerator.generateDisplayId(),
                payMethod.getDisplayName(), PayModeType.valueOf(payMethod.getPayMethod()).getType(),
                payMethod.isHybridDisabled(), !Boolean.parseBoolean(payMethod.getIsDisabled().getStatus()),
                payMethod.getRemainingLimit(), payMethod.getPayOptionRemainingLimits());
        mgvPaymode.setSelected(false); // default
        mgvPaymode.setInsufficientBalance(true);
        mgvPaymode.setInSufficientBalanceMsg(NATIVE_ERROR_MESSAGE_INSUFFICIENTBALANCE_MSG);
        mgvPaymode.setAvailableBalance(0.0);

        Double txnAmount = Double.parseDouble(requestData.getTxnAmount());
        if (CollectionUtils.isNotEmpty(payMethod.getPayChannelOptions()) && mgvPaymode.isEnabled()) {
            AccountInfo balanceInfo = ((BalanceChannel) payMethod.getPayChannelOptions().get(0)).getBalanceInfo();
            if (balanceInfo != null && balanceInfo.getAccountBalance() != null
                    && !StringUtils.isEmpty(balanceInfo.getAccountBalance().getValue())) {
                mgvPaymode.setAvailableBalance(Double.parseDouble(balanceInfo.getAccountBalance().getValue()));
                // currently only 1 template is supported
                mgvPaymode.setTemplateId(((MerchantGiftVoucher) payMethod.getPayChannelOptions().get(0))
                        .getTemplateId());

                if (isPcfEnabled) {
                    Money totalTransactionAmount;
                    PCFFeeCharges pcfFeeCharges = null;
                    pcfFeeCharges = processTransactionUtil.getPCFFeecharges(requestData.getTxnToken(),
                            EPayMethod.GIFT_VOUCHER, null, null);
                    totalTransactionAmount = pcfFeeCharges.getTotalTransactionAmount();
                    if (Double.parseDouble((totalTransactionAmount).getValue()) <= mgvPaymode.getAvailableBalance()) {
                        mgvPaymode.setInsufficientBalance(false);
                    }
                    mgvPaymode.setPcfFeeCharges(pcfFeeCharges);
                    return mgvPaymode;

                } else if (mgvPaymode.getAvailableBalance() != null && mgvPaymode.getAvailableBalance() >= txnAmount) {
                    mgvPaymode.setInsufficientBalance(false);
                }
            }

        }
        if (null == mgvPaymode.getAvailableBalance() || 0.0 == mgvPaymode.getAvailableBalance()) {
            EXT_LOGGER.customInfo("MGV balance 0");
            return null;
        }
        return mgvPaymode;
    }

    private void setUpiDataList(List<UpiPspHandlerData> upiDataList, Map.Entry<String, Map<String, String>> entry,
            Map<String, String> pspProperties) {
        String name = entry.getKey();
        String upiDisplayNameStr = com.paytm.pgplus.theia.utils.ConfigurationUtil.getProperty("upi.psp.order", "")
                .replaceAll(" ", "");
        Set<String> upiDisplayNameSet = new HashSet<String>(Arrays.asList(upiDisplayNameStr.split(",")));
        if (upiDisplayNameSet.contains(name)) {
            String displayName = pspProperties.get(entry.getKey() + ".displayname");
            List<String> handlerList = parseCsvToList(pspProperties.get(entry.getKey() + ".handler"));
            upiDataList.add(new UpiPspHandlerData(name, displayName, handlerList));
        }

    }

    private Map<String, Map<String, String>> groupByPsp(Iterator<String> iterator) {
        Map<String, Map<String, String>> map = new HashMap<>();
        while ((iterator.hasNext())) {
            String key = iterator.next();
            String value = com.paytm.pgplus.theia.utils.ConfigurationUtil.getUpiProperty(key);
            String psp = key.split(Pattern.quote("."))[0];
            if (map.get(psp) == null) {
                map.put(psp, new HashMap<>());
            }
            map.get(psp).put(key, value);
        }
        return map;
    }

    private List<String> parseCsvToList(String val) {
        if (StringUtils.isNotBlank(val)) {

            String[] list = val.replaceAll(" ", "").split(Pattern.quote(","));
            return Arrays.asList(list);
        }
        return Collections.emptyList();
    }

    private EnhancedCashierSavedCard buildEnhancedCashierSavedCard(PayOption payOption,
            EnhancedCashierPageDisplayIdGenerator displayIdGenerator) {

        List<SavedInstrument> savedCards = getSavedCardInfo(payOption, displayIdGenerator);

        if (CollectionUtils.isNotEmpty(savedCards)) {

            return new EnhancedCashierSavedCard(displayIdGenerator.generateDisplayId(),
                    EPayMethod.SAVED_CARD.getNewDisplayName(), PayModeType.SAVED_CARDS.getType(), savedCards);
        }
        return null;
    }

    private EnhancedCashierSavedVPA buildEnhancedCashierSavedVpa(PaymentRequestBean requestData,
            InitiateTransactionResponse initiateTransactionResponse, PayOption payOption,
            EnhancedCashierPageDisplayIdGenerator displayIdGenerator) {

        List<SavedVPAInstrument> savedVPAs = getSavedVPAInfo(requestData, initiateTransactionResponse, payOption,
                displayIdGenerator);

        if (CollectionUtils.isNotEmpty(savedVPAs)) {
            return new EnhancedCashierSavedVPA(displayIdGenerator.generateDisplayId(),
                    EPayMethod.SAVED_VPA.getNewDisplayName(), PayModeType.SAVED_VPAS.getType(), savedVPAs);
        }

        return null;
    }

    private FetchMerchantUserInfoRequest createFetchMerchantUserInfoRequest(PaymentRequestBean requestData,
            InitiateTransactionResponse initiateTransactionResponse) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss");
        FetchMerchantUserInfoRequest fetchMerchantUserInfoRequest = new FetchMerchantUserInfoRequest();
        FetchMerchantUserInfoRequestBody fetchMerchantUserInfoRequestBody = new FetchMerchantUserInfoRequestBody();
        TokenTypeRequestHeader requestHeader = new TokenTypeRequestHeader();
        requestHeader.setRequestTimestamp(sdf.format(new Timestamp(System.currentTimeMillis())).toString());
        requestHeader.setChannelId(EChannelId.valueOf(requestData.getChannelId()));
        requestHeader.setTokenType(TokenType.TXN_TOKEN.name());
        requestHeader.setToken(initiateTransactionResponse.getBody().getTxnToken());
        fetchMerchantUserInfoRequest.setHead(requestHeader);
        fetchMerchantUserInfoRequestBody.setMid(requestData.getMid());
        fetchMerchantUserInfoRequestBody.setOrderId(requestData.getOrderId());
        fetchMerchantUserInfoRequest.setBody(fetchMerchantUserInfoRequestBody);
        return fetchMerchantUserInfoRequest;
    }

    private String getCallBackUrl(InitiateTransactionRequest request) {
        if (StringUtils.isNotEmpty(request.getBody().getCallbackUrl())) {
            return request.getBody().getCallbackUrl();
        }
        MerchantUrlInput input = new MerchantUrlInput(request.getBody().getMid(),
                MappingMerchantUrlInfo.UrlTypeId.RESPONSE, request.getBody().getWebsiteName());
        MappingMerchantUrlInfo merchantUrlInfo = null;
        try {
            merchantUrlInfo = merchantUrlService.getMerchantUrlInfo(input);
        } catch (PaymentRequestValidationException ex) {
            LOGGER.error("Error while fetching callbackUrl for mid : {}", request.getBody().getMid());
            return null;
        }
        return merchantUrlInfo.getPostBackurl();
    }

    private String getCallBackUrlFromInitTxnReqBody(InitiateTransactionRequestBody requestBody) {
        if (StringUtils.isNotEmpty(requestBody.getCallbackUrl())) {
            return requestBody.getCallbackUrl();
        }
        MerchantUrlInput input = new MerchantUrlInput(requestBody.getMid(), MappingMerchantUrlInfo.UrlTypeId.RESPONSE,
                requestBody.getWebsiteName());
        MappingMerchantUrlInfo merchantUrlInfo = null;
        try {
            merchantUrlInfo = merchantUrlService.getMerchantUrlInfo(input);
        } catch (PaymentRequestValidationException ex) {
            LOGGER.error("Error while fetching callbackUrl for mid : {}", requestBody.getMid());
            return null;
        }
        return (merchantUrlInfo != null) ? merchantUrlInfo.getPostBackurl() : null;
    }

    @Deprecated
    private VpaDetailsResponse getNativeVpaDetailsResponse(PaymentRequestBean requestData,
            InitiateTransactionResponse initiateTransactionResponse) throws Exception {
        FetchVpaDetailsRequest fetchVpaDetailsRequest = createFetchVPADetailRequest(requestData,
                initiateTransactionResponse);
        IRequestProcessor<FetchVpaDetailsRequest, VpaDetailsResponse> requestProcessor = requestProcessorFactory
                .getRequestProcessor(RequestType.NATIVE_FETCH_VPA_DETAILS);
        return requestProcessor.process(fetchVpaDetailsRequest);
    }

    private FetchVpaDetailsRequest createFetchVPADetailRequest(PaymentRequestBean requestData,
            InitiateTransactionResponse initiateTransactionResponse) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss");
        FetchVpaDetailsRequest fetchVpaDetailsRequest = new FetchVpaDetailsRequest();
        TokenRequestHeader requestHeader = new TokenRequestHeader();
        requestHeader.setRequestTimestamp(sdf.format(new Timestamp(System.currentTimeMillis())).toString());
        requestHeader.setChannelId(EChannelId.valueOf(requestData.getChannelId()));
        requestHeader.setTxnToken(initiateTransactionResponse.getBody().getTxnToken());
        fetchVpaDetailsRequest.setHead(requestHeader);
        return fetchVpaDetailsRequest;
    }

    private void getInsufficientTxnPaymode(List<EnhancedCashierPagePayModeBase> paymodes, String txnToken,
            EPayMode paymentMode, boolean checkPcf, boolean isPcfEnabled) throws Exception {

        if (CollectionUtils.isNotEmpty(paymodes)) {

            EnhancedCashierPagePayModeBase savedVPA = paymodes
                    .stream()
                    .filter(paymode -> StringUtils.equalsIgnoreCase(PayModeType.SAVED_VPAS.getType(), paymode.getType()))
                    .findFirst().orElse(null);
            if (savedVPA != null) {
                if (!EPayMode.ADDANDPAY.equals(paymentMode) && !EPayMode.HYBRID.equals(paymentMode) && checkPcf
                        && isPcfEnabled && savedVPA instanceof EnhancedCashierSavedVPA) {
                    SavedVPAInstrument savedVPAInstrument = ((EnhancedCashierSavedVPA) savedVPA).getSavedVPAs().get(0);
                    savedVPAInstrument.setPcfFeeCharges(processTransactionUtil.getPCFFeecharges(txnToken,
                            EPayMethod.UPI, null, null));
                }
                savedVPA.setSelected(true);
                return;
            }

            EnhancedCashierPagePayModeBase savedCard = paymodes
                    .stream()
                    .filter(paymode -> StringUtils.equalsIgnoreCase(PayModeType.SAVED_CARDS.getType(),
                            paymode.getType())).findFirst().orElse(null);
            if (savedCard != null) {
                if (!EPayMode.ADDANDPAY.equals(paymentMode) && !EPayMode.HYBRID.equals(paymentMode) && checkPcf
                        && isPcfEnabled && savedCard instanceof EnhancedCashierSavedCard) {
                    SavedInstrument savedInstrument = ((EnhancedCashierSavedCard) savedCard).getSavedCards().get(0);

                    savedInstrument.setPcfFeeCharges(processTransactionUtil.getPCFFeecharges(txnToken,
                            EPayMethod.getPayMethodByOldName(savedInstrument.getSubType()),
                            savedInstrument.getCardScheme(), null));
                }
                savedCard.setSelected(true);
                return;
            }

            EnhancedCashierPagePayModeBase payMode = paymodes.stream().findFirst().orElse(null);
            if (payMode != null) {
                if (!EPayMode.ADDANDPAY.equals(paymentMode) && !EPayMode.HYBRID.equals(paymentMode) && checkPcf
                        && isPcfEnabled) {
                    payMode.setPcfFeeCharges(processTransactionUtil.getPCFFeecharges(txnToken,
                            EPayMethod.getPayMethodByNewDisplayName(payMode.getName()), null, null));
                }
                payMode.setSelected(true);
                return;
            }
        }

    }

    public void setNativePaymentsDataForRetry(PaymentRequestBean requestData) throws Exception {
        String nativeRequestData = IOUtils.toString(((ModifiableHttpServletRequest) requestData.getRequest())
                .getRequest().getInputStream(), Charsets.UTF_8.name());
        if (org.apache.commons.lang3.StringUtils.isNotBlank(nativeRequestData)) {
            NativePaymentRequest nativePaymentRequest = JsonMapper.mapJsonToObject(nativeRequestData,
                    NativePaymentRequest.class);
            NativePaymentRequestBody nativePaymentRequestBody = nativePaymentRequest.getBody();

            // Modify cardInfo
            StringBuilder cardInfo = null;
            if (null != nativePaymentRequestBody.getCardInfo()) {
                cardInfo = new StringBuilder(nativePaymentRequestBody.getCardInfo());
            }

            if (null != cardInfo) {
                String[] cardInfoArray = cardInfo.toString().split(Pattern.quote("|"));
                if (cardInfoArray[0].length() > 1) {
                    // saved card id present
                    nativePaymentRequestBody.setCardInfo(cardInfoArray[0]);
                } else {
                    // saved card id not present
                    nativePaymentRequestBody.setCardInfo("|" + cardInfoArray[1]);
                }
            }
            // remove mpin
            nativePaymentRequestBody.setMpin(null);

            // set this updated data in cache
            setRetryDataInEnhancedCashierPageInCache(requestData, nativePaymentRequestBody);

        }
    }

    private void setRetryDataInEnhancedCashierPageInCache(PaymentRequestBean paymentRequestBean,
            NativePaymentRequestBody nativePaymentRequestBody) {
        String mid = paymentRequestBean.getMid();
        String orderId = paymentRequestBean.getOrderId();
        String key = enhancedCashierPageServiceHelper.fetchRedisKey(mid, orderId);

        // Get the enhancedCashierPage response from cache.
        EnhanceCashierPageCachePayload existingEnhanceCashierPageCachePayload = (EnhanceCashierPageCachePayload) nativeSessionUtil
                .getKey(key);
        if (null == existingEnhanceCashierPageCachePayload) {
            LOGGER.info("enhanced cashier page not found in cache.");
            return;
        }
        int expiryTime = enhancedCashierPageServiceHelper.getTokenExpiryTime();

        EnhancedCashierPage enhancedCashierPage = existingEnhanceCashierPageCachePayload.getEnhancedCashierPage();
        enhancedCashierPage.setRetryData(nativePaymentRequestBody);

        existingEnhanceCashierPageCachePayload.setEnhancedCashierPage(enhancedCashierPage);

        nativeSessionUtil.setKey(key, existingEnhanceCashierPageCachePayload, expiryTime);
    }

    private void removePPBLVPAForPPBLNB(List<EnhancedCashierPagePayModeBase> paymodes) {

        if (CollectionUtils.isNotEmpty(paymodes)) {
            boolean isPPBLPayModeValid = paymodes.stream().anyMatch(
                    paymode -> StringUtils.equalsIgnoreCase(PayModeType.PPBL.getType(), paymode.getType())
                            && paymode instanceof EnhancedCashierPagePayMode
                            && BooleanUtils.isTrue(((EnhancedCashierPagePayMode) paymode).isEnabled()));
            if (isPPBLPayModeValid) {

                // Remove PPBL saved VPA
                EnhancedCashierSavedVPA savedVPA = (EnhancedCashierSavedVPA) paymodes
                        .stream()
                        .filter(paymode -> StringUtils.equalsIgnoreCase(PayModeType.SAVED_VPAS.getType(),
                                paymode.getType())).findFirst().orElse(null);
                if (savedVPA != null && savedVPA.getSavedVPAs() != null) {

                    List<SavedVPAInstrument> ppblSavedVPAs = savedVPA.getSavedVPAs().stream().filter(Objects::nonNull)
                            .filter(s -> s.getBankName().equalsIgnoreCase(EPayMethod.PPBL.getNewDisplayName()))
                            .collect(Collectors.toList());

                    if (CollectionUtils.isNotEmpty(ppblSavedVPAs)) {
                        savedVPA.getSavedVPAs().removeAll(ppblSavedVPAs);
                        // Remove savedVPA payMode if saved VPA list has been
                        // emptied
                        if (CollectionUtils.isEmpty(savedVPA.getSavedVPAs())) {
                            paymodes.remove(savedVPA);
                        }
                    }

                }

            }
        }
    }

    public static Map<EPayMethod, Integer> getPayModeSequenceMap(String orderedMerchantPaymodeStr,
            boolean isMerchantSequencing) {
        String[] orderedMerchantPaymodeList = orderedMerchantPaymodeStr.split(Pattern.quote(","));
        Map<EPayMethod, Integer> merchantPaymodesOrdering = new HashMap<>();
        int i = 0;
        for (String paymode : orderedMerchantPaymodeList) {
            if (isMerchantSequencing) {
                merchantPaymodesOrdering.put(EPayMethod.getPayMethodByShortName(paymode.trim()), i++);
            } else {
                merchantPaymodesOrdering.put(EPayMethod.getPayMethodByMethod(paymode.trim()), i++);
            }
        }
        return merchantPaymodesOrdering;
    }

    private EnhancedCashierPagePayModeBase buildEnhancedCashierPagePaymentMode(PayMethod payMethod,
            NativeCashierInfoResponse nativeCashierInfoResponse,
            EnhancedCashierPageDisplayIdGenerator displayIdGenerator, PaymentRequestBean requestBean, boolean addNPay,
            EnhancedCashierPageWalletInfo walletInfo) {
        boolean isUpiIntentSupported = false;
        if (payMethod != null && payMethod.getPayMethod() != null) {

            // no need to add paymethod if paymethod is disabled for the txn
            if (payMethod.getIsDisabled() != null && Boolean.valueOf(payMethod.getIsDisabled().getStatus())
                    && !Boolean.TRUE.equals(payMethod.getIsDisabled().getShowDisabled())) {
                return null;
            }
            boolean returnDisableChannels = iPgpFf4jClient.checkWithdefault(
                    TheiaConstant.FF4J.THEIA_ENHANCED_RETURN_DISABLED_CHANNELS, new HashMap<>(), false);
            EPayMethod paymentInstrument = EPayMethod.getPayMethodByMethod(payMethod.getPayMethod());
            EnhancedCashierBankData data = null;
            List<EnhancedCashierPayModeData> bank = null;
            List<UpiPspHandlerData> upiDataList;
            switch (paymentInstrument) {
            case CREDIT_CARD:
            case DEBIT_CARD:
            case PAYTM_DIGITAL_CREDIT:
            case ADVANCE_DEPOSIT_ACCOUNT:
                return new EnhancedCashierPagePayMode(displayIdGenerator.generateDisplayId(),
                        payMethod.getDisplayName(), PayModeType.valueOf(payMethod.getPayMethod()).getType(),
                        !Boolean.valueOf(payMethod.getIsDisabled().getStatus()), payMethod.isHybridDisabled(),
                        payMethod.isOnboarding(), payMethod.getRemainingLimit(),
                        payMethod.getPayOptionRemainingLimits());
            case UPI:

                if (payMethod.getPayChannelOptions() != null && !payMethod.getPayChannelOptions().isEmpty()) {
                    data = new EnhancedCashierBankData();
                    bank = new ArrayList<>();
                    Iterator<PayChannelBase> payChannelBaseIterator = payMethod.getPayChannelOptions().iterator();
                    while (payChannelBaseIterator.hasNext()) {
                        UPI payChannelBase = (UPI) payChannelBaseIterator.next();
                        if (Boolean.FALSE.toString().equals(payChannelBase.getIsDisabled().getStatus())) {
                            if (UPI_PUSH.equals(payChannelBase.getInstId())) {
                                isUpiIntentSupported = true;
                            }
                            EnhancedCashierPayModeData enhancedCashierPayModeData = new EnhancedCashierPayModeData(
                                    payChannelBase.getInstId(), payChannelBase.getInstName(),
                                    payChannelBase.getIconUrl(), payChannelBase.getHasLowSuccess(),
                                    payChannelBase.isHybridDisabled());
                            enhancedCashierPayModeData.setBankLogoUrl(payChannelBase.getBankLogoUrl());
                            bank.add(enhancedCashierPayModeData);
                        } else {
                            if (returnDisableChannels
                                    && Boolean.TRUE.equals(payChannelBase.getIsDisabled().getShowDisabled())) {
                                if (UPI_PUSH.equals(payChannelBase.getInstId())) {
                                    isUpiIntentSupported = true;
                                }
                                EnhancedCashierPayModeData enhancedCashierPayModeData = new EnhancedCashierPayModeData(
                                        payChannelBase.getInstId(), payChannelBase.getInstName(),
                                        payChannelBase.getIconUrl(), payChannelBase.getHasLowSuccess(),
                                        payChannelBase.isHybridDisabled());
                                enhancedCashierPayModeData.setIsDisabled(payChannelBase.getIsDisabled());
                                enhancedCashierPayModeData.setBankLogoUrl(payChannelBase.getBankLogoUrl());
                                bank.add(enhancedCashierPayModeData);
                            }
                        }
                    }
                    data.setBanks(bank);
                }

                EnhancedCashierPagePayMode enhancedCashierPagePayModenew = new EnhancedCashierPagePayMode(
                        displayIdGenerator.generateDisplayId(), payMethod.getDisplayName(), PayModeType.valueOf(
                                payMethod.getPayMethod()).getType(), data, !Boolean.valueOf(payMethod.getIsDisabled()
                                .getStatus()), payMethod.isHybridDisabled(), payMethod.getRemainingLimit(),
                        payMethod.getPayOptionRemainingLimits());

                /**
                 * For the paymode - Pay using UPI apps, PGP-UI team do not have
                 * a way to control the MID based rollout from the backend
                 * because this paymode has been developed on the UI side only.
                 *
                 */
                // Removing ff4j check and using the merchant Prefrence to check
                // UPI_APPS_PAY_MODE
                enhancedCashierPagePayModenew.setUpiAppsPayModeSupported(nativeCashierInfoResponse.getBody()
                        .isUpiAppsPayModeEnabled());

                if (addNPay && merchantPreferenceService.isCollectBoxEnabledForAddNPay(requestBean.getMid())) {
                    LOGGER.info("Merchant Preference Enabled for UPI Collect Box on AddNPay");
                    String maxAmount = ConfigurationUtil.getProperty("maximum.amount.for.addnpay.for.upi.collect",
                            "2000");
                    Double txnAmount = Double.parseDouble(requestBean.getTxnAmount());
                    Double walletBalance = walletInfo.getWalletBalance();
                    Double maximumAmountForUPICollectForAddnPay = Double.parseDouble(maxAmount);
                    enhancedCashierPagePayModenew
                            .setUpiCollectBoxEnabled((txnAmount - walletBalance) < maximumAmountForUPICollectForAddnPay);
                }

                if (addNPay) {
                    enhancedCashierPagePayModenew.setUpiCollectBoxEnabled(merchantPreferenceService
                            .isCollectBoxEnabledForAddNPay(requestBean.getMid()));
                }

                String upiPspPriority = "";
                if (requestBean.isSubscription()
                        && !CollectionUtils.isEmpty(nativeCashierInfoResponse.getBody().getMandateSupportedApps())
                        && !addNPay) {
                    Collections.sort(nativeCashierInfoResponse.getBody().getMandateSupportedApps(),
                            UpiPspListComparator.UPI_PSP_LIST_PRIORITY_COMPARATOR);
                    List<String> mandateSupportedEnhanceApp = new ArrayList<>();
                    for (MandateSupportedApps mandateSupportedApp : nativeCashierInfoResponse.getBody()
                            .getMandateSupportedApps()) {
                        mandateSupportedEnhanceApp.add(mandateSupportedApp.getPackageName());
                    }
                    upiPspPriority = String.join(",", mandateSupportedEnhanceApp);
                    ;
                } else {
                    upiPspPriority = ConfigurationUtil.getProperty("upi.psp.priority", StringUtils.EMPTY);
                }
                String upiBlockedPsp = com.paytm.pgplus.theia.utils.ConfigurationUtil.getProperty("upi.blocked.psp",
                        StringUtils.EMPTY);
                enhancedCashierPagePayModenew.setUpiPspPriority(upiPspPriority);
                enhancedCashierPagePayModenew.setUpiBlockedPsp(upiBlockedPsp);
                String upiIntentEnabled = ConfigurationUtil.getProperty("upi.intent.enabled", "true");
                enhancedCashierPagePayModenew.setUpiIntentEnable(Boolean.valueOf(upiIntentEnabled));
                return enhancedCashierPagePayModenew;
            case WALLET:
            case PPBL:
            case RENEW_PPBL:
                EnhancedCashierPagePayMode enhancedCashierPagePayMode = new EnhancedCashierPagePayMode(
                        displayIdGenerator.generateDisplayId(), payMethod.getDisplayName(), PayModeType.valueOf(
                                payMethod.getPayMethod()).getType(), !Boolean.valueOf(payMethod.getIsDisabled()
                                .getStatus()), payMethod.isHybridDisabled(), payMethod.getRemainingLimit(),
                        payMethod.getPayOptionRemainingLimits());
                enhancedCashierPagePayMode.setBankLogoUrl(commonFacade.getBankLogo("PPBL"));
                return enhancedCashierPagePayMode;
            case NET_BANKING:
                if (payMethod.getPayChannelOptions() != null && !payMethod.getPayChannelOptions().isEmpty()) {
                    data = new EnhancedCashierBankData();
                    bank = new ArrayList<>();
                    Iterator<PayChannelBase> payChannelBaseIterator = payMethod.getPayChannelOptions().iterator();
                    while (payChannelBaseIterator.hasNext()) {
                        Bank payChannelBase = (Bank) payChannelBaseIterator.next();
                        // Extracting ZestMoney from NB List
                        if (payChannelBase.getInstId().equals(ZEST)) {
                            LOGGER.info("Zest enabled as netbanking paymethod, not adding in final NB list");
                        } else {
                            if (Boolean.FALSE.toString().equals(payChannelBase.getIsDisabled().getStatus())) {
                                EnhancedCashierPayModeData enhancedCashierPayModeData = new EnhancedCashierPayModeData(
                                        payChannelBase.getInstId(), payChannelBase.getInstName(),
                                        payChannelBase.getIconUrl(), payChannelBase.getHasLowSuccess(),
                                        payChannelBase.isHybridDisabled());
                                enhancedCashierPayModeData.setBankLogoUrl(payChannelBase.getBankLogoUrl());
                                enhancedCashierPayModeData.setChannelDisplayName(payChannelBase.getInstDispCode());
                                bank.add(enhancedCashierPayModeData);
                            } else {
                                if (returnDisableChannels
                                        && Boolean.TRUE.equals(payChannelBase.getIsDisabled().getShowDisabled())) {
                                    EnhancedCashierPayModeData enhancedCashierPayModeData = new EnhancedCashierPayModeData(
                                            payChannelBase.getInstId(), payChannelBase.getInstName(),
                                            payChannelBase.getIconUrl(), payChannelBase.getHasLowSuccess(),
                                            payChannelBase.isHybridDisabled());
                                    enhancedCashierPayModeData.setIsDisabled(payChannelBase.getIsDisabled());
                                    enhancedCashierPayModeData.setBankLogoUrl(payChannelBase.getBankLogoUrl());
                                    enhancedCashierPayModeData.setChannelDisplayName(payChannelBase.getInstDispCode());
                                    bank.add(enhancedCashierPayModeData);
                                }
                            }
                        }
                    }
                    data.setBanks(bank);
                }
                return (data == null) ? null : new EnhancedCashierPagePayMode(displayIdGenerator.generateDisplayId(),
                        payMethod.getDisplayName(), PayModeType.valueOf(payMethod.getPayMethod()).getType(), data,
                        !Boolean.valueOf(payMethod.getIsDisabled().getStatus()), payMethod.isHybridDisabled(),
                        payMethod.getRemainingLimit(), payMethod.getPayOptionRemainingLimits());
            case EMI:
                return buildEmiPayMode(payMethod, nativeCashierInfoResponse, displayIdGenerator);
            case COD:
                return buildCODPayMode(payMethod, displayIdGenerator);
            case BANK_MANDATE:
                if (MandateMode.E_MANDATE.name().equals(requestBean.getMandateType())) {
                    return buildBankMandatePayMode(payMethod, displayIdGenerator, data, bank);
                }
            }
        }
        return null;
    }

    enum PayMethodComparator implements Comparator<EnhancedCashierPagePayModeBase> {
        MERCHANT_PAYMODES_COMPARATOR {
            @Override
            public int compare(EnhancedCashierPagePayModeBase paymode1, EnhancedCashierPagePayModeBase paymode2) {
                return comparePaymodes(merchantPaymodesOrdering, paymode1, paymode2);
            }
        },
        ADDMONEY_PAYMODES_COMPARATOR {
            @Override
            public int compare(EnhancedCashierPagePayModeBase paymode1, EnhancedCashierPagePayModeBase paymode2) {
                return comparePaymodes(addMoneyPaymodesOrdering, paymode1, paymode2);
            }
        };

        static Map<EPayMethod, Integer> merchantPaymodesOrdering = new HashMap<>();
        static Map<EPayMethod, Integer> addMoneyPaymodesOrdering = new HashMap<>();

        static {
            String orderedMerchantPaymodeStr = ConfigurationUtil.getProperty(UIREVAMP_PAYMODES, "");
            String orderedAddMoneyPaymodeStr = ConfigurationUtil.getProperty(UIREVAMP_ADDMONEY_PAYMODES, "");
            merchantPaymodesOrdering = getPayModeSequenceMap(orderedMerchantPaymodeStr, false);
            addMoneyPaymodesOrdering = getPayModeSequenceMap(orderedAddMoneyPaymodeStr, false);
        }

        private static Integer getOrderValue(Map<EPayMethod, Integer> ordering, EPayMethod key) {
            Integer i = ordering.get(key);
            return i == null ? 100 : i;
        }

        private static int comparePaymodes(Map<EPayMethod, Integer> ordering, EnhancedCashierPagePayModeBase paymode1,
                EnhancedCashierPagePayModeBase paymode2) {
            return getOrderValue(ordering, EPayMethod.getPayMethodByNewDisplayName(paymode1.getName())).compareTo(
                    getOrderValue(ordering, EPayMethod.getPayMethodByNewDisplayName(paymode2.getName())));
        }
    }

    enum UpiPspListComparator implements Comparator<MandateSupportedApps> {
        UPI_PSP_LIST_PRIORITY_COMPARATOR {
            @Override
            public int compare(MandateSupportedApps mandateSupportedApps1, MandateSupportedApps mandateSupportedApps2) {
                return mandateSupportedApps1.getPriority().compareTo(mandateSupportedApps2.getPriority());
            }
        };
    }

    enum UpiDisplayNameComparator implements Comparator<UpiPspHandlerData> {
        UPI_DISPLAYNAME_COMPARATOR {
            @Override
            public int compare(UpiPspHandlerData upiMode1, UpiPspHandlerData upiMode2) {
                return comparePaymodes(upiDisplaynameOrdering, upiMode1, upiMode2);
            }
        };

        static Map<String, Integer> upiDisplaynameOrdering = new HashMap<>();

        static {
            String orderedUpiDisplayNameStr = com.paytm.pgplus.theia.utils.ConfigurationUtil.getProperty(
                    "upi.psp.order", "");
            String[] orderedUpiDisplayNameList = orderedUpiDisplayNameStr.split(",");

            int i = 0;
            for (String paymode : orderedUpiDisplayNameList) {
                upiDisplaynameOrdering.put(paymode, i++);
            }

        }

        private static Integer getOrderValue(Map<String, Integer> ordering, String key) {
            Integer i = ordering.get(key);
            return i == null ? 100 : i;
        }

        private static int comparePaymodes(Map<String, Integer> ordering, UpiPspHandlerData upiMode1,
                UpiPspHandlerData upiMode2) {
            return getOrderValue(ordering, upiMode1.getName()).compareTo(getOrderValue(ordering, upiMode2.getName()));
        }
    }

    public void sortMerchantPaymode(String mid, List<EnhancedCashierPagePayModeBase> merchantPayModes,
            PayModeOrder payModeOrder, PaymodeSequenceEnum paymodeSequenceEnum, String paymodeSequence) {
        String merchantBasedPayModeSeq = StringUtils.isNotBlank(paymodeSequence) ? paymodeSequence
                : merchantPreferenceService.getMerchantPaymodeSequence(mid, paymodeSequenceEnum);
        boolean sortDefault = true;
        if (StringUtils.isNotEmpty(merchantBasedPayModeSeq)) {
            try {
                sortDefault = false;
                Map<EPayMethod, Integer> merchantBasedPayModeSeqMap = getPayModeSequenceMap(merchantBasedPayModeSeq,
                        true);
                if (merchantBasedPayModeSeqMap.containsKey(EPayMethod.PPBL)
                        && !merchantBasedPayModeSeqMap.containsKey(EPayMethod.RENEW_PPBL)) {
                    merchantBasedPayModeSeqMap.put(EPayMethod.RENEW_PPBL,
                            merchantBasedPayModeSeqMap.get(EPayMethod.PPBL));
                }
                if (merchantBasedPayModeSeqMap.containsKey(EPayMethod.RENEW_PPBL)
                        && !merchantBasedPayModeSeqMap.containsKey(EPayMethod.PPBL)) {
                    merchantBasedPayModeSeqMap.put(EPayMethod.PPBL,
                            merchantBasedPayModeSeqMap.get(EPayMethod.RENEW_PPBL));
                }
                Collections.sort(merchantPayModes, (paymode1, paymode2) -> PayMethodComparator.comparePaymodes(
                        merchantBasedPayModeSeqMap, paymode1, paymode2));
            } catch (Exception ex) {
                LOGGER.error("Error in Enhance flow in Merchant based paymodes sequencing: {}", ex);
                sortDefault = true;
            }
        }
        if (sortDefault) {
            if (PayModeOrder.ADD_MONEY_PAYMODE.equals(payModeOrder))
                Collections.sort(merchantPayModes, PayMethodComparator.ADDMONEY_PAYMODES_COMPARATOR);
            else
                Collections.sort(merchantPayModes, PayMethodComparator.MERCHANT_PAYMODES_COMPARATOR);
        }
    }

    private EnhancedCashierCODPayMode buildCODPayMode(PayMethod codPayMethod,
            EnhancedCashierPageDisplayIdGenerator displayIdGenerator) {

        Money codMinAmount = null;
        String hybridErrorMsg = "";
        String codDetailedMsg = "";
        if (codPayMethod != null) {
            CODChannel codChannel = (CODChannel) codPayMethod.getPayChannelOptions().stream().findFirst().orElse(null);
            if (codChannel != null) {
                codMinAmount = codChannel.getMinAmount();
                hybridErrorMsg = codChannel.getCodHybridErrMsg();
                codDetailedMsg = codChannel.getCodMessage();
            }
        }

        return new EnhancedCashierCODPayMode(displayIdGenerator.generateDisplayId(), codPayMethod.getDisplayName(),
                PayModeType.valueOf(codPayMethod.getPayMethod()).getType(), !Boolean.valueOf(codPayMethod
                        .getIsDisabled().getStatus()), codMinAmount, hybridErrorMsg, codDetailedMsg,
                codPayMethod.isHybridDisabled(), codPayMethod.getRemainingLimit(),
                codPayMethod.getPayOptionRemainingLimits());

    }

    private EnhancedCashierSavedCard buildEnhancedCashierSavedCardEmi(
            NativeCashierInfoResponse nativeCashierInfoResponse,
            EnhancedCashierPageDisplayIdGenerator displayIdGenerator) {
        List<SavedInstrument> savedCards = getSavedCardInfoForEmi(nativeCashierInfoResponse, displayIdGenerator);

        if (CollectionUtils.isNotEmpty(savedCards)) {

            return new EnhancedCashierSavedCard(displayIdGenerator.generateDisplayId(),
                    EPayMethod.SAVED_CARD.getNewDisplayName(), PayModeType.SAVED_CARDS.getType(), savedCards);
        }

        return null;

    }

    private List<SavedInstrument> getSavedCardInfoForEmi(NativeCashierInfoResponse nativeCashierInfoResponse,
            EnhancedCashierPageDisplayIdGenerator displayIdGenerator) {

        List<SavedInstrument> merchantSavedInstrumentsList = new ArrayList<>();
        if (nativeCashierInfoResponse.getBody().getMerchantPayOption() != null
                && BizParamValidator.validateInputListParam(nativeCashierInfoResponse.getBody().getMerchantPayOption()
                        .getSavedInstruments())) {
            List<PayChannelBase> savedCards = nativeCashierInfoResponse.getBody().getMerchantPayOption()
                    .getSavedInstruments();
            for (PayChannelBase saved : savedCards) {
                if (saved instanceof SavedCard && ((SavedCard) saved).getIsEmiAvailable()) {
                    getSavedCardData(saved, merchantSavedInstrumentsList, displayIdGenerator);

                }
            }
            // direct flag disbled in case of EMI for saved card
            for (SavedInstrument savedInstrument : merchantSavedInstrumentsList) {
                savedInstrument.setIdebit(false);
            }
        }
        return merchantSavedInstrumentsList;

    }

    private EnhancedCashierEmiPayMode buildEmiPayMode(PayMethod payMethod,
            NativeCashierInfoResponse nativeCashierInfoResponse,
            EnhancedCashierPageDisplayIdGenerator displayIdGenerator) {
        EnhancedCashierBankData data = null;
        EnhancedCashierSavedCard savedCard = null;
        List<EnhancedCashierPayModeData> bank = null;
        List<EnhancedCashierEmiPlanData> emiPlanData = null;
        if (payMethod.getPayChannelOptions() != null && !payMethod.getPayChannelOptions().isEmpty()) {
            boolean returnDisableChannels = iPgpFf4jClient.checkWithdefault(
                    TheiaConstant.FF4J.THEIA_ENHANCED_RETURN_DISABLED_CHANNELS, new HashMap<>(), false);
            data = new EnhancedCashierBankData();
            bank = new ArrayList<>();
            Iterator<PayChannelBase> payChannelBaseIterator = payMethod.getPayChannelOptions().iterator();
            while (payChannelBaseIterator.hasNext()) {
                PayChannelBase payChannelBasenext = payChannelBaseIterator.next();
                Bank payChannelBase = (Bank) payChannelBasenext;
                EmiChannel emiChannel = (EmiChannel) payChannelBasenext;
                if (emiChannel.getEmiChannelInfos() != null && !emiChannel.getEmiChannelInfos().isEmpty()) {
                    emiPlanData = new ArrayList<>();
                    for (EMIChannelInfo emiChannelInfo : emiChannel.getEmiChannelInfos()) {
                        emiPlanData.add(new EnhancedCashierEmiPlanData(emiChannelInfo.getPlanId(), emiChannelInfo
                                .getTenureId(), emiChannelInfo.getInterestRate(), emiChannelInfo.getOfMonths(),
                                emiChannelInfo.getMinAmount(), emiChannelInfo.getMaxAmount(), emiChannelInfo
                                        .getCardAcquiringMode(), emiChannelInfo.getPerInstallment(), emiChannelInfo
                                        .getEmiAmount(), emiChannelInfo.getTotalAmount()));
                    }
                }
                if (emiPlanData != null && !emiPlanData.isEmpty()) {
                    emiPlanData.get(0).setSelected(true);
                }
                EnhancedCashierPayModeData enhancedCashierPayModeData = new EnhancedCashierPayModeData(
                        payChannelBase.getInstId(), payChannelBase.getInstName(), payChannelBase.getIconUrl(),
                        payChannelBase.getHasLowSuccess(), emiPlanData);
                enhancedCashierPayModeData.setEmiType(emiChannel.getEmiType().getType());
                enhancedCashierPayModeData.setMinEMIAmount(emiChannel.getMinAmount().getValue());
                enhancedCashierPayModeData.setMaxEMIAmount(emiChannel.getMaxAmount().getValue());
                if (returnDisableChannels && Boolean.TRUE.equals(payChannelBase.getIsDisabled().getShowDisabled())) {
                    enhancedCashierPayModeData.setIsDisabled(payChannelBase.getIsDisabled());
                }
                String displayName = getdisplayNameForEMI(emiChannel);
                enhancedCashierPayModeData.setDisplayName(displayName);
                if (payChannelBase.getInstId() == "ZEST") {
                    enhancedCashierPayModeData.setCardDetailsNotRequired(Boolean.TRUE);
                    enhancedCashierPayModeData.setDisplayName(ZESTMONEY);
                    enhancedCashierPayModeData.setSelectedText(ConfigurationUtil.getProperty(ZEST_MONEY_SELECTED_TEXT,
                            StringUtils.EMPTY));
                }
                enhancedCashierPayModeData.setBankLogoUrl(payChannelBase.getBankLogoUrl());
                enhancedCashierPayModeData.setChannelDisplayName(payChannelBase.getInstDispCode());
                enhancedCashierPayModeData.setHybridDisabled(payChannelBase.isHybridDisabled());
                bank.add(enhancedCashierPayModeData);

            }
            data.setBanks(bank);
            data.getBanks().get(0).setSelected(true);
            savedCard = buildEnhancedCashierSavedCardEmi(nativeCashierInfoResponse, displayIdGenerator);
        }

        return new EnhancedCashierEmiPayMode(displayIdGenerator.generateDisplayId(), payMethod.getDisplayName(),
                PayModeType.valueOf(payMethod.getPayMethod()).getType(), data, savedCard, !Boolean.valueOf(payMethod
                        .getIsDisabled().getStatus()), payMethod.isHybridDisabled(), payMethod.getRemainingLimit(),
                payMethod.getPayOptionRemainingLimits());

    }

    private InitiateTransactionResponse createInitTxnResponse(final String token,
            InitiateTransactionRequestBody initTxnReqBody) {

        /*
         * fetching InitiateTransaction Response from cache
         */
        InitiateTransactionResponseBody body = nativeSessionUtil.getInitiateTxnResponse(token);

        body.setCallbackUrl(getCallBackUrlFromInitTxnReqBody(initTxnReqBody));

        // setting head=null as this response is for internal use only
        return new InitiateTransactionResponse(null, body);
    }

    private boolean isIdempotentInitiateTxnRequest(InitiateTransactionResponse initTxnResp) {
        if (initTxnResp != null
                && initTxnResp.getBody() != null
                && initTxnResp.getBody().getResultInfo() != null
                && StringUtils.equals(SUCCESS_IDEMPOTENT_ERROR.getResultCodeId(), initTxnResp.getBody().getResultInfo()
                        .getResultCode())) {
            return true;
        }
        return false;
    }

    private EnhancedCashierPage getEnhancedCashierPageFromCache(PaymentRequestBean requestData) {
        EnhanceCashierPageCachePayload enhanceCashierPageCachePayload = null;
        EnhancedCashierPage enhancedCashierPage = null;

        try {
            enhanceCashierPageCachePayload = (EnhanceCashierPageCachePayload) nativeSessionUtil
                    .getKey(enhancedCashierPageServiceHelper.fetchRedisKey(requestData.getMid(),
                            requestData.getOrderId()));

            if (enhanceCashierPageCachePayload != null
                    && enhanceCashierPageCachePayload.getEnhancedCashierPage() != null) {
                enhancedCashierPage = enhanceCashierPageCachePayload.getEnhancedCashierPage();
                return enhancedCashierPage;
            }
        } catch (Exception e) {
            LOGGER.error("Exception in fetching EnhancedCashierPage from Cache {}", e);
        }
        return null;
    }

    private void getSavedCardData(PayChannelBase saved, List<SavedInstrument> merchantSavedInstrumentsList,
            EnhancedCashierPageDisplayIdGenerator displayIdGenerator) {

        SavedCard savedCard = (SavedCard) saved;
        SavedInstrument savedInstrument = new SavedInstrument();
        savedInstrument.setSavedInstrumentId(savedCard.getCardDetails().getCardId());
        savedInstrument.setDesc(savedCard.getCardDetails().getCardNumber());
        savedInstrument.setType("sc");
        savedInstrument.setCvvLength(savedCard.getCardDetails().getCvvLength());
        savedInstrument.setCvvRequired(savedCard.getCardDetails().getCvvRequired());
        savedInstrument.setBankLogoUrl(savedCard.getBankLogoUrl());
        if (StringUtils.isNotBlank(savedCard.getCardDetails().getCardType())) {
            savedInstrument.setSubType(EPayMethod.getPayMethodByMethod(savedCard.getCardDetails().getCardType())
                    .getOldName());
        }

        savedInstrument.setDisplayName(savedCard.getDisplayName());
        savedInstrument.setBankName(savedCard.getBankName());
        savedInstrument.setCardType(savedCard.getCardType());
        String firstSixDigit = savedCard.getCardDetails().getFirstSixDigit();
        if (StringUtils.isNotEmpty(savedCard.getCardDetails().getFirstSixDigit())) {
            firstSixDigit = firstSixDigit.substring(0, 4);
        }
        savedInstrument.setFirstSix(firstSixDigit);
        savedInstrument.setLastFour(savedCard.getCardDetails().getLastFourDigit());

        if (savedCard.getAuthModes().contains(AuthMode.PIN.getType())) {
            savedInstrument.setIdebit(true);
        }
        savedInstrument.setEmiAvailable(savedCard.getIsEmiAvailable());
        savedInstrument.setExtra(new HashMap<String, String>());
        savedInstrument.setInstId(savedCard.getIssuingBank());
        savedInstrument.setCardScheme(savedCard.getInstId());
        savedInstrument.setId(String.valueOf(displayIdGenerator.generateDisplayId()));
        if (savedCard.getIsEmiAvailable()) {
            savedInstrument.setMinEMIAmount(savedCard.getMinAmount().getValue());
            savedInstrument.setMaxEMIAmount(savedCard.getMaxAmount().getValue());
            savedInstrument.setEmiHybridDisabled(savedCard.isEmiHybridDisabled());
        }
        savedInstrument.setHybridDisabled(savedCard.isHybridDisabled());
        savedInstrument.setPrepaidCardSupported(savedCard.isPrepaidCardSupported());

        if (iPgpFf4jClient.checkWithdefault(TheiaConstant.FF4J.THEIA_ENHANCED_RETURN_DISABLED_CHANNELS,
                new HashMap<>(), false)
                && savedCard.getIsDisabled() != null
                && Boolean.TRUE.equals(savedCard.getIsDisabled().getShowDisabled())) {
            savedInstrument.setIsDisabled(savedCard.getIsDisabled());
        }
        if (savedCard.getSavedCardEmisubventionDetail() != null) {
            savedInstrument.setSavedCardEmisubventionDetail(savedCard.getSavedCardEmisubventionDetail());
        }
        savedInstrument.setCardCoft(savedCard.isCardCoft());
        savedInstrument.setEligibleForCoft(savedCard.isEligibleForCoft());
        savedInstrument.setCoftPaymentSupported(savedCard.isCoftPaymentSupported());
        savedInstrument.setPar(savedCard.getPar());
        savedInstrument.setTokenStatus(savedCard.getTokenStatus());
        savedInstrument.setPrepaidCard(savedCard.getPrepaidCard());
        savedInstrument.setCorporateCard(savedCard.getCorporateCard());
        merchantSavedInstrumentsList.add(savedInstrument);
        if (savedCard.getPaymentOfferDetails() != null) {
            savedInstrument.setPaymentOfferDetails(savedCard.getPaymentOfferDetails());
        }
    }

    private String getdisplayNameForEMI(EmiChannel emiChannel) {
        if (emiChannel.getInstId().equals(CashierConstant.BAJAJFN)) {
            return emiChannel.getInstName();
        } else {
            return emiChannel.getInstName().concat(StringUtils.SPACE)
                    .concat(EPayMethod.getPayMethodByMethod(emiChannel.getEmiType().getType()).getDisplayName());
        }
    }

    private void setSelectedFlagOnPayModes(List<EnhancedCashierPagePayModeBase> paymodes, PayOption payOption,
            String txnToken, EPayMode paymentMode, boolean checkPcf, boolean isPcfEnabled, boolean isAddMoneyPaymodes,
            EnhancedCashierPageWalletInfo walletInfo) throws Exception {

        if (CollectionUtils.isNotEmpty(paymodes) && paymodes.get(0) != null) {
            // select first paymode
            Iterator<EnhancedCashierPagePayModeBase> iterator = paymodes.iterator();
            EnhancedCashierPagePayModeBase selectedPaymode = iterator.next();
            boolean checkAgain = true;
            while (checkAgain) {
                checkAgain = false;
                if (StringUtils.equalsIgnoreCase(PayModeType.GIFT_VOUCHER.getType(), selectedPaymode.getType())) {
                    EnhancedCashierMgvPayMode mgvPaymode = (EnhancedCashierMgvPayMode) selectedPaymode;
                    if (mgvPaymode.isInsufficientBalance() && iterator.hasNext()) {
                        selectedPaymode = iterator.next();
                        checkAgain = true;
                    }
                }
                if (StringUtils.equalsIgnoreCase(PayModeType.BALANCE.getType(), selectedPaymode.getType())) {
                    EnhancedCashierPageWalletInfo walletPaymode = (EnhancedCashierPageWalletInfo) selectedPaymode;
                    if (!walletPaymode.isEnabled() && iterator.hasNext()) {
                        selectedPaymode = iterator.next();
                        checkAgain = true;
                    }
                }
                if (StringUtils.equalsIgnoreCase(PayModeType.SAVED_CARDS.getType(), selectedPaymode.getType())) {
                    EnhancedCashierSavedCard savedCardPaymode = (EnhancedCashierSavedCard) selectedPaymode;
                    boolean savedCardSelected = false;
                    if (CollectionUtils.isNotEmpty(savedCardPaymode.getSavedCards())) {
                        for (SavedInstrument savedInstrument : savedCardPaymode.getSavedCards()) {
                            if (savedInstrument != null
                                    && (savedInstrument.getIsDisabled() == null || !Boolean.valueOf(savedInstrument
                                            .getIsDisabled().getStatus()))) {
                                savedInstrument.setSelected(true);
                                savedCardSelected = true;
                                break;
                            }
                        }
                    }
                    if (!savedCardSelected) {
                        selectedPaymode = iterator.next();
                        checkAgain = true;
                    } else {
                        checkAgain = false;
                    }
                }
            }
            if (isAddMoneyPaymodes && walletInfo != null && walletInfo.isUsed()
                    && StringUtils.equalsIgnoreCase(PayModeType.UPI.getType(), selectedPaymode.getType())
                    && iterator.hasNext()) {
                selectedPaymode = iterator.next();
            }
            selectedPaymode.setSelected(true);

            if (StringUtils.equalsIgnoreCase(PayModeType.BALANCE.getType(), selectedPaymode.getType())) {
                EnhancedCashierPageWalletInfo walletPaymode = (EnhancedCashierPageWalletInfo) selectedPaymode;
                walletPaymode.setUsed(true);
            } else if (StringUtils.equalsIgnoreCase(PayModeType.SAVED_VPAS.getType(), selectedPaymode.getType())) {
                // Select first saved vpa from the list
                if (CollectionUtils.isNotEmpty(((EnhancedCashierSavedVPA) selectedPaymode).getSavedVPAs())) {
                    SavedVPAInstrument savedVPAInstrument = ((EnhancedCashierSavedVPA) selectedPaymode).getSavedVPAs()
                            .get(0);
                    if (savedVPAInstrument != null) {
                        savedVPAInstrument.setSelected(true);
                        // Add PCF on the selected saved VPA
                        if (!EPayMode.ADDANDPAY.equals(paymentMode) && !EPayMode.HYBRID.equals(paymentMode) && checkPcf
                                && isPcfEnabled) {
                            savedVPAInstrument.setPcfFeeCharges(processTransactionUtil.getPCFFeecharges(txnToken,
                                    EPayMethod.UPI, null, null));
                        }
                    }

                }

            } else if (StringUtils.equalsIgnoreCase(PayModeType.SAVED_CARDS.getType(), selectedPaymode.getType())) {
                EnhancedCashierSavedCard savedCardPaymode = (EnhancedCashierSavedCard) selectedPaymode;
                // Select first saved card from the list
                if (CollectionUtils.isNotEmpty(savedCardPaymode.getSavedCards())) {
                    SavedInstrument savedCard = savedCardPaymode.getSavedCards().stream()
                            .filter(si -> (si != null && si.isSelected())).findFirst()
                            .orElse(savedCardPaymode.getSavedCards().get(0));
                    if (savedCard != null) {
                        // Add PCF on the selected Saved Card
                        if (!EPayMode.ADDANDPAY.equals(paymentMode) && !EPayMode.HYBRID.equals(paymentMode) && checkPcf
                                && isPcfEnabled) {
                            FeeRateFactors feeRateFactors = new FeeRateFactors();
                            feeRateFactors.setInstId(savedCard.getCardScheme());
                            feeRateFactors.setPrepaidCard(BooleanUtils.isTrue(savedCard.getPrepaidCardSupported()));
                            if (payOption.getSavedInstruments() != null) {
                                SavedCard savedInstrument = (SavedCard) payOption
                                        .getSavedInstruments()
                                        .stream()
                                        .filter(sc -> ((SavedCard) sc).getCardDetails().getCardId()
                                                .equals(savedCard.getSavedInstrumentId())).findAny().orElse(null);
                                if (savedInstrument != null && savedInstrument.getCardDetails() != null) {
                                    if (!BooleanUtils.isTrue(savedInstrument.getCardDetails().getIndian())) {
                                        feeRateFactors.setInternationalCardPayment(true);
                                    }
                                }
                            }
                            savedCard.setPcfFeeCharges(processTransactionUtil.getPCFFeecharges(txnToken,
                                    EPayMethod.getPayMethodByOldName(savedCard.getSubType()),
                                    savedCard.getCardScheme(), feeRateFactors));
                        }
                    }
                }
            } else {
                // Add PCF on the selected paymode
                if (!EPayMode.ADDANDPAY.equals(paymentMode) && !EPayMode.HYBRID.equals(paymentMode) && checkPcf
                        && isPcfEnabled) {
                    selectedPaymode.setPcfFeeCharges(processTransactionUtil.getPCFFeecharges(txnToken,
                            EPayMethod.getPayMethodByNewDisplayName(selectedPaymode.getName()), null, null));
                }
            }
        }
    }

    private MandateAccountDetails generateMandateAccountDetails(PaymentRequestBean requestData) {
        MandateAccountDetails mandateAccountDetails = new MandateAccountDetails();
        mandateAccountDetails.setAccountHolderName(requestData.getUserName());
        mandateAccountDetails.setAccountNumber(requestData.getAccountNumber());
        mandateAccountDetails.setAccountType(requestData.getAccountType());
        mandateAccountDetails.setBankCode(requestData.getBankCode());
        mandateAccountDetails.setIfsc(requestData.getBankIFSC());
        return mandateAccountDetails;
    }

    private SubscriptionTransactionRequest cerateSubscriptionTransactionRequest(PaymentRequestBean requestData) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss");
        SubscriptionTransactionRequest subscriptionTransactionRequest = new SubscriptionTransactionRequest();

        // hack to support NORMAL and UNKNOWN Flow in native enhance
        if (StringUtils.isBlank(requestData.getSubsPPIOnly()) && StringUtils.isBlank(requestData.getSubsPaymentMode())) {
            requestData.setSubsPaymentMode(SubsPaymentMode.UNKNOWN.name());
        } else if (SubsPaymentMode.PPI.name().equalsIgnoreCase(requestData.getSubsPaymentMode())) {
            requestData.setSubsPPIOnly("Y");
        }

        // if subscription-amount is not send by merchant use txn-amount as
        // subsmaxamount
        if (AmountType.FIX.getName().equals(requestData.getSubscriptionAmountType())
                && StringUtils.isBlank(requestData.getSubscriptionMaxAmount())) {
            requestData.setSubscriptionMaxAmount(requestData.getTxnAmount());
        }

        // Create request-header
        SecureRequestHeader requestHeader = new SecureRequestHeader();
        requestHeader.setRequestTimestamp(sdf.format(new Timestamp(System.currentTimeMillis())).toString());
        requestHeader.setChannelId(EChannelId.valueOf(requestData.getChannelId()));
        requestHeader.setSignature(requestData.getChecksumhash());
        requestHeader.setWorkFlow(TheiaConstant.EnhancedCashierFlow.ENHANCED_CASHIER_FLOW);

        // Set request-header
        subscriptionTransactionRequest.setHead(requestHeader);

        // Create request-body
        SubscriptionTransactionRequestBody requestBody = new SubscriptionTransactionRequestBody();
        requestBody.setSubsPPIOnly(requestData.getSubsPPIOnly());
        requestBody.setRequestType(requestData.getRequestType());
        requestBody.setMid(requestData.getMid());
        requestBody.setOrderId(requestData.getOrderId());
        requestBody.setWebsiteName(requestData.getWebsite());
        requestBody.setTxnAmount(new Money(requestData.getTxnAmount()));
        requestBody.setUserInfo(getUserInfo(requestData));
        requestBody.setPaytmSsoToken(requestData.getSsoToken());
        requestBody.setPromoCode(requestData.getPromoCampId());

        // Setting emi option
        requestBody.setEmiOption(requestData.getEmiOption());

        requestBody.setCartValidationRequired(BooleanUtils.toStringTrueFalse(requestData.isCartValidationRequired()));
        requestBody.setCardTokenRequired(BooleanUtils.toStringTrueFalse(requestData.getCardTokenRequired()));
        requestBody.setCardHash(requestData.getCardHash());
        requestBody.setExtendInfo(getExtendedInfo(requestData));
        requestBody.setDisablePaymentMode(getDisablePaymentModes(requestData));
        requestBody.setEnablePaymentMode(getEnablePaymentModes(requestData));

        if (!StringUtils.isEmpty(requestData.getCallbackUrl())) {
            requestBody.setCallbackUrl(requestData.getCallbackUrl());
        } else {
            subscriptionTransactionRequest.setBody(requestBody);
            requestBody.setCallbackUrl(getCallBackUrl(subscriptionTransactionRequest));
            requestBody.getExtendInfo().setIsCallbackWebsiteDerived("true");
        }

        if (StringUtils.isNotBlank(requestData.getCreditCardBillNo())) {
            requestBody.setCcBillPayment(new CCBillPayment(requestData.getCreditCardBillNo()));
        }

        requestBody.setChannelId(requestData.getChannelId());

        if (TheiaConstant.RequestTypes.ADD_MONEY.equalsIgnoreCase(requestData.getRequestType())) {
            requestBody.setNativeAddMoney(true);
        }
        requestBody.setSubscriptionPaymentMode(requestData.getSubsPaymentMode());
        requestBody.setSubscriptionAmountType(requestData.getSubscriptionAmountType());
        requestBody.setSubscriptionMaxAmount(requestData.getSubscriptionMaxAmount());
        requestBody.setSubscriptionFrequency(requestData.getSubscriptionFrequency());
        requestBody.setSubscriptionFrequencyUnit(requestData.getSubscriptionFrequencyUnit());
        requestBody.setSubscriptionExpiryDate(requestData.getSubscriptionExpiryDate());
        requestBody.setSubscriptionEnableRetry(requestData.getSubscriptionEnableRetry());
        requestBody.setSubscriptionGraceDays(requestData.getSubscriptionGraceDays());
        requestBody.setSubscriptionStartDate(requestData.getSubscriptionStartDate());
        requestBody.setSubscriptionRetryCount(requestData.getSubscriptionRetryCount());
        requestBody.setAutoRenewal(requestData.isAutoRenewal());
        requestBody.setAutoRetry(requestData.isAutoRetry());
        requestBody.setCommunicationManager(requestData.isCommunicationManager());
        UserInfo userInfo = new UserInfo();
        userInfo.setFirstName(requestData.getUserName());
        userInfo.setMobile(requestData.getMobileNo());
        userInfo.setEmail(requestData.getEmail());
        userInfo.setCustId(requestData.getCustId());
        requestBody.setUserInfo(userInfo);
        requestBody.setRenewalAmount(requestData.getRenewalAmount());
        requestBody.setSubsGoodsInfo(requestData.getSubsGoodsInfo());
        requestBody.setSubscriptionPurpose(requestData.getSubscriptionPurpose());
        // Show only E-mandate banks in enhanced flow if mandateType not
        // received in request
        requestBody.setMandateType(StringUtils.isNotBlank(requestData.getMandateType()) ? requestData.getMandateType()
                : MandateMode.E_MANDATE.name());
        requestData.setMandateType(requestBody.getMandateType());
        requestBody.setMandateAccountDetails(generateMandateAccountDetails(requestData));

        if (ObjectUtils.notEqual(requestData.getUltimateBeneficiaryDetails(), null)
                && StringUtils.isNotBlank(requestData.getUltimateBeneficiaryDetails().getUltimateBeneficiaryName())) {
            UltimateBeneficiaryDetails ultimateBeneficiaryDetails = new UltimateBeneficiaryDetails();
            ultimateBeneficiaryDetails.setUltimateBeneficiaryName(requestData.getUltimateBeneficiaryDetails()
                    .getUltimateBeneficiaryName());
            requestBody.setUltimateBeneficiaryDetails(ultimateBeneficiaryDetails);
        }

        // Set request body
        subscriptionTransactionRequest.setBody(requestBody);

        return subscriptionTransactionRequest;
    }

    private EnhancedCashierSavedMandateBank buildSavedBankMandatePayMode(
            NativeCashierInfoResponseBody nativeCashierInfoResponseBody,
            EnhancedCashierPageDisplayIdGenerator displayIdGenerator) {
        PayOption payOption = nativeCashierInfoResponseBody.getMerchantPayOption();

        List<SavedMandateBank> savedMandateBankList = populateSavedMandateBanks(payOption.getSavedMandateBanks(),
                displayIdGenerator);

        EnhancedCashierSavedMandateBank savedMandateBank = new EnhancedCashierSavedMandateBank(
                displayIdGenerator.generateDisplayId(), EPayMethod.SAVED_MANDATE_BANK.getDisplayName(),
                PayModeType.SAVED_MANDATE_BANKS.getType(), savedMandateBankList);
        savedMandateBank.setSelected(false);
        savedMandateBank.setEnabled(true);
        return savedMandateBank;
    }

    private List<SavedMandateBank> populateSavedMandateBanks(List<SavedMandateBank> savedMandateBanks,
            EnhancedCashierPageDisplayIdGenerator displayIdGenerator) {

        for (SavedMandateBank savedMandateBank : savedMandateBanks) {
            savedMandateBank.setId(String.valueOf(displayIdGenerator.generateDisplayId()));
            savedMandateBank.setType("smb");
        }
        return savedMandateBanks;
    }

    private boolean isNotAppLink() {
        HttpServletRequest httpServletRequest = EnvInfoUtil.httpServletRequest();
        return !Boolean.valueOf(httpServletRequest.getParameter(TheiaConstant.RequestParams.IS_APP_LINK))
                && !Boolean.valueOf(httpServletRequest.getParameter(TheiaConstant.RequestParams.IS_APP_INVOKE));
    }

    private EnhancedCashierPagePayMode buildBankMandatePayMode(PayMethod payMethod,
            EnhancedCashierPageDisplayIdGenerator displayIdGenerator, EnhancedCashierBankData data,
            List<EnhancedCashierPayModeData> bank) {
        if (payMethod.getPayChannelOptions() != null && !payMethod.getPayChannelOptions().isEmpty()) {
            data = new EnhancedCashierBankData();
            bank = new ArrayList<>();
            Iterator<PayChannelBase> payChannelBaseIterator = payMethod.getPayChannelOptions().iterator();
            while (payChannelBaseIterator.hasNext()) {
                BankMandate bankMandate = (BankMandate) payChannelBaseIterator.next();
                EnhancedCashierPayModeData enhancedCashierPayModeData = new EnhancedCashierPayModeData(
                        bankMandate.getMandateMode(), bankMandate.getMandateAuthMode(),
                        bankMandate.getMandateBankCode(), bankMandate.getInstId(), bankMandate.getInstName(),
                        bankMandate.getIconUrl(), bankMandate.getHasLowSuccess(), bankMandate.getIsDisabled(),
                        bankMandate.isHybridDisabled());
                enhancedCashierPayModeData.setChannelDisplayName(bankMandate.getInstDispCode());
                bank.add(enhancedCashierPayModeData);
            }
            data.setBanks(bank);
        }
        return (data == null) ? null : new EnhancedCashierPagePayMode(displayIdGenerator.generateDisplayId(),
                EPayMethod.BANK_MANDATE.getDisplayName(), PayModeType.valueOf(payMethod.getPayMethod()).getType(),
                data, !Boolean.valueOf(payMethod.getIsDisabled().getStatus()), payMethod.isHybridDisabled(),
                payMethod.getRemainingLimit(), payMethod.getPayOptionRemainingLimits());
    }

    private void setRetryInfoForDcc(EnhancedCashierPage enhancedCashierPage, PaymentRequestBean requestData) {

        if (requestData.isPaymentCallFromDccPage()) {
            enhancedCashierPage.getRetryData().setRetryErrorMsg(requestData.getNativeRetryErrorMessage());
        }
    }

    private Boolean isLinkBasedMFOrSTTxn(PaymentRequestBean paymentRequestBean) {
        if (paymentRequestBean.getLinkDetailsData() != null
                && (TheiaConstant.RequestTypes.NATIVE_MF.equals(paymentRequestBean.getLinkDetailsData()
                        .getSubRequestType()) || TheiaConstant.RequestTypes.NATIVE_ST.equals(paymentRequestBean
                        .getLinkDetailsData().getSubRequestType()))) {
            return true;
        }
        return false;
    }
}
