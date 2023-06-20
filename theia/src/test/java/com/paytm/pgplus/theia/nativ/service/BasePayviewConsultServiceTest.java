package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.biz.taskengine.taskexecutor.TaskExecutor;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.request.SubscriptionTransactionRequestBody;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.nativ.model.common.CashierInfoContainerRequest;
import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
import com.paytm.pgplus.theia.nativ.utils.CustomBeanMapper;
import com.paytm.pgplus.theia.nativ.utils.PayviewConsultServiceHelper;
import com.paytm.pgplus.theia.offline.enums.InstrumentType;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.model.base.BaseHeader;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequestBody;
import com.paytm.pgplus.theia.offline.model.request.RequestHeader;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.test.testflow.AbstractPaymentServiceTest;
import com.paytm.pgplus.theia.utils.PRNUtils;
import io.vavr.collection.Array;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.ORDER_ID;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;

/**
 * @createdOn 25-Jun-2021
 * @author kalluru nanda kishore
 */
public class BasePayviewConsultServiceTest extends AbstractPaymentServiceTest {

    @InjectMocks
    BasePayviewConsultService basePayviewConsultService;

    @Mock
    protected IBizService bizService;

    @Mock
    protected IBizRequestResponseMapper bizRequestResponseMapper;

    @Mock
    private CustomBeanMapper customBeanMapper;

    @Mock
    private PayviewConsultServiceHelper payviewConsultServiceHelper;

    @Mock
    AOAUtils aoaUtils;

    @Mock
    private IMerchantPreferenceService merchantPreferenceService;

    @Mock
    private PRNUtils prnUtils;

    @Mock
    private TaskExecutor taskExecutor;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private CashierInfoContainerRequest cashierInfoContainerRequest;

    private CashierInfoRequest cashierInfoRequest;

    @Before
    public void setup() {
        basePayviewConsultService = Mockito.mock(BasePayviewConsultService.class, Mockito.CALLS_REAL_METHODS);
        MockitoAnnotations.initMocks(this);
        cashierInfoRequest = new CashierInfoRequest(new RequestHeader(new BaseHeader("M", "M", "M", "M")),
                new CashierInfoRequestBody());
        cashierInfoRequest.getBody().setChannelId(EChannelId.APP);
        cashierInfoRequest.getHead().setToken("test");
        cashierInfoRequest.getHead().setTokenType(TokenType.TXN_TOKEN);
        cashierInfoContainerRequest = new CashierInfoContainerRequest(cashierInfoRequest);
    }

    @Test
    public void testValidate() throws TheiaDataMappingException {
        // fail();
        basePayviewConsultService.validate(cashierInfoContainerRequest);
        cashierInfoContainerRequest.getCashierInfoRequest().setBody(null);
        exceptionRule.expect(RequestValidationException.class);
        exceptionRule.expectMessage(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg());
        basePayviewConsultService.validate(cashierInfoContainerRequest);

    }

    @Test
    public void testProcess() throws TheiaDataMappingException {

        cashierInfoRequest.getHead().setMid("NONE");
        WorkFlowRequestBean workFlowRequestBean = Mockito.spy(new WorkFlowRequestBean());
        workFlowRequestBean.setEnhancedCashierPageRequest(false);
        workFlowRequestBean.setRequestType(ERequestType.NATIVE);
        Mockito.when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        Mockito.when(prnUtils.checkIfPRNEnabled("MID")).thenReturn(true);
        Mockito.when(merchantPreferenceService.isDynamicFeeMerchant(anyString())).thenReturn(true);
        Mockito.when(merchantPreferenceService.isSlabBasedMDREnabled(anyString())).thenReturn(false);
        Mockito.when(merchantPreferenceService.isDynamicQR2FAEnabledWithPCF(anyString())).thenReturn(false);
        Mockito.when(taskExecutor.execute(workFlowRequestBean)).thenReturn(
                new GenericCoreResponseBean<WorkFlowResponseBean>(new WorkFlowResponseBean()));
        Mockito.when(bizService.processWorkFlow(any(), any())).thenReturn(
                new GenericCoreResponseBean<WorkFlowResponseBean>(new WorkFlowResponseBean()));
        WorkFlowResponseBean response = basePayviewConsultService.process(cashierInfoContainerRequest);
        Mockito.verify(bizRequestResponseMapper, times(1)).mapWorkFlowRequestData(any());
        Mockito.verify(merchantPreferenceService, times(1)).isDynamicFeeMerchant(anyString());
        Mockito.verify(merchantPreferenceService, times(1)).isSlabBasedMDREnabled(anyString());
        Mockito.verify(merchantPreferenceService, times(1)).isDynamicQR2FAEnabledWithPCF(anyString());
        Mockito.verify(bizService, times(0)).processWorkFlow(any(), any());
        assertNull(workFlowRequestBean.getEmiSubventionCustomerId());
        assertFalse(workFlowRequestBean.isEmiSubventionRequired());
        assertFalse(workFlowRequestBean.isInternalFetchPaymentOptions());
    }

    @Test
    public void testTestProcess() throws TheiaDataMappingException {

        cashierInfoRequest.getHead().setMid("NONE");
        WorkFlowRequestBean workFlowRequestBean = Mockito.spy(new WorkFlowRequestBean());
        workFlowRequestBean.setEnhancedCashierPageRequest(true);
        workFlowRequestBean.setRequestType(ERequestType.NATIVE);
        workFlowRequestBean.setNativeAddMoney(true);
        workFlowRequestBean.setDynamicFeeMerchant(true);
        Mockito.when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        Mockito.when(prnUtils.checkIfPRNEnabled("MID")).thenReturn(true);
        Mockito.when(merchantPreferenceService.isDynamicFeeMerchant(anyString())).thenReturn(true);
        Mockito.when(merchantPreferenceService.isSlabBasedMDREnabled(anyString())).thenReturn(false);
        Mockito.when(merchantPreferenceService.isDynamicQR2FAEnabledWithPCF(anyString())).thenReturn(true);
        Mockito.when(taskExecutor.execute(workFlowRequestBean)).thenReturn(
                new GenericCoreResponseBean<WorkFlowResponseBean>(new WorkFlowResponseBean()));
        Mockito.when(bizService.processWorkFlow(any(), any())).thenReturn(
                new GenericCoreResponseBean<WorkFlowResponseBean>(new WorkFlowResponseBean()));
        cashierInfoRequest.getBody().setSubscriptionTransactionRequestBody(new SubscriptionTransactionRequestBody());
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        paymentRequestBean.setEnhancedCashierPageRequest(true);
        WorkFlowResponseBean response = basePayviewConsultService.process(cashierInfoRequest, paymentRequestBean);

    }

    @Test
    public void testRequestTransform() {

        Mockito.when(merchantPreferenceService.isPwpEnabled("MID")).thenReturn(false);
        cashierInfoRequest.getBody().setRequestType(ERequestType.NATIVE_PAY.getType());
        cashierInfoRequest.getBody().setSubwalletAmount("100");
        cashierInfoRequest.getBody().setNativeAddMoney(true);
        cashierInfoRequest.getHead().setMid("MID");
        cashierInfoRequest.getBody().setAmountForWalletConsultInRisk("100");
        cashierInfoRequest.getBody().setCustId("Customer1");
        cashierInfoRequest.getBody().setMlvSupported(true);
        cashierInfoRequest.getBody().setGoodsInfo(new ArrayList<>());
        cashierInfoRequest.getBody().setSubscriptionTransactionRequestBody(new SubscriptionTransactionRequestBody());
        cashierInfoRequest.getBody().setDisabledInstrumentTypes(Collections.singletonList(InstrumentType.WALLET));
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        paymentRequestBean.setEnhancedCashierPageRequest(true);
        cashierInfoContainerRequest.setPaymentRequestBean(paymentRequestBean);
        PaymentRequestBean result = basePayviewConsultService.requestTransform(cashierInfoContainerRequest);
        assertFalse(result.isSessionRequired());
        assertFalse(result.isPreAuth());
        assertNull(result.getWorkflow());
        assertNull(result.getItems());
        assertNull(result.getEmiSubventedTransactionAmount());
        assertTrue(result.isNativeAddMoney());
        assertEquals(ERequestType.NATIVE_PAY.getType(), result.getRequestType());
        // assertEquals();

    }

    @Test
    public void testMakeBackwardCompatibleHttpServletRequest() {

        HttpServletRequest httpServletRequest = new MockHttpServletRequest();
        cashierInfoContainerRequest.getCashierInfoRequest().getHead().setMid("MID");
        CashierInfoRequestBody cashierInfoRequestBody = cashierInfoContainerRequest.getCashierInfoRequest().getBody();
        cashierInfoRequestBody.setIndustryTypeId("Industry");
        cashierInfoRequestBody.setOrderId("order1");
        cashierInfoRequestBody.setDeviceId("device");
        cashierInfoRequestBody.setOrderAmount(new Money());
        cashierInfoRequestBody.setCustId("customer1");
        cashierInfoRequestBody.setEmiOption("emi");
        cashierInfoRequestBody.setSubscriptionPaymentMode("cc");
        basePayviewConsultService.makeBackwardCompatibleHttpServletRequest(httpServletRequest,
                cashierInfoContainerRequest.getCashierInfoRequest());
        assertEquals(cashierInfoRequestBody.getIndustryTypeId(), httpServletRequest.getAttribute(INDUSTRY_TYPE_ID));
        assertEquals(cashierInfoContainerRequest.getCashierInfoRequest().getHead().getMid(),
                httpServletRequest.getAttribute(MID));
        assertEquals(cashierInfoRequestBody.getOrderId(),
                httpServletRequest.getAttribute(TheiaConstant.RequestParams.ORDER_ID));
        assertEquals(cashierInfoRequestBody.getDeviceId(), httpServletRequest.getAttribute(DEVICE_ID));
        assertEquals(cashierInfoRequestBody.getChannelId().toString(), httpServletRequest.getAttribute(CHANNEL_ID));

    }

}