package com.paytm.pgplus.theia.test.testflow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.user.service.impl.SavedCardsImpl;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.services.impl.TheiaSessionDataServiceImpl;
import com.paytm.pgplus.theia.sessiondata.DigitalCreditInfo;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.promo.service.client.model.PromoCodeBaseRequest;
import com.paytm.pgplus.promo.service.client.model.PromoCodeData;
import com.paytm.pgplus.promo.service.client.model.PromoCodeResponse;
import com.paytm.pgplus.promo.service.client.service.impl.PromoServiceHelperImpl;
import com.paytm.pgplus.theia.constants.ResponseCodeConstant;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.services.IPaymentService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.test.util.TestRequestUtil;
import com.paytm.pgplus.theia.test.util.TestResponseUtil;

import mockit.Mock;
import mockit.MockUp;

import javax.servlet.http.HttpServletRequest;

/**
 * @author kartik
 * @date 26-05-2017
 */
public class DefaultPaymentServiceImplSuccessTest extends AbstractPaymentServiceTest {

    private static final String DEFAULT_PAYMENT_TEST_REQUEST_KEY = "default.payment.flow.success.test.request.";
    private static final String DEFAULT_PAYMENT_TEST_RESPONSE_KEY = "default.payment.flow.success.test.response.";

    @Autowired
    @Qualifier(value = "defaultPaymentService")
    private IPaymentService defaultPaymentService;

    @Autowired
    @Qualifier(value = "theiaSessionDataService")
    private ITheiaSessionDataService sessionDataService;

    @Test
    public void testDefaultFlowUserLoggedIn() {

        new MockUp<TheiaSessionDataServiceImpl>() {
            @Mock
            public DigitalCreditInfo getDigitalCreditInfoFromSession(HttpServletRequest request, boolean createNew) {
                DigitalCreditInfo digitalCreditInfo = new DigitalCreditInfo();
                digitalCreditInfo.setDigitalCreditEnabled(true);
                digitalCreditInfo.setExternalAccountNo("TestAccNo");
                digitalCreditInfo.setLenderId("TestLenderId");
                return digitalCreditInfo;
            }
        };

        LOGGER.info("Running test case for Default Payment Service {User Logged In , Charge Payee}!!!");
        String jsonRequest = testResource.getTestProperties().getProperty(DEFAULT_PAYMENT_TEST_REQUEST_KEY + "1");
        PaymentRequestBean requestData = TestRequestUtil.mapJsonToObject(testResource.getObjectMapper(), jsonRequest,
                PaymentRequestBean.class);
        requestData.setOrderId(TestRequestUtil.getOrderID());
        requestData.setRequest(request);
        validatePaymentRequest(defaultPaymentService, requestData);
        PageDetailsResponse pageDetailsResponse = defaultPaymentService.processPaymentRequest(requestData, null);
        TestResponseUtil.validateTestResponse(pageDetailsResponse, requestData, sessionDataService, testResource,
                DEFAULT_PAYMENT_TEST_RESPONSE_KEY + "1");
    }

    @Test
    public void testDefaultFlowUserNotLoggedIn() {
        new MockUp<TheiaSessionDataServiceImpl>() {
            @Mock
            public DigitalCreditInfo getDigitalCreditInfoFromSession(HttpServletRequest request, boolean createNew) {
                DigitalCreditInfo digitalCreditInfo = new DigitalCreditInfo();
                digitalCreditInfo.setDigitalCreditEnabled(false);
                return digitalCreditInfo;
            }
        };

        LOGGER.info("Running test case for Default Payment Service {User Not Logged In , Charge Payee}!!!");
        String jsonRequest = testResource.getTestProperties().getProperty(DEFAULT_PAYMENT_TEST_REQUEST_KEY + "2");
        PaymentRequestBean requestData = TestRequestUtil.mapJsonToObject(testResource.getObjectMapper(), jsonRequest,
                PaymentRequestBean.class);
        requestData.setOrderId(TestRequestUtil.getOrderID());
        requestData.setRequest(request);
        validatePaymentRequest(defaultPaymentService, requestData);
        PageDetailsResponse pageDetailsResponse = defaultPaymentService.processPaymentRequest(requestData, null);
        TestResponseUtil.validateTestResponse(pageDetailsResponse, requestData, sessionDataService, testResource,
                DEFAULT_PAYMENT_TEST_RESPONSE_KEY + "2");
    }

    @Test
    public void testBuyerPaysChargeUserLoggedIn() {

        new MockUp<TheiaSessionDataServiceImpl>() {
            @Mock
            public DigitalCreditInfo getDigitalCreditInfoFromSession(HttpServletRequest request, boolean createNew) {
                DigitalCreditInfo digitalCreditInfo = new DigitalCreditInfo();
                digitalCreditInfo.setDigitalCreditEnabled(true);
                digitalCreditInfo.setExternalAccountNo("TestAccNo");
                digitalCreditInfo.setLenderId("TestLenderId");
                return digitalCreditInfo;
            }
        };

        LOGGER.info("Running test case for Default Payment Service {User Logged In , Charge Payer}!!!");
        String jsonRequest = testResource.getTestProperties().getProperty(DEFAULT_PAYMENT_TEST_REQUEST_KEY + "3");
        PaymentRequestBean requestData = TestRequestUtil.mapJsonToObject(testResource.getObjectMapper(), jsonRequest,
                PaymentRequestBean.class);
        requestData.setOrderId(TestRequestUtil.getOrderID());
        requestData.setRequest(request);
        validatePaymentRequest(defaultPaymentService, requestData);
        PageDetailsResponse pageDetailsResponse = defaultPaymentService.processPaymentRequest(requestData, null);
        TestResponseUtil.validateTestResponse(pageDetailsResponse, requestData, sessionDataService, testResource,
                DEFAULT_PAYMENT_TEST_RESPONSE_KEY + "3");
    }

    @Test
    public void testBuyerPaysChargeUserNotLoggedIn() {
        new MockUp<TheiaSessionDataServiceImpl>() {
            @Mock
            public DigitalCreditInfo getDigitalCreditInfoFromSession(HttpServletRequest request, boolean createNew) {
                DigitalCreditInfo digitalCreditInfo = new DigitalCreditInfo();
                digitalCreditInfo.setDigitalCreditEnabled(false);
                return digitalCreditInfo;
            }
        };

        LOGGER.info("Running test case for Default Payment Service {User Not Logged In , Charge Payer}!!!");
        String jsonRequest = testResource.getTestProperties().getProperty(DEFAULT_PAYMENT_TEST_REQUEST_KEY + "4");
        PaymentRequestBean requestData = TestRequestUtil.mapJsonToObject(testResource.getObjectMapper(), jsonRequest,
                PaymentRequestBean.class);
        requestData.setOrderId(TestRequestUtil.getOrderID());
        requestData.setRequest(request);
        validatePaymentRequest(defaultPaymentService, requestData);
        PageDetailsResponse pageDetailsResponse = defaultPaymentService.processPaymentRequest(requestData, null);
        TestResponseUtil.validateTestResponse(pageDetailsResponse, requestData, sessionDataService, testResource,
                DEFAULT_PAYMENT_TEST_RESPONSE_KEY + "4");
    }

    @Test
    public void testDefaultPaymentWithPromo() {

        new MockUp<PromoServiceHelperImpl>() {
            @Mock
            public PromoCodeResponse validatePromoCode(final PromoCodeBaseRequest promoCodeRequest) {
                PromoCodeResponse promoCodeResponse = new PromoCodeResponse();
                promoCodeResponse.setPromoResponseCode(ResponseCodeConstant.PROMO_SUCCESS);
                PromoCodeData promoCodeDetail = new PromoCodeData();
                Set<String> paymentModes = new HashSet<String>();
                paymentModes.add("CC");
                paymentModes.add("DC");
                promoCodeDetail.setPaymentModes(paymentModes);
                Set<Long> nbBanks = new HashSet<Long>();
                nbBanks.add(101L);
                nbBanks.add(102L);
                promoCodeDetail.setNbBanks(nbBanks);
                promoCodeResponse.setPromoCodeDetail(promoCodeDetail);
                return promoCodeResponse;
            }
        };

        new MockUp<TheiaSessionDataServiceImpl>() {
            @Mock
            public DigitalCreditInfo getDigitalCreditInfoFromSession(HttpServletRequest request, boolean createNew) {
                DigitalCreditInfo digitalCreditInfo = new DigitalCreditInfo();
                digitalCreditInfo.setDigitalCreditEnabled(true);
                digitalCreditInfo.setExternalAccountNo("TestAccNo");
                digitalCreditInfo.setLenderId("TestLenderId");
                return digitalCreditInfo;
            }
        };

        LOGGER.info("Running test case for Default Payment Service {Promo Code Applied}!!!");
        String jsonRequest = testResource.getTestProperties().getProperty(DEFAULT_PAYMENT_TEST_REQUEST_KEY + "5");
        PaymentRequestBean requestData = TestRequestUtil.mapJsonToObject(testResource.getObjectMapper(), jsonRequest,
                PaymentRequestBean.class);
        requestData.setOrderId(TestRequestUtil.getOrderID());
        requestData.setRequest(request);
        validatePaymentRequest(defaultPaymentService, requestData);
        PageDetailsResponse pageDetailsResponse = defaultPaymentService.processPaymentRequest(requestData, null);
        TestResponseUtil.validateTestResponse(pageDetailsResponse, requestData, sessionDataService, testResource,
                DEFAULT_PAYMENT_TEST_RESPONSE_KEY + "5");
    }

}
