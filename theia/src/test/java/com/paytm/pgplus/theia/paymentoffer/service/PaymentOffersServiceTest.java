package com.paytm.pgplus.theia.paymentoffer.service;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.enums.PayMethod;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.ApplyPromoServiceResponseV2;
import com.paytm.pgplus.mappingserviceclient.service.IUserMapping;
import com.paytm.pgplus.theia.accesstoken.model.request.CreateAccessTokenServiceRequest;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.paymentoffer.helper.PaymentOffersServiceHelper;
import com.paytm.pgplus.theia.paymentoffer.helper.PaymentOffersServiceHelperV2;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import com.paytm.pgplus.theia.paymentoffer.model.request.*;
import com.paytm.pgplus.theia.paymentoffer.model.response.ApplyItemLevelPromoResponse;
import mockit.MockUp;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.Collections;
import java.util.HashMap;

public class PaymentOffersServiceTest {

    @InjectMocks
    private PaymentOffersService paymentOffersService = new PaymentOffersService();

    @Mock
    private PaymentOffersServiceHelper paymentOffersServiceHelper;

    @Mock
    private TokenValidationHelper tokenValidationHelper;

    @Mock
    @Qualifier("userMappingService")
    IUserMapping userMapping;

    @Mock
    private NativeSessionUtil nativeSessionUtil;

    @Mock
    private AccessTokenUtils accessTokenUtils;

    @Mock
    private PaymentOffersServiceHelperV2 paymentOffersServiceHelperV2;

    @Mock
    FF4JUtil ff4JUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentOffersService.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        new MockUp<OfflinePaymentUtils>() {
            @mockit.Mock
            public HttpServletRequest gethttpServletRequest() {
                HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
                when(httpServletRequest.getParameter(any())).thenReturn(TheiaConstant.RequestParams.Native.MID)
                        .thenReturn(TheiaConstant.RequestParams.Native.MID);
                return httpServletRequest;
            }
        };
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testApplyPromo() {
        ApplyPromoRequest request = new ApplyPromoRequest();
        getApplyPromoRequestSetHead(request);
        getApplyPromoRequestSetBody(request);
        UserDetailsBiz userDetailsBiz = new UserDetailsBiz();
        userDetailsBiz.setUserId("userId");
        request.getHead().setTokenType(TokenType.SSO);
        when(tokenValidationHelper.validateToken(any(), any(), any(), any())).thenReturn(userDetailsBiz);
        paymentOffersService.applyPromo(request, "version", "referenceId");

    }

    @Test
    public void testApplyPromoWhenTokentypeIsChecksum() {
        ApplyPromoRequest request = new ApplyPromoRequest();
        getApplyPromoRequestSetHead(request);
        getApplyPromoRequestSetBody(request);
        request.getHead().setTokenType(TokenType.CHECKSUM);
        new MockUp<OfflinePaymentUtils>() {
            @mockit.Mock
            public HttpServletRequest gethttpServletRequest() {
                HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
                exceptionRule.expect(BaseException.class);
                when(httpServletRequest.getParameter(any())).thenReturn(TheiaConstant.RequestParams.Native.MID)
                        .thenReturn(TheiaConstant.RequestParams.Native.MID);
                return httpServletRequest;
            }
        };
        paymentOffersService.applyPromo(request, "version", "referenceId");
    }

    @Test
    public void testApplyPromoWhenTokentypeIsAccess() throws RequestValidationException {
        ApplyPromoRequest request = new ApplyPromoRequest();
        getApplyPromoRequestSetHead(request);
        getApplyPromoRequestSetBody(request);
        request.getHead().setTokenType(TokenType.ACCESS);
        CreateAccessTokenServiceRequest accessTokenServiceRequest = new CreateAccessTokenServiceRequest();
        accessTokenServiceRequest.setPaytmSsoToken("paytmSSOToken");
        UserDetailsBiz userDetailsBiz = new UserDetailsBiz();
        userDetailsBiz.setUserId("userId");
        when(tokenValidationHelper.validateToken(any(), any(), any(), any())).thenReturn(userDetailsBiz);
        when(accessTokenUtils.validateAccessToken(any(), any(), any())).thenReturn(accessTokenServiceRequest);
        paymentOffersService.applyPromo(request, "version", "referenceId");
    }

    @Test
    public void testApplyPromoWhenTokentypeIsTxn_Token() {
        ApplyPromoRequest request = new ApplyPromoRequest();
        getApplyPromoRequestSetHead(request);
        getApplyPromoRequestSetBody(request);
        request.getHead().setTokenType(TokenType.TXN_TOKEN);
        paymentOffersService.applyPromo(request, "version", "referenceId");
    }

    @Test
    public void testApplyPromoWhenTokentypeIsOther() throws RequestValidationException {
        ApplyPromoRequest request = new ApplyPromoRequest();
        getApplyPromoRequestSetHead(request);
        getApplyPromoRequestSetBody(request);
        request.getHead().setTokenType(TokenType.JWT);
        exceptionRule.expect(RequestValidationException.class);
        paymentOffersService.applyPromo(request, "version", "referenceId");
    }

    @Test
    public void testFetchAllPaymentOffers() {
        FetchAllPaymentOffersRequest request = new FetchAllPaymentOffersRequest();
        getFetchAllPaymentOffersRequestSetBody(request);
        getFetchAllPaymentOffersRequestSetHead(request);
        request.getHead().setTokenType(TokenType.TXN_TOKEN);
        when(ff4JUtil.isMigrateBankOffersPromo(any())).thenReturn(true);
        paymentOffersService.fetchAllPaymentOffers(request, "referenceId");
    }

    @Test
    public void testFetchAllPaymentOffersWhenTokenTypeSSO() {
        FetchAllPaymentOffersRequest request = new FetchAllPaymentOffersRequest();
        getFetchAllPaymentOffersRequestSetBody(request);
        getFetchAllPaymentOffersRequestSetHead(request);
        request.getHead().setTokenType(TokenType.SSO);
        UserDetailsBiz userDetailsBiz = new UserDetailsBiz();
        userDetailsBiz.setUserId("userId");
        when(tokenValidationHelper.validateToken(any(), any(), any(), any())).thenReturn(userDetailsBiz);
        paymentOffersService.fetchAllPaymentOffers(request, "referenceId");
    }

    @Test
    public void testFetchAllPaymentOffersWhenTokenTypeAccess() {
        FetchAllPaymentOffersRequest request = new FetchAllPaymentOffersRequest();
        getFetchAllPaymentOffersRequestSetBody(request);
        getFetchAllPaymentOffersRequestSetHead(request);
        request.getHead().setTokenType(TokenType.ACCESS);
        UserDetailsBiz userDetailsBiz = new UserDetailsBiz();
        userDetailsBiz.setUserId("userId");
        when(tokenValidationHelper.validateToken(any(), any(), any(), any())).thenReturn(userDetailsBiz);
        paymentOffersService.fetchAllPaymentOffers(request, "referenceId");
    }

    @Test
    public void testFetchAllPaymentOffersWhenTokenTypeOther() {
        FetchAllPaymentOffersRequest request = new FetchAllPaymentOffersRequest();
        getFetchAllPaymentOffersRequestSetBody(request);
        getFetchAllPaymentOffersRequestSetHead(request);
        request.getHead().setTokenType(TokenType.JWT);
        paymentOffersService.fetchAllPaymentOffers(request, "referenceId");
    }

    @Test
    public void testGetApplyPromoResponse() {
        ApplyPromoRequest request = new ApplyPromoRequest();
        getApplyPromoRequestSetBody(request);
        getApplyPromoRequestSetHead(request);
        when(ff4JUtil.isMigrateBankOffersPromo(any())).thenReturn(false);
        paymentOffersService.getApplyPromoResponse(request, "refId");

        when(ff4JUtil.isMigrateBankOffersPromo(any())).thenReturn(true);
        paymentOffersService.getApplyPromoResponse(request, "refId");
    }

    @Test
    public void testApplyItemLevelPromo() {
        ApplyPromoRequest request = new ApplyPromoRequest();
        getApplyPromoRequestSetHead(request);
        getApplyPromoRequestSetBody(request);
        request.getHead().setTokenType(TokenType.TXN_TOKEN);
        when(paymentOffersServiceHelperV2.prepareResponse(any(), any())).thenReturn(new ApplyItemLevelPromoResponse());
        when(paymentOffersServiceHelperV2.applyPromoV2(any(), any())).thenReturn(new ApplyPromoServiceResponseV2());
        paymentOffersService.applyItemLevelPromo(request, "referenceId");
    }

    private void getApplyPromoRequestSetHead(ApplyPromoRequest request) {
        TokenRequestHeader tokenRequestHeader = new TokenRequestHeader();
        tokenRequestHeader.setRequestId("requestId");
        tokenRequestHeader.setRequestTimestamp("requestTimestamp");
        tokenRequestHeader.setToken("token");
        // tokenRequestHeader.setTokenType(TokenType.CHECKSUM);
        tokenRequestHeader.setVersion("version");
        tokenRequestHeader.setChannelId(EChannelId.APP);
        tokenRequestHeader.setTxnToken("txnToken");
        tokenRequestHeader.setWorkFlow("workFlow");
        request.setHead(tokenRequestHeader);
    }

    private void getApplyPromoRequestSetBody(ApplyPromoRequest request) {
        ApplyPromoRequestBody applyPromoRequestBody = new ApplyPromoRequestBody();
        PromoPaymentOption promoPaymentOption = new PromoPaymentOption();
        promoPaymentOption.setTransactionAmount("12345");
        promoPaymentOption.setPayMethod(PayMethod.BALANCE);
        applyPromoRequestBody.setMid("mid");
        applyPromoRequestBody.setOrderId("orderId");
        // applyPromoRequestBody.setCustId("custId");
        applyPromoRequestBody.setPromocode("promoCode");
        applyPromoRequestBody.setPromoContext(new HashMap<>());
        applyPromoRequestBody.setPaymentOptions(Collections.singletonList(promoPaymentOption));
        // applyPromoRequestBody.setPaytmUserId("userId");
        applyPromoRequestBody.setPromoForPCFMerchant(true);
        applyPromoRequestBody.setTotalTransactionAmount("12345");
        request.setBody(applyPromoRequestBody);
    }

    private void getFetchAllPaymentOffersRequestSetBody(FetchAllPaymentOffersRequest request) {
        FetchAllPaymentOffersRequestBody body = new FetchAllPaymentOffersRequestBody();
        body.setMid("mid");
        body.setOrderId("orderId");
        request.setBody(body);
    }

    private void getFetchAllPaymentOffersRequestSetHead(FetchAllPaymentOffersRequest request) {
        TokenRequestHeader tokenRequestHeader = new TokenRequestHeader();
        tokenRequestHeader.setRequestId("requestId");
        tokenRequestHeader.setRequestTimestamp("requestTimestamp");
        tokenRequestHeader.setToken("token");
        // tokenRequestHeader.setTokenType(TokenType.CHECKSUM);
        tokenRequestHeader.setVersion("version");
        tokenRequestHeader.setChannelId(EChannelId.APP);
        tokenRequestHeader.setTxnToken("txnToken");
        tokenRequestHeader.setWorkFlow("workFlow");
        request.setHead(tokenRequestHeader);
    }

}