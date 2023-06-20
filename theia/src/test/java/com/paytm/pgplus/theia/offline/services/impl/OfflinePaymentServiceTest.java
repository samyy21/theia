package com.paytm.pgplus.theia.offline.services.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.taskengine.taskexecutor.TaskExecutor;
import com.paytm.pgplus.biz.workflow.model.ValidationResultBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.common.enums.MandateMode;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.emisubvention.models.Item;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoResponseData;
import com.paytm.pgplus.models.GoodsInfo;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.models.SimplifiedPaymentOffers;
import com.paytm.pgplus.models.UserInfo;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.request.SubscriptionTransactionRequestBody;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.nativ.utils.NativePersistData;
import com.paytm.pgplus.theia.offline.enums.InstrumentType;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.PaymentRequestProcessingException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.model.request.*;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import org.mockito.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;
import java.util.HashMap;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*;

public class OfflinePaymentServiceTest {

    @InjectMocks
    private OfflinePaymentService offlinePaymentService = new OfflinePaymentService();

    @Mock
    protected ITheiaSessionDataService theiaSessionDataService;

    @Mock
    protected IBizService bizService;

    @Mock
    protected IBizRequestResponseMapper bizRequestResponseMapper;

    @Mock
    private IWorkFlow offlineWorkflow;

    @Mock
    private TaskExecutor taskExecutor;

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckBalanceServiceImpl.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testOfflinePaymentServiceWhenHeadNull() throws RequestValidationException {
        CashierInfoRequest request = new CashierInfoRequest();
        request.setHead(null);
        request.setBody(null);
        exceptionRule.expect(RequestValidationException.class);
        exceptionRule.expectMessage("Request parameters are not valid");
        offlinePaymentService.validateRequestBean(request);
    }

    @Test
    public void testValidateRequestBean() throws PaymentRequestProcessingException {
        CashierInfoRequest cashierInfoRequest = new CashierInfoRequest();
        ValidationResultBean validationResultBean = new ValidationResultBean(true);
        getCashierInfoRequestSetHead(cashierInfoRequest);
        getCashierInfoRequestSetBody(cashierInfoRequest);
        cashierInfoRequest.getHead().setTokenType(TokenType.SSO);
        offlinePaymentService.validateRequestBean(cashierInfoRequest);
    }

    @Test
    public void testProcessPaymentRequest() throws TheiaDataMappingException, PaymentRequestProcessingException {
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        CashierInfoRequest request = new CashierInfoRequest();
        getCashierInfoRequestSetBody(request);
        getCashierInfoRequestSetBody(request);
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = new GenericCoreResponseBean<WorkFlowResponseBean>(
                "msg", ResponseConstants.INVALID_CUST_ID);
        exceptionRule.expect(PaymentRequestProcessingException.class);
        when(taskExecutor.execute(any())).thenReturn(bizResponseBean);
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        offlinePaymentService.processPaymentRequest(paymentRequestBean);

        exceptionRule.expect(PaymentRequestProcessingException.class);
        when(taskExecutor.execute(any())).thenThrow(new PaymentRequestProcessingException());
        when(bizService.processWorkFlow(any(), any())).thenThrow(new PaymentRequestProcessingException());
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenThrow(new TheiaDataMappingException());
        offlinePaymentService.processPaymentRequest(paymentRequestBean);

    }

    @Test
    public void testExceptionCase() throws TheiaDataMappingException, PaymentRequestProcessingException {
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = new GenericCoreResponseBean<WorkFlowResponseBean>(
                new WorkFlowResponseBean());
        when(taskExecutor.execute(any())).thenReturn(bizResponseBean);
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        offlinePaymentService.processPaymentRequest(paymentRequestBean);

        exceptionRule.expect(PaymentRequestProcessingException.class);
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenThrow(new TheiaDataMappingException());
        offlinePaymentService.processPaymentRequest(paymentRequestBean);

    }

    @Test
    public void testExceptionCase1() throws TheiaDataMappingException, PaymentRequestProcessingException {
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        workFlowRequestBean.setPaytmMID("123");
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = new GenericCoreResponseBean<WorkFlowResponseBean>(
                "des", null);
        exceptionRule.expect(PaymentRequestProcessingException.class);
        when(taskExecutor.execute(any())).thenReturn(bizResponseBean);
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        offlinePaymentService.processPaymentRequest(paymentRequestBean);

        exceptionRule.expect(PaymentRequestProcessingException.class);
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenThrow(new TheiaDataMappingException());
        offlinePaymentService.processPaymentRequest(paymentRequestBean);
    }

    private void getCashierInfoRequestSetHead(CashierInfoRequest request) {
        RequestHeader requestHeader = new RequestHeader();
        requestHeader.setToken("1234");
        requestHeader.setTokenType(TokenType.SSO);
        requestHeader.setRequestTimestamp("requestTimeStamp");
        requestHeader.setRequestId("requestId");
        requestHeader.setVersion("version");
        requestHeader.setMid("mid");
        requestHeader.setClientId("clientId");
        request.setHead(requestHeader);
    }

    private void getCashierInfoRequestSetBody(CashierInfoRequest request) {
        CashierInfoRequestBody cashierInfoRequestBody = new CashierInfoRequestBody();
        cashierInfoRequestBody.setAccessToken("accessToken");
        cashierInfoRequestBody.setInstrumentTypes(Collections.singletonList(InstrumentType.CC));
        cashierInfoRequestBody.setSavedInstrumentsTypes(Collections.singletonList(InstrumentType.CC));
        cashierInfoRequestBody.setDisabledInstrumentTypes(Collections.singletonList(InstrumentType.CC));
        cashierInfoRequestBody.setOrderAmount(new Money("value"));
        cashierInfoRequestBody.setOrderId("orderId");
        cashierInfoRequestBody.setCustId("custId");
        cashierInfoRequestBody.setEmiOption("emiOption");
        cashierInfoRequestBody.setSubwalletAmount("amount");
        cashierInfoRequestBody.setPromoCode("code");
        cashierInfoRequestBody.setWebsite("website");
        cashierInfoRequestBody.setRequestType("requestType");
        cashierInfoRequestBody.setGoodsInfo(Collections.singletonList(new GoodsInfo()));
        cashierInfoRequestBody.setTargetPhoneNo("00000");
        cashierInfoRequestBody.setKycCode("code");
        cashierInfoRequestBody.setKycVersion("version");
        cashierInfoRequestBody.setNativePersistData(new NativePersistData(new UserDetailsBiz()));
        cashierInfoRequestBody.setSubscriptionPaymentMode("paymentMode");
        cashierInfoRequestBody.setBankAccountNumbers(Collections.singletonList("1234"));
        cashierInfoRequestBody.setSubscriptionTransactionRequestBody(new SubscriptionTransactionRequestBody());
        cashierInfoRequestBody.setAppVersion("version");
        cashierInfoRequestBody.setMandateType(MandateMode.E_MANDATE);
        cashierInfoRequestBody.setItems(Collections.singletonList(new Item()));
        cashierInfoRequestBody.setEmiSubventedTransactionAmount("amount");
        cashierInfoRequestBody.setEmiSubventionCustomerId("custId");
        cashierInfoRequestBody.setSubventionDetails(new SubventionDetails());
        cashierInfoRequestBody.setOriginChannel("channel");
        cashierInfoRequestBody.setProductCode("code");
        cashierInfoRequestBody.setqRCodeInfo(new QRCodeInfoResponseData());
        cashierInfoRequestBody.setInitialAddMoneyAmount("amount");
        cashierInfoRequestBody.setSimplifiedPaymentOffers(new SimplifiedPaymentOffers());
        cashierInfoRequestBody.setReferenceId("id");
        cashierInfoRequestBody.setUserInfo(new UserInfo());
        cashierInfoRequestBody.setChannelId(EChannelId.APP);
        cashierInfoRequestBody.setDeviceId("id");
        cashierInfoRequestBody.setIndustryTypeId("id");
        cashierInfoRequestBody.setExtendInfo(new HashMap<>());
        cashierInfoRequestBody.setSignature("signature");
        request.setBody(cashierInfoRequestBody);
    }
}