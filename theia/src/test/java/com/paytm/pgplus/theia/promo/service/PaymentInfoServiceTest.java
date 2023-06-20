package com.paytm.pgplus.theia.promo.service;

import com.paytm.pgplus.cache.CardNetworkDetailResponse;
import com.paytm.pgplus.cache.model.*;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IBankDetailsDataService;
import com.paytm.pgplus.mappingserviceclient.service.IBankInfoDataService;
import com.paytm.pgplus.mappingserviceclient.service.ICardNetworkDataService;
import com.paytm.pgplus.mappingserviceclient.service.IPayMethodDataService;
import com.paytm.pgplus.theia.promo.model.*;
import org.junit.Test;

import java.util.Collections;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockito.Mock;
import org.mockito.InjectMocks;

import static org.mockito.Mockito.when;

public class PaymentInfoServiceTest {

    @InjectMocks
    private PaymentInfoService paymentInfoService = new PaymentInfoService();

    @Mock
    private IBankInfoDataService bankInfoDataService;

    @Mock
    private IBankDetailsDataService bankDetailsDataService;

    @Mock
    private IPayMethodDataService payMethodDataService;

    @Mock
    private ICardNetworkDataService cardNetworkDataService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentInfoService.class);
    private static final String DEFAULT_BANK_LOGO = "default.png";
    private static final String DEFAULT_CARD_LOGO = "default.png";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testGetPaymentPromoAttributeResponse() throws MappingServiceClientException {
        BankDetailsResponse bankDetailsResponse = new BankDetailsResponse();
        PayMethodDetailsResponse payMethodResponse = new PayMethodDetailsResponse();
        CardNetworkDetailResponse cardNetworResponse = new CardNetworkDetailResponse();
        BankDetails bankDetails = new BankDetails();
        bankDetails.setBankCode("bankCode");
        bankDetails.setLogoUrl(DEFAULT_BANK_LOGO);
        bankDetailsResponse.setBankDetailsList(Collections.singletonList(bankDetails));
        PayMethodDetails payMethodDetails = new PayMethodDetails();
        payMethodDetails.setPayMethod("payMethod");
        payMethodResponse.setPayMethodDetailsList(Collections.singletonList(payMethodDetails));
        CardNetworkDetails cardNetworkDetails = new CardNetworkDetails();
        cardNetworkDetails.setCardNetwork("cardNetwork");
        cardNetworkDetails.setLogoUrl(DEFAULT_CARD_LOGO);
        cardNetworResponse.setCardNetworkDetailsList(Collections.singletonList(cardNetworkDetails));
        when(bankDetailsDataService.getBankDetailResponse()).thenReturn(bankDetailsResponse);
        when(payMethodDataService.getAllPayMethodInfo()).thenReturn(payMethodResponse);
        when(cardNetworkDataService.getCardNetworkDetails()).thenReturn(cardNetworResponse);
        paymentInfoService.getPaymentPromoAttributeResponse();

        when(bankDetailsDataService.getBankDetailResponse()).thenThrow(
                new MappingServiceClientException("errorMessage"));
        paymentInfoService.getPaymentPromoAttributeResponse();

        when(payMethodDataService.getAllPayMethodInfo()).thenThrow(new MappingServiceClientException("errorMessage"));
        paymentInfoService.getPaymentPromoAttributeResponse();

        when(cardNetworkDataService.getCardNetworkDetails()).thenThrow(
                new MappingServiceClientException("errorMessage"));
        paymentInfoService.getPaymentPromoAttributeResponse();
    }

    @Test
    public void testIsValidRequest() {
        FetchPaymentInfoRequest request = new FetchPaymentInfoRequest();
        getFetchMerchantInfoRequestSetHead(request);
        paymentInfoService.isValidRequest(request);

    }

    private void getFetchMerchantInfoRequestSetHead(FetchPaymentInfoRequest request) {
        RequestHeader requestHeader = new RequestHeader();
        requestHeader.setRequestId("requestId");
        requestHeader.setClientId("clientId");
        requestHeader.setToken("token");
        requestHeader.setTokenType("tokenType");
        requestHeader.setVersion("version");
        requestHeader.setRequestTimeStamp("requestTimeStamp");
        request.setHead(requestHeader);
    }

}