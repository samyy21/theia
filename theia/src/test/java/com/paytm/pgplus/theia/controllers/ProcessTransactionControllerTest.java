package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.core.risk.RiskConstants;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.model.link.LinkDetailResponseBody;
import com.paytm.pgplus.dynamicwrapper.service.IWrapperService;
import com.paytm.pgplus.facade.ppb.models.FetchAccountBalanceResponse;
import com.paytm.pgplus.payloadvault.dynamicwrapper.enums.API;
import com.paytm.pgplus.payloadvault.dynamicwrapper.enums.PayloadType;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.aspects.LocaleFieldAspect;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.cache.model.IPreRedisCacheService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.controllers.helper.OldPgRedirectHelper;
import com.paytm.pgplus.theia.controllers.helper.ProcessTransactionControllerHelper;
import com.paytm.pgplus.theia.exceptions.AccountNumberNotExistException;
import com.paytm.pgplus.theia.helper.UIMicroserviceHelper;
import com.paytm.pgplus.theia.models.ModifiableHttpServletRequest;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.models.uimicroservice.response.UIMicroserviceResponse;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.nativ.utils.RiskVerificationUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.services.helper.EncryptedParamsRequestServiceHelper;
import com.paytm.pgplus.theia.sessiondata.SavingsAccountInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import com.paytm.pgplus.theia.utils.*;
import com.paytm.pgplus.theia.workflow.DefaultService;
import com.paytm.pgplus.theia.workflow.NativeService;
import com.paytm.pgplus.theia.workflow.TheiaLinkService;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.paytm.pgplus.common.config.ConfigurationUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.NATIVE_SHOW_LINK_PAYMENT_PAGE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.GvConsent.ADD_MONEY_TO_GV_CONSENT_KEY;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes.LINK_BASED_PAYMENT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants.CHARGE_AMOUNT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.SubscriptionResponseConstant.TXN_AMOUNT;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * @createdOn 28-Apr-2021
 * @author Anmol
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:servlet-context-test.xml" })
@WebAppConfiguration
public class ProcessTransactionControllerTest {

    private MockMvc mockMvc;
    private MockHttpServletResponse response;
    private MockHttpServletRequest request;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        response = new MockHttpServletResponse();
        request = new MockHttpServletRequest();
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        transactionCacheUtils.putTransInfoInCache("20170613111212800110166869000008982", "SCWMER90619707098260", "abc",
                true);
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty("catalina.base", "");
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Autowired
    private TransactionCacheUtils transactionCacheUtils;

    @Autowired
    WebApplicationContext wac;

    @InjectMocks
    private ProcessTransactionController processTransactionController;

    @Mock
    private OldPgRedirectHelper oldPgRedirectHelper;

    @Mock
    private IPreRedisCacheService preRedisCacheServiceImpl;

    @Mock
    EncryptedParamsRequestServiceHelper encParamRequestService;

    @Mock
    private StagingParamValidator stagingParamValidator;

    @Mock
    private ProcessTransactionUtil processTransactionUtil;

    @Mock
    private ITheiaSessionDataService theiaSessionDataService;

    @Mock
    ProcessTransactionControllerHelper processTransactionControllerHelper;

    @Mock
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Mock
    private ITheiaViewResolverService theiaViewResolverService;

    @Mock
    NativeSessionUtil nativeSessionUtil;

    @Mock
    IWrapperService wrapperService;

    @Mock
    DynamicWrapperUtil dynamicWrapperUtil;

    @Mock
    private LocaleFieldAspect localeFieldAspect;

    @Mock
    private LocalizationUtil localizationUtil;

    @Mock
    private IMerchantPreferenceService merchantPreferenceService;

    @Mock
    MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Mock
    private MerchantPreferenceProvider merchantPreferenceProvider;

    @Mock
    private DefaultService defaultService;

    @Mock
    private NativeService nativeService;

    @Mock
    private NativePaymentUtil nativePaymentUtil;

    @Mock
    private RiskVerificationUtil riskVerificationUtil;

    @Mock
    private LinkPaymentUtil linkPaymentUtil;

    @Mock
    private UIMicroserviceHelper uiMicroserviceHelper;

    @Mock
    private IPgpFf4jClient iPgpFf4jClient;

    @Mock
    private TheiaLinkService theiaLinkService;

    private static String STRING_DEFAULT_MF = "DEFAULT_MF";

    @Test
    public void testProcessPaymentPageOldPageRedirect() throws Exception {
        when(oldPgRedirectHelper.isOldPGRequest(request)).thenReturn(true);
        processTransactionController.processPaymentPage(request, null, null, null);
        Mockito.verify(oldPgRedirectHelper, times(1)).handleOldPgRedirect(request, null, false);
    }

    @Test
    public void testProcessPaymentPageNotOldPageRedirect() throws Exception {
        processTransactionController.processPaymentPage(request, null, null, null);
        Mockito.verify(oldPgRedirectHelper, times(0)).handleOldPgRedirect(request, null, false);
    }

    @Test
    public void testProcessPaymentPage() throws Exception {
        ExtendedInfoRequestBean extendedInfoRequestBean = new ExtendedInfoRequestBean();
        extendedInfoRequestBean.setRequestType(STRING_DEFAULT_MF);
        TransactionInfo transactionInfo = new TransactionInfo();
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();

        when(stagingParamValidator.validate(request, null)).thenReturn(true);
        when(theiaSessionDataService.geExtendedInfoRequestBean(request)).thenReturn(extendedInfoRequestBean);
        when(theiaSessionDataService.getTxnInfoFromSession(request)).thenReturn(transactionInfo);
        when(theiaTransactionalRedisUtil.get("RETRY_PAYMENT_null")).thenReturn(paymentRequestBean);

        processTransactionController.processPaymentPage(request, response, null, null);
    }

    @Test
    public void testProcessPaymentPageGVConsentFlow() throws Exception {
        request.setAttribute(ADD_MONEY_TO_GV_CONSENT_KEY, "");
        processTransactionController.processPaymentPage(request, response, null, null);
        Assert.assertEquals("", request.getAttribute(TheiaConstant.GvConsent.ADD_MONEY_TO_GV_CONSENT_KEY));
    }

    @Test
    public void testProcessPaymentPageAccNumMissing() throws Exception {
        ExtendedInfoRequestBean extendedInfoRequestBean = new ExtendedInfoRequestBean();
        extendedInfoRequestBean.setRequestType(STRING_DEFAULT_MF);
        TransactionInfo transactionInfo = new TransactionInfo();
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        paymentRequestBean.setBankCode("PPBL");
        SavingsAccountInfo savingsAccountInfo = new SavingsAccountInfo();
        savingsAccountInfo.setAccountNumber("");

        when(stagingParamValidator.validate(request, null)).thenReturn(true);
        when(theiaSessionDataService.geExtendedInfoRequestBean(request)).thenReturn(extendedInfoRequestBean);
        when(theiaSessionDataService.getTxnInfoFromSession(request)).thenReturn(transactionInfo);
        when(theiaTransactionalRedisUtil.get("RETRY_PAYMENT_null")).thenReturn(paymentRequestBean);
        when(theiaSessionDataService.getSavingsAccountInfoFromSession(request, true)).thenReturn(savingsAccountInfo);

        exceptionRule.expect(AccountNumberNotExistException.class);
        exceptionRule.expectMessage("Account Number received for DEFAULT_MF is empty");

        processTransactionController.processPaymentPage(request, response, null, null);
    }

    @Test
    public void testCustomProcessTransaction() throws Exception {
        request.setParameter("MID", "TestMid");
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        paymentRequestBean.setMid("TestMid");
        paymentRequestBean.setOrderId("TestOrderId");
        paymentRequestBean.setRequestType(TheiaConstant.RequestTypes.NATIVE);
        Map<String, String[]> additionalParamMap = new HashMap<>();
        String[] val1 = { "true" };
        String[] val2 = { null };
        additionalParamMap.put("MERCHANT_KEY", val1);
        additionalParamMap.put("CHECKSUM_ENABLED", val2);

        when(theiaSessionDataService.validateSession(request)).thenReturn(true);
        when(merchantPreferenceService.isChecksumEnabled("TestMid")).thenReturn(true);
        when(dynamicWrapperUtil.isDynamicWrapperEnabled()).thenReturn(true);
        when(dynamicWrapperUtil.isDynamicWrapperConfigPresent("TestMid", API.PROCESS_TRANSACTION, PayloadType.REQUEST))
                .thenReturn(true);

        when(wrapperService.wrapRequest(any(ModifiableHttpServletRequest.class), anyString(), any())).thenReturn(
                paymentRequestBean);

        processTransactionController.customProcessTransaction(request, response, null);
    }

    @Test
    public void testProcessPaymentPageProcessRequest() throws Exception {
        ExtendedInfoRequestBean extendedInfoRequestBean = new ExtendedInfoRequestBean();
        extendedInfoRequestBean.setRequestType(STRING_DEFAULT_MF);
        TransactionInfo transactionInfo = new TransactionInfo();
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        paymentRequestBean.setRequestType("DEFAULT");
        paymentRequestBean.setIndustryTypeId("NA");
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        pageDetailsResponse.setJspName("Test");

        when(stagingParamValidator.validate(request, null)).thenReturn(true);
        when(theiaSessionDataService.geExtendedInfoRequestBean(request)).thenReturn(extendedInfoRequestBean);
        when(theiaSessionDataService.getTxnInfoFromSession(request)).thenReturn(transactionInfo);
        when(theiaTransactionalRedisUtil.get("RETRY_PAYMENT_null")).thenReturn(paymentRequestBean);
        when(processTransactionControllerHelper.checkIfEnhancedCashierFlow(any(PaymentRequestBean.class), anyObject()))
                .thenReturn(true);
        when(defaultService.processDefaultRequest(any(PaymentRequestBean.class), any()))
                .thenReturn(pageDetailsResponse);

        processTransactionController.processPaymentPage(request, response, null, null);
    }

    @Test
    public void testNativeProcessPaymentAccNoNotExist() throws Exception {
        request.setParameter(RiskConstants.RISK_VERIFIER_UI_KEY, "true");
        request.setParameter(RiskConstants.TOKEN, "testToken");
        request.setParameter("REQUEST_TYPE", ERequestType.NATIVE.name());
        request.setParameter("LINK_ID", "");
        request.setParameter("INVOICE_ID", "");
        request.setParameter(TheiaConstant.GvConsent.GV_CONSENT_FLOW, "true");
        request.setParameter("txnToken", "testToken");
        ExtendedInfoRequestBean extendedInfoRequestBean = new ExtendedInfoRequestBean();
        extendedInfoRequestBean.setRequestType(STRING_DEFAULT_MF);
        Map<String, String[]> additionalParams = new HashMap<>();
        String[] testValues = { "Test" };
        additionalParams.put("Test", testValues);
        InitiateTransactionRequestBody orderDetails = new InitiateTransactionRequestBody();
        orderDetails.setRequestType(ERequestType.DEFAULT_MF.getType());
        orderDetails.setAccountNumber("TestAccNumber");
        FetchAccountBalanceResponse accountBalanceResponse = new FetchAccountBalanceResponse();

        when(riskVerificationUtil.getSecurityIdFromPayReuest(request)).thenReturn("testSecurityId");
        when(nativeSessionUtil.getKey(anyString())).thenReturn(additionalParams);
        when(processTransactionUtil.isNativeKycFlow(request)).thenReturn(true);
        when(processTransactionUtil.processNativeKycFlow(any(), any(), any())).thenReturn(true);
        when(theiaSessionDataService.validateSession(request)).thenReturn(true);
        when(nativeSessionUtil.getOrderDetail(anyString())).thenReturn(orderDetails);
        when(nativeSessionUtil.getAccountBalanceResponseFromCache(anyString())).thenReturn(accountBalanceResponse);

        exceptionRule.expect(AccountNumberNotExistException.class);
        exceptionRule.expectMessage("Account Number received for DEFAULT_MF is empty");

        processTransactionController.nativeProcessPayment(request, response, null, null);
    }

    @Test
    public void testNativeProcessRetryCountBreached() throws Exception {
        request.setAttribute("NATIVE_ENHANCED_FLOW", true);
        when(processTransactionUtil.isNativeKycFlow(request)).thenReturn(false);
        when(processTransactionUtil.isMaxPaymentRetryBreached(request)).thenReturn(true);

        exceptionRule.expect(NativeFlowException.class);
        exceptionRule.expectMessage("Retry count breached");

        processTransactionController.nativeProcessPayment(request, response, null, null);
    }

    @Test
    public void testShowPaymentPageNativeAppInvoke() throws Exception {
        processTransactionController.showPaymentPageNativeAppInvoke(request, response, null, null);
    }

    @Test
    public void testShowLinkPaymentPage() throws Exception {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        paymentRequestBean.setLinkId("TestLinkId");
        paymentRequestBean.setRequestType("LINK_BASED_PAYMENT");
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        pageDetailsResponse.setJspName("TtestJSPName");
        request.setAttribute("NATIVE_ENHANCED_FLOW", true);
        request.setRequestURI(NATIVE_SHOW_LINK_PAYMENT_PAGE);
        request.setParameter(TheiaConstant.RequestParams.Native.MID, "MID");
        request.setParameter(TheiaConstant.RequestParams.Native.ORDER_ID, "orderId");
        request.setParameter(TheiaConstant.RequestParams.Native.TXN_TOKEN, "testToken");
        request.setParameter("REQUEST_TYPE", LINK_BASED_PAYMENT);
        request.setParameter("LINK_ID", "Test");

        when(theiaSessionDataService.validateSession(request)).thenReturn(true);
        when(nativeSessionUtil.getLinkId(any())).thenReturn("TestLinkId");
        when(nativeSessionUtil.getInvoiceId(any())).thenReturn("TestInvoiceId");
        when(theiaLinkService.processLinkRequest(any(), any())).thenReturn(pageDetailsResponse);

        processTransactionController.showLinkPaymentPage(request, response, null, null);
    }

    @Test
    public void testShowAddMoneyToGvConsentPage() throws Exception {
        UIMicroserviceResponse uiMicroserviceResponse = new UIMicroserviceResponse();
        uiMicroserviceResponse.setHtmlPage("Test");
        when(uiMicroserviceHelper.getHtmlPageFromUI(any(), any(), any())).thenReturn(uiMicroserviceResponse);

        processTransactionController.showAddMoneyToGvConsentPage(request, response);
    }
}