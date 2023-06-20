package com.paytm.pgplus.theia.services.helper;

import com.paytm.pgplus.biz.core.order.service.IOrderService;
import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.cashier.looper.model.CashierFundOrderStatus;
import com.paytm.pgplus.cashier.looper.model.CashierPaymentStatus;
import com.paytm.pgplus.cashier.looper.model.CashierTransactionStatus;
import com.paytm.pgplus.cashier.models.DoPaymentResponse;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.models.ModifiableHttpServletRequest;
import com.paytm.pgplus.theia.nativ.model.payment.request.NativePaymentRequest;
import com.paytm.pgplus.theia.nativ.model.payment.request.NativePaymentRequestBody;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import mockit.MockUp;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RetryServiceHelperTest {

    @InjectMocks
    private RetryServiceHelper retryServiceHelper = new RetryServiceHelper();

    @Mock
    private IOrderService orderServiceImpl;

    @Mock
    private IMerchantMappingService merchantMappingService;

    @Mock
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Mock
    private NativeSessionUtil nativeSessionUtil;

    @Mock
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryServiceHelper.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testGetRequestDataFromCache() {
        retryServiceHelper.getRequestDataFromCache("transId");
    }

    @Test
    public void testSetRequestDataInCache() {
        PaymentRequestBean requestData = new PaymentRequestBean();
        retryServiceHelper.setRequestDataInCache("transId", requestData);
    }

    @Test
    public void testCheckForRequestDataInCache() {
        retryServiceHelper.checkForRequestDataInCache("");
        when(theiaTransactionalRedisUtil.get(any())).thenReturn(new PaymentRequestBean());
        retryServiceHelper.checkForRequestDataInCache("transId");
        when(theiaTransactionalRedisUtil.get(any())).thenReturn(null);
        retryServiceHelper.checkForRequestDataInCache("transId");
    }

    @Test
    public void testCheckForRetryCount() {
        GenericCoreResponseBean<DoPaymentResponse> cashierResponse1 = new GenericCoreResponseBean<DoPaymentResponse>(
                new DoPaymentResponse());
        retryServiceHelper.checkForRetryCount("merchantId", cashierResponse1);

        Map<String, String> map = new HashMap<>();
        map.put(TheiaConstant.ExtendedInfoPay.MERCHANT_TRANS_ID, "attribute");
        map.put(TheiaConstant.ExtendedInfoPay.RETRY_COUNT, "1");
        DoPaymentResponse doPaymentResponse = new DoPaymentResponse();
        QueryPaymentStatus.QueryPaymentStatusBuilder queryPaymentStatusBuilder = new QueryPaymentStatus.QueryPaymentStatusBuilder(
                "transID", "description", "currencyType", "value", "userId", "value");
        queryPaymentStatusBuilder.setExtendInfo(map);
        QueryPaymentStatus queryPaymentStatus = new QueryPaymentStatus(queryPaymentStatusBuilder);
        doPaymentResponse.setPaymentStatus(new CashierPaymentStatus(queryPaymentStatus));
        doPaymentResponse.setTransactionStatus(new CashierTransactionStatus(
                new CashierTransactionStatus.CashierTransactionStatusBuilder("acquirementId", "merchantTransId",
                        "orderTitle", new HashMap<>())));
        GenericCoreResponseBean<DoPaymentResponse> cashierResponse = new GenericCoreResponseBean<DoPaymentResponse>(
                doPaymentResponse);
        when(merchantExtendInfoUtils.getNumberOfRetries(any())).thenReturn(2);
        retryServiceHelper.checkForRetryCount("merchantId", cashierResponse);

        when(merchantExtendInfoUtils.getNumberOfRetries(any())).thenReturn(2);
        retryServiceHelper.checkForRetryCount("merchantId", cashierResponse);

        when(nativeSessionUtil.getScanAndPayFlag(any())).thenReturn(true);
        retryServiceHelper.checkForRetryCount("merchantId", cashierResponse);

        when(nativeSessionUtil.getScanAndPayFlag(any())).thenReturn(true);
        when(nativeSessionUtil.getKey(any())).thenReturn("txnToken");
        new MockUp<ConfigurationUtil>() {
            @mockit.Mock
            public String getProperty(String key, String defaultValue) {
                return "true";
            }

            @mockit.Mock
            public String getProperty(String key) {
                return "2";
            }
        };
        retryServiceHelper.checkForRetryCount("merchantId", cashierResponse);

        when(nativeSessionUtil.getScanAndPayFlag(any())).thenReturn(true);
        when(nativeSessionUtil.getKey(any())).thenReturn("txnToken");
        new MockUp<ConfigurationUtil>() {
            @mockit.Mock
            public String getProperty(String key, String defaultValue) {
                return "false";
            }
        };
        retryServiceHelper.checkForRetryCount("merchantId", cashierResponse);

        Map<String, String> mp = new HashMap<>();
        DoPaymentResponse doPaymentResponse1 = new DoPaymentResponse();
        QueryPaymentStatus.QueryPaymentStatusBuilder queryPaymentStatusBuilder1 = new QueryPaymentStatus.QueryPaymentStatusBuilder(
                "transID", "description", "currencyType", "value", "userId", "value");
        mp.put(TheiaConstant.ExtendedInfoPay.RETRY_COUNT, null);
        queryPaymentStatusBuilder1.setExtendInfo(mp);
        CashierFundOrderStatus cashierFundOrderStatus = mock(CashierFundOrderStatus.class);
        when(cashierFundOrderStatus.getRequestId()).thenReturn("requestId");
        doPaymentResponse1.setFundOrderStatus(cashierFundOrderStatus);
        QueryPaymentStatus queryPaymentStatus1 = new QueryPaymentStatus(queryPaymentStatusBuilder1);
        doPaymentResponse1.setPaymentStatus(new CashierPaymentStatus(queryPaymentStatus1));
        GenericCoreResponseBean<DoPaymentResponse> cashierResponse2 = new GenericCoreResponseBean<DoPaymentResponse>(
                doPaymentResponse1);
        retryServiceHelper.checkForRetryCount("merchantId", cashierResponse2);

        Map<String, String> mp1 = new HashMap<>();
        DoPaymentResponse doPaymentResponse2 = new DoPaymentResponse();
        QueryPaymentStatus.QueryPaymentStatusBuilder queryPaymentStatusBuilder2 = new QueryPaymentStatus.QueryPaymentStatusBuilder(
                "transID", "description", "currencyType", "value", "userId", "value");
        mp1.put(TheiaConstant.ExtendedInfoPay.RETRY_COUNT, "3");
        queryPaymentStatusBuilder2.setExtendInfo(mp1);
        QueryPaymentStatus queryPaymentStatus2 = new QueryPaymentStatus(queryPaymentStatusBuilder2);
        doPaymentResponse2.setPaymentStatus(new CashierPaymentStatus(queryPaymentStatus2));
        GenericCoreResponseBean<DoPaymentResponse> cashierResponse3 = new GenericCoreResponseBean<DoPaymentResponse>(
                doPaymentResponse2);
        when(merchantExtendInfoUtils.getNumberOfRetries(any())).thenReturn(2);
        retryServiceHelper.checkForRetryCount("merchantId", cashierResponse3);
    }

    @Test
    public void testCheckPaymentRetry() {
        GenericCoreResponseBean<DoPaymentResponse> cashierResponse = new GenericCoreResponseBean<DoPaymentResponse>(
                new DoPaymentResponse());
        EnvInfoRequestBean envInfo = new EnvInfoRequestBean();
        retryServiceHelper.checkPaymentRetry("merchantId", "orderId", cashierResponse, "transId", true, envInfo);

        Map<String, String> map = new HashMap<>();
        map.put(TheiaConstant.ExtraConstants.DUMMY_MERCHANT_ID, "dummyId");
        DoPaymentResponse doPaymentResponse = new DoPaymentResponse();
        QueryPaymentStatus.QueryPaymentStatusBuilder queryPaymentStatusBuilder = new QueryPaymentStatus.QueryPaymentStatusBuilder(
                "transID", "description", "currencyType", "value", "userId", "value");
        queryPaymentStatusBuilder.setExtendInfo(map);
        QueryPaymentStatus queryPaymentStatus = new QueryPaymentStatus(queryPaymentStatusBuilder);
        doPaymentResponse.setPaymentStatus(new CashierPaymentStatus(queryPaymentStatus));
        doPaymentResponse.setTransactionStatus(new CashierTransactionStatus(
                new CashierTransactionStatus.CashierTransactionStatusBuilder("acquirementId", "merchantTransId",
                        "orderTitle", map)));
        GenericCoreResponseBean<DoPaymentResponse> cashierResponse1 = new GenericCoreResponseBean<DoPaymentResponse>(
                doPaymentResponse);
        when(merchantMappingService.getMappingMerchantData(any())).thenReturn(new MappingMerchantData());
        retryServiceHelper.checkPaymentRetry("merchantId", "orderId", cashierResponse1, "transId", false, envInfo);

        CashierFundOrderStatus cashierFundOrderStatus = mock(CashierFundOrderStatus.class);
        doPaymentResponse.setFundOrderStatus(cashierFundOrderStatus);
        retryServiceHelper.checkPaymentRetry("merchantId", "orderId", cashierResponse1, "transId", true, envInfo);

        when(merchantMappingService.getMappingMerchantData(any())).thenReturn(null);
        retryServiceHelper.checkPaymentRetry("merchantId", "orderId", cashierResponse1, "transId", false, envInfo);
    }

    @Test
    public void testCheckNativePaymentRetry() {
        retryServiceHelper.checkNativePaymentRetry("merchantId", "orderId");

        when(nativeSessionUtil.getKey(any())).thenReturn("txnToken");
        when(nativeSessionUtil.getRetryPaymentCount(any())).thenReturn(null);
        retryServiceHelper.checkNativePaymentRetry("merchantId", "orderId");

        when(nativeSessionUtil.getKey(any())).thenReturn("txnToken");
        when(nativeSessionUtil.getRetryPaymentCount(any())).thenReturn(1);
        when(merchantExtendInfoUtils.getNumberOfRetries(any())).thenReturn(3);
        when(nativeSessionUtil.getScanAndPayFlag(any())).thenReturn(true);
        new MockUp<ConfigurationUtil>() {
            @mockit.Mock
            public String getProperty(String key, String defaultValue) {
                return "true";
            }

            @mockit.Mock
            public String getProperty(String key) {
                return "2";
            }
        };
        retryServiceHelper.checkNativePaymentRetry("merchantId", "orderId");

        when(nativeSessionUtil.getKey(any())).thenReturn("txnToken");
        when(nativeSessionUtil.getRetryPaymentCount(any())).thenReturn(3);
        when(merchantExtendInfoUtils.getNumberOfRetries(any())).thenReturn(1);
        when(nativeSessionUtil.getScanAndPayFlag(any())).thenReturn(false);
        retryServiceHelper.checkNativePaymentRetry("merchantId", "orderId");
    }

    @Test
    public void testGetNativePaymentRequestBodyByRequest() throws Exception {
        HttpServletRequest httpServletRequest = mock(ModifiableHttpServletRequest.class);
        PaymentRequestBean requestData = new PaymentRequestBean();
        requestData.setRequest(httpServletRequest);
        ServletInputStream servletInputStream = new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
        };
        ServletRequest servletRequest = mock(ServletRequest.class);
        when(servletRequest.getInputStream()).thenReturn(servletInputStream);
        when(((ModifiableHttpServletRequest) requestData.getRequest()).getRequest()).thenReturn(servletRequest);
        retryServiceHelper.getNativePaymentRequestBodyByRequest(requestData);

        new MockUp<IOUtils>() {
            @mockit.Mock
            public String toString(InputStream input, String encoding) {
                return "{\"name\":\"xyz\"}";
            }
        };
        new MockUp<JsonMapper>() {
            @mockit.Mock
            public NativePaymentRequest mapJsonToObject() {
                NativePaymentRequest nativePaymentRequest = new NativePaymentRequest();
                nativePaymentRequest.setBody(new NativePaymentRequestBody());
                return nativePaymentRequest;
            }
        };
        retryServiceHelper.getNativePaymentRequestBodyByRequest(requestData);
    }

    @Test
    public void testSetNativeCheckOutJsPaymentsDataForRetry() throws Exception {
        PaymentRequestBean requestData = new PaymentRequestBean();
        NativePaymentRequestBody nativePaymentRequestBody = new NativePaymentRequestBody();
        retryServiceHelper.setNativeCheckOutJsPaymentsDataForRetry(requestData, nativePaymentRequestBody);

        nativePaymentRequestBody.setCardInfo("12|2049");
        retryServiceHelper.setNativeCheckOutJsPaymentsDataForRetry(requestData, nativePaymentRequestBody);

        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        paymentRequestBean.setTxnToken("txnToken");
        NativePaymentRequestBody nativePaymentRequestBody1 = new NativePaymentRequestBody();
        nativePaymentRequestBody1.setCardInfo("1|11");
        retryServiceHelper.setNativeCheckOutJsPaymentsDataForRetry(paymentRequestBean, nativePaymentRequestBody1);
    }

    @Test
    public void testCheckNativePaymentRetryByTxnToken() {
        retryServiceHelper.checkNativePaymentRetryByTxnToken("merchantId", "");

        when(nativeSessionUtil.getKey(any())).thenReturn("txnToken");
        when(nativeSessionUtil.getRetryPaymentCount(any())).thenReturn(null);
        retryServiceHelper.checkNativePaymentRetryByTxnToken("merchantId", "txnToken");

        when(nativeSessionUtil.getKey(any())).thenReturn("txnToken");
        when(nativeSessionUtil.getRetryPaymentCount(any())).thenReturn(1);
        when(merchantExtendInfoUtils.getNumberOfRetries(any())).thenReturn(3);
        when(nativeSessionUtil.getScanAndPayFlag(any())).thenReturn(true);
        new MockUp<ConfigurationUtil>() {
            @mockit.Mock
            public String getProperty(String key, String defaultValue) {
                return "true";
            }

            @mockit.Mock
            public String getProperty(String key) {
                return "2";
            }
        };
        retryServiceHelper.checkNativePaymentRetryByTxnToken("merchantId", "txnToken");

        when(nativeSessionUtil.getKey(any())).thenReturn("txnToken");
        when(nativeSessionUtil.getRetryPaymentCount(any())).thenReturn(3);
        when(merchantExtendInfoUtils.getNumberOfRetries(any())).thenReturn(1);
        when(nativeSessionUtil.getScanAndPayFlag(any())).thenReturn(false);
        retryServiceHelper.checkNativePaymentRetryByTxnToken("merchantId", "txnToken");
    }

}