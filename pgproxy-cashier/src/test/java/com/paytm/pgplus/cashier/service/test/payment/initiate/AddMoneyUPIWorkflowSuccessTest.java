package com.paytm.pgplus.cashier.service.test.payment.initiate;

import com.paytm.pgplus.cashier.workflow.UPIWorkFlow;
import com.paytm.pgplus.facade.enums.ServiceUrl;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;

import mockit.Mocked;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.InitiatePaymentResponse;
import com.paytm.pgplus.cashier.service.impl.PaymentServiceImpl;
import com.paytm.pgplus.cashier.service.test.builder.AddMoneyCashierRequestBuilderUPI;
import com.paytm.pgplus.facade.enums.AlipayServiceUrl;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.ConnectionUtil;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.cashier.util.SavedCardUtilService;

/**
 * @author Vivek Kumar
 */

public class AddMoneyUPIWorkflowSuccessTest extends BaseCashierWorkFlowTest {

    @Autowired
    PaymentServiceImpl paymentServiceImpl;

    @Autowired
    AddMoneyCashierRequestBuilderUPI cashierUpiRequest;

    @Autowired
    @Qualifier("UPIWorkflow")
    UPIWorkFlow upiWorkFlow;

    @Mocked
    SavedCardUtilService savedCardUtilService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AddMoneyUPIWorkflowSuccessTest.class);

    @Test
    public void testValidUPIRequest() throws CashierCheckedException {

        new MockUp<ConnectionUtil>() {
            @Mock
            public <T, U> T execute(final U request, final ServiceUrl api, final Class<T> clazz)
                    throws FacadeCheckedException {
                String alipayResponse = "";
                if (api == AlipayServiceUrl.PAYMENT_CASHIER_PAY) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.payment.cashier.pay\",\"clientId\":\"clientId\",\"reqMsgId\":\"aab1db6a191e408fa8548d60222ca67dadministratorthinkpadl450\",\"version\":\"1.1.8\",\"respTime\":\"2017-06-12T17:32:49+05:30\"},\"body\":{\"resultInfo\":{\"resultStatus\":\"A\",\"resultCodeId\":\"00000009\",\"resultMsg\":\"ACCEPTED_SUCCESS\",\"resultCode\":\"ACCEPTED_SUCCESS\"},\"requestId\":\"c11e62c5fd7948028ccfa85a2debdeb9administratorthinkpadl450\",\"cashierRequestId\":\"clientIdc11e62c5fd7948028ccfa85a2debdeb9administratorthinkpadl450\"}},\"signature\":\"no_signature\"}";
                }
                if (api == AlipayServiceUrl.PAYMENT_CASHIER_PAYRESULT_QUERY) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.payment.cashier.payresult.query\",\"clientId\":\"clientId\",\"reqMsgId\":\"75739ea6063a468b992b7acf64b5ce28administratorthinkpadl450\",\"version\":\"1.1.5\",\"respTime\":\"2017-06-12T17:33:19+05:30\"},\"body\":{\"webFormContext\":\"{\\\"vpa\\\":\\\"Paytm@icici\\\",\\\"txnAmount\\\":\\\"2.0\\\"}\",\"paymentStatus\":\"REDIRECT\",\"payOptionInfos\":[{\"payAmount\":{\"value\":\"200\",\"currency\":\"INR\"},\"extendInfo\":\"{\\\"instId\\\":\\\"UPI\\\"}\",\"payMethod\":\"UPI\"}],\"transType\":\"TOP_UP\",\"resultInfo\":{\"resultCodeId\":\"00000000\",\"resultMsg\":\"SUCCESS\",\"resultCode\":\"SUCCESS\",\"resultStatus\":\"S\"},\"transAmount\":{\"value\":\"200\",\"currency\":\"INR\"},\"payerUserId\":\"216810000000000128283\",\"transId\":\"2017061220121481010100166282800034536\",\"extendInfo\":\"{\\\"virtualPaymentAddr\\\":\\\"8948254780@upi\\\",\\\"txnType\\\":\\\"ONLY_PG\\\",\\\"productCode\\\":\\\"51053000100000000001\\\",\\\"ssoToken\\\":\\\"220dd1bd-69b6-4e7f-ab8a-f2c7a35024ff\\\",\\\"userEmail\\\":\\\"tarsnghl@gmail.com\\\",\\\"merchantTransId\\\":\\\"PARCEL858106\\\",\\\"paytmMerchantId\\\":\\\"master86636472935906\\\",\\\"alipayMerchantId\\\":\\\"216820000000139542205\\\",\\\"userMobile\\\":\\\"9899267758\\\",\\\"PAYTM_USER_ID\\\":\\\"10797790\\\",\\\"merchantName\\\":\\\"master\\\",\\\"mccCode\\\":\\\"Retail\\\",\\\"totalTxnAmount\\\":\\\"2.0\\\"}\"}},\"signature\":\"no_signature\"}";
                }
                String responseString = JsonMapper.getParamFromJson(alipayResponse, "response").toString();
                final T response = JsonMapper.mapJsonToObject(responseString, clazz);
                return response;
            }
        };

        try {
            CashierRequest cashierRequest = cashierUpiRequest.getUPIRequest();
            GenericCoreResponseBean<InitiatePaymentResponse> initiateResponse = paymentServiceImpl
                    .initiate(cashierRequest);
            Assert.assertNotNull(initiateResponse.isSuccessfullyProcessed());
        } catch (PaytmValidationException e) {
            LOGGER.error("Exception Occurred : {} ", e);
        }
    }

    @Test
    public void test_saveVpa_saveChannelInfoAfterPayTrue() {

        new Expectations() {
            {
                try {
                    savedCardUtilService.cacheCardData((CashierRequest) any);
                    times = 1;
                } catch (CashierCheckedException e) {
                    Assert.fail();
                }
            }
        };
        try {
            CashierRequest cashierRequest = cashierUpiRequest.getSaveVpaRequest(true);
            InitiatePaymentResponse response = upiWorkFlow.initiatePayment(cashierRequest);
            Assert.assertNotNull(response);
        } catch (PaytmValidationException | CashierCheckedException e) {
            Assert.fail();
        }

    }

    @Test
    public void test_saveVpa_saveChannelInfoAfterPayFalse() {

        new Expectations() {
            {
                try {
                    savedCardUtilService.cacheCardData((CashierRequest) any);
                    times = 0;
                } catch (CashierCheckedException e) {
                    Assert.fail();
                }
            }
        };
        try {
            CashierRequest cashierRequest = cashierUpiRequest.getSaveVpaRequest(false);
            InitiatePaymentResponse response = upiWorkFlow.initiatePayment(cashierRequest);
            Assert.assertNotNull(response);
        } catch (PaytmValidationException | CashierCheckedException e) {
            Assert.fail();
        }

    }

}
