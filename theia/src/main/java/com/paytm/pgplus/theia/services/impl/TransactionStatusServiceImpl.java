package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.core.model.request.BizCancelOrderRequest;
import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.core.order.service.IOrderService;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.biz.utils.DynamicQRCoreService;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.utils.PG2Utilities;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.cache.enums.TransactionType;
import com.paytm.pgplus.cache.model.LinkBasedMerchantInfo;
import com.paytm.pgplus.cache.model.TransactionInfo;
import com.paytm.pgplus.cache.util.Constants;
import com.paytm.pgplus.cashier.cache.service.impl.CashierCacheServiceImpl;
import com.paytm.pgplus.cashier.cachecard.model.BankCardRequest;
import com.paytm.pgplus.cashier.cachecard.model.SavedCardRequest;
import com.paytm.pgplus.cashier.constant.CashierWorkflow;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.looper.model.CashierFundOrderStatus;
import com.paytm.pgplus.cashier.looper.model.CashierPaymentStatus;
import com.paytm.pgplus.cashier.looper.model.CashierTransactionStatus;
import com.paytm.pgplus.cashier.looper.model.LooperRequest;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.CashierRequest.CashierRequestBuilder;
import com.paytm.pgplus.cashier.models.DoPaymentResponse;
import com.paytm.pgplus.cashier.models.PayOption;
import com.paytm.pgplus.cashier.service.impl.PaymentServiceImpl;
import com.paytm.pgplus.cashier.util.RouteUtil;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.enums.SubsPaymentMode;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.common.responsecode.enums.SystemResponseCode;
import com.paytm.pgplus.common.util.PaymentModeMapperUtil;
import com.paytm.pgplus.facade.acquiring.enums.AcquirementStatusType;
import com.paytm.pgplus.facade.acquiring.models.PaymentView;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByMerchantTransIdRequest;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByMerchantTransIdRequestBody;
import com.paytm.pgplus.facade.acquiring.models.response.QueryByMerchantTransIdResponse;
import com.paytm.pgplus.facade.acquiring.services.IAcquiringOrder;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.enums.UIMicroserviceUrl;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.fund.enums.FundOrderStatus;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.facade.payment.models.PayOptionInfo;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.paymentrouter.model.RouteResponse;
import com.paytm.pgplus.facade.paymentrouter.service.IRouteClient;
import com.paytm.pgplus.facade.utils.GenericCallBack;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.facade.utils.RequestIdGenerator;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.response.ChildTransaction;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtendedInfoKeys.PaymentStatusKeys;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams;
import com.paytm.pgplus.theia.constants.TheiaConstant.RetryConstants;
import com.paytm.pgplus.theia.enums.UPIPollStatus;
import com.paytm.pgplus.theia.exceptions.SessionExpiredException;
import com.paytm.pgplus.theia.exceptions.TheiaControllerException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.helper.LinkBasedPaymentHelper;
import com.paytm.pgplus.theia.helper.UIMicroserviceHelper;
import com.paytm.pgplus.theia.kafka.service.IKafkaService;
import com.paytm.pgplus.theia.models.uimicroservice.request.UIMicroserviceRequest;
import com.paytm.pgplus.theia.models.uimicroservice.response.UIMicroserviceResponse;
import com.paytm.pgplus.theia.nativ.model.common.EnhanceCashierPageCachePayload;
import com.paytm.pgplus.theia.nativ.model.common.EnhancedCashierPage;
import com.paytm.pgplus.theia.nativ.model.transactionStatus.UpiTransactionStatusRequest;
import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeRetryUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.constants.PropertyConstrant;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.services.helper.EnhancedCashierPageServiceHelper;
import com.paytm.pgplus.theia.services.helper.FF4JHelper;
import com.paytm.pgplus.theia.services.helper.NativeDirectBankPageHelper;
import com.paytm.pgplus.theia.services.helper.RetryServiceHelper;
import com.paytm.pgplus.theia.sessiondata.MerchantInfo;
import com.paytm.pgplus.theia.utils.*;
import com.paytm.pgplus.theia.validator.service.ValidationService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.paytm.pgplus.biz.utils.BizConstant.MP_ADD_MONEY_MID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.SUBSCRIPTION_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.FEATURE_UI_MICROSERVICE_ENHANCED;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.THEIA_VALIDATE_HEADER_WORKFLOW_ENABLE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.TXN_TOKEN;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.ORDER_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.TRANSACTION_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes.*;
import static com.paytm.pgplus.theia.enums.UPIPollStatus.POLL_AGAIN;
import static com.paytm.pgplus.theia.enums.UPIPollStatus.STOP_POLLING;

@Component("transactionStatusServiceImpl")
public class TransactionStatusServiceImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionStatusServiceImpl.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(TransactionStatusServiceImpl.class);

    @Autowired
    @Qualifier(value = "paymentServiceImpl")
    private PaymentServiceImpl paymentServiceImpl;

    @Autowired
    @Qualifier(value = "cashierCacheServiceImpl")
    private CashierCacheServiceImpl cashierCacheServiceImpl;

    @Autowired
    @Qualifier(value = "retryPaymentServiceImpl")
    private RetryPaymentServiceImpl retryPaymentServiceImpl;

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    private RetryServiceHelper retryServiceHelper;

    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("acquiringOrderImpl")
    private IAcquiringOrder acquiringOrder;

    @Autowired
    @Qualifier("orderService")
    private IOrderService orderServiceImpl;

    @Autowired
    private IKafkaService kafkaService;

    @Autowired
    private DynamicQRCoreService dynamicQRCoreService;

    @Autowired
    @Qualifier("aoaUtils")
    AOAUtils aoaUtils;

    @Autowired
    private NativeRetryUtil nativeRetryUtil;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("pg2Utilities")
    private PG2Utilities pg2Utilities;

    @Autowired
    EnhancedCashierPageServiceHelper enhancedCashierPageServiceHelper;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private ResponseCodeUtil responseCodeUtil;

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("processTransactionUtil")
    ProcessTransactionUtil processTransactionUtil;

    @Autowired
    @Qualifier("nativeDirectBankPageHelper")
    NativeDirectBankPageHelper nativeDirectBankPageHelper;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    private UIMicroserviceHelper uiMicroserviceHelper;

    @Autowired
    private FF4JHelper ff4JHelper;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private TransactionCacheUtils transactionCacheUtils;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    private IRouteClient routeClient;

    @Autowired
    private RouteUtil routeUtil;

    @Autowired
    private RouterUtil routerUtil;

    @Autowired
    private ValidationService validationService;

    /**
     * @param request
     * @return
     */

    private static final String VIEW_BASE = "/WEB-INF/views/jsp/";
    private static String pg2AllPayModes = ConfigurationUtil.getProperty("pg2.all.payModes", "");
    private static List<String> listPg2AllPayModes = Arrays.asList(pg2AllPayModes.split(",")).stream()
            .map(s -> s.trim()).filter(s -> s.length() > 0).collect(Collectors.toList());

    public Map<String, String> getCashierResponse(HttpServletRequest request) {
        String callBackUrl = "";
        String merchantId = request.getParameter(ExtraConstants.MERCHANT_ID);
        String cashierRequestId = request.getParameter(ExtraConstants.CASHIER_REQUEST_ID);
        String transId = request.getParameter(ExtraConstants.TRANS_ID);
        String paymentMode = request.getParameter(ExtraConstants.PAYMENT_MODE);
        String destination = request.getParameter(ExtraConstants.RESPONSE_DESTINATION);
        String requestType = request.getParameter(ExtraConstants.REQUEST_TYPE);
        String orderId = request.getParameter(ExtraConstants.ORDER_ID);
        boolean isJsonTxnStatusReq = StringUtils.equals(TRUE, request.getParameter(TRANSACTION_STATUS_JSON_REQUEST));

        final EnvInfoRequestBean envInfo = EnvInfoUtil.fetchEnvInfo(request);
        boolean fromAoaMerchant = aoaUtils.isAOAMerchant(merchantId);

        LOGGER.info("Request received to fetch TXN_STATUS, CashierRequestId : {}, TransId : {}, MerchantId : {},"
                + " PaymentMode : {}", cashierRequestId, transId, merchantId, paymentMode);

        try {

            if ((ERequestType.NATIVE_SUBSCRIPTION.getType().equals(requestType) || ERequestType.NATIVE_MF_SIP.getType()
                    .equals(requestType))
                    && SubsPaymentMode.BANK_MANDATE.getePayMethodName().equals(paymentMode)
                    && isJsonTxnStatusReq) {
                String subscriptionId = request.getParameter(SUBSCRIPTION_ID);
                String custId = request.getParameter(CUSTOMER_ID);
                orderId = request.getParameter(ExtraConstants.ORDER_ID);
                TransactionResponse transactionResponse = merchantResponseService.prepareMerchantMandateResponse(
                        subscriptionId, merchantId, custId, orderId);

                Map<String, String> data = new HashMap<>();
                putTransactionResponse(data, transactionResponse);

                return data;
            }

            String alipayMerchantId = validateAndGetAlipayMerchantId(merchantId, cashierRequestId, transId, paymentMode);

            TransactionInfo transInfo = transactionCacheUtils.getTransInfoFromCache(transId);

            LOGGER.debug("Transaction info obtained from cache is : {}", transInfo);

            if (transInfo == null) {
                throw new SessionExpiredException("Session not found for transID - " + transId);
            }
            transInfo.setTransId(transId);

            if (StringUtils.isBlank(destination)) {
                LOGGER.info("Received request from WEB");
                callBackUrl = ConfigurationUtil.getProperty(PropertyConstrant.OFFLINE_STATIC_CALLBACK_URL);

                if (StringUtils.isEmpty(callBackUrl)) {
                    LOGGER.info("Please set reloadable property {}", PropertyConstrant.OFFLINE_STATIC_CALLBACK_URL);
                    throw new TheiaServiceException("CallbackUrl is not configured for offline request");

                }
                callBackUrl = callBackUrl + transInfo.getOrderId();

                transInfo.setCallBackurl(callBackUrl);
            }

            GenericCoreResponseBean<DoPaymentResponse> cashierResponse;
            CashierRequest cashierRequest = null;
            String QrPayResponseKey = "PAY_QR_" + transId;
            String QrTransResponseKey = "TRANS_QR_" + transId;

            if (transInfo.getRequestType() != null && isDynamicQRRequest(transInfo)) {

                /*
                 * Case where, TransactionStatus and PayResultQuery are done at
                 * Biz Layer, Ex- Wallet, PostPaid etc.
                 */

                LOGGER.info("Request received for DYNAMIC_QR");
                CashierPaymentStatus cashierPaymentStatus = null;
                CashierTransactionStatus cashierTransactionStatus = null;

                if (!transInfo.isDynamicQR()) {
                    LOGGER.info("No Dynamic QR Request");
                    QueryPaymentStatus queryPaymentStatus = (QueryPaymentStatus) theiaTransactionalRedisUtil
                            .get(QrPayResponseKey);
                    QueryTransactionStatus queryTransactionStatus = (QueryTransactionStatus) theiaTransactionalRedisUtil
                            .get(QrTransResponseKey);

                    if (queryPaymentStatus != null) {
                        cashierPaymentStatus = new CashierPaymentStatus(queryPaymentStatus);

                        List<PayOption> payOptions = new ArrayList<>();
                        for (com.paytm.pgplus.pgproxycommon.models.PayOption po : queryPaymentStatus.getPayOptions()) {
                            // jira : PGP-18847 : To send bankname and gateway
                            // for
                            // wallet in scan n pay
                            PayOption payOption = new PayOption(po.getPayMethodName(), po.getPayMethodOldName(),
                                    po.getPayAmountCurrencyType(), po.getPayAmountValue(), po.getExtendInfo(),
                                    po.getTransAmountValue(), po.getTransAmountCurrencyType(),
                                    po.getChargeAmountValue());
                            payOptions.add(payOption);
                        }
                        cashierPaymentStatus.setPayOptions(payOptions);
                    } else {
                        LOGGER.info("queryPaymentStatus is null");
                    }
                    if (queryTransactionStatus != null) {
                        cashierTransactionStatus = new CashierTransactionStatus(queryTransactionStatus);
                    } else {
                        LOGGER.info("queryTransactionStatus is null");
                    }
                } else {

                    /*
                     * Case where, TransactionStatus and PayResultQuery are done
                     * at Theia, on TransactionStatusController, Ex- CC/DC/NB.
                     */

                    LOGGER.info("Dynamic QR Request");
                    cashierPaymentStatus = (CashierPaymentStatus) theiaTransactionalRedisUtil.get(QrPayResponseKey);
                    cashierTransactionStatus = (CashierTransactionStatus) theiaTransactionalRedisUtil
                            .get(QrTransResponseKey);
                }

                DoPaymentResponse doPaymentResponse = new DoPaymentResponse();
                doPaymentResponse.setPaymentStatus(cashierPaymentStatus);
                doPaymentResponse.setTransactionStatus(cashierTransactionStatus);

                setPrnValidationStatus(request, cashierTransactionStatus, transInfo);

                cashierResponse = new GenericCoreResponseBean<DoPaymentResponse>(doPaymentResponse);
            } else {

                LOGGER.info("Request received for fetching the transaction status");

                CashierWorkflow cashierWorkflow = CashierWorkflow.getCashierWorkFlowByValue(paymentMode, transInfo);

                if (fromAoaMerchant)
                    cashierWorkflow = CashierWorkflow.ISOCARD;
                if (isJsonTxnStatusReq && !TransactionType.FUND.equals(transInfo.getTransactionType()))
                    cashierWorkflow = CashierWorkflow.GENERIC_PAYMENT_WORKFLOW;

                CashierRequestBuilder cashierRequestBuilder = new CashierRequestBuilder(transId,
                        RequestIdGenerator.generateRequestId(), cashierWorkflow).setFromAoaMerchant(fromAoaMerchant)
                        .setRoute(Routes.PG2).setPaytmMerchantId(merchantId);

                if (transInfo.getRequestType() != null
                        && (LINK_BASED_PAYMENT_INVOICE.equalsIgnoreCase(transInfo.getRequestType()) || LINK_BASED_PAYMENT
                                .equalsIgnoreCase(transInfo.getRequestType()))) {

                    cashierRequestBuilder.setLinkBasedPaymentRequest(true);

                }

                cashierRequestBuilder.setEnvInfo(envInfo);
                setLooperRequest(cashierRequestId, transId, alipayMerchantId, transInfo, cashierRequestBuilder);
                cashierRequest = cashierRequestBuilder.build();

                if (BooleanUtils.isTrue(Boolean.valueOf(request.getParameter(IS_ASYNC_TXN_STATUS_FLOW)))) {
                    cashierRequest.setAsyncTxnStatusFlow(true);
                }

                if (StringUtils.isNotBlank(request.getParameter(ALIPAY_MERCHANT_ID))) {
                    cashierRequest.setDummyAlipayMid(request.getParameter(ALIPAY_MERCHANT_ID));
                }

                modifyCashierRequestForAddMoneyMid(merchantId, transInfo, cashierRequest);
                cashierResponse = paymentServiceImpl.submit(cashierRequest);

            }

            return processCashierResponse(cashierRequest, cashierResponse, transInfo, merchantId, transId,
                    QrPayResponseKey, QrTransResponseKey, callBackUrl, envInfo, request, fromAoaMerchant);

        } catch (Exception e) {
            throw new TheiaServiceException(e);
        }

    }

    private void modifyCashierRequestForAddMoneyMid(String merchantId, TransactionInfo transInfo,
            CashierRequest cashierRequest) {
        if (StringUtils.equals(ConfigurationUtil.getProperty(MP_ADD_MONEY_MID), merchantId)
                && StringUtils.isNotBlank(transInfo.getMid())) {
            cashierRequest.setPaytmMerchantId(transInfo.getMid());
        }
        EXT_LOGGER.customInfo("MID fetched for transaction status: {}", cashierRequest.getPaytmMerchantId());
    }

    public void getAsyncCashierResponse(HttpServletRequest request, GenericCallBack<Map<String, String>> callBack) {
        String callBackUrl = "";
        String merchantId = request.getParameter(ExtraConstants.MERCHANT_ID);
        String cashierRequestId = request.getParameter(ExtraConstants.CASHIER_REQUEST_ID);
        String transId = request.getParameter(ExtraConstants.TRANS_ID);
        String paymentMode = request.getParameter(ExtraConstants.PAYMENT_MODE);
        String destination = request.getParameter(ExtraConstants.RESPONSE_DESTINATION);
        final EnvInfoRequestBean envInfo = EnvInfoUtil.fetchEnvInfo(request);
        boolean fromAoaMerchant = aoaUtils.isAOAMerchant(merchantId);

        LOGGER.info("Request received to fetch TXN_STATUS, CashierRequestId : {}, TransId : {}, MerchantId : {},"
                + " PaymentMode : {}", cashierRequestId, transId, merchantId, paymentMode);

        try {
            String alipayMerchantId = validateAndGetAlipayMerchantId(merchantId, cashierRequestId, transId, paymentMode);

            TransactionInfo transInfo = transactionCacheUtils.getTransInfoFromCache(transId);

            LOGGER.debug("Transaction info obtained from cache is : {}", transInfo);

            if (transInfo == null) {
                throw new SessionExpiredException("Session not found for transID - " + transId);
            }

            transInfo.setTransId(transId);

            if (StringUtils.isBlank(destination)) {
                LOGGER.info("Received request from WEB");
                callBackUrl = ConfigurationUtil.getProperty(PropertyConstrant.OFFLINE_STATIC_CALLBACK_URL);

                if (StringUtils.isEmpty(callBackUrl)) {
                    LOGGER.info("Please set reloadable property {}", PropertyConstrant.OFFLINE_STATIC_CALLBACK_URL);
                    throw new TheiaServiceException("CallbackUrl is not configured for offline request");

                }
                callBackUrl = callBackUrl + transInfo.getOrderId();

                transInfo.setCallBackurl(callBackUrl);
            }

            GenericCoreResponseBean<DoPaymentResponse> cashierResponse;
            // CashierRequest cashierRequest = null;
            String QrPayResponseKey = "PAY_QR_" + transId;
            String QrTransResponseKey = "TRANS_QR_" + transId;

            if (transInfo.getRequestType() != null && isDynamicQRRequest(transInfo)) {

                /*
                 * Case where, TransactionStatus and PayResultQuery are done at
                 * Biz Layer, Ex- Wallet, PostPaid etc.
                 */

                LOGGER.info("Request received for DYNAMIC_QR");
                CashierPaymentStatus cashierPaymentStatus = null;
                CashierTransactionStatus cashierTransactionStatus = null;
                CashierRequest cashierRequest = null;

                if (!transInfo.isDynamicQR()) {
                    LOGGER.info("No Dynamic QR Request");
                    QueryPaymentStatus queryPaymentStatus = (QueryPaymentStatus) theiaTransactionalRedisUtil
                            .get(QrPayResponseKey);
                    QueryTransactionStatus queryTransactionStatus = (QueryTransactionStatus) theiaTransactionalRedisUtil
                            .get(QrTransResponseKey);

                    cashierPaymentStatus = new CashierPaymentStatus(queryPaymentStatus);

                    List<PayOption> payOptions = new ArrayList<>();
                    for (com.paytm.pgplus.pgproxycommon.models.PayOption po : queryPaymentStatus.getPayOptions()) {
                        PayOption payOption = new PayOption(po.getPayMethodName(), po.getPayMethodName(),
                                po.getPayAmountCurrencyType(), po.getPayAmountValue(), po.getExtendInfo(),
                                po.getTransAmountValue(), po.getTransAmountCurrencyType(), po.getChargeAmountValue());
                        payOptions.add(payOption);
                    }
                    cashierPaymentStatus.setPayOptions(payOptions);

                    cashierTransactionStatus = new CashierTransactionStatus(queryTransactionStatus);
                } else {

                    /*
                     * Case where, TransactionStatus and PayResultQuery are done
                     * at Theia, on TransactionStatusController, Ex- CC/DC/NB.
                     */

                    // LOGGER.info("Dynamic QR Request");
                    cashierPaymentStatus = (CashierPaymentStatus) theiaTransactionalRedisUtil.get(QrPayResponseKey);
                    cashierTransactionStatus = (CashierTransactionStatus) theiaTransactionalRedisUtil
                            .get(QrTransResponseKey);
                }

                DoPaymentResponse doPaymentResponse = new DoPaymentResponse();
                doPaymentResponse.setPaymentStatus(cashierPaymentStatus);
                doPaymentResponse.setTransactionStatus(cashierTransactionStatus);

                setPrnValidationStatus(request, cashierTransactionStatus, transInfo);

                cashierResponse = new GenericCoreResponseBean<DoPaymentResponse>(doPaymentResponse);
                Map<String, String> data = processCashierResponse(cashierRequest, cashierResponse, transInfo,
                        merchantId, transId, QrPayResponseKey, QrTransResponseKey, callBackUrl, envInfo, request,
                        fromAoaMerchant);
                callBack.processResponse(data);
            } else {

                LOGGER.info("Request received for fetching the transaction status");

                CashierWorkflow cashierWorkflow = CashierWorkflow.getCashierWorkFlowByValue(paymentMode, transInfo);

                if (fromAoaMerchant)
                    cashierWorkflow = CashierWorkflow.ISOCARD;

                CashierRequestBuilder cashierRequestBuilder = new CashierRequestBuilder(transId,
                        RequestIdGenerator.generateRequestId(), cashierWorkflow).setFromAoaMerchant(fromAoaMerchant);

                if (transInfo.getRequestType() != null
                        && (LINK_BASED_PAYMENT_INVOICE.equalsIgnoreCase(transInfo.getRequestType()) || LINK_BASED_PAYMENT
                                .equalsIgnoreCase(transInfo.getRequestType()))) {

                    cashierRequestBuilder.setLinkBasedPaymentRequest(true);

                }

                cashierRequestBuilder.setEnvInfo(envInfo);
                setLooperRequest(cashierRequestId, transId, alipayMerchantId, transInfo, cashierRequestBuilder);
                CashierRequest cashierRequest = cashierRequestBuilder.build();
                final String effectiveCallBackURL = callBackUrl;

                paymentServiceImpl.submitAsync(
                        cashierRequest,
                        cashierResponse1 -> {
                            Map<String, String> data = null;
                            try {
                                data = processCashierResponse(cashierRequest, cashierResponse1, transInfo, merchantId,
                                        transId, QrPayResponseKey, QrTransResponseKey, effectiveCallBackURL, envInfo,
                                        request, fromAoaMerchant);
                            } catch (Exception e) {
                                LOGGER.error("Exception occured while process Cashier Response.", e);
                            }
                            callBack.processResponse(data);
                        });
            }

        } catch (Exception e) {
            throw new TheiaServiceException(e);
        }

    }

    private Map<String, String> processCashierResponse(CashierRequest cashierRequest,
            GenericCoreResponseBean<DoPaymentResponse> cashierResponse, TransactionInfo transInfo, String merchantId,
            String transId, String QrPayResponseKey, String QrTransResponseKey, String callBackUrl,
            EnvInfoRequestBean envInfo, HttpServletRequest request, boolean fromAoaMerchant)
            throws PaytmValidationException, ServletException, IOException, FacadeCheckedException {
        if ((cashierResponse == null) || (cashierResponse.getResponse() == null)) {
            throw new TheiaServiceException("Cashier Response is null");
        }

        Map<String, String> data = new HashMap<>();
        data.put(ExtraConstants.REQUEST_TYPE, transInfo.getRequestType());
        if (ff4jUtils.isFeatureEnabled(THEIA_VALIDATE_HEADER_WORKFLOW_ENABLE, false)
                && !validationService.validateHeaderWorkflow(request.getParameter(HEADER_WORKFLOW))) {
            throw new TheiaServiceException("CRITICAL_ERROR : Invalid HEADER_WORKFLOW : "
                    + request.getParameter(HEADER_WORKFLOW));
        }
        data.put(HEADER_WORKFLOW, request.getParameter(HEADER_WORKFLOW));
        setPaymentRequestFlowInData(data, cashierResponse);

        setDataForNativeDirectBankPage(cashierResponse, request, data);
        setHeaderWorkFlow(data, cashierResponse);

        // Payment Retry Logic
        if ((cashierResponse.getResponse().getPaymentStatus() != null)
                && PaymentStatus.FAIL.name().equalsIgnoreCase(
                        cashierResponse.getResponse().getPaymentStatus().getPaymentStatusValue())
                && cashierResponse.getResponse().getPaymentStatus().isPaymentRetryPossible()) {
            processForFailedPaymentStatus(cashierRequest, cashierResponse, data, transInfo, merchantId, transId,
                    QrPayResponseKey, QrTransResponseKey, callBackUrl, envInfo, fromAoaMerchant, request);

        } else if (isOfflinePPBLRequest(cashierResponse, transInfo)) {
            processForOfflineProcessingPaymentStatus(cashierResponse, data, merchantId, transId, fromAoaMerchant);

        } else {
            LOGGER.info("processing for payment status is success");
            generateDynamicQRData(transInfo, cashierResponse, QrPayResponseKey, QrTransResponseKey, data, callBackUrl);
        }

        if (cashierRequest != null
                && cashierRequest.isLinkBasedPaymentRequest()
                || (isLinkBasedPaymentRequest(cashierResponse) && (StringUtils.isBlank(data
                        .get(RetryConstants.RETRY_INITIATED)) || (StringUtils.isNotBlank(data
                        .get(RetryConstants.RETRY_INITIATED)) && !Boolean.TRUE.toString().equals(
                        data.get(RetryConstants.RETRY_INITIATED)))))) {
            if (cashierResponse.getResponse().getPaymentStatus().getPaymentStatusValue()
                    .equalsIgnoreCase(PaymentStatus.FAIL.name())) {
                LOGGER.info("Closing order for link based payment in fail scenario for transID = {}", transId);
                closeOrder(merchantId, transId, false);
            }

            String offlineFlow = cashierResponse.getResponse().getTransactionStatus().getExtendInfo().get(OFFLINE_FLOW);
            if (cashierRequest != null
                    && !ERequestType.OFFLINE.getType().equals(transInfo.getRequestType())
                    && !(ERequestType.NATIVE.getType().equals(transInfo.getRequestType()) && Boolean
                            .valueOf(offlineFlow))) {

                setRequestAttributesForLinkPayments(request, cashierRequest, cashierResponse);
                data.put(LINK_BASED_PAYMENT, "true");
            }
        }

        return data;
    }

    private void setHeaderWorkFlow(Map<String, String> data, GenericCoreResponseBean<DoPaymentResponse> cashierResponse) {
        if (cashierResponse != null && cashierResponse.getResponse() != null) {
            if (cashierResponse.getResponse().getTransactionStatus() != null
                    && MapUtils.isNotEmpty(cashierResponse.getResponse().getTransactionStatus().getExtendInfo())) {
                cashierResponse.getResponse().getTransactionStatus().getExtendInfo()
                        .put(HEADER_WORKFLOW, data.get(HEADER_WORKFLOW));
            }
            if (cashierResponse.getResponse().getFundOrderStatus() != null
                    && MapUtils.isNotEmpty(cashierResponse.getResponse().getFundOrderStatus().getExtendInfo())) {
                cashierResponse.getResponse().getFundOrderStatus().getExtendInfo()
                        .put(HEADER_WORKFLOW, data.get(HEADER_WORKFLOW));
            }
        }
    }

    private void setDataForNativeDirectBankPage(GenericCoreResponseBean<DoPaymentResponse> cashierResponse,
            HttpServletRequest request, Map<String, String> data) {
        boolean nativeDirectBankPageRetry = BooleanUtils.toBoolean(request.getParameter(DIRECT_BANK_PAGE_RETRY));

        /*
         * This flag is used for regulation of merchantRetry
         */
        if (nativeDirectBankPageRetry && isInvalidOtpCase(cashierResponse)) {
            data.put(DIRECT_BANK_PAGE_INVALID_OTP, Boolean.TRUE.toString());
        }

        if (StringUtils.equals(Boolean.TRUE.toString(), request.getParameter(DIRECT_BANK_PAGE_SUBMIT_REQUEST))) {
            data.put(DIRECT_BANK_PAGE_SUBMIT_REQUEST, Boolean.TRUE.toString());
        }
    }

    private boolean isLinkBasedPaymentRequest(GenericCoreResponseBean<DoPaymentResponse> cashierResponse) {
        if (cashierResponse.getResponse().getTransactionStatus() != null
                && cashierResponse.getResponse().getTransactionStatus().getExtendInfo() != null
                && cashierResponse.getResponse().getTransactionStatus().getExtendInfo().get("merchantUniqueReference") != null) {
            String merchUniqRef = cashierResponse.getResponse().getTransactionStatus().getExtendInfo()
                    .get("merchantUniqueReference");
            if (StringUtils.isNotEmpty(merchUniqRef)
                    && (merchUniqRef.startsWith(ExtraConstants.LINK_ID_PREFIX) || merchUniqRef
                            .startsWith(ExtraConstants.INVOICE_ID_PREFIX))) {
                return true;
            }
        }
        return false;
    }

    private boolean isOfflinePPBLRequest(GenericCoreResponseBean<DoPaymentResponse> cashierResponse,
            TransactionInfo transInfo) {
        if ((cashierResponse.getResponse().getPaymentStatus() != null)
                && !PaymentStatus.SUCCESS.name().equalsIgnoreCase(
                        cashierResponse.getResponse().getPaymentStatus().getPaymentStatusValue())
                && cashierResponse.getResponse().getTransactionStatus() != null
                && ((Boolean.TRUE.toString()).equals(cashierResponse.getResponse().getTransactionStatus()
                        .getExtendInfo().get("offlineFlow")) || TheiaConstant.RequestTypes.OFFLINE.equals(transInfo
                        .getRequestType()))
                && (((EPayMethod.PPBL.toString()).equals(cashierResponse.getResponse().getPaymentStatus()
                        .getPayOptions().get(0).getExtendInfo().get("bankAbbr"))) || (EPayMethod.PPBL.toString())
                        .equals(cashierResponse.getResponse().getPaymentStatus().getPayOptions().get(0).getExtendInfo()
                                .get("instId"))) && !isLinkBasedPaymentRequest(cashierResponse)) {

            return true;
        }
        return false;

    }

    private void processForOfflineProcessingPaymentStatus(GenericCoreResponseBean<DoPaymentResponse> cashierResponse,
            Map<String, String> data, String merchantId, String transId, boolean fromAoaMerchant) {
        LOGGER.info("Closing Pending PPBL Offline Transaction");
        MappingMerchantData theiaMerchantMappingResponse = merchantMappingService.getMappingMerchantData(merchantId);
        WorkFlowTransactionBean transactionBean = new WorkFlowTransactionBean(new WorkFlowRequestBean());
        transactionBean.getWorkFlowBean().setAlipayMID(theiaMerchantMappingResponse.getAlipayId());
        transactionBean.setTransID(transId);
        transactionBean.getWorkFlowBean().setFromAoaMerchant(fromAoaMerchant);
        transactionBean.getWorkFlowBean().setPaytmMID(merchantId);
        RouteResponse routeResponse = routerUtil.getRouteResponse(merchantId, null, transId, null, "closeOrder");
        transactionBean.getWorkFlowBean().setRoute(routeResponse.getName());
        if (Routes.PG2.equals(routeResponse.getName())) {
            if (StringUtils.equals(ConfigurationUtil.getProperty(MP_ADD_MONEY_MID), merchantId)) {
                transactionBean.getWorkFlowBean().setPaytmMID(routeResponse.getMid());
            }
        }
        PaymentStatus paymentStatus = workFlowHelper.triggerCloseOrderPulses(transactionBean);
        cashierResponse.getResponse().getPaymentStatus().setPaymentStatusValue(paymentStatus.name());
        getResponseForMerchantRedirect(cashierResponse, data);

    }

    private void processForFailedPaymentStatus(CashierRequest cashierRequest,
            GenericCoreResponseBean<DoPaymentResponse> cashierResponse, Map<String, String> data,
            TransactionInfo transInfo, String merchantId, String transId, String QrPayResponseKey,
            String QrTransResponseKey, String callBackUrl, EnvInfoRequestBean envInfo, boolean fromAoaMerchant,
            HttpServletRequest request) throws PaytmValidationException, FacadeCheckedException {

        LOGGER.info("processing for payment status is fail");

        CashierTransactionStatus cashierTransactionStatus = cashierResponse.getResponse().getTransactionStatus();
        CashierFundOrderStatus cashierFundOrderStatus = cashierResponse.getResponse().getFundOrderStatus();
        boolean isFundOrder = TransactionType.ACQUIRING.equals(transInfo.getTransactionType()) ? false : true;
        boolean isEnhancedNative = theiaResponseGenerator.checkIfEnhanceCashierPageRequest(isFundOrder,
                cashierTransactionStatus, cashierFundOrderStatus);

        if ((ERequestType.UNI_PAY.name().equals(transInfo.getRequestType()) && isEnhancedNative)
                || ERequestType.SEAMLESS.name().equals(transInfo.getRequestType())) {

            if (cashierTransactionStatus != null
                    && cashierTransactionStatus.getExtendInfo() != null
                    && cashierTransactionStatus
                            .getExtendInfo()
                            .get(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.DUMMY_MERCHANT_ID) != null) {
                merchantId = cashierTransactionStatus.getExtendInfo().get(
                        com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.DUMMY_MERCHANT_ID);
            }

            closeOrder(merchantId, transId, fromAoaMerchant);

            cashierResponse = paymentServiceImpl.submit(cashierRequest);

            getResponseForMerchantRedirect(cashierResponse, data);

        } else if (!"DYNAMIC_QR_TXN_STATUS".equals(transInfo.getRequestType())
                && isRetryPossibleForOrder(cashierFundOrderStatus, cashierTransactionStatus, transInfo)) {
            LOGGER.info("Payment Retry is possible");
            Map<String, String> userRetryMetaData = new LinkedHashMap<>();
            userRetryMetaData.put(ExtraConstants.REQUEST_TYPE, transInfo.getRequestType());
            String paymentMode = request.getParameter(ExtraConstants.PAYMENT_MODE);
            if (paymentMode != null && paymentMode.equals(PaymentTypeIdEnum.UPI.getValue())) {
                paymentMode = ExtraConstants.UPI_COLLECT;
            }
            userRetryMetaData.put(ExtraConstants.PAYMENT_MODE, paymentMode);
            String mid = TransactionType.ACQUIRING.equals(transInfo.getTransactionType()) ? cashierTransactionStatus
                    .getExtendInfo().get(RetryConstants.PAYTM_MID) : cashierFundOrderStatus.getExtendInfo().get(
                    RetryConstants.PAYTM_MID);

            if (TransactionType.ACQUIRING.equals(transInfo.getTransactionType())
                    && cashierTransactionStatus.getExtendInfo().get(RetryConstants.ACTUAL_MID) != null) {
                mid = cashierTransactionStatus.getExtendInfo().get(RetryConstants.ACTUAL_MID);
            }

            String orderId = TransactionType.ACQUIRING.equals(transInfo.getTransactionType()) ? cashierTransactionStatus
                    .getMerchantTransId() : cashierFundOrderStatus.getRequestId();

            if (transInfo.getRequestType() != null
                    && (NATIVE.equalsIgnoreCase(transInfo.getRequestType()) || UNI_PAY.equalsIgnoreCase(transInfo
                            .getRequestType()))) {

                if (!isDirectBankSubmitRequest(data)) {
                    String txnToken = retryServiceHelper.getTxnToken(mid, orderId);
                    if (StringUtils.isNotBlank(txnToken)) {
                        nativeDirectBankPageHelper.resetCounts(txnToken);
                        LOGGER.info("Resetting directBankPage retry counts in case of cancel Payment");
                    }
                }

                Integer currentRetryCount = null;

                /*
                 * In case of retry on nativeDirectBankPage, we do not increase
                 * bankRetryCount
                 */
                boolean transactionStatusJsonRequest = Boolean.valueOf(request
                        .getParameter(ExtraConstants.TRANSACTION_STATUS_JSON_REQUEST));
                boolean upiIntentPayment = transInfo.isUPIIntentPayment();
                if (cashierRequest.isAsyncTxnStatusFlow() || upiIntentPayment
                        || (!isDirectBankPageRetry(data) && !transactionStatusJsonRequest)) {
                    currentRetryCount = increaseRetryCountForBankSideFails(orderId, mid);
                }

                if (currentRetryCount == null) {
                    currentRetryCount = 0;
                }
                boolean nativePaymentRetryFlag = retryServiceHelper.checkNativePaymentRetry(mid, orderId);
                // Check if Enhanced Native flow or not
                // Get the enhancedCashierPage response from cache.
                EnhanceCashierPageCachePayload enhanceCashierPageCachePayload = (EnhanceCashierPageCachePayload) nativeSessionUtil
                        .getKey(enhancedCashierPageServiceHelper.fetchRedisKey(mid, orderId));
                if (null != enhanceCashierPageCachePayload
                        && null != enhanceCashierPageCachePayload.getEnhancedCashierPage()
                        && (isDirectBankPageRetry(data) || nativePaymentRetryFlag)
                        && !isAppInvokedTransaction(enhanceCashierPageCachePayload.getEnhancedCashierPage(), mid,
                                orderId)) {
                    // This is enhanced native flow and retry is
                    // allowed.
                    userRetryMetaData.put(ExtraConstants.REQUEST_TYPE, ExtraConstants.NATIVE_ENHANCED);
                    EventUtils.pushTheiaEvents(mid, orderId, EventNameEnum.USER_RETRY_INITIATED, userRetryMetaData);
                    // Retry PUSH_APP data is inserted in below method in
                    // responsePage field of data map
                    generateNativeEnhanceResponse(request, cashierResponse, data, mid, orderId, transId,
                            enhanceCashierPageCachePayload, currentRetryCount);

                    // generate txn response for enhanceNativeDirectOTP case
                    if (isDirectBankSubmitRequest(data)) {
                        generateResponseForNativeMerchantRedirect(cashierResponse, data, mid, orderId, envInfo,
                                ExternalTransactionStatus.TXN_FAILURE, isEnhancedNative);
                    }
                } else {
                    generateResponseForNativeMerchantRedirect(cashierResponse, data, mid, orderId, envInfo,
                            ExternalTransactionStatus.TXN_FAILURE, isEnhancedNative);
                    if (nativePaymentRetryFlag) {
                        EventUtils.pushTheiaEvents(mid, orderId, EventNameEnum.USER_RETRY_INITIATED, userRetryMetaData);
                    }
                }
            } else if (retryServiceHelper.checkPaymentRetry(mid, orderId, cashierResponse, transId, isFundOrder,
                    envInfo)) {
                EventUtils.pushTheiaEvents(mid, orderId, EventNameEnum.USER_RETRY_INITIATED, userRetryMetaData);
                setRetryConfigData(cashierResponse, data, mid, orderId);
            } else {
                getResponseForMerchantRedirect(cashierResponse, data);
            }
        } else {
            LOGGER.info("Retry is not possible");
            generateDynamicQRData(transInfo, cashierResponse, QrPayResponseKey, QrTransResponseKey, data, callBackUrl);
        }

    }

    private boolean isAppInvokedTransaction(EnhancedCashierPage enhancedCashierPage, String mid, String orderId) {
        return null != enhancedCashierPage
                && !EventNameEnum.ONLINE_ENHANCED_PAYMENT_REQUEST.getEventName().equalsIgnoreCase(
                        nativeSessionUtil.getFlowTypeOnTxnToken(null, mid, orderId));
    }

    private void closeOrder(String merchantId, String transId, boolean fromAoaMerchant) {

        MappingMerchantData theiaMerchantMappingResponse = merchantMappingService.getMappingMerchantData(merchantId);

        RouteResponse routeResponse = routerUtil.getRouteResponse(merchantId, null, transId, null, "closeOrder");
        BizCancelOrderRequest cancelAcquiringOrderRequest = new BizCancelOrderRequest(
                theiaMerchantMappingResponse.getAlipayId(), transId, "Max payment retry count limit reached",
                fromAoaMerchant, merchantId, routeResponse.getName());
        if (Routes.PG2.equals(routeResponse.getName())) {
            if (StringUtils.equals(ConfigurationUtil.getProperty(MP_ADD_MONEY_MID), merchantId)) {
                cancelAcquiringOrderRequest.setPaytmMerchantId(routeResponse.getMid());
            }
        }

        orderServiceImpl.closeOrder(cancelAcquiringOrderRequest);
    }

    private void setPrnValidationStatus(HttpServletRequest request, CashierTransactionStatus cashierTransactionStatus,
            final TransactionInfo transInfoCache) {

        if (!DYNAMIC_QR_2FA.equalsIgnoreCase(transInfoCache.getRequestType()))
            return;

        com.paytm.pgplus.theia.sessiondata.TransactionInfo transactionInfo = theiaSessionDataService
                .getTxnInfoFromSession(request);

        if (Objects.isNull(transactionInfo))
            return;

        cashierTransactionStatus.setPrnValidationStatusSuccess(transactionInfo.isPrnValidationSuccessful());
    }

    private boolean isDynamicQRRequest(TransactionInfo transInfo) {
        return DYNAMIC_QR.equalsIgnoreCase(transInfo.getRequestType())
                || DYNAMIC_QR_2FA.equalsIgnoreCase(transInfo.getRequestType());
    }

    private void generateResponseForNativeMerchantRedirect(GenericCoreResponseBean<DoPaymentResponse> cashierResponse,
            Map<String, String> data, String mid, String orderId, EnvInfoRequestBean envInfo,
            ExternalTransactionStatus status, boolean isEnhancedNative) {
        String txnToken = retryServiceHelper.getTxnToken(mid, orderId);
        StringBuilder callbackUrlBuilder = null;
        if (cashierResponse.getResponse().getTransactionStatus() != null) {
            callbackUrlBuilder = new StringBuilder(theiaResponseGenerator.getCallbackUrlFromExtendInfo(cashierResponse
                    .getResponse().getTransactionStatus().getExtendInfo(), status));
        }
        if (StringUtils.isBlank(callbackUrlBuilder)) {
            if (StringUtils.isBlank(txnToken)) {
                // session has expired hence txnToken will be null
                throw com.paytm.pgplus.theia.offline.exceptions.SessionExpiredException.getException();
            }
            callbackUrlBuilder = new StringBuilder(nativeRetryUtil.getCallbackUrl(txnToken));
        }
        if (callbackUrlBuilder.indexOf("?") != -1) {
            callbackUrlBuilder.append("&");
        } else {
            callbackUrlBuilder.append("?");
        }
        boolean retryAllowed = retryServiceHelper.checkNativePaymentRetry(mid, orderId);
        callbackUrlBuilder.append("retryAllowed=").append(retryAllowed);

        callbackUrlBuilder.append("&errorMessage=").append(
                theiaResponseGenerator.getErrorMsgForNativePaymentFailure(cashierResponse.getResponse()));
        callbackUrlBuilder.append("&errorCode=").append(
                theiaResponseGenerator.getErrorCodeForNativePaymentFaliure(cashierResponse.getResponse()));
        if (!isDirectBankPageRetry(data) && StringUtils.isNotBlank(txnToken)) {
            LOGGER.info("is enhanced workflow :{}", isEnhancedNative);
            Optional.ofNullable(cashierResponse.getResponse().getPaymentStatus())
                    .map(CashierPaymentStatus::getExtendInfo)
                    .map(extendInfo -> CHECKOUT.equals(extendInfo.get(ExtraConstants.WORKFLOW)))
                    .ifPresent(workflow -> LOGGER.info("is checkout flow : {}", true));

            nativeRetryUtil.invalidateSession(txnToken, retryAllowed, mid, orderId, envInfo);

        }
        getResponseForMerchantRedirect(cashierResponse, data, callbackUrlBuilder.toString());

        if (!retryAllowed) {
            invalidateNativeFlowRedisData(txnToken, mid, orderId);
        }
    }

    public Integer increaseRetryCountForBankSideFails(String orderId, String mid) {
        String txnToken = retryServiceHelper.getTxnToken(mid, orderId);
        return nativeRetryUtil.increaseRetryCount(txnToken, mid, orderId);

    }

    public void getAsyncCashierResponseWrapper(HttpServletRequest request, HttpServletResponse response,
            GenericCallBack<Map<String, String>> callBack) throws ServletException, IOException {

        getAsyncCashierResponse(
                request,
                data -> {
                    if (data != null && data.get(LINK_BASED_PAYMENT) != null) {
                        LOGGER.info("Forwarding request to link Payment status page. Request already dispatch to jsp page, no further dispatch to Retry page");
                        data.put(RetryConstants.IS_REQUEST_ALREADY_DISPATCH, RetryConstants.YES);
                        try {
                            request.getRequestDispatcher(
                                    VIEW_BASE + theiaViewResolverService.returnLinkPaymentStatusPage(request) + ".jsp")
                                    .forward(request, response);
                        } catch (Exception e) {
                            LOGGER.error("Exception occured while forwarding servlet request.", e);
                        }
                        callBack.processResponse(data);
                    } else {
                        callBack.processResponse(data);
                    }
                });

    }

    public Map<String, String> getCashierResponseWrapper(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Map<String, String> data = getCashierResponse(request);
        return data;
    }

    private void setRequestAttributesForLinkPayments(HttpServletRequest request, CashierRequest cashierRequest,
            GenericCoreResponseBean<DoPaymentResponse> paymentResponse) throws ServletException, IOException {

        if (paymentResponse != null && paymentResponse.getResponse() != null
                && paymentResponse.getResponse().getPaymentStatus() != null) {
            CashierPaymentStatus paymentStatus = paymentResponse.getResponse().getPaymentStatus();
            CashierTransactionStatus transactionStatus = paymentResponse.getResponse().getTransactionStatus();
            LOGGER.info("Generating final txn status page for Link based payments for transaction ID = {}",
                    paymentStatus.getTransId());

            if (transactionStatus != null) {
                if (theiaResponseGenerator
                        .getIsLinkInvoicePayment(paymentResponse.getResponse().getTransactionStatus())) {
                    request.setAttribute(TheiaConstant.ResponseConstants.LINK_TYPE,
                            TheiaConstant.LinkRiskParams.LINK_INVOICE_TYPE);
                }
                request.setAttribute(ORDER_ID, transactionStatus.getMerchantTransId());
            }

            request.setAttribute(PAYMENT_STATUS, paymentStatus.getPaymentStatusValue() != null ? paymentStatus
                    .getPaymentStatusValue().toUpperCase() : null);
            Date date = paymentStatus.getPaidTime();
            if (date == null) {
                date = transactionStatus != null ? transactionStatus.getCreatedTime() : null;
            }
            request.setAttribute(TXN_DATE, LinkBasedPaymentHelper.getLinkPaymentStatusDate(date));
            request.setAttribute(TRANSACTION_ID, paymentStatus.getTransId());
            request.setAttribute(TXN_AMOUNT,
                    AmountUtils.getTransactionAmountInRupee(paymentStatus.getTransAmountValue()));
            request.setAttribute(SHOW_VIEW_FLAG, PAYMENT_SCREEN);
            LOGGER.info("Fetching merchant info from Redis for Link based payment for  = {}",
                    cashierRequest.getAcquirementId());
            LinkBasedMerchantInfo linkBasedMerchantInfo = (LinkBasedMerchantInfo) theiaTransactionalRedisUtil
                    .get(LINK_BASED_KEY + cashierRequest.getAcquirementId());

            if (linkBasedMerchantInfo != null) {
                request.setAttribute(MERCHANT_NAME, linkBasedMerchantInfo.getMerchantName());
                request.setAttribute(MERCHANT_IMAGE, linkBasedMerchantInfo.getMerchantImage());
            }

            TransactionResponse tranResponse = new TransactionResponse();
            if (transactionStatus != null
                    && AcquirementStatusType.CLOSED.toString().equals(transactionStatus.getStatusDetailType())) {
                theiaResponseGenerator.setResponseMessageAndCode(tranResponse,
                        TheiaConstant.ResponseConstants.ResponseCodes.PAGE_OPEN_RESPONSE_CODE, null, paymentResponse
                                .getResponse().getPaymentStatus());
            } else {
                theiaResponseGenerator.setResponseMessageAndCode(tranResponse, paymentResponse.getResponse()
                        .getPaymentStatus().getPaytmResponseCode(), null, paymentResponse.getResponse()
                        .getPaymentStatus());
            }
            request.setAttribute(ERROR_CODE, tranResponse.getResponseCode());
            request.setAttribute(ERROR_MESSAGE, tranResponse.getResponseMsg());
        }

    }

    public Map<String, String> getUpiCashierResponse(HttpServletRequest request) {

        String merchantId = null;
        String cashierRequestId = null;
        String transId = null;
        String paymentMode = null;
        UpiTransactionStatusRequest upiTransactionStatusRequest = null;
        try {
            if (MediaType.APPLICATION_JSON_VALUE.equals(request.getHeader(HttpHeaders.CONTENT_TYPE))
                    && !request.getRequestURI().contains(V1_TRANSACTION_STATUS_URL)) {
                String requestData = IOUtils.toString(request.getInputStream(), Charsets.UTF_8.name());
                if (StringUtils.isEmpty(requestData))
                    throw new TheiaServiceException("Invalid request, expected json request data is missing");
                upiTransactionStatusRequest = JsonMapper
                        .mapJsonToObject(requestData, UpiTransactionStatusRequest.class);
                if (null == upiTransactionStatusRequest || null == upiTransactionStatusRequest.getHead()
                        || null == upiTransactionStatusRequest.getBody())
                    throw new TheiaServiceException("Invalid request, expected json request data is missing");
                else {
                    merchantId = upiTransactionStatusRequest.getBody().getMid();
                    cashierRequestId = upiTransactionStatusRequest.getBody().getCashierRequestId();
                    transId = upiTransactionStatusRequest.getBody().getTransId();
                    paymentMode = upiTransactionStatusRequest.getBody().getPaymentMode();
                }
            } else {
                merchantId = request.getParameter(ExtraConstants.MERCHANT_ID);
                cashierRequestId = request.getParameter(ExtraConstants.CASHIER_REQUEST_ID);
                transId = request.getParameter(ExtraConstants.TRANS_ID);
                paymentMode = request.getParameter(ExtraConstants.PAYMENT_MODE);
            }
            final EnvInfoRequestBean envInfo = EnvInfoUtil.fetchEnvInfo(request);
            String paymentMid = null;
            boolean fromAoaMerchant = aoaUtils.isAOAMerchant(merchantId);
            LOGGER.info("Request received to fetch UPI TXN_STATUS, CashierRequestId : {},", cashierRequestId);

            String alipayMerchantId = validateAndGetAlipayMerchantId(merchantId, cashierRequestId, transId, paymentMode);
            paymentMid = nativeSessionUtil.getFieldValue(transId, "PaymentMid");
            if (StringUtils.isNotBlank(paymentMid)) {
                alipayMerchantId = paymentMid;
            }
            TransactionInfo transInfo = transactionCacheUtils.getTransInfoFromCache(transId);

            LOGGER.info("Transaction info obtained from cache is : {}", transInfo);

            CashierRequestBuilder cashierRequestBuilder = new CashierRequestBuilder(transId,
                    RequestIdGenerator.generateRequestId(), CashierWorkflow.getCashierWorkFlowByValue(paymentMode,
                            transInfo)).setRoute(Routes.PG2).setPaytmMerchantId(merchantId);
            cashierRequestBuilder.setFromAoaMerchant(fromAoaMerchant);
            setLooperRequest(cashierRequestId, transId, alipayMerchantId, transInfo, cashierRequestBuilder);
            CashierRequest cashierRequest = cashierRequestBuilder.build();
            if (BooleanUtils.isTrue(Boolean.valueOf(request.getParameter(IS_ASYNC_TXN_STATUS_FLOW)))
                    || (null != upiTransactionStatusRequest && null != upiTransactionStatusRequest.getBody() && upiTransactionStatusRequest
                            .getBody().isAsyncTxnStatusFlow())) {
                cashierRequest.setAsyncTxnStatusFlow(true);
            }
            boolean shouldUpiPollingStop = false;

            modifyCashierRequestForAddMoneyMid(merchantId, transInfo, cashierRequest);
            GenericCoreResponseBean<DoPaymentResponse> cashierResponse = paymentServiceImpl.submit(cashierRequest);

            if ((cashierResponse == null) || (cashierResponse.getResponse() == null)) {
                throw new TheiaServiceException("Cashier Response is null");
            }

            Map<String, String> data = new HashMap<>();

            if ((cashierResponse.getResponse().getPaymentStatus() != null)
                    && PaymentStatus.FAIL.name().equalsIgnoreCase(
                            cashierResponse.getResponse().getPaymentStatus().getPaymentStatusValue())
                    && cashierResponse.getResponse().getPaymentStatus().isPaymentRetryPossible()) {

                CashierTransactionStatus cashierTransactionStatus = cashierResponse.getResponse()
                        .getTransactionStatus();

                if ((transInfo != null)
                        && TransactionType.ACQUIRING.equals(transInfo.getTransactionType())
                        && (cashierTransactionStatus != null)
                        && AcquirementStatusType.INIT.getStatusType().equalsIgnoreCase(
                                cashierTransactionStatus.getStatusDetailType())) {

                    String mid = cashierTransactionStatus.getExtendInfo().get(RetryConstants.PAYTM_MID);
                    if (cashierTransactionStatus.getExtendInfo().get(RetryConstants.ACTUAL_MID) != null) {
                        mid = cashierTransactionStatus.getExtendInfo().get(RetryConstants.ACTUAL_MID);
                    }
                    String orderId = cashierTransactionStatus.getMerchantTransId();
                    if (transInfo.getRequestType() != null
                            && (NATIVE.equalsIgnoreCase(transInfo.getRequestType()) || UNI_PAY
                                    .equalsIgnoreCase(transInfo.getRequestType()))) {
                        LOGGER.info("Request received to fetch UPI TXN_STATUS FOR REQUEST TYPE NATIVE");
                        if (retryServiceHelper.checkNativePaymentRetry(mid, orderId)) {
                            EXT_LOGGER.customInfo("Retry possible for failed upi transaction");
                            setRetryConfigData(cashierResponse, data, mid, orderId);
                        }

                    } else if (retryServiceHelper.checkPaymentRetry(mid, orderId, cashierResponse, transId, false,
                            envInfo)) {
                        setRetryConfigData(cashierResponse, data, mid, orderId);
                    }
                }
                shouldUpiPollingStop = true;
            } else {
                if (PaymentStatus.SUCCESS.name().equalsIgnoreCase(
                        cashierResponse.getResponse().getPaymentStatus().getPaymentStatusValue())
                        || PaymentStatus.FAIL.name().equalsIgnoreCase(
                                cashierResponse.getResponse().getPaymentStatus().getPaymentStatusValue())) {
                    shouldUpiPollingStop = true;
                }
            }
            if (StringUtils.isNotBlank(request.getParameter(TXN_TOKEN))) {
                data.put(TXN_TOKEN, request.getParameter(TXN_TOKEN));
            } else if (null != upiTransactionStatusRequest && null != upiTransactionStatusRequest.getBody()
                    && StringUtils.isNotBlank(upiTransactionStatusRequest.getBody().getTxnToken())) {
                data.put(TXN_TOKEN, upiTransactionStatusRequest.getBody().getTxnToken());
            }
            setUPIPollStatus(shouldUpiPollingStop ? STOP_POLLING : POLL_AGAIN, data);
            return data;
        } catch (Exception e) {
            throw new TheiaServiceException(e);
        }

    }

    private boolean isRetryPossibleForOrder(CashierFundOrderStatus cashierFundOrderStatus,
            CashierTransactionStatus cashierTransactionStatus, TransactionInfo transInfo) {
        if (transInfo != null && transInfo.getTransactionType() != null) {
            switch (transInfo.getTransactionType()) {
            case ACQUIRING:
                return cashierTransactionStatus != null
                        && AcquirementStatusType.INIT.getStatusType().equalsIgnoreCase(
                                cashierTransactionStatus.getStatusDetailType());
            case FUND:
                if (cashierFundOrderStatus != null
                        && StringUtils.isNotBlank(cashierFundOrderStatus.getFundOrderStatus())) {
                    return FundOrderStatus.INIT.name().equals(cashierFundOrderStatus.getFundOrderStatus())
                            || FundOrderStatus.PAY_ACCEPT.name().equals(cashierFundOrderStatus.getFundOrderStatus());
                }
            }
        }
        return false;
    }

    private String validateAndGetAlipayMerchantId(String merchantId, String cashierRequestId, String transId,
            String paymentMode) {
        // Validating request data
        validateRequestData(merchantId, cashierRequestId, transId, paymentMode);

        String alipayMerchantId = StringUtils.EMPTY;

        try {
            MappingMerchantData mappingMerchantData = merchantMappingService.getMappingMerchantData(merchantId);

            if (mappingMerchantData != null) {
                alipayMerchantId = mappingMerchantData.getAlipayId();
            }
        } catch (Exception e) {
            LOGGER.error(
                    "Exception occurred while fetching Merchant mapping data from Redis/Mapping Service for merchantId : {}",
                    merchantId, e);
        }
        if (StringUtils.isBlank(alipayMerchantId)) {
            throw new TheiaServiceException("Could not map merchant id, due to merchant id is null or blank ");
        }
        return alipayMerchantId;
    }

    /**
     * @param cashierResponse
     * @param data
     * @param mid
     * @param orderId
     */
    private void setRetryConfigData(GenericCoreResponseBean<DoPaymentResponse> cashierResponse,
            Map<String, String> data, String mid, String orderId) {
        data.put(RetryConstants.IS_RETRY, RetryConstants.YES);
        data.put(RetryConstants.MID, mid);
        data.put(RetryConstants.ORDER_ID, orderId);
        data.put(RetryConstants.ERROR_MESSAGE, cashierResponse.getResponse().getPaymentStatus().getErrorMessage());
        String payMethod = cashierResponse.getResponse().getPaymentStatus().getPayOptions().get(0).getPayMethodName();
        data.put(ExtraConstants.PAY_METHOD, payMethod);
    }

    private void getResponseForMerchantRedirect(GenericCoreResponseBean<DoPaymentResponse> cashierResponse,
            Map<String, String> data, String callBackUrl) {

        TransactionResponse transactionResponse = theiaResponseGenerator.getMerchantResponse(
                cashierResponse.getResponse(), callBackUrl);
        String responseHtml = theiaResponseGenerator.getFinalHtmlResponse(transactionResponse);

        data.put(RetryConstants.RESPONSE_PAGE, responseHtml);
        data.put(RetryConstants.IS_RETRY, RetryConstants.NO);
        putTransactionResponse(data, transactionResponse);
    }

    private void getResponseForMerchantRedirect(GenericCoreResponseBean<DoPaymentResponse> cashierResponse,
            Map<String, String> data) {

        if (StringUtils.isNotBlank(data.get(ExtraConstants.REQUEST_TYPE)) && null != cashierResponse.getResponse()
                && null != cashierResponse.getResponse().getTransactionStatus()) {
            cashierResponse.getResponse().getTransactionStatus().setRequestType(data.get(ExtraConstants.REQUEST_TYPE));
        }

        if (cashierResponse.getResponse() != null && cashierResponse.getResponse().getTransactionStatus() != null
                && MapUtils.isNotEmpty(cashierResponse.getResponse().getTransactionStatus().getExtendInfo())) {
            cashierResponse.getResponse().getTransactionStatus().getExtendInfo()
                    .put(HEADER_WORKFLOW, data.get(HEADER_WORKFLOW));
        }

        TransactionResponse transactionResponse = theiaResponseGenerator.getMerchantResponse(cashierResponse
                .getResponse());
        changePaymentModeFromBalanceToPPIScanNPay(cashierResponse, transactionResponse);

        if (cashierResponse.getResponse() != null && cashierResponse.getResponse().getTransactionStatus() != null
                && MapUtils.isNotEmpty(cashierResponse.getResponse().getTransactionStatus().getExtendInfo())) {
            String aoaSubsOnPgMid = cashierResponse.getResponse().getTransactionStatus().getExtendInfo()
                    .get(AOA_SUBS_ON_PGMID);
            if (StringUtils.isNotBlank(aoaSubsOnPgMid) && aoaSubsOnPgMid.equalsIgnoreCase("true")) {
                String pgMid = transactionResponse.getMid();
                String aoaMid = aoaUtils.getAOAMidForPGMid(pgMid);
                transactionResponse.setMid(aoaMid);
                LOGGER.info("PG mid {} updated to AOA mid {}", pgMid, aoaMid);
                transactionResponse.setGateway(PAYTM);
            }
        }

        String responseHtml = theiaResponseGenerator.getFinalHtmlResponse(transactionResponse);

        data.put(RetryConstants.RESPONSE_PAGE, responseHtml);
        data.put(RetryConstants.IS_RETRY, RetryConstants.NO);
        putTransactionResponse(data, transactionResponse);

        if (ERequestType.NATIVE.getType().equals(data.get(ExtraConstants.REQUEST_TYPE))
                || ERequestType.UNI_PAY.getType().equals(data.get(ExtraConstants.REQUEST_TYPE))) {
            invalidateNativeFlowRedisData(transactionResponse.getTxnToken(), transactionResponse.getMid(),
                    transactionResponse.getOrderId());
        }
    }

    private void invalidateNativeFlowRedisData(String txnToken, String mid, String orderId) {

        if (StringUtils.isBlank(mid) || StringUtils.isBlank(orderId)) {
            return;
        }

        if (EventNameEnum.ONLINE_ENHANCED_PAYMENT_REQUEST.getEventName().equals(
                nativeSessionUtil.getFlowTypeOnTxnToken(txnToken, mid, orderId))) {
            enhancedCashierPageServiceHelper.invalidateEnhancedNativeData(txnToken, mid, orderId);
        }
        if (EventNameEnum.ONLINE_NATIVE_PAYMENT_REQUEST.getEventName().equals(
                nativeSessionUtil.getFlowTypeOnTxnToken(txnToken, mid, orderId))) {
            nativePaymentUtil.invalidateNativeSessionData(txnToken, mid, orderId);
        }
        if (EventNameEnum.ONLINE_NATIVEPLUS_PAYMENT_REQUEST.getEventName().equals(
                nativeSessionUtil.getFlowTypeOnTxnToken(txnToken, mid, orderId))) {
            nativePaymentUtil.invalidateNativeJsonRequestSessionData(txnToken, mid, orderId);
        }
    }

    public boolean getRetryResponse(HttpServletRequest request) {

        String cashierRequestId = request.getParameter(RetryConstants.CASHIER_REQUEST_ID);
        SavedCardRequest savedCardRequestData = null;

        // Fetch data from Redis cache using getMerchantResponsecashierRquestId
        CashierRequest cashierRequestFromCache = cashierCacheServiceImpl.fetchCashierRequest(cashierRequestId);
        if (cashierRequestFromCache != null) {
            // Fetch card details from cache
            BankCardRequest cardData = cashierRequestFromCache.getCardRequest().getBankCardRequest();
            savedCardRequestData = cashierRequestFromCache.getCardRequest().getSavedCardRequest();
            if (cardData != null) {
                request.setAttribute(RetryConstants.CARD_NUMBER, cardData.getCardNo());
                request.setAttribute(RetryConstants.ISSUING_BANK_NAME, cashierRequestFromCache.getPaymentRequest()
                        .getExtendInfo().get(RetryConstants.ISSUING_BANK_NAME));
                request.setAttribute(RetryConstants.EMI_PLAN_ID, cashierRequestFromCache.getPaymentRequest()
                        .getExtendInfo().get(RetryConstants.EMI_PLAN_ID));
                request.setAttribute(RetryConstants.SAVED_CARD_TXN, RetryConstants.NO);
            } else if (savedCardRequestData != null) {
                request.setAttribute(RetryConstants.SAVED_CARD_TXN, RetryConstants.YES);
            }
        } else {
            LOGGER.info("Cashier Request Data not fetched from cache or it is a net banking txn.");
        }
        boolean processed = retryPaymentServiceImpl.processPaymentRequest(request, savedCardRequestData);
        LOGGER.debug("flow successfully processed :{}", processed);
        return processed;
    }

    private void setLooperRequest(String cashierRequestId, String transId, String alipayMerchantId,
            TransactionInfo transInfo, CashierRequestBuilder cashierRequestBuilder) throws CashierCheckedException {
        if (transInfo != null) {
            switch (transInfo.getTransactionType()) {
            case FUND:
                cashierRequestBuilder.setLooperRequest(new LooperRequest(cashierRequestId, transId));
                break;
            case ACQUIRING:
                cashierRequestBuilder.setLooperRequest(new LooperRequest(cashierRequestId, alipayMerchantId, transId));
                break;
            }
        } else {
            cashierRequestBuilder.setLooperRequest(new LooperRequest(cashierRequestId, alipayMerchantId, transId));
        }
    }

    private void validateRequestData(String merchantId, String cashierRequestId, String transId, String paymentMode) {

        if (StringUtils.isBlank(transId)) {
            throw new TheiaServiceException("Transaction Id is not available for fetching transaction status");
        }
        if (StringUtils.isBlank(merchantId)) {
            throw new TheiaServiceException("Merchant Id is not available for fetching transaction status");
        }

        if (StringUtils.isBlank(cashierRequestId)) {
            throw new TheiaServiceException("Cashier request Id is not available for fetching transaction status");
        }

        if (StringUtils.isBlank(paymentMode)) {
            throw new TheiaServiceException("Payment Mode is not available for fetching transaction status");
        }
    }

    public String generateViewName(HttpServletRequest request) {
        /*
         * Get Theme & Channel_Id from retry request,which was set earlier from
         * original request
         */
        String channelId = (String) request.getAttribute(RequestParams.CHANNEL_ID);
        String theme = (String) request.getAttribute(RequestParams.THEME);
        if (StringUtils.isBlank(theme)) {
            theme = "merchant";
        }
        StringBuilder path = new StringBuilder();
        path.append(RetryConstants.JSP_PATH).append(channelId.toLowerCase()).append("/").append(theme).append("/")
                .append(RetryConstants.RETRY_SUCCESS_PAGE);
        LOGGER.info("View path:{}", path.toString());

        return path.toString();
    }

    public String generateRetryRequest(HttpServletRequest request, Map<String, String> data)
            throws UnsupportedEncodingException {
        String mid = data.get(RequestParams.MID);
        String orderId = data.get(RequestParams.ORDER_ID);
        String cashierRequestId = request.getParameter(ExtraConstants.CASHIER_REQUEST_ID);
        String transId = request.getParameter(ExtraConstants.TRANS_ID);
        // String paymentMode =
        // request.getParameter(ExtraConstants.PAYMENT_MODE);
        String payMethod = data.get(ExtraConstants.PAY_METHOD);
        String paymentMode;

        switch (payMethod) {
        case ExtraConstants.DEBIT_CARD:
            paymentMode = ExtraConstants.DC;
            break;
        case ExtraConstants.CREDIT_CARD:
            paymentMode = ExtraConstants.CC;
            break;
        default:
            paymentMode = request.getParameter(ExtraConstants.PAYMENT_MODE);
            break;
        }

        String errorMessageEncoded = "";
        if (StringUtils.isNotBlank(data.get(RetryConstants.ERROR_MESSAGE))) {
            errorMessageEncoded = URLEncoder.encode(data.get(RetryConstants.ERROR_MESSAGE), "UTF-8");
        }
        StringBuilder retryRequest = new StringBuilder();
        if (isOLDPGRequest(mid, orderId)) {
            String oldPGHost = "https://" + ConfigurationUtil.getProperty(ExtraConstants.OLD_PG_BASE_URL_SKIP);
            retryRequest.append(oldPGHost).append("/theia/processRetry").append("?");
        } else {
            retryRequest.append("/theia/processRetry").append("?");
        }
        retryRequest.append(RequestParams.MID).append("=").append(mid).append("&");
        retryRequest.append(RequestParams.ORDER_ID).append("=").append(orderId).append("&");
        retryRequest.append(RetryConstants.CASHIER_REQUEST_ID).append("=").append(cashierRequestId).append("&");
        retryRequest.append(RetryConstants.TRANS_ID).append("=").append(transId).append("&");
        retryRequest.append(RetryConstants.PAYMENT_MODE).append("=").append(paymentMode).append("&");
        retryRequest.append(ExtraConstants.PAY_METHOD).append("=").append(payMethod).append("&");
        retryRequest.append(RetryConstants.ERROR_MESSAGE).append("=").append(errorMessageEncoded);
        return retryRequest.toString();
    }

    private boolean isOLDPGRequest(String mid, String orderId) {
        String host = nativeSessionUtil.getHostForOldPgRequest(mid, orderId);
        if (StringUtils.isNotBlank(host)) {
            return true;
        }
        return false;
    }

    private void changePaymentModeFromBalanceToPPIScanNPay(GenericCoreResponseBean<DoPaymentResponse> cashierResponse,
            TransactionResponse transactionResponse) {
        LOGGER.info("Request type receive: {}", transactionResponse.getRequestType());
        String requestType = transactionResponse.getRequestType();
        String isEnhancedNative = StringUtils.EMPTY;
        try {
            isEnhancedNative = cashierResponse.getResponse().getTransactionStatus().getExtendInfo()
                    .get("isEnhancedNative");
            LOGGER.info("Is Enhanced Native Request: {}", isEnhancedNative);
        } catch (Exception e) {
            LOGGER.info("Error in getting extend info from cashier Response");
        }
        if (DYNAMIC_QR.equalsIgnoreCase(requestType) && Boolean.TRUE.equals(Boolean.parseBoolean(isEnhancedNative))) {
            LOGGER.info("Scan N Pay Request detected,Changing payment mode from balance to PPI");
            String paymentMode = transactionResponse.getPaymentMode();
            if (EPayMethod.BALANCE.getMethod().equals(paymentMode)) {
                transactionResponse.setPaymentMode(EPayMethod.BALANCE.getOldName());
            }
        }
    }

    private void setUPIPollStatus(UPIPollStatus status, Map<String, String> map) {
        map.put("POLL_STATUS", status.getMessage());
    }

    public String fetchResponsePageForAbandonTransaction(HttpServletRequest request,
            com.paytm.pgplus.theia.sessiondata.TransactionInfo txnData, MerchantInfo merchantInfo,
            ExtendedInfoRequestBean extendedInfoRequestBean) {

        try {
            TransactionResponse transactionResponse = new TransactionResponse();
            SystemResponseCode systemResponseCode = SystemResponseCode.POLL_PAGE_ABANDONED_CODE;
            setOrderMetadata(txnData, merchantInfo, transactionResponse);
            transactionResponse.setTransactionStatus(ExternalTransactionStatus.PENDING.name());
            transactionResponse.setCallbackUrl(getCallbackUrl(request));
            setPaymentDetails(transactionResponse, txnData, merchantInfo);
            if (extendedInfoRequestBean != null) {
                transactionResponse.setExtraParamsMap(extendedInfoRequestBean.getExtraParamsMap());
                if (StringUtils.isNotBlank(extendedInfoRequestBean.getClientId()))
                    transactionResponse.setClientId(extendedInfoRequestBean.getClientId());
            } else {
                transactionResponse.setExtraParamsMap(Collections.emptyMap());
            }

            // Set Response-Code and Response-Message
            responseCodeUtil.setRespMsgeAndCode(transactionResponse, null, systemResponseCode);

            String responsePage = theiaResponseGenerator.getFinalHtmlResponse(transactionResponse);
            return responsePage;
        } catch (Exception e) {
            throw new TheiaServiceException(e);
        }
    }

    public String fetchResponsePageForAbandonTransaction(EnhanceCashierPageCachePayload enhanceCashierPageCachePayload) {
        try {
            TransactionResponse transactionResponse = new TransactionResponse();
            SystemResponseCode systemResponseCode = SystemResponseCode.FAILURE;
            transactionResponse.setMid(enhanceCashierPageCachePayload.getMerchantRequestData().getMid());
            transactionResponse.setOrderId(enhanceCashierPageCachePayload.getMerchantRequestData().getOrderId());
            transactionResponse.setTxnId(enhanceCashierPageCachePayload.getEnhancedCashierPage().getTxn().getId());
            transactionResponse.setTxnAmount(AmountUtils.formatNumberToTwoDecimalPlaces(enhanceCashierPageCachePayload
                    .getEnhancedCashierPage().getTxn().getTxnAmount()));
            transactionResponse.setTransactionStatus(ExternalTransactionStatus.TXN_FAILURE.name());
            transactionResponse
                    .setCallbackUrl(enhanceCashierPageCachePayload.getEnhancedCashierPage().getCallbackUrl());
            com.paytm.pgplus.theia.sessiondata.TransactionInfo txnData = new com.paytm.pgplus.theia.sessiondata.TransactionInfo();
            txnData.setOrderId(enhanceCashierPageCachePayload.getMerchantRequestData().getOrderId());
            MerchantInfo merchantInfo = new MerchantInfo();
            merchantInfo.setInternalMid(merchantMappingService.getMappingMerchantData(
                    enhanceCashierPageCachePayload.getMerchantRequestData().getMid()).getAlipayId());
            setPaymentDetails(transactionResponse, txnData, merchantInfo);
            // Set Response-Code and Response-Message
            responseCodeUtil.setRespMsgeAndCode(transactionResponse, null, systemResponseCode);
            String responsePage = theiaResponseGenerator.getFinalHtmlResponse(transactionResponse);
            return responsePage;
        } catch (Exception e) {
            throw new TheiaServiceException(e);
        }
    }

    private void setOrderMetadata(com.paytm.pgplus.theia.sessiondata.TransactionInfo txnData,
            MerchantInfo merchantInfo, TransactionResponse transactionResponse) {
        transactionResponse.setMid(merchantInfo.getMid());
        transactionResponse.setTxnId(txnData.getTxnId());
        transactionResponse.setOrderId(txnData.getOrderId());
        transactionResponse.setTxnAmount(AmountUtils.formatNumberToTwoDecimalPlaces(txnData.getTxnAmount()));
    }

    private void setPaymentDetails(TransactionResponse transactionResponse,
            com.paytm.pgplus.theia.sessiondata.TransactionInfo txnData, MerchantInfo merchantInfo) {
        try {
            QueryByMerchantTransIdResponse response = fetchTransactionData(txnData, merchantInfo);
            if (response != null && response.getBody() != null
                    && "S".equals(response.getBody().getResultInfo().getResultStatus())) {
                List<PaymentView> paymentViews = response.getBody().getPaymentViews();
                if (paymentViews != null && !paymentViews.isEmpty()) {
                    sortOnPaidTime(paymentViews);
                    PaymentView paymentView = paymentViews.get(0);
                    Map<String, String> extendedInfo = paymentView.getExtendInfo();
                    Map<String, String> payRequestExtendedInfo = paymentView.getPayRequestExtendInfo();
                    List<PayOptionInfo> payOptionInfoList = paymentView.getPayOptionInfos();

                    if (extendedInfo != null && isAddAndPay(extendedInfo)) {
                        setAddAndPayDetails(transactionResponse);
                    } else if (payOptionInfoList != null) {
                        if (isHybrid(payOptionInfoList)) {
                            setHybridDetails(transactionResponse, payRequestExtendedInfo, payOptionInfoList);
                        } else {
                            setPGOnlyDetails(transactionResponse, payRequestExtendedInfo, payOptionInfoList);
                        }
                    }

                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred while fetching transaction data : ", e);
        }
    }

    private void sortOnPaidTime(List<PaymentView> paymentViews) {
        paymentViews.sort(new Comparator<PaymentView>() {

            @Override
            public int compare(PaymentView o1, PaymentView o2) {
                return o1.getPaidTime().getTime() < o2.getPaidTime().getTime() ? 1 : -1;
            }
        });
    }

    private boolean isHybrid(List<PayOptionInfo> payOptionInfoList) {
        return payOptionInfoList.size() > 1;
    }

    private void setHybridDetails(TransactionResponse transactionResponse, Map<String, String> payRequestExtendedInfo,
            List<PayOptionInfo> payOptionInfoList) {
        transactionResponse.setPaymentMode(PayMethod.HYBRID_PAYMENT.getOldName());
        transactionResponse.setChildTxnList(getChildTransactions(payOptionInfoList, payRequestExtendedInfo,
                transactionResponse));
    }

    private void setAddAndPayDetails(TransactionResponse transactionResponse) {
        transactionResponse.setGateway(ExtraConstants.AUTO_DEBIT_BANK_NAME);
        transactionResponse.setPaymentMode(PayMethod.BALANCE.getOldName());
    }

    private void setPGOnlyDetails(TransactionResponse transactionResponse, Map<String, String> payRequestExtendedInfo,
            List<PayOptionInfo> payOptionInfoList) {
        transactionResponse.setPaymentMode(PaymentModeMapperUtil.getNewPayModeDisplayName(payOptionInfoList.get(0)
                .getPayMethod().getOldName()));
        transactionResponse.setGateway(payOptionInfoList.get(0).getExtendInfo().get(PaymentStatusKeys.GATEWAY));
        transactionResponse.setBankName(payRequestExtendedInfo.get(PaymentStatusKeys.ISSUING_BANK_NAME));
        String bankTxnId = payOptionInfoList.get(0).getExtendInfo().get(PaymentStatusKeys.BANK_TXN_ID);
        if (StringUtils.isNotBlank(bankTxnId)) {
            transactionResponse.setBankTxnId(bankTxnId);
        }
    }

    private boolean isAddAndPay(Map<String, String> extendedInfo) {
        return ExtraConstants.EXTENDED_INFO_VALUE_TOPUPANDPAY.equals(extendedInfo
                .get(ExtraConstants.EXTENDED_INFO_KEY_TOPUPANDPAY));
    }

    private QueryByMerchantTransIdResponse fetchTransactionData(
            com.paytm.pgplus.theia.sessiondata.TransactionInfo txnData, MerchantInfo merchantInfo)
            throws FacadeInvalidParameterException, FacadeCheckedException {

        QueryByMerchantTransIdRequestBody requestBody = new QueryByMerchantTransIdRequestBody(
                merchantInfo.getInternalMid(), txnData.getOrderId(), true);
        QueryByMerchantTransIdRequest request = new QueryByMerchantTransIdRequest(
                RequestHeaderGenerator.getHeader(ApiFunctions.QUERY_BY_MERCHANT_TRANS_ID), requestBody);
        QueryByMerchantTransIdResponse response = acquiringOrder.queryByMerchantTransId(request);
        return response;
    }

    private String getCallbackUrl(HttpServletRequest request) {
        ExtendedInfoRequestBean extendInfo = theiaSessionDataService.geExtendedInfoRequestBean(request);
        if (extendInfo == null || StringUtils.isBlank(extendInfo.getCallBackURL())) {
            throw new TheiaControllerException("Callback URL could not be obtained. Will break the flow now.");
        }
        return extendInfo.getCallBackURL();
    }

    private List<ChildTransaction> getChildTransactions(List<PayOptionInfo> payOptionInfoList,
            Map<String, String> payRequestExtendedInfo, TransactionResponse txnStatusResponse) {
        List<ChildTransaction> childTransactionsList = new ArrayList<>();

        for (PayOptionInfo payOptionInfo : payOptionInfoList) {
            ChildTransaction childTransaction = new ChildTransaction();

            childTransaction.setTxnId(txnStatusResponse.getTxnId());
            childTransaction.setTxnAmount(AmountUtils.getTransactionAmountInRupee(payOptionInfo.getPayAmount()
                    .getAmount()));
            childTransaction.setPaymentMode(PaymentModeMapperUtil.getNewPayModeDisplayName(payOptionInfo.getPayMethod()
                    .getOldName()));
            if (!PayMethod.BALANCE.equals(payOptionInfo.getPayMethod())) {
                childTransaction.setGateway(payOptionInfo.getExtendInfo().get(PaymentStatusKeys.GATEWAY));
                childTransaction.setBankTxnId(payOptionInfo.getExtendInfo().get(PaymentStatusKeys.BANK_TXN_ID));
                childTransaction.setBankName(payRequestExtendedInfo.get(PaymentStatusKeys.ISSUING_BANK_NAME));
            } else {
                childTransaction.setGateway(ExtraConstants.AUTO_DEBIT_BANK_NAME);
            }
            childTransactionsList.add(childTransaction);
        }
        return childTransactionsList;
    }

    private void generateDynamicQRData(TransactionInfo transInfo,
            GenericCoreResponseBean<DoPaymentResponse> cashierResponse, String qrPayResponseKey,
            String qrTransResponseKey, Map<String, String> data, String callBackURL) {

        /*
         * When RequestType = DYNAMIC_QR_TXN_STATUS, signifies this request came
         * after bank and insta redirection on app.
         */
        String topicName = EnvInfoUtil.httpServletRequest().getParameter("topicName");
        if (StringUtils.isNotBlank(transInfo.getRequestType())
                && "DYNAMIC_QR_TXN_STATUS".equals(transInfo.getRequestType())
                && !StringUtils.equalsIgnoreCase(DYNAMIC_QR, topicName)) {

            // LOGGER.info("Request received for putting dynamic QR DATA : DYNAMIC_QR_TXN_STATUS");
            // Push data to redis with requestType = DYNAMIC_QR ,dynamicQR =
            // true
            // signifies that casting of payment & txn status objects is not
            // required
            transInfo.setDynamicQR(true);
            transInfo.setRequestType(DYNAMIC_QR);

            boolean isQRWith2FAEnabled = merchantPreferenceService.isDynamicQR2FAEnabledWithPCF(transInfo.getMid());

            if (isQRWith2FAEnabled) {
                transInfo.setRequestType(DYNAMIC_QR_2FA);
            }

            theiaTransactionalRedisUtil.set(Constants.TXN_TYPE_KEY_PREFIX + transInfo.getTransId(), transInfo, 1200);
            theiaTransactionalRedisUtil.set(qrPayResponseKey, cashierResponse.getResponse().getPaymentStatus(), 1200);
            theiaTransactionalRedisUtil.set(qrTransResponseKey, cashierResponse.getResponse().getTransactionStatus(),
                    1200);

            getResponseForMerchantRedirect(cashierResponse, data, callBackURL);

            // Find data in cache pushed during paymentNotify
            String paymentMode = cashierResponse.getResponse().getPaymentStatus().getPayOptions().get(0)
                    .getPayMethodOldName();
            String orderId = cashierResponse.getResponse().getTransactionStatus().getMerchantTransId();
            String mid = cashierResponse.getResponse().getTransactionStatus().getExtendInfo().get("paytmMerchantId");

            LOGGER.info("paymentMode : {}, orderId : {}, mid : {}", paymentMode, orderId, mid);

            // dynamicQRCoreService.pushPostTransactionPayload(transInfo.getTransId(),
            // paymentMode, orderId, mid,
            // transInfo.getPaymentId(),
            // cashierResponse.getResponse().getPaymentStatus().getPaymentStatusValue());
        } else {
            getResponseForMerchantRedirect(cashierResponse, data);
        }
    }

    private void generateNativeEnhanceResponse(HttpServletRequest request,
            GenericCoreResponseBean<DoPaymentResponse> cashierResponse, Map<String, String> data, String mid,
            String orderId, String transId, EnhanceCashierPageCachePayload enhanceCashierPageCachePayload,
            Integer currentRetryCount) throws FacadeCheckedException {
        String txnToken = retryServiceHelper.getTxnToken(mid, orderId);
        // update the key TTL expiry in cache for retry
        updateCacheExpiry(enhancedCashierPageServiceHelper.fetchRedisKey(mid, orderId));

        updateCacheExpiry(txnToken);

        StringBuilder midOrderIdKey = new StringBuilder("NativeTxnInitiateRequest");
        midOrderIdKey.append(mid).append("_").append(orderId);
        updateCacheExpiry(midOrderIdKey.toString());

        EnhancedCashierPage enhancedCashierPage = enhanceCashierPageCachePayload.getEnhancedCashierPage();
        PaymentRequestBean paymentRequestBean = enhanceCashierPageCachePayload.getMerchantRequestData();

        String channelId = null;
        if (paymentRequestBean != null) {
            channelId = paymentRequestBean.getChannelId();
        }

        if (null != enhancedCashierPage) {

            String htmlPage = null;

            if (null != enhancedCashierPage.getRetryData()) {
                enhancedCashierPage.getRetryData().setRetryCount(currentRetryCount.toString());
                if (null != cashierResponse.getResponse().getPaymentStatus()) {
                    enhancedCashierPage.getRetryData().setRetryErrorMsg(
                            theiaResponseGenerator.getErrorMsgForNativeEnhancedPaymentFailure(cashierResponse
                                    .getResponse()));
                }
            }
            enhancedCashierPageServiceHelper.settxnTokenTTL(enhancedCashierPage);
            LOGGER.info("PUSH_APP_DATA for enhanced cashier flow {}", enhancedCashierPage);
            String enhancedCashierPageJson = JsonMapper.mapObjectToJson(enhancedCashierPage);
            String isPushAppDataEncoded = ConfigurationUtil.getProperty(ENHANCE_PUSH_APP_DATA_ENCODED, "false");
            // if encodedPropertyTrue then encode the enhancedCashierPAgeJson
            if (!ff4JHelper.isFF4JFeatureForMidEnabled(TheiaConstant.FF4J.FEATURE_UI_SERVER_CONFIG, mid)
                    && Boolean.valueOf(isPushAppDataEncoded)) {
                try {
                    enhancedCashierPageJson = new String(Base64.getEncoder().encode(
                            (enhancedCashierPageJson).getBytes("UTF-8")));
                } catch (Exception e) {
                    LOGGER.error("error while encoding enhanced cashier data :{}", e.getMessage());
                }
            }

            // if mid Applicable

            UIMicroserviceRequest uiMicroserviceRequest = new UIMicroserviceRequest(enhancedCashierPageJson, channelId,
                    isPushAppDataEncoded, UIMicroserviceUrl.ENHANCED_CASHIER_URL);
            UIMicroserviceResponse uiMicroserviceResponse = uiMicroserviceHelper.getHtmlPageFromUI(
                    uiMicroserviceRequest, FEATURE_UI_MICROSERVICE_ENHANCED, mid);
            htmlPage = uiMicroserviceResponse.getHtmlPage();

            // if html Page blank from ui-microservice
            if (StringUtils.isBlank(htmlPage)) {
                htmlPage = enhancedCashierPageServiceHelper.getEnhancedCashierTheme(channelId);

                if (Boolean.valueOf(isPushAppDataEncoded)) {
                    htmlPage = htmlPage
                            .replace(
                                    com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.EnhancedCashierPageKeys.ENCODE_FLAG,
                                    "true");
                }

                if (StringUtils.isNotBlank(htmlPage)) {
                    if (ff4JHelper.isFF4JFeatureForMidEnabled(TheiaConstant.FF4J.FEATURE_UI_SERVER_CONFIG, mid)) {
                        try {
                            enhancedCashierPageJson = new String(Base64.getEncoder().encode(
                                    (enhancedCashierPageJson).getBytes()));
                        } catch (Exception e) {
                            LOGGER.error("error while encoding enhanced cashier data :{}", e.getMessage());
                        }
                    }
                    htmlPage = htmlPage
                            .replace(
                                    com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.EnhancedCashierPageKeys.REPLACE_STRING,
                                    enhancedCashierPageJson);
                } else {
                    throw new TheiaServiceException("Unable to fetch html template from redis");
                }
            }

            if (isDirectBankSubmitRequest(data)) {
                int expiryTime = enhancedCashierPageServiceHelper.getTokenExpiryTime();
                String key = enhancedCashierPageServiceHelper.fetchRedisKey(mid, orderId);
                enhanceCashierPageCachePayload.setEnhancedCashierPage(enhancedCashierPage);
                nativeSessionUtil.setKey(key, enhanceCashierPageCachePayload, expiryTime);
            }

            data.put(RetryConstants.RETRY_INITIATED, Boolean.TRUE.toString());

            data.put(RetryConstants.RESPONSE_PAGE, htmlPage);
        }
        data.put(RetryConstants.IS_RETRY, RetryConstants.YES);
        data.put(TheiaConstant.EnhancedCashierFlow.ENHANCED_CASHIER_FLOW, RetryConstants.YES);
    }

    private void updateCacheExpiry(String key) {
        int expiryTimeInSeconds = enhancedCashierPageServiceHelper.getTokenExpiryTime();
        nativeSessionUtil.expireKey(key, expiryTimeInSeconds);
    }

    private void putTransactionResponse(Map<String, String> data, TransactionResponse transactionResponse) {
        /*
         * This is done so that TransactionResponse could be used in
         * /api/v1/directpage/submit or cancel apis, where we need to send
         * response in JSON, this is for Native+
         */
        try {
            data.put(TRANSACTION_RESPONSE_OBJECT, JsonMapper.mapObjectToJson(transactionResponse));
        } catch (Exception e) {
            LOGGER.error("Exception putting transactionResponse to map {}", e);
        }
        pushEventForNativeJsonRequest(data, transactionResponse);
    }

    private void pushEventForNativeJsonRequest(Map<String, String> data, TransactionResponse transactionResponse) {
        if (StringUtils.equals(NATIVE_JSON_REQUEST, data.get("paymentRequestFlow"))) {
            processTransactionUtil.pushNativeJsonResponseEvent(transactionResponse);
        }
    }

    private void setPaymentRequestFlowInData(Map<String, String> data,
            GenericCoreResponseBean<DoPaymentResponse> cashierResponse) {
        if (data == null) {
            return;
        }
        if (cashierResponse.getResponse() != null) {
            CashierPaymentStatus cashierPaymentStatus = cashierResponse.getResponse().getPaymentStatus();
            if (cashierPaymentStatus != null && cashierPaymentStatus.getExtendInfo() != null) {
                data.put("paymentRequestFlow", cashierPaymentStatus.getExtendInfo().get("paymentRequestFlow"));
            }
        }
    }

    private boolean isInvalidOtpCase(GenericCoreResponseBean<DoPaymentResponse> cashierResponse) {
        if (cashierResponse != null) {
            DoPaymentResponse doPaymentResponse = cashierResponse.getResponse();
            if (doPaymentResponse != null && doPaymentResponse.getPaymentStatus() != null
                    && StringUtils.isNotBlank(doPaymentResponse.getPaymentStatus().getInstErrorCode())) {
                String instErrorCode = doPaymentResponse.getPaymentStatus().getInstErrorCode();
                List<String> errorCodeList = getErrorCodesForDirectBankInvalidOtp();
                if (errorCodeList.contains(instErrorCode)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<String> getErrorCodesForDirectBankInvalidOtp() {
        List<String> errorCodes = new ArrayList<>();
        String errorCode = ConfigurationUtil.getProperty("direct.bank.invalidOTP.codes", "FGW_INVALID_OTP");
        String[] errorCodeList = errorCode.split(Pattern.quote(","));
        for (String fgwError : errorCodeList) {
            errorCodes.add(fgwError);
        }

        return errorCodes;
    }

    private boolean isDirectBankPageRetry(Map<String, String> data) {
        if (StringUtils.equals(Boolean.TRUE.toString(), data.get(DIRECT_BANK_PAGE_INVALID_OTP))) {
            return true;
        }
        return false;
    }

    private boolean isDirectBankSubmitRequest(Map<String, String> data) {
        if (StringUtils.equals(Boolean.TRUE.toString(), data.get(DIRECT_BANK_PAGE_SUBMIT_REQUEST))) {
            return true;
        }
        return false;
    }

    public UPIPollStatus getUpiPollStatus(Map<String, String> data) {
        if (data == null
                || (null != data.get("POLL_STATUS") && UPIPollStatus.POLL_AGAIN.getMessage().equalsIgnoreCase(
                        data.get("POLL_STATUS")))) {
            return POLL_AGAIN;
        }
        return STOP_POLLING;
    }

}
