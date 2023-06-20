package com.paytm.pgplus.cashier.service.test.payment.initiate;

import com.paytm.pglus.cashier.workflow.test.BaseWorkFlowTest;
import com.paytm.pgplus.facade.enums.ServiceUrl;
import mockit.Mock;
import mockit.MockUp;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.InitiatePaymentResponse;
import com.paytm.pgplus.cashier.service.impl.PaymentServiceImpl;
import com.paytm.pgplus.cashier.service.test.builder.AddMoneyCashierRequestBuilderNetbanking;
import com.paytm.pgplus.facade.enums.AlipayServiceUrl;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.ConnectionUtil;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author Vivek Kumar
 */

public class AddMoneyNetbankingWorkflowSuccessTest extends BaseCashierWorkFlowTest {

    @Autowired
    PaymentServiceImpl paymentServiceImpl;

    @Autowired
    AddMoneyCashierRequestBuilderNetbanking cashierRequestBuilder;

    @BeforeClass
    public static void initailize() {

        String rmiServerHost = System.getProperty("java.rmi.server.hostname", null);
        if (rmiServerHost == null) {
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        }
    }

    @Test
    public void testValidNetbankingRequest() throws CashierCheckedException {

        new MockUp<ConnectionUtil>() {
            @Mock
            public <T, U> T execute(final U request, final ServiceUrl api, final Class<T> clazz)
                    throws FacadeCheckedException {
                String alipayResponse = "";
                if (api == AlipayServiceUrl.PAYMENT_CASHIER_PAY) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.payment.cashier.pay\",\"clientId\":\"clientId\",\"reqMsgId\":\"fb123fb74259431287fd3d3638d1b76cadministratorthinkpadl450\",\"version\":\"1.1.8\",\"respTime\":\"2017-06-08T16:16:06+05:30\"},\"body\":{\"resultInfo\":{\"resultStatus\":\"A\",\"resultCodeId\":\"00000009\",\"resultMsg\":\"ACCEPTED_SUCCESS\",\"resultCode\":\"ACCEPTED_SUCCESS\"},\"requestId\":\"f1fd0d30ead74701a8f044245bda7b08administratorthinkpadl450\",\"cashierRequestId\":\"clientIdf1fd0d30ead74701a8f044245bda7b08administratorthinkpadl450\"}},\"signature\":\"no_signature\"}";
                }
                if (api == AlipayServiceUrl.PAYMENT_CASHIER_PAYRESULT_QUERY) {
                    alipayResponse = "{\"response\":{\"head\":{\"function\":\"alipayplus.payment.cashier.payresult.query\",\"clientId\":\"clientId\",\"reqMsgId\":\"91680fdf5e6d48b0a4a81c3825df8c34administratorthinkpadl450\",\"version\":\"1.1.5\",\"respTime\":\"2017-06-08T16:17:59+05:30\"},\"body\":{\"webFormContext\":\"<!DOCTYPE html PUBLIC \\\"-//W3C//DTD HTML 4.01 Transitional//EN\\\" \\\"http://www.w3.org/TR/html4/loose.dtd\\\">\\n<html>\\n<head>\\n<meta http-equiv=\\\"Content-Type\\\" content=\\\"text/html; charset=ISO-8859-1\\\">\\n<title>Paytm</title>\\n\\n</head>\\n<body >\\n <table align='center'>\\n  <tr><td><STRONG>Transaction is being processed,</STRONG></td></tr>\\n  <tr><td><font color='blue'>Please wait ...</font></td></tr>\\n  <tr><td>(Please do not press 'Refresh' or 'Back' button</td></tr>\\n </table>\\n\\n<FORM NAME='frm' ACTION='https://shopping.icicibank.com/corp/BANKAWAY?IWQRYTASKOBJNAME=bay_mc_login&BAY_BANKID=ICI' method='post'>\\n\\t<INPUT TYPE='HIDDEN' NAME='SBMTTYPE' VALUE='POST' >\\n\\t<INPUT TYPE='HIDDEN' NAME='MD' VALUE='P'>\\n\\t<INPUT TYPE='HIDDEN' NAME='PID' VALUE='000000001141'>\\n\\t<INPUT TYPE='HIDDEN' NAME='ES' Value='HjsB9TawdlZdclvUwBE6vtBBrfU2ln70ON4B+wJyZrZnKg6h6QQ6AK+rfw8Jy3tqhnipv4WfCp6DMd/uxL6iN8nGtBjs9i7oZF2VYprTDQtectu7LNyVCqP7Gp7xEXUxu/9Owu/VcfltXxMG8fYR2Kho9waSruCUG9LH+YxVNT5Cy50UmZJygO7GduS6yw14bxjrWoBKlfoZfdY38KPCsyxdbJzRkspuLqQU007yf9c=' >\\n     <INPUT TYPE='HIDDEN' NAME='SPID' Value='100000061642'> \\n</FORM>\\n</body>\\n<script type=\\\"text/javascript\\\">\\n\\tif(window.top !== window){\\t\\n\\t\\tdocument.forms[0].target=\\\"_parent\\\";\\n\\t}\\n\\tdocument.forms[0].submit();\\n</script>\\t  \\n</html>\",\"paymentStatus\":\"REDIRECT\",\"payOptionInfos\":[{\"payAmount\":{\"value\":\"200\",\"currency\":\"INR\"},\"extendInfo\":\"{\\\"instId\\\":\\\"ICICI\\\"}\",\"payMethod\":\"NET_BANKING\"}],\"transType\":\"TOP_UP\",\"resultInfo\":{\"resultCodeId\":\"00000000\",\"resultMsg\":\"SUCCESS\",\"resultCode\":\"SUCCESS\",\"resultStatus\":\"S\"},\"transAmount\":{\"value\":\"200\",\"currency\":\"INR\"},\"payerUserId\":\"216810000000000128283\",\"transId\":\"2017060820121481010100166282800034517\",\"extendInfo\":\"{\\\"txnType\\\":\\\"ONLY_PG\\\",\\\"merchantTransId\\\":\\\"PARCEL707105\\\",\\\"productCode\\\":\\\"51053000100000000001\\\",\\\"paytmMerchantId\\\":\\\"DataCl59062077159771\\\",\\\"alipayMerchantId\\\":\\\"216820000000145754283\\\",\\\"PAYTM_USER_ID\\\":\\\"10797790\\\",\\\"userMobile\\\":\\\"9899267758\\\",\\\"ssoToken\\\":\\\"220dd1bd-69b6-4e7f-ab8a-f2c7a35024ff\\\",\\\"merchantName\\\":\\\"DataClean\\\",\\\"userEmail\\\":\\\"tarsnghl@gmail.com\\\",\\\"mccCode\\\":\\\"Retail\\\",\\\"totalTxnAmount\\\":\\\"2.0\\\"}\"}},\"signature\":\"no_signature\"}";
                }
                String responseString = JsonMapper.getParamFromJson(alipayResponse, "response").toString();
                final T response = JsonMapper.mapJsonToObject(responseString, clazz);
                return response;
            }
        };

        try {
            CashierRequest cashierRequest = cashierRequestBuilder.getNetbankingRequest();
            GenericCoreResponseBean<InitiatePaymentResponse> initiateResponse = paymentServiceImpl
                    .initiate(cashierRequest);
            Assert.assertTrue(initiateResponse.isSuccessfullyProcessed());
        } catch (PaytmValidationException e) {
            e.printStackTrace();
        }
    }
}
