/**
 *
 */
package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.biz.core.model.request.BizCancelFundOrderRequest;
import com.paytm.pgplus.biz.core.model.request.BizCancelOrderRequest;
import com.paytm.pgplus.biz.core.model.request.BizCancelOrderResponse;
import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.core.order.service.IOrderService;
import com.paytm.pgplus.biz.core.risk.RiskConstants;
import com.paytm.pgplus.biz.core.risk.RiskUtil;
import com.paytm.pgplus.biz.core.risk.RiskVerifierPayload;
import com.paytm.pgplus.biz.exception.BizMerchantVelocityBreachedException;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.utils.MerchantVelocityUtil;
import com.paytm.pgplus.biz.utils.RedisUtil;
import com.paytm.pgplus.cache.model.ResponseCodeDetails;
import com.paytm.pgplus.cashier.constant.CashierWorkflow;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.exception.RiskVerificationRequiredException;
import com.paytm.pgplus.cashier.exception.SaveCardValidationException;
import com.paytm.pgplus.cashier.looper.model.LooperRequest;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.DoPaymentResponse;
import com.paytm.pgplus.cashier.models.InitiatePaymentResponse;
import com.paytm.pgplus.cashier.models.UPIPushRequest;
import com.paytm.pgplus.cashier.service.impl.PaymentServiceImpl;
import com.paytm.pgplus.cashier.workflow.PaymentWorkflow;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.common.model.ConsultDetails;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.common.model.MerchantVpaTxnInfo;
import com.paytm.pgplus.common.model.TxnStateLog;
import com.paytm.pgplus.common.util.MaskingUtil;
import com.paytm.pgplus.common.util.PaymentModeMapperUtil;
import com.paytm.pgplus.common.util.ThreadLocalUtil;
import com.paytm.pgplus.facade.acquiring.enums.AcquirementStatusType;
import com.paytm.pgplus.facade.enums.PaymentScenario;
import com.paytm.pgplus.facade.enums.ProductCodes;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.merchantlimit.exceptions.MerchantLimitBreachedException;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.facade.payment.models.request.UPIPushInitiateRequest;
import com.paytm.pgplus.facade.payment.models.response.UPIPushInitiateResponse;
import com.paytm.pgplus.facade.payment.services.IBankProxyService;
import com.paytm.pgplus.facade.risk.models.response.RiskVerifierDoViewResponse;
import com.paytm.pgplus.facade.user.models.PaytmBanksVpaDefaultDebitCredit;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.utils.RequestIdGenerator;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.IResponseCodeService;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.exception.KycValidationException;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.savedcardclient.service.ICacheCardService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.cache.model.MerchantPreferenceStore;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.SessionDataAttributes;
import com.paytm.pgplus.theia.enums.PaymentRequestParam;
import com.paytm.pgplus.theia.exceptions.CoreSessionExpiredException;
import com.paytm.pgplus.theia.exceptions.TheiaControllerException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.helper.LinkBasedPaymentHelper;
import com.paytm.pgplus.theia.helper.PaymentRequestHelper;
import com.paytm.pgplus.theia.nativ.utils.RiskVerificationUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.*;
import com.paytm.pgplus.theia.services.helper.RetryServiceHelper;
import com.paytm.pgplus.theia.session.utils.UpiInfoSessionUtil;
import com.paytm.pgplus.theia.sessiondata.*;
import com.paytm.pgplus.theia.utils.*;
import com.paytm.pgplus.theia.utils.helper.VPAHelper;
import com.paytm.pgplus.theia.validator.service.ValidationService;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static com.paytm.pgplus.payloadvault.theia.constant.EnumValueToMask.SSOTOKEN;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.KYC_FLOW;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.KYC_TXN_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.THEIA_VALIDATE_MID_ENABLE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.THEIA_VALIDATE_ORDER_ID_ENABLE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.TRANSACTION_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.*;

/**
 * @author Lalit, Amit
 * @since April 6, 2016
 */
@Controller
@RequestMapping("payment/request")
public class SubmitPaymentRequestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubmitPaymentRequestController.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(SubmitPaymentRequestController.class);

    private static final String RISK_FEE = "RISK_CONVENIENCE_FEE_APPLICABLE";
    private static final String TOKEN = "cacheCardToken";
    private static final String PPB_BANK_CODE = "PPBL";
    private static final String VIEW_BASE = "/WEB-INF/views/jsp/";

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    private PaymentRequestHelper paymentRequestHelper;

    @Autowired
    private PaymentServiceImpl paymentServiceImpl;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("theiaCardService")
    private ITheiaCardService theiaCardService;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    private IResponseCodeService responseCodeService;

    @Autowired
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("orderService")
    private IOrderService orderServiceImpl;

    @Autowired
    @Qualifier("cacheCardService")
    private ICacheCardService cacheCardService;

    @Autowired
    @Qualifier("bankProxyServiceImpl")
    private IBankProxyService bankProxyServiceImpl;

    @Autowired
    @Qualifier("userKycServiceImpl")
    private IUserKycService userKycServiceImpl;

    @Autowired
    @Qualifier("cashierInternalPaymentRetry")
    private InternalPaymentRetryService internalPaymentRetryService;

    @Autowired
    private RetryServiceHelper retryServiceHelper;

    @Autowired
    ResponseCodeUtil responseCodeUtil;

    @Autowired
    private MerchantVelocityUtil merchantVelocityUtil;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService sessionDataService;

    @Autowired
    private UpiInfoSessionUtil upiInfoSessionUtil;

    @Autowired
    private RiskVerificationUtil riskVerificationUtil;

    @Autowired
    private RiskUtil riskUtil;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private TransactionCacheUtils transactionCacheUtils;

    @Autowired
    private Ff4jUtils ff4JUtils;

    @Autowired
    private ValidationService validationService;

    private static String isLimitRollBacked = "isLimitRollBacked";

    /**
     * @param request
     * @param response
     * @return
     * @throws IOException
     * @throws ServletException
     */
    @RequestMapping(value = "submit", method = RequestMethod.POST)
    public String submitPaymentInfo(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, ServletException {
        String kycValidate = ConfigurationUtil.getProperty(TheiaConstant.RequestParams.KYC_VALIDATE_FLAG, "N");
        long startTime = System.currentTimeMillis();
        response.setContentType("text/html");
        CashierRequest cashierRequest = null;
        try {
            if (!theiaSessionDataService.isSessionExists(request)) {
                throw new CoreSessionExpiredException("Session Does not exist");
            }
            checkPaymentCountBreached(request);
            /** This Method remove AutoLoginFlag value from OAuth JSON String */
            resetSessionData(request);
            setStateLoggingParams(request);
            if (Boolean.valueOf(request.getParameter(RiskConstants.RISK_VERIFIER_UI_KEY))) {
                cashierRequest = getRiskVerifiedCashierRequest(request);
            }
            /* RBI-KYC hack */
            else if (kycValidate.equalsIgnoreCase("Y")) {

                String userId = StringUtils.isNotBlank(theiaSessionDataService.getLoginInfoFromSession(request)
                        .getUser().getUserID()) ? theiaSessionDataService.getLoginInfoFromSession(request).getUser()
                        .getUserID() : StringUtils.EMPTY;

                String userKycKey = "KYC_" + userId;

                boolean hasCustomerDoneKYCRecently = theiaTransactionalRedisUtil.get(userKycKey) != null ? true : false;

                boolean isAllowedForAllCustomers = Boolean.valueOf(ConfigurationUtil.getProperty(
                        TheiaConstant.RequestParams.KYC_ALLOWED_ALL_FLAG, "false"));

                List<String> allowedCustIds = Collections.emptyList();

                if (StringUtils.isNotBlank(ConfigurationUtil.getProperty(TheiaConstant.RequestParams.KYC_ALLOWED_LIST))) {
                    allowedCustIds = Arrays.asList(ConfigurationUtil.getProperty(
                            TheiaConstant.RequestParams.KYC_ALLOWED_LIST).split(","));
                }

                //

                if (!hasCustomerDoneKYCRecently && (isAllowedForAllCustomers || allowedCustIds.contains(userId))) {

                    String kycFLow = request.getParameter(KYC_FLOW);

                    if (!StringUtils.isBlank(kycFLow) && kycFLow.equalsIgnoreCase("yes")) {

                        LOGGER.info("Request received from the KYC page");

                        String kycFlowKey = "KYC_FLOW_" + request.getParameter(KYC_TXN_ID);
                        userKycServiceImpl.doKYC(request);

                        LOGGER.info("Success response received from the KYC service");
                        long timeOut = (long) 30 * 60;
                        theiaTransactionalRedisUtil.set(userKycKey, true, timeOut);
                        cashierRequest = (CashierRequest) theiaTransactionalRedisUtil.get(kycFlowKey);

                    } else {
                        LOGGER.debug("Request received from the cashier page");

                        cashierRequest = paymentRequestHelper.prepareCashierRequest(request);

                        if (cashierRequest.isOnTheFlyKYCRequired()) {
                            LOGGER.info("KYC form returned successfully for userId : {}", userId);
                            return theiaViewResolverService.returnKYCPage();
                        }
                    }
                } else {
                    LOGGER.info("Transaction selected for normal flow , KYC flag is on");
                    cashierRequest = paymentRequestHelper.prepareCashierRequest(request);
                }
            } else {
                LOGGER.info("Transaction selected for normal flow , KYC flag is off");
                cashierRequest = paymentRequestHelper.prepareCashierRequest(request);
            }

            if (cashierRequest != null && !cashierRequest.isProcessed()) {

                processCashierRequest(request, response, cashierRequest);

                boolean upiPushFlag = false;
                if (null != cashierRequest.getUpiPushRequest() && cashierRequest.getUpiPushRequest().isUpiPushTxn()) {
                    upiPushFlag = true;
                }
                if ((cashierRequest.getCashierWorkflow().equals(CashierWorkflow.UPI) || cashierRequest
                        .getCashierWorkflow().equals(CashierWorkflow.ADD_MONEY_UPI))
                        && theiaSessionDataService.isUPIAccepted(request) && !upiPushFlag) {
                    TransactionInfo txnInfo = theiaSessionDataService.getTxnInfoFromSession(request);
                    String mid = txnInfo.getMid();
                    Map<String, Object> context = new HashMap<>();
                    context.put("mid", mid);
                    if (iPgpFf4jClient.checkWithdefault("upiPollPageMid", context, false)) {
                        String upiPollHtmlPage = com.paytm.pgplus.common.config.ConfigurationUtil.getUpiPollingPage();
                        if (upiPollHtmlPage != null && !upiPollHtmlPage.isEmpty()) {// to
                                                                                    // check
                                                                                    // ui
                                                                                    // file
                                                                                    // exists
                                                                                    // for
                                                                                    // this
                                                                                    // flow
                            UPITransactionInfo upiTransactionInfo = theiaSessionDataService
                                    .getUPUpiTransactionInfoFromSession(request);
                            MerchantInfo merchantinfo = theiaSessionDataService.getMerchantInfoFromSession(request);
                            JSONObject pollingJson = new JSONObject();
                            JSONObject txnInformation = new JSONObject();
                            String txnId = txnInfo.getTxnId();
                            String orderId = txnInfo.getOrderId();
                            String txnAmount = txnInfo.getTxnAmount();
                            txnInformation.put("txnId", txnId);
                            txnInformation.put("orderId", orderId);
                            txnInformation.put("txnAmount", txnAmount);
                            pollingJson.put("upiTransactionInfo", upiTransactionInfo);
                            pollingJson.put("txnInfo", txnInformation);
                            pollingJson.put("merchantinfo", merchantinfo);
                            upiPollHtmlPage = upiPollHtmlPage.replace("pollingJSON",
                                    JsonMapper.mapObjectToJson(pollingJson));
                            response.setContentType("text/html; charset=UTF-8");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().print(upiPollHtmlPage);// this
                                                                        // will
                                                                        // set
                                                                        // html
                                                                        // response
                                                                        // page
                            return null;

                        }
                    }
                    return theiaViewResolverService.returnUPIPollPage();
                }
            } else {
                LOGGER.warn("Cashier Request is Already Processed");
            }

            return theiaViewResolverService.returnForwarderPage();

        } catch (KycValidationException e) {
            return pushForKycPageLoad(request, e);
        } catch (BizMerchantVelocityBreachedException e) {
            return pushForLimitBreachedPage(request, response, e.getLimitType(), e.getLimitDuration());
        } catch (PaytmValidationException e) {
            return pushForPageLoad(request, cashierRequest, e);
        } catch (CashierCheckedException e) {
            LOGGER.error("Exception Occurred :: ", e);
        } catch (MerchantLimitBreachedException e) {
            return pushForLimitBreachedPage(request, response, e.getLimitType(), e.getLimitDuration());
        } catch (RiskVerificationRequiredException e) {
            return pushForRiskVerificationPage(cashierRequest, e, request, response);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception Occurred : {} ", e);
        } finally {
            LOGGER.info("Total time taken for Controller {} is {} ms", "SubmitPaymentRequestController",
                    System.currentTimeMillis() - startTime);
        }
        return theiaViewResolverService.returnOOPSPage(request);
    }

    private String pushForLimitBreachedPage(HttpServletRequest request, HttpServletResponse response, String limitType,
            String limitDuration) throws ServletException, IOException {
        LOGGER.error("Merchant {} {} limit breached!", limitDuration, limitType);
        request.setAttribute(SHOW_VIEW_FLAG, MERCHANT_LIMIT_ERROR_SCREEN);
        request.setAttribute("ERROR_MESSAGE", MapperUtils.getResultCodeForMerchantBreached(limitType, limitDuration)
                .getResultMsg());
        request.getRequestDispatcher(VIEW_BASE + theiaViewResolverService.returnLinkPaymentStatusPage(request) + ".jsp")
                .forward(request, response);
        return theiaViewResolverService.returnForwarderPage();
    }

    private String pushForKycPageLoad(HttpServletRequest request, KycValidationException e) {
        LOGGER.error("exception thrown by the KYC service", e);

        boolean retryAllowed = paymentRequestHelper.prepareKycRetryData(request);
        if (retryAllowed) {
            LOGGER.info("KYC retry form returned");
            return theiaViewResolverService.returnKYCPage();
        }

        String merchantResponsePage = theiaResponseGenerator.createFailureMerchantResponse(request);
        if (StringUtils.isBlank(merchantResponsePage)) {
            throw new TheiaControllerException("Could not get response page");
        }

        theiaSessionDataService.setRedirectPageInSession(request, merchantResponsePage);
        return theiaViewResolverService.returnForwarderPage();
    }

    private String pushForRiskVerificationPage(CashierRequest cashierRequest, RiskVerificationRequiredException e,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        String mid = request.getParameter(RiskConstants.MID);
        String orderId = request.getParameter(RiskConstants.ORDER_ID);
        String transId = cashierRequest.getPaymentRequest().getTransId();
        if (ff4JUtils.isFeatureEnabled(THEIA_VALIDATE_MID_ENABLE, false) && !validationService.validateMid(mid)) {
            throw new TheiaServiceException("CRITICAL_ERROR : Invalid NATIVE_MID : " + mid);
        }
        if (ff4JUtils.isFeatureEnabled(THEIA_VALIDATE_ORDER_ID_ENABLE, false)
                && !validationService.validateOrderId(orderId)) {
            throw new TheiaServiceException("CRITICAL_ERROR : Invalid NATIVE_ORDER_ID : " + orderId);
        }
        RiskVerifierDoViewResponse doViewResponse = null;
        String htmlPage = null;
        String callbackUrl = null;
        try {
            doViewResponse = riskUtil.getDoViewResponse(e.getSecurityId(), e.getMethod());
            RiskVerifierPayload riskVerifierPayload = riskUtil.setRiskDoViewInCache(doViewResponse, mid, orderId,
                    transId, null);
            if (riskVerifierPayload == null) {
                throw new TheiaControllerException("risk doView unsuccessful.");
            }
            riskVerificationUtil.setCashierRequestInCache(cashierRequest);
            callbackUrl = ConfigurationUtil.getProperty(RiskConstants.THEIA_BASE_URL) + RiskConstants.SUBMIT_URL + mid
                    + "&ORDER_ID=" + orderId + "&route=";
            htmlPage = riskVerificationUtil.getRiskVerifierHtmlPage(riskVerifierPayload, mid, orderId, transId,
                    callbackUrl);
        } catch (FacadeCheckedException e1) {
            LOGGER.error("Failed to get doView response");
            throw new TheiaControllerException("risk doView unsuccessful.");
        }
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(htmlPage);
        return null;
    }

    private CashierRequest getRiskVerifiedCashierRequest(HttpServletRequest request) {
        String token = request.getParameter(RiskConstants.TOKEN);
        String securityId = riskVerificationUtil.getSecurityIdFromPayReuest(request);
        if (StringUtils.isBlank(securityId)) {
            throw new TheiaServiceException("unverified pay request");
        }
        CashierRequest cashierRequest = riskVerificationUtil.getCashierRequestFromCache(token);
        if (cashierRequest == null) {
            throw new TheiaServiceException("No data found in cache for resuming risk verified payment request");
        }
        cashierRequest.getPaymentRequest().setPaymentScenario(PaymentScenario.PAY_VERIFICATION);
        cashierRequest.getPaymentRequest().setSecurityId(securityId);
        String requestId = RequestIdGenerator.generateRequestId();
        cashierRequest.getPaymentRequest().setRequestId(requestId);
        cashierRequest.setRequestId(requestId);
        cashierRequest.setAcquirementId(cashierRequest.getPaymentRequest().getTransId());
        return cashierRequest;
    }

    private String getCallbackUrl(HttpServletRequest request) {
        ExtendedInfoRequestBean extendInfo = theiaSessionDataService.geExtendedInfoRequestBean(request);
        if (extendInfo == null || StringUtils.isBlank(extendInfo.getCallBackURL())) {
            throw new TheiaControllerException("Callback URL could not be obtained. Will break the flow now.");
        }
        return extendInfo.getCallBackURL();
    }

    private String getTxnAmount(CashierRequest cashierRequest) {
        if (cashierRequest != null) {
            Map<String, String> extendInfoMap = cashierRequest.getPaymentRequest().getExtendInfo();
            if (extendInfoMap != null) {
                String txnAmount = extendInfoMap.get("totalTxnAmount");
                return txnAmount != null ? txnAmount : "";
            }
        }
        return "";
    }

    private String pushForPageLoad(HttpServletRequest request, CashierRequest cashierRequest, PaytmValidationException e) {
        if (e.getType() != null) {
            if (PaytmValidationExceptionType.INVALID_BANK_FORM.equals(e.getType())) {
                try {
                    LOGGER.error("{} found for issuing bank {} and pay method {}", e.getType()
                            .getValidationFailedCode(), cashierRequest.getValidationRequest().getSelectedBank(),
                            cashierRequest.getValidationRequest().getTxnMode());
                } catch (Exception ex) {
                    LOGGER.error("Error in Parameter Validation");
                }
            } else if (PaytmValidationExceptionType.TRANSACTION_CLOSED.equals(e.getType())) {
                String responsePage = null;
                if (ResponseConstants.NATIVE_RETRY_COUNT_BREACHED.getMessage().equals(e.getReason())) {
                    responsePage = getTxnClosedResponsePage(request, null, e.getReason());
                } else {
                    responsePage = getTxnClosedResponsePage(request, cashierRequest, null);
                }
                if (StringUtils.isNotBlank(responsePage)) {
                    theiaSessionDataService.setRedirectPageInSession(request, responsePage);
                    return theiaViewResolverService.returnForwarderPage();
                } else {
                    throw new TheiaControllerException("Could not get response page");
                }
            }
            LOGGER.error("Parameter_Validation_Failed_:_{}", e.getType().getValidationFailedCode());
        } else {
            LOGGER.error("Parameter validation failed : ", e);
        }

        ServletContext context = request.getSession().getServletContext();
        request.setAttribute("path", context.getContextPath());

        if ((e instanceof SaveCardValidationException)
                && PaytmValidationExceptionType.DELETED_SAVECARD.equals(e.getType())) {
            SaveCardValidationException cardException = (SaveCardValidationException) e;
            LOGGER.warn("Saved card exception for card id : {}", cardException.getSaveCardID());
            theiaCardService.processDeleteCard(request, cardException.getSaveCardID(), false);
        }

        String paymentTypeID = null;
        if (StringUtils.isNotBlank(request.getParameter(PaymentRequestParam.SAVED_CARD_ID.getValue()))) {
            paymentTypeID = "SC";
        } else {
            paymentTypeID = request.getParameter(PaymentRequestParam.TXN_MODE.getValue());
        }

        TransactionInfo txnInfo = theiaSessionDataService.getTxnInfoFromSession(request);
        txnInfo.setTxnMode(paymentTypeID);
        txnInfo.setPaymentTypeId(paymentTypeID);

        if (PaytmValidationExceptionType.RISK_FRAUD_MESSAGE.equals(e.getType())) {
            return theiaViewResolverService.returnRiskPaymentPage(request);
        }

        checkRetryCountForDigitalCredit(request, e);
        checkRetryCountForPaymentsBank(request, cashierRequest, e);
        setErrorParams(request, e);

        return theiaViewResolverService.returnPaymentPage(request);
    }

    private String getTxnClosedResponsePage(HttpServletRequest request, CashierRequest cashierRequest,
            String responseMsg) {
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setMid(request.getParameter("MID"));
        transactionResponse.setOrderId((String) request.getAttribute("ORDER_ID"));
        transactionResponse.setPaymentMode(PaymentModeMapperUtil.getNewPayModeDisplayName(request
                .getParameter("txnMode")));
        String txnAmount = getTxnAmount(cashierRequest);
        if (StringUtils.isNotBlank(txnAmount)) {
            transactionResponse.setTxnAmount(txnAmount);
        }

        transactionResponse.setResponseCode(ResponseConstants.MERCHANT_FAILURE_RESPONSE.getCode());
        if (responseMsg != null) {
            transactionResponse.setResponseMsg(responseMsg);
        } else {
            transactionResponse.setResponseMsg(ResponseConstants.MERCHANT_FAILURE_RESPONSE.getMessage());
            ResponseCodeDetails responseCodeDetails = null;
            try {
                responseCodeDetails = responseCodeService
                        .getPaytmResponseCodeDetails(ResponseConstants.MERCHANT_FAILURE_RESPONSE.getCode());
                EXT_LOGGER.customInfo(
                        "Mapping response - ResponseCodeDetails for MERCHANT_FAILURE_RESPONSE code :: {}",
                        responseCodeDetails);
                if (responseCodeDetails != null) {
                    transactionResponse
                            .setResponseMsg(StringUtils.isNotBlank(responseCodeDetails.getDisplayMessage()) ? responseCodeDetails
                                    .getDisplayMessage() : responseCodeDetails.getRemark());
                }
            } catch (Exception e) {
                LOGGER.error("Exception occured while fetching response Code details ", e);
            }
        }

        transactionResponse.setCallbackUrl(getCallbackUrl(request));
        transactionResponse.setTransactionStatus(ExternalTransactionStatus.TXN_FAILURE.name());
        return theiaResponseGenerator.getFinalHtmlResponse(transactionResponse);
    }

    /**
     * @param request
     * @param cashierRequest
     * @throws PaytmValidationException
     */

    private void processCashierRequest(final HttpServletRequest request, final HttpServletResponse response,
            CashierRequest cashierRequest) throws PaytmValidationException, ServletException, IOException {

        try {
            switch (cashierRequest.getCashierWorkflow()) {
            case ADD_MONEY_ISOCARD:
            case ADD_MONEY_NB:
            case ADD_MONEY_ATM:
            case ADD_MONEY_UPI:
                initiatePayment(request, cashierRequest, true);
                break;
            case ISOCARD:
            case NB:
            case ATM:
            case UPI:
                initiatePayment(request, cashierRequest, false);
                break;
            case IMPS:
            case COD:
            case WALLET:
            case ADD_MONEY_IMPS:
            case DIGITAL_CREDIT_PAYMENT:
                initiateAndSubmitPaymentWrapper(request, response, cashierRequest);
                break;
            case SUBSCRIPTION:
            case DIRECT_BANK_CARD_PAYMENT:
            case RISK_POLICY_CONSULT:
            default:
                break;
            }
        } catch (PaytmValidationException e) {
            LOGGER.info("something went wrong while processing payment ");
            if (PaytmValidationExceptionType.INVALID_PASS_CODE == e.getType()) {
                LOGGER.info("rolling back merchant limit");

                if (request.getAttribute(isLimitRollBacked) == null
                        || request.getAttribute(isLimitRollBacked).equals("true") == false) {
                    rollbackMerchantLimit(request, cashierRequest, true);
                }
            }
            throw e;
        }
    }

    private void setStateLoggingParams(final HttpServletRequest request) {
        // Added information for transaction State Management.
        TransactionInfo transactionInfo = theiaSessionDataService.getTxnInfoFromSession(request);

        if (transactionInfo == null) {
            throw new CoreSessionExpiredException("Unable to get transaction info from session");
        }

        ThreadLocalUtil.set(new TxnStateLog(transactionInfo.getTxnId(), transactionInfo.getMid(), transactionInfo
                .getOrderId(), transactionInfo.getTxnAmount()));
    }

    public void rollbackMerchantLimit(HttpServletRequest httpServletRequest, CashierRequest cashierRequest,
            boolean isTxnAmtInPaise) {
        if (cashierRequest == null || cashierRequest.getCashierMerchant() == null
                || cashierRequest.getPaymentRequest().getPayBillOptions() != null) {
            return;
        }
        final String mid = cashierRequest.getCashierMerchant().getMerchantId();
        final String amount = String.valueOf(cashierRequest.getPaymentRequest().getPayBillOptions().getServiceAmount());
        final ExtendedInfoRequestBean extendedInfoRequestBean = sessionDataService
                .geExtendedInfoRequestBean(httpServletRequest);

        if (extendedInfoRequestBean == null) {
            LOGGER.info("rolling back merchant limit in as extendedInfo is null");
            return;
        }

        boolean isLimitUpdated = Boolean.valueOf(cashierRequest.getPaymentRequest().getExtendInfo()
                .get(TheiaConstant.ExtendedInfoPay.IS_MERCHANT_LIMIT_UPDATED_FOR_PAY));
        boolean isLimitEnabled = Boolean.valueOf(cashierRequest.getPaymentRequest().getExtendInfo()
                .get(TheiaConstant.ExtendedInfoPay.IS_MERCHANT_LIMIT_ENABLED_FOR_PAY));

        extendedInfoRequestBean.setMerchantLimitUpdated(isLimitUpdated);
        extendedInfoRequestBean.setMerchantLimitEnabled(isLimitEnabled);
        // Calling velocity service
        if (isLimitUpdated && isLimitEnabled) {

            try {
                EXT_LOGGER.customInfo(
                        "rolling back merchant velocity limit for mid={} ,txnAmount={} , isTxnAmtInPaise = {}", mid,
                        amount, isTxnAmtInPaise);

                String txnAmtInRupees = (isTxnAmtInPaise) ? AmountUtils.getTransactionAmountInRupee(amount) : amount;
                boolean isRolledBack = merchantVelocityUtil.rollbackMerchantVelocityLimitUpdate(mid, txnAmtInRupees,
                        extendedInfoRequestBean);
                LOGGER.info("rollback status " + isRolledBack);
                if (!isRolledBack && isLimitUpdated) {
                    httpServletRequest.setAttribute(isLimitRollBacked, "true");
                    LOGGER.warn("Not able to rollback merchant velocity limit update");
                }
            } catch (Exception e) {
                LOGGER.warn("Exception in rolling back merchant velocity limit update");
                LOGGER.warn(ExceptionUtils.getStackTrace(e));
            }
        }
    }

    /**
     * @param request
     * @param cashierRequest
     * @throws PaytmValidationException
     */
    private void initiateAndSubmitPayment(final HttpServletRequest request, CashierRequest cashierRequest)
            throws PaytmValidationException {
        LOGGER.debug("Request received for initiate and submit is :: {}", cashierRequest);

        GenericCoreResponseBean<DoPaymentResponse> paymentResponse = paymentServiceImpl
                .initiateAndSubmit(cashierRequest);

        if (paymentResponse != null
                && paymentResponse.getResponse() != null
                && paymentResponse.getResponse().getPaymentStatus() != null
                && PaymentStatus.FAIL.name().equalsIgnoreCase(
                        paymentResponse.getResponse().getPaymentStatus().getPaymentStatusValue())) {

            final EnvInfoRequestBean envInfo = EnvInfoUtil.fetchEnvInfo(request);
            rollbackMerchantLimit(request, cashierRequest, true);
            if (paymentResponse.getResponse().getFundOrderStatus() != null) {
                BizCancelFundOrderRequest cancelFundOrderRequest = new BizCancelFundOrderRequest(paymentResponse
                        .getResponse().getFundOrderStatus().getFundOrderId(), envInfo);
                orderServiceImpl.closeFundOrder(cancelFundOrderRequest);
            } else if (paymentResponse.getResponse().getTransactionStatus() != null) {
                BizCancelOrderRequest cancelAcquiringOrderRequest = new BizCancelOrderRequest(cashierRequest
                        .getCashierMerchant().getInternalMerchantId(), cashierRequest.getAcquirementId(),
                        "Max payment retry count limit reached");
                orderServiceImpl.closeOrder(cancelAcquiringOrderRequest);
            }
        }

        if (cashierRequest.isLinkBasedPaymentRequest()) {
            setRequestAttributesForLinkPayments(request, cashierRequest, paymentResponse);
        }

        String responsePage = theiaResponseGenerator.getMerchantResponse(paymentResponse);

        if (StringUtils.isBlank(responsePage)) {
            throw new TheiaControllerException("Could not get response page");
        }

        theiaSessionDataService.setRedirectPageInSession(request, responsePage);
    }

    private void initiateAndSubmitPaymentWrapper(final HttpServletRequest request, HttpServletResponse response,
            CashierRequest cashierRequest) throws PaytmValidationException, ServletException, IOException {

        initiateAndSubmitPayment(request, cashierRequest);

        if (cashierRequest.isLinkBasedPaymentRequest()) {
            LOGGER.info("Forwarding request to link payment status page");
            request.getRequestDispatcher(
                    VIEW_BASE + theiaViewResolverService.returnLinkPaymentStatusPage(request) + ".jsp").forward(
                    request, response);
        }
    }

    private void setRequestAttributesForLinkPayments(HttpServletRequest request, CashierRequest cashierRequest,
            GenericCoreResponseBean<DoPaymentResponse> paymentResponse) {

        if (paymentResponse.getResponse().getPaymentStatus() != null) {
            LOGGER.info("Generating final txn status page for Link based payments for transaction ID = {}",
                    paymentResponse.getResponse().getPaymentStatus().getTransId());

            request.setAttribute(PAYMENT_STATUS, paymentResponse.getResponse().getPaymentStatus()
                    .getPaymentStatusValue().toUpperCase());
            Date date = paymentResponse.getResponse().getPaymentStatus().getPaidTime();
            if (date == null) {
                date = paymentResponse.getResponse().getTransactionStatus().getCreatedTime();
            }
            request.setAttribute(TXN_DATE, LinkBasedPaymentHelper.getLinkPaymentStatusDate(date));
            request.setAttribute(TRANSACTION_ID, paymentResponse.getResponse().getPaymentStatus().getTransId());
            request.setAttribute(
                    TXN_AMOUNT,
                    AmountUtils.getTransactionAmountInRupee(paymentResponse.getResponse().getPaymentStatus()
                            .getTransAmountValue()));
            request.setAttribute(SHOW_VIEW_FLAG, PAYMENT_SCREEN);
            request.setAttribute(MERCHANT_NAME, cashierRequest.getMerchantName());
            request.setAttribute(MERCHANT_IMAGE, cashierRequest.getMerchantImage());
            request.setAttribute(TheiaConstant.RequestParams.ORDER_ID, paymentResponse.getResponse()
                    .getTransactionStatus().getMerchantTransId());
            TransactionResponse tranResponse = new TransactionResponse();
            if (AcquirementStatusType.CLOSED.toString().equals(
                    paymentResponse.getResponse().getTransactionStatus().getStatusDetailType())) {
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

    /**
     * @param request
     * @param cashierRequest
     * @throws PaytmValidationException
     */
    private void initiatePayment(final HttpServletRequest request, CashierRequest cashierRequest, boolean isFundOrder)
            throws PaytmValidationException {
        GenericCoreResponseBean<InitiatePaymentResponse> initiatePaymentResponse;

        MerchantPreferenceStore merchantPreferenceStore = merchantPreferenceService
                .getMerchantPreferenceStore(cashierRequest.getCashierMerchant().getMerchantId());
        TransactionInfo txnInfo = theiaSessionDataService.getTxnInfoFromSession(request);
        ExtendedInfoRequestBean extendedInfoRequestBean = theiaSessionDataService.geExtendedInfoRequestBean(request);
        cashierRequest.setTransCreatedtime(txnInfo.getTransCreatedTime());
        cashierRequest.setCardTokenRequired(txnInfo.isCardTokenRequired());
        cashierRequest.setCartValidationRequired(txnInfo.isCartValidationRequired());
        if (StringUtils.isNotEmpty(txnInfo.getSsoToken())) {
            cashierRequest.setSsoToken(txnInfo.getSsoToken());
        } else if (extendedInfoRequestBean != null && StringUtils.isNotEmpty(extendedInfoRequestBean.getSsoToken())) {
            cashierRequest.setSsoToken(extendedInfoRequestBean.getSsoToken());
        }

        String txnMode = request.getParameter("txnMode");
        // Check the merchant for the selected convenience fee
        if (merchantPreferenceStore.getPreferences() != null
                && merchantPreferenceStore.getPreferences().get(RISK_FEE) != null
                && merchantPreferenceStore.getPreferences().get(RISK_FEE).isEnabled()) {
            LOGGER.info("Merchant preference applied for selected convenience fee");

            // Check the txn mode for the selected convenience fee
            if (txnInfo.isRiskAllowed() && ("CC".equals(txnMode) || "DC".equals(txnMode))) {
                LOGGER.info("Consulting from the RISK_API");

                LoginInfo loginInfo = theiaSessionDataService.getLoginInfoFromSession(request);
                TransactionConfig txnConfig = theiaSessionDataService.getTxnConfigFromSession(request);
                try {
                    cashierRequest.setProductCode(ProductCodes.getProductById(txnConfig.getProductCode()));
                } catch (FacadeInvalidParameterException e) {
                    throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PRODUCT_CODE);
                }

                String userId = "";

                if ((loginInfo != null) && (loginInfo.getUser() != null)
                        && !StringUtils.isBlank(loginInfo.getUser().getPayerUserID())) {
                    userId = loginInfo.getUser().getPayerUserID();
                }

                initiatePaymentResponse = paymentServiceImpl.chargeFeeOnRiskAnalysis(cashierRequest, userId);
                ConsultDetails consultDetails = initiatePaymentResponse.getResponse() != null ? initiatePaymentResponse
                        .getResponse().getConsultDetails() : null;

                if (consultDetails != null) {
                    txnInfo.setTotalConvenienceCharges(consultDetails.getTotalConvenienceCharges().toString());
                    txnInfo.setTotalTransactionAmount(consultDetails.getTotalTransactionAmount().toString());
                    request.setAttribute(TOKEN, initiatePaymentResponse.getResponse().getCacheCardTokenId());

                    throw new PaytmValidationException(PaytmValidationExceptionType.RISK_FRAUD_MESSAGE);
                }
            } else {
                initiatePaymentResponse = paymentServiceImpl.initiate(cashierRequest);
            }
        } else {
            initiatePaymentResponse = paymentServiceImpl.initiate(cashierRequest);
        }

        // Checking if bank form re fetching is required
        if (internalPaymentRetryService.isInternalPaymentRetryRequired(initiatePaymentResponse)) {
            GenericCoreResponseBean<InitiatePaymentResponse> retryInitiatePaymentResponse = (GenericCoreResponseBean<InitiatePaymentResponse>) internalPaymentRetryService
                    .retryBankFormFetchWithPayment(cashierRequest, paymentServiceImpl);
            if (retryInitiatePaymentResponse != null) {
                initiatePaymentResponse = retryInitiatePaymentResponse;
            }
        }

        if (null == initiatePaymentResponse.getResponse() || !initiatePaymentResponse.isSuccessfullyProcessed()) {
            LOGGER.info("rolling back velocity limit ");
            rollbackMerchantLimit(request, cashierRequest, true);
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE);
        }

        if ((null != initiatePaymentResponse.getResponse().getCashierPaymentStatus())
                && StringUtils.isBlank(initiatePaymentResponse.getResponse().getCashierPaymentStatus()
                        .getWebFormContext())) {
            // for handling retry request, to save only that card or vpa through
            // which successful txn happened
            cacheCardService.deleteCardDetailsFromCache(cashierRequest.getPaymentRequest().getTransId());
            returnToCashierPageWithErrorMessage(initiatePaymentResponse);
        }
        initiatePaymentResponse.getResponse().getCashierPaymentStatus();
        redisUtil.pushCashierIdForAcquirementId(cashierRequest.getAcquirementId(), initiatePaymentResponse
                .getResponse().getCashierRequestId());
        processForRedirectToBank(request, cashierRequest, isFundOrder, initiatePaymentResponse);

    }

    private void returnToCashierPageWithErrorMessage(
            GenericCoreResponseBean<InitiatePaymentResponse> initiatePaymentResponse) throws PaytmValidationException {
        String errorMessage = PaytmValidationExceptionType.INVALID_BANK_FORM.getValidationFailedMsg();

        if (null == initiatePaymentResponse
                || null == initiatePaymentResponse.getResponse()
                || null == initiatePaymentResponse.getResponse().getCashierPaymentStatus()
                || StringUtils.isBlank(initiatePaymentResponse.getResponse().getCashierPaymentStatus()
                        .getInstErrorCode())) {
            LOGGER.error("Required data is missing : {}", MaskingUtil.maskObject(initiatePaymentResponse,
                    SSOTOKEN.getFieldName(), SSOTOKEN.getPrex(), SSOTOKEN.getEndx()));
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_BANK_FORM);
        }

        try {
            ResponseCodeDetails responseCodeDetails = responseCodeService
                    .getResponseCodeDetails(initiatePaymentResponse.getResponse().getCashierPaymentStatus()
                            .getInstErrorCode());
            EXT_LOGGER.customInfo("Mapping response - ResponseCodeDetails :: {} for error code :: {}",
                    responseCodeDetails, initiatePaymentResponse.getResponse().getCashierPaymentStatus()
                            .getInstErrorCode());
            if (null != responseCodeDetails) {
                errorMessage = StringUtils.isNotBlank(responseCodeDetails.getDisplayMessage()) ? responseCodeDetails
                        .getDisplayMessage() : responseCodeDetails.getRemark();
            }
        } catch (Exception e) {
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_BANK_FORM);
        }

        throw new PaytmValidationException(errorMessage, PaytmValidationExceptionType.INVALID_BANK_FORM);
    }

    private void processForRedirectToBank(final HttpServletRequest request, CashierRequest cashierRequest,
            boolean isFundOrder, GenericCoreResponseBean<InitiatePaymentResponse> initiatePaymentResponse)
            throws PaytmValidationException {
        if (CashierWorkflow.UPI.equals(cashierRequest.getCashierWorkflow())
                || CashierWorkflow.ADD_MONEY_UPI.equals(cashierRequest.getCashierWorkflow())) {

            if ((null != initiatePaymentResponse.getResponse())
                    && (null != initiatePaymentResponse.getResponse().getCashierPaymentStatus())) {
                theiaSessionDataService.setUPIAccepted(request, true);
            } else {
                theiaSessionDataService.setUPIAccepted(request, false);
            }

            UPITransactionInfo upiTransactionInfo = generateRequiredUPITransactionInfo(request, cashierRequest,
                    isFundOrder, initiatePaymentResponse.getResponse());
            if (null != cashierRequest.getUpiPushRequest() && cashierRequest.getUpiPushRequest().isUpiPushTxn()) {
                try {
                    if (!cashierRequest.getUpiPushRequest().isUpiPushExpressSupported()) {
                        UPIPushInitiateRequest instaCallbackRequest = createUpiPushInitiateRequestBean(
                                upiTransactionInfo.getMerchantVpaTxnInfo(), cashierRequest);
                        final UPIPushInitiateResponse instaCallbackResponse = bankProxyServiceImpl
                                .initiateUpiPushTransaction(instaCallbackRequest);
                        if (!instaCallbackResponse.isSuccessfullyProcessed()) {
                            LOGGER.error("Error while sending callback to Insta for UPI Push :{}",
                                    instaCallbackResponse.getResponseMessage());
                        }
                    }
                    LooperRequest looperRequest = new LooperRequest(initiatePaymentResponse.getResponse()
                            .getCashierRequestId(), cashierRequest.getCashierMerchant().getInternalMerchantId(),
                            cashierRequest.getAcquirementId());
                    cashierRequest.setLooperRequest(looperRequest);
                    LOGGER.debug("cashier request is :{}", cashierRequest);
                    GenericCoreResponseBean<DoPaymentResponse> cashierResponse = paymentServiceImpl
                            .submit(cashierRequest);
                    LOGGER.debug("cashierResponse received is: {}", cashierResponse);
                    ProcessUPIPushResponse(cashierResponse, cashierRequest, request);
                    return;
                } catch (FacadeCheckedException | CashierCheckedException e) {
                    LOGGER.error("Exception while sending callback to Insta for UPI Push : {}", e);
                    returnToCashierPageWithErrorMessage(initiatePaymentResponse);
                }
            }
            theiaSessionDataService.setUPITransactionInfoInSession(request, upiTransactionInfo);
        }
        String bankPage = theiaResponseGenerator.getBankPage(initiatePaymentResponse);
        TransactionInfo txnInfo = theiaSessionDataService.getTxnInfoFromSession(request);
        String orderID = txnInfo.getOrderId();
        String mid = txnInfo.getMid();
        if (txnInfo.getRequestType().equalsIgnoreCase(ERequestType.LINK_BASED_PAYMENT.getType())
                || txnInfo.getRequestType().equalsIgnoreCase(ERequestType.LINK_BASED_PAYMENT_INVOICE.getType())) {
            LOGGER.info("Setting transaction info in cache with request type for {}", txnInfo.getRequestType());
            transactionCacheUtils.putTransInfoInCacheWrapper(cashierRequest.getPaymentRequest().getTransId(), mid,
                    orderID, isFundOrder, ERequestType.getByRequestType(txnInfo.getRequestType()));
        } else {
            transactionCacheUtils.putTransInfoInCache(cashierRequest.getPaymentRequest().getTransId(), mid, orderID,
                    isFundOrder, txnInfo.getRequestType());
        }

        theiaSessionDataService.setRedirectPageInSession(request, bankPage);
        paymentRequestHelper.incrementRetryCount(txnInfo.getTxnId());
    }

    public UPIPushInitiateRequest createUpiPushInitiateRequestBean(final MerchantVpaTxnInfo merchantVpaTxnInfo,
            CashierRequest cashierRequest) {

        UPIPushInitiateRequest callbackRequest = new UPIPushInitiateRequest();

        LOGGER.info("MerchantVpaTxnInfo : {}", merchantVpaTxnInfo);
        UPIPushRequest upiPushRequest = cashierRequest.getUpiPushRequest();
        PaytmBanksVpaDefaultDebitCredit defaultDebit = upiPushRequest.getSarvatraVpaDetails().getDefaultDebit();

        callbackRequest.setAmount(merchantVpaTxnInfo.getTxnAmount());
        callbackRequest.setExternalSrNo(merchantVpaTxnInfo.getExternalSrNo());
        callbackRequest.setUrl(merchantVpaTxnInfo.getRU());
        callbackRequest.setAccountNumber(defaultDebit.getAccount());
        callbackRequest.setBankName(defaultDebit.getBank());
        callbackRequest.setCredBlock(defaultDebit.getCredsAllowed().toString());
        callbackRequest.setDeviceId(upiPushRequest.getDeviceId());
        callbackRequest.setIfsc(defaultDebit.getIfsc());
        callbackRequest.setMobileNo(upiPushRequest.getMobile());
        callbackRequest.setMpin(upiPushRequest.getMpin());
        callbackRequest.setPayerVpa(upiPushRequest.getSarvatraVpaDetails().getName());
        callbackRequest.setSeqNo(upiPushRequest.getSeqNo());
        callbackRequest.setOrderId(upiPushRequest.getOrderId());
        callbackRequest.setAppId(upiPushRequest.getAppId());

        LOGGER.info("UPIPushInitiateRequest : {}", callbackRequest);

        return callbackRequest;
    }

    private void resetSessionData(HttpServletRequest request) {
        resetAutoLoginFlag(request);
        resetRetryData(request);
    }

    private void resetRetryData(HttpServletRequest request) {
        TransactionInfo txnInfo = theiaSessionDataService.getTxnInfoFromSession(request);
        if (txnInfo != null && txnInfo.isRetry()) {
            txnInfo.setRetry(false);
            theiaSessionDataService.removeAttributeFromSession(request, SessionDataAttributes.retryPaymentInfo);
        }
    }

    private void resetAutoLoginFlag(HttpServletRequest request) {
        final LoginInfo loginInfo = theiaSessionDataService.getLoginInfoFromSession(request, true);
        String oAuthInfoJson = loginInfo.getOauthInfo();
        if (StringUtils.isBlank(oAuthInfoJson)) {
            return;
        }

        JSONObject jsonObject = new JSONObject(oAuthInfoJson);
        jsonObject.remove(AUTO_LOGIN);
        loginInfo.setOauthInfo(jsonObject.toString());
    }

    private void setErrorParams(HttpServletRequest request, PaytmValidationException paytmValidationException) {
        if (paytmValidationException.getType() == null) {
            String errorMessage = "Some error occured, Please try after some time";
            if (!StringUtils.isEmpty(paytmValidationException.getErrorMessage())) {
                errorMessage = paytmValidationException.getErrorMessage();
            }
            request.setAttribute("errorMsg", errorMessage);
            return;
        }

        Map<String, String> errorMessages = new HashMap<>();

        switch (paytmValidationException.getType()) {
        case INVALID_BANK_FORM:
            String errorMessage = StringUtils.isNotBlank(paytmValidationException.getErrorMessage()) ? paytmValidationException
                    .getErrorMessage() : PaytmValidationExceptionType.INVALID_BANK_FORM.getValidationFailedMsg();
            request.setAttribute("errorMsg", errorMessage);
            break;
        case INVALID_PAYMODE:
        case INVALID_PAYMODE_CC:
        case INVALID_PAYMODE_DC:
        case INVALID_PAYMODE_EMI:
        case INVALID_PAYMODE_SC:
        case INVALID_RISK_PARAM:
        case INVALID_PAYMODE_NB:
        case INVALID_PARAM_CASHIER_PAY:
        case INVALID_PARAM_CACHE_CARD:
        case INVALID_PAYMODE_INTL_CARD:
        case INVALID_SUBS_PAYMENT_DETAILS:
        case INVALID_CONSULT_DETAILS:
        case INVALID_PRODUCT_CODE:
        case NO_SAVED_CARD_SELECTED:
        case INVALID_PAYMODE_PAYTMCC:
        case INVALID_PAYMODE_PAYTM_PAYMENTS_BANK:
        case INVALID_ADD_MONEY_CARD:
        case INVALID_MONTHLY_LIMIT:
        case INVALID_ADD_MONEY_USER:
        case PROMO_VALIDATION_ERROR:
            request.setAttribute("errorMsg", paytmValidationException.getType().getValidationFailedMsg());
            break;
        case INVALID_PASS_CODE:
            String txnMode = request.getParameter(PaymentRequestParam.TXN_MODE.getValue());
            DigitalCreditInfo digitalCreditInfo = theiaSessionDataService.getDigitalCreditInfoFromSession(request,
                    false);
            if (digitalCreditInfo != null && StringUtils.isNotBlank(txnMode)
                    && EPayMethod.PAYTM_DIGITAL_CREDIT.getOldName().equals(txnMode)) {
                digitalCreditInfo.setInvalidPassCodeMessage(paytmValidationException.getReason());
            }
            SavingsAccountInfo savingsAccountInfo = theiaSessionDataService.getSavingsAccountInfoFromSession(request,
                    false);
            if (savingsAccountInfo != null && StringUtils.isNotBlank(txnMode)
                    && (EPayMethod.NET_BANKING.getOldName().equals(txnMode) || PPB_BANK_CODE.equals(txnMode))) {
                savingsAccountInfo.setInvalidPassCodeMessage(paytmValidationException.getReason());
            }
            break;
        case INVALID_CARD:
        case INVALID_BANK_CODE:
        case INVALID_CARD_SCHEME:
        case INVALID_CARD_TYPE:
        case INVALID_CARD_LENGTH:
        case INVALID_CREDIT_CARD:
        case INVALID_SAVED_CARD:
        case INVALID_BIN_DETAILS:
        case INVALID_CREDIT_CARD_LUHN:
        case INVALID_DEBIT_CARD_LUHN:
        case INVALID_BIN_CARD_TYPE:
        case INVALID_BIN_CARD_NAME:
        case INVALID_INTERNATIONAL_CARD:
            errorMessages.put(PaytmValidationExceptionType.INVALID_CARD.name(),
                    paytmValidationException.getErrorMessage());
            break;
        case INVALID_CARD_EXPIRY:
        case INVALID_CARD_EXPIRY_YEAR:
            errorMessages.put(PaytmValidationExceptionType.INVALID_CARD_EXPIRY.name(),
                    paytmValidationException.getErrorMessage());
            break;
        case INVALID_CVV:
        case INVALID_CVV_UNDEFIEND:
        case INVALID_CVV_NOT_NUMERIC:
        case INVALID_CVV_LENGTH:
            errorMessages.put(PaytmValidationExceptionType.INVALID_CVV.name(),
                    paytmValidationException.getErrorMessage());
            break;
        case INVALID_VPA:
            errorMessages.put(PaytmValidationExceptionType.INVALID_VPA.name(),
                    paytmValidationException.getErrorMessage());
            break;
        case INVALID_PASS_CODE_BLANK:
            errorMessages.put(PaytmValidationExceptionType.INVALID_PASS_CODE_BLANK.name(), paytmValidationException
                    .getType().getValidationFailedMsg());
            break;
        case DELETED_SAVECARD:
        case INVALID_MMID:
        case INVALID_MOBILE:
        case INVALID_OTP:
        case INVALID_PROMO_DETAILS:
        case MOBILE_NO:
        case INVALID_RISK_CONSULT:
        case RISK_FRAUD_MESSAGE:
        case INVALID_PAYMODE_BAJAJFN_EMI:
        case INVALID_PAYMODE_UPI:
        case INVALID_PAYMODE_UPIPUSH:
        default:
            errorMessages.put(paytmValidationException.getType().name(), paytmValidationException.getErrorMessage());
            break;
        }

        request.setAttribute("validationErrors", errorMessages);
    }

    private UPITransactionInfo generateRequiredUPITransactionInfo(HttpServletRequest request,
            CashierRequest cashierRequest, boolean isFundOrder, InitiatePaymentResponse initiatePaymentResponse) {

        UPITransactionInfo transactionInfo = (UPITransactionInfo) request.getSession().getAttribute(
                SessionDataAttributes.upiTransactionInfo.name());
        if (transactionInfo == null) {
            transactionInfo = new UPITransactionInfo();
        }

        String vpaID = null;
        if (null != cashierRequest) {
            transactionInfo.setAcquirementId(cashierRequest.getAcquirementId());
            if (null != cashierRequest.getCashierMerchant()) {
                transactionInfo.setPaytmMerchantId(cashierRequest.getCashierMerchant().getMerchantId());
                transactionInfo.setAlipayMerchantId(cashierRequest.getCashierMerchant().getInternalMerchantId());
            }
            vpaID = cashierRequest.getPaymentRequest().getPayBillOptions().getChannelInfo()
                    .get(TheiaConstant.ExtendedInfoKeys.ChannelInfoKeys.VIRTUAL_PAYMENT_ADDRESS);
        }
        transactionInfo.setCashierRequestId(initiatePaymentResponse.getCashierRequestId());
        transactionInfo.setVpaID(vpaID);
        transactionInfo.setMerchantTransId(request.getParameter(TheiaConstant.RequestParams.ORDER_ID));

        String vpa = transactionInfo.getVpaID();
        ThemeInfo themeInfo = theiaSessionDataService.getThemeInfoFromSession(request, false);
        if (StringUtils.isNotBlank(vpa)) {
            String[] vpaHandle = vpa.split("@");
            if (vpaHandle != null && vpaHandle.length == 2) {
                if ("paytm".equalsIgnoreCase(vpaHandle[1])
                        && themeInfo != null
                        && ("WAP".equalsIgnoreCase(themeInfo.getChannel()) || "APP".equalsIgnoreCase(themeInfo
                                .getChannel()))) {
                    transactionInfo.setPaytmVpa(true);
                }
            }
        }

        TransactionInfo transactionInfo1 = theiaSessionDataService.getTxnInfoFromSession(request);

        if (transactionInfo1 != null && cashierRequest.getUpiPushRequest() != null
                && !cashierRequest.getUpiPushRequest().isUpiPushExpressSupported()) {

            String webFormContext = null;

            if (initiatePaymentResponse != null && initiatePaymentResponse.getCashierPaymentStatus() != null) {
                webFormContext = initiatePaymentResponse.getCashierPaymentStatus().getWebFormContext();
            }

            if (StringUtils.isNotBlank(webFormContext)) {
                try {
                    MerchantVpaTxnInfo merchantVpaTxnInfo = JsonMapper.mapJsonToObject(webFormContext,
                            MerchantVpaTxnInfo.class);

                    transactionInfo.setMerchantVpaTxnInfo(merchantVpaTxnInfo);
                    if (StringUtils.isNotBlank(merchantVpaTxnInfo.getVpa())) {
                        transactionInfo1.setVPA(merchantVpaTxnInfo.getVpa());
                        if (cashierRequest.getCashierMerchant() != null
                                || cashierRequest.getCashierMerchant().getMerchantId() != null) {
                            merchantVpaTxnInfo.setMaskedMerchantVpa(VPAHelper.setMaskedMerchantVpa(
                                    merchantVpaTxnInfo.getVpa(), cashierRequest.getCashierMerchant().getMerchantId()));
                        } else {
                            merchantVpaTxnInfo.setMaskedMerchantVpa(merchantVpaTxnInfo.getVpa());
                        }
                    }
                    if (StringUtils.isNotBlank(merchantVpaTxnInfo.getTxnAmount())) {
                        // String txnAmt =
                        // AmountUtils.getTransactionAmountInRupee(merchantVpaTxnInfo.getTxnAmount());
                        transactionInfo.setTransactionAmount(merchantVpaTxnInfo.getTxnAmount());
                    }
                } catch (Exception e) {
                    LOGGER.error("Exception while fetching Vpa and transaction Amount from webformcontext:", e);
                    transactionInfo1.setVPA("paytm@icici");
                    String walletAmount = request.getParameter(PaymentRequestParam.WALLET_AMOUNT.getValue());
                    if (StringUtils.isNotBlank(walletAmount) && NumberUtils.isNumber(walletAmount)) {
                        Float txnAmount = Float.valueOf(transactionInfo1.getTxnAmount());
                        Float walletAmout = Float.valueOf(walletAmount);
                        transactionInfo.setTransactionAmount(String.valueOf(txnAmount - walletAmout));
                    } else {
                        transactionInfo.setTransactionAmount(transactionInfo1.getTxnAmount());
                    }
                }
            }
        }

        transactionInfo.setBaseUrl(ConfigurationUtil
                .getProperty(TheiaConstant.UpiConfiguration.MERCHANT_STATUS_SERVICE_BASE_URL)
                + ConfigurationUtil.getProperty(TheiaConstant.UpiConfiguration.MERCHANT_STATUS_SERVICE_API));
        transactionInfo.setStatusTimeOut(ConfigurationUtil
                .getProperty(TheiaConstant.UpiConfiguration.STATUS_QUERY_TIMEOUT));
        transactionInfo.setStatusTimeOut(upiInfoSessionUtil.getStatusTimeout(upiInfoSessionUtil
                .getPaymentTimeoutinMinsForUpi(cashierRequest.getCashierMerchant().getMerchantId())));
        transactionInfo.setStatusInterval(ConfigurationUtil
                .getProperty(TheiaConstant.UpiConfiguration.STATUS_QUERY_INTERVAL));

        Iterator<String> upiProperties = com.paytm.pgplus.theia.utils.ConfigurationUtil.getUpiProperties();
        String key = null;
        boolean flag = false;
        Map<String, Map<String, String>> map = groupByPsp(upiProperties);
        for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
            Map<String, String> m = entry.getValue();
            for (Map.Entry<String, String> e : m.entrySet()) {
                if (e.getValue().contains(transactionInfo.getVpaID().split("@")[1])) {
                    transactionInfo.setHandlerName(entry.getKey());
                    key = entry.getKey();
                    flag = true;
                    break;
                }
            }
            if (flag)
                break;
        }
        if (map.get(key) != null && map.get(key).get(key.concat(".displayname")) != null) {
            transactionInfo.setDisplayName(map.get(key).get(key.concat(".displayname")));
        } else {
            transactionInfo.setDisplayName("");
        }

        return transactionInfo;
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

    private void checkRetryCountForDigitalCredit(HttpServletRequest request, PaytmValidationException e) {

        if (PayMethod.PAYTM_DIGITAL_CREDIT.getMethod().equals(
                request.getParameter(PaymentRequestParam.TXN_MODE.getValue()))
                && PaytmValidationExceptionType.INVALID_PASS_CODE.equals(e.getType())) {
            DigitalCreditInfo digitalCreditInfo = theiaSessionDataService.getDigitalCreditInfoFromSession(request,
                    false);
            int retryCount = digitalCreditInfo.getPaymentRetryCount() - 1;
            digitalCreditInfo.setPaymentRetryCount(retryCount);
            if (digitalCreditInfo.getPaymentRetryCount() <= TheiaConstant.DigitalCreditConfiguration.PASS_CODE_INVALID_ATTEMPTS) {
                digitalCreditInfo.setDigitalCreditInactive(true);
            }
        }

    }

    private void checkRetryCountForPaymentsBank(HttpServletRequest request, CashierRequest cashierRequest,
            PaytmValidationException e) {
        String txnMode = request.getParameter(PaymentRequestParam.TXN_MODE.getValue());
        String bankCode = request.getParameter(PaymentRequestParam.BANK_CODE.getValue());

        if ((PayMethod.NET_BANKING.getOldName().equals(txnMode) && PPB_BANK_CODE.equals(bankCode) || PPB_BANK_CODE
                .equals(txnMode))) {
            decrementAvailableRetriesForPaymentsBank(request, cashierRequest);
        }
    }

    private void decrementAvailableRetriesForPaymentsBank(HttpServletRequest request, CashierRequest cashierRequest) {
        SavingsAccountInfo savingsAccountInfo = theiaSessionDataService
                .getSavingsAccountInfoFromSession(request, false);
        int retryCount = savingsAccountInfo.getPaymentRetryCount() - 1;
        savingsAccountInfo.setPaymentRetryCount(retryCount);
        if (savingsAccountInfo.getPaymentRetryCount() <= TheiaConstant.PaymentsBankConfiguration.PASS_CODE_INVALID_ATTEMPTS) {
            savingsAccountInfo.setSavingsAccountInactive(true);
            redirectToResponsePageForReseller(request, cashierRequest);
        }
    }

    private void ProcessUPIPushResponse(GenericCoreResponseBean<DoPaymentResponse> cashierResponse,
            CashierRequest cashierRequest, final HttpServletRequest request) {
        if ((cashierResponse.getResponse().getPaymentStatus() != null)
                && PaymentStatus.FAIL.name().equalsIgnoreCase(
                        cashierResponse.getResponse().getPaymentStatus().getPaymentStatusValue())) {

            final EnvInfoRequestBean envInfo = EnvInfoUtil.fetchEnvInfo(request);
            if (cashierResponse.getResponse().getFundOrderStatus() != null) {
                BizCancelFundOrderRequest cancelFundOrderRequest = new BizCancelFundOrderRequest(cashierResponse
                        .getResponse().getFundOrderStatus().getFundOrderId(), envInfo);
                orderServiceImpl.closeFundOrder(cancelFundOrderRequest);
            } else if (cashierResponse.getResponse().getTransactionStatus() != null) {
                BizCancelOrderRequest cancelAcquiringOrderRequest = new BizCancelOrderRequest(cashierRequest
                        .getCashierMerchant().getInternalMerchantId(), cashierRequest.getAcquirementId(),
                        "Max payment retry count limit reached");
                orderServiceImpl.closeOrder(cancelAcquiringOrderRequest);
            }
        }
        String responsePage = theiaResponseGenerator.getMerchantResponse(cashierResponse);

        if (StringUtils.isBlank(responsePage)) {
            throw new TheiaControllerException("Could not get response page");
        }

        theiaSessionDataService.setRedirectPageInSession(request, responsePage);
    }

    private void redirectToResponsePageForReseller(HttpServletRequest request, CashierRequest cashierRequest) {
        EntityPaymentOptionsTO entityPaymentOption = theiaSessionDataService.getEntityPaymentOptions(request, false);
        if (null != entityPaymentOption && !entityPaymentOption.isReseller()) {
            return;
        }
        BizCancelOrderRequest cancelAcquiringOrderRequest = new BizCancelOrderRequest(cashierRequest
                .getCashierMerchant().getInternalMerchantId(), cashierRequest.getAcquirementId(),
                "Max payment retry count limit reached");
        GenericCoreResponseBean<BizCancelOrderResponse> closeOrderResponse = orderServiceImpl
                .closeOrder(cancelAcquiringOrderRequest);
        if (!closeOrderResponse.isSuccessfullyProcessed()) {
            LOGGER.error("close order failed reason :{}", closeOrderResponse.getFailureMessage());
        }

        PaymentWorkflow paymentWorkFlow = new PaymentWorkflow();
        try {
            DoPaymentResponse doPaymentResponse = paymentWorkFlow.doPayment(cashierRequest);
            TransactionResponse transactionResponse = theiaResponseGenerator.getMerchantResponse(doPaymentResponse);
            String responsePage = theiaResponseGenerator.getFinalHtmlResponse(transactionResponse);

            if (StringUtils.isBlank(responsePage)) {
                throw new TheiaControllerException("Could not get response page");
            }

            theiaSessionDataService.setRedirectPageInSession(request, responsePage);
        } catch (PaytmValidationException | CashierCheckedException e) {
            LOGGER.error("Exception occured while redirecting to final page for reseller flow :{}", e);
        }
    }

    private void checkPaymentCountBreached(HttpServletRequest request) throws PaytmValidationException {
        try {
            TransactionInfo txnInfo = theiaSessionDataService.getTxnInfoFromSession(request);
            if (txnInfo != null && isRequestTypeForMaxPaymentCheck(txnInfo.getRequestType())
                    && isMaxPaymentRetryBreached(txnInfo.getTxnId())) {

                throw new PaytmValidationException(PaytmValidationExceptionType.TRANSACTION_CLOSED,
                        ResponseConstants.NATIVE_RETRY_COUNT_BREACHED.getMessage());
            }
        } catch (PaytmValidationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.warn("Something went wrong while fetching transaction Info from session:", e);
        }

    }

    private boolean isMaxPaymentRetryBreached(String transId) {
        try {
            int maxPaymentCount = Integer.valueOf(ConfigurationUtil.getProperty(MAX_PAYMENT_COUNT, "10"));
            if (StringUtils.isNotBlank(transId)) {
                PaymentRequestBean requestBean = retryServiceHelper.getRequestDataFromCache(transId);
                int totalPaymentCount = requestBean.getCurrentPaymentCount();
                totalPaymentCount = totalPaymentCount + 1;
                if (totalPaymentCount > maxPaymentCount) {
                    return true;
                }
                requestBean.setCurrentPaymentCount(totalPaymentCount);
                retryServiceHelper.setRequestDataInCache(transId, requestBean);
            }
        } catch (Exception e) {
            LOGGER.info("Exception occured while checking for max payment");
        }
        return false;
    }

    private boolean isRequestTypeForMaxPaymentCheck(String requestType) {
        if (TheiaConstant.RequestTypes.DEFAULT.equals(requestType)) {
            return true;
        }
        return false;
    }
}
