package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.taskengine.taskexecutor.TaskExecutor;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.cache.model.MerchantInfo;
import com.paytm.pgplus.common.enums.FrequencyUnit;
import com.paytm.pgplus.common.enums.SubsPaymentMode;
import com.paytm.pgplus.enums.TxnType;
import com.paytm.pgplus.facade.emisubvention.models.Item;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.models.*;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.request.InitiateTransactionRequest;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.request.SubscriptionTransactionRequestBody;
import com.paytm.pgplus.theia.accesstoken.model.request.CreateAccessTokenServiceRequest;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.cache.impl.MerchantMappingServiceImpl;
import com.paytm.pgplus.theia.cache.model.MerchantPreference;
import com.paytm.pgplus.theia.cache.model.MerchantPreferenceStore;
import com.paytm.pgplus.theia.emiSubvention.helper.SubventionEmiServiceHelper;
import com.paytm.pgplus.theia.helper.PreRedisCacheHelper;
import com.paytm.pgplus.theia.helper.PreRedisCacheHelperTest;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.processor.impl.NativeKYCDetailRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.impl.NativePayviewConsultRequestProcessor;
import com.paytm.pgplus.theia.nativ.service.BasePayviewConsultService;
import com.paytm.pgplus.theia.nativ.service.UserLoggedInLitePayviewConsultService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeValidationService;
import com.paytm.pgplus.theia.redis.impl.TheiaSessionRedisUtil;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import mockit.MockUp;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.MerchantPreference.PreferenceKeys.SUBSCRIPTION_WALLET_LIMIT_ENABLED;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class NativeKYCControllerTest extends AbstractNativeControllerTest {

    @Autowired
    @InjectMocks
    NativeKYCController nativeKYCController;

    @Autowired
    @InjectMocks
    NativeKYCDetailRequestProcessor requestProcessor;

    @Autowired
    @InjectMocks
    NativeSessionUtil nativeSessionUtil;

    @Autowired
    @InjectMocks
    NativePayviewConsultRequestProcessor nativePayviewConsultRequestProcessor;

    @Autowired
    @InjectMocks
    IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @InjectMocks
    MerchantMappingServiceImpl merchantMappingService;

    @Autowired
    @InjectMocks
    UserLoggedInLitePayviewConsultService basePayviewConsultService;

    @Mock
    TaskExecutor taskExecutor;

    @Mock
    ITheiaViewResolverService theiaViewResolverService;

    @Mock
    IMerchantDataService merchantDataService;

    @Mock
    PreRedisCacheHelper preRedisCacheHelper;

    @Mock
    NativeValidationService nativeValidationService;

    @Mock
    TheiaSessionRedisUtil theiaSessionRedisUtil;

    @Mock
    SubventionEmiServiceHelper subventionEmiServiceHelper;

    @Before
    public void setup() {
        super.setUp();
        MockitoAnnotations.initMocks(this);
    }

    @BeforeClass
    public static void setSystemProperty() {
        System.setProperty("catalina.base", "");
    }

    @Test
    public void validateKYC() throws Exception {

        String nativeKYCDetailRequest = "{\"head\":{\"ssoToken\":\"ssoToken\",\"txnToken\":\"txnToken\",\"tokenType\":\"JWT\",\"token\":\"token\",\"requestTimestamp\":\"\",\"workFlow\":\"\",\"version\":\"v2\",\"channelId\":\"APP\",\"requestId\":\"request\"},\"body\":{\"kycNameOnDoc\":\"fake_data\",\"kycDocCode\":\"fake_data\",\"kycDocValue\":\"fake_data\"}}";

        NativeInitiateRequest nativeInitiateRequest = new NativeInitiateRequest();
        InitiateTransactionRequest initiateTransactionRequest = new InitiateTransactionRequest();
        SubscriptionTransactionRequestBody orderDetail = getInitiateTransactionRequestBody(nativeInitiateRequest,
                initiateTransactionRequest);

        when(theiaSessionRedisUtil.hget("txnToken", "orderDetail")).thenReturn(nativeInitiateRequest);
        when(nativeValidationService.validateTxnToken("txnToken")).thenReturn(orderDetail);
        doNothing().when(nativeValidationService).validateMidOrderId("mid", "orderId");
        when(theiaSessionRedisUtil.hget("txnToken", "cashierInfo")).thenReturn(null).thenReturn(
                new NativeCashierInfoResponse());
        CreateAccessTokenServiceRequest accessTokenData = new CreateAccessTokenServiceRequest();
        accessTokenData.setUserInfo(new UserInfo());
        accessTokenData.getUserInfo().setCustId("custId");
        accessTokenData.setPaytmSsoToken("ssoToken");
        when(nativeValidationService.validateAccessToken(any())).thenReturn(accessTokenData);
        when(theiaSessionRedisUtil.hget("ssoToken", "ssoToken")).thenReturn("ssoToken");
        when(nativeValidationService.validateSSOToken(any(), any())).thenReturn(new UserDetailsBiz());
        when(subventionEmiServiceHelper.prepareItemListForAmountBasedSubvention(orderDetail)).thenReturn(
                Collections.singletonList(new Item()));
        MerchantPreferenceStore merchantPreferenceStore = new MerchantPreferenceStore();
        merchantPreferenceStore.setMerchantId("mid");
        MerchantPreference merchantPreference = new MerchantPreference();
        merchantPreference.setEnabled(true);
        merchantPreferenceStore.setPreferences(Collections.singletonMap(SUBSCRIPTION_WALLET_LIMIT_ENABLED,
                merchantPreference));
        when(preRedisCacheHelper.getMerchantPreferenceStore("mid")).thenReturn(merchantPreferenceStore);
        when(merchantDataService.getMerchantMappingData("mid")).thenReturn(
                new MerchantInfo("paytmId", "alipayId", "officlaName"));
        WorkFlowRequestBean requestBean = new WorkFlowRequestBean();
        requestBean.setPaytmMID("mid");
        WorkFlowResponseBean responseBean = new WorkFlowResponseBean();
        responseBean.setWorkFlowRequestBean(requestBean);
        responseBean.setMerchnatLiteViewResponse(new LitePayviewConsultResponseBizBean());
        when(taskExecutor.execute(any())).thenReturn(new GenericCoreResponseBean<WorkFlowResponseBean>(responseBean));
        new MockUp<ConfigurationUtil>() {

            @mockit.Mock
            public String getProperty(String key) {
                return "mid";
            }

        };
        assertNotNull(mvc
                .perform(
                        MockMvcRequestBuilders.post("/api/v1/submitKYC").accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON).content(nativeKYCDetailRequest))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn());

    }

    @NotNull
    private SubscriptionTransactionRequestBody getInitiateTransactionRequestBody(
            NativeInitiateRequest nativeInitiateRequest, InitiateTransactionRequest initiateTransactionRequest) {
        nativeInitiateRequest.setInitiateTxnReq(initiateTransactionRequest);
        initiateTransactionRequest.setBody(new SubscriptionTransactionRequestBody());
        initiateTransactionRequest.getBody().setMid("mid");
        initiateTransactionRequest.getBody().setOrderId("orderId");
        initiateTransactionRequest.getBody().setTxnAmount(new Money());
        initiateTransactionRequest.getBody().setUserInfo(new UserInfo());
        initiateTransactionRequest.getBody().setPromoCode("promo");
        initiateTransactionRequest.getBody().setNeedAppIntentEndpoint(true);
        SubscriptionTransactionRequestBody orderDetail = (SubscriptionTransactionRequestBody) initiateTransactionRequest
                .getBody();
        orderDetail.setAddMoneyFeeAppliedOnWallet(true);
        orderDetail.setRiskFeeDetails(new RiskFeeDetails());
        orderDetail.getRiskFeeDetails().setInitialAmount(new Money());
        orderDetail.setPaytmSsoToken("ssoToken");
        orderDetail.setEmiOption("emiOption");
        orderDetail.getUserInfo().setCustId("custId");
        orderDetail.setExtendInfo(new ExtendInfo());
        orderDetail.setPromoCode("promo");
        orderDetail.setRequestType("SUBSCRIBE");
        orderDetail.setSimplifiedPaymentOffers(new SimplifiedPaymentOffers());
        SimplifiedSubvention subvention = new SimplifiedSubvention();
        subvention.setSelectPlanOnCashierPage(true);
        orderDetail.setSimplifiedSubvention(subvention);
        orderDetail.setMandateType("E_MANDATE");
        orderDetail.setSubscriptionMaxAmount("200000");
        orderDetail.setSubscriptionFrequencyUnit(FrequencyUnit.FORTNIGHT.getName());
        orderDetail.setSubscriptionPaymentMode(SubsPaymentMode.BANK_MANDATE.name());
        orderDetail.setFlexiSubscription(false);
        orderDetail.setTxnAmount(new Money("4"));
        orderDetail.setTxnType(TxnType.AUTH);

        return orderDetail;
    }

    @Test
    public void renderNativeKycPage() throws ServletException, IOException {

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(KYC_TXN_ID)).thenReturn(KYC_TXN_ID);
        when(request.getParameter(KYC_FLOW)).thenReturn(KYC_FLOW);
        when(request.getParameter(KYC_MID)).thenReturn(KYC_MID).thenReturn(KYC_MID);
        when(request.getParameter(ORDER_ID)).thenReturn(ORDER_ID).thenReturn(ORDER_ID);
        doNothing().when(request).setAttribute(any(), any());
        doNothing().when(request).setAttribute(any(), any());
        doNothing().when(request).setAttribute(any(), any());
        doNothing().when(request).setAttribute(any(), any());
        doNothing().when(request).setAttribute(any(), any());
        RequestDispatcher requestDispatcher = mock(RequestDispatcher.class);
        doNothing().when(requestDispatcher).forward(any(), any());
        when(request.getRequestDispatcher(any())).thenReturn(requestDispatcher);
        when(theiaViewResolverService.returnNativeKycPage()).thenReturn("kyc");
        nativeKYCController.renderNativeKycPage(request, null);
    }
}