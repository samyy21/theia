package com.paytm.pgplus.theia.offline.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.AutoDebitResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.common.model.link.LinkDetailResponseBody;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.PayOption;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;
import com.paytm.pgplus.theia.datamapper.impl.BizRequestResponseMapperImpl;
import com.paytm.pgplus.theia.nativ.utils.NativeRetryUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.model.request.*;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.utils.LinkPaymentUtil;
import com.paytm.pgplus.theia.utils.MerchantDataUtil;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import mockit.MockUp;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*;

import java.util.*;

public class FastForwardServiceHelperTest {

    @InjectMocks
    FastForwardServiceHelper fastForwardServiceHelper = new FastForwardServiceHelper();

    @Mock
    private IPgpFf4jClient iPgpFf4jClient;

    @Mock
    NativeSessionUtil nativeSessionUtil;

    @Mock
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Mock
    private NativeRetryUtil nativeRetryUtil;

    @Mock
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Mock
    private LinkPaymentUtil linkPaymentUtil;

    @Mock
    private Ff4jUtils ff4jUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(FastForwardServiceHelper.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String RISK_EXTENDED_INFO = "risk_extended_info";
    private static final String ORDER_ADDITIONAL_INFO = "orderAdditionalInfo";
    private static final String ADDITIONAL_INFO_DELIMITER = "|";
    private static final String ADDITIONAL_INFO_KEY_VAL_SEPARATOR = ":";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testpreparePaymentRequestBean() {
        FastForwardRequest request = new FastForwardRequest();
        MerchantDataUtil merchantDataUtil = new MerchantDataUtil();
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        getFastForwardServiceHelperWhenBodyNull(request);
        getFastForwardServiceHelperWhenHeadNull(request);
        paymentRequestBean.setOrderId(request.getBody().getOrderId());
        new MockUp<OfflinePaymentUtils>() {
            @mockit.Mock
            public HttpServletRequest gethttpServletRequest() {
                HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
                when(httpServletRequest.getHeader(any())).thenReturn(RISK_EXTENDED_INFO).thenReturn(RISK_EXTENDED_INFO);
                return httpServletRequest;
            }
        };
        when(ff4jUtils.isFeatureEnabledOnMid(any(), any(), anyBoolean())).thenReturn(true);
        fastForwardServiceHelper.preparePaymentRequestBean(request, merchantDataUtil);

    }

    @Test
    public void testGenerateResponseForExceptionCases() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        FastForwardRequest request = new FastForwardRequest();
        getFastForwardServiceHelperWhenBodyNull(request);
        getFastForwardServiceHelperWhenHeadNull(request);
        LinkDetailResponseBody linkDetailResponseBody = new LinkDetailResponseBody();
        ResultInfo resultInfo = new ResultInfo();
        linkDetailResponseBody.setCustomPaymentSuccessMessage("success");
        linkDetailResponseBody.setCustomPaymentFailureMessage("message");
        linkDetailResponseBody.setRedirectionUrlFailure("redirectionUrlFailure");
        resultInfo.setResultCode(ResponseConstants.SYSTEM_ERROR.getAlipayResultCode());
        resultInfo.setResultMsg(ResponseConstants.SYSTEM_ERROR.getAlipayResultMsg());
        when(linkPaymentUtil.getLinkDetailCachedResponse(paymentRequestBean)).thenReturn(linkDetailResponseBody);
        when(ff4jUtils.isFeatureEnabledOnMid(any(), any(), anyBoolean())).thenReturn(true);
        fastForwardServiceHelper.generateResponseForExceptionCases(request, "errormsg", paymentRequestBean);

    }

    @Test
    public void testgenerateFastForwardResponseData() {
        WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        setworkFlowResponseBeanNotNull(workFlowResponseBean);
        Map<String, Object> map = new HashMap<>();
        map.put(ORDER_ADDITIONAL_INFO, "string");
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        FastForwardRequest request = new FastForwardRequest();
        getFastForwardServiceHelperWhenHeadNull(request);
        getFastForwardServiceHelperWhenBodyNull(request);
        LinkDetailResponseBody linkDetailResponseBody = new LinkDetailResponseBody();
        linkDetailResponseBody.setRedirectionUrlFailure("RedirectionUrlFailure");
        linkDetailResponseBody.setCustomPaymentSuccessMessage("Success");
        linkDetailResponseBody.setCustomPaymentFailureMessage("failure");
        workFlowResponseBean.setWorkFlowRequestBean(new WorkFlowRequestBean());
        workFlowResponseBean.getWorkFlowRequestBean().setPostConvenience(true);
        workFlowResponseBean.getWorkFlowRequestBean().setChargeAmount("amount");
        when(linkPaymentUtil.getLinkDetailCachedResponse(paymentRequestBean)).thenReturn(linkDetailResponseBody);
        when(nativeRetryUtil.isRetryPossible(any())).thenReturn(true);
        when(iPgpFf4jClient.checkWithdefault(any(), any(), anyBoolean())).thenReturn(true);
        fastForwardServiceHelper.generateFastForwardResponseData(paymentRequestBean, workFlowResponseBean, request);
    }

    @Test
    public void testConvertObjectToJson() {
        FastForwardResponse fastForwardResponse = new FastForwardResponse();
        new MockUp<JsonMapper>() {
            @mockit.Mock
            public String mapObjectToJson(FastForwardResponse fastForwardResponse) throws FacadeCheckedException {
                throw new FacadeCheckedException();
            }
        };
        assertNotNull(FastForwardServiceHelper.convertObjectToJson(fastForwardResponse));
    }

    @Test
    public void testIsPaymentRetryInfoEnabled() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        when(iPgpFf4jClient.checkWithdefault(any(), any(), anyBoolean())).thenReturn(true);
        fastForwardServiceHelper.isPaymentRetryInfoEnabled(paymentRequestBean);
    }

    @Test
    public void testCheckAndSetIfScanAndPayFlow() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        paymentRequestBean.setRequestType(ERequestType.SEAMLESS_3D_FORM.getType());
        paymentRequestBean.setScanAndPayFlow(true);
        paymentRequestBean.setMid("mid");
        paymentRequestBean.setOrderId("orderId");
        new MockUp<BizRequestResponseMapperImpl>() {
            @mockit.Mock
            public boolean isQRCodeRequest(PaymentRequestBean requestData) {
                return true;
            }
        };
        fastForwardServiceHelper.checkAndSetIfScanAndPayFlow(paymentRequestBean);
    }

    private void getFastForwardServiceHelperWhenHeadNull(FastForwardRequest requestData) {
        RequestHeader requestHeader = new RequestHeader();
        requestHeader.setClientId("clientId");
        requestHeader.setRequestId("id");
        requestHeader.setMid("mid");
        requestHeader.setVersion("version");
        requestHeader.setTokenType(TokenType.SSO);
        requestHeader.setToken("token");
        requestHeader.setRequestTimestamp("timeStamp");
        requestData.setHead(requestHeader);
    }

    private void getFastForwardServiceHelperWhenBodyNull(FastForwardRequest requestData) {
        FastForwardRequestBody fastForwardRequestBody = new FastForwardRequestBody();
        Map<String, Object> map = new HashMap<>();
        map.put(ORDER_ADDITIONAL_INFO, "string");
        fastForwardRequestBody.setAppIP("appIp");
        fastForwardRequestBody.setAuthMode("authMode");
        fastForwardRequestBody.setChannel("channel");
        fastForwardRequestBody.setCurrency("currency");
        fastForwardRequestBody.setCustomerId("custId");
        fastForwardRequestBody.setDeviceId("deviceId");
        fastForwardRequestBody.setReqType("LINK_BASED_PAYMENT");
        fastForwardRequestBody.setIndustryType("type");
        fastForwardRequestBody.setOrderId("orderId");
        fastForwardRequestBody.setPaymentMode("paymentMode");
        fastForwardRequestBody.setTxnAmount("txnAmount");
        fastForwardRequestBody.setExtendInfo(map);
        fastForwardRequestBody.setSignature("signature");
        fastForwardRequestBody.setChannelId(EChannelId.APP);
        fastForwardRequestBody.setIndustryTypeId("industryTypeID");
        requestData.setBody(fastForwardRequestBody);
    }

    private void setworkFlowResponseBeanNotNull(WorkFlowResponseBean workFlowResponseBean) {
        AutoDebitResponse autoDebitResponse = new AutoDebitResponse();
        autoDebitResponse.setStatus("TXN_SUCCESS");
        Map<String, String> map = new HashMap<>();
        map.put("referenceNo", "referenceNo");
        workFlowResponseBean.setAutoDebitResponse(autoDebitResponse);
        QueryPaymentStatus.QueryPaymentStatusBuilder queryPaymentStatusBuilder = new QueryPaymentStatus.QueryPaymentStatusBuilder(
                "transId", "transTypeDescription", "transAmountCurrencyType", "transAmountValue", "payerUserId",
                "paymentStatusValue");
        queryPaymentStatusBuilder.setPayOptions(Collections.singletonList(new PayOption("payMethodName",
                "payMethodOldName", "currencyType", "AmountValue", map)));
        queryPaymentStatusBuilder.setExtendInfo(map);
        workFlowResponseBean.setQueryPaymentStatus(new QueryPaymentStatus(queryPaymentStatusBuilder));
        workFlowResponseBean.setQueryTransactionStatus(new QueryTransactionStatus(
                new QueryTransactionStatus.QueryTransactionStatusBuilder("AID", "MID", "orderTitle", new HashMap<>())));
    }
}