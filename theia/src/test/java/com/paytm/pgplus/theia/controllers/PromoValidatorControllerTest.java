package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cache.model.PaytmProperty;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.promo.service.client.model.PromoCodeResponse;
import com.paytm.pgplus.promo.service.client.service.IPromoServiceHelper;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @createdOn 04-June-2021
 * @author Siva
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:servlet-context-test.xml" })
@WebAppConfiguration
public class PromoValidatorControllerTest {

    private MockMvc mockMvc;
    private MockHttpServletResponse response;
    private MockHttpServletRequest request;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        response = new MockHttpServletResponse();
        request = new MockHttpServletRequest();
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        transactionCacheUtils.putTransInfoInCache("20170613111212800110166869000008982", "SCWMER90619707098260", "abc",
                true);
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty("catalina.base", "");
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Autowired
    private TransactionCacheUtils transactionCacheUtils;

    @Autowired
    WebApplicationContext wac;

    @InjectMocks
    private PromoValidatorController promoValidatorController;

    @Mock
    private ITheiaViewResolverService theiaViewResolverService;

    @Mock
    private ITheiaSessionDataService theiaSessionDataService;

    @Mock
    private IPromoServiceHelper promoServiceHelper;

    @Mock
    private CardUtils cardUtils;

    @Mock
    private IConfigurationDataService configurationDataService;

    @Test
    public void testCheckPromoValidityForDebitPromoOldCardTransacted() throws PaytmValidationException {

        when(cardUtils.fetchBinDetails(any())).thenReturn(setRequestTestData("DEBIT_CARD"));
        PromoCodeResponse promoCodeResponse = new PromoCodeResponse();
        promoCodeResponse.setPromoResponseCode("710");
        when(promoServiceHelper.validateCardPromoCode(any())).thenReturn(promoCodeResponse);
        PaytmProperty paytmProperty = new PaytmProperty();
        paytmProperty.setValue("Testvalue");
        when(configurationDataService.getPaytmProperty(any())).thenReturn(paytmProperty);
        promoValidatorController.checkPromoValidity(request, response, null, null);
    }

    @Test
    public void testCheckPromoValidityDBPromoMaxCountExceeded() throws PaytmValidationException {

        when(cardUtils.fetchBinDetails(any())).thenReturn(setRequestTestData("DEBIT_CARD"));
        PromoCodeResponse promoCodeResponse = new PromoCodeResponse();
        promoCodeResponse.setPromoResponseCode("709");
        when(promoServiceHelper.validateCardPromoCode(any())).thenReturn(promoCodeResponse);
        PaytmProperty paytmProperty = new PaytmProperty();
        paytmProperty.setValue("Testvalue");
        when(configurationDataService.getPaytmProperty(any())).thenReturn(paytmProperty);
        promoValidatorController.checkPromoValidity(request, response, null, null);
    }

    @Test
    public void testCheckPromoValidityDBPromoMaxAmountExceeded() throws PaytmValidationException {

        when(cardUtils.fetchBinDetails(any())).thenReturn(setRequestTestData("DEBIT_CARD"));
        PromoCodeResponse promoCodeResponse = new PromoCodeResponse();
        promoCodeResponse.setPromoResponseCode("708");
        when(promoServiceHelper.validateCardPromoCode(any())).thenReturn(promoCodeResponse);
        PaytmProperty paytmProperty = new PaytmProperty();
        paytmProperty.setValue("Testvalue");
        when(configurationDataService.getPaytmProperty(any())).thenReturn(paytmProperty);
        promoValidatorController.checkPromoValidity(request, response, null, null);
    }

    @Test
    public void testCheckPromoValidityForDebitPromoSuccess() throws PaytmValidationException {

        when(cardUtils.fetchBinDetails(any())).thenReturn(setRequestTestData("DEBIT_CARD"));
        PromoCodeResponse promoCodeResponse = new PromoCodeResponse();
        promoCodeResponse.setPromoResponseCode("RANDOM");
        when(promoServiceHelper.validateCardPromoCode(any())).thenReturn(promoCodeResponse);
        PaytmProperty paytmProperty = new PaytmProperty();
        paytmProperty.setValue("Testvalue");
        when(configurationDataService.getPaytmProperty(any())).thenReturn(paytmProperty);
        promoValidatorController.checkPromoValidity(request, response, null, null);
    }

    @Test
    public void testCheckPromoValidityForCreditCardsPromoOldCardTransacted() throws PaytmValidationException {

        when(cardUtils.fetchBinDetails(any())).thenReturn(setRequestTestData("CREDIT_CARD"));
        PromoCodeResponse promoCodeResponse = new PromoCodeResponse();
        promoCodeResponse.setPromoResponseCode("710");
        when(promoServiceHelper.validateCardPromoCode(any())).thenReturn(promoCodeResponse);
        PaytmProperty paytmProperty = new PaytmProperty();
        paytmProperty.setValue("Testvalue");
        when(configurationDataService.getPaytmProperty(any())).thenReturn(paytmProperty);
        promoValidatorController.checkPromoValidity(request, response, null, null);
    }

    @Test
    public void testCheckPromoValidityForCreditCardPromoMaxCountExceeded() throws PaytmValidationException {

        when(cardUtils.fetchBinDetails(any())).thenReturn(setRequestTestData("CREDIT_CARD"));
        PromoCodeResponse promoCodeResponse = new PromoCodeResponse();
        promoCodeResponse.setPromoResponseCode("709");
        when(promoServiceHelper.validateCardPromoCode(any())).thenReturn(promoCodeResponse);
        PaytmProperty paytmProperty = new PaytmProperty();
        paytmProperty.setValue("Testvalue");
        when(configurationDataService.getPaytmProperty(any())).thenReturn(paytmProperty);
        promoValidatorController.checkPromoValidity(request, response, null, null);
    }

    @Test
    public void testCheckPromoValidityForCreditCardPromoMaxAmountExceeded() throws PaytmValidationException {

        when(cardUtils.fetchBinDetails(any())).thenReturn(setRequestTestData("CREDIT_CARD"));
        PromoCodeResponse promoCodeResponse = new PromoCodeResponse();
        promoCodeResponse.setPromoResponseCode("708");
        when(promoServiceHelper.validateCardPromoCode(any())).thenReturn(promoCodeResponse);
        PaytmProperty paytmProperty = new PaytmProperty();
        paytmProperty.setValue("Testvalue");
        when(configurationDataService.getPaytmProperty(any())).thenReturn(paytmProperty);
        promoValidatorController.checkPromoValidity(request, response, null, null);
    }

    @Test
    public void testCheckPromoValidityForCreditPromoSuccess() throws PaytmValidationException {

        when(cardUtils.fetchBinDetails(any())).thenReturn(setRequestTestData("CREDIT_CARD"));
        PromoCodeResponse promoCodeResponse = new PromoCodeResponse();
        promoCodeResponse.setPromoResponseCode("RANDOM");
        when(promoServiceHelper.validateCardPromoCode(any())).thenReturn(promoCodeResponse);
        PaytmProperty paytmProperty = new PaytmProperty();
        paytmProperty.setValue("Testvalue");
        when(configurationDataService.getPaytmProperty(any())).thenReturn(paytmProperty);
        promoValidatorController.checkPromoValidity(request, response, null, null);
    }

    private BinDetail setRequestTestData(String cardType) {
        request.setParameter("MID", "TestMid");
        request.setParameter("PROMO_CAMP_ID", "TestPROMO_CAMP_ID");
        request.setParameter("CARD_NO", "TestCARD_NO");
        BinDetail binDetail = new BinDetail(null, null, null, null, null, cardType, null, null, true, null, true, true,
                true, null, true, null, false, false, false, null, null, null, null, null, null, null, null, false,
                null, null, null);
        return binDetail;
    }
}