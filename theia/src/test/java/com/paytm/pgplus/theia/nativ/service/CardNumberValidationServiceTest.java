package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.theia.models.VisaCyberSourceResponse;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.model.validateCard.CardNumberValidationRequest;
import com.paytm.pgplus.theia.nativ.model.validateCard.CardNumberValidationRequestBody;
import com.paytm.pgplus.theia.nativ.model.validateCard.CardNumberValidationResponse;

import com.paytm.pgplus.theia.offline.model.common.BinData;
import com.paytm.pgplus.theia.offline.model.response.BinDetailResponse;
import com.paytm.pgplus.theia.offline.model.response.BinDetailResponseBody;
import com.paytm.pgplus.theia.offline.services.impl.BinDetailService;
import com.paytm.pgplus.theia.services.IVisaCyberSourceService;
import com.paytm.pgplus.theia.utils.CardTypeUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.mockito.MockitoAnnotations;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:servlet-context-test.xml" })
@WebAppConfiguration
@EnableWebMvc
public class CardNumberValidationServiceTest {

    @InjectMocks
    CardNumberValidationService cardNumberValidationService;

    @Mock
    BinDetailService binDetailService;

    @Mock
    CardUtils cardUtils;

    @Mock
    CardTypeUtil cardTypeUtil;

    @Mock
    IVisaCyberSourceService visaCyberSourceService;

    @Autowired
    WebApplicationContext wac;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty("catalina.base", "");
    }

    @Test
    public void testFetchCardNumberValidationDetailWhenExpiryDateNotValid() throws PaytmValidationException {
        when(binDetailService.fetchBinDetailsWithSuccessRateforThirdparty(any())).thenReturn(new BinDetailResponse());
        when(cardTypeUtil.checkForCardType(any(), any())).thenReturn(true);
        doThrow(new PaytmValidationException(PaytmValidationExceptionType.INVALID_CARD_EXPIRY)).when(cardUtils)
                .validateExpiryDate(anyString(), anyString(), any(), any());

        CardNumberValidationRequestBody cardNumberValidationRequestBody = new CardNumberValidationRequestBody(
                "1234567845678", "112020");
        cardNumberValidationRequestBody.setRequestId("abc");
        CardNumberValidationRequest cardNumberValidationRequest = new CardNumberValidationRequest();
        cardNumberValidationRequest.setBody(cardNumberValidationRequestBody);

        TokenRequestHeader tokenRequestHeader = new TokenRequestHeader();
        tokenRequestHeader.setChannelId(EChannelId.APP);
        cardNumberValidationRequest.setHead(tokenRequestHeader);
        CardNumberValidationResponse actualcardNumberValidationResponse = cardNumberValidationService
                .fetchCardNumberValidationDetail(cardNumberValidationRequest);
        Assert.assertEquals("F", actualcardNumberValidationResponse.getBody().getPerformanceStatus());
    }

    @Test
    public void testFetchCardNumberValidationDetailWhenCardTypeRupay() throws PaytmValidationException {

        when(binDetailService.fetchBinDetailsWithSuccessRateforThirdparty(any())).thenReturn(new BinDetailResponse());
        when(cardTypeUtil.checkForCardType(any(), any())).thenReturn(true);
        doNothing().when(cardUtils).validateExpiryDate(any(), any(), any(), any());

        CardNumberValidationRequestBody cardNumberValidationRequestBody = new CardNumberValidationRequestBody(
                "1234567845678", "112025");
        cardNumberValidationRequestBody.setRequestId("abc");
        CardNumberValidationRequest cardNumberValidationRequest = new CardNumberValidationRequest();
        cardNumberValidationRequest.setBody(cardNumberValidationRequestBody);

        TokenRequestHeader tokenRequestHeader = new TokenRequestHeader();
        tokenRequestHeader.setChannelId(EChannelId.APP);
        cardNumberValidationRequest.setHead(tokenRequestHeader);
        CardNumberValidationResponse actualcardNumberValidationResponse = cardNumberValidationService
                .fetchCardNumberValidationDetail(cardNumberValidationRequest);
        Assert.assertEquals("U", actualcardNumberValidationResponse.getBody().getPerformanceStatus());

    }

    @Test
    public void testFetchCardNumberValidationDetailWhenReturnCardNumberValidationResponse()
            throws PaytmValidationException {

        when(binDetailService.fetchBinDetailsWithSuccessRateforThirdparty(any())).thenReturn(new BinDetailResponse());
        when(cardTypeUtil.checkForCardType(any(), any())).thenReturn(false);
        doNothing().when(cardUtils).validateExpiryDate(any(), any(), any(), any());
        when(visaCyberSourceService.getCardDetailFromVisaCyberSource(any())).thenReturn(new VisaCyberSourceResponse());

        CardNumberValidationRequestBody cardNumberValidationRequestBody = new CardNumberValidationRequestBody(
                "1234567845678", "112025");
        cardNumberValidationRequestBody.setRequestId("abc");
        CardNumberValidationRequest cardNumberValidationRequest = new CardNumberValidationRequest();
        cardNumberValidationRequest.setBody(cardNumberValidationRequestBody);

        TokenRequestHeader tokenRequestHeader = new TokenRequestHeader();
        tokenRequestHeader.setChannelId(EChannelId.APP);
        cardNumberValidationRequest.setHead(tokenRequestHeader);

        Assert.assertNotNull(cardNumberValidationService.fetchCardNumberValidationDetail(cardNumberValidationRequest));

    }

    @Test
    public void testGenerateUniqueRandomString() {
        String timeNano = String.valueOf(System.nanoTime());
        String sb = cardNumberValidationService.generateUniqueRandomString(5);
        Assert.assertEquals(5 + timeNano.length(), sb.length());
    }

    @Test
    public void testPrepareValidateCardResponseWhenZeroSuccessRateTrue() {

        BinData binDetail = new BinData();
        binDetail.setIssuingBank("SBI");
        binDetail.setChannelName("ABC");
        binDetail.setPayMethod("CC");

        BinDetailResponseBody binDetailResponseBody = new BinDetailResponseBody();
        binDetailResponseBody.setBinDetail(binDetail);
        binDetailResponseBody.setIconUrl("xyz");
        binDetailResponseBody.setZeroSuccessRate(true);

        BinDetailResponse binDetailResponse = new BinDetailResponse();
        binDetailResponse.setBody(binDetailResponseBody);

        VisaCyberSourceResponse visaCyberSourceResponse = new VisaCyberSourceResponse();
        CardNumberValidationResponse actualCardNumberValidationResponse = cardNumberValidationService
                .prepareValidateCardResponse(binDetailResponse, visaCyberSourceResponse, "abc");
        Assert.assertEquals("F", actualCardNumberValidationResponse.getBody().getPerformanceStatus());
    }

    @Test
    public void testGetNewResultInfoResponse() {
        VisaCyberSourceResponse visaCyberSourceResponse = new VisaCyberSourceResponse();
        visaCyberSourceResponse.setStatus("abc");

        BinDetailResponse binDetailResponse = null;
        CardNumberValidationResponse cardNumberValidationResponse = cardNumberValidationService
                .prepareValidateCardResponse(binDetailResponse, visaCyberSourceResponse, "abc");
        Assert.assertEquals("U", cardNumberValidationResponse.getBody().getPerformanceStatus());
    }

}