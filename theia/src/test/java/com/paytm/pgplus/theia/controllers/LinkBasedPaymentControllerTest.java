package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.user.models.request.SendOTPRequestV1;
import com.paytm.pgplus.facade.user.models.request.SendOtpRequest;
import com.paytm.pgplus.facade.user.models.response.GenerateLoginOtpResponse;
import com.paytm.pgplus.facade.user.services.IAuthentication;
import com.paytm.pgplus.theia.helper.LinkBasedPaymentHelper;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.utils.MerchantDataUtil;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
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

import java.util.UUID;

import static org.mockito.Matchers.any;

/**
 * @author Anmol
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:servlet-context-test.xml" })
@WebAppConfiguration
public class LinkBasedPaymentControllerTest {

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
    private LinkBasedPaymentController linkBasedPaymentController;

    @Mock
    private IAuthentication authenticationImpl;

    @Mock
    private LinkBasedPaymentHelper linkBasedPaymentHelper;

    @Mock
    private ITheiaViewResolverService theiaViewResolverService;

    @Mock
    private MerchantDataUtil merchantDataUtil;

    @Mock
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Mock
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Test
    public void generateLoginOTPV2Test() throws FacadeCheckedException {
        SendOtpRequest sendOtpRequest = getSendOtpRequestTestData();
        GenerateLoginOtpResponse generateLoginOtpResponse = new GenerateLoginOtpResponse("status", "state",
                "responseCode", "message");
        Mockito.when(authenticationImpl.generateSendOtp(any())).thenReturn(generateLoginOtpResponse);

        String res = linkBasedPaymentController.genrateLoginOTPV2(request, response, null, null, sendOtpRequest);
        Assert.assertNotNull(res);
    }

    @Test
    public void generateSendOTPV1LimitBreachedTest() throws FacadeCheckedException {
        SendOTPRequestV1 sendOTPRequestV1 = new SendOTPRequestV1();
        sendOTPRequestV1.setBody(getSendOtpRequestTestData());
        sendOTPRequestV1.getBody().setResendCount("4");
        GenerateLoginOtpResponse generateLoginOtpResponse = new GenerateLoginOtpResponse("status", "state",
                "responseCode", "message");
        Mockito.when(authenticationImpl.generateSendOtp(any())).thenReturn(generateLoginOtpResponse);

        String finalResponse = linkBasedPaymentController.generateSendOTPV1(request, response, null, null,
                sendOTPRequestV1);
        String expectedResponse = "{\"status\":\"FAILURE\",\"responseCode\":\"531\",\"message\":\"You have exceeded the max retries for sending OTP. Please try after sometime\"}";

        Assert.assertEquals(expectedResponse, finalResponse);
    }

    private SendOtpRequest getSendOtpRequestTestData() {
        SendOtpRequest sendOtpRequest = new SendOtpRequest();
        sendOtpRequest.setMid("TestMid");
        sendOtpRequest.setUniqueId(UUID.randomUUID().toString());
        sendOtpRequest.setLinkId("TestLinkId");
        sendOtpRequest.setMerchantName("TestMerchantName");
        sendOtpRequest.setMobileNumber("9001234567");
        sendOtpRequest.setTxnAmount("100");
        sendOtpRequest.setResendCount("1");
        return sendOtpRequest;
    }

}