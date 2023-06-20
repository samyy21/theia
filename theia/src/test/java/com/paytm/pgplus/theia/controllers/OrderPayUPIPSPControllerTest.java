package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.core.wallet.service.IWalletQRCodeService;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.UPIPSPResponseBody;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.signature.JWTWithHmacSHA256;
import com.paytm.pgplus.common.signature.wrapper.SignatureUtilWrapper;
import com.paytm.pgplus.facade.payment.models.FeeRateFactors;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoBaseResponse;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.facade.utils.JsonMapper;

import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.models.UPIPSPHeader;
import com.paytm.pgplus.theia.models.UPIPSPRequest;
import com.paytm.pgplus.theia.models.UPIPSPResponse;
import com.paytm.pgplus.theia.models.UPIPSPResponseHeader;
import com.paytm.pgplus.theia.nativ.utils.NativeRetryUtil;
import com.paytm.pgplus.theia.utils.MerchantDataUtil;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
import mockit.MockUp;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
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
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * @createdOn 28/06/21
 * @author Aman Shevkar.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:servlet-context-test.xml" })
@WebAppConfiguration
public class OrderPayUPIPSPControllerTest {

    @Mock
    IBizRequestResponseMapper bizRequestResponseMapper;

    @Mock
    IWorkFlow bizWorkFlow;

    @Mock
    MerchantDataUtil merchantDataUtil;

    @Mock
    MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Mock
    IWalletQRCodeService walletQRCodeService;

    @Mock
    IMerchantPreferenceService merchantPreferenceService;

    @Mock
    NativeRetryUtil nativeRetryUtil;

    @Mock
    UPIPSPRequest upipspRequest;

    @Mock
    Ff4jUtils ff4jUtils;
    @InjectMocks
    OrderPayUPIPSPController orderPayUPIPSPController;

    @Autowired
    WebApplicationContext wac;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Autowired
    private TransactionCacheUtils transactionCacheUtils;
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
    public void testBeanValidation_isValid() throws Exception {
        String payrequest = "{\"body\":{\"requestType\":\"SEAMLESS_3D_FORM\",\"txnAmount\":\"100\",\"payerVpa\":\"TestPayerVpa\",\"mid\":\"TestMid\",\"orderId\":\"OrderId@123456789\",\"mobileNo\":\"9157679845\"},\"header\":{\"requestTimestamp\":\"TestRequestTimestamp\",\"version\":\"TestVersion\",\"requestMsgId\":\"MsgId@123456789\",\"signature\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.T9!&S7V&53A$PAYTM\" }}";
        upipspRequest = JsonMapper.mapJsonToObject(payrequest, UPIPSPRequest.class);
        boolean result = orderPayUPIPSPController.beanValidation(upipspRequest);
        Assert.assertEquals(true, result);
    }

    @Test
    public void testBeanValidation_isNotValid() throws Exception {
        // To test functionality when the attributes are violets regex or null
        String payrequest = "{\"body\":{\"requestType\":\"SEAMLESS_3D_FORM\",\"txnAmount\":\"TxnAmount\",\"payerVpa\":\"TestPayerVpa\",\"mid\":\"TestMid\",\"orderId\":\"OrderId\",\"mobileNo\":\"9157679845\"},\"header\":{\"requestTimestamp\":\"TestRequestTimestamp\",\"version\":\"TestVersion\",\"requestMsgId\":\"MsgId\",\"signature\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.T9!&S7V&53A$PAYTM\" }}";
        upipspRequest = JsonMapper.mapJsonToObject(payrequest, UPIPSPRequest.class);
        boolean result = orderPayUPIPSPController.beanValidation(upipspRequest);
        Assert.assertEquals(false, result);
    }

    @Test
    public void testBeanValidation_notSeamless() throws Exception {
        // To test functionality when the RequestType is not Seamless_3D
        String payrequest = "{\"body\":{\"requestType\":\"RequestType\",\"txnAmount\":\"100\",\"payerVpa\":\"TestPayerVpa\",\"mid\":\"TestMid\",\"orderId\":\"OrderId@123456789\",\"mobileNo\":\"9157679845\"},\"header\":{\"requestTimestamp\":\"TestRequestTimestamp\",\"version\":\"TestVersion\",\"requestMsgId\":\"MsgId@123456789\",\"signature\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.T9!&S7V&53A$PAYTM\" }}";
        upipspRequest = JsonMapper.mapJsonToObject(payrequest, UPIPSPRequest.class);
        boolean result = orderPayUPIPSPController.beanValidation(upipspRequest);
        Assert.assertEquals(false, result);
    }

    @Test
    public void testOrderPayUPIPSP_RegexInvalid() throws Exception {
        String payrequest = "{\"body\":{\"requestType\":\"FAUX_SEAMLESS_3D_FORM\",\"txnAmount\":\"TestAmount\",\"payerVpa\":\"TestPayerVpa\",\"mid\":\"TestMid\",\"orderId\":\"\",\"mobileNo\":\"9157679845\"},\"header\":{\"requestTimestamp\":\"TestRequestTimestamp\",\"version\":\"TestVersion\",\"requestMsgId\":\"MsgId@123456789\",\"signature\":\"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyZXF1ZXN0VHlwZSI6IlNFQU1MRVNTXzNEX0ZPUk0iLCJwYXllclBTUCI6Ikdvb2dsZSBQYXkiLCJwYXllZVZwYSI6InBheXRtcXIyODEwMDUwNTAxMDE4emUza3VldjRwNjZAcGF5dG0iLCJpc3MiOiJ0cyIsIm1pZCI6ImJFQ1REWjY2MTI3MjI3MTAwMDYxIiwicGF5ZXJOYW1lIjoiSFJJVEhJSyBSQUpFU0ggS1VNQVIgU0lOR0giLCJwYXllclZwYSI6ImhyaXRoaWszMTAzOTlAb2toZGZjYmFuayIsInR4bkFtb3VudCI6IjY4LjAwIn0.or2qHKp3dQ34Y06PO4jIH96oU81T8D9eq9FEcNR3aBg\" }}";
        ResponseEntity<UPIPSPResponse> result = orderPayUPIPSPController.orderPayUPIPSP(payrequest, request);
        Assert.assertEquals(HttpStatus.OK.value(), result.getStatusCode().value());
    }

    @Test
    public void testOrderPayUPIPSP_ChecksumInvalid() throws Exception {
        String payrequest = "{\"body\":{\"requestType\":\"SEAMLESS_3D_FORM\",\"txnAmount\":\"100\",\"payerVpa\":\"TestPayerVpa\",\"mid\":\"TestMid\",\"orderId\":\"\",\"mobileNo\":\"9157679845\"},\"header\":{\"requestTimestamp\":\"TestRequestTimestamp\",\"version\":\"TestVersion\",\"requestMsgId\":\"MsgId@123456789\",\"signature\":\"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyZXF1ZXN0VHlwZSI6IlNFQU1MRVNTXzNEX0ZPUk0iLCJwYXllclBTUCI6Ikdvb2dsZSBQYXkiLCJwYXllZVZwYSI6InBheXRtcXIyODEwMDUwNTAxMDE4emUza3VldjRwNjZAcGF5dG0iLCJpc3MiOiJ0cyIsIm1pZCI6ImJFQ1REWjY2MTI3MjI3MTAwMDYxIiwicGF5ZXJOYW1lIjoiSFJJVEhJSyBSQUpFU0ggS1VNQVIgU0lOR0giLCJwYXllclZwYSI6ImhyaXRoaWszMTAzOTlAb2toZGZjYmFuayIsInR4bkFtb3VudCI6IjY4LjAwIn0.or2qHKp3dQ34Y06PO4jIH96oU81T8D9eq9FEcNR3aBg\" }}";
        new MockUp<JWTWithHmacSHA256>() {
            @mockit.Mock
            public boolean verifyJsonWebToken(Map<String, String> jwtClaims, String jwtToken) {
                return false;
            }
        };
        ResponseEntity<UPIPSPResponse> result = orderPayUPIPSPController.orderPayUPIPSP(payrequest, request);
        Assert.assertEquals(HttpStatus.OK.value(), result.getStatusCode().value());
    }

    @Test
    public void testOrderPayUPIPSP_IsOnus() throws Exception {
        String payrequest = "{\"body\":{\"requestType\":\"SEAMLESS_3D_FORM\",\"txnAmount\":\"100\",\"payerVpa\":\"TestPayerVpa\",\"mid\":\"TestMid\",\"orderId\":\"\",\"mobileNo\":\"9157679845\",\"custID\":\"CustomerID@123\",\"payerName\":\"TestPayerName\",\"payerPSP\":\"TestPayerPSP\",\"payeeVpa\":\"12qr@paytm\",\"upiOrderTimeOutInSeconds\":\"200\",\"extendInfo\":{\"additionalInfo\":\"Pune\"}},\"header\":{\"requestTimestamp\":\"TestRequestTimestamp\",\"version\":\"TestVersion\",\"requestMsgId\":\"MsgId@123456789\",\"signature\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.T9!&S7V&53A$PAYTM\" }}";
        new MockUp<JWTWithHmacSHA256>() {
            @mockit.Mock
            public boolean verifyJsonWebToken(Map<String, String> jwtClaims, String jwtToken) {
                return true;
            }
        };
        when(merchantExtendInfoUtils.isMerchantOnPaytm("TestMid")).thenReturn(true);
        ResponseEntity<UPIPSPResponse> result = orderPayUPIPSPController.orderPayUPIPSP(payrequest, request);
        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatusCode().value());
    }

    @Test
    public void testOrderPayUPIPSP_GenerateAggregatorMid() throws Exception {
        String payrequest = "{\"body\":{\"requestType\":\"SEAMLESS_3D_FORM\",\"txnAmount\":\"100\",\"payerVpa\":\"TestPayerVpa\",\"mid\":\"TestMid\",\"orderId\":\"\",\"mobileNo\":\"9157679845\",\"custID\":\"CustomerID@123\",\"payerName\":\"TestPayerName\",\"payerPSP\":\"TestPayerPSP\",\"payeeVpa\":\"12qr@paytm\",\"upiOrderTimeOutInSeconds\":\"GarbageValue\",\"extendInfo\":{\"additionalInfo\":\"Pune\"}},\"header\":{\"requestTimestamp\":\"TestRequestTimestamp\",\"version\":\"TestVersion\",\"requestMsgId\":\"MsgId@123456789\",\"signature\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.T9!&S7V&53A$PAYTM\" }}";
        new MockUp<JWTWithHmacSHA256>() {
            @mockit.Mock
            public boolean verifyJsonWebToken(Map<String, String> jwtClaims, String jwtToken) {
                return true;
            }
        };
        when(nativeRetryUtil.isRetryPossible(any())).thenReturn(false);
        when(merchantExtendInfoUtils.isMerchantOnPaytm("TestMid")).thenReturn(false);
        when(merchantDataUtil.getAggregatorMid("TestMid")).thenReturn("getAggregatorMidResponse");
        ResponseEntity<UPIPSPResponse> result = orderPayUPIPSPController.orderPayUPIPSP(payrequest, request);
        Assert.assertEquals(HttpStatus.OK.value(), result.getStatusCode().value());
    }

    @Test
    public void testOrderPayUPIPSP_CheckForRetry_NativeCountBreach() throws Exception {
        when(merchantDataUtil.getAggregatorMid("TestMid")).thenReturn("getAggregatorMidResponse");
        when(merchantExtendInfoUtils.isMerchantOnPaytm("TestMid")).thenReturn(true);
        when(merchantPreferenceService.isPostConvenienceFeesEnabled("PaytmMid@12345678")).thenReturn(false);
        when(merchantPreferenceService.isSlabBasedMDREnabled("PaytmMid@12345678")).thenReturn(false);
        when(nativeRetryUtil.isRetryPossible(any())).thenReturn(false);
        when(nativeRetryUtil.increaseRetryCount("TextToken", "TestMid", "TestOrderId")).thenReturn(Integer.valueOf(0));
        String payrequest = "{\"body\":{\"requestType\":\"SEAMLESS_3D_FORM\",\"txnAmount\":\"100\",\"payerVpa\":\"TestPayerVpa\",\"mid\":\"TestMid\",\"orderId\":\"OrderId@123456789\",\"mobileNo\":\"9157679845\",\"custID\":\"CustomerID@123\",\"payerName\":\"TestPayerName\",\"payerPSP\":\"TestPayerPSP\",\"payeeVpa\":\"12qr@paytm\",\"upiOrderTimeOutInSeconds\":\"avdbg\",\"extendInfo\":{\"additionalInfo\":\"Pune\"}},\"header\":{\"requestTimestamp\":\"TestRequestTimestamp\",\"version\":\"TestVersion\",\"requestMsgId\":\"MsgId@123456789\",\"signature\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.T9!&S7V&53A$PAYTM\" }}";
        new MockUp<JWTWithHmacSHA256>() {
            @mockit.Mock
            public boolean verifyJsonWebToken(Map<String, String> jwtClaims, String jwtToken) {
                return true;
            }
        };
        ExtendedInfoRequestBean extendedInfoRequestBean = new ExtendedInfoRequestBean();
        extendedInfoRequestBean.setCustomerName("TestName");
        extendedInfoRequestBean.setMerchantContactNo("9845763456");
        extendedInfoRequestBean.setUdf1("PostId@123");
        extendedInfoRequestBean.setPosId("PostId@123");
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        workFlowRequestBean.setRequestType(ERequestType.SEAMLESS_3D_FORM);
        workFlowRequestBean.setStaticQrUpiPayment(true);
        workFlowRequestBean.setMobileNo("9157679845");
        workFlowRequestBean.setOrderID("OrderId@123456789");
        workFlowRequestBean.setPaytmMID("PaytmMid@12345678");
        workFlowRequestBean.setAlipayMID("AlipayMiD@12345678");
        workFlowRequestBean.setTxnAmount("100");
        workFlowRequestBean.setPaymentDetails("TestPayerVpa");
        workFlowRequestBean.setVirtualPaymentAddress("TestPayerVpa");
        workFlowRequestBean.setPaymentTypeId("UPI_QR_CODE");
        workFlowRequestBean.setExtendInfo(extendedInfoRequestBean);
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any(PaymentRequestBean.class))).thenReturn(
                workFlowRequestBean);
        when(walletQRCodeService.getQRCodeInfoByQrCodeId("12QR@PAYTM")).thenReturn(
                new QRCodeInfoBaseResponse("SUCCESS"));
        when(ff4jUtils.isFeatureEnabled(TheiaConstant.DynamicQRRetryConstant.THEIA_DYNAMIC_QR_RETRY_ENABLED, false))
                .thenReturn(true);
        ResponseEntity<UPIPSPResponse> result = orderPayUPIPSPController.orderPayUPIPSP(payrequest, request);
        Assert.assertEquals(HttpStatus.OK.value(), result.getStatusCode().value());
    }

    @Test
    public void testOrderPayUPIPSP_OverAllCheck() throws Exception {
        // To Test functionality when all attributes are valid and
        when(merchantDataUtil.getAggregatorMid("TestMid")).thenReturn("getAggregatorMidResponse");
        when(merchantExtendInfoUtils.isMerchantOnPaytm("TestMid")).thenReturn(true);
        when(merchantPreferenceService.isPostConvenienceFeesEnabled("PaytmMid@12345678")).thenReturn(true);
        when(merchantPreferenceService.isSlabBasedMDREnabled("PaytmMid@12345678")).thenReturn(true);
        when(nativeRetryUtil.isRetryPossible(any())).thenReturn(true);
        when(nativeRetryUtil.increaseRetryCount("TextToken", "TestMid", "TestOrderId")).thenReturn(Integer.valueOf(0));
        String payrequest = "{\"body\":{\"requestType\":\"SEAMLESS_3D_FORM\",\"txnAmount\":\"100\",\"payerVpa\":\"TestPayerVpa\",\"mid\":\"TestMid\",\"orderId\":\"OrderId@123456789\",\"mobileNo\":\"9157679845\",\"custID\":\"CustomerID@123\",\"payerName\":\"TestPayerName\",\"payerPSP\":\"TestPayerPSP\",\"payeeVpa\":\"12qr@paytm\",\"upiOrderTimeOutInSeconds\":\"200\",\"extendInfo\":{\"additionalInfo\":\"Pune\"}},\"header\":{\"requestTimestamp\":\"TestRequestTimestamp\",\"version\":\"TestVersion\",\"requestMsgId\":\"MsgId@123456789\",\"signature\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.T9!&S7V&53A$PAYTM\" }}";
        new MockUp<JWTWithHmacSHA256>() {
            @mockit.Mock
            public boolean verifyJsonWebToken(Map<String, String> jwtClaims, String jwtToken) {
                return true;
            }
        };
        ExtendedInfoRequestBean extendedInfoRequestBean = new ExtendedInfoRequestBean();
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        workFlowRequestBean.setRequestType(ERequestType.SEAMLESS_3D_FORM);
        workFlowRequestBean.setMobileNo("9157679845");
        workFlowRequestBean.setOrderID("OrderId@123456789");
        workFlowRequestBean.setPaytmMID("PaytmMid@12345678");
        workFlowRequestBean.setAlipayMID("AlipayMiD@12345678");
        workFlowRequestBean.setTxnAmount("100");
        workFlowRequestBean.setPaymentDetails("TestPayerVpa");
        workFlowRequestBean.setVirtualPaymentAddress("TestPayerVpa");
        workFlowRequestBean.setPaymentTypeId("UPI_QR_CODE");
        workFlowRequestBean.setExtendInfo(extendedInfoRequestBean);
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any(PaymentRequestBean.class))).thenReturn(
                workFlowRequestBean);
        when(walletQRCodeService.getQRCodeInfoByQrCodeId("12QR@PAYTM")).thenReturn(
                new QRCodeInfoBaseResponse("SUCCESS"));
        when(ff4jUtils.isFeatureEnabled(TheiaConstant.DynamicQRRetryConstant.THEIA_DYNAMIC_QR_RETRY_ENABLED, false))
                .thenReturn(true);
        UPIPSPResponseHeader upipspResponseHeader = new UPIPSPResponseHeader();
        upipspResponseHeader.setResponseTimestamp(Long.MIN_VALUE);
        upipspResponseHeader.setVersion("v1");
        UPIPSPResponseBody upipspResponseBody = new UPIPSPResponseBody();
        upipspResponseBody.setOrderId("OrderId@123456789");
        upipspResponseBody.setResultCodeId("001");
        upipspResponseBody.setResultMsg("success");
        upipspResponseBody.setResultCode("SUCCESS");
        upipspResponseBody.setExternalSerialNo("Xefkl443CFW5rv33!thb=ASD");
        upipspResponseBody.setTxnAmount("100");
        upipspResponseBody.setMid("PaytmMid@12345678");
        upipspResponseBody.setRequestMsgId("MsgId@123456789");
        UPIPSPResponse upipspResponse = new UPIPSPResponse();
        upipspResponse.setBody(upipspResponseBody);
        upipspResponse.setHead(upipspResponseHeader);
        WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        workFlowResponseBean.setUpiPSPResponse(upipspResponseBody);
        workFlowResponseBean.setWorkFlowRequestBean(workFlowRequestBean);
        final GenericCoreResponseBean<WorkFlowResponseBean> genericCoreResponseBean = new GenericCoreResponseBean<WorkFlowResponseBean>(
                workFlowResponseBean);
        when(bizWorkFlow.process(any(WorkFlowRequestBean.class))).thenReturn(genericCoreResponseBean);
        ResponseEntity<UPIPSPResponse> result = orderPayUPIPSPController.orderPayUPIPSP(payrequest, request);
        String ExpectedResult = "UPIPSPResponse [resultCode=SUCCESS, resultCodeId=001, resultMsg=success, externalSerialNo=Xefkl443CFW5rv33!thb=ASD, orderId=OrderId@123456789, requestMsgId=MsgId@123456789, txnAmount=100, mid=PaytmMid@12345678]";
        Assert.assertEquals(HttpStatus.OK.value(), result.getStatusCode().value());
        Assert.assertEquals(ExpectedResult, result.getBody().getBody().toString());
    }

}