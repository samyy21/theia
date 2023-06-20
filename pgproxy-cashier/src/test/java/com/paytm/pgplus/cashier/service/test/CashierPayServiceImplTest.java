package com.paytm.pgplus.cashier.service.test;

import com.paytm.pgplus.cashier.facade.service.IFacadeService;
import com.paytm.pgplus.cashier.pay.service.impl.CashierPayServiceImpl;
import com.paytm.pgplus.cashier.service.impl.PaymentServiceImpl;
import com.paytm.pgplus.facade.enums.ServiceUrl;
import mockit.MockUp;
import org.junit.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.h2.tools.Server;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.exception.CashierInvalidParameterException;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.pay.service.ICashierPayService;
import com.paytm.pgplus.cashier.service.test.builder.CashierRequestBuilderBankcard;
import com.paytm.pgplus.cashier.service.test.builder.CashierRequestBuilderSavecard;
import com.paytm.pgplus.facade.enums.AlipayServiceUrl;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.ConnectionUtil;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;

/**
 * @author Vivek Kumar
 */

public class CashierPayServiceImplTest {

    @Autowired
    CashierRequestBuilderBankcard cashierRequestBankcardBuilder = new CashierRequestBuilderBankcard();

    @Autowired
    CashierRequestBuilderSavecard cashierRequestSavedCardBuilder = new CashierRequestBuilderSavecard();

    @InjectMocks
    ICashierPayService cashierPayServiceImpl = new CashierPayServiceImpl();

    @Mock
    IFacadeService facadeService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(CashierPayServiceImplTest.class);

    @Test
    public void testSubmitPayForBankCard() {

        new MockUp<ConnectionUtil>() {
            @mockit.Mock
            public <T, U> T execute(final U request, final ServiceUrl api, final Class<T> clazz)
                    throws FacadeCheckedException {
                String alipayResponse = "";
                if (api == AlipayServiceUrl.PAYMENT_CASHIER_PAY) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.payment.cashier.pay\",\"clientId\":\"clientId\",\"reqMsgId\":\"89feb6226c064ca5b16609f51878022aadministratorthinkpadl450\",\"version\":\"1.1.5\",\"respTime\":\"2017-06-01T13:44:55+05:30\"},\"body\":{\"resultInfo\":{\"resultStatus\":\"A\",\"resultCodeId\":\"00000009\",\"resultMsg\":\"ACCEPTED_SUCCESS\",\"resultCode\":\"ACCEPTED_SUCCESS\"},\"requestId\":\"75e7df239f754e9abc0ad94940fec2f7administratorthinkpadl450\",\"cashierRequestId\":\"clientId75e7df239f754e9abc0ad94940fec2f7administratorthinkpadl450\"}},\"signature\":\"no_signature\"}";
                }
                String responseString = JsonMapper.getParamFromJson(alipayResponse, "response").toString();
                final T response = JsonMapper.mapJsonToObject(responseString, clazz);
                return response;
            }
        };

        try {
            CashierRequest cashierRequest = cashierRequestBankcardBuilder.getBankCardRequest();
            cashierRequest.getPaymentRequest().getPayBillOptions()
                    .setCardCacheToken("201706010551188b0d18abad17f319f68feb8a394f7c0");

            String cashierRequestId = cashierPayServiceImpl.submitPay(cashierRequest);
            Assert.assertEquals("clientId75e7df239f754e9abc0ad94940fec2f7administratorthinkpadl450", cashierRequestId);
        } catch (Exception e) {
            LOGGER.error("error: ", e);
        }
    }

    @Test
    public void testSubmitPayForSavedCard() {

        new MockUp<ConnectionUtil>() {
            @mockit.Mock
            public <T, U> T execute(final U request, final ServiceUrl api, final Class<T> clazz)
                    throws FacadeCheckedException {
                String alipayResponse = "";
                if (api == AlipayServiceUrl.PAYMENT_CASHIER_PAY) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.payment.cashier.pay\",\"clientId\":\"clientId\",\"reqMsgId\":\"03c9a378566d41b285d5dc201a653381administratorthinkpadl450\",\"version\":\"1.1.5\",\"respTime\":\"2017-06-01T16:21:14+05:30\"},\"body\":{\"resultInfo\":{\"resultStatus\":\"A\",\"resultCodeId\":\"00000009\",\"resultMsg\":\"ACCEPTED_SUCCESS\",\"resultCode\":\"ACCEPTED_SUCCESS\"},\"requestId\":\"6e3f918952f7462ca3c237705e198a35administratorthinkpadl450\",\"cashierRequestId\":\"clientId6e3f918952f7462ca3c237705e198a35administratorthinkpadl450\"}},\"signature\":\"no_signature\"}";
                }
                String responseString = JsonMapper.getParamFromJson(alipayResponse, "response").toString();
                final T response = JsonMapper.mapJsonToObject(responseString, clazz);
                return response;
            }
        };

        try {
            CashierRequest cashierRequest = cashierRequestSavedCardBuilder.getSavedCardRequest();
            cashierRequest.getPaymentRequest().getPayBillOptions()
                    .setCardCacheToken("20170601051114a610604cf39b8ec01728795bf347236");

            String cashierRequestId = cashierPayServiceImpl.submitPay(cashierRequest);
            Assert.assertEquals("clientId6e3f918952f7462ca3c237705e198a35administratorthinkpadl450", cashierRequestId);
        } catch (Exception e) {
            LOGGER.error("error: ", e);
        }
    }

    @Test(expected = CashierInvalidParameterException.class)
    public void testNullCashierRequest() throws PaytmValidationException, CashierCheckedException {
        cashierPayServiceImpl.submitPay(null);
    }

    @Test(expected = CashierCheckedException.class)
    public void testEmptyCashierRequestId() throws PaytmValidationException, CashierCheckedException {

        new MockUp<ConnectionUtil>() {
            @mockit.Mock
            public <T, U> T execute(final U request, final ServiceUrl api, final Class<T> clazz)
                    throws FacadeCheckedException {
                String alipayResponse = "";
                if (api == AlipayServiceUrl.PAYMENT_CASHIER_PAY) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.payment.cashier.pay\",\"clientId\":\"clientId\",\"reqMsgId\":\"89feb6226c064ca5b16609f51878022aadministratorthinkpadl450\",\"version\":\"1.1.5\",\"respTime\":\"2017-06-01T13:44:55+05:30\"},\"body\":{\"resultInfo\":{\"resultStatus\":\"A\",\"resultCodeId\":\"00000009\",\"resultMsg\":\"ACCEPTED_SUCCESS\",\"resultCode\":\"ACCEPTED_SUCCESS\"},\"requestId\":\"75e7df239f754e9abc0ad94940fec2f7administratorthinkpadl450\",\"cashierRequestId\":\"\"}},\"signature\":\"no_signature\"}";
                }
                String responseString = JsonMapper.getParamFromJson(alipayResponse, "response").toString();
                final T response = JsonMapper.mapJsonToObject(responseString, clazz);
                return response;
            }
        };

        CashierRequest cashierRequest = cashierRequestBankcardBuilder.getBankCardRequest();
        cashierRequest.getPaymentRequest().getPayBillOptions()
                .setCardCacheToken("201706010551188b0d18abad17f319f68feb8a394f7c0");
        String cashierRequestId = cashierPayServiceImpl.submitPay(cashierRequest);
        Assert.assertEquals("clientId75e7df239f754e9abc0ad94940fec2f7administratorthinkpadl450", cashierRequestId);

    }

    @Test(expected = CashierCheckedException.class)
    public void testInvalidTopupRequest() throws PaytmValidationException, CashierCheckedException {
        new MockUp<ConnectionUtil>() {
            @mockit.Mock
            public <T, U> T execute(final U request, final ServiceUrl api, final Class<T> clazz)
                    throws FacadeCheckedException {
                String alipayResponse = "";
                if (api == AlipayServiceUrl.PAYMENT_CASHIER_PAY) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.payment.cashier.pay\",\"clientId\":\"clientId\",\"reqMsgId\":\"89feb6226c064ca5b16609f51878022aadministratorthinkpadl450\",\"version\":\"1.1.5\",\"respTime\":\"2017-06-01T13:44:55+05:30\"},\"body\":{\"resultInfo\":{\"resultStatus\":\"A\",\"resultCodeId\":\"00000009\",\"resultMsg\":\"ACCEPTED_SUCCESS\",\"resultCode\":\"ACCEPTED_SUCCESS\"},\"requestId\":\"75e7df239f754e9abc0ad94940fec2f7administratorthinkpadl450\",\"cashierRequestId\":\"clientId75e7df239f754e9abc0ad94940fec2f7administratorthinkpadl450\"}},\"signature\":\"no_signature\"}";
                }
                String responseString = JsonMapper.getParamFromJson(alipayResponse, "response").toString();
                final T response = JsonMapper.mapJsonToObject(responseString, clazz);
                return response;
            }
        };

        CashierRequest cashierRequest = cashierRequestBankcardBuilder.getInvalidTopupBankcardRequest();
        cashierRequest.getPaymentRequest().getPayBillOptions()
                .setCardCacheToken("201706010551188b0d18abad17f319f68feb8a394f7c0");
        cashierRequest.getPaymentRequest();

        String cashierRequestId = cashierPayServiceImpl.submitPay(cashierRequest);
        Assert.assertEquals("clientId75e7df239f754e9abc0ad94940fec2f7administratorthinkpadl450", cashierRequestId);
    }

}
