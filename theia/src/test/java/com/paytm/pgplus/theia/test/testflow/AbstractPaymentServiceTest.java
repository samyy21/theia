package com.paytm.pgplus.theia.test.testflow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.paytm.pgplus.biz.core.user.service.impl.SavedCardsImpl;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.cache.model.MerchantExtendedInfoResponse;
import com.paytm.pgplus.facade.enums.ServiceUrl;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.mappingserviceclient.service.impl.MerchantDataServiceImpl;
import com.paytm.pgplus.mappingserviceclient.util.MappingClientUtil;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.user.service.impl.LoginImpl;
import com.paytm.pgplus.checksum.utils.MappingServiceUtil;
import com.paytm.pgplus.facade.enums.AlipayServiceUrl;
import com.paytm.pgplus.facade.payment.models.request.PayviewConsultRequest;
import com.paytm.pgplus.facade.user.helper.AuthenticationHelper;
import com.paytm.pgplus.facade.user.models.UserDetails;
import com.paytm.pgplus.facade.user.models.UserDetailsV2;
import com.paytm.pgplus.facade.user.models.response.FetchUserDetailsResponse;
import com.paytm.pgplus.facade.utils.ConnectionUtil;
import com.paytm.pgplus.mappingserviceclient.application.MappingServiceClient;
import com.paytm.pgplus.mappingserviceclient.constant.MappingConstant;
import com.paytm.pgplus.mappingserviceclient.enums.MappingServiceUrl;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.util.MappingClientPropertiesUtil;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.services.IJsonResponsePaymentService;
import com.paytm.pgplus.theia.services.IPaymentService;
import com.paytm.pgplus.theia.test.util.TestRequestUtil;
import com.paytm.pgplus.theia.test.util.TestResource;

import mockit.Mock;
import mockit.MockUp;

/**
 * @author kartik
 * @date 30-05-2017
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:servlet-context-test.xml" })
@WebAppConfiguration
@EnableWebMvc
public abstract class AbstractPaymentServiceTest {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    WebApplicationContext wac;

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractPaymentServiceTest.class);
    protected static TestResource testResource = TestResource.getInstance();
    protected static String MAPPING_SERVICE_BASE_URL;
    protected HttpServletRequest request;
    protected String alipayResponse;
    protected static List<String> testMerchantsList = new ArrayList<String>();
    protected static List<String> testBanksList = new ArrayList<String>();
    protected static List<String> testUsersList = new ArrayList<String>();
    protected static List<String> testResponseCodesList = new ArrayList<String>();
    protected static List<String> testResponseCodesDetailList = new ArrayList<String>();

    @BeforeClass
    public static void initialize() {
        String catalinaBase = System.getProperty("catalina.base", null);
        if (catalinaBase == null) {
            System.setProperty("catalina.base", "/path/to/catalina/base/");
        }
        String rmiServerHost = System.getProperty("java.rmi.server.hostname", null);
        if (rmiServerHost == null) {
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        }
        try {
            MAPPING_SERVICE_BASE_URL = MappingClientPropertiesUtil.getInstance().getProperties()
                    .getProperty(MappingConstant.PropertyConstant.MAPPING_SERVICE_BASE_URL);
        } catch (IOException e) {
            LOGGER.error("Error occured while loading mapping client properties ", e);
        }
        String testMerchants = testResource.getTestProperties().getProperty("test.merchants.list");
        String testBanks = testResource.getTestProperties().getProperty("test.banks.list");
        String testUsers = testResource.getTestProperties().getProperty("test.users.list");
        String testResponseCodes = testResource.getTestProperties().getProperty("test.response.codes.list");
        String testPaytmProperties = testResource.getTestProperties().getProperty("test.oauth.properties");
        String testResponseCodesDetail = testResource.getTestProperties()
                .getProperty("test.response.code.details.list");

        createList(testMerchants, testMerchantsList);
        createList(testBanks, testBanksList);
        createList(testUsers, testUsersList);
        createList(testResponseCodes, testResponseCodesList);
        createList(testResponseCodesDetail, testResponseCodesDetailList);
    }

    protected static void createList(String str, List<String> list) {
        String[] values = str.split(",");
        for (String value : values) {
            list.add(value);
        }
    }

    @Before
    public void setUp() {

        request = new MockHttpServletRequest();
        ((MockHttpServletRequest) request).addHeader("X-Forwarded-For", "157.49.0.80,49.44.115.108");
        ((MockHttpServletRequest) request).addHeader("User-Agent", "DummyUserAgent");
        ((MockHttpServletRequest) request).addHeader("CHANNEL_ID", "WEB");
        ((MockHttpServletRequest) request).addHeader("DEVICE_ID", "Xiaomi-RedmiNote4-864238032497308");
        request.getSession(true);
        mockAuthenticationHelper();

        mockPlatformPlus();

        mockMappingServiceUtil();

        mockLoginImpl();

        mockMappingServiceClient();

        mockSavedCardsImpl();
    }

    private void mockMappingServiceClient() {
        // Mock MS
        new MockUp<MappingServiceClient>() {
            @SuppressWarnings("incomplete-switch")
            @Mock
            public <T> T getData(final String redisKey, final String mappingID,
                    final MappingServiceUrl mappingServiceUrlEnum, final Class<T> clazz)
                    throws MappingServiceClientException {

                String mappingResponse = null;
                String[] arrPathParams = mappingID.split("/");
                int i;
                switch (mappingServiceUrlEnum) {
                case MERCHANT_MAPPING:
                    i = checkTestValueInList(arrPathParams[0], testMerchantsList);
                    mappingResponse = testResource.getTestProperties().getProperty("test.merchant.mapping." + i);
                    if (StringUtils.isBlank(mappingResponse)) {
                        throw new IllegalArgumentException("Merchant not found in test merchants list");
                    }
                    break;
                case FETCH_MERCHANT_DATA:
                    i = checkTestValueInList(arrPathParams[0], testMerchantsList);
                    mappingResponse = testResource.getTestProperties().getProperty("test.merchant.data." + i);
                    if (StringUtils.isBlank(mappingResponse)) {
                        throw new IllegalArgumentException("Merchant not found in test merchants list");
                    }
                    break;
                case FETCH_PREFERENCE_DATA:
                    i = checkTestValueInList(arrPathParams[0], testMerchantsList);
                    mappingResponse = testResource.getTestProperties().getProperty("test.merchant.preference." + i);
                    if (StringUtils.isBlank(mappingResponse)) {
                        throw new IllegalArgumentException("Merchant not found in test merchants list");
                    }
                    break;
                case MERCHANT_URL_INFO:
                    i = checkTestValueInList(arrPathParams[0], testMerchantsList);
                    mappingResponse = testResource.getTestProperties().getProperty("test.merchant.url.info." + i);
                    if (StringUtils.isBlank(mappingResponse)) {
                        throw new IllegalArgumentException("Merchant not found in test merchants list");
                    }
                    break;
                case MERCHANT_OFFER_DETAILS:
                    throw new IllegalArgumentException("No offer configured on this merchant");
                case GET_USER_MAPPING_URL:
                    i = checkTestValueInList(arrPathParams[1], testUsersList);
                    mappingResponse = testResource.getTestProperties().getProperty("test.user.mapping." + i);
                    if (StringUtils.isBlank(mappingResponse)) {
                        throw new IllegalArgumentException("User not found in test users list");
                    }
                    break;
                case BANK_INFO:
                    i = checkTestValueInList(arrPathParams[0], testBanksList);
                    mappingResponse = testResource.getTestProperties().getProperty("test.bank.info." + i);
                    if (StringUtils.isBlank(mappingResponse)) {
                        throw new IllegalArgumentException("Bank not found in test banks list");
                    }
                    break;
                case GET_RESPONSE_CODE_URL_PAYTM:
                    i = checkTestValueInList(arrPathParams[0], testResponseCodesList);
                    mappingResponse = testResource.getTestProperties().getProperty("test.response.code.details." + i);
                    if (StringUtils.isBlank(mappingResponse)) {
                        throw new IllegalArgumentException("Response code not found in test response codes list");
                    }
                    break;
                case GET_RESPONSE_CODE_URL_ALIPAY:
                    i = checkTestValueInList(arrPathParams[0], testResponseCodesDetailList);
                    mappingResponse = testResource.getTestProperties().getProperty("test.response.code.list." + i);
                    if (StringUtils.isBlank(mappingResponse)) {
                        throw new IllegalArgumentException("Response code not found in test response codes list");
                    }
                    break;
                case GET_PAYTM_PROPERTY:
                    mappingResponse = testResource.getTestProperties().getProperty("test.oauth.properties");
                    if (StringUtils.isBlank(mappingResponse)) {
                        throw new IllegalArgumentException("Response code not found in test response codes list");
                    }
                    break;
                case MERCHANT_API_URL_INFO:
                    mappingResponse = testResource.getTestProperties().getProperty("test.merchant.api.url.info");
                    if (StringUtils.isBlank(mappingResponse)) {
                        throw new IllegalArgumentException("Response code not found in test response codes list");
                    }
                    break;
                case GET_BIN_DETAIL_URL:
                    mappingResponse = testResource.getTestProperties().getProperty("test.bin.detail.url.reponse");
                    if (StringUtils.isBlank(mappingResponse)) {
                        throw new IllegalArgumentException("Response code not found in test response codes list");
                    }
                    break;
                }

                if (mappingServiceUrlEnum.isWrapped()) {
                    try {
                        mappingResponse = testResource.getObjectMapper().readTree(mappingResponse).get("response")
                                .toString();
                    } catch (IOException e) {
                        throw new MappingServiceClientException("Exception while processing response string", e);
                    }
                }
                try {
                    return testResource.getObjectMapper().readValue(mappingResponse, clazz);
                } catch (IOException e) {
                    LOGGER.error("Unable to case mappingResponse string");
                    throw new MappingServiceClientException("Unable to case mappingResponse string");
                }
            }
        };
    }

    private void mockLoginImpl() {
        // Mock SavedCardService
        new MockUp<LoginImpl>() {
            @Mock
            private void fetchSavedCardDetails(UserDetailsBiz userDetails) {
                CardBeanBiz savedCard = new CardBeanBiz(2001737L, "4018064502938440", "VISA", "022021", 401806L, 7946L,
                        1, "1107195773", "ICICI", "custId", "mId");
                List<CardBeanBiz> cardBeanBiz = new ArrayList<CardBeanBiz>();
                cardBeanBiz.add(savedCard);
                userDetails.setMerchantViewSavedCardsList(cardBeanBiz);
            }
        };
    }

    private void mockSavedCardsImpl() {
        new MockUp<SavedCardsImpl>() {
            @Mock
            public GenericCoreResponseBean<List<CardBeanBiz>> fetchSavedCardsByUserId(String userid) {
                CardBeanBiz savedCard = new CardBeanBiz(2001737L, "4018064502938440", "VISA", "022021", 401806L, 7946L,
                        1, "1107195773", "ICICI", "custId", "mId");
                List<CardBeanBiz> bizSavedCards = new ArrayList<CardBeanBiz>();
                bizSavedCards.add(savedCard);
                return new GenericCoreResponseBean<>(bizSavedCards);
            }
        };
    }

    private void mockMappingServiceUtil() {
        new MockUp<MappingServiceUtil>() {

            @Mock
            public String getMasterKey() {
                return "T9!&S7V&53A$PAYTM";
            }

        };
    }

    private void mockAuthenticationHelper() {
        // Mock OAuth
        new MockUp<AuthenticationHelper>() {
            @Mock
            public FetchUserDetailsResponse validateAndProcessResponseForFetchUserDetails(Response response)
                    throws FacadeCheckedException {
                String outhUserDetailsJson = testResource.getTestProperties().getProperty(
                        "oauth.fetch.user.details.response");
                UserDetailsV2 userDetailsV2 = TestRequestUtil.mapJsonToObject(testResource.getObjectMapper(),
                        outhUserDetailsJson, UserDetailsV2.class);
                UserDetails userDetails = new UserDetails(userDetailsV2);
                return new FetchUserDetailsResponse(userDetails);
            }
        };
    }

    private void mockPlatformPlus() {
        // Mock Platform+
        new MockUp<ConnectionUtil>() {
            @Mock
            public <T, U> T execute(final U request, final AlipayServiceUrl api, final Class<T> clazz, String reqMsgId)
                    throws FacadeCheckedException {
                String alipayResponse = "";
                if (api.equals(AlipayServiceUrl.ACQUIRING_ORDER_CREATEORDER)) {
                    alipayResponse = testResource.getTestProperties().getProperty("createOrder.response");
                }
                if (api.equals(AlipayServiceUrl.PAYMENT_CASHIER_PAYVIEW_CONSULT)) {
                    PayviewConsultRequest payViewRequest = PayviewConsultRequest.class.cast(request);
                    if (StringUtils.isBlank(payViewRequest.getBody().getPayerUserId())) {
                        alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.payment.cashier.payview.consult\",\"clientId\":\"clientId\",\"reqMsgId\":\"0f4d7d2a2ec64b7485201bc5d38caf22administratorthinkpadl450\",\"version\":\"1.1.7\",\"respTime\":\"2017-06-05T13:55:37+05:30\"},\"body\":{\"transDesc\":\"PARCEL892193\",\"transAmount\":{\"currency\":\"INR\",\"value\":\"200\"},\"transId\":\"20170605111212800110166452600008765\",\"resultInfo\":{\"resultMsg\":\"SUCCESS\",\"resultCode\":\"SUCCESS\",\"resultCodeId\":\"00000000\",\"resultStatus\":\"S\"},\"productCode\":\"51051000100000000001\",\"transCreatedTime\":\"2017-06-05T13:54:47+05:30\",\"transType\":\"ACQUIRING\",\"securityId\":\"sid899bbbe9c90a4ad22b451f92c3e566b2\",\"payMethodViews\":[{\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"CREDIT_CARD\",\"payChannelOptionViews\":[{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_VISA\",\"enableStatus\":true,\"instId\":\"VISA\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"Visa Inc.\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_MASTER\",\"enableStatus\":true,\"instId\":\"MASTER\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"MasterCard Inc.\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_MAESTRO\",\"enableStatus\":true,\"instId\":\"MAESTRO\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"Maestro\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_RUPAY\",\"enableStatus\":true,\"instId\":\"RUPAY\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"RuPay\"},{\"payOption\":\"CREDIT_CARD_AMEX\",\"enableStatus\":false,\"instId\":\"AMEX\",\"payMethod\":\"CREDIT_CARD\",\"disableReason\":\"SERVICE_INST_NOT_EXIST\",\"instName\":\"American Express\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_DINERS\",\"enableStatus\":true,\"instId\":\"DINERS\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"Diners Club International\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_DISCOVER\",\"enableStatus\":true,\"instId\":\"DISCOVER\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"Discover Inc.\"}]},{\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"DEBIT_CARD\",\"payChannelOptionViews\":[{\"supportCountries\":[\"IN\"],\"payOption\":\"DEBIT_CARD_VISA\",\"enableStatus\":true,\"instId\":\"VISA\",\"payMethod\":\"DEBIT_CARD\",\"instName\":\"Visa Inc.\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"DEBIT_CARD_MASTER\",\"enableStatus\":true,\"instId\":\"MASTER\",\"payMethod\":\"DEBIT_CARD\",\"instName\":\"MasterCard Inc.\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"DEBIT_CARD_MAESTRO\",\"enableStatus\":true,\"instId\":\"MAESTRO\",\"payMethod\":\"DEBIT_CARD\",\"instName\":\"Maestro\"},{\"payOption\":\"DEBIT_CARD_RUPAY\",\"enableStatus\":false,\"instId\":\"RUPAY\",\"payMethod\":\"DEBIT_CARD\",\"disableReason\":\"SERVICE_INST_NOT_EXIST\",\"instName\":\"RuPay\"},{\"payOption\":\"DEBIT_CARD_DINERS\",\"enableStatus\":false,\"instId\":\"DINERS\",\"payMethod\":\"DEBIT_CARD\",\"disableReason\":\"SERVICE_INST_NOT_EXIST\",\"instName\":\"Diners Club International\"}]},{\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"NET_BANKING\",\"payChannelOptionViews\":[{\"payOption\":\"NET_BANKING_HDFC\",\"enableStatus\":false,\"instId\":\"HDFC\",\"payMethod\":\"NET_BANKING\",\"disableReason\":\"CHANNEL_NOT_AVAILABLE\",\"instName\":\"HDFC Bank (Housing Development Finance Corporation)\"},{\"payOption\":\"NET_BANKING_ICICI\",\"enableStatus\":false,\"instId\":\"ICICI\",\"payMethod\":\"NET_BANKING\",\"disableReason\":\"CHANNEL_NOT_AVAILABLE\",\"instName\":\"ICICI Bank (Industrial Credit and Investment Corporation of India)\"},{\"payOption\":\"NET_BANKING_AXIS\",\"enableStatus\":false,\"instId\":\"AXIS\",\"payMethod\":\"NET_BANKING\",\"disableReason\":\"SERVICE_INST_NOT_EXIST\",\"instName\":\"Axis Bank Limited\"},{\"payOption\":\"NET_BANKING_STB\",\"enableStatus\":true,\"instId\":\"STB\",\"payMethod\":\"NET_BANKING\",\"instName\":\"Saraswat Co-operative Bank Ltd\"}]},{\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"EMI\",\"payChannelOptionViews\":[{\"payOption\":\"EMI_HDFC\",\"enableStatus\":true,\"instId\":\"HDFC\",\"payMethod\":\"EMI\",\"instName\":\"HDFC Bank (Housing Development Finance Corporation)\",\"emiChannelInfos\":[{\"minAmount\":{\"currency\":\"INR\",\"value\":\"100\"},\"maxAmount\":{\"value\":\"10000\",\"currency\":\"INR\"},\"cardAcquiringMode\":\"ONUS\",\"interestRate\":\"15.0\",\"ofMonths\":\"6\"},{\"minAmount\":{\"currency\":\"INR\",\"value\":\"100\"},\"maxAmount\":{\"value\":\"10000\",\"currency\":\"INR\"},\"cardAcquiringMode\":\"ONUS\",\"interestRate\":\"15.0\",\"ofMonths\":\"3\"}]}]},{\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"UPI\",\"payChannelOptionViews\":[{\"payOption\":\"UPI\",\"enableStatus\":true,\"instId\":\"UPI\",\"payMethod\":\"UPI\",\"instName\":\"Unified Payment Interace\"}]}],\"extendInfo\":\"{\\\"website\\\":\\\"retail\\\",\\\"productCode\\\":\\\"51051000100000000001\\\",\\\"theme\\\":\\\"merchant\\\",\\\"ssoToken\\\":\\\"7a43d1c5-2cd2-45b5-a03b-203d3857f3dd\\\",\\\"requestType\\\":\\\"DEFAULT\\\",\\\"phoneNo\\\":\\\"YYYYYYYYYY\\\",\\\"merchantTransId\\\":\\\"PARCEL892193\\\",\\\"topupAndPay\\\":\\\"false\\\",\\\"callBackURL\\\":\\\"https://pg-staging.paytm.in/MerchantSite/bankResponse\\\",\\\"email\\\":\\\"XXX@YYY.ZZZ\\\",\\\"paytmMerchantId\\\":\\\"DataCl59062077159771\\\",\\\"isSupportAddPay\\\":\\\"Y\\\",\\\"alipayMerchantId\\\":\\\"216820000000145754283\\\",\\\"merchantName\\\":\\\"DataClean\\\",\\\"mccCode\\\":\\\"Retail\\\",\\\"totalTxnAmount\\\":\\\"200\\\",\\\"promoCode\\\":\\\"\\\"}\",\"merchantId\":\"216820000000145754283\",\"chargePayer\":false}},\"signature\":\"no_signature\"}";
                    } else {
                        alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.payment.cashier.payview.consult\",\"clientId\":\"clientId\",\"reqMsgId\":\"0f4d7d2a2ec64b7485201bc5d38caf22administratorthinkpadl450\",\"version\":\"1.1.7\",\"respTime\":\"2017-06-05T13:55:37+05:30\"},\"body\":{\"transDesc\":\"PARCEL892193\",\"transAmount\":{\"currency\":\"INR\",\"value\":\"200\"},\"transId\":\"20170605111212800110166452600008765\",\"resultInfo\":{\"resultMsg\":\"SUCCESS\",\"resultCode\":\"SUCCESS\",\"resultCodeId\":\"00000000\",\"resultStatus\":\"S\"},\"productCode\":\"51051000100000000001\",\"transCreatedTime\":\"2017-06-05T13:54:47+05:30\",\"transType\":\"ACQUIRING\",\"securityId\":\"sid899bbbe9c90a4ad22b451f92c3e566b2\",\"payMethodViews\":[{\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"BALANCE\",\"payChannelOptionViews\":[{\"payOption\":\"BALANCE\",\"enableStatus\":true,\"payMethod\":\"BALANCE\",\"balanceChannelInfos\":[{\"payerAccountNo\":\"20070000000006569456\",\"accountBalance\":{\"value\":\"578000\",\"currency\":\"INR\"}}]}]},{\"payChannelOptionViews\":[{\"externalAccountInfos\":[{\"extendInfo\":\"{\\\"lenderId\\\":\\\"ICICI\\\",\\\"lenderDescription\\\":\\\"ICICI Bank Digital Credit\\\",\\\"otpRequired\\\":\\\"false\\\"}\",\"accountBalance\":{\"value\":\"747650\",\"currency\":\"INR\"},\"externalAccountNo\":\"test-pg-account-number\"}],\"payOption\":\"PAYTM_DIGITAL_CREDIT\",\"enableStatus\":true,\"instId\":\"PAYTMCC\",\"instName\":\"Paytm Digital Credit\",\"payMethod\":\"PAYTM_DIGITAL_CREDIT\"}],\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"PAYTM_DIGITAL_CREDIT\"},{\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"CREDIT_CARD\",\"payChannelOptionViews\":[{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_VISA\",\"enableStatus\":true,\"instId\":\"VISA\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"Visa Inc.\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_MASTER\",\"enableStatus\":true,\"instId\":\"MASTER\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"MasterCard Inc.\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_MAESTRO\",\"enableStatus\":true,\"instId\":\"MAESTRO\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"Maestro\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_RUPAY\",\"enableStatus\":true,\"instId\":\"RUPAY\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"RuPay\"},{\"payOption\":\"CREDIT_CARD_AMEX\",\"enableStatus\":false,\"instId\":\"AMEX\",\"payMethod\":\"CREDIT_CARD\",\"disableReason\":\"SERVICE_INST_NOT_EXIST\",\"instName\":\"American Express\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_DINERS\",\"enableStatus\":true,\"instId\":\"DINERS\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"Diners Club International\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_DISCOVER\",\"enableStatus\":true,\"instId\":\"DISCOVER\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"Discover Inc.\"}]},{\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"DEBIT_CARD\",\"payChannelOptionViews\":[{\"supportCountries\":[\"IN\"],\"payOption\":\"DEBIT_CARD_VISA\",\"enableStatus\":true,\"instId\":\"VISA\",\"payMethod\":\"DEBIT_CARD\",\"instName\":\"Visa Inc.\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"DEBIT_CARD_MASTER\",\"enableStatus\":true,\"instId\":\"MASTER\",\"payMethod\":\"DEBIT_CARD\",\"instName\":\"MasterCard Inc.\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"DEBIT_CARD_MAESTRO\",\"enableStatus\":true,\"instId\":\"MAESTRO\",\"payMethod\":\"DEBIT_CARD\",\"instName\":\"Maestro\"},{\"payOption\":\"DEBIT_CARD_RUPAY\",\"enableStatus\":false,\"instId\":\"RUPAY\",\"payMethod\":\"DEBIT_CARD\",\"disableReason\":\"SERVICE_INST_NOT_EXIST\",\"instName\":\"RuPay\"},{\"payOption\":\"DEBIT_CARD_DINERS\",\"enableStatus\":false,\"instId\":\"DINERS\",\"payMethod\":\"DEBIT_CARD\",\"disableReason\":\"SERVICE_INST_NOT_EXIST\",\"instName\":\"Diners Club International\"}]},{\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"NET_BANKING\",\"payChannelOptionViews\":[{\"payOption\":\"NET_BANKING_HDFC\",\"enableStatus\":false,\"instId\":\"HDFC\",\"payMethod\":\"NET_BANKING\",\"disableReason\":\"CHANNEL_NOT_AVAILABLE\",\"instName\":\"HDFC Bank (Housing Development Finance Corporation)\"},{\"payOption\":\"NET_BANKING_ICICI\",\"enableStatus\":false,\"instId\":\"ICICI\",\"payMethod\":\"NET_BANKING\",\"disableReason\":\"CHANNEL_NOT_AVAILABLE\",\"instName\":\"ICICI Bank (Industrial Credit and Investment Corporation of India)\"},{\"payOption\":\"NET_BANKING_AXIS\",\"enableStatus\":false,\"instId\":\"AXIS\",\"payMethod\":\"NET_BANKING\",\"disableReason\":\"SERVICE_INST_NOT_EXIST\",\"instName\":\"Axis Bank Limited\"},{\"payOption\":\"NET_BANKING_STB\",\"enableStatus\":true,\"instId\":\"STB\",\"payMethod\":\"NET_BANKING\",\"instName\":\"Saraswat Co-operative Bank Ltd\"}]},{\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"EMI\",\"payChannelOptionViews\":[{\"payOption\":\"EMI_HDFC\",\"enableStatus\":true,\"instId\":\"HDFC\",\"payMethod\":\"EMI\",\"instName\":\"HDFC Bank (Housing Development Finance Corporation)\",\"emiChannelInfos\":[{\"minAmount\":{\"currency\":\"INR\",\"value\":\"100\"},\"maxAmount\":{\"value\":\"10000\",\"currency\":\"INR\"},\"cardAcquiringMode\":\"ONUS\",\"interestRate\":\"15.0\",\"ofMonths\":\"6\"},{\"minAmount\":{\"currency\":\"INR\",\"value\":\"100\"},\"maxAmount\":{\"value\":\"10000\",\"currency\":\"INR\"},\"cardAcquiringMode\":\"ONUS\",\"interestRate\":\"15.0\",\"ofMonths\":\"3\"}]}]},{\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"UPI\",\"payChannelOptionViews\":[{\"payOption\":\"UPI\",\"enableStatus\":true,\"instId\":\"UPI\",\"payMethod\":\"UPI\",\"instName\":\"Unified Payment Interace\"}]}],\"extendInfo\":\"{\\\"website\\\":\\\"retail\\\",\\\"productCode\\\":\\\"51051000100000000001\\\",\\\"theme\\\":\\\"merchant\\\",\\\"ssoToken\\\":\\\"7a43d1c5-2cd2-45b5-a03b-203d3857f3dd\\\",\\\"requestType\\\":\\\"DEFAULT\\\",\\\"phoneNo\\\":\\\"YYYYYYYYYY\\\",\\\"merchantTransId\\\":\\\"PARCEL892193\\\",\\\"topupAndPay\\\":\\\"false\\\",\\\"callBackURL\\\":\\\"https://pg-staging.paytm.in/MerchantSite/bankResponse\\\",\\\"email\\\":\\\"XXX@YYY.ZZZ\\\",\\\"paytmMerchantId\\\":\\\"DataCl59062077159771\\\",\\\"isSupportAddPay\\\":\\\"Y\\\",\\\"alipayMerchantId\\\":\\\"216820000000145754283\\\",\\\"merchantName\\\":\\\"DataClean\\\",\\\"mccCode\\\":\\\"Retail\\\",\\\"totalTxnAmount\\\":\\\"200\\\",\\\"promoCode\\\":\\\"\\\"}\",\"merchantId\":\"216820000000145754283\",\"chargePayer\":false}},\"signature\":\"no_signature\"}";
                    }
                }
                if (api.equals(AlipayServiceUrl.QUERY_SUCCESS_RATE)) {
                    alipayResponse = testResource.getTestProperties().getProperty("query.success.rate.response");
                }
                if (api.equals(AlipayServiceUrl.BOSS_CHARGE_FEE_BATCH_CONSULT)) {
                    alipayResponse = testResource.getTestProperties().getProperty(
                            "boss.charge.fee.batch.consult.response");
                }

                if (api.equals(AlipayServiceUrl.ACQUIRING_ORDER_CREATEORDER_AND_PAY)) {
                    String toAppend = System.getProperty("acquiringOrderCreateOrderAndPayCustomBehaviour", "");
                    alipayResponse = testResource.getTestProperties().getProperty(
                            "createOrderAndPay.response" + toAppend);
                }

                if (api.equals(AlipayServiceUrl.USER_ASSET_CACHE_CARD)) {
                    alipayResponse = testResource.getTestProperties().getProperty("user.asset.cacheCard.response");
                }

                if (api.equals(AlipayServiceUrl.PAYMENT_CASHIER_PAYRESULT_QUERY)) {
                    alipayResponse = testResource.getTestProperties().getProperty("payresult.query.response");
                }

                if (api.equals(AlipayServiceUrl.CHANNEL_ACCOUNT_QUERY)) {
                    alipayResponse = testResource.getTestProperties().getProperty("channel.account.query.response");
                }

                if (api.equals(AlipayServiceUrl.PAYMENT_CASHIER_LITEPAYVIEW_CONSULT)) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.payment.cashier.litepayview.consult\",\"clientId\":\"2016030715243903536806\",\"reqMsgId\":\"7216bb15b7e84eb989c62dc6de3c3920pgptheia39paytmlocal\",\"version\":\"fixed-a\",\"respTime\":\"2021-04-12T20:24:52+05:30\"},\"body\":{\"chargePayer\":\"false\",\"payMethodViews\":[{\"payChannelOptionViews\":[{\"enableStatus\":\"true\",\"instId\":\"UPIPUSH\",\"instName\":\"Unified Payment Interface - PUSH\",\"oneClickChannel\":\"false\",\"payMethod\":\"UPI\",\"payOption\":\"UPI_PUSH\",\"prepaidCardChannel\":\"false\"},{\"enableStatus\":\"true\",\"instId\":\"UPI\",\"instName\":\"Unified Payment Interace\",\"oneClickChannel\":\"false\",\"payMethod\":\"UPI\",\"payOption\":\"UPI\",\"prepaidCardChannel\":\"false\"},{\"enableStatus\":\"true\",\"instId\":\"UPIPUSHEXPRESS\",\"instName\":\"Unified Payment Interface - PUSH Express\",\"oneClickChannel\":\"false\",\"payMethod\":\"UPI\",\"payOption\":\"UPI_PUSH_EXPRESS\",\"prepaidCardChannel\":\"false\"}],\"payMethod\":\"UPI\"},{\"payChannelOptionViews\":[{\"enableStatus\":\"true\",\"instId\":\"VISA\",\"instName\":\"Visa Inc.\",\"oneClickChannel\":\"false\",\"payMethod\":\"CREDIT_CARD\",\"payOption\":\"CREDIT_CARD_VISA\",\"prepaidCardChannel\":\"false\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]},{\"enableStatus\":\"true\",\"instId\":\"MASTER\",\"instName\":\"MasterCard Inc.\",\"oneClickChannel\":\"false\",\"payMethod\":\"CREDIT_CARD\",\"payOption\":\"CREDIT_CARD_MASTER\",\"prepaidCardChannel\":\"false\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]},{\"enableStatus\":\"true\",\"instId\":\"MAESTRO\",\"instName\":\"Maestro\",\"oneClickChannel\":\"false\",\"payMethod\":\"CREDIT_CARD\",\"payOption\":\"CREDIT_CARD_MAESTRO\",\"prepaidCardChannel\":\"false\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]},{\"enableStatus\":\"true\",\"instId\":\"RUPAY\",\"instName\":\"RuPay\",\"oneClickChannel\":\"false\",\"payMethod\":\"CREDIT_CARD\",\"payOption\":\"CREDIT_CARD_RUPAY\",\"prepaidCardChannel\":\"false\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]},{\"enableStatus\":\"true\",\"instId\":\"AMEX\",\"instName\":\"American Express\",\"oneClickChannel\":\"false\",\"payMethod\":\"CREDIT_CARD\",\"payOption\":\"CREDIT_CARD_AMEX\",\"prepaidCardChannel\":\"false\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]},{\"enableStatus\":\"true\",\"instId\":\"DINERS\",\"instName\":\"Diners Club International\",\"oneClickChannel\":\"false\",\"payMethod\":\"CREDIT_CARD\",\"payOption\":\"CREDIT_CARD_DINERS\",\"prepaidCardChannel\":\"false\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]},{\"enableStatus\":\"true\",\"instId\":\"DISCOVER\",\"instName\":\"Discover Inc.\",\"oneClickChannel\":\"false\",\"payMethod\":\"CREDIT_CARD\",\"payOption\":\"CREDIT_CARD_DISCOVER\",\"prepaidCardChannel\":\"false\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]}],\"payMethod\":\"CREDIT_CARD\"},{\"payChannelOptionViews\":[{\"enableStatus\":\"true\",\"instId\":\"VISA\",\"instName\":\"Visa Inc.\",\"oneClickChannel\":\"false\",\"payMethod\":\"DEBIT_CARD\",\"payOption\":\"DEBIT_CARD_VISA\",\"prepaidCardChannel\":\"true\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]},{\"enableStatus\":\"true\",\"instId\":\"MASTER\",\"instName\":\"MasterCard Inc.\",\"oneClickChannel\":\"false\",\"payMethod\":\"DEBIT_CARD\",\"payOption\":\"DEBIT_CARD_MASTER\",\"prepaidCardChannel\":\"true\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]},{\"enableStatus\":\"true\",\"instId\":\"MAESTRO\",\"instName\":\"Maestro\",\"oneClickChannel\":\"false\",\"payMethod\":\"DEBIT_CARD\",\"payOption\":\"DEBIT_CARD_MAESTRO\",\"prepaidCardChannel\":\"true\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]},{\"enableStatus\":\"true\",\"instId\":\"RUPAY\",\"instName\":\"RuPay\",\"oneClickChannel\":\"false\",\"payMethod\":\"DEBIT_CARD\",\"payOption\":\"DEBIT_CARD_RUPAY\",\"prepaidCardChannel\":\"true\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]},{\"enableStatus\":\"true\",\"instId\":\"DINERS\",\"instName\":\"Diners Club International\",\"oneClickChannel\":\"false\",\"payMethod\":\"DEBIT_CARD\",\"payOption\":\"DEBIT_CARD_DINERS\",\"prepaidCardChannel\":\"true\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]}],\"payMethod\":\"DEBIT_CARD\"},{\"payChannelOptionViews\":[{\"enableStatus\":\"true\",\"instId\":\"SBI\",\"instName\":\"State Bank of India\",\"oneClickChannel\":\"false\",\"payMethod\":\"NET_BANKING\",\"payOption\":\"NET_BANKING_SBI\",\"prepaidCardChannel\":\"false\"},{\"enableStatus\":\"true\",\"instId\":\"PNB\",\"instName\":\"Punjab National Bank\",\"oneClickChannel\":\"false\",\"payMethod\":\"NET_BANKING\",\"payOption\":\"NET_BANKING_PNB\",\"prepaidCardChannel\":\"false\"},{\"enableStatus\":\"true\",\"instId\":\"RBS\",\"instName\":\"Royal Bank of Scotland\",\"oneClickChannel\":\"false\",\"payMethod\":\"NET_BANKING\",\"payOption\":\"NET_BANKING_RBS\",\"prepaidCardChannel\":\"false\"}],\"payMethod\":\"NET_BANKING\"},{\"payChannelOptionViews\":[{\"enableStatus\":\"true\",\"instId\":\"PPBL\",\"instName\":\"Paytm Payments Bank\",\"oneClickChannel\":\"false\",\"payMethod\":\"PPBL\",\"payOption\":\"PPBL\",\"prepaidCardChannel\":\"false\"}],\"payMethod\":\"PPBL\"}],\"pwpEnabled\":\"false\",\"resultInfo\":{\"resultCode\":\"SUCCESS\",\"resultCodeId\":\"00000000\",\"resultMsg\":\"success\",\"resultStatus\":\"S\"}}},\"signature\":\"90960427c586e0c07e26db75389ecaa1af195ab69c7336a28018a670a723db50\"}";

                }

                Gson gson = new Gson();
                JsonElement json = gson.fromJson(alipayResponse, JsonElement.class);
                String alipayResponseAsJsonString = gson.toJson(json);

                String responseString = null;
                try {
                    responseString = testResource.getObjectMapper().readTree(alipayResponseAsJsonString)
                            .get("response").toString();
                } catch (JsonProcessingException e) {
                    LOGGER.error("{}", e);
                } catch (IOException e) {
                    LOGGER.error("{}", e);
                }
                final T response = TestRequestUtil.mapJsonToObject(testResource.getObjectMapper(), responseString,
                        clazz);
                return response;
            }
        };
        new MockUp<ConnectionUtil>() {
            @Mock
            public <T, U> T execute(final U request, final ServiceUrl api, final Class<T> clazz)
                    throws FacadeCheckedException {
                String alipayResponse = "";
                if (api.equals(AlipayServiceUrl.ACQUIRING_ORDER_CREATEORDER)) {
                    alipayResponse = testResource.getTestProperties().getProperty("createOrder.response");
                }
                if (api.equals(AlipayServiceUrl.PAYMENT_CASHIER_PAYVIEW_CONSULT)) {
                    PayviewConsultRequest payViewRequest = PayviewConsultRequest.class.cast(request);
                    if (StringUtils.isBlank(payViewRequest.getBody().getPayerUserId())) {
                        alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.payment.cashier.payview.consult\",\"clientId\":\"clientId\",\"reqMsgId\":\"0f4d7d2a2ec64b7485201bc5d38caf22administratorthinkpadl450\",\"version\":\"1.1.7\",\"respTime\":\"2017-06-05T13:55:37+05:30\"},\"body\":{\"transDesc\":\"PARCEL892193\",\"transAmount\":{\"currency\":\"INR\",\"value\":\"200\"},\"transId\":\"20170605111212800110166452600008765\",\"resultInfo\":{\"resultMsg\":\"SUCCESS\",\"resultCode\":\"SUCCESS\",\"resultCodeId\":\"00000000\",\"resultStatus\":\"S\"},\"productCode\":\"51051000100000000001\",\"transCreatedTime\":\"2017-06-05T13:54:47+05:30\",\"transType\":\"ACQUIRING\",\"securityId\":\"sid899bbbe9c90a4ad22b451f92c3e566b2\",\"payMethodViews\":[{\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"CREDIT_CARD\",\"payChannelOptionViews\":[{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_VISA\",\"enableStatus\":true,\"instId\":\"VISA\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"Visa Inc.\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_MASTER\",\"enableStatus\":true,\"instId\":\"MASTER\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"MasterCard Inc.\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_MAESTRO\",\"enableStatus\":true,\"instId\":\"MAESTRO\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"Maestro\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_RUPAY\",\"enableStatus\":true,\"instId\":\"RUPAY\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"RuPay\"},{\"payOption\":\"CREDIT_CARD_AMEX\",\"enableStatus\":false,\"instId\":\"AMEX\",\"payMethod\":\"CREDIT_CARD\",\"disableReason\":\"SERVICE_INST_NOT_EXIST\",\"instName\":\"American Express\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_DINERS\",\"enableStatus\":true,\"instId\":\"DINERS\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"Diners Club International\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_DISCOVER\",\"enableStatus\":true,\"instId\":\"DISCOVER\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"Discover Inc.\"}]},{\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"DEBIT_CARD\",\"payChannelOptionViews\":[{\"supportCountries\":[\"IN\"],\"payOption\":\"DEBIT_CARD_VISA\",\"enableStatus\":true,\"instId\":\"VISA\",\"payMethod\":\"DEBIT_CARD\",\"instName\":\"Visa Inc.\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"DEBIT_CARD_MASTER\",\"enableStatus\":true,\"instId\":\"MASTER\",\"payMethod\":\"DEBIT_CARD\",\"instName\":\"MasterCard Inc.\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"DEBIT_CARD_MAESTRO\",\"enableStatus\":true,\"instId\":\"MAESTRO\",\"payMethod\":\"DEBIT_CARD\",\"instName\":\"Maestro\"},{\"payOption\":\"DEBIT_CARD_RUPAY\",\"enableStatus\":false,\"instId\":\"RUPAY\",\"payMethod\":\"DEBIT_CARD\",\"disableReason\":\"SERVICE_INST_NOT_EXIST\",\"instName\":\"RuPay\"},{\"payOption\":\"DEBIT_CARD_DINERS\",\"enableStatus\":false,\"instId\":\"DINERS\",\"payMethod\":\"DEBIT_CARD\",\"disableReason\":\"SERVICE_INST_NOT_EXIST\",\"instName\":\"Diners Club International\"}]},{\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"NET_BANKING\",\"payChannelOptionViews\":[{\"payOption\":\"NET_BANKING_HDFC\",\"enableStatus\":false,\"instId\":\"HDFC\",\"payMethod\":\"NET_BANKING\",\"disableReason\":\"CHANNEL_NOT_AVAILABLE\",\"instName\":\"HDFC Bank (Housing Development Finance Corporation)\"},{\"payOption\":\"NET_BANKING_ICICI\",\"enableStatus\":false,\"instId\":\"ICICI\",\"payMethod\":\"NET_BANKING\",\"disableReason\":\"CHANNEL_NOT_AVAILABLE\",\"instName\":\"ICICI Bank (Industrial Credit and Investment Corporation of India)\"},{\"payOption\":\"NET_BANKING_AXIS\",\"enableStatus\":false,\"instId\":\"AXIS\",\"payMethod\":\"NET_BANKING\",\"disableReason\":\"SERVICE_INST_NOT_EXIST\",\"instName\":\"Axis Bank Limited\"},{\"payOption\":\"NET_BANKING_STB\",\"enableStatus\":true,\"instId\":\"STB\",\"payMethod\":\"NET_BANKING\",\"instName\":\"Saraswat Co-operative Bank Ltd\"}]},{\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"EMI\",\"payChannelOptionViews\":[{\"payOption\":\"EMI_HDFC\",\"enableStatus\":true,\"instId\":\"HDFC\",\"payMethod\":\"EMI\",\"instName\":\"HDFC Bank (Housing Development Finance Corporation)\",\"emiChannelInfos\":[{\"minAmount\":{\"currency\":\"INR\",\"value\":\"100\"},\"maxAmount\":{\"value\":\"10000\",\"currency\":\"INR\"},\"cardAcquiringMode\":\"ONUS\",\"interestRate\":\"15.0\",\"ofMonths\":\"6\"},{\"minAmount\":{\"currency\":\"INR\",\"value\":\"100\"},\"maxAmount\":{\"value\":\"10000\",\"currency\":\"INR\"},\"cardAcquiringMode\":\"ONUS\",\"interestRate\":\"15.0\",\"ofMonths\":\"3\"}]}]},{\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"UPI\",\"payChannelOptionViews\":[{\"payOption\":\"UPI\",\"enableStatus\":true,\"instId\":\"UPI\",\"payMethod\":\"UPI\",\"instName\":\"Unified Payment Interace\"}]}],\"extendInfo\":\"{\\\"website\\\":\\\"retail\\\",\\\"productCode\\\":\\\"51051000100000000001\\\",\\\"theme\\\":\\\"merchant\\\",\\\"ssoToken\\\":\\\"7a43d1c5-2cd2-45b5-a03b-203d3857f3dd\\\",\\\"requestType\\\":\\\"DEFAULT\\\",\\\"phoneNo\\\":\\\"YYYYYYYYYY\\\",\\\"merchantTransId\\\":\\\"PARCEL892193\\\",\\\"topupAndPay\\\":\\\"false\\\",\\\"callBackURL\\\":\\\"https://pg-staging.paytm.in/MerchantSite/bankResponse\\\",\\\"email\\\":\\\"XXX@YYY.ZZZ\\\",\\\"paytmMerchantId\\\":\\\"DataCl59062077159771\\\",\\\"isSupportAddPay\\\":\\\"Y\\\",\\\"alipayMerchantId\\\":\\\"216820000000145754283\\\",\\\"merchantName\\\":\\\"DataClean\\\",\\\"mccCode\\\":\\\"Retail\\\",\\\"totalTxnAmount\\\":\\\"200\\\",\\\"promoCode\\\":\\\"\\\"}\",\"merchantId\":\"216820000000145754283\",\"chargePayer\":false}},\"signature\":\"no_signature\"}";
                    } else {
                        alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.payment.cashier.payview.consult\",\"clientId\":\"clientId\",\"reqMsgId\":\"0f4d7d2a2ec64b7485201bc5d38caf22administratorthinkpadl450\",\"version\":\"1.1.7\",\"respTime\":\"2017-06-05T13:55:37+05:30\"},\"body\":{\"transDesc\":\"PARCEL892193\",\"transAmount\":{\"currency\":\"INR\",\"value\":\"200\"},\"transId\":\"20170605111212800110166452600008765\",\"resultInfo\":{\"resultMsg\":\"SUCCESS\",\"resultCode\":\"SUCCESS\",\"resultCodeId\":\"00000000\",\"resultStatus\":\"S\"},\"productCode\":\"51051000100000000001\",\"transCreatedTime\":\"2017-06-05T13:54:47+05:30\",\"transType\":\"ACQUIRING\",\"securityId\":\"sid899bbbe9c90a4ad22b451f92c3e566b2\",\"payMethodViews\":[{\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"BALANCE\",\"payChannelOptionViews\":[{\"payOption\":\"BALANCE\",\"enableStatus\":true,\"payMethod\":\"BALANCE\",\"balanceChannelInfos\":[{\"payerAccountNo\":\"20070000000006569456\",\"accountBalance\":{\"value\":\"578000\",\"currency\":\"INR\"}}]}]},{\"payChannelOptionViews\":[{\"externalAccountInfos\":[{\"extendInfo\":\"{\\\"lenderId\\\":\\\"ICICI\\\",\\\"lenderDescription\\\":\\\"ICICI Bank Digital Credit\\\",\\\"otpRequired\\\":\\\"false\\\"}\",\"accountBalance\":{\"value\":\"747650\",\"currency\":\"INR\"},\"externalAccountNo\":\"test-pg-account-number\"}],\"payOption\":\"PAYTM_DIGITAL_CREDIT\",\"enableStatus\":true,\"instId\":\"PAYTMCC\",\"instName\":\"Paytm Digital Credit\",\"payMethod\":\"PAYTM_DIGITAL_CREDIT\"}],\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"PAYTM_DIGITAL_CREDIT\"},{\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"CREDIT_CARD\",\"payChannelOptionViews\":[{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_VISA\",\"enableStatus\":true,\"instId\":\"VISA\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"Visa Inc.\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_MASTER\",\"enableStatus\":true,\"instId\":\"MASTER\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"MasterCard Inc.\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_MAESTRO\",\"enableStatus\":true,\"instId\":\"MAESTRO\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"Maestro\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_RUPAY\",\"enableStatus\":true,\"instId\":\"RUPAY\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"RuPay\"},{\"payOption\":\"CREDIT_CARD_AMEX\",\"enableStatus\":false,\"instId\":\"AMEX\",\"payMethod\":\"CREDIT_CARD\",\"disableReason\":\"SERVICE_INST_NOT_EXIST\",\"instName\":\"American Express\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_DINERS\",\"enableStatus\":true,\"instId\":\"DINERS\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"Diners Club International\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"CREDIT_CARD_DISCOVER\",\"enableStatus\":true,\"instId\":\"DISCOVER\",\"payMethod\":\"CREDIT_CARD\",\"instName\":\"Discover Inc.\"}]},{\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"DEBIT_CARD\",\"payChannelOptionViews\":[{\"supportCountries\":[\"IN\"],\"payOption\":\"DEBIT_CARD_VISA\",\"enableStatus\":true,\"instId\":\"VISA\",\"payMethod\":\"DEBIT_CARD\",\"instName\":\"Visa Inc.\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"DEBIT_CARD_MASTER\",\"enableStatus\":true,\"instId\":\"MASTER\",\"payMethod\":\"DEBIT_CARD\",\"instName\":\"MasterCard Inc.\"},{\"supportCountries\":[\"IN\"],\"payOption\":\"DEBIT_CARD_MAESTRO\",\"enableStatus\":true,\"instId\":\"MAESTRO\",\"payMethod\":\"DEBIT_CARD\",\"instName\":\"Maestro\"},{\"payOption\":\"DEBIT_CARD_RUPAY\",\"enableStatus\":false,\"instId\":\"RUPAY\",\"payMethod\":\"DEBIT_CARD\",\"disableReason\":\"SERVICE_INST_NOT_EXIST\",\"instName\":\"RuPay\"},{\"payOption\":\"DEBIT_CARD_DINERS\",\"enableStatus\":false,\"instId\":\"DINERS\",\"payMethod\":\"DEBIT_CARD\",\"disableReason\":\"SERVICE_INST_NOT_EXIST\",\"instName\":\"Diners Club International\"}]},{\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"NET_BANKING\",\"payChannelOptionViews\":[{\"payOption\":\"NET_BANKING_HDFC\",\"enableStatus\":false,\"instId\":\"HDFC\",\"payMethod\":\"NET_BANKING\",\"disableReason\":\"CHANNEL_NOT_AVAILABLE\",\"instName\":\"HDFC Bank (Housing Development Finance Corporation)\"},{\"payOption\":\"NET_BANKING_ICICI\",\"enableStatus\":false,\"instId\":\"ICICI\",\"payMethod\":\"NET_BANKING\",\"disableReason\":\"CHANNEL_NOT_AVAILABLE\",\"instName\":\"ICICI Bank (Industrial Credit and Investment Corporation of India)\"},{\"payOption\":\"NET_BANKING_AXIS\",\"enableStatus\":false,\"instId\":\"AXIS\",\"payMethod\":\"NET_BANKING\",\"disableReason\":\"SERVICE_INST_NOT_EXIST\",\"instName\":\"Axis Bank Limited\"},{\"payOption\":\"NET_BANKING_STB\",\"enableStatus\":true,\"instId\":\"STB\",\"payMethod\":\"NET_BANKING\",\"instName\":\"Saraswat Co-operative Bank Ltd\"}]},{\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"EMI\",\"payChannelOptionViews\":[{\"payOption\":\"EMI_HDFC\",\"enableStatus\":true,\"instId\":\"HDFC\",\"payMethod\":\"EMI\",\"instName\":\"HDFC Bank (Housing Development Finance Corporation)\",\"emiChannelInfos\":[{\"minAmount\":{\"currency\":\"INR\",\"value\":\"100\"},\"maxAmount\":{\"value\":\"10000\",\"currency\":\"INR\"},\"cardAcquiringMode\":\"ONUS\",\"interestRate\":\"15.0\",\"ofMonths\":\"6\"},{\"minAmount\":{\"currency\":\"INR\",\"value\":\"100\"},\"maxAmount\":{\"value\":\"10000\",\"currency\":\"INR\"},\"cardAcquiringMode\":\"ONUS\",\"interestRate\":\"15.0\",\"ofMonths\":\"3\"}]}]},{\"riskResult\":{\"result\":\"ACCEPT\"},\"payMethod\":\"UPI\",\"payChannelOptionViews\":[{\"payOption\":\"UPI\",\"enableStatus\":true,\"instId\":\"UPI\",\"payMethod\":\"UPI\",\"instName\":\"Unified Payment Interace\"}]}],\"extendInfo\":\"{\\\"website\\\":\\\"retail\\\",\\\"productCode\\\":\\\"51051000100000000001\\\",\\\"theme\\\":\\\"merchant\\\",\\\"ssoToken\\\":\\\"7a43d1c5-2cd2-45b5-a03b-203d3857f3dd\\\",\\\"requestType\\\":\\\"DEFAULT\\\",\\\"phoneNo\\\":\\\"YYYYYYYYYY\\\",\\\"merchantTransId\\\":\\\"PARCEL892193\\\",\\\"topupAndPay\\\":\\\"false\\\",\\\"callBackURL\\\":\\\"https://pg-staging.paytm.in/MerchantSite/bankResponse\\\",\\\"email\\\":\\\"XXX@YYY.ZZZ\\\",\\\"paytmMerchantId\\\":\\\"DataCl59062077159771\\\",\\\"isSupportAddPay\\\":\\\"Y\\\",\\\"alipayMerchantId\\\":\\\"216820000000145754283\\\",\\\"merchantName\\\":\\\"DataClean\\\",\\\"mccCode\\\":\\\"Retail\\\",\\\"totalTxnAmount\\\":\\\"200\\\",\\\"promoCode\\\":\\\"\\\"}\",\"merchantId\":\"216820000000145754283\",\"chargePayer\":false}},\"signature\":\"no_signature\"}";
                    }
                }
                if (api.equals(AlipayServiceUrl.QUERY_SUCCESS_RATE)) {
                    alipayResponse = testResource.getTestProperties().getProperty("query.success.rate.response");
                }
                if (api.equals(AlipayServiceUrl.BOSS_CHARGE_FEE_BATCH_CONSULT)) {
                    alipayResponse = testResource.getTestProperties().getProperty(
                            "boss.charge.fee.batch.consult.response");
                }

                if (api.equals(AlipayServiceUrl.ACQUIRING_ORDER_CREATEORDER_AND_PAY)) {
                    String toAppend = System.getProperty("acquiringOrderCreateOrderAndPayCustomBehaviour", "");
                    alipayResponse = testResource.getTestProperties().getProperty(
                            "createOrderAndPay.response" + toAppend);
                }

                if (api.equals(AlipayServiceUrl.USER_ASSET_CACHE_CARD)) {
                    alipayResponse = testResource.getTestProperties().getProperty("user.asset.cacheCard.response");
                }

                if (api.equals(AlipayServiceUrl.PAYMENT_CASHIER_PAYRESULT_QUERY)) {
                    alipayResponse = testResource.getTestProperties().getProperty("payresult.query.response");
                }

                if (api.equals(AlipayServiceUrl.CHANNEL_ACCOUNT_QUERY)) {
                    alipayResponse = testResource.getTestProperties().getProperty("channel.account.query.response");
                }

                if (api.equals(AlipayServiceUrl.PAYMENT_CASHIER_LITEPAYVIEW_CONSULT)) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.payment.cashier.litepayview.consult\",\"clientId\":\"2016030715243903536806\",\"reqMsgId\":\"7216bb15b7e84eb989c62dc6de3c3920pgptheia39paytmlocal\",\"version\":\"fixed-a\",\"respTime\":\"2021-04-12T20:24:52+05:30\"},\"body\":{\"chargePayer\":\"false\",\"payMethodViews\":[{\"payChannelOptionViews\":[{\"enableStatus\":\"true\",\"instId\":\"UPIPUSH\",\"instName\":\"Unified Payment Interface - PUSH\",\"oneClickChannel\":\"false\",\"payMethod\":\"UPI\",\"payOption\":\"UPI_PUSH\",\"prepaidCardChannel\":\"false\"},{\"enableStatus\":\"true\",\"instId\":\"UPI\",\"instName\":\"Unified Payment Interace\",\"oneClickChannel\":\"false\",\"payMethod\":\"UPI\",\"payOption\":\"UPI\",\"prepaidCardChannel\":\"false\"},{\"enableStatus\":\"true\",\"instId\":\"UPIPUSHEXPRESS\",\"instName\":\"Unified Payment Interface - PUSH Express\",\"oneClickChannel\":\"false\",\"payMethod\":\"UPI\",\"payOption\":\"UPI_PUSH_EXPRESS\",\"prepaidCardChannel\":\"false\"}],\"payMethod\":\"UPI\"},{\"payChannelOptionViews\":[{\"enableStatus\":\"true\",\"instId\":\"VISA\",\"instName\":\"Visa Inc.\",\"oneClickChannel\":\"false\",\"payMethod\":\"CREDIT_CARD\",\"payOption\":\"CREDIT_CARD_VISA\",\"prepaidCardChannel\":\"false\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]},{\"enableStatus\":\"true\",\"instId\":\"MASTER\",\"instName\":\"MasterCard Inc.\",\"oneClickChannel\":\"false\",\"payMethod\":\"CREDIT_CARD\",\"payOption\":\"CREDIT_CARD_MASTER\",\"prepaidCardChannel\":\"false\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]},{\"enableStatus\":\"true\",\"instId\":\"MAESTRO\",\"instName\":\"Maestro\",\"oneClickChannel\":\"false\",\"payMethod\":\"CREDIT_CARD\",\"payOption\":\"CREDIT_CARD_MAESTRO\",\"prepaidCardChannel\":\"false\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]},{\"enableStatus\":\"true\",\"instId\":\"RUPAY\",\"instName\":\"RuPay\",\"oneClickChannel\":\"false\",\"payMethod\":\"CREDIT_CARD\",\"payOption\":\"CREDIT_CARD_RUPAY\",\"prepaidCardChannel\":\"false\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]},{\"enableStatus\":\"true\",\"instId\":\"AMEX\",\"instName\":\"American Express\",\"oneClickChannel\":\"false\",\"payMethod\":\"CREDIT_CARD\",\"payOption\":\"CREDIT_CARD_AMEX\",\"prepaidCardChannel\":\"false\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]},{\"enableStatus\":\"true\",\"instId\":\"DINERS\",\"instName\":\"Diners Club International\",\"oneClickChannel\":\"false\",\"payMethod\":\"CREDIT_CARD\",\"payOption\":\"CREDIT_CARD_DINERS\",\"prepaidCardChannel\":\"false\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]},{\"enableStatus\":\"true\",\"instId\":\"DISCOVER\",\"instName\":\"Discover Inc.\",\"oneClickChannel\":\"false\",\"payMethod\":\"CREDIT_CARD\",\"payOption\":\"CREDIT_CARD_DISCOVER\",\"prepaidCardChannel\":\"false\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]}],\"payMethod\":\"CREDIT_CARD\"},{\"payChannelOptionViews\":[{\"enableStatus\":\"true\",\"instId\":\"VISA\",\"instName\":\"Visa Inc.\",\"oneClickChannel\":\"false\",\"payMethod\":\"DEBIT_CARD\",\"payOption\":\"DEBIT_CARD_VISA\",\"prepaidCardChannel\":\"true\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]},{\"enableStatus\":\"true\",\"instId\":\"MASTER\",\"instName\":\"MasterCard Inc.\",\"oneClickChannel\":\"false\",\"payMethod\":\"DEBIT_CARD\",\"payOption\":\"DEBIT_CARD_MASTER\",\"prepaidCardChannel\":\"true\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]},{\"enableStatus\":\"true\",\"instId\":\"MAESTRO\",\"instName\":\"Maestro\",\"oneClickChannel\":\"false\",\"payMethod\":\"DEBIT_CARD\",\"payOption\":\"DEBIT_CARD_MAESTRO\",\"prepaidCardChannel\":\"true\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]},{\"enableStatus\":\"true\",\"instId\":\"RUPAY\",\"instName\":\"RuPay\",\"oneClickChannel\":\"false\",\"payMethod\":\"DEBIT_CARD\",\"payOption\":\"DEBIT_CARD_RUPAY\",\"prepaidCardChannel\":\"true\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]},{\"enableStatus\":\"true\",\"instId\":\"DINERS\",\"instName\":\"Diners Club International\",\"oneClickChannel\":\"false\",\"payMethod\":\"DEBIT_CARD\",\"payOption\":\"DEBIT_CARD_DINERS\",\"prepaidCardChannel\":\"true\",\"supportCountries\":[\"IN\"],\"supportPayOptionSubTypes\":[\"CORPORATE_CARD\"]}],\"payMethod\":\"DEBIT_CARD\"},{\"payChannelOptionViews\":[{\"enableStatus\":\"true\",\"instId\":\"SBI\",\"instName\":\"State Bank of India\",\"oneClickChannel\":\"false\",\"payMethod\":\"NET_BANKING\",\"payOption\":\"NET_BANKING_SBI\",\"prepaidCardChannel\":\"false\"},{\"enableStatus\":\"true\",\"instId\":\"PNB\",\"instName\":\"Punjab National Bank\",\"oneClickChannel\":\"false\",\"payMethod\":\"NET_BANKING\",\"payOption\":\"NET_BANKING_PNB\",\"prepaidCardChannel\":\"false\"},{\"enableStatus\":\"true\",\"instId\":\"RBS\",\"instName\":\"Royal Bank of Scotland\",\"oneClickChannel\":\"false\",\"payMethod\":\"NET_BANKING\",\"payOption\":\"NET_BANKING_RBS\",\"prepaidCardChannel\":\"false\"}],\"payMethod\":\"NET_BANKING\"},{\"payChannelOptionViews\":[{\"enableStatus\":\"true\",\"instId\":\"PPBL\",\"instName\":\"Paytm Payments Bank\",\"oneClickChannel\":\"false\",\"payMethod\":\"PPBL\",\"payOption\":\"PPBL\",\"prepaidCardChannel\":\"false\"}],\"payMethod\":\"PPBL\"}],\"pwpEnabled\":\"false\",\"resultInfo\":{\"resultCode\":\"SUCCESS\",\"resultCodeId\":\"00000000\",\"resultMsg\":\"success\",\"resultStatus\":\"S\"}}},\"signature\":\"90960427c586e0c07e26db75389ecaa1af195ab69c7336a28018a670a723db50\"}";

                }

                Gson gson = new Gson();
                JsonElement json = gson.fromJson(alipayResponse, JsonElement.class);
                String alipayResponseAsJsonString = gson.toJson(json);

                String responseString = null;
                try {
                    responseString = testResource.getObjectMapper().readTree(alipayResponseAsJsonString)
                            .get("response").toString();
                } catch (JsonProcessingException e) {
                    LOGGER.error("{}", e);
                } catch (IOException e) {
                    LOGGER.error("{}", e);
                }
                final T response = TestRequestUtil.mapJsonToObject(testResource.getObjectMapper(), responseString,
                        clazz);
                return response;
            }
        };
    }

    @After
    public void oneTimeTearDown() {
        System.clearProperty("catalina.base");
        System.clearProperty("java.rmi.server.hostname");
    }

    protected int checkTestValueInList(String value, List<String> list) {
        int i = -1;
        for (i = 0; i < list.size(); i++) {
            if (list.get(i).equals(value)) {
                break;
            }
        }
        return i;
    }

    protected void validatePaymentRequest(IPaymentService paymentService, PaymentRequestBean requestData) {
        ValidationResults validationResult = paymentService.validatePaymentRequest(requestData);
        if (ValidationResults.CHECKSUM_VALIDATION_FAILURE.equals(validationResult)) {
            Assert.fail("Checksum failure!");
        }
        return;
    }

    protected void validatePaymentRequest(IJsonResponsePaymentService paymentService, PaymentRequestBean requestData) {
        ValidationResults validationResult = paymentService.validatePaymentRequest(requestData);
        if (ValidationResults.CHECKSUM_VALIDATION_FAILURE.equals(validationResult)) {
            Assert.fail("Checksum failure!");
        }
        return;
    }
}
