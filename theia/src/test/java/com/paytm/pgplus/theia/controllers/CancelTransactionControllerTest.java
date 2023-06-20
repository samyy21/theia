//package com.paytm.pgplus.theia.controllers;
//
//import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
//import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
//import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
//import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
//import com.paytm.pgplus.common.responsecode.enums.SystemResponseCode;
//import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
//import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
//import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
//import com.paytm.pgplus.pgproxycommon.utils.ValidationMessage;
//import com.paytm.pgplus.request.InitiateTransactionRequest;
//import com.paytm.pgplus.request.InitiateTransactionRequestBody;
//import com.paytm.pgplus.theia.cache.IMerchantMappingService;
//import com.paytm.pgplus.theia.constants.TheiaConstant;
//import com.paytm.pgplus.theia.exceptions.CoreSessionExpiredException;
//import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
//import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
//import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
//import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
//import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
//import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
//import com.paytm.pgplus.theia.services.helper.FF4JHelper;
//import com.paytm.pgplus.theia.sessiondata.MerchantInfo;
//import com.paytm.pgplus.theia.sessiondata.TransactionConfig;
//import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
//import com.paytm.pgplus.theia.utils.*;
//import org.junit.*;
//import org.junit.rules.ExpectedException;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.mock.web.MockHttpServletRequest;
//import org.springframework.mock.web.MockHttpServletResponse;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.web.WebAppConfiguration;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//
//import javax.ws.rs.core.MultivaluedHashMap;
//import javax.ws.rs.core.MultivaluedMap;
//
//import static org.mockito.Mockito.times;
//
///**
// * @createdOn 26-Apr-2021
// * @author Anmol
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = { "classpath:servlet-context-test.xml" })
//@WebAppConfiguration
//public class CancelTransactionControllerTest {
//
//    @InjectMocks
//    private CancelTransactionController cancelTransactionController;
//
//    @Before
//    public void setup() {
//        MockitoAnnotations.initMocks(this);
//    }
//
//    @Rule
//    public ExpectedException exceptionRule = ExpectedException.none();
//
//    @Mock
//    private FF4JHelper ff4JHelper;
//
//    @Mock
//    private NativeSessionUtil nativeSessionUtil;
//
//    @Mock
//    private ITheiaSessionDataService theiaSessionDataService;
//
//    @Mock
//    private TheiaResponseGenerator theiaResponseGenerator;
//
//    @Mock
//    ResponseCodeUtil responseCodeUtil;
//
//    @Mock
//    ITheiaSessionRedisUtil theiaSessionRedisUtil;
//
//    @Mock
//    private ITheiaViewResolverService theiaViewResolverService;
//
//    @Mock
//    private IMerchantMappingService merchantMappingService;
//
//    @Mock
//    private FlowDataMapper requestGeneratorHelper;
//
//    @Mock
//    private AOAUtils aoaUtils;
//
//    @Mock
//    private IWorkFlow cancelTransactionFlow;
//
//    @Mock
//    private AddMoneyToGvConsentUtil addMoneyToGvConsentUtil;
//
//    @Autowired
//    private TransactionCacheUtils transactionCacheUtils;
//
//    @Autowired
//    WebApplicationContext wac;
//
//    private MockMvc mockMvc;
//    private MockHttpServletResponse response;
//    private MockHttpServletRequest request;
//
//    @BeforeClass
//    public static void setUpBeforeClass() throws Exception {
//        System.setProperty("catalina.base", "");
//    }
//
//    @Before
//    public void setUp() {
//        MockitoAnnotations.initMocks(this);
//        response = new MockHttpServletResponse();
//        request = new MockHttpServletRequest();
//        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
//        transactionCacheUtils.putTransInfoInCache("20170613111212800110166869000008982", "SCWMER90619707098260", "abc",
//                true);
//    }
//
//    private final String TEST_TXN_TOKEN = "TestToken";
//    private final String TEST_MID = "TestMid";
//    private final String TEST_CALLBACK_URL = "TestCallBackURL";
//    private final String TXN_TOKEN_PARAM = "txnToken";
//    private final String TEST_TXN_ID = "Test";
//
//    @Test
//    public void testFailCancelTxnWithoutToken() {
//        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
//        request.setParameter(TXN_TOKEN_PARAM, "");
//        Mockito.when(
//                ff4JHelper.isFF4JFeatureForMidEnabled(
//                        TheiaConstant.ExtraConstants.THEIA_FAIL_CANCEL_TRANSACTION_WITHOUT_TOKEN,
//                        paymentRequestBean.getMid())).thenReturn(true);
//        cancelTransactionController.cancelTransaction(request, null, null, null);
//        Mockito.verify(responseCodeUtil, times(1)).getResponseCodeDetails(SystemResponseCode.DEFAULT_PENDING_CODE);
//    }
//
//    @Test
//    public void testMismatchedTxnToken() {
//        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
//        request.setParameter(TXN_TOKEN_PARAM, TEST_TXN_TOKEN);
//        Mockito.when(
//                ff4JHelper.isFF4JFeatureForMidEnabled(
//                        TheiaConstant.ExtraConstants.THEIA_FAIL_CANCEL_TRANSACTION_WITHOUT_TOKEN,
//                        paymentRequestBean.getMid())).thenReturn(true);
//        cancelTransactionController.cancelTransaction(request, null, null, null);
//        Mockito.verify(theiaViewResolverService, times(1)).returnOOPSPage(request);
//    }
//
//    @Test
//    public void testCancelNativeTransactionTransIdNull() {
//        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
//        request.setParameter(TXN_TOKEN_PARAM, TEST_TXN_TOKEN);
//        Mockito.when(
//                ff4JHelper.isFF4JFeatureForMidEnabled(
//                        TheiaConstant.ExtraConstants.THEIA_FAIL_CANCEL_TRANSACTION_WITHOUT_TOKEN,
//                        paymentRequestBean.getMid())).thenReturn(true);
//        Mockito.when(nativeSessionUtil.getKey(null)).thenReturn(TEST_TXN_TOKEN);
//        exceptionRule.expect(CoreSessionExpiredException.class);
//        exceptionRule.expectMessage("Exception Occurred while cancel transcation");
//        cancelTransactionController.cancelTransaction(request, null, null, null);
//    }
//
//    @Test
//    public void testCancelNativeTransactionFail() throws Exception {
//        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
//        InitiateTransactionRequestBody initiateTransactionRequestBody = new InitiateTransactionRequestBody();
//        InitiateTransactionRequest initiateTransactionRequest = new InitiateTransactionRequest();
//        initiateTransactionRequest.setBody(initiateTransactionRequestBody);
//        NativeInitiateRequest nativeInitiateRequest = new NativeInitiateRequest();
//        nativeInitiateRequest.setInitiateTxnReq(initiateTransactionRequest);
//        MappingMerchantData mappingMerchantData = new MappingMerchantData();
//        WorkFlowRequestBean errorWorkFlowReqBean = new WorkFlowRequestBean();
//        request.setParameter(TXN_TOKEN_PARAM, TEST_TXN_TOKEN);
//        TransactionResponse transactionResponse = new TransactionResponse();
//        transactionResponse.setMid(TEST_MID);
//        GenericCoreResponseBean errorResponse = new GenericCoreResponseBean(errorWorkFlowReqBean);
//
//        Mockito.when(
//                ff4JHelper.isFF4JFeatureForMidEnabled(
//                        TheiaConstant.ExtraConstants.THEIA_FAIL_CANCEL_TRANSACTION_WITHOUT_TOKEN,
//                        paymentRequestBean.getMid())).thenReturn(true);
//        Mockito.when(nativeSessionUtil.getKey(null)).thenReturn(TEST_TXN_TOKEN);
//        Mockito.when(nativeSessionUtil.getTxnId(TEST_TXN_TOKEN)).thenReturn("");
//        Mockito.when(nativeSessionUtil.validate(TEST_TXN_TOKEN)).thenReturn(nativeInitiateRequest);
//        Mockito.when(merchantMappingService.getMappingMerchantData(null)).thenReturn(mappingMerchantData);
//        Mockito.when(cancelTransactionFlow.process(errorWorkFlowReqBean)).thenReturn(errorResponse);
//        Mockito.when(requestGeneratorHelper.createCancelTransactionRequest("", null, "closeReason*141")).thenReturn(
//                errorWorkFlowReqBean);
//        Mockito.when(
//                theiaResponseGenerator.createTransactionResponseForNativeCloseOrder(initiateTransactionRequestBody,
//                        paymentRequestBean, "")).thenReturn(transactionResponse);
//
//        exceptionRule.expect(CoreSessionExpiredException.class);
//        exceptionRule.expectMessage("Exception Occurred while cancel transcation");
//        cancelTransactionController.cancelTransaction(request, null, null, null);
//    }
//
//    @Test
//    public void testCancelTransactionMerchantDataIsNull() {
//        TransactionInfo txnData = getTxnInfoTestData();
//        request.setParameter(TXN_TOKEN_PARAM, TEST_TXN_TOKEN);
//        TransactionResponse transactionResponse = new TransactionResponse();
//        transactionResponse.setMid(TEST_MID);
//
//        Mockito.when(theiaSessionDataService.getTxnInfoFromSession(request)).thenReturn(txnData);
//
//        exceptionRule.expect(CoreSessionExpiredException.class);
//        exceptionRule.expectMessage("Exception Occurred while cancel transcation");
//        cancelTransactionController.cancelTransaction(request, null, null, null);
//    }
//
//    @Test
//    public void testCancelTransactionTxnConfigNull() {
//        TransactionInfo txnData = getTxnInfoTestData();
//        request.setParameter(TXN_TOKEN_PARAM, TEST_TXN_TOKEN);
//        TransactionResponse transactionResponse = new TransactionResponse();
//        transactionResponse.setMid(TEST_MID);
//        MerchantInfo merchantInfo = new MerchantInfo();
//
//        Mockito.when(theiaSessionDataService.getTxnInfoFromSession(request)).thenReturn(txnData);
//        Mockito.when(theiaSessionDataService.getMerchantInfoFromSession(request)).thenReturn(merchantInfo);
//
//        exceptionRule.expect(CoreSessionExpiredException.class);
//        exceptionRule.expectMessage("Exception Occurred while cancel transcation");
//        cancelTransactionController.cancelTransaction(request, null, null, null);
//    }
//
//    @Test
//    public void testCancelTransactionFailTxnTypeAcquiring() {
//        TransactionInfo txnData = getTxnInfoTestData();
//        request.setParameter(TXN_TOKEN_PARAM, TEST_TXN_TOKEN);
//        TransactionResponse transactionResponse = new TransactionResponse();
//        transactionResponse.setMid(TEST_MID);
//        MerchantInfo merchantInfo = new MerchantInfo();
//        TransactionConfig txnConfig = new TransactionConfig();
//        txnConfig.setTxnType(TheiaConstant.TxnType.ACQUIRING);
//        WorkFlowRequestBean errorWorkFlowReqBean = new WorkFlowRequestBean();
//        MultivaluedMap<ValidationMessage, String> errors = new MultivaluedHashMap<>();
//        errors.add(ValidationMessage.ERROR_MSG, "");
//        GenericCoreResponseBean errorResponse = new GenericCoreResponseBean(errors);
//        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
//
//        Mockito.when(theiaSessionDataService.getTxnInfoFromSession(request)).thenReturn(txnData);
//        Mockito.when(theiaSessionDataService.getMerchantInfoFromSession(request)).thenReturn(merchantInfo);
//        Mockito.when(theiaSessionDataService.getTxnConfigFromSession(request)).thenReturn(txnConfig);
//        Mockito.when(
//                requestGeneratorHelper.createCancelTransactionRequest(txnData, merchantInfo, paymentRequestBean,
//                        "closeReason*141")).thenReturn(errorWorkFlowReqBean);
//        Mockito.when(cancelTransactionFlow.process(null)).thenReturn(errorResponse);
//
//        exceptionRule.expect(CoreSessionExpiredException.class);
//        exceptionRule.expectMessage("Exception Occurred while cancel transcation");
//        cancelTransactionController.cancelTransaction(request, null, null, null);
//    }
//
//    @Test
//    public void testCancelTransactionFailTxnTypeFund() {
//        TransactionInfo txnData = getTxnInfoTestData();
//        request.setParameter(TXN_TOKEN_PARAM, TEST_TXN_TOKEN);
//        TransactionResponse transactionResponse = new TransactionResponse();
//        transactionResponse.setMid(TEST_MID);
//        MerchantInfo merchantInfo = new MerchantInfo();
//        TransactionConfig txnConfig = new TransactionConfig();
//        txnConfig.setTxnType(TheiaConstant.TxnType.FUND);
//        WorkFlowRequestBean errorWorkFlowReqBean = new WorkFlowRequestBean();
//        MultivaluedMap<ValidationMessage, String> errors = new MultivaluedHashMap<>();
//        errors.add(ValidationMessage.ERROR_MSG, "");
//        GenericCoreResponseBean errorResponse = new GenericCoreResponseBean(errors);
//        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
//
//        Mockito.when(theiaSessionDataService.getTxnInfoFromSession(request)).thenReturn(txnData);
//        Mockito.when(theiaSessionDataService.getMerchantInfoFromSession(request)).thenReturn(merchantInfo);
//        Mockito.when(theiaSessionDataService.getTxnConfigFromSession(request)).thenReturn(txnConfig);
//        Mockito.when(
//                requestGeneratorHelper.createCancelTransactionRequest(txnData, merchantInfo, paymentRequestBean,
//                        "closeReason*141")).thenReturn(errorWorkFlowReqBean);
//        Mockito.when(cancelTransactionFlow.process(null)).thenReturn(errorResponse);
//
//        exceptionRule.expect(CoreSessionExpiredException.class);
//        exceptionRule.expectMessage("Exception Occurred while cancel transcation");
//        cancelTransactionController.cancelTransaction(request, null, null, null);
//    }
//
//    @Test
//    public void testCancelTransactionTxnTypeFund() {
//        TransactionInfo txnData = getTxnInfoTestData();
//        request.setParameter(TXN_TOKEN_PARAM, TEST_TXN_TOKEN);
//        TransactionResponse transactionResponse = getTransactionResponseTestData();
//        MerchantInfo merchantInfo = new MerchantInfo();
//        merchantInfo.setMid(TEST_MID);
//        TransactionConfig txnConfig = new TransactionConfig();
//        txnConfig.setTxnType(TheiaConstant.TxnType.FUND);
//        WorkFlowRequestBean errorWorkFlowReqBean = new WorkFlowRequestBean();
//        GenericCoreResponseBean errorResponse = new GenericCoreResponseBean(errorWorkFlowReqBean);
//        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
//        ExtendedInfoRequestBean extendInfo = new ExtendedInfoRequestBean();
//        extendInfo.setCallBackURL(TEST_CALLBACK_URL);
//
//        Mockito.when(theiaSessionDataService.getTxnInfoFromSession(request)).thenReturn(txnData);
//        Mockito.when(theiaSessionDataService.getMerchantInfoFromSession(request)).thenReturn(merchantInfo);
//        Mockito.when(theiaSessionDataService.getTxnConfigFromSession(request)).thenReturn(txnConfig);
//        Mockito.when(
//                requestGeneratorHelper.createCancelTransactionRequest(txnData, merchantInfo, paymentRequestBean,
//                        "closeReason*141")).thenReturn(errorWorkFlowReqBean);
//        Mockito.when(cancelTransactionFlow.process(null)).thenReturn(errorResponse);
//        Mockito.when(theiaSessionDataService.geExtendedInfoRequestBean(request)).thenReturn(extendInfo);
//        Mockito.when(theiaResponseGenerator.getFinalHtmlResponse(transactionResponse)).thenReturn("TestResponse");
//
//        String result = cancelTransactionController.cancelTransaction(request, null, null, null);
//        Assert.assertNull(result);
//    }
//
//    private TransactionResponse getTransactionResponseTestData() {
//        TransactionResponse transactionResponse = new TransactionResponse();
//        transactionResponse.setMid(TEST_MID);
//        transactionResponse.setTransactionStatus("TXN_FAILURE");
//        transactionResponse.setCurrency("INR");
//        transactionResponse.setCallbackUrl(TEST_CALLBACK_URL);
//        transactionResponse.setOfflineRequest(false);
//        transactionResponse.setTxnId(TEST_TXN_ID);
//        return transactionResponse;
//    }
//
//    private TransactionInfo getTxnInfoTestData() {
//        TransactionInfo txnData = new TransactionInfo();
//        txnData.setTxnId(TEST_TXN_ID);
//        return txnData;
//    }
//
// }