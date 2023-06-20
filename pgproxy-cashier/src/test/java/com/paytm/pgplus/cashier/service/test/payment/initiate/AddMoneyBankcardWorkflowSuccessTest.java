package com.paytm.pgplus.cashier.service.test.payment.initiate;

import com.paytm.pglus.cashier.workflow.test.BaseWorkFlowTest;
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
import com.paytm.pgplus.cashier.service.test.builder.AddMoneyCashierRequestBuilderBankcard;
import com.paytm.pgplus.facade.enums.AlipayServiceUrl;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.ConnectionUtil;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author Vivek Kumar
 */

public class AddMoneyBankcardWorkflowSuccessTest extends BaseCashierWorkFlowTest {

    @Autowired
    AddMoneyCashierRequestBuilderBankcard cashierBankcardRequest;

    @Autowired
    PaymentServiceImpl paymentServiceImpl;

    private static final Logger LOGGER = LoggerFactory.getLogger(AddMoneyBankcardWorkflowSuccessTest.class);

    @BeforeClass
    public static void initailize() {

        String rmiServerHost = System.getProperty("java.rmi.server.hostname", null);
        if (rmiServerHost == null) {
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        }
    }

    @Test
    public void testValidBankcardRequest() throws CashierCheckedException {

        new MockUp<ConnectionUtil>() {
            @Mock
            public <T, U> T execute(final U request, final ServiceUrl api, final Class<T> clazz)
                    throws FacadeCheckedException {
                String alipayResponse = "";
                if (api == AlipayServiceUrl.USER_ASSET_CACHE_CARD) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.user.asset.cacheCard\",\"clientId\":\"clientId\",\"reqMsgId\":\"8a009e177d2543cebbcc7cd6916d852aadministratorthinkpadl450\",\"version\":\"1.1.5\",\"respTime\":\"2017-06-09T17:06:25+05:30\"},\"body\":{\"maskedCardNo\":\"416021******1737\",\"resultInfo\":{\"resultCodeId\":\"00000000\",\"resultMsg\":\"SUCCESS\",\"resultStatus\":\"S\",\"resultCode\":\"SUCCESS\"},\"tokenId\":\"20170609055114fecd11e1cfbe183e32d96e6bf18e56c\",\"cardIndexNo\":\"2017010500510a65e478de7fc6f64b005c9ef855f4cbf\"}},\"signature\":\"no_signature\"}";
                }
                if (api == AlipayServiceUrl.PAYMENT_CASHIER_PAY) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.payment.cashier.pay\",\"clientId\":\"clientId\",\"reqMsgId\":\"fe2b2d9805d74968a3525ac574920fd0administratorthinkpadl450\",\"version\":\"1.1.8\",\"respTime\":\"2017-06-09T17:07:53+05:30\"},\"body\":{\"resultInfo\":{\"resultStatus\":\"A\",\"resultCodeId\":\"00000009\",\"resultMsg\":\"ACCEPTED_SUCCESS\",\"resultCode\":\"ACCEPTED_SUCCESS\"},\"requestId\":\"786d081ee96342e1a6705e5b92e41605administratorthinkpadl450\",\"cashierRequestId\":\"clientId786d081ee96342e1a6705e5b92e41605administratorthinkpadl450\"}},\"signature\":\"no_signature\"}";
                }
                if (api == AlipayServiceUrl.PAYMENT_CASHIER_PAYRESULT_QUERY) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.payment.cashier.payresult.query\",\"clientId\":\"clientId\",\"reqMsgId\":\"c7c598f5eef7416fb71a47d809309ab2administratorthinkpadl450\",\"version\":\"1.1.5\",\"respTime\":\"2017-06-09T17:08:38+05:30\"},\"body\":{\"webFormContext\":\"<!DOCTYPE html PUBLIC \\\"-//W3C//DTD HTML 4.01 Transitional//EN\\\" \\\"http://www.w3.org/TR/html4/loose.dtd\\\">\\n<html>\\n\\n<head>\\n<meta http-equiv=\\\"Content-Type\\\" content=\\\"text/html; charset=ISO-8859-1\\\">\\n<title>Paytm</title>\\n\\n</head>\\n<body >\\n\\n <table align='center'>\\n  <tr><td><STRONG>Transaction is being processed,</STRONG></td></tr>\\n  <tr><td><font color='blue'>Please wait ...</font></td></tr>\\n  <tr><td>(Please do not press 'Refresh' or 'Back' button</td></tr>\\n </table>\\n<FORM NAME='frm' ACTION='https://netsafe.hdfcbank.com/ACSWeb/com.enstage.entransact.servers.AccessControlServerSSL?ty=V' method='post'>\\n\\n\\t<input type=\\\"hidden\\\" name=\\\"PaReq\\\" value=\\\"eJxVkdtugkAQhl+FcNuUWUFZMcMaD2mLUeOpVS8JbJRGDi6g4tN3V7G2d/PtzP4z8w92L/FBO3GRR2ni6g2D6BpPgjSMkp2rf67eXtt6l+FqLzgfLnlQCs5wwvPc33EtCl3d42JEq+Xm+2sThbv1y64/Tueh/x7PPFdnOOst+JFh3YBJfcNEeKBUEsHeTwqGfnDse1PWtCyTNhFqxJgLb8gc6jhthDtg4seczXrb1QThFmOQlkkhKma1bIQHYCkObF8UWQfgfD4bmV8VsRGkMSCoFMKz+6xUUS6lLlHIhoM5pFfbagrYR9XVDg/eIZicYDzdugiqAkO/4MwkDUps4mik1SG005Jz397Rj9UMbJGbBiFymTtiprr07mCqxN8HlN4Kaf1jiwchv2RpwtUXhN8Y4Tny4EPZFxTKqNO8VY1ItqbFwG40HUpIW/pJiaMsvZUovUjaY9qE3gQVICgRqK8F9YFl9O/wP62ptw8=\\\">\\t\\t\\n\\t<input type=\\\"hidden\\\" name=\\\"MD\\\" value=\\\"5474353071771600\\\">\\n\\t<input type=\\\"hidden\\\" name=\\\"TermUrl\\\" value=\\\"http://52.76.10.37/instaproxy/bankresponse/HDFC/CC/90170609000007239828\\\">\\n\\n</FORM>\\n</body>\\n<script type=\\\"text/javascript\\\">\\n\\tif(window.top !== window){\\t\\n\\t\\tdocument.forms[0].target=\\\"_parent\\\";\\n\\t}\\n\\tdocument.forms[0].submit();\\n</script>\\t  \\n</html>\",\"paymentStatus\":\"REDIRECT\",\"payOptionInfos\":[{\"payAmount\":{\"value\":\"200\",\"currency\":\"INR\"},\"extendInfo\":\"{\\\"instId\\\":\\\"VISA\\\",\\\"maskedCardNo\\\":\\\"416021******1737\\\",\\\"cardIndexNo\\\":\\\"2017010500510a65e478de7fc6f64b005c9ef855f4cbf\\\"}\",\"payMethod\":\"DEBIT_CARD\"}],\"transType\":\"TOP_UP\",\"resultInfo\":{\"resultCodeId\":\"00000000\",\"resultMsg\":\"SUCCESS\",\"resultCode\":\"SUCCESS\",\"resultStatus\":\"S\"},\"transAmount\":{\"value\":\"200\",\"currency\":\"INR\"},\"payerUserId\":\"216810000000000128283\",\"transId\":\"2017060920121481010100166282800034520\",\"extendInfo\":\"{\\\"txnType\\\":\\\"ONLY_PG\\\",\\\"productCode\\\":\\\"51053000100000000001\\\",\\\"issuingBankId\\\":\\\"HDFC\\\",\\\"ssoToken\\\":\\\"220dd1bd-69b6-4e7f-ab8a-f2c7a35024ff\\\",\\\"userEmail\\\":\\\"tarsnghl@gmail.com\\\",\\\"merchantTransId\\\":\\\"PARCEL545244\\\",\\\"issuingBankName\\\":\\\"HDFC\\\",\\\"paytmMerchantId\\\":\\\"DataCl59062077159771\\\",\\\"alipayMerchantId\\\":\\\"216820000000145754283\\\",\\\"userMobile\\\":\\\"9899267758\\\",\\\"PAYTM_USER_ID\\\":\\\"10797790\\\",\\\"merchantName\\\":\\\"DataClean\\\",\\\"mccCode\\\":\\\"Retail\\\",\\\"totalTxnAmount\\\":\\\"2.0\\\"}\"}},\"signature\":\"no_signature\"}";
                }
                String responseString = JsonMapper.getParamFromJson(alipayResponse, "response").toString();
                final T response = JsonMapper.mapJsonToObject(responseString, clazz);
                return response;
            }
        };

        try {

            CashierRequest cashierRequest = cashierBankcardRequest.getBankcardRequest();
            GenericCoreResponseBean<InitiatePaymentResponse> initiateResponse = paymentServiceImpl
                    .initiate(cashierRequest);
            Assert.assertTrue(initiateResponse.isSuccessfullyProcessed());

        } catch (PaytmValidationException e) {
            LOGGER.error("Exception Occurred : {} ", e);
        }

    }

}
