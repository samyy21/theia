package com.paytm.pgplus.theia.offline.services.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.workflow.model.ValidationResultBean;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.postpaid.IPaytmDigitalCreditCheckBalanceService;
import com.paytm.pgplus.facade.postpaid.model.CheckBalanceResponse;
import com.paytm.pgplus.facade.postpaid.model.PaytmDigitalCreditResponse;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.enums.RetryStatus;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.FetchBalanceInfoException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.model.request.DigitalCreditCheckBalanceRequest;
import com.paytm.pgplus.theia.offline.model.request.DigitalCreditCheckBalanceRequestBody;
import com.paytm.pgplus.theia.offline.model.request.RequestHeader;
import com.paytm.pgplus.theia.offline.utils.AuthUtil;
import org.mockito.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*;
import java.util.Collections;

public class CheckBalanceServiceImplTest {
    @InjectMocks
    private CheckBalanceServiceImpl checkBalanceServiceImpl = new CheckBalanceServiceImpl();

    @Mock
    private IPaytmDigitalCreditCheckBalanceService checkBalanceService;

    @Mock
    private AuthUtil authUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckBalanceServiceImpl.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testCheckBalanceServiceImplWhenHeadNull() throws RequestValidationException {
        DigitalCreditCheckBalanceRequest request = new DigitalCreditCheckBalanceRequest();
        request.setHead(null);
        request.setBody(null);
        exceptionRule.expect(RequestValidationException.class);
        exceptionRule.expectMessage("Request parameters are not valid");
        checkBalanceServiceImpl.checkDigitalCreditBalance(request);
    }

    @Test
    public void testValidateRequestIsSuccessfullyProcessed() throws RequestValidationException {
        DigitalCreditCheckBalanceRequest request = new DigitalCreditCheckBalanceRequest();
        ValidationResultBean validationResultBean = new ValidationResultBean(false);
        exceptionRule.expect(RequestValidationException.class);
        exceptionRule.expectMessage("Request parameters are not valid");
        checkBalanceServiceImpl.checkDigitalCreditBalance(request);
    }

    @Test
    public void testValidateRequestIsSSOToken() throws RequestValidationException {
        DigitalCreditCheckBalanceRequest request = new DigitalCreditCheckBalanceRequest();
        getDigitalCreditCheckBalanceSetHead(request);
        getDigitalCreditCheckBalanceRequestBody(request);
        request.getHead().setTokenType(TokenType.JWT);
        exceptionRule.expect(RequestValidationException.class);
        exceptionRule.expectMessage("Request parameters are not valid");
        checkBalanceServiceImpl.checkDigitalCreditBalance(request);
    }

    @Test
    public void testCheckDigitalCreditBalance() throws FacadeCheckedException, RequestValidationException {
        DigitalCreditCheckBalanceRequest request = new DigitalCreditCheckBalanceRequest();
        getDigitalCreditCheckBalanceSetHead(request);
        getDigitalCreditCheckBalanceRequestBody(request);
        GenericCoreResponseBean<UserDetailsBiz> userDetailsBiz = new GenericCoreResponseBean<UserDetailsBiz>(
                new UserDetailsBiz());
        userDetailsBiz.getResponse().setUserId("1234");
        request.getBody().setAmount("2000");
        when(authUtil.fetchUserDetails(any())).thenReturn(userDetailsBiz);
        PaytmDigitalCreditResponse paytmDigitalCreditResponse = new PaytmDigitalCreditResponse();
        paytmDigitalCreditResponse.setStatusCode(0);
        CheckBalanceResponse checkBalanceResponse = new CheckBalanceResponse();
        checkBalanceResponse.setAmount(5000.0);
        checkBalanceResponse.setAccountStatus(TheiaConstant.PaytmDigitalCreditConstant.ACCOUNT_STATUS_NOT_ACTIVE);
        checkBalanceResponse.setDisplayMessage("DisplayMessage");
        paytmDigitalCreditResponse.setResponse(Collections.singletonList(checkBalanceResponse));
        when(checkBalanceService.checkBalance(any(), any())).thenReturn(paytmDigitalCreditResponse);
        checkBalanceServiceImpl.checkDigitalCreditBalance(request);

        checkBalanceResponse.setAccountStatus(TheiaConstant.PaytmDigitalCreditConstant.ACCOUNT_STATUS_DEACTIVE);
        when(checkBalanceService.checkBalance(any(), any())).thenReturn(paytmDigitalCreditResponse);
        checkBalanceServiceImpl.checkDigitalCreditBalance(request);

        checkBalanceResponse.setAccountStatus(TheiaConstant.PaytmDigitalCreditConstant.ACCOUNT_STATUS_FROZEN);
        when(checkBalanceService.checkBalance(any(), any())).thenReturn(paytmDigitalCreditResponse);
        checkBalanceServiceImpl.checkDigitalCreditBalance(request);

        checkBalanceResponse.setAccountStatus(TheiaConstant.PaytmDigitalCreditConstant.ACCOUNT_STATUS_ON_HOLD);
        when(checkBalanceService.checkBalance(any(), any())).thenReturn(paytmDigitalCreditResponse);
        checkBalanceServiceImpl.checkDigitalCreditBalance(request);

        checkBalanceResponse.setAccountStatus(TheiaConstant.PaytmDigitalCreditConstant.LENDER);
        when(checkBalanceService.checkBalance(any(), any())).thenReturn(paytmDigitalCreditResponse);
        checkBalanceServiceImpl.checkDigitalCreditBalance(request);

        checkBalanceResponse.setAccountStatus(TheiaConstant.PaytmDigitalCreditConstant.ACCOUNT_STATUS_ACTIVE);
        when(checkBalanceService.checkBalance(any(), any())).thenReturn(paytmDigitalCreditResponse);
        checkBalanceServiceImpl.checkDigitalCreditBalance(request);

        paytmDigitalCreditResponse.setStatusCode(1);
        when(checkBalanceService.checkBalance(any(), any())).thenReturn(paytmDigitalCreditResponse);
        checkBalanceServiceImpl.checkDigitalCreditBalance(request);

        exceptionRule.expect(FetchBalanceInfoException.class);
        when(checkBalanceService.checkBalance(any(), any())).thenThrow(new FacadeCheckedException());
        checkBalanceServiceImpl.checkDigitalCreditBalance(request);

    }

    @Test
    public void testWhenUserBinDetailsIsNull() throws FacadeCheckedException, RequestValidationException,
            FetchBalanceInfoException {
        DigitalCreditCheckBalanceRequest request = new DigitalCreditCheckBalanceRequest();
        getDigitalCreditCheckBalanceSetHead(request);
        getDigitalCreditCheckBalanceRequestBody(request);
        GenericCoreResponseBean<UserDetailsBiz> userDetailsBiz1 = new GenericCoreResponseBean<UserDetailsBiz>(null,
                RetryStatus.BANK_FORM_FETCH_FAILED);
        when(authUtil.fetchUserDetails(anyString())).thenReturn(userDetailsBiz1);
        exceptionRule.expect(FetchBalanceInfoException.class);
        checkBalanceServiceImpl.checkDigitalCreditBalance(request);
    }

    private void getDigitalCreditCheckBalanceSetHead(DigitalCreditCheckBalanceRequest request) {
        RequestHeader requestHeader = new RequestHeader();
        requestHeader.setToken("M");
        requestHeader.setMid("mid");
        requestHeader.setClientId("clientId");
        requestHeader.setVersion("1234");
        requestHeader.setRequestId("requestId");
        requestHeader.setTokenType(TokenType.SSO);
        request.setHead(requestHeader);
    }

    private void getDigitalCreditCheckBalanceRequestBody(DigitalCreditCheckBalanceRequest request) {
        DigitalCreditCheckBalanceRequestBody digitalCreditCheckBalanceRequestBody = new DigitalCreditCheckBalanceRequestBody();
        digitalCreditCheckBalanceRequestBody.setAmount("200");
        digitalCreditCheckBalanceRequestBody.setChannelId(EChannelId.APP);
        request.setBody(digitalCreditCheckBalanceRequestBody);
    }
}