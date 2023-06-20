package com.paytm.pgplus.theia.nativ.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.common.model.ResultInfo;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.payment.models.ChannelAccount;
import com.paytm.pgplus.facade.payment.models.ChannelAccountView;
import com.paytm.pgplus.facade.payment.models.response.ChannelAccountQueryResponse;
import com.paytm.pgplus.facade.payment.models.response.ChannelAccountQueryResponseBody;
import com.paytm.pgplus.facade.payment.services.ICashier;
import com.paytm.pgplus.facade.postpaid.model.CheckBalanceResponse;
import com.paytm.pgplus.facade.postpaid.model.PaytmDigitalCreditResponse;
import com.paytm.pgplus.facade.ppb.models.FetchAccountBalanceResponse;
import com.paytm.pgplus.facade.ppb.services.IPaymentsBankAccountQuery;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.nativ.enums.PaymentBankAccountStatus;
import com.paytm.pgplus.theia.nativ.model.balanceinfo.BalanceInfoServiceRequest;
import com.paytm.pgplus.theia.nativ.model.balanceinfo.FetchBalanceInfoRequest;
import com.paytm.pgplus.theia.nativ.model.balanceinfo.FetchBalanceInfoRequestBody;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.model.payview.response.*;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import com.paytm.pgplus.theia.utils.ProcessTransactionUtil;
import mockit.MockUp;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * @createdOn 25-Jun-2021
 * @author kalluru nanda kishore
 */
public class BalanceInfoServiceTest {

    @InjectMocks
    private BalanceInfoService balanceInfoService;

    @Mock
    private IPaymentsBankAccountQuery paymentsBankAccountQuery;

    @Mock
    private NativeSessionUtil nativeSessionUtil;

    @Mock
    private ProcessTransactionUtil processTransactionUtil;

    @Mock
    ICashier cashier;

    @Mock
    private IMerchantMappingService merchantMappingService;

    @Mock
    protected WorkFlowHelper workFlowHelper;

    @Mock
    private NativePaymentUtil nativePaymentUtil;

    @Mock
    private IPgpFf4jClient iPgpFf4jClient;

    @Mock
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    private FetchBalanceInfoRequest request = new FetchBalanceInfoRequest();
    private BalanceInfoServiceRequest serviceRequest = new BalanceInfoServiceRequest();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        TokenRequestHeader header = new TokenRequestHeader();
        header.setTokenType(TokenType.JWT);
        request.setHead(header);
        serviceRequest.setTxnToken("txnToken");
        doNothing().when(nativePaymentUtil).fetchPaymentOptions(any(), any(), any());
        UserDetailsBiz userDetailsBiz = new UserDetailsBiz();
        userDetailsBiz.setUserId("user");
        when(nativeSessionUtil.getUserDetails(serviceRequest.getTxnToken())).thenReturn(userDetailsBiz);
        request.setBody(new FetchBalanceInfoRequestBody());
        request.getBody().setMid("MID");

    }

    @Test
    public void testFetchBalanceForEPayPPBLMethod() throws Exception {
        when(nativeSessionUtil.getCashierInfoResponse(any())).thenReturn(new NativeCashierInfoResponse());
        when(nativeSessionUtil.getCashierInfoResponse(any())).thenReturn(null);
        serviceRequest.setPaymentMode(EPayMethod.PPBL.getOldName());
        when(merchantExtendInfoUtils.isMerchantOnPaytm(request.getBody().getMid())).thenReturn(true);
        when(iPgpFf4jClient.checkWithdefault(anyString(), anyObject(), anyBoolean())).thenReturn(true);
        FetchAccountBalanceResponse fetchAccountBalanceResponse = new FetchAccountBalanceResponse();
        fetchAccountBalanceResponse.setAccountState(PaymentBankAccountStatus.DEBIT_FREEZED.name());
        when(paymentsBankAccountQuery.queryAccountDetailsFromBankMiddlewareV4(any())).thenReturn(
                fetchAccountBalanceResponse);
        new MockUp<JsonMapper>() {

            @mockit.Mock
            public void readValue(String fromValue, TypeReference<?> typeReference) {
            }
        };

        balanceInfoService.fetchBalance(request, serviceRequest);
        verify(merchantExtendInfoUtils, times(1)).isMerchantOnPaytm(request.getBody().getMid());
        verify(paymentsBankAccountQuery, times(1)).queryAccountDetailsFromBankMiddlewareV4(any());
    }

    @Test
    public void testFetchBalanceForEpayBALANCEMethod() throws Exception {

        serviceRequest.setPaymentMode(EPayMethod.BALANCE.getMethod());
        when(nativeSessionUtil.getCashierInfoResponse(any())).thenReturn(null);
        request.getHead().setTokenType(TokenType.SSO);
        when(
                nativePaymentUtil.fetchPaymentOptionsWithSsoToken(request.getHead(), request.getBody().getMid(), false,
                        null)).thenReturn(null);
        PayMethod payMethod = new PayMethod();
        BalanceChannel balanceChannel = new BalanceChannel();
        balanceChannel.setBalanceInfo(new AccountInfo());
        balanceChannel.setIsDisabled(new StatusInfo());
        balanceChannel.getIsDisabled().setStatus("false");
        payMethod.setPayChannelOptions(Collections.singletonList(balanceChannel));
        when(processTransactionUtil.getPayMethod(any(), any())).thenReturn(payMethod);
        balanceInfoService.fetchBalance(request, serviceRequest);
    }

    @Test
    public void testFetchBalanceForEpayPAYTM_DIGITAL_CREDIT() throws FacadeCheckedException {
        serviceRequest.setPaymentMode(EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod());
        PaytmDigitalCreditResponse paytmDigitalCreditResponse = new PaytmDigitalCreditResponse();
        CheckBalanceResponse checkBalanceResponse = new CheckBalanceResponse();
        checkBalanceResponse.setMonthlyAvailableSanctionLimit(3000d);
        checkBalanceResponse.setAccountStatus(TheiaConstant.PaytmDigitalCreditConstant.ACCOUNT_STATUS_ACTIVE);
        paytmDigitalCreditResponse.setResponse(Collections.singletonList(checkBalanceResponse));
        doNothing().when(nativeSessionUtil).setPostPaidMPinRequired(anyString(), anyBoolean());
        when(workFlowHelper.getPaytmDigitalCreditBalanceResponse(any(), any())).thenReturn(paytmDigitalCreditResponse);
        when(nativeSessionUtil.getCashierInfoResponse(serviceRequest.getTxnToken())).thenReturn(
                new NativeCashierInfoResponse());
        when(processTransactionUtil.getPayMethod(any(), any())).thenReturn(new PayMethod());
        when(workFlowHelper.getExtendInfoForDigitalCreditBalanceResponse(checkBalanceResponse))
                .thenReturn("extendInfo");
        InitiateTransactionRequestBody initiateTransactionRequestBody = new InitiateTransactionRequestBody();
        initiateTransactionRequestBody.setMid("MID");
        initiateTransactionRequestBody.setTxnAmount(new Money("10000"));
        when(nativeSessionUtil.getOrderDetail(anyString())).thenReturn(initiateTransactionRequestBody);
        balanceInfoService.fetchBalance(request, serviceRequest);

    }

    @Test
    public void testFetchBalanceForEPayADVANCE_DEPOSIT_ACCOUNTMethod() throws FacadeCheckedException {
        HttpServletRequest httpServletRequest = mock(MockHttpServletRequest.class);
        RequestAttributes attributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(attributes, true);
        serviceRequest.setPaymentMode(EPayMethod.ADVANCE_DEPOSIT_ACCOUNT.getMethod());
        when(processTransactionUtil.getPayMethod(any(), any())).thenReturn(new PayMethod());
        UserDetailsBiz userDetailsBiz = new UserDetailsBiz();
        when(nativeSessionUtil.getUserDetails(serviceRequest.getTxnToken())).thenReturn(new UserDetailsBiz());
        request.getHead().setChannelId(EChannelId.APP);
        serviceRequest.setMid("MID");
        when(merchantMappingService.fetchMerchanData(serviceRequest.getMid())).thenReturn(
                new GenericCoreResponseBean<MappingMerchantData>(new MappingMerchantData()));
        when(nativeSessionUtil.getExtendInfo(request.getHead().getTxnToken())).thenReturn(
                Collections.singletonMap("key", "value"));
        new MockUp<JsonMapper>() {

            @mockit.Mock
            public String mapObjectToJson(String extendinfo) {

                return "extendInfo";
            }
        };
        ChannelAccountQueryResponse channelQueryResponse = Mockito.mock(ChannelAccountQueryResponse.class,
                CALLS_REAL_METHODS);
        ChannelAccountQueryResponseBody channelAccountQueryResponseBody = Mockito
                .mock(ChannelAccountQueryResponseBody.class);
        ResultInfo resultInfo = Mockito.mock(ResultInfo.class);
        when(resultInfo.getResultCode()).thenReturn(TheiaConstant.ExtraConstants.DEBIT);
        channelAccountQueryResponseBody.setResultInfo(resultInfo);
        ChannelAccountView channelAccountView = Mockito.mock(ChannelAccountView.class);
        ChannelAccount channelAccount = new ChannelAccount();
        channelAccount.setStatus(false);
        when(channelAccountView.getChannelAccounts()).thenReturn(Collections.singletonList(channelAccount));
        when(channelAccountView.isEnableStatus()).thenReturn(true);
        when(channelAccountQueryResponseBody.getChannelAccountViews()).thenReturn(
                Collections.singletonList(channelAccountView));
        when(channelAccountQueryResponseBody.getChannelAccountViews()).thenReturn(
                Collections.singletonList(new ChannelAccountView()));
        when(channelQueryResponse.getBody()).thenReturn(channelAccountQueryResponseBody);
        when(channelAccountQueryResponseBody.getResultInfo()).thenReturn(resultInfo);
        when(cashier.channelAccountQuery(any())).thenReturn(channelQueryResponse);
        new MockUp<RequestHeaderGenerator>() {

            @mockit.Mock
            public String getAlipayClientId() {

                return "clientId";
            }

            @mockit.Mock
            public String getAlipayClientSecret() {
                return "clientSecret";
            }
        };
        balanceInfoService.fetchBalance(request, serviceRequest);

    }

}