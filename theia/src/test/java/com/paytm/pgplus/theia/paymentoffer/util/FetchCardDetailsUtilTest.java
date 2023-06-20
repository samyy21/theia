package com.paytm.pgplus.theia.paymentoffer.util;

import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.workflow.service.helper.CardCenterHelper;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.user.models.CardBinDigestDetailInfo;
import com.paytm.pgplus.facade.user.models.response.CardBinHashResponse;
import com.paytm.pgplus.facade.user.models.response.CardInfoResponse;
import com.paytm.pgplus.facade.user.models.response.QueryNonSensitiveAssetInfoResponse;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailRequest;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailResponse;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailResponseBody;
import com.paytm.pgplus.theia.nativ.model.bin.cardhash.NativeBinCardHashAPIRequest;
import com.paytm.pgplus.theia.nativ.model.bin.cardhash.NativeBinCardHashAPIRequestBody;
import com.paytm.pgplus.theia.nativ.model.bin.cardhash.NativeBinCardHashAPIServiceReq;
import com.paytm.pgplus.theia.nativ.model.bin.cardhash.NativeBinCardHashAPIServiceRes;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.processor.impl.NativeBinCardHashRequestProcessor;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.model.common.BinData;
import org.junit.Test;
import org.mockito.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Matchers.*;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class FetchCardDetailsUtilTest {

    @InjectMocks
    private FetchCardDetailsUtil fetchCardDetailsUtil = new FetchCardDetailsUtil();

    @Mock
    private NativeBinCardHashRequestProcessor nativeBinCardHashRequestProcessor;

    @Mock
    FF4JUtil ff4JUtil;

    @Mock
    WorkFlowHelper workFlowHelper;

    @Mock
    private CardCenterHelper cardCenterHelper;

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchCardDetailsUtil.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testProcessForCardNumber() throws Exception {
        NativeBinCardHashAPIServiceReq nativeBinCardHashAPIServiceReq = new NativeBinCardHashAPIServiceReq();
        NativeBinCardHashAPIServiceRes nativeBinCardHashAPIServiceRes = new NativeBinCardHashAPIServiceRes();
        NativeBinCardHashAPIRequest request = new NativeBinCardHashAPIRequest();
        CardBinHashResponse cardBinHashResponse = mock(CardBinHashResponse.class);
        NativeBinDetailResponseBody body = new NativeBinDetailResponseBody();
        CardBinDigestDetailInfo detailInfo = mock(CardBinDigestDetailInfo.class);
        body.setBinDetail(new BinData());
        getNativeBinCardHashAPIRequestSetBody(request);
        getNativeBinCardHashAPIRequestSetHead(request);
        when(detailInfo.getEightDigitBinHash()).thenReturn("12345678");
        when(nativeBinCardHashRequestProcessor.getNativeBinDetailRequest(any(), any())).thenReturn(
                new NativeBinDetailRequest());
        when(nativeBinCardHashRequestProcessor.getBinDetails(any())).thenReturn(
                new NativeBinDetailResponse(new ResponseHeader(), body));
        when(workFlowHelper.getCardIndexNoFromCardNumber(any())).thenReturn("cardNumber");
        when(workFlowHelper.getCardBinHash(any())).thenReturn(cardBinHashResponse);
        when(cardBinHashResponse.getCardBinDigestDetailInfo()).thenReturn(detailInfo);
        fetchCardDetailsUtil.processForCardNumber(nativeBinCardHashAPIServiceReq, nativeBinCardHashAPIServiceRes,
                request);

        exceptionRule.expect(Exception.class);
        when(workFlowHelper.getCardBinHash(any())).thenReturn(null);
        fetchCardDetailsUtil.processForCardNumber(nativeBinCardHashAPIServiceReq, nativeBinCardHashAPIServiceRes,
                request);
    }

    @Test
    public void testProcessForCardNumberWhenCardIndexNumberIsNull() throws Exception {
        NativeBinCardHashAPIServiceReq nativeBinCardHashAPIServiceReq = new NativeBinCardHashAPIServiceReq();
        NativeBinCardHashAPIServiceRes nativeBinCardHashAPIServiceRes = new NativeBinCardHashAPIServiceRes();
        NativeBinCardHashAPIRequest request = new NativeBinCardHashAPIRequest();
        getNativeBinCardHashAPIRequestSetBody(request);
        getNativeBinCardHashAPIRequestSetHead(request);

        exceptionRule.expect(Exception.class);
        when(workFlowHelper.getCardIndexNoFromCardNumber(any())).thenReturn(null);
        fetchCardDetailsUtil.processForCardNumber(nativeBinCardHashAPIServiceReq, nativeBinCardHashAPIServiceRes,
                request);
    }

    @Test
    public void testProcessForSavedCardId() throws Exception {
        NativeBinCardHashAPIServiceReq nativeBinCardHashAPIServiceReq = new NativeBinCardHashAPIServiceReq();
        NativeBinCardHashAPIServiceRes nativeBinCardHashAPIServiceRes = new NativeBinCardHashAPIServiceRes();
        NativeBinCardHashAPIRequest request = new NativeBinCardHashAPIRequest();
        getNativeBinCardHashAPIRequestSetBody(request);
        getNativeBinCardHashAPIRequestSetHead(request);
        QueryNonSensitiveAssetInfoResponse response = new QueryNonSensitiveAssetInfoResponse();
        Map<String, String> map = new HashMap<>();
        map.put(TheiaConstant.ExtraConstants.EIGHT_DIGIT_BIN_HASH, "12345678");
        CardInfoResponse cardInfoResponse = new CardInfoResponse();
        cardInfoResponse.setMaskedCardNo("maskedCardNo");
        cardInfoResponse.setExtendInfo(map);
        response.setCardInfo(cardInfoResponse);
        CardBinHashResponse cardBinHashResponse = mock(CardBinHashResponse.class);
        NativeBinDetailResponseBody body = new NativeBinDetailResponseBody();
        CardBinDigestDetailInfo detailInfo = mock(CardBinDigestDetailInfo.class);
        body.setBinDetail(new BinData());
        when(cardCenterHelper.queryNonSensitiveAssetInfo(any(), any())).thenReturn(response);
        when(nativeBinCardHashRequestProcessor.getCardNumber(any())).thenReturn("12345678");
        when(nativeBinCardHashRequestProcessor.getNativeBinDetailRequest(any(), any())).thenReturn(
                new NativeBinDetailRequest());
        when(nativeBinCardHashRequestProcessor.getBinDetails(any())).thenReturn(
                new NativeBinDetailResponse(new ResponseHeader(), body));
        when(workFlowHelper.getCardIndexNoFromCardNumber(any())).thenReturn("70234515678");
        when(workFlowHelper.getCardBinHash(any())).thenReturn(cardBinHashResponse);
        when(cardBinHashResponse.getCardBinDigestDetailInfo()).thenReturn(detailInfo);
        fetchCardDetailsUtil.processForSavedCardId(nativeBinCardHashAPIServiceReq, nativeBinCardHashAPIServiceRes,
                request);

        exceptionRule.expect(Exception.class);
        when(cardCenterHelper.queryNonSensitiveAssetInfo(any(), any())).thenReturn(null);
        fetchCardDetailsUtil.processForSavedCardId(nativeBinCardHashAPIServiceReq, nativeBinCardHashAPIServiceRes,
                request);
    }

    @Test
    public void testProcessForSavedCardIdWhenResponseNUll() throws Exception {
        NativeBinCardHashAPIServiceReq nativeBinCardHashAPIServiceReq = new NativeBinCardHashAPIServiceReq();
        NativeBinCardHashAPIServiceRes nativeBinCardHashAPIServiceRes = new NativeBinCardHashAPIServiceRes();
        NativeBinCardHashAPIRequest request = new NativeBinCardHashAPIRequest();
        getNativeBinCardHashAPIRequestSetHead(request);
        NativeBinCardHashAPIRequestBody body1 = new NativeBinCardHashAPIRequestBody();
        body1.setSavedCardId("1234");
        body1.setEightDigitBinRequired(true);
        request.setBody(body1);
        CardBinHashResponse cardBinHashResponse = mock(CardBinHashResponse.class);
        NativeBinDetailResponseBody body = new NativeBinDetailResponseBody();
        CardBinDigestDetailInfo detailInfo = mock(CardBinDigestDetailInfo.class);
        body.setBinDetail(new BinData());
        when(ff4JUtil.isFeatureEnabledForPromo(any())).thenReturn(true);
        when(nativeBinCardHashRequestProcessor.getCardNumber(any())).thenReturn("12345678");
        when(nativeBinCardHashRequestProcessor.getNativeBinDetailRequest(any(), any())).thenReturn(
                new NativeBinDetailRequest());
        when(nativeBinCardHashRequestProcessor.getBinDetails(any())).thenReturn(
                new NativeBinDetailResponse(new ResponseHeader(), body));
        when(workFlowHelper.getCardIndexNoFromCardNumber(any())).thenReturn("70234515678");
        when(workFlowHelper.getCardBinHash(any())).thenReturn(cardBinHashResponse);
        when(cardBinHashResponse.getCardBinDigestDetailInfo()).thenReturn(detailInfo);
        fetchCardDetailsUtil.processForSavedCardId(nativeBinCardHashAPIServiceReq, nativeBinCardHashAPIServiceRes,
                request);

        exceptionRule.expect(Exception.class);
        when(workFlowHelper.getCardBinHash(any())).thenReturn(null);
        fetchCardDetailsUtil.processForSavedCardId(nativeBinCardHashAPIServiceReq, nativeBinCardHashAPIServiceRes,
                request);

    }

    @Test
    public void testBothWhenIsEightDigitBinRequiredIsFalse() throws Exception {
        NativeBinCardHashAPIServiceReq nativeBinCardHashAPIServiceReq = new NativeBinCardHashAPIServiceReq();
        NativeBinCardHashAPIServiceRes nativeBinCardHashAPIServiceRes = new NativeBinCardHashAPIServiceRes();
        NativeBinCardHashAPIRequest request = new NativeBinCardHashAPIRequest();
        getNativeBinCardHashAPIRequestSetHead(request);
        NativeBinCardHashAPIRequestBody body2 = new NativeBinCardHashAPIRequestBody();
        body2.setEightDigitBinRequired(false);
        body2.setSavedCardId("123456789123456");
        request.setBody(body2);
        CardBinHashResponse cardBinHashResponse = mock(CardBinHashResponse.class);
        NativeBinDetailResponseBody body = new NativeBinDetailResponseBody();
        CardBinDigestDetailInfo detailInfo = mock(CardBinDigestDetailInfo.class);
        body.setBinDetail(new BinData());
        when(ff4JUtil.isFeatureEnabledForPromo(any())).thenReturn(true);
        when(nativeBinCardHashRequestProcessor.getCardNumber(any())).thenReturn("12345678");
        when(nativeBinCardHashRequestProcessor.getNativeBinDetailRequest(any(), any())).thenReturn(
                new NativeBinDetailRequest());
        when(nativeBinCardHashRequestProcessor.getBinDetails(any())).thenReturn(
                new NativeBinDetailResponse(new ResponseHeader(), body));
        when(workFlowHelper.getCardIndexNoFromCardNumber(any())).thenReturn("70234515678");
        when(workFlowHelper.getCardBinHash(any())).thenReturn(cardBinHashResponse);
        when(cardBinHashResponse.getCardBinDigestDetailInfo()).thenReturn(detailInfo);
        fetchCardDetailsUtil.processForSavedCardId(nativeBinCardHashAPIServiceReq, nativeBinCardHashAPIServiceRes,
                request);
        fetchCardDetailsUtil.processForCardNumber(nativeBinCardHashAPIServiceReq, nativeBinCardHashAPIServiceRes,
                request);
    }

    private void getNativeBinCardHashAPIRequestSetHead(NativeBinCardHashAPIRequest request) {
        TokenRequestHeader tokenRequestHeader = new TokenRequestHeader();
        tokenRequestHeader.setTokenType(TokenType.SSO);
        tokenRequestHeader.setRequestId("requestId");
        tokenRequestHeader.setVersion("version");
        tokenRequestHeader.setToken("token");
        tokenRequestHeader.setChannelId(EChannelId.APP);
        tokenRequestHeader.setRequestTimestamp("requestTimestamp");
        request.setHead(tokenRequestHeader);
    }

    private void getNativeBinCardHashAPIRequestSetBody(NativeBinCardHashAPIRequest request) {
        NativeBinCardHashAPIRequestBody body = new NativeBinCardHashAPIRequestBody();
        body.setCardNumber("cardNumber");
        body.setMid("mid");
        body.setEightDigitBinRequired(true);
        body.setPaymentFlow("paymentFlow");
        body.setSavedCardId("12345678910111214");
        request.setBody(body);
    }
}