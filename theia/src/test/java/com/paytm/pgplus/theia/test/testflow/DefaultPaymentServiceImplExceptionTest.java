package com.paytm.pgplus.theia.test.testflow;

import com.paytm.pgplus.biz.exception.BizMerchantVelocityBreachedException;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.enums.RetryStatus;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.promo.service.client.model.PromoCodeData;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.services.impl.DefaultPaymentServiceImpl;
import com.paytm.pgplus.theia.services.impl.TheiaSessionDataServiceImpl;
import com.paytm.pgplus.theia.sessiondata.DigitalCreditInfo;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.promo.service.client.model.PromoCodeBaseRequest;
import com.paytm.pgplus.promo.service.client.model.PromoCodeResponse;
import com.paytm.pgplus.promo.service.client.service.impl.PromoServiceHelperImpl;
import com.paytm.pgplus.theia.constants.ResponseCodeConstant;
import com.paytm.pgplus.theia.datamapper.impl.BizRequestResponseMapperImpl;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.services.IPaymentService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.test.util.TestRequestUtil;

import mockit.Mock;
import mockit.MockUp;

import javax.servlet.http.HttpServletRequest;

/**
 * @author kartik
 * @date 09-06-2017
 */
public class DefaultPaymentServiceImplExceptionTest extends AbstractPaymentServiceTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Autowired
    @Qualifier(value = "defaultPaymentService")
    private IPaymentService defaultPaymentService;

    @Autowired
    @Qualifier(value = "theiaSessionDataService")
    private ITheiaSessionDataService sessionDataService;

    private static final String DEFAULT_PAYMENT_TEST_REQUEST_KEY = "default.payment.flow.exception.test.request.";

    @Test
    public void testDefaultPaymentWithInvalidPromo() {

        new MockUp<PromoServiceHelperImpl>() {
            @Mock
            public PromoCodeResponse validatePromoCode(final PromoCodeBaseRequest promoCodeRequest) {
                PromoCodeResponse promoCodeResponse = new PromoCodeResponse();
                promoCodeResponse.setPromoResponseCode(ResponseCodeConstant.PROMO_INVALID);
                PromoCodeData promoCodeDetail = new PromoCodeData();
                promoCodeDetail.setPromoCode("");
                promoCodeResponse.setPromoCodeDetail(promoCodeDetail);
                return promoCodeResponse;
            }
        };

        LOGGER.info("Running test case for Default Payment Service {Invalid Promo Code}!!!");
        String jsonRequest = testResource.getTestProperties().getProperty(DEFAULT_PAYMENT_TEST_REQUEST_KEY + "1");
        PaymentRequestBean requestData = TestRequestUtil.mapJsonToObject(testResource.getObjectMapper(), jsonRequest,
                PaymentRequestBean.class);
        requestData.setOrderId(TestRequestUtil.getOrderID());
        requestData.setRequest(request);
        validatePaymentRequest(defaultPaymentService, requestData);
        defaultPaymentService.processPaymentRequest(requestData, null);
    }

    @Test(expected = TheiaServiceException.class)
    public void testDefaultPaymentNoWorkFlowRequest() {

        new MockUp<BizRequestResponseMapperImpl>() {
            @Mock
            public WorkFlowRequestBean mapWorkFlowRequestData(final PaymentRequestBean requestData) {
                return null;
            }
        };

        LOGGER.info("Running test case for Default Payment Service {No WorkFlowRequest bean}!!!");
        PaymentRequestBean requestData = new PaymentRequestBean();
        defaultPaymentService.processPaymentRequest(requestData, null);
    }

    @Test
    public void testDefaultPaymentInvalidMerchantId() throws PaymentRequestValidationException {
        LOGGER.info("Running test case for Default Payment Service {Invalid MID}!!!");
        String jsonRequest = testResource.getTestProperties().getProperty(DEFAULT_PAYMENT_TEST_REQUEST_KEY + "1");
        PaymentRequestBean requestData = TestRequestUtil.mapJsonToObject(testResource.getObjectMapper(), jsonRequest,
                PaymentRequestBean.class);
        requestData.setOrderId(TestRequestUtil.getOrderID());
        requestData.setRequest(request);
        requestData.setMid("InvalidMid");
        exceptionRule.expect(PaymentRequestValidationException.class);
        exceptionRule
                .expectMessage("Error while fetching merchantMapping Detail from mapping service for MID:InvalidMid");
        defaultPaymentService.processPaymentRequest(requestData, null);
    }

    @Test
    public void testDefaultPaymentProcessWorkflowResponseToMerchantTrue() {
        new MockUp<DefaultPaymentServiceImpl>() {
            @Mock
            public GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(
                    WorkFlowRequestBean workFlowRequestBean) {
                GenericCoreResponseBean genericCoreResponseBean = new GenericCoreResponseBean("",
                        ResponseConstants.INVALID_CUST_ID, "");
                return genericCoreResponseBean;
            }
        };
        LOGGER.info("Running test case for Default Payment Service response to merchant true!");
        String jsonRequest = testResource.getTestProperties().getProperty(DEFAULT_PAYMENT_TEST_REQUEST_KEY + "1");
        PaymentRequestBean requestData = TestRequestUtil.mapJsonToObject(testResource.getObjectMapper(), jsonRequest,
                PaymentRequestBean.class);
        requestData.setOrderId(TestRequestUtil.getOrderID());
        requestData.setRequest(request);
        PageDetailsResponse response = defaultPaymentService.processPaymentRequest(requestData, null);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getHtmlPage());
    }

    @Test
    public void testDefaultPaymentProcessMerchantLimitBreached() {
        new MockUp<DefaultPaymentServiceImpl>() {
            @Mock
            public GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(
                    WorkFlowRequestBean workFlowRequestBean) {
                WorkFlowResponseBean responseBean = new WorkFlowResponseBean();
                return new GenericCoreResponseBean("Merchant limit is breached");
            }
        };
        LOGGER.info("Running test case for Default Payment Service Merchant limit is breached!");
        String jsonRequest = testResource.getTestProperties().getProperty(DEFAULT_PAYMENT_TEST_REQUEST_KEY + "1");
        PaymentRequestBean requestData = TestRequestUtil.mapJsonToObject(testResource.getObjectMapper(), jsonRequest,
                PaymentRequestBean.class);
        requestData.setOrderId(TestRequestUtil.getOrderID());
        requestData.setRequest(request);
        exceptionRule.expect(BizMerchantVelocityBreachedException.class);
        defaultPaymentService.processPaymentRequest(requestData, null);
    }
}
