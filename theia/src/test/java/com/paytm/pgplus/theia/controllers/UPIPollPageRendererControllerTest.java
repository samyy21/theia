package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.common.model.MerchantVpaTxnInfo;
import com.paytm.pgplus.request.InitiateTransactionRequest;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.aspects.LocaleFieldAspect;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.models.UPIHandleInfo;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.enhancenative.NativeUpiData;
import com.paytm.pgplus.theia.nativ.model.enhancenative.UPIContent;
import com.paytm.pgplus.theia.nativ.model.enhancenative.UPIPollResponse;
import com.paytm.pgplus.theia.nativ.model.enhancenative.UPIPollResponseBody;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.model.response.ResponseHeader;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.sessiondata.MerchantInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import com.paytm.pgplus.theia.utils.LocalizationUtil;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
import com.paytm.pgplus.theia.utils.UPIPollPageUtil;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @createdOn 07-June-2021
 * @author Siva
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:servlet-context-test.xml" })
@WebAppConfiguration
public class UPIPollPageRendererControllerTest {

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
    UPIPollPageRendererController upiPollPageRendererController;

    @Mock
    private ITheiaViewResolverService theiaViewResolverService;

    @Mock
    private ITheiaSessionDataService theiaSessionDataService;

    @Mock
    private NativeSessionUtil nativeSessionUtil;

    @Mock
    private INativeValidationService nativeValidationService;

    @Mock
    private LocaleFieldAspect localeFieldAspect;

    @Mock
    private LocalizationUtil localizationUtil;

    @Mock
    private UPIPollPageUtil upiPollPageUtil;

    @Mock
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Test
    public void testRenderPollPage() throws ServletException, IOException {

        request.setParameter("cacheKey", "TestCacheKey");
        Map<String, String> dataMap = new HashMap<>();
        dataMap = setparams();
        TransactionInfo txnInfo = new TransactionInfo();
        MerchantInfo merchInfo = new MerchantInfo();
        when(theiaTransactionalRedisUtil.get(any())).thenReturn(dataMap);
        when(theiaSessionDataService.getTxnInfoFromSession(request, true)).thenReturn(txnInfo);
        when(theiaSessionDataService.getMerchantInfoFromSession(request, true)).thenReturn(merchInfo);
        when(theiaViewResolverService.returnUPIPollPage()).thenReturn("TestPage");
        upiPollPageRendererController.renderPollPage(request, response);

    }

    @Test
    public void testRenderPollPageForNulCacheKey() throws ServletException, IOException {
        when(theiaViewResolverService.returnUPIPollPage()).thenReturn("TestPage");
        upiPollPageRendererController.renderPollPage(request, response);

    }

    @Test
    public void testShowUPIPollPage() throws Exception {
        TransactionInfo txnInfo = new TransactionInfo();
        MerchantInfo merchInfo = new MerchantInfo();
        request.setParameter(TheiaConstant.RequestParams.Native.TXN_TOKEN, "TestTXnToken");
        when(nativeSessionUtil.validate(any())).thenReturn(setNativeInitiateRequest());
        when(nativeSessionUtil.getNativeUpiData(any())).thenReturn(setNativeUpiData());
        when(theiaSessionDataService.getTxnInfoFromSession(request, true)).thenReturn(txnInfo);
        when(theiaSessionDataService.getMerchantInfoFromSession(request, true)).thenReturn(merchInfo);
        upiPollPageRendererController.showUPIPollPage(request, response);

    }

    private Map<String, String> setparams() {
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("mid", "testMid");
        dataMap.put("orderId", "testOrderId");
        dataMap.put("acquirementId", "TestAcquirementId");
        dataMap.put("cashierRequestId", "TestCashierRequestId");
        dataMap.put("txnAmount", "testTxnAmount");
        dataMap.put("statusInterval", "TestStatusInterval");
        dataMap.put("statusTimeout", "testStatusTimeout");
        dataMap.put("userVpa", "testUserVpa");
        dataMap.put("isPaytmVPA", "true");
        dataMap.put("merchantVPA", "testMerchantVPA");

        return dataMap;
    }

    private NativeInitiateRequest setNativeInitiateRequest() {
        NativeInitiateRequest nativeInitiateRequest = new NativeInitiateRequest();
        InitiateTransactionRequest initiateTransactionRequest = new InitiateTransactionRequest();
        InitiateTransactionRequestBody initiateTransactionRequestBody = new InitiateTransactionRequestBody();
        initiateTransactionRequestBody.setMid("testMid");
        initiateTransactionRequestBody.setOrderId("testOrderId");
        initiateTransactionRequest.setBody(initiateTransactionRequestBody);
        nativeInitiateRequest.setInitiateTxnReq(initiateTransactionRequest);

        return nativeInitiateRequest;
    }

    private NativeUpiData setNativeUpiData() {
        NativeUpiData nativeUpiData = new NativeUpiData();
        UPIPollResponse upiPollResponse = new UPIPollResponse();
        com.paytm.pgplus.response.ResponseHeader head = new com.paytm.pgplus.response.ResponseHeader();
        head.setRequestId("TestRequestId");
        UPIPollResponseBody upiPollResponseBody = new UPIPollResponseBody();
        MerchantInfo merchantInfo = new MerchantInfo();
        UPIContent upiContent = new UPIContent();
        upiContent.setMid("TestMid");
        upiContent.setOrderId("TestOrderId");
        upiContent.setTxnId("TestTxnId");
        upiContent.setCashierRequestId("testCashierRequestId");
        upiContent.setTxnAmount("TestTxnAmount");
        upiContent.setTimeInterval("testTimeInterval");
        upiContent.setTimeOut("TestTimeOut");
        upiContent.setVpaID("testVpaId");
        upiContent.setSelfPush(true);
        upiContent.setMerchantVPA("TestMerchantVpa");
        MerchantVpaTxnInfo merchantVpaTxnInfo = new MerchantVpaTxnInfo();
        merchantVpaTxnInfo.setMaskedMerchantVpa("testMaskedMerchantVpa");
        upiContent.setMerchantVpaTxnInfo(merchantVpaTxnInfo);
        UPIHandleInfo upiHandleInfo = new UPIHandleInfo("testUpiAppName", "testUpiImageName");
        upiContent.setUpiHandleInfo(upiHandleInfo);
        upiPollResponseBody.setContent(upiContent);
        upiPollResponseBody.setCallbackUrl("testCallbackUrl");
        upiPollResponse.setBody(upiPollResponseBody);
        upiPollResponse.setHead(head);

        merchantInfo.setMid("testMid");
        merchantInfo.setMerchantName("testMerchantName");
        merchantInfo.setMerchantImage("testMerchantImage");
        merchantInfo.setUseNewImagePath(true);

        nativeUpiData.setUpiPollResponse(upiPollResponse);
        nativeUpiData.setMerchantInfo(merchantInfo);
        return nativeUpiData;
    }

}