package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.utils.AuthUtil;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.sessiondata.*;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * @createdOn 07/07/21
 * @author Aman Shevkar.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:servlet-context-test.xml" })
@WebAppConfiguration
public class TestControllerTest {
    @Mock
    ITheiaViewResolverService theiaViewResolverService;

    @Mock
    ITheiaSessionDataService sessionDataService;

    @Mock
    AuthUtil authUtil;

    @Mock
    NativeSessionUtil nativeSessionUtil;

    @Mock
    Model model;

    @InjectMocks
    TestController testController;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Autowired
    private TransactionCacheUtils transactionCacheUtils;

    @Autowired
    WebApplicationContext wac;

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

    @Test
    public void testHome() throws Exception {
        String result = testController.home(Locale.US, model);
        Assert.assertEquals("merchantcheckout", result);
    }

    @Test
    public void testNpciMock() throws Exception {
        String result = testController.npciMock(Locale.US, model);
        Assert.assertEquals("npcimock", result);
    }

    @Test
    public void testDmrc() throws Exception {
        String result = testController.dmrc(Locale.US, model);
        Assert.assertEquals("dmrcmerchantcheckout", result);
    }

    @Test
    public void testPaytmCallback() throws Exception {
        String result = testController.paytmCallback(Locale.US, model);
        Assert.assertEquals("paytmCallback", result);
    }

    @Test
    public void testTest() throws Exception {
        when(theiaViewResolverService.returnPaymentPage(any())).thenReturn("returnPaymentPageResponse");
        String result = testController.test(request, Locale.US, model);
        Assert.assertEquals("returnPaymentPageResponse", result);
    }

    @Test
    public void testPayment_IfNoRequestParameters() throws Exception {
        when(theiaViewResolverService.returnPaymentPage(any())).thenReturn("returnPaymentPageResponse");
        when(sessionDataService.getLoginInfoFromSession(any(), anyBoolean())).thenReturn(new LoginInfo());
        when(sessionDataService.getTxnInfoFromSession(any(), anyBoolean())).thenReturn(new TransactionInfo());
        when(sessionDataService.getTxnConfigFromSession(any(), anyBoolean())).thenReturn(new TransactionConfig());
        when(sessionDataService.getWalletInfoFromSession(any(), anyBoolean())).thenReturn(new WalletInfo());
        String result = testController.payment(request);
        Assert.assertEquals("returnPaymentPageResponse", result);
    }

    @Test
    public void testPayment_IfRequestParametersExists() throws Exception {
        when(theiaViewResolverService.returnPaymentPage(any())).thenReturn("returnPaymentPageResponse");
        when(sessionDataService.getLoginInfoFromSession(any(), anyBoolean())).thenReturn(new LoginInfo());
        when(sessionDataService.getTxnInfoFromSession(any(), anyBoolean())).thenReturn(new TransactionInfo());
        when(sessionDataService.getTxnConfigFromSession(any(), anyBoolean())).thenReturn(new TransactionConfig());
        when(sessionDataService.getWalletInfoFromSession(any(), anyBoolean())).thenReturn(new WalletInfo());
        request.setParameter("Parameter_1", "TestParameter_1");
        request.setParameter("Parameter_2", "TestParameter_2");
        request.setParameter("Parameter_3", "TestParameter_3");
        request.setParameter("Parameter_4", "TestParameter_4");
        String result = testController.payment(request);
        Assert.assertEquals("returnPaymentPageResponse", result);
    }

    @Test
    public void testOauth() throws Exception {
        when(theiaViewResolverService.returnOAuthPage(any())).thenReturn("returnOAuthPageResponse");
        when(sessionDataService.getLoginInfoFromSession(any(), anyBoolean())).thenReturn(new LoginInfo());
        when(sessionDataService.getTxnInfoFromSession(any(), anyBoolean())).thenReturn(new TransactionInfo());
        when(sessionDataService.getTxnConfigFromSession(any(), anyBoolean())).thenReturn(new TransactionConfig());
        when(sessionDataService.getWalletInfoFromSession(any(), anyBoolean())).thenReturn(new WalletInfo());

        String result = testController.oauth(request, Locale.CANADA, model);
        Assert.assertEquals("returnOAuthPageResponse", result);
    }

    @Test
    public void testOops() throws Exception {
        when(theiaViewResolverService.returnOOPSPage(any())).thenReturn("returnOOPSPageResponse");
        when(sessionDataService.getLoginInfoFromSession(any(), anyBoolean())).thenReturn(new LoginInfo());
        when(sessionDataService.getTxnInfoFromSession(any(), anyBoolean())).thenReturn(new TransactionInfo());
        when(sessionDataService.getTxnConfigFromSession(any(), anyBoolean())).thenReturn(new TransactionConfig());
        when(sessionDataService.getWalletInfoFromSession(any(), anyBoolean())).thenReturn(new WalletInfo());

        String result = testController.oops(request, Locale.CANADA, model);
        Assert.assertEquals("returnOOPSPageResponse", result);
    }

    @Test
    public void testError() throws Exception {
        when(theiaViewResolverService.returnOOPSPage(any())).thenReturn("returnOOPSPageResponse");

        String result = testController.error(request, Locale.CANADA, model);
        Assert.assertEquals("returnOOPSPageResponse", result);
    }

    @Test
    public void testHome2() throws Exception {
        when(theiaViewResolverService.returnPaymentPage(any())).thenReturn("returnPaymentPageResponse");
        when(sessionDataService.getLoginInfoFromSession(any(), anyBoolean())).thenReturn(new LoginInfo());
        when(sessionDataService.getTxnInfoFromSession(any(), anyBoolean())).thenReturn(new TransactionInfo());
        when(sessionDataService.getTxnConfigFromSession(any(), anyBoolean())).thenReturn(new TransactionConfig());
        when(sessionDataService.getWalletInfoFromSession(any(), anyBoolean())).thenReturn(new WalletInfo());

        String result = testController.home2(request, Locale.CANADA, model);
        Assert.assertEquals("returnPaymentPageResponse", result);
    }

    @Test
    public void testTestJSON() throws Exception {
        PaymentRequestBean requestBean = new PaymentRequestBean();
        requestBean.setMid("TestMid");
        requestBean.setOrderId("TestOrderId");
        requestBean.setCustId("TestCustomerId");
        requestBean.setTxnAmount("100");
        requestBean.setChannelId("TestChannelId");
        requestBean.setMobileNo("9889345799");
        requestBean.setPeonURL("TestPeonURL");
        requestBean.setPaymentModeOnly("TestPaymentMode");
        testController.testJSON(requestBean, request);
    }

    @Test
    public void testProcessRequestForFetchingOrderId_RequestDataNotPresent() throws Exception {
        // To test functionality when the InputStream is null or
        // ServletInputStream is null
        request.setMethod("TestMethod");
        request.setRequestURI("TestRequestURI");
        request.setServletPath("ServletPathLink");
        request.setContextPath("ContextPathLink");
        ResponseEntity<Map<String, Object>> result = testController.processRequestForFetchingOrderId(request);
        Assert.assertEquals(null, result);
    }

    @Test
    public void testProcessRequestForFetchingOrderId_InvalidCachedAttributes() throws Exception {
        // To test functionality when the cachedMid or cachedSSOToken is not
        // equal to corresponding Mid or SSoToken
        request.setMethod("TestMethod");
        request.setRequestURI("TestRequestURI");
        request.setServletPath("ServletPathLink");
        request.setContextPath("ContextPathLink");
        when(nativeSessionUtil.getField(anyString(), eq(TheiaConstant.RequestParams.Native.MID))).thenReturn(
                "FauxTestMiDId");
        when(nativeSessionUtil.getField(anyString(), eq(BizConstant.ExtendedInfoKeys.SSO_TOKEN))).thenReturn(
                "FauxTestSSOToken");
        String requestString = "{\"basePath\":\"/SomePath\",\"refId\":\"TestRefId\",\"mid\":\"TestMiDId\",\"ssoToken\":\"TestSSOToken\"}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                requestString.getBytes(StandardCharsets.UTF_8));
        ServletInputStream servletInputStream = new ServletInputStream() {
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
        byte token[] = IOUtils.toByteArray(servletInputStream);
        request.setContent(token);
        ResponseEntity<Map<String, Object>> result = testController.processRequestForFetchingOrderId(request);
        Assert.assertEquals(null, result);
    }

    @Test
    public void testProcessRequestForFetchingOrderId_InvalidUserAuth() throws Exception {
        // To test functionality when the SsoToken passed for user is Invalid or
        // User is Invalid
        request.setMethod("TestMethod");
        request.setRequestURI("TestRequestURI");
        request.setServletPath("ServletPathLink");
        request.setContextPath("ContextPathLink");
        when(authUtil.isUserValid(anyString())).thenReturn(false);
        when(nativeSessionUtil.getField(anyString(), eq(TheiaConstant.RequestParams.Native.MID))).thenReturn(
                "TestMiDId");
        when(nativeSessionUtil.getField(anyString(), eq(BizConstant.ExtendedInfoKeys.SSO_TOKEN))).thenReturn(
                "TestSSOToken");
        when(nativeSessionUtil.getField(anyString(), eq(TheiaConstant.RequestParams.Native.ORDER_ID))).thenReturn(
                "TestOrderId@1234");
        String requestString = "{\"basePath\":\"/SomePath\",\"refId\":\"TestRefId\",\"mid\":\"TestMiDId\",\"ssoToken\":\"TestSSOToken\"}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                requestString.getBytes(StandardCharsets.UTF_8));
        ServletInputStream servletInputStream = new ServletInputStream() {
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
        byte token[] = IOUtils.toByteArray(servletInputStream);
        request.setContent(token);
        ResponseEntity<Map<String, Object>> result = testController.processRequestForFetchingOrderId(request);
        Assert.assertEquals(null, result);
    }

    @Test
    public void testProcessRequestForFetchingOrderId() throws Exception {
        // To test functionality as a whole when required attributes are passed.
        request.setMethod("TestMethod");
        request.setRequestURI("TestRequestURI");
        request.setServletPath("ServletPathLink");
        request.setContextPath("ContextPathLink");
        when(authUtil.isUserValid(anyString())).thenReturn(true);
        when(nativeSessionUtil.getField(anyString(), eq(TheiaConstant.RequestParams.Native.MID))).thenReturn(
                "TestMiDId");
        when(nativeSessionUtil.getField(anyString(), eq(BizConstant.ExtendedInfoKeys.SSO_TOKEN))).thenReturn(
                "TestSSOToken");
        when(nativeSessionUtil.getField(anyString(), eq(TheiaConstant.RequestParams.Native.ORDER_ID))).thenReturn(
                "TestOrderId@1234");
        String requestString = "{\"basePath\":\"/SomePath\",\"refId\":\"TestRefId\",\"mid\":\"TestMiDId\",\"ssoToken\":\"TestSSOToken\"}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                requestString.getBytes(StandardCharsets.UTF_8));
        ServletInputStream servletInputStream = new ServletInputStream() {
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
        byte token[] = IOUtils.toByteArray(servletInputStream);
        request.setContent(token);
        ResponseEntity<Map<String, Object>> result = testController.processRequestForFetchingOrderId(request);
        Assert.assertEquals(HttpStatus.OK.value(), result.getStatusCode().value());
    }

}