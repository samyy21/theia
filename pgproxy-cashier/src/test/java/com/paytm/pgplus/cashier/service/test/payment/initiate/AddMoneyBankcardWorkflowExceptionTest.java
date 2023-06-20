package com.paytm.pgplus.cashier.service.test.payment.initiate;

import com.paytm.pglus.cashier.workflow.test.BaseWorkFlowTest;
import com.paytm.pgplus.facade.enums.ServiceUrl;
import mockit.Mock;
import mockit.MockUp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.service.impl.PaymentServiceImpl;
import com.paytm.pgplus.cashier.service.test.builder.AddMoneyCashierRequestBuilderBankcard;
import com.paytm.pgplus.cashier.workflow.BankcardWorkflow;
import com.paytm.pgplus.facade.enums.AlipayServiceUrl;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.ConnectionUtil;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;

/**
 * @author Vivek Kumar
 */

public class AddMoneyBankcardWorkflowExceptionTest extends BaseCashierWorkFlowTest {

    @Autowired
    AddMoneyCashierRequestBuilderBankcard cashierBankcardRequest;

    @Autowired
    PaymentServiceImpl paymentServiceImpl;

    @Autowired
    BankcardWorkflow BankcardWorkflow;

    @BeforeClass
    public static void initailize() {

        String rmiServerHost = System.getProperty("java.rmi.server.hostname", null);
        if (rmiServerHost == null) {
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        }
    }

    @Test(expected = CashierCheckedException.class)
    public void testNullCashierRequest() throws PaytmValidationException, CashierCheckedException {
        BankcardWorkflow.initiatePayment(null);
    }

    @Test(expected = CashierCheckedException.class)
    public void testNullCardRequest() throws PaytmValidationException, CashierCheckedException {
        CashierRequest cashierRequest = cashierBankcardRequest.getBankcardRequest();
        cashierRequest.setCardRequest(null);
        BankcardWorkflow.initiatePayment(cashierRequest);
    }

    @Test(expected = PaytmValidationException.class)
    public void testEmptyToken() throws PaytmValidationException, CashierCheckedException {
        new MockUp<ConnectionUtil>() {
            @Mock
            public <T, U> T execute(final U request, final ServiceUrl api, final Class<T> clazz)
                    throws FacadeCheckedException {
                String alipayResponse = "";
                if (api == AlipayServiceUrl.USER_ASSET_CACHE_CARD) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.user.asset.cacheCard\",\"clientId\":\"clientId\",\"reqMsgId\":\"8a009e177d2543cebbcc7cd6916d852aadministratorthinkpadl450\",\"version\":\"1.1.5\",\"respTime\":\"2017-06-09T17:06:25+05:30\"},\"body\":{\"maskedCardNo\":\"416021******1737\",\"resultInfo\":{\"resultCodeId\":\"00000000\",\"resultMsg\":\"SUCCESS\",\"resultStatus\":\"S\",\"resultCode\":\"SUCCESS\"},\"tokenId\":\"\",\"cardIndexNo\":\"2017010500510a65e478de7fc6f64b005c9ef855f4cbf\"}},\"signature\":\"no_signature\"}";
                }
                String responseString = JsonMapper.getParamFromJson(alipayResponse, "response").toString();
                final T response = JsonMapper.mapJsonToObject(responseString, clazz);
                return response;
            }
        };

        CashierRequest cashierRequest = cashierBankcardRequest.getBankcardRequest();
        paymentServiceImpl.initiate(cashierRequest);
    }

    @Test(expected = PaytmValidationException.class)
    public void testEmptyCashierRequestId() throws PaytmValidationException, CashierCheckedException {
        new MockUp<ConnectionUtil>() {
            @Mock
            public <T, U> T execute(final U request, final ServiceUrl api, final Class<T> clazz)
                    throws FacadeCheckedException {
                String alipayResponse = "";
                if (api == AlipayServiceUrl.USER_ASSET_CACHE_CARD) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.user.asset.cacheCard\",\"clientId\":\"clientId\",\"reqMsgId\":\"8a009e177d2543cebbcc7cd6916d852aadministratorthinkpadl450\",\"version\":\"1.1.5\",\"respTime\":\"2017-06-09T17:06:25+05:30\"},\"body\":{\"maskedCardNo\":\"416021******1737\",\"resultInfo\":{\"resultCodeId\":\"00000000\",\"resultMsg\":\"SUCCESS\",\"resultStatus\":\"S\",\"resultCode\":\"SUCCESS\"},\"tokenId\":\"20170609055114fecd11e1cfbe183e32d96e6bf18e56c\",\"cardIndexNo\":\"2017010500510a65e478de7fc6f64b005c9ef855f4cbf\"}},\"signature\":\"no_signature\"}";
                }
                if (api == AlipayServiceUrl.PAYMENT_CASHIER_PAY) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.payment.cashier.pay\",\"clientId\":\"clientId\",\"reqMsgId\":\"fe2b2d9805d74968a3525ac574920fd0administratorthinkpadl450\",\"version\":\"1.1.8\",\"respTime\":\"2017-06-09T17:07:53+05:30\"},\"body\":{\"resultInfo\":{\"resultStatus\":\"F\",\"resultCodeId\":\"00000004\",\"resultMsg\":\"Illegal parameters\",\"resultCode\":\"PARAM_ILLEGAL\"},\"requestId\":\"786d081ee96342e1a6705e5b92e41605administratorthinkpadl450\",\"cashierRequestId\":\"\"}},\"signature\":\"no_signature\"}";
                }
                String responseString = JsonMapper.getParamFromJson(alipayResponse, "response").toString();
                final T response = JsonMapper.mapJsonToObject(responseString, clazz);
                return response;
            }
        };

        CashierRequest cashierRequest = cashierBankcardRequest.getBankcardRequest();
        paymentServiceImpl.initiate(cashierRequest);
    }

}
