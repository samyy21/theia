package com.paytm.pgplus.theia.test.testflow;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.paytm.pgplus.biz.core.model.request.BizCreateOrderResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.SuccessRateQueryRequestBean;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.models.response.SuccessRateQueryResponseBean;
import com.paytm.pgplus.theia.services.IPaymentService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.impl.SuccessRateQueryServiceImpl;
import com.paytm.pgplus.theia.test.util.TestRequestUtil;
import com.paytm.pgplus.theia.test.util.TestResponseUtil;

import mockit.Mock;
import mockit.MockUp;

/**
 * @author kartik
 * @date 08-06-2017
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DefaultPaymentServiceImplFailureTest extends AbstractPaymentServiceTest {

    private static final String DEFAULT_PAYMENT_TEST_REQUEST_KEY = "default.payment.flow.failure.test.request.";
    private static final String DEFAULT_PAYMENT_TEST_RESPONSE_KEY = "default.payment.flow.failure.test.response.";

    @Autowired
    @Qualifier(value = "defaultPaymentService")
    private IPaymentService defaultPaymentService;

    @Autowired
    @Qualifier(value = "theiaSessionDataService")
    private ITheiaSessionDataService sessionDataService;

    @Test
    public void order1_testDefaultFlowInvalidAmount() {
        LOGGER.info("Running test case for Default Payment Service {Invalid Txn Amount}!!!");
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
    public void order3_testDefaultFlowSuccessIdempotentError() {

        new MockUp<WorkFlowHelper>() {
            @Mock
            public GenericCoreResponseBean<BizCreateOrderResponse> createOrder(
                    WorkFlowTransactionBean workFlowTransBean, boolean isTimeOutZero) {
                GenericCoreResponseBean<BizCreateOrderResponse> bizCreateOrderResponse = new GenericCoreResponseBean<BizCreateOrderResponse>(
                        "repeated submit, the business is success already.", ResponseConstants.SUCCESS_IDEMPOTENT_ERROR);
                return bizCreateOrderResponse;
            }
        };

        LOGGER.info("Running test case for Default Payment Service {Success Idempotent Error}!!!");
        String jsonRequest = testResource.getTestProperties().getProperty(DEFAULT_PAYMENT_TEST_REQUEST_KEY + "2");
        PaymentRequestBean requestData = TestRequestUtil.mapJsonToObject(testResource.getObjectMapper(), jsonRequest,
                PaymentRequestBean.class);
        requestData.setRequest(request);
        validatePaymentRequest(defaultPaymentService, requestData);
        PageDetailsResponse pageDetailsResponse = defaultPaymentService.processPaymentRequest(requestData, null);
        TestResponseUtil.validateTestResponse(pageDetailsResponse, requestData, sessionDataService, testResource,
                DEFAULT_PAYMENT_TEST_RESPONSE_KEY + "2");

    }

    @Test
    public void order2_testDefaultFlowSuccessRateQueryFailure() {

        new MockUp<SuccessRateQueryServiceImpl>() {
            @Mock
            public SuccessRateQueryResponseBean getSuccessRatesForPayMethod(
                    SuccessRateQueryRequestBean successRateQueryRequestBean) {
                throw new TheiaServiceException("Success Rate query failed!");
            }
        };

        LOGGER.info("Running test case for Default Payment Service {Success Rate Query Failure}!!!");
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

}
