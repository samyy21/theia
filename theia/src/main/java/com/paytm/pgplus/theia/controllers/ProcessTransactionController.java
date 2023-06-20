package com.paytm.pgplus.theia.controllers;

import com.google.gson.Gson;
import com.paytm.pgplus.biz.core.risk.RiskConstants;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.exception.AccountMismatchException;
import com.paytm.pgplus.biz.exception.AccountNotExistsException;
import com.paytm.pgplus.biz.exception.BizMerchantVelocityBreachedException;
import com.paytm.pgplus.biz.exception.EdcLinkBankAndBrandEmiCheckoutException;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.biz.workflow.service.util.FailureLogUtil;
import com.paytm.pgplus.cache.model.MerchantBussinessLogoInfo;
import com.paytm.pgplus.cache.model.MerchantExtendedInfoResponse;
import com.paytm.pgplus.cache.model.MerchantPreferenceInfoResponse;
import com.paytm.pgplus.cache.model.MerchantProfile;
import com.paytm.pgplus.checksum.utils.AggregatorMidKeyUtil;
import com.paytm.pgplus.common.enums.*;
import com.paytm.pgplus.common.model.*;
import com.paytm.pgplus.common.model.link.LinkDetailResponseBody;
import com.paytm.pgplus.common.util.ThreadLocalUtil;
import com.paytm.pgplus.dynamicwrapper.exceptions.WrapperServiceException;
import com.paytm.pgplus.dynamicwrapper.service.IWrapperService;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.enums.UIMicroserviceUrl;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.linkService.services.ILinkService;
import com.paytm.pgplus.facade.merchantlimit.exceptions.MerchantLimitBreachedException;
import com.paytm.pgplus.facade.ppb.models.FetchAccountBalanceResponse;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleAPI;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.IBinFetchService;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.models.CoftConsent;
import com.paytm.pgplus.models.EmiSubventionInfo;
import com.paytm.pgplus.models.SplitSettlementInfoData;
import com.paytm.pgplus.models.UltimateBeneficiaryDetails;
import com.paytm.pgplus.payloadvault.dynamicwrapper.enums.API;
import com.paytm.pgplus.payloadvault.dynamicwrapper.enums.PayloadType;
import com.paytm.pgplus.payloadvault.subscription.response.MandateResponseBody;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.request.UpiLiteRequestData;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.aspects.LocaleFieldAspect;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.cache.model.IPreRedisCacheService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes;
import com.paytm.pgplus.theia.controllers.helper.OldPgRedirectHelper;
import com.paytm.pgplus.theia.controllers.helper.ProcessTransactionControllerHelper;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.*;
import com.paytm.pgplus.theia.helper.UIMicroserviceHelper;
import com.paytm.pgplus.theia.logging.ExceptionLogUtils;
import com.paytm.pgplus.theia.models.*;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.models.uimicroservice.request.UIMicroserviceRequest;
import com.paytm.pgplus.theia.models.uimicroservice.response.UIMicroserviceResponse;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.common.EnhancedCashierPayModeData;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantInfoServiceRequest;
import com.paytm.pgplus.theia.nativ.model.payment.request.NativePaymentRequest;
import com.paytm.pgplus.theia.nativ.model.payview.response.AccountInfo;
import com.paytm.pgplus.theia.nativ.model.payview.response.BalanceChannel;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.service.MerchantInfoService;
import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.nativ.utils.RiskVerificationUtil;
import com.paytm.pgplus.theia.offline.constants.PropertyConstrant;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.*;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.IPaymentService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.services.helper.EncryptedParamsRequestServiceHelper;
import com.paytm.pgplus.theia.services.helper.EnhancedCashierPageServiceHelper;
import com.paytm.pgplus.theia.services.helper.FF4JHelper;
import com.paytm.pgplus.theia.sessiondata.MerchantInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import com.paytm.pgplus.theia.sessiondata.UPITransactionInfo;
import com.paytm.pgplus.theia.utils.*;
import com.paytm.pgplus.theia.validator.service.ValidationService;
import com.paytm.pgplus.theia.workflow.*;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.paytm.pgplus.biz.utils.BizConstant.ERROR_CODE;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.LINK_NATIVE_PAYMENT_IN_PROCESS_ERROR;
import static com.paytm.pgplus.biz.utils.BizConstant.LINK_INVOICE_ID;
import static com.paytm.pgplus.biz.utils.BizConstant.SUBSCRIPTION_ID;
import static com.paytm.pgplus.biz.utils.BizConstant.UPI_ACC_REF_ID;
import static com.paytm.pgplus.biz.utils.BizConstant.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.ASSAM_WRAPPER;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.MANIPUR_WRAPPER;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.LINK_ID;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.CREDIT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.FAILURE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.MERCHANT_NAME;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.SUCCESS;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.TXN_SUCCESS;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.GvConsent.PTR_URL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.CALLBACK_URL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.COFT_CONSENT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.MID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.NATIVE_JSON_REQUEST;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.SSO_TOKEN;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.TIP_AMOUNT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.TRANSACTION_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.TXN_AMOUNT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.WEBSITE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.ORDER_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes.LINK_BASED_PAYMENT;

/**
 * @createdOn 17-Mar-2016
 * @author kesari
 */

@Controller
public class ProcessTransactionController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessTransactionController.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(ProcessTransactionController.class);
    private static final String VIEW_BASE = "/WEB-INF/views/jsp/";
    private static final String CHECKSUM_FAILED_ERROR_MSG = "Checksum validation failed for merchant id : {}";
    private static final String CHECKSUM_UNKNOWN_ERROR_MSG = "Unknown checksum for merchant id : {}";
    private static final String VALIDATION_FAILURE = "Validation failed for merchant id : {}";
    private static final Gson gson = new Gson();
    private static final String ADDITIONAL_INFO_DELIMITER = "|";
    private static final String ADDITIONAL_INFO_KEY_VAL_SEPARATOR = ":";
    private static final String PAYMENT_PROCESSING = "payment is still processing";

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @Autowired
    @Qualifier(value = "wrapperImpl")
    private IWrapperService wrapperService;

    @Autowired
    DynamicWrapperUtil dynamicWrapperUtil;

    @Autowired
    private LocaleFieldAspect localeFieldAspect;

    @Autowired
    private LocalizationUtil localizationUtil;

    @Autowired
    @Qualifier(value = "seamlessNBPaymentValidationService")
    private SeamlessNBPaymentService seamlessNBPaymentService;

    @Autowired
    EncryptedParamsRequestServiceHelper encParamRequestService;

    @Autowired
    @Qualifier("processTransactionUtil")
    private ProcessTransactionUtil processTransactionUtil;

    @Autowired
    @Qualifier("invoiceService")
    private InvoiceService invoiceService;

    @Autowired
    @Qualifier("defaultService")
    private DefaultService defaultService;

    @Autowired
    @Qualifier("addMoneyService")
    private AddMoneyService addMoneyService;

    @Autowired
    @Qualifier("subscriptionService")
    private SubscriptionService subscriptionService;

    @Autowired
    @Qualifier("nativeService")
    private NativeService nativeService;

    @Autowired
    @Qualifier("seamlessService")
    private SeamlessService seamlessService;

    @Autowired
    @Qualifier("expressService")
    private ExpressService expressService;

    @Autowired
    @Qualifier("stockTradingService")
    private StockTradingService stockTradingService;

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    private IMerchantDataService merchantDataService;

    @Autowired
    @Qualifier("theiaLinkService")
    private TheiaLinkService theiaLinkService;

    @Autowired
    @Qualifier("motoService")
    private MotoService motoService;

    @Autowired
    @Qualifier("qrService")
    private QRService qrService;

    @Autowired
    @Qualifier("processTransactionControllerHelper")
    ProcessTransactionControllerHelper processTransactionControllerHelper;

    @Autowired
    @Qualifier(value = "enhancedCashierPageService")
    private IPaymentService enhancedCashierPageService;

    @Autowired
    @Qualifier(value = "merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier(value = "defaultMFService")
    private DefaultMFService defaultMFService;

    @Autowired
    private StagingParamValidator stagingParamValidator;

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    EnhancedCashierPageServiceHelper enhancedCashierPageServiceHelper;

    @Autowired
    NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("binfetchservice")
    private IBinFetchService binFetchService;

    @Autowired
    private OldPgRedirectHelper oldPgRedirectHelper;

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    @Qualifier("preRedisCacheServiceImpl")
    private IPreRedisCacheService preRedisCacheServiceImpl;

    @Autowired
    @Qualifier("merchantPreferenceProvider")
    private MerchantPreferenceProvider merchantPreferenceProvider;

    @Autowired
    private RiskVerificationUtil riskVerificationUtil;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    private ILinkService linkService;

    @Autowired
    MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    private LinkPaymentUtil linkPaymentUtil;

    @Autowired
    private AddMoneyToGvConsentUtil addMoneyToGvConsentUtil;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;
    @Autowired
    private UPIPollPageUtil upiPollPageUtil;

    @Autowired
    private UIMicroserviceHelper uiMicroserviceHelper;

    @Autowired
    private FF4JHelper ff4JHelper;

    @Autowired
    private Ff4jUtils ff4JUtils;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    private static final String REQUEST_TYPE = "REQUEST_TYPE";

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("aoaUtils")
    private AOAUtils aoaUtils;

    @Autowired
    private FF4JUtil ff4JUtil;

    @Autowired
    @Qualifier("failureLogUtil")
    private FailureLogUtil failureLogUtil;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private MerchantInfoService merchantInfoService;

    /**
     * @param request
     * @param response
     * @param model
     * @param locale
     * @return
     * @throws IOException
     *             extended in sdk/processTransaction
     */
    @LocaleAPI(apiName = PTR_URL, responseClass = EnhancedCashierPayModeData.class, isResponseObjectType = false)
    @RequestMapping(value = "/processTransaction", method = { RequestMethod.GET, RequestMethod.POST })
    public void processPaymentPage(HttpServletRequest request, final HttpServletResponse response, final Model model,
            final Locale locale) throws Exception {
        final long startTime = System.currentTimeMillis();
        boolean isRedisOPtimizedFlow = ConfigurationUtil.isRedisOPtimizedFlow();
        try {
            if (oldPgRedirectHelper.isOldPGRequest(request)) {
                oldPgRedirectHelper.handleOldPgRedirect(request, response, false);
                return;
            }
            MerchantPreferenceInfoResponse merchantPreferenceInfoResponse = null;
            if (isRedisOPtimizedFlow) {
                merchantPreferenceInfoResponse = preRedisCacheServiceImpl.getMerchantPreferenceInfoResponse(request
                        .getParameter(RequestParams.MID));
            }
            checkIfMerchantBlocked(request);
            EncryptedParameterRequest encryptedParameterRequest = encParamRequestService.wrapHttpRequestIfEncrypted(
                    request, response, merchantPreferenceInfoResponse);

            if (encryptedParameterRequest != null) {
                request = encryptedParameterRequest.getRequest();
            }
            if (!isValidRequestForStaging(request, merchantPreferenceInfoResponse)) {
                return;
            }
            // makeSessionForLinkBasedPayment(request);
            if (Boolean.TRUE.toString().equals(request.getParameter(TheiaConstant.GvConsent.GV_CONSENT_FLOW))) {
                addMoneyToGvConsentUtil.setAttributesForGvConsentFlow(request);
            }
            processPaymentRequest(request, response, model, true, merchantPreferenceInfoResponse);
            if (request.getAttribute(TheiaConstant.GvConsent.ADD_MONEY_TO_GV_CONSENT_KEY) != null) {
                request.setAttribute(TheiaConstant.GvConsent.GV_CONSENT_FLOW, Boolean.TRUE.toString());
                nativeSessionUtil.setKey(
                        (String) request.getAttribute(TheiaConstant.GvConsent.ADD_MONEY_TO_GV_CONSENT_KEY),
                        request.getParameterMap(), 900);
                request.removeAttribute(TheiaConstant.GvConsent.ADD_MONEY_TO_GV_CONSENT_KEY);
            }
            // RedisClientJedisService.logRedisConnectionStats();
            // RedisClientLettuceService.logLettuceConnectionStats("normal_stats");
            return;
        } catch (AccountNumberMismatchException | AccountNumberNotExistException | InvalidRequestParameterException
                | StagingRequestException e) {
            throw e;
        } catch (final NativeFlowException nfe) {
            LOGGER.error("NativeFlowException in /processTransaction : ", nfe);
            throw nfe;
        } catch (PaymentRequestValidationException e) {
            LOGGER.error("request is invalid in processTransactioz ", e);
            throw e;
        } catch (final Exception e) {
            LOGGER.error("SYSTEM_ERROR : {}", e.getMessage());
            throw e;
        } finally {
            LOGGER.info("Total time taken for ProcessTransactionController is {} ms", System.currentTimeMillis()
                    - startTime);
        }
    }

    /**
     * @param request
     * @param response
     * @param model
     * @param locale
     * @return
     * @throws IOException
     *             This API is used to initiate/create order and return the app
     *             intent Url to invoke app.
     */
    @RequestMapping(value = "/initiate/processTransaction", method = { RequestMethod.POST })
    public void createTransaction(HttpServletRequest request, final HttpServletResponse response, final Model model,
            final Locale locale) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /initiate/processTransaction");
            Map<String, String> metadata = createEventLogMetadata(request);
            nativePaymentUtil.logNativeRequests(request.toString(), INITIATE_PROCESS_TRANSACTION, metadata);
            // This is to distinguish b/w ptc and initiate/ptc
            request.setAttribute(NEED_APP_INTENT_ENDPOINT, true);
            processPaymentPage(request, response, model, locale);
            LOGGER.info("Native response returned for API: /initiate/processTransaction");
        } finally {
            LOGGER.info("Total time taken for Initiate ProcessTransaction is {} ms", System.currentTimeMillis()
                    - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

    public Map<String, String> createEventLogMetadata(HttpServletRequest request) {
        Map<String, String> metadata = new HashMap<>();

        if (request != null) {
            metadata.put(TheiaConstant.EventLogConstants.MID, MDC.get(TheiaConstant.EventLogConstants.MID));
            metadata.put(TheiaConstant.EventLogConstants.ORDER_ID, MDC.get(TheiaConstant.EventLogConstants.ORDER_ID));
            metadata.put(TheiaConstant.EventLogConstants.REQUEST_TYPE, request.getParameter(REQUEST_TYPE));
            metadata.put(TheiaConstant.EventLogConstants.OS_VERSION, request.getParameter(OS));
            metadata.put(TheiaConstant.EventLogConstants.PLATFORM, request.getParameter(PLATFORM));
            metadata.put(TheiaConstant.EventLogConstants.PLATFORM_VERSION, request
                    .getParameter(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.VERSION));
        }

        return metadata;
    }

    @RequestMapping(value = "/customProcessTransaction", method = { RequestMethod.GET, RequestMethod.POST })
    public void customProcessTransaction(final HttpServletRequest request, final HttpServletResponse response,
            final Model model) throws IOException, ServletException {

        final long startTime = System.currentTimeMillis();
        String merchantId = (request.getParameter(TheiaConstant.RequestParams.MID) != null) ? request
                .getParameter(TheiaConstant.RequestParams.MID) : (String) request.getAttribute(RequestParams.MID);
        if (ff4JUtils.isFeatureEnabled(THEIA_VALIDATE_MID_ENABLE, false) && !validationService.validateMid(merchantId)) {
            throw new TheiaServiceException("CRITICAL_ERROR : Invalid Merchant Id : " + merchantId);
        }
        LOGGER.info("Request received for custom Process Transaction for merchantId : {}", merchantId);
        try {
            if (oldPgRedirectHelper.isOldPGRequest(request)) {
                oldPgRedirectHelper.handleOldPgRedirect(request, response, true);
                return;
            }
            if (isNotPageRefresh(request)) {
                PaymentRequestBean paymentRequestBean = null;
                try {
                    LOGGER.info("DynamicWrapperEnabled = {}, merchantConfPresent = {}", dynamicWrapperUtil
                            .isDynamicWrapperEnabled(), dynamicWrapperUtil.isDynamicWrapperConfigPresent(merchantId,
                            API.PROCESS_TRANSACTION, PayloadType.REQUEST));
                    Map<String, String[]> additionalParamMap = createAdditionalParamMap(merchantId);
                    ModifiableHttpServletRequest servletRequestWrapper = new ModifiableHttpServletRequest(request,
                            additionalParamMap);
                    if (dynamicWrapperUtil.isDynamicWrapperEnabled()
                            && dynamicWrapperUtil.isDynamicWrapperConfigPresent(merchantId, API.PROCESS_TRANSACTION,
                                    PayloadType.REQUEST)) {
                        LOGGER.info("Config check passed");
                        paymentRequestBean = wrapperService.wrapRequest(servletRequestWrapper, merchantId,
                                API.PROCESS_TRANSACTION);
                        MDC.clear();
                        MDC.put(RequestParams.MID, paymentRequestBean.getMid());
                        MDC.put(RequestParams.ORDER_ID, paymentRequestBean.getOrderId());
                        paymentRequestBean.setRequest(request);
                        LOGGER.info("paymentRequestBean created = {}", paymentRequestBean);
                        validateRequestBean(paymentRequestBean);
                        theiaTransactionalRedisUtil.set(
                                paymentRequestBean.getMid() + "#" + paymentRequestBean.getOrderId(),
                                paymentRequestBean.getExtraParamsMap(), 900);
                    } else {
                        LOGGER.info("Config check failed");
                        processPaymentPage(request, response, model, null);
                        return;
                    }
                } catch (WrapperServiceException e) {
                    LOGGER.error(ExceptionUtils.getMessage(e));
                } catch (BaseException ex) {
                    String htmlPage = null;
                    if (ex.getResultInfo() != null) {
                        htmlPage = merchantResponseService.processMerchantFailResponse(paymentRequestBean,
                                ex.getResultInfo());
                    } else {
                        htmlPage = merchantResponseService.processMerchantFailResponse(paymentRequestBean,
                                ResponseConstants.SYSTEM_ERROR);
                    }
                    response.getWriter().write(htmlPage);
                    return;
                }
                processRequest(request, response, paymentRequestBean, model);
                return;
            }

            /** This method clears all error params from session.. */
            clearErrorParams(request);
            request.getRequestDispatcher(VIEW_BASE + theiaViewResolverService.returnPaymentPage(request) + ".jsp")
                    .forward(request, response);
            return;
        } catch (PaymentRequestValidationException e) {
            LOGGER.error("request validation exception in customProcessTransaction: ", e);
        } catch (final Exception e) {
            LOGGER.error("SYSTEM_ERROR : {}", e.getMessage());
        } finally {
            LOGGER.info("Total time taken for ProcessTransactionController is {} ms", System.currentTimeMillis()
                    - startTime);
        }

        request.getRequestDispatcher(VIEW_BASE + theiaViewResolverService.returnOOPSPage(request) + ".jsp").forward(
                request, response);
        return;
    }

    private void validateRequestBean(PaymentRequestBean paymentRequestBean) {
        /**
         * lic related
         */
        if (StringUtils.isNotBlank(paymentRequestBean.getRequestedTimeStamp())) {
            String[] dateTime = paymentRequestBean.getRequestedTimeStamp().trim().split(Pattern.quote("\\s+"));
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            try {
                Date requestedDate = dateFormat.parse(dateTime[0]);
                Date todayDate = new Date();
                if (!DateUtils.isSameDay(requestedDate, todayDate)) {
                    LOGGER.error("requestDate is either before or after todays date {} ", requestedDate);
                    throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
                }

            } catch (ParseException e) {
                LOGGER.error("AdditionalInfo6 is not in correct format {}", paymentRequestBean.getRequestedTimeStamp());
                throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
            }
        }
        if (paymentRequestBean.getExtraParamsMap() != null
                && paymentRequestBean.getExtraParamsMap().get("isSplitInfoRequired") != null) {
            if ("true".equals(paymentRequestBean.getExtraParamsMap().get("isSplitInfoRequired"))
                    && paymentRequestBean.getSplitSettlementInfoData() == null) {
                LOGGER.error("Invalid Office Code {}", paymentRequestBean.getExtraParamsMap().get("AdditionalInfo1"));
                throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
            }
        }
        if (paymentRequestBean.getExtraParamsMap() != null
                && paymentRequestBean.getExtraParamsMap().get("wrapperName") != null) {
            String wrapperName = paymentRequestBean.getExtraParamsMap().get("wrapperName").toString();
            try {
                if (StringUtils.equals("Rajasthan", wrapperName)) {
                    Double amt = Double.parseDouble(paymentRequestBean.getExtraParamsMap().get("AMT").toString());
                    Double txnAmount = Double.parseDouble(paymentRequestBean.getTxnAmount());
                    if (Double.compare(txnAmount, amt) != 0) {
                        LOGGER.error("Amount does not match {} {}", txnAmount, amt);
                        throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
                    }

                }
            } catch (Exception e) {
                LOGGER.error("Amount parsing error {}", e.getMessage());
                throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
            }
            // Manipur related validations
            if (StringUtils.equals(MANIPUR_WRAPPER, wrapperName)) {
                validateRequestParams(paymentRequestBean);
            }

            // Assam related validations
            if (StringUtils.equals(ASSAM_WRAPPER, wrapperName)) {
                boolean requestParamsValidation = (boolean) paymentRequestBean.getExtraParamsMap().get(
                        "requestParamsValidationResult");
                if (!requestParamsValidation) {
                    LOGGER.error("Reuest params validation failed");
                    throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
                }
            }
        }
    }

    private void validateRequestParams(PaymentRequestBean paymentRequestBean) {
        String object = "Object";
        String objectAmt = "ObjectAmt";
        Map<String, Object> extraParamsMap = paymentRequestBean.getExtraParamsMap();
        for (int i = 1; i <= 9; i++) {
            if ((Objects.nonNull(extraParamsMap.get(object + i)) && StringUtils.isNotBlank(extraParamsMap.get(
                    object + i).toString()))
                    && (Objects.nonNull(extraParamsMap.get(objectAmt + i))
                            && StringUtils.isBlank(extraParamsMap.get(objectAmt + i).toString()) || Objects
                                .isNull(extraParamsMap.get(objectAmt + i)))) {
                LOGGER.error("Request Param validation failed ObjectAmt" + i + " is {}",
                        extraParamsMap.get(objectAmt + i));
                throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
            }
        }
        Map<String, String> encryptedParams = getEncryptedParams(extraParamsMap);

        if (StringUtils.isBlank(paymentRequestBean.getOrderId()) || StringUtils.isBlank(encryptedParams.get("GRN"))
                || !encryptedParams.get("GRN").equals(paymentRequestBean.getOrderId())) {
            LOGGER.error("Request Param validation failed for GRN");
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
        if (StringUtils.isBlank(paymentRequestBean.getTxnAmount())
                || StringUtils.isBlank(encryptedParams.get("TotalAmount"))
                || !encryptedParams.get("TotalAmount").equals(paymentRequestBean.getTxnAmount())) {
            LOGGER.error("Request Param validation failed for TotalAmount");
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
        if (Objects.isNull(extraParamsMap.get("PayeeName")) || StringUtils.isBlank(encryptedParams.get("PayeeName"))
                || !encryptedParams.get("PayeeName").equals(extraParamsMap.get("PayeeName").toString())) {
            LOGGER.error("Request Param validation failed for PayeeName");
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
        if (Objects.isNull(extraParamsMap.get("ChallanInitiationDate"))
                || StringUtils.isBlank(encryptedParams.get("ChallanInitiationDate"))
                || !encryptedParams.get("ChallanInitiationDate").equals(
                        extraParamsMap.get("ChallanInitiationDate").toString())) {
            LOGGER.error("Request Param validation failed for ChallanInitiationDate");
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }

    }

    private Map<String, String> getEncryptedParams(Map<String, Object> extraParamsMap) {
        Map<String, String> encryptedParams = new HashMap<>();
        String decryptedData = Objects.isNull(extraParamsMap.get("decryptedDataWithoutChecksum")) ? null
                : extraParamsMap.get("decryptedDataWithoutChecksum").toString();
        if (StringUtils.isNotBlank(decryptedData)) {
            String[] splitRequest = decryptedData.split(Pattern.quote("|"));
            for (String splitted : splitRequest) {
                String key = splitted.substring(0, splitted.indexOf("="));
                String value = splitted.substring(splitted.indexOf("=") + 1);
                if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
                    encryptedParams.put(key, value);
                }
            }
        }

        return encryptedParams;
    }

    private Map<String, String[]> createAdditionalParamMap(String merchantId) {
        Map<String, String[]> addtionalMap = new HashMap<>();
        addtionalMap.put("CHECKSUM_ENABLED",
                new String[] { String.valueOf(merchantPreferenceService.isChecksumEnabled(merchantId)) });
        addtionalMap.put("MERCHANT_KEY", new String[] { merchantExtendInfoUtils.getMerchantKey(merchantId) });
        return addtionalMap;
    }

    private void processPaymentRequest(final HttpServletRequest request, final HttpServletResponse response,
            final Model model, final boolean isCashierPageRenderRequest,
            MerchantPreferenceInfoResponse merchantPreferenceInfoResponse) throws IOException, ServletException {

        final PaymentRequestBean paymentRequestData;
        if (Boolean.TRUE.toString().equals(request.getParameter(TheiaConstant.GvConsent.GV_CONSENT_FLOW))
                && !RequestTypes.NATIVE.equals(request.getParameter(REQUEST_TYPE))) {
            paymentRequestData = new PaymentRequestBean(request, true);
        } else {
            paymentRequestData = new PaymentRequestBean(request);
        }
        EXT_LOGGER.customInfo("paymentRequestData :  {} ", paymentRequestData);

        pushPaymentEventForCCOnUPI(request, paymentRequestData);

        if (ff4JUtils.isFeatureEnabled(THEIA_VALIDATE_MID_ENABLE, false)
                && !validationService.validateMid(paymentRequestData.getMid())) {
            throw new TheiaServiceException("CRITICAL_ERROR : Invalid NATIVE_MID : " + paymentRequestData.getMid());
        }
        if (StringUtils.isBlank(paymentRequestData.getCardPreAuthType())
                && Objects.isNull(paymentRequestData.getPreAuthBlockSeconds())
                && StringUtils.isNotBlank(paymentRequestData.getTxnToken())) {
            InitiateTransactionRequestBody orderDetail = nativeSessionUtil.getOrderDetail(paymentRequestData
                    .getTxnToken());
            if (Objects.nonNull(orderDetail)) {
                {
                    if (Objects.nonNull(orderDetail.getCardPreAuthType()))
                        paymentRequestData.setCardPreAuthType(orderDetail.getCardPreAuthType().name());
                    if (Objects.nonNull(orderDetail.getPreAuthBlockSeconds()))
                        paymentRequestData.setPreAuthBlockSeconds(orderDetail.getPreAuthBlockSeconds());
                }
            }

        }
        if (paymentRequestData.getExtendInfo() != null && paymentRequestData.getExtendInfo().getHeadAccount() != null) {
            workFlowHelper.setChallanIdNum(paymentRequestData);
        }
        processTransactionUtil.cacheHostForOldPG(request, paymentRequestData);
        pushPaymentEvent(paymentRequestData);
        requestValidationForDefaultMFEnhanceFlow(paymentRequestData);
        if (isNotPageRefresh(request)) {
            settingSplitSettlementInfo(paymentRequestData, request);
            if (ConfigurationUtil.isRedisOPtimizedFlow()) {
                MerchantExtendedInfoResponse merchantExtendedInfoResponse = preRedisCacheServiceImpl
                        .getMerchantExtendedDataWithoutCache(paymentRequestData.getMid());
                paymentRequestData.setMerchantPreferenceInfoResponse(merchantPreferenceInfoResponse);
                paymentRequestData.setMerchantExtendedInfoResponse(merchantExtendedInfoResponse);
            }
            processTransactionControllerHelper.removeTagLineForOffline(paymentRequestData);
            processRequest(request, response, paymentRequestData, model);
            return;
        } else {
            settingSplitSettlementInfo(paymentRequestData, request);
            boolean isEnhancedCashierFlow = processTransactionControllerHelper.checkIfEnhancedCashierFlow(
                    paymentRequestData, request);
            if (isCashierPageRenderRequest && isEnhancedCashierFlow) {
                /*
                 * this is when its case of refresh for enhanced native, we give
                 * push-app data
                 */
                processRequest(request, response, paymentRequestData, model);
                return;
            }
            if ((ERequestType.NATIVE.name().equals(paymentRequestData.getRequestType()) || ERequestType.UNI_PAY.name()
                    .equals(paymentRequestData.getRequestType()))
                    && !isCashierPageRenderRequest
                    && !isEnhancedCashierFlow) {
                processRequest(request, response, paymentRequestData, model);
                return;
            }
        }

        /** This method clears all error params from session.. */

        clearErrorParams(request);
        validatePPBLAccountNumberForMF(request);
        request.getRequestDispatcher(VIEW_BASE + theiaViewResolverService.returnPaymentPage(request) + ".jsp").forward(
                request, response);

    }

    private void pushPaymentEvent(PaymentRequestBean paymentRequestData) {
        if (ERequestType.OFFLINE.getType().equals(paymentRequestData.getRequestType())) {
            processTransactionUtil.pushPaymentEvent(EventNameEnum.OFFLINE_PAYMENT_REQUEST,
                    paymentRequestData.getPaymentTypeId());
        }
    }

    private boolean isValidRequestForStaging(HttpServletRequest request,
            MerchantPreferenceInfoResponse merchantPreferenceInfoResponse) {
        return stagingParamValidator.validate(request, merchantPreferenceInfoResponse);
    }

    private void validatePPBLAccountNumberForMF(HttpServletRequest request) {
        String requestType = theiaSessionDataService.geExtendedInfoRequestBean(request).getRequestType();
        if (!ERequestType.DEFAULT_MF.name().equals(requestType)) {
            LOGGER.info("Validating PPBL Account Number, request received {}, expected {}", requestType,
                    ERequestType.DEFAULT_MF.name());
            return;
        }

        StringBuilder sb = new StringBuilder();
        final TransactionInfo transInfo = theiaSessionDataService.getTxnInfoFromSession(request);
        if (transInfo != null) {
            sb.append(TheiaConstant.RetryConstants.RETRY_PAYMENT_).append(transInfo.getTxnId());
        }
        PaymentRequestBean originalRequest = (PaymentRequestBean) theiaTransactionalRedisUtil.get(sb.toString());
        originalRequest.setRequest(request);

        if (!ExtraConstants.PAYMENTS_BANK_CODE.equals(originalRequest.getBankCode())) {
            return;
        }

        String accountNumber = theiaSessionDataService.getSavingsAccountInfoFromSession(request, true)
                .getAccountNumber();
        if (StringUtils.isEmpty(accountNumber)) {
            failureLogUtil.setFailureMsgForDwhPush(null,
                    BizConstant.FailureLogs.ACCOUNT_NUMBER_FOR_DEFAULT_MF_IS_EMPTY, null, true);

            throw new AccountNumberNotExistException(BizConstant.FailureLogs.ACCOUNT_NUMBER_FOR_DEFAULT_MF_IS_EMPTY,
                    originalRequest);
        }
        if (originalRequest != null && StringUtils.isNotEmpty(originalRequest.getAccountNumber())
                && originalRequest.getAccountNumber().equals(accountNumber)) {
            {
                LOGGER.info("Account Number matched for DEFAULT_MF");
                return;
            }
        }
        failureLogUtil.setFailureMsgForDwhPush(null, BizConstant.FailureLogs.ACCOUNT_NUMBER_MISMATCH_FOR_DEFAULT_MF,
                null, true);

        throw new AccountNumberMismatchException(BizConstant.FailureLogs.ACCOUNT_NUMBER_MISMATCH_FOR_DEFAULT_MF,
                originalRequest);
    }

    private void pushPaymentEventForCCOnUPI(HttpServletRequest request, PaymentRequestBean paymentRequestData) {
        String accountType = "";
        try {
            Map<String, Object> creditBlockMap = null;
            if (null != paymentRequestData.getCreditBlock()) {
                creditBlockMap = JsonMapper.getMapFromJson(paymentRequestData.getCreditBlock());
            }

            Map<String, String> defaultDebitMap = null;
            if (null != creditBlockMap) {
                defaultDebitMap = (Map<String, String>) creditBlockMap.get("defaultDebit");
            }

            if (null != creditBlockMap && defaultDebitMap == null) {
                accountType = (String) creditBlockMap.get("accountType");
            }
            if (null != defaultDebitMap) {
                accountType = defaultDebitMap.get("accountType");
            }
        } catch (Exception e) {
            LOGGER.info("creditBlock in received in request {}", request.getParameter(Native.CREDIT_BLOCK));
            LOGGER.error("Exception while fetching accountType from request ", e);
        }
        if (CREDIT.equalsIgnoreCase(accountType)) {
            paymentRequestData.setCCOnUPI(true);
        }
    }

    private void requestValidationForDefaultMFEnhanceFlow(PaymentRequestBean paymentRequestData) {
        if (StringUtils.equals(ERequestType.DEFAULT_MF.getType(), paymentRequestData.getRequestType())) {
            if (StringUtils.isEmpty(paymentRequestData.getAccountNumber())
                    || StringUtils.isEmpty(paymentRequestData.getBankCode())) {
                throw new InvalidRequestParameterException("Invalid request received for DEFAULT_MF",
                        paymentRequestData);
            }
        }
        // In case of request_type default_mf for enhance flow, account
        // validation is done here
        if (StringUtils.isNotBlank(paymentRequestData.getTxnToken())) {
            InitiateTransactionRequestBody orderDetail = nativeSessionUtil.getOrderDetail(paymentRequestData
                    .getTxnToken());
            if (orderDetail != null) {
                String requestType = orderDetail.getRequestType();
                String accountNumber = orderDetail.getAccountNumber();
                FetchAccountBalanceResponse accountBalanceResponse = nativeSessionUtil
                        .getAccountBalanceResponseFromCache(paymentRequestData.getTxnToken());
                String bankAccountNumber = "";
                if (accountBalanceResponse != null)
                    bankAccountNumber = accountBalanceResponse.getAccountNumber();
                if (StringUtils.isEmpty(requestType) || StringUtils.isEmpty(accountNumber))
                    return;
                if (ERequestType.DEFAULT_MF.getType().equals(requestType)) {
                    LOGGER.info("Payment Bank is allowed for this transaction {}, Going to Match Account No",
                            requestType);
                    if (StringUtils.isEmpty(bankAccountNumber)) {
                        throw new AccountNumberNotExistException("Account Number received for DEFAULT_MF is empty",
                                paymentRequestData, ResponseConstants.ACCOUNT_NUMBER_NOT_EXIST);
                    }
                    if (!StringUtils.equals(bankAccountNumber, accountNumber)) {
                        throw new AccountNumberMismatchException("Account Number Mismatch for DEFAULT_MF",
                                paymentRequestData, ResponseConstants.ACCOUNT_NUMBER_MISMATCH);
                    }
                    LOGGER.info("Account Number matched for DEFAULT_MF");
                }
            }
        }
    }

    @LocaleAPI(apiName = "/theia/api/v1/processTransaction", responseClass = TransactionInfo.class, isResponseObjectType = false)
    @RequestMapping(value = "/api/v1/processTransaction", method = { RequestMethod.POST }, consumes = {
            MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE })
    public void nativeProcessPayment(final HttpServletRequest request, final HttpServletResponse response,
            final Model model, final Locale locale) throws ServletException, IOException, FacadeCheckedException {

        final long startTime = System.currentTimeMillis();

        Map<String, String[]> additionalParams = null;
        Map<String, String[]> nativeRequestParamMap = null;
        ModifiableHttpServletRequest mServletRequest = null;
        ModifiableHttpServletRequest cachedServletRequest = null;
        /*
         * NativePaymentRequest is used for payment call, request comes as JSON,
         * but this has been made specific for enhancedCashierPage UI
         */
        NativePaymentRequest nativePaymentRequest = null;

        /*
         * NativeJsonRequest is used for payment call, request comes as JSON,
         * but this has been made specific for nativePlusFlow
         */
        NativeJsonRequest nativeJsonRequest = null;

        request.setAttribute("NATIVE_ENHANCED_FLOW", false);

        try {
            /*
             * check if it is risk verified pay resume request
             */
            if (Boolean.valueOf(request.getParameter(RiskConstants.RISK_VERIFIER_UI_KEY))) {
                resumeRiskVerifiedRequest(request, response, model);
                return;
            }
            /*
             * check if it is KYC flow
             */
            if (Boolean.TRUE.toString().equals(request.getParameter(TheiaConstant.GvConsent.GV_CONSENT_FLOW))) {
                resumeGvConsentFlowRequest(request, response, model);
                return;
            }
            if (processTransactionUtil.isNativeKycFlow(request)) {
                additionalParams = new HashMap<>();

                boolean isKycProcessed = processTransactionUtil.processNativeKycFlow(request, response,
                        additionalParams);
                if (!isKycProcessed) {
                    return;
                }

                mServletRequest = new ModifiableHttpServletRequest(request, additionalParams);
                processPaymentRequest(mServletRequest, response, model, false, null);
                checkPaymentCountBreached(request);
                return;
            }

            if (Boolean.parseBoolean(ConfigurationUtil.getProperty(
                    TheiaConstant.ExtraConstants.DUPLICATE_PAYMENT_REQUEST_CHECK, "true"))) {
                processTransactionUtil.checkIfDuplicatePaymentRequest(request);
            }

            Boolean paymentCallFromDcc = (Boolean) request.getAttribute(TheiaConstant.DccConstants.PAYMENT_CALL_DCC);
            String merchantId = (request.getParameter(TheiaConstant.RequestParams.MID) != null) ? request
                    .getParameter(TheiaConstant.RequestParams.MID) : (String) request.getAttribute(RequestParams.MID);
            String requestData = IOUtils.toString(request.getInputStream(), Charsets.UTF_8.name());

            if (BooleanUtils.isTrue(paymentCallFromDcc)) {
                nativeJsonRequest = JsonMapper.mapJsonToObject(requestData, NativeJsonRequest.class);
                nativeRequestParamMap = buildRequestForDccPayment(request, requestData, nativeJsonRequest);
                mServletRequest = new ModifiableHttpServletRequest(request, nativeRequestParamMap);
                additionalParams = processTransactionUtil.getAdditionalParamMap(mServletRequest, response);
                mServletRequest = new ModifiableHttpServletRequest(mServletRequest, additionalParams);

                checkPaymentCountBreached(request);
                checkIfMerchantBlocked(request);
                processPaymentRequest(mServletRequest, response, model, false, null);
                return;
            }

            if (StringUtils.isNotBlank(requestData)) {
                String workFlow = (String) request.getAttribute(TheiaConstant.EnhancedCashierFlow.WORKFLOW);
                boolean isQRIdFlowOnly = false;

                if (StringUtils.equals(workFlow, TheiaConstant.EnhancedCashierFlow.ENHANCED_CASHIER_FLOW)) {
                    /*
                     * This block handles for NativePaymentRequest
                     * (enhancedNative!)
                     */
                    LOGGER.info("Payment call for Enhanced Flow");
                    request.setAttribute("NATIVE_ENHANCED_FLOW", true);

                    nativePaymentRequest = JsonMapper.mapJsonToObject(requestData, NativePaymentRequest.class);
                    if (nativePaymentRequest.getHead() != null) {
                        // request Id attribute set for logging purpose (event
                        // Log)
                        request.setAttribute(Native.REQUEST_ID, nativePaymentRequest.getHead().getRequestId());
                    }
                    if (nativePaymentRequest.getBody() != null
                            && nativePaymentRequest.getBody().getExtendInfo() != null
                            && nativePaymentRequest.getBody().getExtendInfo()
                                    .get(TheiaConstant.RequestParams.Native.PAYERCMID) != null) {
                        request.setAttribute(TheiaConstant.RequestParams.Native.PAYERCMID, nativePaymentRequest
                                .getBody().getExtendInfo().get(TheiaConstant.RequestParams.Native.PAYERCMID));
                    }
                    EnvInfoUtil.setChannelDFromUserAgent(nativePaymentRequest.getHead());
                    nativeRequestParamMap = mapJsonRequestParametersForEnhancedFlow(nativePaymentRequest);

                    processTransactionUtil.pushNativeEnhancedEvent(nativePaymentRequest.getBody().getMid(),
                            nativePaymentRequest.getBody().getOrderId(), "Payment call for Enhanced Flow",
                            nativePaymentRequest.getBody().getPaymentMode());

                } else {
                    /*
                     * This block handles for NativeJsonRequest (nativePlus!)
                     */
                    LOGGER.info("Payment call for NativeJsonRequest flow");
                    request.setAttribute(NATIVE_JSON_REQUEST, true);
                    nativeJsonRequest = JsonMapper.mapJsonToObject(requestData, NativeJsonRequest.class);

                    // AOA Subscription support
                    processTransactionUtil.changeMidFromAOAToPGIfApplicableForSubscription(nativeJsonRequest);

                    if (nativeJsonRequest.getHead() != null) {
                        // request Id attribute set for logging purpose
                        // (event
                        // Log)
                        request.setAttribute(Native.REQUEST_ID, nativeJsonRequest.getHead().getRequestId());
                    }
                    if (nativeJsonRequest.getBody() != null
                            && nativeJsonRequest.getBody().getExtendInfo() != null
                            && nativeJsonRequest.getBody().getExtendInfo()
                                    .get(TheiaConstant.RequestParams.Native.PAYERCMID) != null) {
                        request.setAttribute(TheiaConstant.RequestParams.Native.PAYERCMID, nativeJsonRequest.getBody()
                                .getExtendInfo().get(TheiaConstant.RequestParams.Native.PAYERCMID));
                    }
                    // this block handles if we receive qrcodeId instead of mid
                    // in request
                    if (StringUtils.isBlank(request.getParameter(RequestParams.MID)) && nativeJsonRequest != null
                            && nativeJsonRequest.getBody() != null && nativeJsonRequest.getBody().getQrCodeId() != null) {
                        isQRIdFlowOnly = true;
                        processTransactionUtil.setMidUsingQrcodeId(merchantId, nativeJsonRequest);
                        processTransactionUtil.setQrCodeAdditionalInfo(nativeJsonRequest);
                    }
                    processTransactionUtil.checkAndGenerateOrderIdIfNeeded(request, nativeJsonRequest);
                    LOGGER.info("NativeJsonRequest received: {}", MaskingUtil.maskPayload(nativeJsonRequest));
                    nativeRequestParamMap = mapParamForNativeJsonRequestFlow(nativeJsonRequest);
                    processTransactionUtil.pushNativeJsonRequestEvent(nativeJsonRequest);
                }

                ModifiableHttpServletRequest servletRequestWrapper = new ModifiableHttpServletRequest(request,
                        nativeRequestParamMap);

                additionalParams = processTransactionUtil.getAdditionalParamMap(servletRequestWrapper, response);

                if (redirectToShowPaymentPageAPI(additionalParams)) {
                    theiaResponseGenerator.setDataRedirectToShowPaymentPageAPI(additionalParams, response);
                    return;
                }

                if (processTransactionUtil.isNativeJsonRequest(request)) {
                    String[] isOnTheFlyKYCRequired = additionalParams.get("isOnTheFlyKYCRequired");
                    if (isOnTheFlyKYCRequired != null && Boolean.TRUE.toString().equals(isOnTheFlyKYCRequired[0])) {
                        showNativeKycPage(request, response, additionalParams);
                        return;
                    }
                }

                if (isQRIdFlowOnly) {
                    additionalParams.put("isQRIdFlowOnly", new String[] { Boolean.TRUE.toString() });
                }
            } else {
                if (dccEnabledOnMerchantAndCardPayMode(merchantId, request)) {
                    nativeRequestParamMap = mapParamForNativeDccRequestFlow(request);
                }
                additionalParams = processTransactionUtil.getAdditionalParamMap(request, response);
                processTransactionUtil.pushNativePaymentEvent(request.getParameter(MID),
                        request.getParameter(ORDER_ID), "Payment call for Native flow");
                String[] isOnTheFlyKYCRequired = additionalParams.get("isOnTheFlyKYCRequired");
                if (isOnTheFlyKYCRequired != null && Boolean.TRUE.toString().equals(isOnTheFlyKYCRequired[0])) {
                    handleOntheflyKycRequired(additionalParams, request, response);
                    return;
                }
                if (redirectToShowPaymentPageAPI(additionalParams)) {
                    theiaResponseGenerator.setDataRedirectToShowPaymentPageAPI(additionalParams, response);
                    return;
                }
            }

            mServletRequest = new ModifiableHttpServletRequest(request, additionalParams);

            if (dccEnabledOnMerchantAndCardPayMode(merchantId, mServletRequest)) {
                String txnToken = mServletRequest.getParameter(Native.TXN_TOKEN);
                if (StringUtils.isNotBlank(txnToken))
                    nativeSessionUtil.setRequestParamMapForDcc(txnToken, nativeRequestParamMap);
            }

            checkPaymentCountBreached(request);
            checkIfMerchantBlocked(request);
            processPaymentRequest(mServletRequest, response, model, false, null);
            if (request.getAttribute(TheiaConstant.GvConsent.ADD_MONEY_TO_GV_CONSENT_KEY) != null) {
                additionalParams.put(TheiaConstant.GvConsent.GV_CONSENT_FLOW, new String[] { Boolean.TRUE.toString() });
                additionalParams.remove(NATIVE_JSON_REQUEST);
                nativeSessionUtil.setKey(
                        (String) request.getAttribute(TheiaConstant.GvConsent.ADD_MONEY_TO_GV_CONSENT_KEY),
                        additionalParams, 900);
                request.removeAttribute(TheiaConstant.GvConsent.ADD_MONEY_TO_GV_CONSENT_KEY);
            }
            // response.setHeader("Access-Control-Allow-Origin", "*");

            if (Boolean.valueOf((String) request.getAttribute(RiskConstants.IS_RISK_VERIFICATION_REQUIRED))) {
                processTransactionUtil.setAdditionalParamsInSession(request, additionalParams);
                request.removeAttribute(RiskConstants.IS_RISK_VERIFICATION_REQUIRED);
                request.removeAttribute(RiskConstants.TRANS_ID);
            }
            return;

        } catch (final DuplicatePaymentRequestException dpe) {
            LOGGER.error("DuplicatePaymentRequestException : ", ExceptionLogUtils.limitLengthOfStackTrace(dpe));
            failureLogUtil.setFailureMsgForDwhPush(dpe.getResultCode() != null ? dpe.getResultCode().getResultCodeId()
                    : null, dpe.getMessage(), null, true);
            if (processTransactionUtil.isNativeJsonRequest(request)) {
                throw new NativeFlowException.ExceptionBuilder(ResultCode.DUPLICATE_PAYMENT_REQUEST_EXCEPTION)
                        .isHTMLResponse(false).isNativeJsonRequest(true).build();
            }
            throw dpe;
        } catch (final PassCodeValidationException e) {
            LOGGER.error("PassCodeValidationException : ", ExceptionLogUtils.limitLengthOfStackTrace(e));
            failureLogUtil.setFailureMsgForDwhPush(e.getResultCode() != null ? e.getResultCode().getResultCodeId()
                    : null, e.getMessage(), null, true);
            if (processTransactionUtil.isNativeJsonRequest(request)) {
                throw new NativeFlowException.ExceptionBuilder(e.getResultInfo()).isHTMLResponse(false)
                        .isNativeJsonRequest(true).build();
            }
            throw PassCodeValidationException.getException(e.getResultInfo());
        } catch (final NativeFlowException nfe) {
            LOGGER.error("NativeFlowException in /api/v1/processTransaction : ",
                    ExceptionLogUtils.limitLengthOfStackTrace(nfe));
            failureLogUtil.setFailureMsgForDwhPush(nfe.getResultCode() != null ? nfe.getResultCode().getResultCodeId()
                    : null, nfe.getMessage(), null, true);
            throw nfe;
        } catch (final AccountNumberMismatchException | AccountNumberNotExistException e) {
            failureLogUtil.setFailureMsgForDwhPush(null, e.getMessage(), null, true);
            throw e;
        } catch (PaymentRequestValidationException e) {
            LOGGER.error(" request is invalid in /v1/processTransaction: ", e);
            failureLogUtil.setFailureMsgForDwhPush(e.getResultCode() != null ? e.getResultCode().getResultCodeId()
                    : null, e.getMessage(), null, true);
            if (processTransactionUtil.isDccPaymentRequest(request)) {
                throw new NativeFlowException.ExceptionBuilder(e.getResponseConstants()).isHTMLResponse(false)
                        .isNativeJsonRequest(true).build();
            }
            if (processTransactionUtil.isEnhancedNativeFlow(request)) {
                throw MerchantRedirectRequestException.getException();
            }
            if (processTransactionUtil.isNativeJsonRequest(request)) {
                throw new NativeFlowException.ExceptionBuilder(e.getResponseConstants()).isHTMLResponse(false)
                        .isNativeJsonRequest(true).build();
            }
        } catch (final MandateException me) {
            LOGGER.error("MandateException in /api/v1/processTransaction : ", me);
            failureLogUtil.setFailureMsgForDwhPush(me.getResultCode() != null ? me.getResultCode().getResultCodeId()
                    : null, me.getMessage(), null, true);
            throw me;
        } catch (final RiskRejectException rje) {
            failureLogUtil.setFailureMsgForDwhPush(rje.getResultCode() != null ? rje.getResultCode().getResultCodeId()
                    : null, rje.getMessage(), null, true);
            boolean isNativeJsonRequest = processTransactionUtil.isNativeJsonRequest(request);
            boolean isEnhanceNativeRequest = processTransactionUtil.isEnhancedNativeFlow(request);
            boolean isHTMLResponse = isNativeJsonRequest || isEnhanceNativeRequest;
            boolean isRedirectEnhanceFlow = (!rje.isRetryAllowed() && isEnhanceNativeRequest);
            throw new NativeFlowException.ExceptionBuilder(rje.getResponseConstant())
                    .setMsg(rje.getCustomCallbackMsg()).setCustomCallbackMsg(rje.getCustomCallbackMsg())
                    .isHTMLResponse(!isHTMLResponse).isNativeJsonRequest(isNativeJsonRequest)
                    .isRetryAllowed(rje.isRetryAllowed()).isRedirectEnhanceFlow(isRedirectEnhanceFlow).build();
        } catch (final EdcMerchantLimitBreachException | AddMoneyLimitBreachException e) {
            failureLogUtil.setFailureMsgForDwhPush(e.getResultCode() != null ? e.getResultCode().getResultCodeId()
                    : null, e.getMessage(), null, true);
            throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).setMsg(e.getMessage())
                    .setCustomCallbackMsg(e.getMessage()).isHTMLResponse(false).isNativeJsonRequest(true).build();
        } catch (PaymentRequestQrException e) {
            failureLogUtil.setFailureMsgForDwhPush(e.getResultCode() != null ? e.getResultCode().getResultCodeId()
                    : null, e.getMessage(), null, true);
            if (processTransactionUtil.isDccPaymentRequest(request)) {
                throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isHTMLResponse(false)
                        .isNativeJsonRequest(true).build();
            }

            if (processTransactionUtil.isEnhancedNativeFlow(request)) {
                throw MerchantRedirectRequestException.getException();
            }

            if (processTransactionUtil.isNativeJsonRequest(request)) {
                throw new NativeFlowException.ExceptionBuilder(e.getResultCode()).isHTMLResponse(false)
                        .isNativeJsonRequest(true).build();
            }

        } catch (EdcLinkBankAndBrandEmiCheckoutException e) {
            NativeFlowException nativeFlowException;
            failureLogUtil.setFailureMsgForDwhPush(e.getResultCode() != null ? e.getResultCode().getCode() : null,
                    e.getMessage(), null, true);
            if (processTransactionUtil.isNativeJsonRequest(request)) {
                nativeFlowException = new NativeFlowException.ExceptionBuilder(ResultCode.FAILED)
                        .setMsg(e.getMessage()).isHTMLResponse(false).isNativeJsonRequest(true).build();
            } else {
                nativeFlowException = new NativeFlowException.ExceptionBuilder(ResultCode.FAILED)
                        .setMsg(e.getMessage()).build();
            }
            if (BizConstant.EdcLinkEmiTxn.EMI_RESPONSE_MISMATCH_MESSAGE.equals(e.getMessage())) {
                nativeFlowException.setCustomCallbackMsg(e.getMessage());
            }
            throw nativeFlowException;
        } catch (final Exception e) {
            LOGGER.error("SYSTEM_ERROR : {}", ExceptionLogUtils.limitLengthOfStackTrace(e));
            failureLogUtil.setFailureMsgForDwhPush(null, e.getMessage(), null, true);

            if (processTransactionUtil.isDccPaymentRequest(request)) {
                throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isHTMLResponse(false)
                        .isNativeJsonRequest(true).build();
            }

            if (processTransactionUtil.isEnhancedNativeFlow(request)) {
                throw MerchantRedirectRequestException.getException();
            }
            if (processTransactionUtil.isNativeJsonRequest(request)) {
                throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isHTMLResponse(false)
                        .isNativeJsonRequest(true).build();
            }

        } finally {
            // setting redis key to allow payment retries, if allowed.
            if (Boolean.parseBoolean(ConfigurationUtil.getProperty(
                    TheiaConstant.ExtraConstants.DUPLICATE_PAYMENT_REQUEST_CHECK, "true")))
                processTransactionUtil.removeRedisKeyToAllowPayment(request);

            if (processTransactionUtil.isEnhancedNativeFlow(request)) {
                LOGGER.info("Total time taken for Enhanced ProcessTransactionController is {} ms",
                        System.currentTimeMillis() - startTime);
                nativePaymentUtil.logNativeResponse(startTime);
            } else if (processTransactionUtil.isNativeJsonRequest(request)) {
                long totalTime = (System.currentTimeMillis() - startTime);
                LOGGER.info(
                        "Total time taken for NativeJsonRequest ProcessTransactionController is {} ms for paymentMode={}",
                        totalTime, getPaymentMode(nativeJsonRequest));
                processTransactionUtil.pushNativeJsonRequestEvent(totalTime, nativeJsonRequest);
            } else {
                LOGGER.info("Total time taken for  native ProcessTransactionController is {} ms",
                        System.currentTimeMillis() - startTime);
                nativePaymentUtil.logNativeResponse(startTime);
            }
            logDwhFailureMsgToKafka(request, mServletRequest, nativeRequestParamMap);
        }
        request.getRequestDispatcher(VIEW_BASE + theiaViewResolverService.returnOOPSPage(request) + ".jsp").forward(
                request, response);
        return;
    }

    private boolean redirectToShowPaymentPageAPI(Map<String, String[]> additionalParams) {
        return (additionalParams != null && additionalParams.get("redirectShowPaymentPage") != null
                && additionalParams.get(REQUEST_TYPE) != null && (additionalParams.get(REQUEST_TYPE)[0]
                .equals(TheiaConstant.RequestTypes.NATIVE) || additionalParams.get(REQUEST_TYPE)[0]
                .equals(RequestTypes.NATIVE_SUBSCRIPTION)));
    }

    private void resumeGvConsentFlowRequest(HttpServletRequest request, HttpServletResponse response, Model model)
            throws IOException, ServletException {
        LOGGER.info("Resume pay request for GV-consent flow");
        String token = request.getParameter(TheiaConstant.GvConsent.TOKEN);
        Map<String, String[]> additionalParams = (Map<String, String[]>) nativeSessionUtil.getKey(token);
        if (additionalParams == null) {
            failureLogUtil.setFailureMsgForDwhPush(null,
                    BizConstant.FailureLogs.NO_DATA_FOUND_IN_CACHE_FOR_RESUMING_GV_CONSENT, null, true);
            throw new TheiaServiceException(BizConstant.FailureLogs.NO_DATA_FOUND_IN_CACHE_FOR_RESUMING_GV_CONSENT);
        }
        ModifiableHttpServletRequest mServletRequest = new ModifiableHttpServletRequest(request, additionalParams);
        processPaymentRequest(mServletRequest, response, model, false, null);
        return;
    }

    private boolean isBlockedMerchant(HttpServletRequest request) {
        if (StringUtils.isNotBlank(request.getParameter(RequestParams.MID))
                || StringUtils.isNotBlank(request.getParameter(NATIVE_MID))) {
            String mid = StringUtils.isNotBlank(request.getParameter(RequestParams.MID)) ? request
                    .getParameter(RequestParams.MID) : request.getParameter(NATIVE_MID);
            return merchantExtendInfoUtils.isMerchantActiveOrBlocked(mid);
        }
        return false;
    }

    private String getPaymentMode(NativeJsonRequest nativeJsonRequest) {
        if (nativeJsonRequest != null && nativeJsonRequest.getBody() != null) {
            return nativeJsonRequest.getBody().getPaymentMode();
        }
        return "";
    }

    private void showNativeKycPage(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
            Map<String, String[]> additionalParams) throws Exception {
        NativeJsonResponse nativeJsonResponse = theiaResponseGenerator.returnNativeKycJsonPage(httpServletRequest,
                httpServletResponse, additionalParams);

        String nativeKycJsonPage = JsonMapper.mapObjectToJson(nativeJsonResponse);
        LOGGER.info("nativeKycJsonPage {}", nativeKycJsonPage);

        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.getWriter().write(nativeKycJsonPage);
    }

    /*
     * this API is for AppInvoke flow, it renders EnhancedCashierPage
     */
    @RequestMapping(value = { "/api/v1/showPaymentPage", "/api/v2/showPaymentPage", "/api/v3/showPaymentPage" }, method = {
            RequestMethod.GET, RequestMethod.POST })
    public void showPaymentPageNativeAppInvoke(HttpServletRequest request, final HttpServletResponse response,
            final Model model, final Locale locale) throws Exception {

        final long startTime = System.currentTimeMillis();
        String appInvokeUrl = request.getRequestURI();
        LOGGER.info("NativeAppInvoke Flow START | <{}>", appInvokeUrl);

        try {

            if (!isValidNativeAppInvokeRequest(request)) {
                request.getRequestDispatcher(VIEW_BASE + theiaViewResolverService.returnOOPSPage(request) + ".jsp")
                        .forward(request, response);
                return;
            }

            processPaymentRequest(request, response, model, true, null);

        } catch (NativeFlowException nfe) {
            LOGGER.error("NativeFlowException in {} : {}", appInvokeUrl, nfe);
            throw nfe;
        } catch (PaymentRequestValidationException e) {
            LOGGER.error("invalid request in {} : {} ", appInvokeUrl, e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Exception in {} : {} ", appInvokeUrl, e);
            throw e;
        } finally {
            LOGGER.info("NativeAppInvoke Flow END | <{}> | Total time taken by is {} ms", appInvokeUrl,
                    System.currentTimeMillis() - startTime);
        }
    }

    /*
     * this API is for link based payment of mutual fund and stock trade
     */
    @RequestMapping(value = { "/api/v1/showLinkPaymentPage" }, method = { RequestMethod.GET, RequestMethod.POST })
    public void showLinkPaymentPage(HttpServletRequest request, final HttpServletResponse response, final Model model,
            final Locale locale) throws Exception {

        final long startTime = System.currentTimeMillis();
        String linkPaymentUrl = request.getRequestURI();
        LOGGER.info("Link  Flow START | <{}>", linkPaymentUrl);

        try {

            if (!isValidLinkPaymentPageRequest(request)) {
                request.getRequestDispatcher(VIEW_BASE + theiaViewResolverService.returnOOPSPage(request) + ".jsp")
                        .forward(request, response);
                return;
            }

            processPaymentRequest(request, response, model, true, null);

        } catch (NativeFlowException nfe) {
            LOGGER.error("NativeFlowException in {} : {}", linkPaymentUrl, nfe);
            throw nfe;
        } catch (PaymentRequestValidationException e) {
            LOGGER.error("invalid request in {} : {} ", linkPaymentUrl, e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Exception in {} : {} ", linkPaymentUrl, e);
            throw e;
        } finally {
            LOGGER.info("Link payment of MF,ST  Flow END | <{}> | Total time taken by is {} ms", linkPaymentUrl,
                    System.currentTimeMillis() - startTime);
        }
    }

    @RequestMapping(value = "/api/v1/showConsentPage", method = { RequestMethod.POST })
    public void showAddMoneyToGvConsentPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOGGER.info("Request: show add to gv consent page.");
        try {
            String mid = request.getParameter(NATIVE_MID);
            String orderId = request.getParameter(NATIVE_ORDER_ID);
            String callbackUrl = com.paytm.pgplus.common.config.ConfigurationUtil
                    .getProperty(TheiaConstant.ExtraConstants.THEIA_BASE_URL) + TheiaConstant.GvConsent.PTC_V1_URL;
            String cancelUrl = com.paytm.pgplus.common.config.ConfigurationUtil
                    .getProperty(TheiaConstant.ExtraConstants.THEIA_BASE_URL) + TheiaConstant.GvConsent.CANCEL_TXN_URL;
            String token = request.getParameter(TheiaConstant.GvConsent.TOKEN);
            if (ff4JUtils.isFeatureEnabled(THEIA_VALIDATE_MID_ENABLE, false) && !validationService.validateMid(mid)) {
                throw new TheiaServiceException("CRITICAL_ERROR : Invalid NATIVE_MID : " + mid);
            }
            if (ff4JUtils.isFeatureEnabled(THEIA_VALIDATE_ORDER_ID_ENABLE, false)
                    && !validationService.validateOrderId(orderId)) {
                throw new TheiaServiceException("CRITICAL_ERROR : Invalid NATIVE_ORDER_ID : " + orderId);
            }
            if (ff4JUtils.isFeatureEnabled(THEIA_VALIDATE_TOKEN_ENABLE, false)
                    && !validationService.validateToken(token)) {
                throw new TheiaServiceException("CRITICAL_ERROR : Invalid token : " + token);
            }
            GvConsentPagePayload payload = new GvConsentPagePayload(mid, orderId, callbackUrl, cancelUrl, token, false,
                    TheiaConstant.GvConsent.GV_CONSENT_FLOW);
            String jsonPayload = JsonMapper.mapObjectToJson(payload);

            String htmlPage = null;

            // if mid is configured

            UIMicroserviceRequest uiMicroserviceRequest = new UIMicroserviceRequest(jsonPayload,
                    UIMicroserviceUrl.GV_CONSENT_URL);
            UIMicroserviceResponse uiMicroserviceResponse = uiMicroserviceHelper.getHtmlPageFromUI(
                    uiMicroserviceRequest, FEATURE_UI_MICROSERVICE_GVCONSENT, mid);
            htmlPage = uiMicroserviceResponse.getHtmlPage();

            if (StringUtils.isBlank(htmlPage)) {
                htmlPage = com.paytm.pgplus.common.config.ConfigurationUtil.getGvConsentPage();
                htmlPage = htmlPage.replace(TheiaConstant.GvConsent.PUSH_APP_DATA, jsonPayload);
            }
            response.getOutputStream().write(htmlPage.getBytes(StandardCharsets.UTF_8));
            return;
        } catch (FacadeCheckedException e) {
            LOGGER.error("Failed to map object to json in native plus gvConsent!");
            throw e;
        } catch (Exception e) {
            LOGGER.info("Exception in {} : {} ", TheiaConstant.GvConsent.SHOW_ADD_MONEY_TO_GV_CONSENT_PAGE_URL, e);
            throw e;
        }

    }

    private void handleOntheflyKycRequired(Map<String, String[]> additionalParams, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute(KYC_TXN_ID, request.getParameter(Native.TXN_TOKEN));
        request.setAttribute(KYC_FLOW, "YES");
        request.setAttribute(KYC_MID, request.getAttribute(MID));
        request.setAttribute(KYC_ORDER_ID, request.getAttribute(ORDER_ID));
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("mid=").append(request.getParameter(MID));
        queryBuilder.append("&orderId=").append(request.getParameter(ORDER_ID));

        request.setAttribute("queryStringForSession", queryBuilder.toString());
        String kycKey = "Native_KYC_" + request.getParameter(Native.TXN_TOKEN);
        nativeSessionUtil.setKey(kycKey, additionalParams, 900);
        loadNativeKycPage(request, response);
        return;
    }

    private void loadNativeKycPage(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        LOGGER.info("Loading kyc page for native request");
        request.getRequestDispatcher(VIEW_BASE + NATIVE_KYC_JSP + ".jsp").forward(request, response);
    }

    private boolean isValidNativeAppInvokeRequest(HttpServletRequest request) {

        String mid = request.getParameter(Native.MID);
        String orderId = request.getParameter(Native.ORDER_ID);
        String txnToken = request.getParameter(Native.TXN_TOKEN);

        if (!StringUtils.equals(NATIVE_APP_INVOKE_URL, request.getRequestURI())
                && !StringUtils.equals(NATIVE_APP_INVOKE_URL_V2, request.getRequestURI())
                && !StringUtils.equals(NATIVE_APP_INVOKE_URL_V3, request.getRequestURI())) {
            return false;
        }

        if (StringUtils.isBlank(txnToken) || StringUtils.isBlank(mid) || StringUtils.isBlank(orderId)) {
            /*
             * mid, oid and txnToken are mandatory parameters!
             */
            LOGGER.error("Mandatory Parameters Missing. <mid: {}> <orderId: {}> <txnToken: {}>. Returning oops page.",
                    mid, orderId, txnToken);
            return false;
        }
        return true;
    }

    private boolean isValidLinkPaymentPageRequest(HttpServletRequest request) {

        String mid = request.getParameter(Native.MID);
        String orderId = request.getParameter(Native.ORDER_ID);
        String txnToken = request.getParameter(Native.TXN_TOKEN);

        if (!StringUtils.equals(NATIVE_SHOW_LINK_PAYMENT_PAGE, request.getRequestURI())) {
            return false;
        }

        if (StringUtils.isBlank(txnToken) || StringUtils.isBlank(mid) || StringUtils.isBlank(orderId)) {
            /*
             * mid, oid and txnToken are mandatory parameters!
             */
            LOGGER.error("Mandatory Parameters Missing. <mid: {}> <orderId: {}> <txnToken: {}>. Returning oops page.",
                    mid, orderId, txnToken);
            return false;
        }
        return true;
    }

    private Map<String, String[]> mapJsonRequestParametersForEnhancedFlow(NativePaymentRequest nativePaymentRequest)
            throws FacadeCheckedException {
        Map<String, String[]> paramMap = new HashMap<>();
        paramMap.put(Native.MID, new String[] { nativePaymentRequest.getBody().getMid() });
        paramMap.put(Native.ORDER_ID, new String[] { nativePaymentRequest.getBody().getOrderId() });
        paramMap.put(Native.TXN_TOKEN, new String[] { nativePaymentRequest.getHead().getTxnToken() });
        paramMap.put(Native.PAYMENT_MODE, new String[] { nativePaymentRequest.getBody().getPaymentMode() });
        paramMap.put(Native.CARD_INFO, new String[] { nativePaymentRequest.getBody().getCardInfo() });
        paramMap.put(Native.AUTH_MODE, new String[] { nativePaymentRequest.getBody().getAuthMode() });
        paramMap.put(Native.PAYMENT_FLOW, new String[] { nativePaymentRequest.getBody().getPaymentFlow() });
        paramMap.put(Native.STORE_INSTRUMENT, new String[] { nativePaymentRequest.getBody().getStoreInstrument() });
        paramMap.put(Native.CHANNEL_ID, new String[] { nativePaymentRequest.getHead().getChannelId().name() });
        paramMap.put(Native.CHANNEL_CODE, new String[] { nativePaymentRequest.getBody().getChannelCode() });
        paramMap.put(Native.PLAN_ID, new String[] { nativePaymentRequest.getBody().getPlanId() });
        paramMap.put(Native.PAYER_ACCOUNT, new String[] { nativePaymentRequest.getBody().getPayerAccount() });
        paramMap.put(Native.MPIN, new String[] { nativePaymentRequest.getBody().getMpin() });
        paramMap.put(Native.ACCOUNT_NUMBER, new String[] { nativePaymentRequest.getBody().getAccountNumber() });
        paramMap.put(Native.BANK_NAME, new String[] { nativePaymentRequest.getBody().getBankName() });
        paramMap.put(Native.CREDIT_BLOCK, new String[] { nativePaymentRequest.getBody().getCreditBlock() });
        paramMap.put(Native.SEQ_NUMBER, new String[] { nativePaymentRequest.getBody().getSeqNumber() });
        paramMap.put(Native.DEVICE_ID, new String[] { nativePaymentRequest.getBody().getDeviceId() });
        paramMap.put(Native.EMI_TYPE, new String[] { nativePaymentRequest.getBody().getEmiType() });
        paramMap.put(Native.APP_ID, new String[] { nativePaymentRequest.getBody().getAppId() });
        paramMap.put(Native.TXN_NOTE, new String[] { nativePaymentRequest.getBody().getTxnNote() });
        paramMap.put(Native.REF_URL, new String[] { nativePaymentRequest.getBody().getRefUrl() });
        paramMap.put(SUBSCRIPTION_ID, new String[] { nativePaymentRequest.getBody().getSubscriptionId() });
        paramMap.put(UPI_ACC_REF_ID, new String[] { nativePaymentRequest.getBody().getUpiAccRefId() });
        paramMap.put(Native.ORIGIN_CHANNEL, new String[] { nativePaymentRequest.getBody().getOriginChannel() });
        // MGV template id
        paramMap.put(TEMPLATE_ID, new String[] { nativePaymentRequest.getBody().getTemplateId() });
        Map<String, String> extendInfo = nativePaymentRequest.getBody().getExtendInfo();
        if (extendInfo != null) {
            paramMap.put(Native.EXTEND_INFO, new String[] { extendInfo.toString() });
            if (extendInfo.get(USER_INVESTMENT_CONSENT_FLAG) != null
                    && Boolean.valueOf(extendInfo.get(USER_INVESTMENT_CONSENT_FLAG))) {
                paramMap.put(USER_INVESTMENT_CONSENT_FLAG,
                        new String[] { extendInfo.get(USER_INVESTMENT_CONSENT_FLAG) });
            }
        }
        // for Bank mandates
        paramMap.put(Native.BANK_IFSC, new String[] { nativePaymentRequest.getBody().getBankIfsc() });
        paramMap.put(Native.USER_NAME, new String[] { nativePaymentRequest.getBody().getUserName() });
        paramMap.put(Native.ACCOUNT_TYPE, new String[] { nativePaymentRequest.getBody().getAccountType() });
        paramMap.put(Native.MANDATE_AUTH_MODE, new String[] { nativePaymentRequest.getBody().getMandateAuthMode() });

        // DataEnrichment fields
        paramMap.put(Native.RISK_EXTENDED_INFO, new String[] { nativePaymentRequest.getBody().getRiskExtendInfo() });
        if (nativePaymentRequest.getHead() != null && nativePaymentRequest.getHead().getTokenType() != null) {
            paramMap.put(Native.TOKEN_TYPE, new String[] { nativePaymentRequest.getHead().getTokenType().getType() });
        }

        // EMI Subvention All in one SDK Flow
        EmiSubventionInfo emiSubventionInfo = nativePaymentRequest.getBody().getEmiSubventionInfo();
        if (emiSubventionInfo != null) {
            String jsonEmiSubventionInfo = JsonMapper.mapObjectToJson(emiSubventionInfo);
            paramMap.put(Native.EMI_SUBVENTION_INFO, new String[] { jsonEmiSubventionInfo });
        }
        if (nativePaymentRequest.getBody().getConvertToAddAndPayTxn() != null) {
            paramMap.put(Native.CONVERT_TO_ADDANDPAY_TXN,
                    new String[] { String.valueOf(nativePaymentRequest.getBody().getConvertToAddAndPayTxn()) });
        }
        CoftConsent coftConsentInfo = nativePaymentRequest.getBody().getCoftConsent();
        if (coftConsentInfo != null) {
            String jsonCoftConsentInfo = JsonMapper.mapObjectToJson(coftConsentInfo);
            paramMap.put(COFT_CONSENT, new String[] { jsonCoftConsentInfo });
        }

        CardTokenInfo cardtokeninfo = nativePaymentRequest.getBody().getCardTokenInfo();
        if (cardtokeninfo != null) {
            String jsonCardtokeninfo = JsonMapper.mapObjectToJson(cardtokeninfo);
            paramMap.put(CARDTOKEN_INFO, new String[] { jsonCardtokeninfo });
        }

        if (nativePaymentRequest.getBody().getAddOneRupee() != null) {
            paramMap.put(Native.ADD_ONE_RUPEE,
                    new String[] { String.valueOf(nativePaymentRequest.getBody().getAddOneRupee()) });
        }

        paramMap.put(VARIABLE_LENGTH_OTP_SUPPORTED,
                new String[] { String.valueOf(nativePaymentRequest.getBody().isVariableLengthOtpSupported()) });
        if (nativePaymentRequest.getBody().getTwoFAConfig() != null) {
            try {
                String twoFAConfigJson = JsonMapper.mapObjectToJson(nativePaymentRequest.getBody().getTwoFAConfig());
                paramMap.put(TWO_FA_CONFIG, new String[] { twoFAConfigJson });
            } catch (Exception e) {
                LOGGER.error("Exception occurred while converting twoFAConfig Object to Json : {}", e.getMessage());
            }
        }
        return paramMap;
    }

    private Map<String, String[]> mapParamForNativeDccRequestFlow(HttpServletRequest request) {
        Map<String, String[]> paramMap = new HashMap<>();
        paramMap.put(Native.MID, new String[] { request.getParameter(Native.MID) });
        paramMap.put(Native.ORDER_ID, new String[] { request.getParameter(Native.ORDER_ID) });
        paramMap.put(Native.PAYMENT_MODE, new String[] { request.getParameter(Native.PAYMENT_MODE) });

        paramMap.put(Native.REQUEST_ID, new String[] { request.getParameter(Native.REQUEST_ID) });
        paramMap.put(Native.TXN_TOKEN, new String[] { request.getParameter(Native.TXN_TOKEN) });
        paramMap.put(SSO_TOKEN, new String[] { request.getParameter(SSO_TOKEN) });
        paramMap.put(WEBSITE, new String[] { request.getParameter(WEBSITE) });
        paramMap.put(Native.TOKEN_TYPE, new String[] { request.getParameter(Native.TOKEN_TYPE) });
        paramMap.put(CALLBACK_URL, new String[] { request.getParameter(CALLBACK_URL) });

        paramMap.put(Native.STORE_INSTRUMENT,
                new String[] { request.getParameter(TheiaConstant.RequestParams.Native.STORE_INSTRUMENT) });

        paramMap.put(Native.WORKFLOW, new String[] { request.getParameter(Native.WORKFLOW) });
        paramMap.put(Native.GUEST_TOKEN, new String[] { request.getParameter(Native.GUEST_TOKEN) });
        paramMap.put(Native.TOKEN, new String[] { request.getParameter(Native.TOKEN) });

        paramMap.put(CUSTOMER_ID, new String[] { request.getParameter(CUSTOMER_ID) });
        paramMap.put(TheiaConstant.RequestParams.Native.AGG_MID,
                new String[] { request.getParameter(TheiaConstant.RequestParams.Native.AGG_MID) });

        paramMap.put(Native.CHANNEL_ID, new String[] { request.getParameter(Native.CHANNEL_ID) });
        paramMap.put(Native.CHANNEL_CODE, new String[] { request.getParameter(Native.CHANNEL_CODE) });
        paramMap.put("cardHash", new String[] { request.getParameter("cardHash") });
        paramMap.put(Native.CARD_INFO, new String[] { request.getParameter(Native.CARD_INFO) });
        paramMap.put(Native.ENCRYPTED_CARD_INFO, new String[] { request.getParameter(Native.ENCRYPTED_CARD_INFO) });
        request.getParameter(IDEBIT_OPTION);
        paramMap.put(Native.AUTH_MODE,
                new String[] { request.getParameter(TheiaConstant.RequestParams.Native.AUTH_MODE) });
        paramMap.put(Native.PAYMENT_FLOW,
                new String[] { request.getParameter(TheiaConstant.RequestParams.Native.PAYMENT_FLOW) });
        paramMap.put(TheiaConstant.RequestParams.REQUEST_TYPE,
                new String[] { request.getParameter(TheiaConstant.RequestParams.REQUEST_TYPE) });

        return paramMap;
    }

    private Map<String, String[]> mapParamForNativeJsonRequestFlow(NativeJsonRequest nativeJsonRequest)
            throws FacadeCheckedException {
        Map<String, String[]> paramMap = new HashMap<>();
        paramMap.put(Native.MID, new String[] { nativeJsonRequest.getBody().getMid() });
        paramMap.put(Native.ORDER_ID, new String[] { nativeJsonRequest.getBody().getOrderId() });

        if (TokenType.SSO == nativeJsonRequest.getHead().getTokenType()) {
            nativeJsonRequest.getHead().setTxnToken(
                    nativeJsonRequest.getBody().getMid() + nativeJsonRequest.getBody().getOrderId());
            if (nativeJsonRequest.getBody().getTxnAmount() != null) {
                paramMap.put(TXN_AMOUNT, new String[] { nativeJsonRequest.getBody().getTxnAmount().getValue() });
                paramMap.put(CURRENCY, new String[] { nativeJsonRequest.getBody().getTxnAmount().getCurrency()
                        .getCurrency() });
            }
            if (nativeJsonRequest.getBody().getTipAmount() != null) {
                paramMap.put(TIP_AMOUNT, new String[] { nativeJsonRequest.getBody().getTipAmount().getValue() });
            }
            paramMap.put(TheiaConstant.RequestParams.SSO_TOKEN, new String[] { nativeJsonRequest.getHead().getToken() });
            paramMap.put(TheiaConstant.RequestParams.WEBSITE, new String[] { nativeJsonRequest.getBody().getWebsite() });
            paramMap.put(Native.TOKEN_TYPE, new String[] { nativeJsonRequest.getHead().getTokenType().getType() });
            paramMap.put(CUSTOMER_ID, new String[] { nativeJsonRequest.getBody().getCustId() });
        }
        if (StringUtils.isNotBlank(nativeJsonRequest.getBody().getCallbackUrl())) {
            paramMap.put(TheiaConstant.RequestParams.CALLBACK_URL, new String[] { nativeJsonRequest.getBody()
                    .getCallbackUrl() });
        }

        paramMap.put(Native.TXN_TOKEN, new String[] { nativeJsonRequest.getHead().getTxnToken() });

        paramMap.put(Native.PAYMENT_MODE, new String[] { nativeJsonRequest.getBody().getPaymentMode() });

        paramMap.put(Native.AUTH_MODE, new String[] { nativeJsonRequest.getBody().getAuthMode() });

        paramMap.put(Native.CARD_INFO, new String[] { nativeJsonRequest.getBody().getCardInfo() });
        paramMap.put(Native.STORE_INSTRUMENT, new String[] { nativeJsonRequest.getBody().getStoreInstrument() });

        UpiLiteRequestData upiLiteRequestData = nativeJsonRequest.getBody().getUpiLiteRequestData();
        if (upiLiteRequestData != null) {
            String jsonUpiRequestData = JsonMapper.mapObjectToJson(upiLiteRequestData);
            paramMap.put(UPILITE_REQUEST_DATA, new String[] { jsonUpiRequestData });
        }

        OneClickInfo oneClickInfo = nativeJsonRequest.getBody().getOneClickInfo();
        if (oneClickInfo != null) {
            String jsonOneClickInfo = JsonMapper.mapObjectToJson(oneClickInfo);
            paramMap.put(Native.ONE_CLICK_INFO, new String[] { jsonOneClickInfo });
        }

        EcomTokenInfo ecomTokenInfo = nativeJsonRequest.getBody().getEcomTokenInfo();
        if (ecomTokenInfo != null) {

            String jsonEcomTokenInfo = JsonMapper.mapObjectToJson(ecomTokenInfo);
            paramMap.put(Native.ECOM_TOKEN_INFO, new String[] { jsonEcomTokenInfo });
        }
        CoftConsent coftConsentInfo = nativeJsonRequest.getBody().getCoftConsent();
        if (coftConsentInfo != null) {
            String jsonCoftConsentInfo = JsonMapper.mapObjectToJson(coftConsentInfo);
            paramMap.put(COFT_CONSENT, new String[] { jsonCoftConsentInfo });
        }
        CardTokenInfo cardtokeninfo = nativeJsonRequest.getBody().getCardTokenInfo();
        if (cardtokeninfo != null) {
            String jsonCardtokeninfo = JsonMapper.mapObjectToJson(cardtokeninfo);
            paramMap.put(CARDTOKEN_INFO, new String[] { jsonCardtokeninfo });
        }
        paramMap.put(Native.PAYMENT_FLOW, new String[] { nativeJsonRequest.getBody().getPaymentFlow() });

        paramMap.put(Native.CHANNEL_CODE, new String[] { nativeJsonRequest.getBody().getChannelCode() });
        paramMap.put(Native.PLAN_ID, new String[] { nativeJsonRequest.getBody().getPlanId() });

        paramMap.put(Native.PAYER_ACCOUNT, new String[] { nativeJsonRequest.getBody().getPayerAccount() });
        paramMap.put(Native.MPIN, new String[] { nativeJsonRequest.getBody().getMpin() });
        paramMap.put(Native.ACCOUNT_NUMBER, new String[] { nativeJsonRequest.getBody().getAccountNumber() });
        paramMap.put(Native.BANK_NAME, new String[] { nativeJsonRequest.getBody().getBankName() });
        paramMap.put(Native.CREDIT_BLOCK, new String[] { nativeJsonRequest.getBody().getCreditBlock() });
        paramMap.put(Native.SEQ_NUMBER, new String[] { nativeJsonRequest.getBody().getSeqNumber() });
        paramMap.put(UPI_ACC_REF_ID, new String[] { nativeJsonRequest.getBody().getUpiAccRefId() });

        paramMap.put(Native.ORIGIN_CHANNEL, new String[] { nativeJsonRequest.getBody().getOriginChannel() });
        paramMap.put(Native.DEVICE_ID, new String[] { nativeJsonRequest.getBody().getDeviceId() });
        paramMap.put(Native.EMI_TYPE, new String[] { nativeJsonRequest.getBody().getEmiType() });
        paramMap.put(Native.APP_ID, new String[] { nativeJsonRequest.getBody().getAppId() });
        paramMap.put(Native.AGG_MID, new String[] { nativeJsonRequest.getBody().getAggMid() });
        paramMap.put(Native.TXN_NOTE, new String[] { nativeJsonRequest.getBody().getTxnNote() });
        paramMap.put(Native.REF_URL, new String[] { nativeJsonRequest.getBody().getRefUrl() });
        // MGV template id
        paramMap.put(TEMPLATE_ID, new String[] { nativeJsonRequest.getBody().getTemplateId() });
        Map<String, String> extendInfo = nativeJsonRequest.getBody().getExtendInfo();
        if (extendInfo != null) {
            if (extendInfo.get(USER_INVESTMENT_CONSENT_FLAG) != null) {
                paramMap.put(USER_INVESTMENT_CONSENT_FLAG,
                        new String[] { extendInfo.get(USER_INVESTMENT_CONSENT_FLAG) });
            }
            String jsonExtendInfo = TokenType.SSO == nativeJsonRequest.getHead().getTokenType() ? JsonMapper
                    .mapObjectToJson(extendInfo) : extendInfo.toString();
            paramMap.put(Native.EXTEND_INFO, new String[] { jsonExtendInfo });
            // TODO remove try catch after review
            try {
                String offlineExtendInfo = JsonMapper.mapObjectToJson(extendInfo);
                paramMap.put(Native.OFFLINE_EXTEND_INFO, new String[] { offlineExtendInfo });
            } catch (Exception e) {
                // no need to log
            }
        }
        paramMap.put(SUBSCRIPTION_ID, new String[] { nativeJsonRequest.getBody().getSubscriptionId() });
        String riskExtendInfo = nativeJsonRequest.getBody().getRiskExtendInfo();
        if (StringUtils.isNotBlank(riskExtendInfo)) {
            paramMap.put(Native.RISK_EXTENDED_INFO, new String[] { riskExtendInfo });
        }

        EChannelId channelId = nativeJsonRequest.getHead().getChannelId();
        if (channelId != null) {
            if (EChannelId.APP.getValue().equals(channelId.getValue())) {
                /*
                 * This is done so that native+ plus integration with offline
                 * works well (apparently!)
                 */
                channelId = EChannelId.WAP;
            }
            paramMap.put(TheiaConstant.RequestParams.Native.CHANNEL_ID, new String[] { channelId.getValue() });
        }

        paramMap.put(Native.WORKFLOW, new String[] { nativeJsonRequest.getHead().getWorkFlow() });
        paramMap.put(Native.GUEST_TOKEN, new String[] { nativeJsonRequest.getBody().getGuestToken() });
        paramMap.put(Native.TOKEN, new String[] { nativeJsonRequest.getHead().getToken() });

        paramMap.put(OS_TYPE, new String[] { nativeJsonRequest.getBody().getOsType() });
        paramMap.put(PSP_APP, new String[] { nativeJsonRequest.getBody().getPspApp() });
        paramMap.put(Native.ACCESS_TOKEN, new String[] { nativeJsonRequest.getBody().getAccessToken() });

        // For Bank Mandates
        paramMap.put(Native.BANK_IFSC, new String[] { nativeJsonRequest.getBody().getBankIfsc() });
        paramMap.put(Native.USER_NAME, new String[] { nativeJsonRequest.getBody().getUserName() });
        paramMap.put(Native.ACCOUNT_TYPE, new String[] { nativeJsonRequest.getBody().getAccountType() });
        paramMap.put(Native.MANDATE_AUTH_MODE, new String[] { nativeJsonRequest.getBody().getMandateAuthMode() });
        paramMap.put(Native.MANDATE_TYPE, new String[] { nativeJsonRequest.getBody().getMandateType() });
        paramMap.put(Native.PAYMENT_CALL_DCC, new String[] { nativeJsonRequest.getBody().getPaymentCallFromDccPage() });
        paramMap.put(Native.DCC_SELECTED_BY_USER, new String[] { nativeJsonRequest.getBody().getDccSelectedByUser() });

        // EMI Subvention All in one SDK Flow
        EmiSubventionInfo emiSubventionInfo = nativeJsonRequest.getBody().getEmiSubventionInfo();
        if (emiSubventionInfo != null) {
            String jsonEmiSubventionInfo = JsonMapper.mapObjectToJson(emiSubventionInfo);
            paramMap.put(Native.EMI_SUBVENTION_INFO, new String[] { jsonEmiSubventionInfo });
        }

        paramMap.put(Native.PREFERRED_OTP_PAGE, new String[] { nativeJsonRequest.getBody().getPreferredOtpPage() });

        if (nativeJsonRequest.getBody().getCardPreAuthType() != null) {
            paramMap.put(Native.CARD_PRE_AUTH_TYPE,
                    new String[] { String.valueOf(nativeJsonRequest.getBody().getCardPreAuthType()) });
        }
        if (nativeJsonRequest.getBody().getPreAuthBlockSeconds() != null) {
            paramMap.put(Native.PRE_AUTH_BLOCK_SECONDS,
                    new String[] { String.valueOf(nativeJsonRequest.getBody().getPreAuthBlockSeconds()) });
        }
        if (nativeJsonRequest.getBody().getConvertToAddAndPayTxn() != null) {
            paramMap.put(Native.CONVERT_TO_ADDANDPAY_TXN,
                    new String[] { String.valueOf(nativeJsonRequest.getBody().getConvertToAddAndPayTxn()) });
        }

        if (nativeJsonRequest.getBody().getTwoFAConfig() != null) {
            try {
                String twoFAConfigJson = JsonMapper.mapObjectToJson(nativeJsonRequest.getBody().getTwoFAConfig());
                paramMap.put(TWO_FA_CONFIG, new String[] { twoFAConfigJson });
            } catch (Exception e) {
                LOGGER.error("Exception occurred while converting twoFAConfig Object to Json : {}", e.getMessage());
            }
        }

        if (StringUtils.isNotBlank(nativeJsonRequest.getBody().getMerchantVpa())) {
            paramMap.put(MERCHANT_VPA, new String[] { String.valueOf(nativeJsonRequest.getBody().getMerchantVpa()) });
        }

        if (nativeJsonRequest.getBody().getAddOneRupee() != null) {
            paramMap.put(Native.ADD_ONE_RUPEE,
                    new String[] { String.valueOf(nativeJsonRequest.getBody().getAddOneRupee()) });
        }

        paramMap.put(VARIABLE_LENGTH_OTP_SUPPORTED,
                new String[] { String.valueOf(nativeJsonRequest.getBody().isVariableLengthOtpSupported()) });

        if (StringUtils.isNotBlank(nativeJsonRequest.getBody().getSimSubscriptionId())) {
            paramMap.put(SIM_SUBSCRIPTION_ID, new String[] { nativeJsonRequest.getBody().getSimSubscriptionId() });
        }
        return paramMap;
    }

    private void setMidOrderIdInRequestAttribute(final HttpServletRequest request, String mid, String orderId) {
        request.setAttribute(MID, mid);
        request.setAttribute(ORDER_ID, orderId);
    }

    private String getIndustryTypeId(String mid) {
        String industryTypeId = "NA";
        try {
            MerchantProfile merchantProfileInfo = merchantDataService.getMerchantProfileInfo(mid);
            EXT_LOGGER.customInfo("Mapping response - MerchantProfile :: {}", merchantProfileInfo);
            if (null != merchantProfileInfo && null != merchantProfileInfo.getMccCodes()
                    && !merchantProfileInfo.getMccCodes().isEmpty()) {
                industryTypeId = merchantProfileInfo.getMccCodes().get(0);
            }
        } catch (Exception e) {
            LOGGER.warn("Exception occured on calling merchant profile api");
        }
        return industryTypeId;
    }

    private AccountInfo getAccountInfo(NativeCashierInfoResponse cashierInfoResponse) {
        String paymentMode = EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod();
        PayMethod payMethod = getPayMethod(cashierInfoResponse, paymentMode);
        if (null != payMethod) {
            if (null != payMethod.getPayChannelOptions() && !payMethod.getPayChannelOptions().isEmpty()) {
                AccountInfo accountInfo = ((BalanceChannel) payMethod.getPayChannelOptions().get(0)).getBalanceInfo();
                if (null == accountInfo) {
                    return null;
                } else {
                    if (accountInfo.isPayerAccountExists()) {
                        return accountInfo;
                    } else {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private PayMethod getPayMethod(NativeCashierInfoResponse cashierInfoResponse, String ePayMethod) {
        if (null != cashierInfoResponse && null != cashierInfoResponse.getBody()
                && null != cashierInfoResponse.getBody().getMerchantPayOption()
                && null != cashierInfoResponse.getBody().getMerchantPayOption().getPayMethods()) {
            List<PayMethod> payMethods = cashierInfoResponse.getBody().getMerchantPayOption().getPayMethods();
            for (PayMethod payMethod : payMethods) {
                if (ePayMethod.equals(payMethod.getPayMethod())) {
                    return payMethod;
                }
            }
        }
        return null;
    }

    private void invalidateSession(final HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session != null) {
            LOGGER.info("sessionInvalidating");
            session.invalidate();
        }
    }

    private boolean isNotPageRefresh(final HttpServletRequest request) {
        return theiaSessionDataService.validateSession(request);
    }

    /**
     * @param paymentRequestData
     * @param model
     */
    private PageDetailsResponse processPaymentRequest(final PaymentRequestBean paymentRequestData, final Model model) {
        PageDetailsResponse pageDetailsResponse;
        LOGGER.info("PaymentRequestBean received : {}", paymentRequestData);

        Map<String, Object> context = new HashMap<>();
        context.put("mid", paymentRequestData.getMid());

        if (PaymentTypeIdEnum.UPI_LITE.value.equals(paymentRequestData.getPaymentTypeId())) {
            paymentRequestData.setPaymentTypeId(PaymentTypeIdEnum.UPI.value);
        }

        EventUtils.pushTheiaEvents(EventNameEnum.ORDER_INITIATED, new ImmutablePair<>("REQUEST_TYPE",
                paymentRequestData.getRequestType()));

        // Added this check to confirm if API_Disabled is true for merchant in
        // DEFAULT or NATIVE FLOW.
        // If yes then API based Transaction should fail.
        boolean failureCheck = checkAPIBasedTransactionFailure(paymentRequestData);

        /*
         * Extracheck to avoid redis call for all the transactions and allow
         * payment only for khatabook aggregator
         */
        if (failureCheck && isKhatabookSdMerchant(paymentRequestData)) {
            failureCheck = false;
        }

        if (failureCheck) {
            LOGGER.error("Merchant is not allowed to do API based transaction : {}", paymentRequestData);

            if (paymentRequestData.isNativeJsonRequest()) {
                throw new NativeFlowException.ExceptionBuilder(ResponseConstants.MERCHANT_BLOCKED_PAYMENT)
                        .isHTMLResponse(false).build();
            }

            String htmlPage = merchantResponseService.processMerchantFailResponse(paymentRequestData,
                    ResponseConstants.MERCHANT_BLOCKED_PAYMENT);

            PageDetailsResponse pageResponse = new PageDetailsResponse();
            pageResponse.setSuccessfullyProcessed(false);
            pageResponse.setHtmlPage(htmlPage);
            return pageResponse;
        }

        try {
            if (paymentRequestData.isEnhancedCashierPageRequest()) {
                return validateChecksumForEnhancedCashierPageRequest(paymentRequestData, model);
            }
            switch (paymentRequestData.getRequestType()) {
            case RequestTypes.EMAIL_INVOICE:
            case RequestTypes.SMS_INVOICE:
                pageDetailsResponse = invoiceService.processInvoiceRequest(paymentRequestData, model);
                break;
            case RequestTypes.DEFAULT:
            case RequestTypes.CC_BILL_PAYMENT:
            case RequestTypes.RESELLER:
                pageDetailsResponse = defaultService.processDefaultRequest(paymentRequestData, model);
                break;
            case RequestTypes.ADD_MONEY:
                pageDetailsResponse = addMoneyService.processAddMoneyRequest(paymentRequestData, model);
                break;
            case RequestTypes.SUBSCRIPTION:
                pageDetailsResponse = subscriptionService.processSubscriptionRequest(paymentRequestData, model);
                break;
            case RequestTypes.RENEW_SUBSCRIPTION:
            case RequestTypes.PARTIAL_RENEW_SUBSCRIPTION:
                pageDetailsResponse = subscriptionService.processRenewSubscriptionRequest(paymentRequestData, model);
                break;
            case RequestTypes.NATIVE_SUBSCRIPTION:
            case RequestTypes.NATIVE:
            case RequestTypes.UNI_PAY:
            case RequestTypes.NATIVE_MF:
            case RequestTypes.NATIVE_ST:
            case RequestTypes.NATIVE_MF_SIP:
                pageDetailsResponse = nativeService.processNativeRequest(paymentRequestData, model);
                break;
            case RequestTypes.OFFLINE:
            case RequestTypes.SEAMLESS:
            case RequestTypes.SEAMLESS_NATIVE:
                pageDetailsResponse = seamlessService.processSeamlessNativeRequest(paymentRequestData, model);
                break;
            case RequestTypes.PAYTM_EXPRESS:
                pageDetailsResponse = expressService.processPaytmExpressRequest(paymentRequestData, model);
                break;
            case RequestTypes.TOPUP_EXPRESS:
                pageDetailsResponse = expressService.processTopupExpressRequest(paymentRequestData, model);
                break;
            case RequestTypes.SEAMLESS_ACS:
                pageDetailsResponse = seamlessService.processSeamlessACSRequest(paymentRequestData, model);
                break;
            case RequestTypes.STOCK_TRADE:
                pageDetailsResponse = stockTradingService.processStockTradingRequest(paymentRequestData, model);
                break;
            case RequestTypes.LINK_BASED_PAYMENT:
            case RequestTypes.LINK_BASED_PAYMENT_INVOICE:
                pageDetailsResponse = theiaLinkService.processLinkRequest(paymentRequestData, model);
                break;
            case RequestTypes.ADDMONEY_EXPRESS:
                pageDetailsResponse = expressService.processAddMoneyExpressRequest(paymentRequestData, model);
                break;
            case RequestTypes.MOTO_CHANNEL:
                pageDetailsResponse = motoService.processMotoRequest(paymentRequestData, model);
                break;
            case RequestTypes.DYNAMIC_QR_2FA:
            case RequestTypes.DYNAMIC_QR:
                pageDetailsResponse = qrService.processDynamicQRRequest(paymentRequestData, model);
                break;
            case RequestTypes.SEAMLESS_NB:
                pageDetailsResponse = seamlessNBPaymentService.processSeamlessNBRequest(paymentRequestData, model);
                break;
            case RequestTypes.DEFAULT_MF:
                pageDetailsResponse = defaultMFService.processDefaultMFRequest(paymentRequestData, model);
                break;
            default:
                throw new PaymentRequestValidationException("Invalid Payment Request type : "
                        + paymentRequestData.getRequestType());
            }
        } catch (InvalidRequestParameterException e) {
            throw e;
        } catch (BizMerchantVelocityBreachedException e) {
            throw e;
        } catch (MerchantLimitBreachedException e) {
            throw e;
        } catch (PassCodeValidationException e) {
            throw e;
        } catch (NativeFlowException nfe) {
            throw nfe;
        } catch (AccountMismatchException ame) {
            throw new AccountNumberMismatchException(ame.getMessage(), paymentRequestData);
        } catch (AccountNotExistsException ame) {
            throw new AccountNumberNotExistException(ame.getMessage(), paymentRequestData);
        } catch (MandateException me) {
            throw me;
        } catch (PaymentRequestQrException e) {
            if (e.getResponseConstants() != null && ResultCode.QR_EXPIRED.equals(e.getResultCode())) {
                throw new PaymentRequestQrException(ResultCode.QR_EXPIRED, ResponseConstants.QR_EXPIRED_ERROR);
            } else {
                throw new TheiaControllerException("Exception occurred while processing payment request : ", e);
            }
        } catch (EdcLinkBankAndBrandEmiCheckoutException e) {
            throw e;
        } catch (Exception ex) {
            throw new TheiaControllerException("Exception occurred while processing payment request : ", ex);
        }

        return pageDetailsResponse;
    }

    private void processRequest(final HttpServletRequest request, final HttpServletResponse response,
            PaymentRequestBean paymentRequestData, final Model model) throws IOException, ServletException {

        ThreadLocalUtil.set(new TxnStateLog(StringUtils.EMPTY, paymentRequestData.getMid(), paymentRequestData
                .getOrderId(), paymentRequestData.getTxnAmount()));
        EXT_LOGGER.customInfo("processRequest paymentRequestData : {} ", paymentRequestData);
        boolean isEnhancedCashierFlow = processTransactionControllerHelper.checkIfEnhancedCashierFlow(
                paymentRequestData, request);

        if (StringUtils.isNotBlank(com.paytm.pgplus.common.config.ConfigurationUtil
                .getProperty(BizConstant.MP_ADD_MONEY_MID))
                && !com.paytm.pgplus.common.config.ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID)
                        .equalsIgnoreCase(paymentRequestData.getMid())) {

            if (ERequestType.OFFLINE.getType().equalsIgnoreCase(paymentRequestData.getRequestType())) {
                mapLinkParamsForOfflineFlow(paymentRequestData);
            }
            if (StringUtils.isNotEmpty(paymentRequestData.getLinkId())
                    || StringUtils.isNotEmpty(paymentRequestData.getInvoiceId())) {
                String source = Channel.WEB.getName();
                if (paymentRequestData.getLinkDetailsData() != null) {
                    // This has added for link based payment of mutual fund and
                    // stock trade
                    linkPaymentUtil.addLinkDetailsInPaymentRequestBeanInPayment(paymentRequestData);
                } else if (ERequestType.OFFLINE.getType().equalsIgnoreCase(paymentRequestData.getRequestType())
                        || ERequestType.NATIVE.getType().equalsIgnoreCase(paymentRequestData.getRequestType())
                        || ERequestType.LINK_BASED_PAYMENT.getType().equalsIgnoreCase(
                                paymentRequestData.getRequestType())
                        || ERequestType.LINK_BASED_PAYMENT_INVOICE.getType().equalsIgnoreCase(
                                paymentRequestData.getRequestType())) {

                    if (ERequestType.OFFLINE.getType().equalsIgnoreCase(paymentRequestData.getRequestType())
                            || ERequestType.NATIVE.getType().equalsIgnoreCase(paymentRequestData.getRequestType())) {
                        source = Channel.APP.getName();
                    }
                    linkPaymentUtil.getLinkDetailResponse(paymentRequestData, !isEnhancedCashierFlow);// PGP-41600

                    if (Boolean.TRUE.toString().equals(
                            com.paytm.pgplus.theia.utils.ConfigurationUtil.getProperty(SET_LINK_CALLBACKURL))) {

                        String errorMsg = linkPaymentUtil.validateLinkAndSetCallBackURL(paymentRequestData,
                                !isEnhancedCashierFlow);

                        if (StringUtils.isNotBlank(errorMsg)) {
                            if (paymentRequestData.isNativeJsonRequest()) {
                                ResultCode resultCode = ResultCode.INVALID_LINK_AMT_OR_DESC;
                                if (errorMsg.contains(PAYMENT_PROCESSING)
                                        && ff4JUtil.isFeatureEnabled(LINK_NATIVE_PAYMENT_IN_PROCESS_ERROR,
                                                paymentRequestData.getMid())) {
                                    resultCode = ResultCode.PAYMENT_IN_PROCESS;
                                }

                                if (ResultCode.LINK_PAYMENT_IN_PROCESS.getResultMsg().equals(errorMsg)) {
                                    resultCode = ResultCode.LINK_PAYMENT_IN_PROCESS;
                                    failureLogUtil.setFailureMsgForDwhPush(
                                            ResultCode.LINK_PAYMENT_IN_PROCESS.getResultCodeId(), errorMsg, null, true);
                                } else if (ResultCode.LINK_PAYMENT_ALREADY_PROCESSED.getResultMsg().equals(errorMsg)) {
                                    resultCode = ResultCode.LINK_PAYMENT_ALREADY_PROCESSED;
                                    failureLogUtil.setFailureMsgForDwhPush(
                                            ResultCode.LINK_PAYMENT_ALREADY_PROCESSED.getResultCodeId(), errorMsg,
                                            null, true);
                                } else if (ResultCode.TXN_AMT_AND_PAID_AMT_EXCEEDS_TOTAL_PAYMENT_AMT.getResultMsg()
                                        .equals(errorMsg)) {
                                    resultCode = ResultCode.TXN_AMT_AND_PAID_AMT_EXCEEDS_TOTAL_PAYMENT_AMT;
                                    failureLogUtil
                                            .setFailureMsgForDwhPush(
                                                    ResultCode.TXN_AMT_AND_PAID_AMT_EXCEEDS_TOTAL_PAYMENT_AMT
                                                            .getResultCodeId(), errorMsg, null, true);
                                } else if (ResultCode.TOTAL_PAYMENT_AMOUNT_LIMIT_REACHED.getResultMsg()
                                        .equals(errorMsg)) {
                                    resultCode = ResultCode.TOTAL_PAYMENT_AMOUNT_LIMIT_REACHED;
                                    failureLogUtil.setFailureMsgForDwhPush(
                                            ResultCode.TOTAL_PAYMENT_AMOUNT_LIMIT_REACHED.getResultCodeId(), errorMsg,
                                            null, true);
                                } else {
                                    failureLogUtil.setFailureMsgForDwhPush(
                                            ResultCode.PAYMENT_IN_PROCESS.getResultCodeId(), errorMsg, null, true);
                                }
                                throw new NativeFlowException.ExceptionBuilder(resultCode).isHTMLResponse(false)
                                        .build();
                            }
                            String callbackUrl = com.paytm.pgplus.common.config.ConfigurationUtil
                                    .getProperty(PropertyConstrant.OFFLINE_STATIC_CALLBACK_URL);
                            paymentRequestData.setCallbackUrl(callbackUrl);
                            ResultInfo resultInfo = new ResultInfo();
                            resultInfo.setResultCode(ResponseConstants.INVALID_REQUEST_TYPE.getCode());
                            resultInfo.setResultMsg(errorMsg);
                            failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.INVALID_REQUEST_TYPE.getCode(),
                                    errorMsg, null, true);
                            String htmlPage = theiaResponseGenerator.generateResponseForValidationFailure(
                                    paymentRequestData, resultInfo);
                            response.setContentType("text/html");
                            response.getOutputStream().print(htmlPage);
                            EventUtils.pushLinkBasedPaymentInitiatedEvent(paymentRequestData, source);
                            return;
                        }
                    }

                }
                EventUtils.pushLinkBasedPaymentInitiatedEvent(paymentRequestData, source);
            }

            if (StringUtils.isNotEmpty(paymentRequestData.getTxnToken())
                    && paymentRequestData.getLinkDetailsData() == null) {
                String linkid = nativeSessionUtil.getLinkId(paymentRequestData.getTxnToken());
                String invoiceId = nativeSessionUtil.getInvoiceId(paymentRequestData.getTxnToken());
                if (linkid != null) {
                    paymentRequestData.setLinkId(linkid);
                    request.setAttribute(LINK_ID, linkid);
                }
                if (invoiceId != null) {
                    paymentRequestData.setInvoiceId(invoiceId);
                    request.setAttribute(INVOICE_ID, invoiceId);
                }
            }
            if ((StringUtils.isNotEmpty(paymentRequestData.getInvoiceId()) || StringUtils.isNotEmpty(paymentRequestData
                    .getLinkId())) && StringUtils.isEmpty(paymentRequestData.getCallbackUrl())) {
                paymentRequestData.setCallbackUrl(com.paytm.pgplus.common.config.ConfigurationUtil
                        .getProperty(THEIA_BUISNESS_BASE_PATH) + LINK_PAYMENT_STATUS_URL);
            }
        }
        // validateLinkOnlyMerchantRequest(paymentRequestData, request);

        processTransactionControllerHelper.checkAndSetIfDynamicQRFlow(paymentRequestData);

        processTransactionControllerHelper.checkForSeamlessNBCases(paymentRequestData);

        // processTransactionControllerHelper.checkForPWPMerchant(paymentRequestData);

        processTransactionControllerHelper.checkAndSetIfScanAndPayFlow(paymentRequestData);

        final long startTime = System.currentTimeMillis();
        PageDetailsResponse pageResponse = null;

        // Below code for native subscription payment support.
        try {
            processTransactionControllerHelper.checkAndSetIfNativeSubscriptionFlow(paymentRequestData, request);
        } catch (Exception e) {
            HttpServletRequest servletRequest = EnvInfoUtil.httpServletRequest();
            servletRequest.setAttribute(TheiaConstant.RequestParams.NATIVE_SUBSCRIPTION_RESULT_ERROR_MESSAGE,
                    e.getMessage());
            failureLogUtil.setFailureMsgForDwhPush(null, e.getMessage(), null, true);
            throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isHTMLResponse(true)
                    .setMsg(e.getMessage()).build();
        }

        try {
            if (isEnhancedCashierFlow) {
                EXT_LOGGER.customInfo("Enhanced Cashier Flow started");
                processTransactionUtil.pushNativeEnhancedEvent(paymentRequestData.getMid(),
                        paymentRequestData.getOrderId(), "Enhanced Cashier Flow started", null);
            }

            /** Below method call is for Sounbox */
            processTransactionControllerHelper.setAdditionalInfoRequestTypeForSoundBox(paymentRequestData);

            pageResponse = processPaymentRequest(paymentRequestData, model);
        } catch (BizMerchantVelocityBreachedException e) {
            handleMerchantLimitBreached(request, response, e.getLimitType(), e.getLimitDuration());
            return;
        } catch (MerchantLimitBreachedException e) {
            handleMerchantLimitBreached(request, response, e.getLimitType(), e.getLimitDuration());
            return;
        } finally {
            if (isEnhancedCashierFlow) {
                LOGGER.info("Total time taken for Enhanced Cashier Flow is {} ms", System.currentTimeMillis()
                        - startTime);
            }
        }
        Assert.notNull(pageResponse, "Service page response received was null");
        HttpServletRequest httpServletRequest = EnvInfoUtil.httpServletRequest();

        // This is to handle json response for sdk/processTransaction
        // Write your response handling below this code
        if (BooleanUtils.isTrue(paymentRequestData.isSdkProcessTxnFlow())
                || paymentRequestData.isNeedAppIntentEndpoint()) {
            if (!pageResponse.isSuccessfullyProcessed()) {
                failureLogUtil.setFailureMsgForDwhPush(
                        com.paytm.pgplus.common.enums.ResultCode.SYSTEM_ERROR.getResultCodeId(),
                        pageResponse.getException() != null ? pageResponse.getException().getMessage() : null, null,
                        true);
                if (pageResponse.getException() != null && pageResponse.getException() instanceof BaseException) {
                    throw (BaseException) pageResponse.getException();
                } else {
                    throw BaseException.getException(new ResultInfo(
                            com.paytm.pgplus.common.enums.ResultCode.SYSTEM_ERROR));
                }
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(pageResponse.getS2sResponse());
                return;
            }
        }

        if (StringUtils.isNotBlank(pageResponse.getAddMoneyToGvConsentKey())) {
            request.setAttribute(TheiaConstant.GvConsent.ADD_MONEY_TO_GV_CONSENT_KEY,
                    pageResponse.getAddMoneyToGvConsentKey());
        }

        if (pageResponse.isRiskVerificationRequired()) {
            request.setAttribute(RiskConstants.IS_RISK_VERIFICATION_REQUIRED, RiskConstants.TRUE);
            request.setAttribute(RiskConstants.TRANS_ID, pageResponse.getTransId());
        }

        if (httpServletRequest != null
                && httpServletRequest.getAttribute(RequestTypes.PAY_MODE_NOT_PRESENT_FOR_PROMO) != null
                && (Boolean) httpServletRequest.getAttribute(RequestTypes.PAY_MODE_NOT_PRESENT_FOR_PROMO) == true
                && paymentRequestData.isEnhancedCashierPageRequest() == true) {

            String htmlPage = merchantResponseService.processMerchantFailResponse(paymentRequestData,
                    ResponseConstants.PAY_MODES_ARE_BLANK_FOR_PROMO_CODE);
            failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.PAY_MODES_ARE_BLANK_FOR_PROMO_CODE.getCode(),
                    ResponseConstants.PAY_MODES_ARE_BLANK_FOR_PROMO_CODE.getMessage(), null, true);
            PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
            pageDetailsResponse.setSuccessfullyProcessed(true);
            pageDetailsResponse.setHtmlPage(htmlPage);
            response.setContentType("text/html");
            // response.setContentLength(pageResponse.getHtmlPage().length());
            // response.setHeader("Connection","close");
            // response.getOutputStream().print(pageDetailsResponse.getHtmlPage());
            /*
             * UTF-8 Encoding Fix for Native Enhance
             */
            response.getOutputStream().write(pageDetailsResponse.getHtmlPage().getBytes(StandardCharsets.UTF_8));
            return;
        }

        if (pageResponse.getData() != null && pageResponse.getData().get("LINK_BASED_PAYMENT") != null
                && pageResponse.getData().get("LINK_BASED_PAYMENT").equalsIgnoreCase("true")) {
            setLinkBasedRequestAttributes(pageResponse.getData(), request);
            LOGGER.info("LinkType in exception page {}", request.getAttribute("LINK_TYPE"));
            request.getRequestDispatcher(
                    VIEW_BASE + theiaViewResolverService.returnLinkPaymentStatusPage(request) + ".jsp").forward(
                    request, response);
            return;

        }
        if (StringUtils.isNotBlank(pageResponse.getHtmlPage()) && paymentRequestData.isEnhancedCashierPageRequest()) {
            response.setContentType("text/html");
            // response.setContentLength(pageResponse.getHtmlPage().length());
            // response.setHeader("Connection","close");
            // response.getOutputStream().print(pageResponse.getHtmlPage());
            response.getOutputStream().write(pageResponse.getHtmlPage().getBytes(StandardCharsets.UTF_8));

            return;
        } else if (StringUtils.isNotBlank(pageResponse.getHtmlPage())) {
            invalidateSession(request);
            response.setContentType("text/html");
            // response.getOutputStream().print(pageResponse.getHtmlPage());
            response.getOutputStream().write(pageResponse.getHtmlPage().getBytes(StandardCharsets.UTF_8));
            if (pageResponse.getData() != null
                    && StringUtils.equals(ResponseConstants.INVALID_SSO_TOKEN.getAlipayResultMsg(), pageResponse
                            .getData().get(TheiaConstant.ResponseConstants.RESPONSE_CODE))) {
                failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.INVALID_SSO_TOKEN.getCode(),
                        ResponseConstants.INVALID_SSO_TOKEN.getMessage(), null, true);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            return;
        }

        if (RequestTypes.SEAMLESS_ACS.equals(paymentRequestData.getRequestType())
                || RequestTypes.RENEW_SUBSCRIPTION.equals(paymentRequestData.getRequestType())
                || RequestTypes.PARTIAL_RENEW_SUBSCRIPTION.equals(paymentRequestData.getRequestType())
                || (ExtraConstants.CONNECTION_TYPE_S2S.equals(paymentRequestData.getConnectiontype()) && RequestTypes.SUBSCRIPTION
                        .equals(paymentRequestData.getRequestType()))
                || RequestTypes.MOTO_CHANNEL.equals(paymentRequestData.getRequestType())) {
            invalidateSession(request);
            response.getOutputStream().print(pageResponse.getS2sResponse());
            response.setContentType("application/json");
            return;
        }
        if ((ERequestType.isNativeOrNativeSubscriptionOrEnhancedAoaRequest(paymentRequestData.getRequestType()) || ERequestType.NATIVE_MF_SIP
                .getType().equals(paymentRequestData.getRequestType()))
                && paymentRequestData.getRequest() != null
                && paymentRequestData.getRequest().getAttribute("NATIVE_ENHANCED_FLOW") != null
                && Boolean.TRUE.equals(paymentRequestData.getRequest().getAttribute("NATIVE_ENHANCED_FLOW"))
                && StringUtils.isNotBlank(pageResponse.getS2sResponse())) {

            response.setContentType("application/json");
            // response.getOutputStream().print(pageResponse.getS2sResponse());
            response.getOutputStream().write(pageResponse.getS2sResponse().getBytes(StandardCharsets.UTF_8));
            return;
        }

        /*
         * in case 1. the request type is NATIVE_MF_SIP or NATIVE_SUBSCRIPTION
         * 2. Payment Mode is BANK_MANDATE then , check whether the it is paper
         * or e mandate a. If e mandate then redirect the request to npci url
         * with the npci request received from subscription service. b. if paper
         * mandate then provide html response which has been created from
         * subscription service's response.
         */
        if ((RequestTypes.NATIVE_MF_SIP.equalsIgnoreCase(paymentRequestData.getRequestType()) || RequestTypes.NATIVE_SUBSCRIPTION
                .equalsIgnoreCase(paymentRequestData.getRequestType()))
                && SubsPaymentMode.BANK_MANDATE.name().equalsIgnoreCase(paymentRequestData.getSubsPaymentMode())
                && (MapUtils.isNotEmpty(pageResponse.getData()) && MandateMode.E_MANDATE.name().equalsIgnoreCase(
                        pageResponse.getData().get(ExtraConstants.SUBS_BM_MODE)))) {
            invalidateSession(request);
            request.setAttribute("npci", gson.fromJson(pageResponse.getS2sResponse(), MandateResponseBody.class));
            request.setAttribute("npciUrl", pageResponse.getRedirectionUrl());
            request.getRequestDispatcher(VIEW_BASE + pageResponse.getJspName() + ".jsp").forward(request, response);
            return;
        }

        if (processTransactionUtil.isNativeJsonRequest(request)
                && StringUtils.isNotBlank(pageResponse.getS2sResponse())) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(pageResponse.getS2sResponse());
            return;
        }

        if (isEnhancedCashierFlow && StringUtils.isNotBlank(pageResponse.getRedirectionUrl())) {
            response.sendRedirect(pageResponse.getRedirectionUrl());
            return;
        }
        UltimateBeneficiaryDetails ultimateBeneficiaryDetails = null;
        if (StringUtils.isNotBlank(paymentRequestData.getTxnToken())) {
            InitiateTransactionRequestBody orderDetail = nativeSessionUtil.getOrderDetail(paymentRequestData
                    .getTxnToken());
            ultimateBeneficiaryDetails = orderDetail != null ? orderDetail.getUltimateBeneficiaryDetails() : null;
        }

        // to check if controller is returning upi polling page and flow is
        // NATIVE
        if (pageResponse.getJspName().equalsIgnoreCase(theiaViewResolverService.returnUPIPollPage())
                && ERequestType.NATIVE.getType().equalsIgnoreCase(paymentRequestData.getRequestType())) {

            TransactionInfo txnInfo = theiaSessionDataService.getTxnInfoFromSession(request);
            if (txnInfo != null && upiPollPageUtil.isUPIPollPageEnabledOnMid(txnInfo.getMid())) {
                UPITransactionInfo upiTransactionInfo = theiaSessionDataService
                        .getUPUpiTransactionInfoFromSession(request);
                MerchantInfo merchantinfo = theiaSessionDataService.getMerchantInfoFromSession(request);
                if (upiPollPageUtil.returnUPIHTMLPollPage(txnInfo, upiTransactionInfo, merchantinfo, response,
                        ultimateBeneficiaryDetails)) {
                    theiaResponseGenerator.pushPtcResultStatusToStatsD(SUCCESS_RESULT_STATUS);
                    return;
                }
            }
        }
        theiaSessionDataService.updateBeneficiaryDetailsInMerchantInfo(request, ultimateBeneficiaryDetails);
        request.getRequestDispatcher(VIEW_BASE + pageResponse.getJspName() + ".jsp").forward(request, response);
        return;
    }

    private void validateLinkOnlyMerchantRequest(PaymentRequestBean paymentRequestData, HttpServletRequest request) {
        if (processTransactionUtil.isRequestOfType(V1_PTC)
                && merchantPreferenceService.isLinkPaymentOnlyMerchant(paymentRequestData.getMid())) {
            if (StringUtils.isNotEmpty(paymentRequestData.getInvoiceId())
                    || StringUtils.isNotEmpty(paymentRequestData.getLinkId())) {
                LOGGER.info("Link based Request with pref enabled, orderID = {}", paymentRequestData.getOrderId());
            } else if (ERequestType.getByRequestType(paymentRequestData.getRequestType()).getProductCode()
                    .equals(ERequestType.LINK_BASED_PAYMENT.getProductCode())) {
                LOGGER.error("Only Link based transactions allowed on merchant using product code {}"
                        + ProductCodes.StandardDirectPayAcquiringProd.getId());
                processTransactionUtil.throwNativeException(request, ResponseConstants.UNAUTH_API_FLOW_ACCESS,
                        "Only Link based transactions allowed on merchant");
            }
        }
    }

    private void handleMerchantLimitBreached(HttpServletRequest request, HttpServletResponse response,
            String limitType, String limitDuration) throws ServletException, IOException {
        LOGGER.error("Merchant {} {} limit breached!", limitDuration, limitType);
        ResultCode resultCode = MapperUtils.getResultCodeForMerchantBreached(limitType, limitDuration);
        if (TheiaConstant.ExtraConstants.EDC_MERCHANT_VELOCITY_LIMIT_BREACH.equalsIgnoreCase(resultCode.getCode())) {
            failureLogUtil.setFailureMsgForDwhPush(resultCode.getResultCodeId(), resultCode.getResultMsg(), null, true);
            throw new EdcMerchantLimitBreachException.ExceptionBuilder().setMessage(resultCode.getResultMsg()).build();
        } else if (TheiaConstant.ExtraConstants.ADDNPAY_MONTHLY_AMOUNT_LIMIT_EXCEED.equalsIgnoreCase(resultCode
                .getCode())) {
            failureLogUtil.setFailureMsgForDwhPush(resultCode.getResultCodeId(), resultCode.getResultMsg(), null, true);
            throw new AddMoneyLimitBreachException.ExceptionBuilder().setMessage(resultCode.getResultMsg()).build();
        } else {
            request.setAttribute(SHOW_VIEW_FLAG, MERCHANT_LIMIT_ERROR_SCREEN);
            request.setAttribute("ERROR_MESSAGE", MapperUtils
                    .getResultCodeForMerchantBreached(limitType, limitDuration).getResultMsg());
            request.getRequestDispatcher(
                    VIEW_BASE + theiaViewResolverService.returnLinkPaymentStatusPage(request) + ".jsp").forward(
                    request, response);
        }

    }

    private void setLinkBasedRequestAttributes(Map<String, String> data, HttpServletRequest request) {

        request.setAttribute(PAYMENT_STATUS, data.get(PAYMENT_STATUS));
        request.setAttribute(TXN_DATE, data.get(TXN_DATE));
        request.setAttribute(TRANSACTION_ID, data.get(TRANSACTION_ID));
        request.setAttribute(TXN_AMOUNT, data.get(TXN_AMOUNT));
        request.setAttribute(SHOW_VIEW_FLAG, PAYMENT_SCREEN);
        request.setAttribute(MERCHANT_NAME, data.get(MERCHANT_NAME));
        request.setAttribute(MERCHANT_IMAGE, data.get(MERCHANT_IMAGE));
        request.setAttribute(LINK_BASED_PAYMENT, data.get(LINK_BASED_PAYMENT));
        request.setAttribute(ERROR_MESSAGE, data.get(ERROR_MESSAGE));
        request.setAttribute(ERROR_CODE, data.get(ERROR_CODE));
        request.setAttribute(TheiaConstant.ResponseConstants.LINK_TYPE,
                data.get(TheiaConstant.ResponseConstants.LINK_TYPE));
        request.setAttribute(TheiaConstant.RequestParams.ORDER_ID, data.get(TheiaConstant.RequestParams.ORDER_ID));

        double totalAmount = Double.parseDouble(data.get(TXN_AMOUNT));

        if (StringUtils.isNotEmpty(data.get(TheiaConstant.ResponseConstants.CHARGE_AMOUNT))) {
            request.setAttribute(TheiaConstant.ResponseConstants.CHARGE_AMOUNT,
                    data.get(TheiaConstant.ResponseConstants.CHARGE_AMOUNT));
            totalAmount = Double.sum(totalAmount,
                    Double.parseDouble(data.get(TheiaConstant.ResponseConstants.CHARGE_AMOUNT)));
        }

        request.setAttribute(TheiaConstant.ResponseConstants.TOTAL_AMOUNT, totalAmount);
    }

    //
    // private void makeSessionForLinkBasedPayment(HttpServletRequest
    // httpServletRequest) {
    //
    // if
    // (RequestTypes.LINK_BASED_PAYMENT.equalsIgnoreCase(httpServletRequest.getParameter(REQUEST_TYPE)))
    // {
    // LOGGER.info("Creating session for Link Based Payment request");
    // Map<String, Object> attributeMap =
    // encParamRequestService.buildCustomAttributeMap(httpServletRequest);
    // encParamRequestService.invokeCustomValve(attributeMap);
    // }
    // }

    private void clearErrorParams(HttpServletRequest request) {
        request.removeAttribute("errorMsg");
        request.removeAttribute("validationErrors");
    }

    private PageDetailsResponse validateChecksumForEnhancedCashierPageRequest(
            final PaymentRequestBean paymentRequestData, final Model model) {

        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        ValidationResults validationResult;
        String jspName;

        boolean ignoreChecksumAppInvokeFlow = processTransactionControllerHelper.allowForNativeAppInvokeFlow(
                paymentRequestData, null);

        /*
         * Post paid onboarding is enable by default for enhance Native Flow and
         * we are using this flag for offline and native app backward
         * compatibility
         */
        paymentRequestData.setPostpaidOnboardingSupported(true);
        /**
         * This flag is used to identify whether qr is need to create again or
         * not in login and logout case
         */
        paymentRequestData.setDynamicQrRequired(true);
        boolean isSdkProcessTransaction = BooleanUtils.isTrue(paymentRequestData.isSdkProcessTxnFlow());

        if (ignoreChecksumAppInvokeFlow) {

            /*
             * we ignore checksum validation as it has already been validated in
             * /api/v1/initiateTransaction
             */

            validationResult = ValidationResults.VALIDATION_SUCCESS;

        } else {
            validationResult = enhancedCashierPageService.validatePaymentRequest(paymentRequestData);
        }
        switch (validationResult) {
        case CHECKSUM_VALIDATION_FAILURE:
            LOGGER.error(CHECKSUM_FAILED_ERROR_MSG, paymentRequestData.getMid());
            EventUtils.pushChecksumFailureEvent(CHECKSUM_FAILED_ERROR_MSG, paymentRequestData.getMid(),
                    paymentRequestData.getOrderId());
            if (isSdkProcessTransaction || paymentRequestData.isNeedAppIntentEndpoint()) {
                pageDetailsResponse = new PageDetailsResponse(false);
                pageDetailsResponse.setException(BaseException.getException(ResultCode.INVALID_CHECKSUM));
                break;
            }
            pageDetailsResponse = theiaResponseGenerator.getPageDetailsResponse(paymentRequestData,
                    ResponseConstants.INVALID_CHECKSUM);
            break;
        case UNKNOWN_VALIDATION_FAILURE:
            LOGGER.error(CHECKSUM_UNKNOWN_ERROR_MSG, paymentRequestData.getMid());
            EventUtils.pushChecksumFailureEvent(CHECKSUM_UNKNOWN_ERROR_MSG, paymentRequestData.getMid(),
                    paymentRequestData.getOrderId());
            if (isSdkProcessTransaction || paymentRequestData.isNeedAppIntentEndpoint()) {
                pageDetailsResponse = new PageDetailsResponse(false);
                pageDetailsResponse.setException(BaseException.getException(ResultCode.INVALID_CHECKSUM));
                break;
            }
            jspName = theiaViewResolverService.returnOOPSPage(paymentRequestData.getRequest());
            pageDetailsResponse.setJspName(jspName);
            break;
        case VALIDATION_SUCCESS:
            pageDetailsResponse = enhancedCashierPageService.processPaymentRequest(paymentRequestData, model);
            break;
        default:
            break;
        }
        return pageDetailsResponse;
    }

    private String processForError(final PaymentRequestBean paymentRequestData, final Model model) {
        throw new TheiaControllerException("Checksum is Invalid for the request.");
    }

    private boolean checkAPIBasedTransactionFailure(PaymentRequestBean paymentRequestData) {
        // TODO ask in review
        if (paymentRequestData.getAggMid() != null
                && AggregatorMidKeyUtil.isMidEnabledForAggregatorMid(paymentRequestData.getAggMid())) {
            return false;
        } else if (StringUtils.equals(paymentRequestData.getWorkflow(),
                TheiaConstant.EnhancedCashierFlow.ENHANCED_CASHIER_FLOW)
                || TokenType.SSO.getType().equals(paymentRequestData.getTokenType())
                || paymentRequestData.getLinkDetailsData() != null
                || StringUtils.isNotEmpty(paymentRequestData.getLinkId())
                || StringUtils.isNotEmpty(paymentRequestData.getInvoiceId())
                || merchantPreferenceService.isDealsEnabled(paymentRequestData.getMid(), false)) {
            return false;
        }
        boolean apiDisabled = merchantPreferenceProvider.isAPIDisabled(paymentRequestData);
        boolean isAPIDisabled = apiDisabled;
        if (!isAPIDisabled) {
            return false;
        }
        String requestType = paymentRequestData.getRequestType();
        boolean typeCheck = ERequestType.DEFAULT.getType().equalsIgnoreCase(requestType)
                || ERequestType.NATIVE.getType().equalsIgnoreCase(requestType);
        return typeCheck;
    }

    @RequestMapping(value = "/linkPaymentRedirect", method = RequestMethod.POST)
    public void processPaymentPage(HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        LOGGER.info("Redirect to wallet transaction page");
        String mid = request.getParameter("MID");
        LOGGER.info("MID found in link Redirect request : {}", mid);
        Map<String, Object> context = new HashMap<>();
        context.put("mid", mid);
        String linkPaymentRedrectPage = com.paytm.pgplus.common.config.ConfigurationUtil
                .getLinkPaymentRedirectionHtmlPage();
        boolean toShowHtmlPage = iPgpFf4jClient.checkWithdefault("linkPaymentRedirect", context, true)
                && linkPaymentRedrectPage != null && !linkPaymentRedrectPage.isEmpty();
        if (Boolean.TRUE.equals(toShowHtmlPage)) {
            Enumeration<String> sessionAttributes = request.getAttributeNames();
            while (sessionAttributes.hasMoreElements()) {
                request.removeAttribute(sessionAttributes.nextElement());
            }
        }
        request.setAttribute(PAYMENT_STATUS, request.getParameter(PAYMENT_STATUS));
        request.setAttribute("requestType", request.getParameter("requestType"));
        request.setAttribute(SHOW_VIEW_FLAG, request.getParameter(SHOW_VIEW_FLAG));
        request.setAttribute(TheiaConstant.RequestParams.ORDER_ID,
                request.getParameter(TheiaConstant.RequestParams.ORDER_ID));
        request.setAttribute(TXN_AMOUNT, request.getParameter(TXN_AMOUNT));
        request.setAttribute(TXN_DATE, request.getParameter(TXN_DATE));
        MerchantInfoServiceRequest merchantDetailsRequest = new MerchantInfoServiceRequest(mid);
        MerchantBussinessLogoInfo merchantDetailsResponse = merchantInfoService.getMerchantInfo(merchantDetailsRequest);
        if (merchantDetailsResponse != null && StringUtils.isNotBlank(merchantDetailsResponse.getMerchantDisplayName())) {
            request.setAttribute(MERCHANT_NAME, merchantDetailsResponse.getMerchantDisplayName());
        } else {
            request.setAttribute(MERCHANT_NAME, request.getParameter(MERCHANT_NAME));
        }
        request.setAttribute(MERCHANT_IMAGE, request.getParameter(MERCHANT_IMAGE));
        request.setAttribute(LINK_BASED_PAYMENT, request.getParameter(LINK_BASED_PAYMENT));
        request.setAttribute(TRANSACTION_ID, request.getParameter(TRANSACTION_ID));
        request.setAttribute(ERROR_CODE, request.getParameter(ERROR_CODE));
        if (request.getParameter(ERROR_MESSAGE) != null) {
            request.setAttribute(ERROR_MESSAGE, request.getParameter(ERROR_MESSAGE));
        } else {
            request.setAttribute(ERROR_MESSAGE, request.getParameter("errorMessage"));
        }
        request.setAttribute(TheiaConstant.ResponseConstants.LINK_TYPE,
                request.getParameter(TheiaConstant.ResponseConstants.LINK_TYPE));
        LinkDetailResponseBody linkDetailResponseBody = linkPaymentUtil.getLinkDetailCachedResponse(request);
        try {
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("RESPONSE_STATUS", linkDetailResponseBody.getResultInfo().getResultStatus());
            responseMap.put("RESPONSE_MESSAGE", linkDetailResponseBody.getResultInfo().getResultMsg());
            statsDUtils.pushResponse("linkPaymentRedirect", responseMap);
        } catch (Exception exception) {
            LOGGER.error("Error in pushing response message " + "linkPaymentRedirect" + "to grafana", exception);
        }
        LOGGER.info("link detail response  is: {}", linkDetailResponseBody);
        if (linkDetailResponseBody != null) {
            if (TXN_SUCCESS.equalsIgnoreCase(request.getParameter("STATUS"))
                    || SUCCESS.equalsIgnoreCase(request.getParameter("STATUS"))) {
                if (linkDetailResponseBody.getCustomPaymentSuccessMessage() != null) {
                    request.setAttribute(TheiaConstant.LinkBasedParams.CUSTOM_PAYMENT_SUCCESS_MESSAGE,
                            linkDetailResponseBody.getCustomPaymentSuccessMessage());
                }
                if (linkDetailResponseBody.getRedirectionUrlSuccess() != null) {
                    request.setAttribute(TheiaConstant.LinkBasedParams.REDIRECTION_URL_SUCCESS,
                            linkDetailResponseBody.getRedirectionUrlSuccess());
                }
                boolean showLinkPromotion = true;
                if (StringUtils.isNotBlank(request.getParameter(TheiaConstant.LinkBasedParams.SHOW_LINK_PROMOTION))
                        && request.getParameter(TheiaConstant.LinkBasedParams.SHOW_LINK_PROMOTION).equalsIgnoreCase(
                                FALSE)) {
                    showLinkPromotion = false;
                } else if (MapUtils.isNotEmpty(linkDetailResponseBody.getExtendInfo())) {
                    String merch_unq_ref = linkDetailResponseBody.getExtendInfo().get(
                            TheiaConstant.ExtendedInfoKeys.MERCH_UNQ_REF);
                    if (StringUtils.isNotBlank(merch_unq_ref) && merch_unq_ref.startsWith(PAYMENT_BUTTONS_PREFIX)) {
                        showLinkPromotion = false;
                    }
                }
                request.setAttribute(TheiaConstant.LinkBasedParams.SHOW_LINK_PROMOTION, showLinkPromotion);
            } else if (TXN_FAILURE.equalsIgnoreCase(request.getParameter("STATUS"))
                    || FAILURE.equalsIgnoreCase(request.getParameter("STATUS"))
                    || TXN_CANCEL.equalsIgnoreCase(request.getParameter("STATUS"))) {
                if (linkDetailResponseBody.getCustomPaymentFailureMessage() != null) {
                    request.setAttribute(TheiaConstant.LinkBasedParams.CUSTOM_PAYMENT_FAILURE_MESSAGE,
                            linkDetailResponseBody.getCustomPaymentFailureMessage());
                }
                if (linkDetailResponseBody.getRedirectionUrlFailure() != null) {
                    request.setAttribute(TheiaConstant.LinkBasedParams.REDIRECTION_URL_FAILURE,
                            linkDetailResponseBody.getRedirectionUrlFailure());
                }
            } else if (PENDING.equalsIgnoreCase(request.getParameter("STATUS"))
                    || PROCESSING.equalsIgnoreCase(request.getParameter("STATUS"))) {
                if (linkDetailResponseBody.getCustomPaymentPendingMessage() != null) {
                    request.setAttribute(TheiaConstant.LinkBasedParams.CUSTOM_PAYMENT_PENDING_MESSAGE,
                            linkDetailResponseBody.getCustomPaymentPendingMessage());
                }
                if (linkDetailResponseBody.getRedirectionUrlPending() != null) {
                    request.setAttribute(TheiaConstant.LinkBasedParams.REDIRECTION_URL_PENDING,
                            linkDetailResponseBody.getRedirectionUrlPending());
                }
            }
            if (StringUtils.isNotBlank(linkDetailResponseBody.getLinkDescription())) {
                request.setAttribute(LINK_DESCRIPTION, linkDetailResponseBody.getLinkDescription());
            }
        }
        if (StringUtils.isNotEmpty(request.getParameter(TXN_AMOUNT))) {
            double totalAmount = Double.parseDouble(request.getParameter(TXN_AMOUNT));

            if (StringUtils.isNotEmpty(request.getParameter(TheiaConstant.ResponseConstants.CHARGE_AMOUNT))) {
                request.setAttribute(TheiaConstant.ResponseConstants.CHARGE_AMOUNT,
                        request.getParameter(TheiaConstant.ResponseConstants.CHARGE_AMOUNT));
                totalAmount = Double.sum(totalAmount,
                        Double.parseDouble(request.getParameter(TheiaConstant.ResponseConstants.CHARGE_AMOUNT)));
            }

            request.setAttribute(TheiaConstant.ResponseConstants.TOTAL_AMOUNT, totalAmount);
        } else {
            request.setAttribute(TheiaConstant.ResponseConstants.TOTAL_AMOUNT, request.getParameter(TXN_AMOUNT));
        }
        processTransactionControllerHelper.deleteRedisKeyForCancelledTxns(request);

        if (Boolean.TRUE.equals(toShowHtmlPage)) {
            LOGGER.info("Inside link redirect html page serve");
            JSONObject jsonObj = linkPaymentUtil.requestParamsToJSON(request);
            LOGGER.info("JsonObj served to  linkPaymentRedirect html page  : {}", jsonObj);
            linkPaymentRedrectPage = linkPaymentRedrectPage.replace("PUSH_APP_DATA", jsonObj.toString());
            response.setContentType("text/html; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(linkPaymentRedrectPage);
        } else {
            request.getRequestDispatcher(
                    VIEW_BASE + theiaViewResolverService.returnLinkPaymentStatusPage(request) + ".jsp").forward(
                    request, response);
        }

    }

    public void checkPaymentCountBreached(HttpServletRequest request) throws NativeFlowException {
        if (processTransactionUtil.isMaxPaymentRetryBreached(request)) {
            failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED.getCode(),
                    ResponseConstants.NATIVE_RETRY_COUNT_BREACHED.getMessage(), null, true);

            if (Boolean.TRUE.equals(request.getAttribute("NATIVE_ENHANCED_FLOW"))) {
                throw new NativeFlowException.ExceptionBuilder(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED)
                        .isHTMLResponse(false).isRedirectEnhanceFlow(true).isRetryAllowed(false)
                        .setMsg(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED.getMessage()).build();
            } else if (processTransactionUtil.isNativeJsonRequest(request)) {
                throw new NativeFlowException.ExceptionBuilder(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED)
                        .isHTMLResponse(false).isRetryAllowed(false)
                        .setMsg(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED.getMessage()).build();
            } else {
                throw new NativeFlowException.ExceptionBuilder(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED)
                        .isHTMLResponse(true).isRetryAllowed(false)
                        .setMsg(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED.getMessage()).build();
            }
        }

    }

    public void resumeRiskVerifiedRequest(HttpServletRequest request, HttpServletResponse response, Model model)
            throws IOException, ServletException {
        LOGGER.info("Resume pay request for risk-verified flow");

        String securityId = riskVerificationUtil.getSecurityIdFromPayReuest(request);
        String key = RiskConstants.RISK_VERIFICATION_KEY_PREFIX + request.getParameter(RiskConstants.TOKEN);
        Map<String, String[]> additionalParams = (Map<String, String[]>) nativeSessionUtil.getKey(key);
        if (additionalParams == null) {
            failureLogUtil.setFailureMsgForDwhPush(null,
                    BizConstant.FailureLogs.NO_DATA_FOUND_IN_CACHE_FOR_RESUMING_RISK_VERIFIED_REQUEST, null, true);
            throw new TheiaServiceException(
                    BizConstant.FailureLogs.NO_DATA_FOUND_IN_CACHE_FOR_RESUMING_RISK_VERIFIED_REQUEST);
        }

        if (securityId != null) {
            additionalParams.put(RiskConstants.SECURITY_ID, new String[] { securityId });
        }
        additionalParams.put(PAYMENT_RESUME, new String[] { Boolean.TRUE.toString() });
        ModifiableHttpServletRequest mServletRequest = new ModifiableHttpServletRequest(request, additionalParams);
        if (securityId == null) {
            riskVerificationUtil.sendFailureResponseToMerchant(new PaymentRequestBean(mServletRequest), response);
            failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.MERCHANT_FAILURE_RESPONSE.getCode(),
                    "Risk_not_verified request. Failing!", null, true);
            return;
        }
        processPaymentRequest(mServletRequest, response, model, false, null);
        return;
    }

    void checkIfMerchantBlocked(final HttpServletRequest request) throws NativeFlowException {
        if (isBlockedMerchant(request)) {
            LOGGER.error("Merchant is either blocked or Inactive");
            failureLogUtil.setFailureMsgForDwhPush(ResponseConstants.MERCHANT_BLOCKED.getCode(),
                    ResponseConstants.MERCHANT_BLOCKED.getMessage(), null, true);
            throw new NativeFlowException.ExceptionBuilder(ResponseConstants.MERCHANT_BLOCKED).build();
        }
    }

    private boolean isKhatabookSdMerchant(PaymentRequestBean paymentRequestData) {
        if (paymentRequestData.getTxnToken() != null) {
            NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.getNativeInitiateRequest(paymentRequestData
                    .getTxnToken());
            if (nativeInitiateRequest != null && nativeInitiateRequest.getInitiateTxnReq() != null) {
                InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
                String aggMid = orderDetail.getAggMid();
                if (StringUtils.isNotBlank(aggMid) && AggregatorMidKeyUtil.isMidEnabledForAggregatorMid(aggMid)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void settingSplitSettlementInfo(PaymentRequestBean paymentRequestData, HttpServletRequest request) {
        if (StringUtils
                .isNotEmpty(request
                        .getParameter(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtendedInfoKeys.MERCHANT_SPLIT_SETTLEMENT_INFO))) {
            try {
                paymentRequestData
                        .setSplitSettlementInfoData(JsonMapper.mapJsonToObject(
                                request.getParameter(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtendedInfoKeys.MERCHANT_SPLIT_SETTLEMENT_INFO),
                                SplitSettlementInfoData.class));
            } catch (FacadeCheckedException e) {
                LOGGER.error("illegal splitSettlementInfo {} ", e);
                throw new PaymentRequestValidationException(e, ResponseConstants.INVALID_REQUEST);

            }
        }
    }

    private void mapLinkParamsForOfflineFlow(PaymentRequestBean paymentRequestData) {
        String linkId = null;
        String linkType = null;

        if (StringUtils.isBlank(paymentRequestData.getLinkId())
                && StringUtils.isBlank(paymentRequestData.getInvoiceId())
                && StringUtils.isNotBlank(paymentRequestData.getAdditionalInfo())) {
            String[] additionalInfoKeyValArray = paymentRequestData.getAdditionalInfo().split(
                    Pattern.quote(ADDITIONAL_INFO_DELIMITER));
            for (String keyVal : additionalInfoKeyValArray) {
                if (keyVal.contains(LINK_INVOICE_ID)) {
                    String[] keyValSplit = keyVal.split(Pattern.quote(ADDITIONAL_INFO_KEY_VAL_SEPARATOR), 2);
                    if (keyValSplit.length == 2) {
                        linkId = keyValSplit[1].trim();
                    }
                } else if (keyVal.contains(LINK_TYPE)) {
                    String[] keyValSplit = keyVal.split(Pattern.quote(ADDITIONAL_INFO_KEY_VAL_SEPARATOR), 2);
                    if (keyValSplit.length == 2) {
                        linkType = keyValSplit[1].trim();
                    }

                }
            }
            if (INVOICE.equals(linkType)) {
                paymentRequestData.setInvoiceId(linkId);
            } else {
                paymentRequestData.setLinkId(linkId);
            }
        }
    }

    private boolean checkIfCardPayMode(HttpServletRequest request) {

        String paymentMode = request.getParameter(TheiaConstant.RequestParams.Native.PAYMENT_MODE);
        if (EPayMethod.DEBIT_CARD.getMethod().equalsIgnoreCase(paymentMode)
                || EPayMethod.CREDIT_CARD.getMethod().equalsIgnoreCase(paymentMode)) {
            return true;
        }
        return false;

    }

    private Map<String, String[]> buildRequestForDccPayment(HttpServletRequest request, String requestData,
            NativeJsonRequest nativeJsonRequest) throws Exception {
        Map<String, String[]> nativeRequestParamMap;
        LOGGER.info("NativeJsonRequest received: {}", nativeJsonRequest);
        String txnToken = nativeJsonRequest.getHead().getTxnToken();

        String workFlow = (String) request.getAttribute(TheiaConstant.EnhancedCashierFlow.WORKFLOW);

        if (StringUtils.equals(workFlow, TheiaConstant.EnhancedCashierFlow.ENHANCED_CASHIER_FLOW)) {
            LOGGER.info("Payment call for Enhanced Flow dcc ");
            request.setAttribute("NATIVE_ENHANCED_FLOW", true);
        } else {
            LOGGER.info("Payment call for Native/NativeJson flow dcc ");
            request.setAttribute(NATIVE_JSON_REQUEST, true);
        }
        nativeRequestParamMap = buildNativeRequestParamsForDcc(nativeJsonRequest, txnToken);
        return nativeRequestParamMap;
    }

    private Map<String, String[]> buildNativeRequestParamsForDcc(NativeJsonRequest nativeJsonRequest, String txnToken)
            throws Exception {
        Map<String, String[]> cachedServletRequestMap = nativeSessionUtil.getRequestParamForDcc(txnToken);
        Map<String, String[]> nativeRequestParamMap = mapParamForNativeJsonRequestFlow(nativeJsonRequest);

        Map<String, String[]> intermediateMap = removeNullValuesFromNativeParamMap(cachedServletRequestMap);
        nativeRequestParamMap = removeNullValuesFromNativeParamMap(nativeRequestParamMap);
        if (intermediateMap != null)
            intermediateMap.putAll(nativeRequestParamMap);
        return intermediateMap;

    }

    private boolean dccEnabledOnMerchantAndCardPayMode(String merchantId, HttpServletRequest request) {
        return merchantPreferenceService.isDccEnabledMerchant(merchantId) && checkIfCardPayMode(request);
    }

    private Map<String, String[]> removeNullValuesFromNativeParamMap(Map<String, String[]> nativeParamMap) {
        if (nativeParamMap == null) {
            return new HashMap<>();
        }
        return nativeParamMap.entrySet().stream().filter(entry -> {
            return (Arrays.stream(entry.getValue()).filter(stringele -> {
                return (stringele != null && stringele.length() != 0);
            })).toArray().length > 0;
        }).collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));

    }

    private void logDwhFailureMsgToKafka(HttpServletRequest request, ModifiableHttpServletRequest mServletRequest,
            Map<String, String[]> nativeRequestParamMap) {
        try {
            if (mServletRequest == null && MapUtils.isEmpty(nativeRequestParamMap)) {
                Map<String, String[]> parametersMap;
                String requestData = IOUtils.toString(request.getInputStream(), Charsets.UTF_8.name());
                if (StringUtils.isNotBlank(requestData)) {
                    String workFlow = (String) request.getAttribute(TheiaConstant.EnhancedCashierFlow.WORKFLOW);
                    if (StringUtils.equals(workFlow, TheiaConstant.EnhancedCashierFlow.ENHANCED_CASHIER_FLOW)) {
                        NativePaymentRequest nativePaymentRequest = JsonMapper.mapJsonToObject(requestData,
                                NativePaymentRequest.class);
                        parametersMap = mapJsonRequestParametersForEnhancedFlow(nativePaymentRequest);
                    } else {
                        NativeJsonRequest nativeJsonRequest = JsonMapper.mapJsonToObject(requestData,
                                NativeJsonRequest.class);
                        parametersMap = mapParamForNativeJsonRequestFlow(nativeJsonRequest);
                    }
                } else {
                    parametersMap = request.getParameterMap();
                }
                failureLogUtil.pushFailureLogToDwhKafka(parametersMap, TheiaConstant.ResponseConstants.V1_PTC);
            } else if (mServletRequest == null && MapUtils.isNotEmpty(nativeRequestParamMap)) {
                failureLogUtil.pushFailureLogToDwhKafka(nativeRequestParamMap, TheiaConstant.ResponseConstants.V1_PTC);
            } else if (mServletRequest != null) {
                failureLogUtil.pushFailureLogToDwhKafka(mServletRequest.getParameterMap(),
                        TheiaConstant.ResponseConstants.V1_PTC);
            }
        } catch (Exception e) {
            LOGGER.error("Error while calling pushFailureLogToDwhKafka method: {}", e.getMessage());
        } finally {
            failureLogUtil.deleteKeyFromRedis(request);
        }
    }

}
