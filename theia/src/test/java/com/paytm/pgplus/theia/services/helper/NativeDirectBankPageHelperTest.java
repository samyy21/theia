package com.paytm.pgplus.theia.services.helper;

import com.google.gson.Gson;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.bankForm.model.BankForm;
import com.paytm.pgplus.common.bankForm.model.DirectAPIResponse;
import com.paytm.pgplus.common.bankForm.model.FormDetail;
import com.paytm.pgplus.common.util.AllowedMidCustidPropertyUtil;
import com.paytm.pgplus.facade.enums.NativeDirectBankPageRequestType;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.models.NativeJsonResponse;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.model.directpage.*;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.utils.NativeRetryUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.services.ISeamlessDirectBankCardService;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.junit.Test;
import mockit.MockUp;
import org.mockito.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NativeDirectBankPageHelperTest {

    @InjectMocks
    private NativeDirectBankPageHelper nativeDirectBankPageHelper;

    @Mock
    private IMerchantPreferenceService merchantPreferenceService;

    @Mock
    private ISeamlessDirectBankCardService seamlessDirectBankCardService;

    @Mock
    NativeSessionUtil nativeSessionUtil;

    @Mock
    private RequestProcessorFactory requestProcessorFactory;

    @Mock
    private NativeRetryUtil nativeRetryUtil;

    @Mock
    private FF4JHelper fF4JHelper;

    @Mock
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Mock
    GenericCoreResponseBean<WorkFlowResponseBean> workFlowResponseBean;

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeDirectBankPageHelper.class);
    private static final Gson gsonInstance = new Gson();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testGetGsonInstance() {
        NativeDirectBankPageHelper.getGsonInstance();
    }

    @Test
    public void testValidateDirectBankPageRequestWhenHeadAndBodyNull() throws Exception {
        NativeDirectBankPageRequest request = new NativeDirectBankPageRequest();
        request.setHead(null);
        request.setBody(null);
        exceptionRule.expect(NativeFlowException.class);
        nativeDirectBankPageHelper.validateDirectBankPageRequest(request);
    }

    @Test
    public void testValidateDirectBankPageRequest() throws Exception {
        NativeDirectBankPageRequest request = new NativeDirectBankPageRequest();
        getValidateDirectBankPageRequestSetBody(request);
        getValidateDirectBankPageRequestSetHead(request);
        nativeDirectBankPageHelper.validateDirectBankPageRequest(request);

        NativeDirectBankPageRequest request1 = new NativeDirectBankPageRequest();
        getValidateDirectBankPageRequestSetHead(request1);
        NativeDirectBankPageRequestBody body = new NativeDirectBankPageRequestBody();
        body.setRequestType("type");
        request1.setBody(body);
        exceptionRule.expect(NativeFlowException.class);
        nativeDirectBankPageHelper.validateDirectBankPageRequest(request1);
    }

    @Test
    public void testGetOrderDetail() {
        NativeDirectBankPageRequest request = new NativeDirectBankPageRequest();
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getAttribute("MID")).thenReturn("txn");
        when(httpServletRequest.getAttribute("ORDER_ID")).thenReturn("Token");
        request.setHttpServletRequest(httpServletRequest);
        getValidateDirectBankPageRequestSetHead(request);
        getValidateDirectBankPageRequestSetBody(request);
        nativeDirectBankPageHelper.getOrderDetail(request);

        when(httpServletRequest.getAttribute(any())).thenReturn("attribute");
        nativeDirectBankPageHelper.getOrderDetail(request);
    }

    @Test
    public void testCreateNativeDirectBankPageRequest() {
        Map<String, String> content = new HashMap<>();
        content.put(TheiaConstant.EnhancedCashierFlow.WORKFLOW, "workFlow");
        content.put(TheiaConstant.RequestParams.Native.TXN_TOKEN, "txnToken");
        content.put("otp", "otp");
        content.put("requestType", "requestType");
        content.put("apiRequestOrigin", "apiRequestOrigin");
        content.put("isForceResendOtp", "isForceResendOtp");
        content.put("acquirementId", "acquirementId");
        content.put("maxOtpRetryCount", "maxOtpRetryCount");
        content.put("maxOtpResendCount", "maxOtpResendCount");
        nativeDirectBankPageHelper.createNativeDirectBankPageRequest(content);
    }

    @Test
    public void testDoSubmitRequest() throws Exception {
        NativeDirectBankPageRequest request = new NativeDirectBankPageRequest();
        IRequestProcessor<NativeDirectBankPageRequest, NativeJsonResponse> requestProcessor = mock(IRequestProcessor.class);
        when(requestProcessorFactory.getRequestProcessor(any())).thenReturn(requestProcessor);
        nativeDirectBankPageHelper.doSubmitRequest(request);
    }

    @Test
    public void testDoCancelRequest() throws Exception {
        NativeDirectBankPageRequest request = new NativeDirectBankPageRequest();
        IRequestProcessor<NativeDirectBankPageRequest, NativeJsonResponse> requestProcessor = mock(IRequestProcessor.class);
        when(requestProcessorFactory.getRequestProcessor(any())).thenReturn(requestProcessor);
        nativeDirectBankPageHelper.doCancelRequest(request);
    }

    @Test
    public void testDoResentOtpRequest() throws Exception {
        NativeDirectBankPageRequest request = new NativeDirectBankPageRequest();
        IRequestProcessor<NativeDirectBankPageRequest, NativeJsonResponse> requestProcessor = mock(IRequestProcessor.class);
        when(requestProcessorFactory.getRequestProcessor(any())).thenReturn(requestProcessor);
        nativeDirectBankPageHelper.doResentOtpRequest(request);
    }

    @Test
    public void testCreateResendOtpRequest() throws Exception {
        FormDetail formDetail = new FormDetail();
        NativeDirectBankPageRequest request = new NativeDirectBankPageRequest();
        getValidateDirectBankPageRequestSetBody(request);
        nativeDirectBankPageHelper.createResendOtpRequest(formDetail, request);

        NativeDirectBankPageRequest request1 = new NativeDirectBankPageRequest();
        NativeDirectBankPageRequestBody body1 = new NativeDirectBankPageRequestBody();
        body1.setApiRequestOrigin(PG);
        request1.setBody(body1);
        getValidateDirectBankPageRequestSetHead(request1);
        new MockUp<ConfigurationUtil>() {
            @mockit.Mock
            public String getProperty(String key) {
                return "target";
            }
        };
        nativeDirectBankPageHelper.createResendOtpRequest(formDetail, request1);

        new MockUp<ConfigurationUtil>() {
            @mockit.Mock
            public String getProperty(String key) {
                return null;
            }
        };
        exceptionRule.expect(NativeFlowException.class);
        nativeDirectBankPageHelper.createResendOtpRequest(formDetail, request1);
    }

    @Test
    public void testCreateSubmitRequest() throws Exception {
        NativeDirectBankPageServiceRequest serviceRequest = new NativeDirectBankPageServiceRequest();
        FormDetail formDetail = new FormDetail();
        NativeDirectBankPageRequest request = new NativeDirectBankPageRequest();
        getValidateDirectBankPageRequestSetBody(request);
        Map<String, String> map = new HashMap<>();
        map.put(PG, "value");
        formDetail.setContent(map);
        serviceRequest.setCurrentDirectBankPageResendOtpRetryCount(2);
        serviceRequest.setTotalAllowedDirectBankPageSubmitRetryCount(4);
        nativeDirectBankPageHelper.createSubmitRequest(formDetail, request, serviceRequest);

        NativeDirectBankPageRequest request1 = new NativeDirectBankPageRequest();
        NativeDirectBankPageRequestBody body1 = new NativeDirectBankPageRequestBody();
        body1.setApiRequestOrigin(PG);
        request1.setBody(body1);
        getValidateDirectBankPageRequestSetHead(request1);
        new MockUp<ConfigurationUtil>() {
            @mockit.Mock
            public String getProperty(String key) {
                return "target";
            }
        };
        nativeDirectBankPageHelper.createSubmitRequest(formDetail, request1, serviceRequest);

        new MockUp<ConfigurationUtil>() {
            @mockit.Mock
            public String getProperty(String key) {
                return null;
            }
        };
        exceptionRule.expect(NativeFlowException.class);
        nativeDirectBankPageHelper.createSubmitRequest(formDetail, request1, serviceRequest);
    }

    @Test
    public void testCreateCancelRequest() throws Exception {
        FormDetail formDetail = new FormDetail();
        NativeDirectBankPageRequest request = new NativeDirectBankPageRequest();
        getValidateDirectBankPageRequestSetBody(request);
        Map<String, String> map = new HashMap<>();
        map.put(PG, "value");
        formDetail.setContent(map);
        nativeDirectBankPageHelper.createCancelRequest(formDetail);
    }

    @Test
    public void testValidateRequestWhenHeadAndBodyNull() {
        NativeDirectBankPageRequest request = new NativeDirectBankPageRequest();
        request.setHead(null);
        request.setBody(null);
        exceptionRule.expect(NativeFlowException.class);
        nativeDirectBankPageHelper.validateRequest(request);
    }

    @Test
    public void testValidateRequestWhenTxnTokenBlank() {
        NativeDirectBankPageRequest request = new NativeDirectBankPageRequest();
        TokenRequestHeader head = new TokenRequestHeader();
        head.setRequestId("requestId");
        request.setHead(head);
        getValidateDirectBankPageRequestSetBody(request);
        exceptionRule.expect(NativeFlowException.class);
        nativeDirectBankPageHelper.validateRequest(request);
    }

    @Test
    public void testValidateRequest() {
        NativeDirectBankPageRequest request1 = new NativeDirectBankPageRequest();
        getValidateDirectBankPageRequestSetBody(request1);
        getValidateDirectBankPageRequestSetHead(request1);
        request1.getBody().setApiRequestOrigin(PG);
        request1.getBody().setAcquirementId("acqId");
        nativeDirectBankPageHelper.validateRequest(request1);

        NativeDirectBankPageRequest request = new NativeDirectBankPageRequest();
        getValidateDirectBankPageRequestSetBody(request);
        getValidateDirectBankPageRequestSetHead(request);
        request.getBody().setApiRequestOrigin(PG);
        exceptionRule.expect(NativeFlowException.class);
        nativeDirectBankPageHelper.validateRequest(request);
    }

    @Test
    public void testValidateRequest1() {
        NativeDirectBankPageRequest request = new NativeDirectBankPageRequest();
        getValidateDirectBankPageRequestSetBody(request);
        getValidateDirectBankPageRequestSetHead(request);
        request.getBody().setApiRequestOrigin("origin");
        nativeDirectBankPageHelper.validateRequest(request);
    }

    @Test
    public void testGetContentForInstaProxyDirectApis() {
        NativeDirectBankPageServiceRequest serviceRequest = new NativeDirectBankPageServiceRequest();
        NativeDirectBankPageCacheData nativeDirectBankPageCacheData = new NativeDirectBankPageCacheData();
        serviceRequest.setCachedBankFormData(nativeDirectBankPageCacheData);
        nativeDirectBankPageCacheData.setBankForm(null);
        nativeDirectBankPageHelper.getContentForInstaProxyDirectApis(serviceRequest,
                NativeDirectBankPageRequestType.resend);

        BankForm bankForm = new BankForm();
        FormDetail[] directForms = new FormDetail[2];
        directForms[1] = new FormDetail("url", "method", "resend", new HashMap<>(), new HashMap<>(), new HashMap<>());
        directForms[0] = new FormDetail("url", "method", "cancel", new HashMap<>(), new HashMap<>(), new HashMap<>());
        bankForm.setDirectForms(directForms);
        nativeDirectBankPageCacheData.setBankForm(bankForm);
        serviceRequest.setCachedBankFormData(nativeDirectBankPageCacheData);
        nativeDirectBankPageHelper.getContentForInstaProxyDirectApis(serviceRequest,
                NativeDirectBankPageRequestType.resend);
    }

    @Test
    public void testProcessForMerchantOwnedDirectBankPage() {
        WorkFlowResponseBean responseBean = new WorkFlowResponseBean();
        WorkFlowRequestBean flowRequestBean = new WorkFlowRequestBean();
        flowRequestBean.setOfflineTxnFlow(true);
        flowRequestBean.setOfflineFlow(true);
        BankForm bankForm = new BankForm();
        bankForm.setPageType("direct");
        nativeDirectBankPageHelper.processForMerchantOwnedDirectBankPage(responseBean, flowRequestBean, bankForm);

        bankForm.setPageType("type");
        nativeDirectBankPageHelper.processForMerchantOwnedDirectBankPage(responseBean, flowRequestBean, bankForm);

        FormDetail[] directForms = new FormDetail[3];
        directForms[1] = new FormDetail("url", "method", "resend", new HashMap<>(), new HashMap<>(), new HashMap<>());
        directForms[0] = new FormDetail("url", "method", "cancel", new HashMap<>(), new HashMap<>(), new HashMap<>());
        directForms[2] = new FormDetail("url", "method", "submit", new HashMap<>(), new HashMap<>(), new HashMap<>());
        bankForm.setDirectForms(directForms);
        bankForm.setPageType("direct");
        when(fF4JHelper.isFF4JFeatureForMidEnabled(any(), any())).thenReturn(true);
        nativeDirectBankPageHelper.processForMerchantOwnedDirectBankPage(responseBean, flowRequestBean, bankForm);

        when(fF4JHelper.isFF4JFeatureForMidEnabled(any(), any())).thenReturn(false);
        new MockUp<AllowedMidCustidPropertyUtil>() {
            @mockit.Mock
            public boolean isMidCustIdEligible(String mid, String custId, String propertyKey, String defaultProperty,
                    Boolean defaultReturnValue) {
                return true;
            }
        };
        when(merchantPreferenceService.isNativeOtpSupported(any())).thenReturn(true);
        nativeDirectBankPageHelper.processForMerchantOwnedDirectBankPage(responseBean, flowRequestBean, bankForm);

        new MockUp<AllowedMidCustidPropertyUtil>() {
            @mockit.Mock
            public boolean isMidCustIdEligible(String mid, String custId, String propertyKey, String defaultProperty,
                    Boolean defaultReturnValue) {
                return false;
            }
        };
        nativeDirectBankPageHelper.processForMerchantOwnedDirectBankPage(responseBean, flowRequestBean, bankForm);

    }

    @Test
    public void testChangeContractFromInstaToTheiaForEnhance() {
        WorkFlowResponseBean responseBean = new WorkFlowResponseBean();
        WorkFlowRequestBean flowRequestBean = new WorkFlowRequestBean();
        flowRequestBean.setOfflineTxnFlow(true);
        flowRequestBean.setOfflineFlow(true);
        BankForm bankForm = new BankForm();
        bankForm.setPageType("direct");
        nativeDirectBankPageHelper.changeContractFromInstaToTheiaForEnhance(responseBean, flowRequestBean, bankForm);

        bankForm.setPageType("type");
        nativeDirectBankPageHelper.changeContractFromInstaToTheiaForEnhance(responseBean, flowRequestBean, bankForm);

        FormDetail[] directForms = new FormDetail[3];
        directForms[1] = new FormDetail("url", "method", "resend", new HashMap<>(), new HashMap<>(), new HashMap<>());
        directForms[0] = new FormDetail("url", "method", "cancel", new HashMap<>(), new HashMap<>(), new HashMap<>());
        directForms[2] = new FormDetail("url", "method", "submit", new HashMap<>(), new HashMap<>(), new HashMap<>());
        bankForm.setDirectForms(directForms);
        bankForm.setPageType("direct");
        nativeDirectBankPageHelper.changeContractFromInstaToTheiaForEnhance(responseBean, flowRequestBean, bankForm);
    }

    @Test
    public void testSetParamsForCashierResponseInHttpRequest() {
        NativeDirectBankPageRequest request = new NativeDirectBankPageRequest();
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        NativeDirectBankPageServiceRequest serviceRequest = new NativeDirectBankPageServiceRequest();
        NativeDirectBankPageServiceResponse serviceResponse = new NativeDirectBankPageServiceResponse(new FormDetail(),
                new DirectAPIResponse());
        getValidateDirectBankPageRequestSetHead(request);
        getValidateDirectBankPageRequestSetBody(request);
        request.setHttpServletRequest(httpServletRequest);
        serviceRequest.setCurrentDirectBankPageSubmitRetryCount(3);
        serviceRequest.setTotalAllowedDirectBankPageSubmitRetryCount(4);
        NativeDirectBankPageCacheData nativeDirectBankPageCacheData = new NativeDirectBankPageCacheData();
        nativeDirectBankPageCacheData.setCashierRequestId("requestId");
        nativeDirectBankPageCacheData.setTransId("transId");
        nativeDirectBankPageCacheData.setMerchantId("merchantId");
        serviceRequest.setCachedBankFormData(nativeDirectBankPageCacheData);
        Map<String, String> map = new HashMap<>();
        map.put(PAYMENT_MODE, "CC");
        FormDetail formDetail = new FormDetail();
        formDetail.setContent(map);
        serviceResponse.setFormDetail(formDetail);
        nativeDirectBankPageHelper.setParamsForCashierResponseInHttpRequest(request, serviceRequest, serviceResponse);

        serviceRequest.setCurrentDirectBankPageSubmitRetryCount(5);
        serviceRequest.setTotalAllowedDirectBankPageSubmitRetryCount(2);
        nativeDirectBankPageHelper.setParamsForCashierResponseInHttpRequest(request, serviceRequest, serviceResponse);

        request.getBody().setRequestType("cancel");
        request.getBody().setApiRequestOrigin(PG);
        InitiateTransactionRequestBody initiateTransactionRequestBody = new InitiateTransactionRequestBody();
        initiateTransactionRequestBody.setMid("mid");
        serviceRequest.setOrderDetail(initiateTransactionRequestBody);
        nativeDirectBankPageHelper.setParamsForCashierResponseInHttpRequest(request, serviceRequest, serviceResponse);
    }

    @Test
    public void testGetCachedBankFormData() {
        NativeDirectBankPageRequest request = new NativeDirectBankPageRequest();
        getValidateDirectBankPageRequestSetBody(request);
        getValidateDirectBankPageRequestSetHead(request);
        request.getBody().setApiRequestOrigin(PG);
        when(nativeSessionUtil.getCashierRequestId(any())).thenReturn("cashierRequestId");
        nativeDirectBankPageHelper.getCachedBankFormData(request);

        request.getBody().setApiRequestOrigin("origin");
        nativeDirectBankPageHelper.getCachedBankFormData(request);

        request.getBody().setApiRequestOrigin(PG);
        when(nativeSessionUtil.getCashierRequestId(any())).thenReturn(null);
        exceptionRule.expect(NativeFlowException.class);
        nativeDirectBankPageHelper.getCachedBankFormData(request);
    }

    @Test
    public void testCheckIfInvalidOtpCase() {
        Map<String, String> data = new HashMap<>();
        nativeDirectBankPageHelper.checkIfInvalidOtpCase(data);

        data.put(DIRECT_BANK_PAGE_INVALID_OTP, "true");
        nativeDirectBankPageHelper.checkIfInvalidOtpCase(data);
    }

    @Test
    public void testProcessNewPaymentRequest() {
        NativeDirectBankPageRequest request = new NativeDirectBankPageRequest();
        getValidateDirectBankPageRequestSetHead(request);
        getValidateDirectBankPageRequestSetBody(request);
        NativeDirectBankPageServiceRequest serviceRequest = new NativeDirectBankPageServiceRequest();
        NativeDirectBankPageCacheData nativeDirectBankPageCacheData = new NativeDirectBankPageCacheData();
        nativeDirectBankPageCacheData.setCashierRequestId("cashierRequestId");
        serviceRequest.setCachedBankFormData(nativeDirectBankPageCacheData);
        nativeDirectBankPageHelper.processNewPaymentRequest(request, serviceRequest);

        InitiateTransactionRequestBody initiateTransactionRequestBody = new InitiateTransactionRequestBody();
        initiateTransactionRequestBody.setMid("mid");
        initiateTransactionRequestBody.setOrderId("orderId");
        serviceRequest.setOrderDetail(initiateTransactionRequestBody);
        nativeDirectBankPageHelper.processNewPaymentRequest(request, serviceRequest);
    }

    @Test
    public void testProcessNewPaymentRequest1() {
        NativeDirectBankPageRequest request = new NativeDirectBankPageRequest();
        getValidateDirectBankPageRequestSetHead(request);
        getValidateDirectBankPageRequestSetBody(request);
        NativeDirectBankPageServiceRequest serviceRequest = new NativeDirectBankPageServiceRequest();
        NativeDirectBankPageCacheData nativeDirectBankPageCacheData = new NativeDirectBankPageCacheData();
        nativeDirectBankPageCacheData.setCashierRequestId("cashierRequestId");
        serviceRequest.setCachedBankFormData(nativeDirectBankPageCacheData);
        GenericCoreResponseBean<WorkFlowResponseBean> workFlowResponseBean = new GenericCoreResponseBean<WorkFlowResponseBean>(
                new WorkFlowResponseBean());
        workFlowResponseBean.setInternalErrorCode(null);
        QueryPaymentStatus queryPaymentStatus = new QueryPaymentStatus(
                new QueryPaymentStatus.QueryPaymentStatusBuilder("transId", "description", "currencyType", "amount",
                        "userId", "value"));
        queryPaymentStatus.setWebFormContext("{\"name\":\"xyz\"}");
        workFlowResponseBean.getResponse().setQueryPaymentStatus(queryPaymentStatus);
        when(seamlessDirectBankCardService.doPayment(any())).thenReturn(workFlowResponseBean);
        when(theiaSessionRedisUtil.get(any())).thenReturn(new WorkFlowRequestBean());
        nativeDirectBankPageHelper.processNewPaymentRequest(request, serviceRequest);

        request.getBody().setApiRequestOrigin(PG);
        nativeDirectBankPageHelper.processNewPaymentRequest(request, serviceRequest);
    }

    @Test
    public void testProcessNewPaymentRequest2() {
        NativeDirectBankPageRequest request = new NativeDirectBankPageRequest();
        getValidateDirectBankPageRequestSetHead(request);
        getValidateDirectBankPageRequestSetBody(request);
        NativeDirectBankPageServiceRequest serviceRequest = new NativeDirectBankPageServiceRequest();
        NativeDirectBankPageCacheData nativeDirectBankPageCacheData = new NativeDirectBankPageCacheData();
        nativeDirectBankPageCacheData.setCashierRequestId("cashierRequestId");
        serviceRequest.setCachedBankFormData(nativeDirectBankPageCacheData);
        QueryPaymentStatus queryPaymentStatus = new QueryPaymentStatus(
                new QueryPaymentStatus.QueryPaymentStatusBuilder("transId", "description", "currencyType", "amount",
                        "userId", "value"));
        when(workFlowResponseBean.getInternalErrorCode()).thenReturn("errorCode");
        queryPaymentStatus.setWebFormContext(null);
        when(seamlessDirectBankCardService.doPayment(any())).thenReturn(workFlowResponseBean);
        when(theiaSessionRedisUtil.get(any())).thenReturn(new WorkFlowRequestBean());
        nativeDirectBankPageHelper.processNewPaymentRequest(request, serviceRequest);

        when(workFlowResponseBean.getInternalErrorCode()).thenReturn(null);
        when(workFlowResponseBean.isSuccessfullyProcessed()).thenReturn(false);
        nativeDirectBankPageHelper.processNewPaymentRequest(request, serviceRequest);

        when(workFlowResponseBean.getInternalErrorCode()).thenReturn(null);
        when(workFlowResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        nativeDirectBankPageHelper.processNewPaymentRequest(request, serviceRequest);

        when(workFlowResponseBean.getInternalErrorCode()).thenReturn(null);
        when(workFlowResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(workFlowResponseBean.getResponse()).thenReturn(new WorkFlowResponseBean());
        nativeDirectBankPageHelper.processNewPaymentRequest(request, serviceRequest);

        when(workFlowResponseBean.getInternalErrorCode()).thenReturn(null);
        when(workFlowResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        WorkFlowResponseBean workFlowResponseBean1 = new WorkFlowResponseBean();
        workFlowResponseBean1.setQueryPaymentStatus(queryPaymentStatus);
        when(workFlowResponseBean.getResponse()).thenReturn(workFlowResponseBean1);
        nativeDirectBankPageHelper.processNewPaymentRequest(request, serviceRequest);
    }

    @Test
    public void testIncrementDirectBankPageSubmitRetryCount() {
        nativeDirectBankPageHelper.incrementDirectBankPageSubmitRetryCount("txnToken", 2);
    }

    @Test
    public void testValidateDirectBankPageSubmitRetryCount() {
        NativeDirectBankPageServiceRequest serviceRequest = new NativeDirectBankPageServiceRequest();
        NativeDirectBankPageRequest request = new NativeDirectBankPageRequest();
        getValidateDirectBankPageRequestSetBody(request);
        getValidateDirectBankPageRequestSetHead(request);
        nativeDirectBankPageHelper.validateDirectBankPageSubmitRetryCount("txnToken", serviceRequest, request);

        serviceRequest.setCachedBankFormData(new NativeDirectBankPageCacheData());
        nativeDirectBankPageHelper.validateDirectBankPageSubmitRetryCount("txnToken", serviceRequest, request);

        NativeDirectBankPageCacheData nativeDirectBankPageCacheData = new NativeDirectBankPageCacheData();
        BankForm bankForm = new BankForm();
        FormDetail[] directForms = new FormDetail[3];
        Map<String, String> content = new HashMap<>();
        content.put(MAX_OTP_RETRY_COUNT, "2");
        directForms[1] = new FormDetail("url", "method", "resend", new HashMap<>(), content, new HashMap<>());
        directForms[0] = new FormDetail("url", "method", "cancel", new HashMap<>(), content, new HashMap<>());
        directForms[2] = new FormDetail("url", "method", "submit", new HashMap<>(), content, new HashMap<>());
        bankForm.setDirectForms(directForms);
        nativeDirectBankPageCacheData.setBankForm(bankForm);
        serviceRequest.setCachedBankFormData(nativeDirectBankPageCacheData);
        nativeDirectBankPageHelper.validateDirectBankPageSubmitRetryCount("txnToken", serviceRequest, request);

        request.getBody().setMaxOtpRetryCount("3");
        exceptionRule.expect(NativeFlowException.class);
        when(nativeSessionUtil.getDirectBankPageSubmitRetryCount(any())).thenReturn(4);
        nativeDirectBankPageHelper.validateDirectBankPageSubmitRetryCount("txnToken", serviceRequest, request);
    }

    @Test
    public void testValidateDirectBankPageResendOtpRetryCount() {
        NativeDirectBankPageServiceRequest serviceRequest = new NativeDirectBankPageServiceRequest();
        NativeDirectBankPageRequest request = new NativeDirectBankPageRequest();
        getValidateDirectBankPageRequestSetBody(request);
        getValidateDirectBankPageRequestSetHead(request);
        nativeDirectBankPageHelper.validateDirectBankPageResendOtpRetryCount("txnToken", serviceRequest, request);

        serviceRequest.setCachedBankFormData(new NativeDirectBankPageCacheData());
        nativeDirectBankPageHelper.validateDirectBankPageResendOtpRetryCount("txnToken", serviceRequest, request);

        NativeDirectBankPageCacheData nativeDirectBankPageCacheData = new NativeDirectBankPageCacheData();
        BankForm bankForm = new BankForm();
        FormDetail[] directForms = new FormDetail[3];
        Map<String, String> content = new HashMap<>();
        content.put(MAX_OTP_RESEND_COUNT, "2");
        directForms[1] = new FormDetail("url", "method", "resend", new HashMap<>(), content, new HashMap<>());
        directForms[0] = new FormDetail("url", "method", "cancel", new HashMap<>(), content, new HashMap<>());
        directForms[2] = new FormDetail("url", "method", "submit", new HashMap<>(), content, new HashMap<>());
        bankForm.setDirectForms(directForms);
        nativeDirectBankPageCacheData.setBankForm(bankForm);
        serviceRequest.setCachedBankFormData(nativeDirectBankPageCacheData);
        nativeDirectBankPageHelper.validateDirectBankPageResendOtpRetryCount("txnToken", serviceRequest, request);

        request.getBody().setMaxOtpResendCount("3");
        when(nativeSessionUtil.getDirectBankPageResendOtpRetryCount(any())).thenReturn(4);
        nativeDirectBankPageHelper.validateDirectBankPageResendOtpRetryCount("txnToken", serviceRequest, request);

        request.getBody().setMaxOtpResendCount("0");
        nativeDirectBankPageHelper.validateDirectBankPageResendOtpRetryCount("txnToken", serviceRequest, request);
    }

    @Test
    public void testIncrementDirectBankPageResendOtpRetryCount() {
        nativeDirectBankPageHelper.incrementDirectBankPageResendOtpRetryCount("txnToken", 2);
    }

    @Test
    public void testGetEnhanceNativeCallBackUrl() {
        nativeDirectBankPageHelper.getEnhanceNativeCallBackUrl("mid", "orderId");
    }

    @Test
    public void testMakeResponseForEnhancedNativeFlow() {
        NativeDirectBankPageRequest request = new NativeDirectBankPageRequest();
        getValidateDirectBankPageRequestSetHead(request);
        TransactionResponse transactionResponse = new TransactionResponse();
        Map<String, String> txnInfo = new HashMap<>();
        transactionResponse.setOrderId("orderId");
        transactionResponse.setMid("mid");
        nativeDirectBankPageHelper.makeResponseForEnhancedNativeFlow(request, transactionResponse, txnInfo);
    }

    @Test
    public void testIsResendOtpAllowed() {
        NativeDirectBankPageServiceRequest serviceRequest = new NativeDirectBankPageServiceRequest();
        serviceRequest.setCurrentDirectBankPageResendOtpRetryCount(2);
        serviceRequest.setTotalAllowedDirectBankPageSubmitRetryCount(1);
        nativeDirectBankPageHelper.isResendOtpAllowed(serviceRequest);

        serviceRequest.setCurrentDirectBankPageResendOtpRetryCount(5);
        serviceRequest.setTotalAllowedDirectBankPageSubmitRetryCount(2);
        nativeDirectBankPageHelper.isResendOtpAllowed(serviceRequest);
    }

    @Test
    public void testIsForceResendOtp() {
        NativeDirectBankPageRequest request = new NativeDirectBankPageRequest();
        FormDetail formDetail = new FormDetail();
        getValidateDirectBankPageRequestSetBody(request);
        request.getBody().setIsForceResendOtp("true");
        nativeDirectBankPageHelper.isForceResendOtp(request, formDetail);

        request.getBody().setIsForceResendOtp("false");
        Map<String, String> map = new HashMap<>();
        map.put("isForceResendOtp", "true");
        formDetail.setContent(map);
        nativeDirectBankPageHelper.isForceResendOtp(request, formDetail);

        request.getBody().setIsForceResendOtp("false");
        map.put("isForceResendOtp", "false");
        formDetail.setContent(map);
        nativeDirectBankPageHelper.isForceResendOtp(request, formDetail);
    }

    @Test
    public void testGetResendOtpMsgWithCount() {
        NativeDirectBankPageServiceRequest serviceRequest = new NativeDirectBankPageServiceRequest();
        serviceRequest.setTotalAllowedDirectBankPageSubmitRetryCount(5);
        serviceRequest.setCurrentDirectBankPageResendOtpRetryCount(2);
        nativeDirectBankPageHelper.getResendOtpMsgWithCount(serviceRequest);
    }

    @Test
    public void testResetCounts() {
        nativeDirectBankPageHelper.resetCounts("txnToken");
    }

    @Test
    public void testGetLastRetryMsg() {
        NativeDirectBankPageServiceRequest serviceRequest = new NativeDirectBankPageServiceRequest();
        serviceRequest.setTotalAllowedDirectBankPageSubmitRetryCount(0);
        serviceRequest.setCurrentDirectBankPageResendOtpRetryCount(0);
        nativeDirectBankPageHelper.getLastRetryMsg(serviceRequest);

        serviceRequest.setTotalAllowedDirectBankPageSubmitRetryCount(5);
        serviceRequest.setCurrentDirectBankPageResendOtpRetryCount(2);
        nativeDirectBankPageHelper.getLastRetryMsg(serviceRequest);
    }

    @Test
    public void testAddMoneyMerchant() {
        nativeDirectBankPageHelper.addMoneyMerchant();

        new MockUp<com.paytm.pgplus.biz.utils.ConfigurationUtil>() {
            @mockit.Mock
            public String getTheiaProperty(String key) {
                return "xyz";
            }
        };

        new MockUp<MDC>() {
            @mockit.Mock
            public String get(String key) {
                return "xyz";
            }
        };
        nativeDirectBankPageHelper.addMoneyMerchant();
    }

    private void getValidateDirectBankPageRequestSetHead(NativeDirectBankPageRequest request) {
        TokenRequestHeader head = new TokenRequestHeader();
        head.setRequestId("requestId");
        head.setTxnToken("txnToken");
        request.setHead(head);
    }

    private void getValidateDirectBankPageRequestSetBody(NativeDirectBankPageRequest request) {
        NativeDirectBankPageRequestBody body = new NativeDirectBankPageRequestBody();
        body.setRequestType("submit");
        body.setApiRequestOrigin("apiRequestSource");
        body.setOtp("otp");
        request.setBody(body);
    }
}