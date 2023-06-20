package com.paytm.pgplus.theia.validator.service;

import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.pgproxycommon.enums.CardScheme;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.theia.models.ExpressCardTokenRequest;
import com.paytm.pgplus.theia.services.helper.ExpressPaymentServiceHelper;
import mockit.MockUp;
import org.junit.Test;
import org.mockito.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*;

public class ExpressPaymentValidationTest {

    @InjectMocks
    private ExpressPaymentValidation expressPaymentValidation = new ExpressPaymentValidation();

    @Mock
    ExpressPaymentServiceHelper expressPaymentServiceHelper;

    @Mock
    private CardUtils cardUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressPaymentValidation.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        new MockUp<CardScheme>() {
            @mockit.Mock
            public CardScheme getCardSchemebyName() {
                when(CardScheme.getCardSchemebyName("MAESTRO")).thenReturn(CardScheme.MAESTRO).thenReturn(
                        CardScheme.MAESTRO);
                when(CardScheme.getCardSchemebyName("AMEX")).thenReturn(CardScheme.AMEX).thenReturn(CardScheme.AMEX);
                when(CardScheme.getCardSchemebyName(any())).thenReturn(CardScheme.OTHERS).thenReturn(CardScheme.OTHERS);
                return CardScheme.getCardSchemebyName("scheme");
            }
        };
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testValidateCardNumber() {
        expressPaymentValidation.validateCardNumber("cardNumber");
        expressPaymentValidation.validateCardNumber(null);
    }

    @Test
    public void testValidateExpiryDate() throws PaytmValidationException {
        expressPaymentValidation.validateExpiryDate("month", "year", "cardScheme");

        doThrow(new PaytmValidationException("message")).when(cardUtils).validateExpiryDate(any(), any(), any(), any());
        expressPaymentValidation.validateExpiryDate("month", "year", "cardScheme");
    }

    @Test
    public void testValidateCvv() throws PaytmValidationException {
        ExpressCardTokenRequest request = new ExpressCardTokenRequest();
        request.setCardNumber("1234567");
        request.setCardType("cardType");
        request.setCvv("cvv123");
        when(expressPaymentServiceHelper.fetchBinRelatedDetails(any())).thenReturn(null);
        expressPaymentValidation.validateCvv(request);

        BinDetail binDetail = new BinDetail();
        binDetail.setCardType("cardType");
        binDetail.setCardName("MAESTRO");
        binDetail.setBankCode("bankCode");
        when(expressPaymentServiceHelper.fetchBinRelatedDetails(any())).thenReturn(binDetail);
        expressPaymentValidation.validateCvv(request);

        BinDetail binDetail1 = new BinDetail();
        binDetail1.setCardName("AMEX");
        when(expressPaymentServiceHelper.fetchBinRelatedDetails(any())).thenReturn(binDetail1);
        expressPaymentValidation.validateCvv(request);

        BinDetail binDetail2 = new BinDetail();
        binDetail2.setCardName("VISA");
        when(expressPaymentServiceHelper.fetchBinRelatedDetails(any())).thenReturn(binDetail2);
        expressPaymentValidation.validateCvv(request);

        when(expressPaymentServiceHelper.fetchBinRelatedDetails(any())).thenThrow(
                new PaytmValidationException("errorcode", "message"));
        expressPaymentValidation.validateCvv(request);
    }

    @Test
    public void testValidatingExpireByCardscheme() {
        expressPaymentValidation.validatingExpireByCardscheme("cvvvv", "BAJAJ");
    }

}