package com.paytm.pgplus.theia.services.helper;

import com.google.gson.Gson;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.bankForm.model.BankForm;
import com.paytm.pgplus.common.bankForm.model.DirectAPIRequest;
import com.paytm.pgplus.common.bankForm.model.FormDetail;
import com.paytm.pgplus.common.util.AllowedMidCustidPropertyUtil;
import com.paytm.pgplus.facade.enums.NativeDirectBankPageRequestType;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.httpclient.HttpRequestPayload;
import com.paytm.pgplus.httpclient.enums.HttpMethod;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.models.ModifiableHttpServletRequest;
import com.paytm.pgplus.theia.models.NativeJsonResponse;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.model.directpage.*;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.utils.NativeRetryUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.services.ISeamlessDirectBankCardService;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.biz.utils.BizConstant.MP_ADD_MONEY_MID;
import static com.paytm.pgplus.facade.enums.NativeDirectBankPageRequestType.resend;
import static com.paytm.pgplus.facade.enums.NativeDirectBankPageRequestType.submit;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.MerchantPreference.PreferenceKeys.NATIVE_OTP_SUPPORTED_PROPERTY;

@Service("nativeDirectBankPageHelper")
public class NativeDirectBankPageHelper {

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("seamlessDirectBankCardServiceImpl")
    private ISeamlessDirectBankCardService seamlessDirectBankCardService;

    @Autowired
    NativeSessionUtil nativeSessionUtil;

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    private NativeRetryUtil nativeRetryUtil;

    @Autowired
    private FF4JHelper fF4JHelper;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeDirectBankPageHelper.class);

    private static final Gson gsonInstance = new Gson();

    public static Gson getGsonInstance() {
        return gsonInstance;
    }

    public void validateDirectBankPageRequest(NativeDirectBankPageRequest request) throws Exception {
        if (request == null || request.getHead() == null || request.getBody() == null) {
            throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                    .isRetryAllowed(false).isNativeJsonRequest(true).build();
        }

        String requestType = request.getBody().getRequestType();
        NativeDirectBankPageRequestType requestTypeEnum = NativeDirectBankPageRequestType.getType(requestType);
        if (StringUtils.isBlank(requestType) || requestTypeEnum == null) {
            throw new NativeFlowException.ExceptionBuilder(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION)
                    .isRetryAllowed(false).isNativeJsonRequest(true).build();
        }
    }

    public InitiateTransactionRequestBody getOrderDetail(NativeDirectBankPageRequest request) {
        InitiateTransactionRequestBody orderDetail = null;

        String mid = (String) request.getHttpServletRequest().getAttribute("MID");
        String orderId = (String) request.getHttpServletRequest().getAttribute("ORDER_ID");

        // creating order detail for mid+sso flow
        if (request.getHead().getTxnToken() != null
                && StringUtils.equals(mid + orderId, request.getHead().getTxnToken())) {
            orderDetail = new InitiateTransactionRequestBody();
            orderDetail.setOrderId(orderId);
            orderDetail.setMid(mid);
        } else {
            orderDetail = nativeSessionUtil.getOrderDetail(request.getHead().getTxnToken());
        }

        return orderDetail;
    }

    public NativeDirectBankPageRequest createNativeDirectBankPageRequest(Map<String, String> content) {
        NativeDirectBankPageRequest nativeDirectBankPageRequest = new NativeDirectBankPageRequest();
        nativeDirectBankPageRequest.setHead(new TokenRequestHeader());
        nativeDirectBankPageRequest.setBody(new NativeDirectBankPageRequestBody());
        nativeDirectBankPageRequest.getHead().setWorkFlow(content.get(TheiaConstant.EnhancedCashierFlow.WORKFLOW));
        nativeDirectBankPageRequest.getHead().setTxnToken(content.get(TheiaConstant.RequestParams.Native.TXN_TOKEN));
        nativeDirectBankPageRequest.getBody().setOtp(content.get("otp"));
        nativeDirectBankPageRequest.getBody().setRequestType(content.get("requestType"));
        nativeDirectBankPageRequest.getBody().setApiRequestOrigin(content.get("apiRequestOrigin"));
        nativeDirectBankPageRequest.getBody().setIsForceResendOtp(content.get("isForceResendOtp"));
        nativeDirectBankPageRequest.getBody().setAcquirementId(content.get("acquirementId"));
        nativeDirectBankPageRequest.getBody().setMaxOtpRetryCount(content.get("maxOtpRetryCount"));
        nativeDirectBankPageRequest.getBody().setMaxOtpResendCount(content.get("maxOtpResendCount"));
        return nativeDirectBankPageRequest;
    }

    @SuppressWarnings("unchecked")
    public NativeJsonResponse doSubmitRequest(NativeDirectBankPageRequest request) throws Exception {
        LOGGER.info("Request received for SUBMIT from directPage");
        NativeJsonResponse response = null;
        IRequestProcessor<NativeDirectBankPageRequest, NativeJsonResponse> requestProcessor = requestProcessorFactory
                .getRequestProcessor(RequestProcessorFactory.RequestType.NATIVE_DIRECT_BANK_PAGE_SUBMIT);
        response = requestProcessor.process(request);
        return response;
    }

    @SuppressWarnings("unchecked")
    public NativeJsonResponse doCancelRequest(NativeDirectBankPageRequest request) throws Exception {
        LOGGER.info("Request received for CANCEL from directPage");
        NativeJsonResponse response = null;
        IRequestProcessor<NativeDirectBankPageRequest, NativeJsonResponse> requestProcessor = requestProcessorFactory
                .getRequestProcessor(RequestProcessorFactory.RequestType.NATIVE_DIRECT_BANK_PAGE_CANCEL);
        response = requestProcessor.process(request);
        return response;
    }

    @SuppressWarnings("unchecked")
    public NativeJsonResponse doResentOtpRequest(NativeDirectBankPageRequest request) throws Exception {
        LOGGER.info("Request received for RESEND-OTP from directPage");
        NativeJsonResponse response = null;
        IRequestProcessor<NativeDirectBankPageRequest, NativeJsonResponse> requestProcessor = requestProcessorFactory
                .getRequestProcessor(RequestProcessorFactory.RequestType.NATIVE_DIRECT_BANK_PAGE_RESEND_OTP);
        response = requestProcessor.process(request);
        return response;
    }

    public HttpRequestPayload<String> createResendOtpRequest(FormDetail formDetail, NativeDirectBankPageRequest request)
            throws Exception {
        HttpRequestPayload<String> requestPayload = new HttpRequestPayload<>();
        requestPayload.setHttpMethod(HttpMethod.POST);
        requestPayload.setMediaTypeProduced(MediaType.APPLICATION_JSON);
        requestPayload.setMediaTypeConsumed(MediaType.APPLICATION_JSON);

        Map<String, String> content = getContent(formDetail, request);

        if (isApiRequestOriginPG(request)) {
            content.put("isForceResendOtp", String.valueOf(request.getBody().isForceResendOtp()));
        }

        String target = getTargetToHit(formDetail, request);
        requestPayload.setTarget(target);

        DirectAPIRequest directAPIRequest = createDirectAPIRequest(content);
        requestPayload.setEntity(JsonMapper.mapObjectToJson(directAPIRequest));

        return requestPayload;
    }

    public HttpRequestPayload<String> createSubmitRequest(FormDetail formDetail, NativeDirectBankPageRequest request,
            NativeDirectBankPageServiceRequest serviceRequest) throws Exception {
        HttpRequestPayload<String> requestPayload = new HttpRequestPayload<>();
        requestPayload.setHttpMethod(HttpMethod.POST);
        requestPayload.setMediaTypeProduced(MediaType.APPLICATION_JSON);
        requestPayload.setMediaTypeConsumed(MediaType.APPLICATION_JSON);

        Map<String, String> content = getContent(formDetail, request);

        String target = getTargetToHit(formDetail, request);
        LOGGER.info("Hitting InstaProxy on URL " + target);
        requestPayload.setTarget(target);

        DirectAPIRequest directAPIRequest = createDirectAPIRequest(content);
        directAPIRequest.getBody().put("otp", request.getBody().getOtp());
        directAPIRequest.getBody().put(CURRENT_COUNT,
                String.valueOf(serviceRequest.getCurrentDirectBankPageSubmitRetryCount()));
        directAPIRequest.getBody().put(MAX_COUNT,
                String.valueOf(serviceRequest.getTotalAllowedDirectBankPageSubmitRetryCount()));
        requestPayload.setEntity(JsonMapper.mapObjectToJson(directAPIRequest));

        return requestPayload;
    }

    public HttpRequestPayload<String> createCancelRequest(FormDetail formDetail) throws Exception {
        HttpRequestPayload<String> requestPayload = new HttpRequestPayload<>();
        requestPayload.setHttpMethod(HttpMethod.POST);
        requestPayload.setMediaTypeProduced(MediaType.APPLICATION_JSON);
        requestPayload.setMediaTypeConsumed(MediaType.APPLICATION_JSON);
        requestPayload.setTarget(formDetail.getActionUrl());

        Map<String, String> content = formDetail.getContent();
        DirectAPIRequest directAPIRequest = createDirectAPIRequest(content);
        requestPayload.setEntity(JsonMapper.mapObjectToJson(directAPIRequest));

        return requestPayload;
    }

    private DirectAPIRequest createDirectAPIRequest(Map<String, String> content) {
        DirectAPIRequest directAPIRequest = new DirectAPIRequest();
        directAPIRequest.setBody(content);
        return directAPIRequest;
    }

    public void validateRequest(NativeDirectBankPageRequest request) {
        if (request == null || request.getHead() == null || request.getBody() == null) {
            LOGGER.error("Request Head/Body is null or empty");
            throwException(ResultCode.FAILED, null);
        }

        if (StringUtils.isBlank(request.getHead().getTxnToken())) {
            LOGGER.error("txnToken is null or empty");
            throwException(ResultCode.FAILED, null);
        }

        validateApiRequestOriginPG(request);
    }

    private void validateApiRequestOriginPG(NativeDirectBankPageRequest request) {
        if (isApiRequestOriginPG(request)) {
            if (StringUtils.isBlank(request.getBody().getAcquirementId())) {
                LOGGER.error("isApiRequestOriginPG, acquirementId is null/empty");
                throwException(ResultCode.FAILED, null);
            }
        }
    }

    private void throwException(ResultCode resultCode, InitiateTransactionRequestBody orderDetail) {
        throw new NativeFlowException.ExceptionBuilder(resultCode).isHTMLResponse(false).isNativeJsonRequest(true)
                .setOrderDetail(orderDetail).build();
    }

    public FormDetail getContentForInstaProxyDirectApis(NativeDirectBankPageServiceRequest serviceRequest,
            NativeDirectBankPageRequestType requestType) {

        NativeDirectBankPageCacheData cachedBankForm = serviceRequest.getCachedBankFormData();
        BankForm bankForm = cachedBankForm.getBankForm();

        if (bankForm != null) {
            for (FormDetail formDetail : bankForm.getDirectForms()) {
                if (StringUtils.equals(requestType.getType(), formDetail.getType())) {
                    return formDetail;
                }
            }
        }
        return null;
    }

    public void processForMerchantOwnedDirectBankPage(WorkFlowResponseBean responseBean,
            WorkFlowRequestBean flowRequestBean, BankForm bankForm) {
        if (bankForm == null || !StringUtils.equals("direct", bankForm.getPageType())) {
            return;
        }

        if (bankForm.getDirectForms() == null) {
            LOGGER.error("DirectForms in bankForm is null, making pageType=redirect");
            bankForm.setPageType("redirect");
            return;
        }

        LOGGER.info("Received direct bank page");
        boolean isOfflineFlowAndFf4jEnabledForDirectBankPageEnabled = false;
        /*
         * PGP-22934 Remove merchant level preference for returning direct OTP
         * page for offline flow
         */
        if (fF4JHelper.isFF4JFeatureForMidEnabled(
                TheiaConstant.ExtraConstants.DIRECT_BANK_FORM_FOR_MERCHANT_OFFLINE_FLOW, flowRequestBean.getPaytmMID())
                && (flowRequestBean.isOfflineFlow() || flowRequestBean.isOfflineTxnFlow())) {
            isOfflineFlowAndFf4jEnabledForDirectBankPageEnabled = true;
        }

        /*
         * First check if "nativeOtpSupported" preference is disabled on
         * merchant, if disabled, remove directBankPage array
         */
        boolean isNativeOtpSupportedOnMerchant = merchantPreferenceService.isNativeOtpSupported(flowRequestBean
                .getPaytmMID());

        if (!isOfflineFlowAndFf4jEnabledForDirectBankPageEnabled
                && (!isNativeOtpSupportAllowedProperty(flowRequestBean) || !isNativeOtpSupportedOnMerchant)) {
            bankForm.setDirectForms(null);
            LOGGER.info("Merchant has nativeOtpSupported=false, setting directForms=null, making pageType=redirect");
            bankForm.setPageType("redirect");
            return;
        }

        flowRequestBean.setInstaDirectForm(true);

        NativeDirectBankPageCacheData toBeCachedData = getDirectBankPageRenderDataForCache(flowRequestBean,
                responseBean, bankForm);

        setDirectBankPageRenderDataInCache(flowRequestBean.getTxnToken(), toBeCachedData);
        setTheiaUrlsForMerchantOwnedDirectBankPage(flowRequestBean, responseBean, bankForm);
    }

    public void processForMerchantOwnedDirectBankPageForAOA(WorkFlowResponseBean responseBean,
            WorkFlowRequestBean flowRequestBean, BankForm bankForm) {
        if (bankForm == null || !StringUtils.equals("direct", bankForm.getPageType())) {
            return;
        }

        if (bankForm.getDirectForms() == null) {
            LOGGER.error("DirectForms in bankForm is null, making pageType=redirect");
            bankForm.setPageType("redirect");
            return;
        }

        LOGGER.info("Received direct bank page for AOA flow");

        /*
         * First check if "nativeOtpSupported" preference is disabled on
         * merchant, if disabled, remove directBankPage array
         */
        boolean isNativeOtpSupportedOnMerchant = merchantPreferenceService.isNativeOtpSupported(flowRequestBean
                .getPaytmMID());

        if (!isNativeOtpSupportAllowedProperty(flowRequestBean) || !isNativeOtpSupportedOnMerchant) {
            bankForm.setDirectForms(null);
            LOGGER.info("Merchant has nativeOtpSupported=false, setting directForms=null, making pageType=redirect");
            bankForm.setPageType("redirect");
            return;
        }

        flowRequestBean.setInstaDirectForm(true);

        NativeDirectBankPageCacheData toBeCachedData = getDirectBankPageRenderDataForCache(flowRequestBean,
                responseBean, bankForm);
        setDirectBankPageRenderDataForAOAInCache(flowRequestBean.getPaytmMID() + flowRequestBean.getOrderID(),
                toBeCachedData);
        setTheiaUrlsForMerchantOwnedDirectBankPage(flowRequestBean, responseBean, bankForm);
    }

    public void changeContractFromInstaToTheiaForEnhance(WorkFlowResponseBean workFlowResponseBean,
            WorkFlowRequestBean workFlowRequestBean, BankForm bankForm) {
        if (bankForm == null || !StringUtils.equals("direct", bankForm.getPageType())) {
            return;
        }

        if (bankForm.getDirectForms() == null) {
            LOGGER.error("DirectForms in bankForm is null, making pageType=redirect");
            bankForm.setPageType("redirect");
            return;
        }

        workFlowRequestBean.setInstaDirectForm(true);

        NativeDirectBankPageCacheData toBeCachedData = getDirectBankPageRenderDataForCache(workFlowRequestBean,
                workFlowResponseBean, bankForm);

        setDirectBankPageRenderDataInCache(workFlowRequestBean.getTxnToken(), toBeCachedData);

        changeContract(workFlowRequestBean, workFlowResponseBean, bankForm);

    }

    private void changeContract(WorkFlowRequestBean workFlowRequestBean, WorkFlowResponseBean workFlowResponseBean,
            BankForm bankForm) {
        if (bankForm.getDirectForms() == null) {
            LOGGER.error("bankForm has DirectForms as null");
            return;
        }
        for (FormDetail formDetail : bankForm.getDirectForms()) {
            if (formDetail != null) {
                NativeDirectBankPageRequestType enumType = NativeDirectBankPageRequestType
                        .getType(formDetail.getType());
                if (enumType != null) {
                    switch (enumType) {
                    case submit:
                    case resend:
                        setNativeDirectBankPageTheiaUrlsUtil(workFlowRequestBean, formDetail, enumType, true);
                        break;
                    }
                }
            }
        }
    }

    private void setDataForCashierResponse(WorkFlowRequestBean flowRequestBean, WorkFlowResponseBean responseBean,
            NativeDirectBankPageCacheData nativeDirectBankPageCacheData) {

        nativeDirectBankPageCacheData.setCashierRequestId(responseBean.getCashierRequestId());
        nativeDirectBankPageCacheData.setTransId(responseBean.getTransID());
        nativeDirectBankPageCacheData.setMerchantId(flowRequestBean.getPaytmMID());
    }

    private void setTheiaUrlsForMerchantOwnedDirectBankPage(WorkFlowRequestBean flowRequestBean,
            WorkFlowResponseBean responseBean, BankForm bankForm) {
        /*
         * direct bankForms from instaProxy-Theia contract is now transformed to
         * Theia->Merchant contract
         */
        if (bankForm.getDirectForms() == null) {
            LOGGER.error("bankForm has DirectForms as null");
            return;
        }
        for (FormDetail formDetail : bankForm.getDirectForms()) {
            if (formDetail != null) {
                NativeDirectBankPageRequestType enumType = NativeDirectBankPageRequestType
                        .getType(formDetail.getType());
                if (enumType != null) {
                    switch (enumType) {
                    case cancel:
                    case submit:
                    case resend:
                        setNativeDirectBankPageTheiaUrlsUtil(flowRequestBean, formDetail, enumType, false);
                        break;
                    }
                }
            }
        }
    }

    private void setNativeDirectBankPageTheiaUrlsUtil(WorkFlowRequestBean flowRequestBean, FormDetail formDetail,
            NativeDirectBankPageRequestType bankPageRequestType, boolean isEnhanced) {
        formDetail.setHeaders(new HashMap<>());
        formDetail.setEncType(null);
        formDetail.setContent(new HashMap<>());
        // Setting content to be sent to merchant. Merchant will later send this
        // data to theia in directBankPage APIs

        if (StringUtils.isBlank(flowRequestBean.getTxnToken())) {
            formDetail.getContent().put(TheiaConstant.RequestParams.Native.TXN_TOKEN,
                    flowRequestBean.getPaytmMID() + flowRequestBean.getOrderID());
        } else {
            formDetail.getContent().put(TheiaConstant.RequestParams.Native.TXN_TOKEN, flowRequestBean.getTxnToken());
        }
        formDetail.getContent().put("requestType", bankPageRequestType.getType());
        if (isEnhanced) {
            formDetail.getContent().put(TheiaConstant.EnhancedCashierFlow.WORKFLOW,
                    TheiaConstant.EnhancedCashierFlow.ENHANCED_CASHIER_FLOW);
        }
        if (submit == bankPageRequestType) {
            formDetail.getContent().put("otp", "<OTP>");
        }

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("?mid=").append(flowRequestBean.getPaytmMID());
        queryBuilder.append("&orderId=").append(flowRequestBean.getOrderID());

        if (StringUtils.isNotBlank(flowRequestBean.getFromAOARequest())
                && flowRequestBean.getFromAOARequest().equalsIgnoreCase("true")) {
            formDetail.setActionUrl(bankPageRequestType.getAoaInternalLbUrl() + queryBuilder);
        } else {
            formDetail.setActionUrl(bankPageRequestType.getUrl() + queryBuilder);
        }

        formDetail.getHeaders().put("Content-Type", "application/json;charset=UTF-8");

        formDetail.setMethod("POST");
    }

    private NativeDirectBankPageCacheData getDirectBankPageRenderDataForCache(WorkFlowRequestBean flowRequestBean,
            WorkFlowResponseBean responseBean, BankForm bankForm) {
        NativeDirectBankPageCacheData nativeDirectBankPageCacheData = new NativeDirectBankPageCacheData();

        setDataForCashierResponse(flowRequestBean, responseBean, nativeDirectBankPageCacheData);

        nativeDirectBankPageCacheData.setBankForm(bankForm);

        return nativeDirectBankPageCacheData;
    }

    private void setDirectBankPageRenderDataInCache(String txnToken, NativeDirectBankPageCacheData toBeCachedData) {
        nativeSessionUtil.setDirectBankPageRenderData(txnToken, toBeCachedData);
    }

    private void setDirectBankPageRenderDataForAOAInCache(String txnToken, NativeDirectBankPageCacheData toBeCachedData) {
        nativeSessionUtil.setDirectBankPageRenderDataAOA(txnToken, toBeCachedData);
    }

    private boolean isNativeOtpSupportAllowedProperty(WorkFlowRequestBean flowRequestBean) {
        boolean isPropertyAllowed = AllowedMidCustidPropertyUtil.isMidCustIdEligible(flowRequestBean.getPaytmMID(),
                flowRequestBean.getCustID(), NATIVE_OTP_SUPPORTED_PROPERTY, TheiaConstant.ExtraConstants.NONE,
                Boolean.FALSE);
        if (!isPropertyAllowed) {
            LOGGER.info("Blocked by {} property", NATIVE_OTP_SUPPORTED_PROPERTY);
        }
        return isPropertyAllowed;
    }

    public void setParamsForCashierResponseInHttpRequest(NativeDirectBankPageRequest request,
            NativeDirectBankPageServiceRequest serviceRequest, NativeDirectBankPageServiceResponse serviceResponse) {
        Map<String, String[]> paramMap = new HashMap<>();

        if (StringUtils.equals(submit.getType(), request.getBody().getRequestType())) {
            if (canRetryForDirectBankSubmit(serviceRequest)) {
                paramMap.put(DIRECT_BANK_PAGE_RETRY, new String[] { Boolean.TRUE.toString() });
            }
            paramMap.put(DIRECT_BANK_PAGE_SUBMIT_REQUEST, new String[] { Boolean.TRUE.toString() });
        }

        if (isApiRequestOriginPG(request)) {
            /*
             * Here, we get these fields from instaProxy in response to its api
             * hit
             */
            String txnToken = request.getHead().getTxnToken();

            paramMap.put(CASHIER_REQUEST_ID, new String[] { nativeSessionUtil.getCashierRequestId(txnToken) });
            paramMap.put(TRANS_ID, new String[] { request.getBody().getAcquirementId() });
            paramMap.put(MERCHANT_ID, new String[] { serviceRequest.getOrderDetail().getMid() });
            paramMap.put(PAYMENT_MODE, new String[] { nativeSessionUtil.getPaymentTypeId(txnToken) });

        } else {
            /*
             * Here, we fetch these fields form cache, nativePlus case!
             */
            paramMap.put(CASHIER_REQUEST_ID, new String[] { serviceRequest.getCachedBankFormData()
                    .getCashierRequestId() });
            paramMap.put(TRANS_ID, new String[] { serviceRequest.getCachedBankFormData().getTransId() });
            paramMap.put(MERCHANT_ID, new String[] { serviceRequest.getCachedBankFormData().getMerchantId() });
            paramMap.put(PAYMENT_MODE, new String[] { serviceResponse.getFormDetail().getContent().get(PAYMENT_MODE) });
        }

        ModifiableHttpServletRequest modifiableHttpServletRequest = new ModifiableHttpServletRequest(
                request.getHttpServletRequest(), paramMap);

        request.setHttpServletRequest(modifiableHttpServletRequest);
    }

    public NativeDirectBankPageCacheData getCachedBankFormData(NativeDirectBankPageRequest request) {
        /*
         * This is case when directForms have been given by insta for merchant
         * to make its own page, we fetch it from cache
         */
        if (!isApiRequestOriginPG(request)) {
            return nativeSessionUtil.getDirectBankPageRenderData(request.getHead().getTxnToken());
        }

        String cashierRequestId = nativeSessionUtil.getCashierRequestId(request.getHead().getTxnToken());
        if (StringUtils.isBlank(cashierRequestId)) {
            LOGGER.error("cashierRequestId is null/empty from cache for isApiRequestOriginPG");
            throwException();
        }

        NativeDirectBankPageCacheData nativeDirectBankPageCacheData = new NativeDirectBankPageCacheData();
        nativeDirectBankPageCacheData.setCashierRequestId(cashierRequestId);

        return nativeDirectBankPageCacheData;
    }

    public void setCashierRequestIdInCache(String txnToken, String cashierRequestId) {
        nativeSessionUtil.setCashierRequestId(txnToken, cashierRequestId);
    }

    public boolean isApiRequestOriginPG(NativeDirectBankPageRequest request) {
        return StringUtils.equals(PG, request.getBody().getApiRequestOrigin());
    }

    private String getTargetToHit(FormDetail formDetail, NativeDirectBankPageRequest request) {
        if (isApiRequestOriginPG(request)) {
            StringBuilder sb = new StringBuilder();
            sb.append("native.directBankPage.instaProxy.").append(request.getBody().getRequestType()).append(".url");
            String target = ConfigurationUtil.getProperty(sb.toString());
            if (StringUtils.isBlank(target)) {
                throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isNativeJsonRequest(true)
                        .isRetryAllowed(false).build();
            }
            return target;
        }
        return formDetail.getActionUrl();
    }

    private Map<String, String> getContent(FormDetail formDetail, NativeDirectBankPageRequest request) {
        Map<String, String> content = new HashMap<>();
        if (isApiRequestOriginPG(request)) {
            content.put(TRANS_ID, request.getBody().getAcquirementId());
            return content;
        }
        return formDetail.getContent();
    }

    public boolean checkIfInvalidOtpCase(Map<String, String> data) {
        if (StringUtils.equals(Boolean.TRUE.toString(), data.get(DIRECT_BANK_PAGE_INVALID_OTP))) {
            return true;
        }
        return false;
    }

    private int getDirectBankPageSubmitRetryCount(String txnToken) {
        return nativeSessionUtil.getDirectBankPageSubmitRetryCount(txnToken);
    }

    public void processNewPaymentRequest(NativeDirectBankPageRequest request,
            NativeDirectBankPageServiceRequest serviceRequest) {

        LOGGER.info("Processing a new payment request for retry on nativeDirectBankPage!");

        String cashierRequestId = serviceRequest.getCachedBankFormData().getCashierRequestId();

        WorkFlowRequestBean flowRequestBean = (WorkFlowRequestBean) theiaSessionRedisUtil
                .get(com.paytm.pgplus.biz.utils.ConfigurationUtil.getProperty("directBankRequestBeanKey")
                        + cashierRequestId);

        GenericCoreResponseBean<WorkFlowResponseBean> workFlowResponseBean = null;

        try {

            flowRequestBean.setForceDirectChannel("forceDirect");

            workFlowResponseBean = seamlessDirectBankCardService.doPayment(flowRequestBean);

            boolean isValidResponse = validateWorkFlowResponseBean(workFlowResponseBean);
            if (isValidResponse) {
                serviceRequest.setWorkFlowResponseBean(workFlowResponseBean);
            }

            updateCachedData(request, serviceRequest);

        } catch (Exception e) {
            LOGGER.error("Exception while processing new paymentRequest for DirectBankPage");
            workFlowResponseBean = null;
            serviceRequest.setWorkFlowResponseBean(workFlowResponseBean);
            if (serviceRequest.getOrderDetail() != null) {
                nativeRetryUtil.increaseRetryCount(request.getHead().getTxnToken(), serviceRequest.getOrderDetail()
                        .getMid(), serviceRequest.getOrderDetail().getOrderId());
            }
        }
    }

    private void updateCachedData(NativeDirectBankPageRequest request, NativeDirectBankPageServiceRequest serviceRequest) {

        WorkFlowResponseBean workFlowResponseBean = serviceRequest.getWorkFlowResponseBean().getResponse();

        if (isApiRequestOriginPG(request)) {
            LOGGER.info("isApiRequestOriginPG=true, updated CashierRequestId!");
            nativeSessionUtil.setCashierRequestId(request.getHead().getTxnToken(),
                    workFlowResponseBean.getCashierRequestId());
            return;
        }

        /*
         * We need to update directForms because the new payment initiated has
         * got new data
         */

        String bankFormJson = workFlowResponseBean.getQueryPaymentStatus().getWebFormContext();
        BankForm bankForm = getGsonInstance().fromJson(bankFormJson, BankForm.class);

        NativeDirectBankPageCacheData cachedData = serviceRequest.getCachedBankFormData();
        cachedData.setBankForm(bankForm);
        cachedData.setCashierRequestId(workFlowResponseBean.getCashierRequestId());

        setDirectBankPageRenderDataInCache(request.getHead().getTxnToken(), cachedData);
        LOGGER.info("Updated directBankPageRenderDataInCache!");
    }

    private boolean validateWorkFlowResponseBean(GenericCoreResponseBean<WorkFlowResponseBean> flowResponseBean) {
        /*
         * Here, we're failing payment in case of any error
         */

        if (StringUtils.isNotBlank(flowResponseBean.getInternalErrorCode())) {
            return false;
        }

        if (!flowResponseBean.isSuccessfullyProcessed()) {
            return false;
        }

        if (flowResponseBean.getResponse() == null) {
            return false;
        }

        if (flowResponseBean.getResponse().getQueryPaymentStatus() == null) {
            return false;
        }

        String bankForm = flowResponseBean.getResponse().getQueryPaymentStatus().getWebFormContext();
        if (StringUtils.isBlank(bankForm)) {
            return false;
        }

        return true;
    }

    private void throwException() {
        throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isHTMLResponse(false)
                .isNativeJsonRequest(true).isRetryAllowed(false).build();
    }

    public void incrementDirectBankPageSubmitRetryCount(String txnToken, int count) {
        nativeSessionUtil.setDirectBankPageSubmitRetryCount(txnToken, count + 1);
    }

    public void validateDirectBankPageSubmitRetryCount(String txnToken,
            NativeDirectBankPageServiceRequest serviceRequest, NativeDirectBankPageRequest request) {

        int currentDirectBankPageSubmitRetryCount = getDirectBankPageSubmitRetryCount(txnToken);
        // LOGGER.info("current retry count is" +
        // currentDirectBankPageSubmitRetryCount);
        // Fetching total count from insta
        int totalAllowedDirectBankPageSubmitRetryCount = getMaxRetryAllowedForSubmit(serviceRequest,
                NativeDirectBankPageRequestType.submit, request);
        LOGGER.info("current retry count is {} and total retry count is {}", currentDirectBankPageSubmitRetryCount,
                totalAllowedDirectBankPageSubmitRetryCount);
        // LOGGER.info("Total retry count is" +
        // totalAllowedDirectBankPageSubmitRetryCount);

        if (currentDirectBankPageSubmitRetryCount > totalAllowedDirectBankPageSubmitRetryCount) {
            LOGGER.error("currentDirectBankPageSubmitRetryCount > totalAllowedDirectBankPageSubmitRetryCount");
            throwException();
        }

        serviceRequest.setCurrentDirectBankPageSubmitRetryCount(currentDirectBankPageSubmitRetryCount);
        serviceRequest.setTotalAllowedDirectBankPageSubmitRetryCount(totalAllowedDirectBankPageSubmitRetryCount);

    }

    private int getMaxRetryAllowedForSubmit(NativeDirectBankPageServiceRequest serviceRequest,
            NativeDirectBankPageRequestType requestType, NativeDirectBankPageRequest request) {

        // for native get max count from request body
        if (request.getBody().getMaxOtpRetryCount() != null) {
            LOGGER.info("fetching retry count from requestBody " + request.getBody().getMaxOtpRetryCount());
            return (Integer.parseInt(request.getBody().getMaxOtpRetryCount()));
        }

        // for enhance and native+ get the max counts from cached bank form
        NativeDirectBankPageCacheData cachedBankFormData = serviceRequest.getCachedBankFormData();
        if (cachedBankFormData != null) {
            BankForm bankForm = cachedBankFormData.getBankForm();
            FormDetail formDetail = getDirectBankFormDetail(bankForm, requestType.getType());

            if (formDetail != null && formDetail.getContent() != null
                    && formDetail.getContent().get(MAX_OTP_RETRY_COUNT) != null) {
                return (Integer.parseInt(formDetail.getContent().get(MAX_OTP_RETRY_COUNT)));
            }
        }
        LOGGER.info("fetching count from default propery");
        // default submit count if count not configured in insta db
        return Integer.parseInt(ConfigurationUtil.getProperty(DIRECT_BANK_SUBMIT_COUNT_PROP, "2"));

    }

    private int getMaxRetryAllowedForResend(NativeDirectBankPageServiceRequest serviceRequest,
            NativeDirectBankPageRequestType requestType, NativeDirectBankPageRequest request) {

        if (request.getBody().getMaxOtpResendCount() != null) {
            return (Integer.parseInt(request.getBody().getMaxOtpResendCount()));
        }

        NativeDirectBankPageCacheData cachedBankFormData = serviceRequest.getCachedBankFormData();
        if (cachedBankFormData != null) {
            BankForm bankForm = cachedBankFormData.getBankForm();
            FormDetail formDetail = getDirectBankFormDetail(bankForm, requestType.getType());
            if (formDetail != null && formDetail.getContent() != null
                    && formDetail.getContent().get(MAX_OTP_RESEND_COUNT) != null) {
                return (Integer.parseInt(formDetail.getContent().get(MAX_OTP_RESEND_COUNT)));
            }
        }
        return Integer.parseInt(ConfigurationUtil.getProperty(DIRECT_BANK_RESEND_COUNT_PROP, "2"));

    }

    private FormDetail getDirectBankFormDetail(BankForm bankForm, String requestType) {
        if (bankForm != null) {
            for (FormDetail formDetail : bankForm.getDirectForms()) {
                if (StringUtils.equals(requestType, formDetail.getType())) {
                    return formDetail;
                }
            }
        }
        return null;
    }

    public void incrementDirectBankPageResendOtpRetryCount(String txnToken, int count) {
        nativeSessionUtil.setDirectBankPageResendOtpRetryCount(txnToken, count + 1);
    }

    private boolean canRetryForDirectBankSubmit(NativeDirectBankPageServiceRequest serviceRequest) {
        int currentCount = serviceRequest.getCurrentDirectBankPageSubmitRetryCount();
        if (currentCount < (serviceRequest.getTotalAllowedDirectBankPageSubmitRetryCount())) {
            return true;
        }
        return false;
    }

    public boolean validateDirectBankPageResendOtpRetryCount(String txnToken,
            NativeDirectBankPageServiceRequest serviceRequest, NativeDirectBankPageRequest request) {

        int currentDirectBankPageResendOtpRetryCount = getDirectBankPageResendOtpRetryCount(txnToken);
        int totalAllowedDirectBankPageResendOtpRetryCount = getMaxRetryAllowedForResend(serviceRequest, resend, request);

        if (totalAllowedDirectBankPageResendOtpRetryCount == 0) {
            LOGGER.info("native.directBankPage.resendOtpRetryCount=0");
        }

        if (totalAllowedDirectBankPageResendOtpRetryCount != 0
                && (currentDirectBankPageResendOtpRetryCount > totalAllowedDirectBankPageResendOtpRetryCount)) {
            LOGGER.error("currentDirectBankPageResendOtpRetryCount > totalAllowedDirectBankPageResendOtpRetryCount");
            return false;
        }

        serviceRequest.setCurrentDirectBankPageResendOtpRetryCount(currentDirectBankPageResendOtpRetryCount);
        serviceRequest.setTotalAllowedDirectBankPageResendRetryCount(totalAllowedDirectBankPageResendOtpRetryCount);
        return true;
    }

    private void makeTxnInfoDataForEnhanceNativeFlow(TransactionResponse transactionResponse,
            NativeDirectBankPageRequest request, Map<String, String> txnInfo) {
        txnInfo.put(TheiaConstant.RequestParams.Native.ORDER_ID, transactionResponse.getOrderId());
        txnInfo.put(TheiaConstant.RequestParams.Native.MID, transactionResponse.getMid());
        txnInfo.put(TheiaConstant.RequestParams.Native.TXN_TOKEN, request.getHead().getTxnToken());
    }

    public String getEnhanceNativeCallBackUrl(String mid, String orderId) {
        String theiaBaseURL = NativeDirectBankPageRequestType.getBaseUrl();

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(theiaBaseURL).append(NATIVE_APP_INVOKE_URL).append("?mid=").append(mid).append("&orderId=")
                .append(orderId);

        return queryBuilder.toString();
    }

    public void makeResponseForEnhancedNativeFlow(NativeDirectBankPageRequest request,
            TransactionResponse transactionResponse, Map<String, String> txnInfo) {
        txnInfo.clear();
        makeTxnInfoDataForEnhanceNativeFlow(transactionResponse, request, txnInfo);
    }

    public boolean isResendOtpAllowed(NativeDirectBankPageServiceRequest serviceRequest) {
        return serviceRequest.getCurrentDirectBankPageResendOtpRetryCount() < serviceRequest
                .getTotalAllowedDirectBankPageResendRetryCount();
    }

    private int getDirectBankPageResendOtpRetryCount(String txnToken) {
        return nativeSessionUtil.getDirectBankPageResendOtpRetryCount(txnToken);
    }

    public boolean isForceResendOtp(NativeDirectBankPageRequest request, FormDetail formDetail) {
        if (request.getBody().isForceResendOtp()) {
            LOGGER.info("isForceResendOtp=true, pg owned page");
            return true;
        } else if (formDetail != null && formDetail.getContent() != null
                && StringUtils.equalsIgnoreCase("true", formDetail.getContent().get("isForceResendOtp"))) {
            LOGGER.info("isForceResendOtp=true, merchant owned page case");
            return true;
        }
        return false;
    }

    public String getResendOtpMsgWithCount(NativeDirectBankPageServiceRequest serviceRequest) {
        int count = serviceRequest.getTotalAllowedDirectBankPageResendRetryCount()
                - serviceRequest.getCurrentDirectBankPageResendOtpRetryCount();
        return " You've " + count + " OTP retries left.";
    }

    public void resetCounts(String txnToken) {
        LOGGER.info("Resetting submit and retry count on native direct bank page");
        nativeSessionUtil.setDirectBankPageSubmitRetryCount(txnToken, 0);
        nativeSessionUtil.setDirectBankPageResendOtpRetryCount(txnToken, 1);
    }

    public String getLastRetryMsg(NativeDirectBankPageServiceRequest serviceRequest) {
        int count = serviceRequest.getTotalAllowedDirectBankPageResendRetryCount()
                - serviceRequest.getCurrentDirectBankPageResendOtpRetryCount();
        if (count == 0) {
            return (". This is your last attempt.");
        }
        return "";
    }

    public boolean addMoneyMerchant() {
        String requestMid = MDC.get(TheiaConstant.RequestParams.MID);
        if (StringUtils.equals(requestMid,
                com.paytm.pgplus.biz.utils.ConfigurationUtil.getTheiaProperty(MP_ADD_MONEY_MID))) {
            return true;
        }
        return false;

    }
}
