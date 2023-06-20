package com.paytm.pgplus.theia.test.testflow;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.services.IJsonResponsePaymentService;
import com.paytm.pgplus.theia.test.util.TestRequestUtil;
import com.paytm.pgplus.theia.test.util.TestResponseUtil;

/**
 * 
 * @author vaishakhnair
 * 
 */
public class SeamlessACSPaymentServiceImplTest extends AbstractPaymentServiceTest {

    private static final String SEAMLESS_ACS_PAYMENT_TEST_REQUEST_KEY = "seamless.acs.payment.flow.success.test.request.";
    private static final String SEAMLESS_ACS_PAYMENT_TEST_RESPONSE_KEY = "seamless.acs.payment.flow.success.test.response.";

    @Autowired
    @Qualifier(value = "seamlessACSPaymentService")
    private IJsonResponsePaymentService seamlessACSPaymentService;

    @Test
    public void testSeamlessAcsPaymentInvalidMID() throws Exception {
        LOGGER.info("Running test case for Seamless ACS Payment Service {Invalid MID}!!!");
        test("invalidmid");
    }

    @Test
    public void testSeamlessAcsPaymentInvalidAmount() throws Exception {
        LOGGER.info("Running test case for Seamless ACS Payment Service {Invalid Amount}!!!");
        test("invalidamt");
    }

    @Test
    public void testSeamlessAcsPaymentInvalidCardNo() throws Exception {
        LOGGER.info("Running test case for Seamless ACS Payment Service {Invalid Card No}!!!");
        test("invalidcardno");
    }

    @Test
    public void testSeamlessAcsPaymentInvalidExpiry() throws Exception {
        LOGGER.info("Running test case for Seamless ACS Payment Service {Invalid Expiry}!!!");
        test("invalidexpiry");
    }

    @Test
    public void testSeamlessAcsPaymentInvalidPaymentMode() throws Exception {
        LOGGER.info("Running test case for Seamless ACS Payment Service {Invalid Payment Mode}!!!");
        test("invalidpaymode");
    }

    @Test
    public void testSeamlessAcsPaymentEmptyPaymentMode() throws Exception {
        LOGGER.info("Running test case for Seamless ACS Payment Service {Empty Payment mode}!!!");
        test("emptypaymode");
    }

    @Test
    public void testSeamlessAcsPaymentHappyCase() throws Exception {
        LOGGER.info("Running test case for Seamless ACS Payment Service {Happy Case}!!!");
        test("successrequest");
    }

    @Test
    public void testSeamlessAcsPaymentDuplicateOrderId() throws Exception {
        System.setProperty("acquiringOrderCreateOrderAndPayCustomBehaviour", ".duplicateOrderId");
        LOGGER.info("Running test case for Seamless ACS Payment Service {Dupliacte Order ID}!!!");
        test("duplicateorderid");
        System.clearProperty("acquiringOrderCreateOrderAndPayCustomBehaviour");
    }

    @Test
    public void testSeamlessAcsPaymentEmptyCustId() throws Exception {
        LOGGER.info("Running test case for Seamless ACS Payment Service {Empty Cust ID}!!!");
        test("emptycustid");
    }

    private void test(String key) throws Exception {
        String jsonRequest = testResource.getTestProperties().getProperty(SEAMLESS_ACS_PAYMENT_TEST_REQUEST_KEY + key);
        PaymentRequestBean requestData = TestRequestUtil.mapJsonToObject(testResource.getObjectMapper(), jsonRequest,
                PaymentRequestBean.class);
        requestData.setRequest(request);
        try {
            validatePaymentRequest(seamlessACSPaymentService, requestData);
            WorkFlowResponseBean workflowResponseBean = seamlessACSPaymentService.processPaymentRequest(requestData);
            TestResponseUtil.validateTestResponseForSeamlessACS(workflowResponseBean, testResource,
                    SEAMLESS_ACS_PAYMENT_TEST_RESPONSE_KEY + key);
        } catch (Exception e) {
            LOGGER.error("error: ", e);
            throw e;
        }
    }
}
