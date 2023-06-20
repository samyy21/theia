package com.paytm.pgplus.cashier.service.test.payment.initiate;

import com.paytm.pglus.cashier.workflow.test.BaseWorkFlowTest;
import com.paytm.pgplus.facade.enums.ServiceUrl;
import mockit.Mock;
import mockit.MockUp;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.exception.SaveCardValidationException;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.service.impl.PaymentServiceImpl;
import com.paytm.pgplus.cashier.service.test.builder.AddMoneyCashierRequestBuilderNetbanking;
import com.paytm.pgplus.cashier.workflow.NetbankingWorkflow;
import com.paytm.pgplus.facade.enums.AlipayServiceUrl;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.ConnectionUtil;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;

/**
 * @author Vivek Kumar
 */

public class AddMoneyNetbankingWorkflowExceptionTest extends BaseCashierWorkFlowTest {

    @Autowired
    PaymentServiceImpl paymentServiceImpl;

    @Autowired
    AddMoneyCashierRequestBuilderNetbanking cashierRequestBuilder = new AddMoneyCashierRequestBuilderNetbanking();

    @Autowired
    NetbankingWorkflow NetbankingWorkflow;

    @Test(expected = CashierCheckedException.class)
    public void testNullCashierRequest() throws PaytmValidationException, CashierCheckedException {
        NetbankingWorkflow.initiatePayment(null);
    }

    @Test(expected = CashierCheckedException.class)
    public void testNullPaymentRequest() throws CashierCheckedException, SaveCardValidationException,
            PaytmValidationException {
        CashierRequest cashierRequest = cashierRequestBuilder.getNetbankingRequest();
        cashierRequest.setPaymentRequest(null);
        NetbankingWorkflow.initiatePayment(cashierRequest);

    }

    @Test
    public void testEmptyCashierRequestId() throws CashierCheckedException, SaveCardValidationException,
            PaytmValidationException {
        new MockUp<ConnectionUtil>() {
            @Mock
            public <T, U> T execute(final U request, final ServiceUrl api, final Class<T> clazz)
                    throws FacadeCheckedException {
                String alipayResponse = "";
                if (api == AlipayServiceUrl.PAYMENT_CASHIER_PAY) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.payment.cashier.pay\",\"clientId\":\"clientId\",\"reqMsgId\":\"fb123fb74259431287fd3d3638d1b76cadministratorthinkpadl450\",\"version\":\"1.1.8\",\"respTime\":\"2017-06-08T16:16:06+05:30\"},\"body\":{\"resultInfo\":{\"resultStatus\":\"F\",\"resultCodeId\":\"00000004\",\"resultMsg\":\"Illegal parameters\",\"resultCode\":\"PARAM_ILLEGAL\"},\"requestId\":\"f1fd0d30ead74701a8f044245bda7b08administratorthinkpadl450\",\"cashierRequestId\":\"\"}},\"signature\":\"no_signature\"}";
                }
                String responseString = JsonMapper.getParamFromJson(alipayResponse, "response").toString();
                final T response = JsonMapper.mapJsonToObject(responseString, clazz);
                return response;
            }
        };

        CashierRequest cashierRequest = cashierRequestBuilder.getNetbankingRequest();
        paymentServiceImpl.initiate(cashierRequest);
    }

}
