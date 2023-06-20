package com.paytm.pgplus.theia.controller.test;

import java.util.HashMap;
import java.util.Map;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.cashier.enums.PaymentType;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.CashierEnvInfo;
import com.paytm.pgplus.cashier.pay.model.PaymentRequest;
import com.paytm.pgplus.cashier.payoption.PayBillOptions;
import com.paytm.pgplus.facade.enums.TerminalType;
import com.paytm.pgplus.facade.enums.TransType;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeUncheckedException;
import com.paytm.pgplus.facade.user.models.UserDetails;
import com.paytm.pgplus.facade.user.models.UserDetailsV2;
import com.paytm.pgplus.facade.user.models.request.FetchUserDetailsRequest;
import com.paytm.pgplus.facade.user.models.response.FetchUserDetailsResponse;
import com.paytm.pgplus.facade.user.services.impl.AuthenticationImpl;
import com.paytm.pgplus.theia.test.util.TestRequestUtil;
import com.paytm.pgplus.theia.test.util.TestResource;
import mockit.Mock;
import mockit.MockUp;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.DigitalCreditRequest;
import com.paytm.pgplus.cashier.validator.service.IDigitalCreditPassCodeValidator;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:servlet-context-test.xml" })
@WebAppConfiguration
@Configuration
public class SeamlessPaymentTest {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    WebApplicationContext wac;

    private ObjectMapper mapper = new ObjectMapper();
    private MockMvc mockMvc;

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeControllerTest.class);

    protected static TestResource testResource = TestResource.getInstance();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty("catalina.base", "");
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        mockAuthenticationImpl();
    }

    @Autowired
    @Qualifier("digitalCreditPassCodeValidator")
    private IDigitalCreditPassCodeValidator digitalCreditPassCodeValidator;

    @Test
    public void testDigitalCreditPayment() throws Exception {
        try {
            digitalCreditPassCodeValidator.validatePassCodeForPaytmCC(getCashierRequest());
        } catch (PaytmValidationException e) {
            LOGGER.error("Exception Occurred : {} ", e);
            throw new PaytmValidationException(e);
        }
    }

    private CashierRequest getCashierRequest() throws CashierCheckedException {
        System.setProperty("catalina.base", "temp");
        String oauthClientId = "paytm-pg-client-staging";
        String oauthClientSecret = "a7426be0-a2dd-47cf-a181-b37c801f34c6";
        Map<PayMethod, String> payOptions = new HashMap<>();
        payOptions.put(PayMethod.MP_COD, "MP_COD_CODMOCK");
        Map<String, String> channelInfo = new HashMap<>();
        channelInfo.put("isEMI", "N");
        PayBillOptions.PayBillOptionsBuilder payBillOptionsBuillder = new PayBillOptions.PayBillOptionsBuilder(100L,
                0L, payOptions).setTopAndPay(false).setSaveChannelInfoAfterPay(false).setChannelInfo(channelInfo);
        // These are not production env values. Please ignore.
        final CashierEnvInfo cashierEnvInfo = new CashierEnvInfo.CashierEnvInfoBuilder("10.0.122.22", TerminalType.WEB)
                .clientKey("e5806b64-598d-414f-b7f7-83f9576eb6f").websiteLanguage("en_US").osType("Windows.PC")
                .appVersion("1.0").sdkVersion("1.0").sessionId("8EU6mLl5mUpUBgyRFT4v7DjfQ3fcauthcenter")
                .tokenId("a8d359d6-ca3d-4048-9295-bbea5f6715a6").orderOsType("orderOsType")
                .orderTerminalType("orderTerminalType").merchantAppVersion("merchantAppVersion").build();
        PaymentRequest paymentRequest = new PaymentRequest(new PaymentRequest.PaymentRequestBuilder(
                PaymentType.ONLY_COD, "20160414111212800110166285700000698", TransType.ACQUIRING, "1234",
                payBillOptionsBuillder.build(), cashierEnvInfo));
        CashierRequest cashierRequest = new CashierRequest(new CashierRequest.CashierRequestBuilder("", true));
        cashierRequest.setPaymentRequest(paymentRequest);
        DigitalCreditRequest digitalCreditRequest = new DigitalCreditRequest("", "", "", "1111", 3);
        digitalCreditRequest.setUserMobile("9599711105");
        digitalCreditRequest.setClientId("paytm-pg-client-staging");
        digitalCreditRequest.setClientSecret("a7426be0-a2dd-47cf-a181-b37c801f34c6");
        cashierRequest.setDigitalCreditRequest(digitalCreditRequest);
        return cashierRequest;
    }

    private void mockAuthenticationImpl() {
        new MockUp<AuthenticationImpl>() {
            @Mock
            public FetchUserDetailsResponse fetchUserDetailsV2(FetchUserDetailsRequest fetchUserDetailsRequest)
                    throws FacadeCheckedException, FacadeUncheckedException {
                String outhUserDetailsJson = "{\"accessToken\":{\"expiryTime\":\"1530614453093\",\"clientId\":\"1107195935\",\"scopes\":[\"wallet\"],\"userId\":1107195935},\"basicInfo\":{\"phone\":\"9997864013\",\"countryCode\":\"91\",\"firstName\":\"saurabh\",\"lastName\":\"yadav\",\"displayName\":\"saurabh\"},\"userAttributeInfo\":{},\"isKyc\":false}";
                UserDetailsV2 userDetailsV2 = TestRequestUtil.mapJsonToObject(testResource.getObjectMapper(),
                        outhUserDetailsJson, UserDetailsV2.class);
                UserDetails userDetails = new UserDetails(userDetailsV2);
                return new FetchUserDetailsResponse(userDetails);
            }
        };
    }
}
