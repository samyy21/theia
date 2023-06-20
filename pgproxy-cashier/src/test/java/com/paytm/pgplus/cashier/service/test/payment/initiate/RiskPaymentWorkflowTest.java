package com.paytm.pgplus.cashier.service.test.payment.initiate;

import com.paytm.pgplus.facade.enums.ServiceUrl;
import mockit.Mock;
import mockit.MockUp;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.InitiatePaymentResponse;
import com.paytm.pgplus.cashier.service.impl.PaymentServiceImpl;
import com.paytm.pgplus.cashier.service.test.builder.CashierRequestBuilderRiskPayment;
import com.paytm.pgplus.facade.enums.AlipayServiceUrl;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.ConnectionUtil;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author Vivek Kumar
 */

public class RiskPaymentWorkflowTest extends BaseCashierWorkFlowTest {

    @Autowired
    PaymentServiceImpl paymentServiceImpl;

    @Autowired
    CashierRequestBuilderRiskPayment cashierRequestRiskPayment;

    private static final Logger LOGGER = LoggerFactory.getLogger(RiskPaymentWorkflowTest.class);

    @BeforeClass
    public static void initailize() {

        String rmiServerHost = System.getProperty("java.rmi.server.hostname", null);
        if (rmiServerHost == null) {
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        }
    }

    @Test
    public void testRiskAcceptRequest() throws CashierCheckedException {

        new MockUp<ConnectionUtil>() {
            @Mock
            public <T, U> T execute(final U request, final ServiceUrl api, final Class<T> clazz)
                    throws FacadeCheckedException {
                String alipayResponse = "";
                if (api == AlipayServiceUrl.USER_ASSET_CACHE_CARD) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.user.asset.cacheCard\",\"clientId\":\"clientId\",\"reqMsgId\":\"090184c83c444a179b6512c3d265c853administratorthinkpadl450\",\"version\":\"1.1.5\",\"respTime\":\"2017-06-13T13:34:18+05:30\"},\"body\":{\"maskedCardNo\":\"360886****5515\",\"resultInfo\":{\"resultCodeId\":\"00000000\",\"resultMsg\":\"SUCCESS\",\"resultStatus\":\"S\",\"resultCode\":\"SUCCESS\"},\"tokenId\":\"2017061305211871977e9e2eb61e013f0a4370e5d6407\",\"cardIndexNo\":\"2016122800210c00c685968cff8475507d006759e7f95\"}},\"signature\":\"no_signature\"}";
                }
                if (api == AlipayServiceUrl.RISK_POLICY_CONSULT) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.risk.policy.consult\",\"clientId\":\"clientId\",\"reqMsgId\":\"5febc84a5ab1441294b87586f4442de4administratorthinkpadl450\",\"version\":\"1.2\",\"respTime\":\"2017-06-13T13:35:31+05:30\"},\"body\":{\"riskResult\":\"ACCEPT\",\"resultInfo\":{\"resultMsg\":\"SUCCESS\",\"resultCodeId\":\"00000000\",\"resultStatus\":\"S\",\"resultCode\":\"SUCCESS\"},\"securityId\":\"sid4d63b8ed812b4589d3ad7d7b3eced9e3\"}},\"signature\":\"no_signature\"}";
                }
                if (api == AlipayServiceUrl.PAYMENT_CASHIER_PAY) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.payment.cashier.pay\",\"clientId\":\"clientId\",\"reqMsgId\":\"c05cf1b7a50b487d94e022ec5a337562administratorthinkpadl450\",\"version\":\"1.1.8\",\"respTime\":\"2017-06-13T13:53:29+05:30\"},\"body\":{\"resultInfo\":{\"resultStatus\":\"A\",\"resultCodeId\":\"00000009\",\"resultMsg\":\"ACCEPTED_SUCCESS\",\"resultCode\":\"ACCEPTED_SUCCESS\"},\"requestId\":\"ecc3b64235814a40ad7d04e010c4ad10administratorthinkpadl450\",\"cashierRequestId\":\"clientIdecc3b64235814a40ad7d04e010c4ad10administratorthinkpadl450\"}},\"signature\":\"no_signature\"}";
                }
                if (api == AlipayServiceUrl.PAYMENT_CASHIER_PAYRESULT_QUERY) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.payment.cashier.payresult.query\",\"clientId\":\"clientId\",\"reqMsgId\":\"4dc512eefc0c4fc2b23d8762b9b3219cadministratorthinkpadl450\",\"version\":\"1.1.5\",\"respTime\":\"2017-06-13T13:55:25+05:30\"},\"body\":{\"webFormContext\":\"<!DOCTYPE html PUBLIC \\\"-//W3C//DTD HTML 4.01 Transitional//EN\\\" \\\"http://www.w3.org/TR/html4/loose.dtd\\\">\\n<html>\\n\\n<head>\\n<meta http-equiv=\\\"Content-Type\\\" content=\\\"text/html; charset=ISO-8859-1\\\">\\n<title>Paytm</title>\\n\\n</head>\\n<body >\\n\\n <table align='center'>\\n  <tr><td><STRONG>Transaction is being processed,</STRONG></td></tr>\\n  <tr><td><font color='blue'>Please wait ...</font></td></tr>\\n  <tr><td>(Please do not press 'Refresh' or 'Back' button</td></tr>\\n </table>\\n<FORM NAME='frm' ACTION='https://netsafe.hdfcbank.com/ACSWeb/com.enstage.entransact.servers.AccessControlServerSSL?ty=D' method='post'>\\n\\n\\t<input type=\\\"hidden\\\" name=\\\"PaReq\\\" value=\\\"eJxVkdtygkAMhl+F4b5kOQjihHVo6QGnHsbiWC+361aYEVBYRd++rEpt7/Jtsn+SPzg85VvtKKo6K4tANw2ia6Lg5TorNoG+SF4e+vqQYpJWQkQfgh8qQXEs6ppthJatA10el5nfLOQ0mZAkWUX5xikXoyb9dFeBTnEWzsWe4q0BbfUNC6HDVqniKSskRcb3j/GE2m7PJQThhpiLKo6o7/l+H+EKWLBc0Fm4SsYIlxh5eShkdaZ2z0XoAA/VlqZS7gYATdMYO3aWucHLHBBUCuHefXZQUd1KnbI1FZMvxsKwGNvpqzsW5VS+e9+rRTSNnwMEVYFrJgW1iOkR17Q1Yg569sA2ES7vyHI1A53XlkH8dpkr4k51Ca9gqcTfB2y9rVrruy06QnHalYVQXxB+Y4T7yE9vyj4uW28cvnRPiZ+PHB73Tcf3bMeyTJN4rrL0UqL0MuUVId5FUAGCEoHbteB24Db6d/gfiwa2xA==\\\">\\t\\t\\n\\t<input type=\\\"hidden\\\" name=\\\"MD\\\" value=\\\"2941795531371640\\\">\\n\\t<input type=\\\"hidden\\\" name=\\\"TermUrl\\\" value=\\\"http://52.76.10.37/instaproxy/bankresponse/HDFC/CC/90170613000006728782\\\">\\n\\n</FORM>\\n</body>\\n<script type=\\\"text/javascript\\\">\\n\\tif(window.top !== window){\\t\\n\\t\\tdocument.forms[0].target=\\\"_parent\\\";\\n\\t}\\n\\tdocument.forms[0].submit();\\n</script>\\t  \\n</html>\",\"paymentStatus\":\"REDIRECT\",\"payOptionInfos\":[{\"payAmount\":{\"value\":\"209\",\"currency\":\"INR\"},\"extendInfo\":\"{\\\"instId\\\":\\\"DINERS\\\",\\\"maskedCardNo\\\":\\\"360886****5515\\\",\\\"cardIndexNo\\\":\\\"2016122800210c00c685968cff8475507d006759e7f95\\\"}\",\"payMethod\":\"CREDIT_CARD\"}],\"transType\":\"ACQUIRING\",\"resultInfo\":{\"resultCodeId\":\"00000000\",\"resultMsg\":\"SUCCESS\",\"resultCode\":\"SUCCESS\",\"resultStatus\":\"S\"},\"transAmount\":{\"value\":\"200\",\"currency\":\"INR\"},\"transId\":\"20170613111212800110166828700008886\",\"extendInfo\":\"{\\\"txnType\\\":\\\"ONLY_PG\\\",\\\"productCode\\\":\\\"51051000100000000002\\\",\\\"issuingBankId\\\":\\\"HDFC\\\",\\\"merchantTransId\\\":\\\"PARCEL908635\\\",\\\"issuingBankName\\\":\\\"HDFC\\\",\\\"paytmMerchantId\\\":\\\"HYBADD50520222544592\\\",\\\"retryCount\\\":\\\"0\\\",\\\"alipayMerchantId\\\":\\\"216820000000141015249\\\",\\\"merchantName\\\":\\\"HYB-AD\\\",\\\"mccCode\\\":\\\"Retail\\\",\\\"totalTxnAmount\\\":\\\"2.0\\\"}\"}},\"signature\":\"no_signature\"}";
                }
                String responseString = JsonMapper.getParamFromJson(alipayResponse, "response").toString();
                final T response = JsonMapper.mapJsonToObject(responseString, clazz);
                return response;
            }
        };

        try {

            CashierRequest cashierRequest = cashierRequestRiskPayment.getRiskAcceptRequest();
            GenericCoreResponseBean<InitiatePaymentResponse> initiateResponse = paymentServiceImpl
                    .chargeFeeOnRiskAnalysis(cashierRequest, "");
            Assert.assertTrue(initiateResponse.isSuccessfullyProcessed());

        } catch (PaytmValidationException e) {
            LOGGER.error("Exception Occurred : {] ", e);
        }

    }

}
