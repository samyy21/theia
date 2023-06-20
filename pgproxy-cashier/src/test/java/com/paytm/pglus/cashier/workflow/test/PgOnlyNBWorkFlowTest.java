package com.paytm.pglus.cashier.workflow.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.paytm.pgplus.cashier.constant.CashierWorkflow;
import com.paytm.pgplus.cashier.enums.PaymentType;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.cashier.models.CashierEnvInfo;
import com.paytm.pgplus.cashier.models.CashierRequest.CashierRequestBuilder;
import com.paytm.pgplus.cashier.models.InitiatePaymentResponse;
import com.paytm.pgplus.cashier.pay.model.PaymentRequest.PaymentRequestBuilder;
import com.paytm.pgplus.cashier.payoption.PayBillOptions.PayBillOptionsBuilder;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.enums.TerminalType;
import com.paytm.pgplus.facade.enums.TransType;
import com.paytm.pgplus.facade.utils.RequestIdGenerator;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 *
 * @author surendra.yadav
 *
 */
public class PgOnlyNBWorkFlowTest extends BaseWorkFlowTest {

    @Test
    public void testNullRequest() throws CashierCheckedException, PaytmValidationException {
        GenericCoreResponseBean<InitiatePaymentResponse> response = paymentServiceImpl.initiate(null);
        String message = response.getFailureMessage();
        System.out.println("Inside testNullRequest() message:=" + message);
        Assert.assertFalse(response.isSuccessfullyProcessed());
    }

    @Test
    public void testValidRequestPgOnly() throws PaytmValidationException {
        try {
            String requestId = RequestIdGenerator.generateRequestId();

            Map<PayMethod, String> payOptions = new HashMap<>();
            payOptions.put(PayMethod.NET_BANKING, "NET_BANKING_ICICI");

            Map<String, String> extendInfo = new HashMap<>();
            extendInfo.put("merchantTransId", "order_id_suren5");
            extendInfo.put("paytmMerchantId", "klbGlV59135347348753");
            extendInfo.put("alipayMerchantId", "216820000000000023235");
            extendInfo.put("ssoToken", "9a00d797-8b09-429b-aae1-ca8539be3a75");
            extendInfo.put("mccCode", "Retail");

            Map<String, String> channelInfo = new HashMap<>();
            channelInfo.put("isEMI", "N");

            PayBillOptionsBuilder payBillOptionsBuillder = new PayBillOptionsBuilder(90L, 0L, payOptions)
                    .setSaveChannelInfoAfterPay(false).setChannelInfo(channelInfo).setExtendInfo(extendInfo);

            // These are not production env values. Please ignore.
            final CashierEnvInfo cashierEnvInfo = new CashierEnvInfo.CashierEnvInfoBuilder("10.0.122.22",
                    TerminalType.WEB).clientKey("e5806b64-598d-414f-b7f7-83f9576eb6f").websiteLanguage("en_US")
                    .osType("Windows.PC").appVersion("1.0").sdkVersion("1.0")
                    .sessionId("8EU6mLl5mUpUBgyRFT4v7DjfQ3fcauthcenter")
                    .tokenId("a8d359d6-ca3d-4048-9295-bbea5f6715a6").orderOsType("orderOsType")
                    .orderTerminalType("orderTerminalType").merchantAppVersion("merchantAppVersion").build();

            PaymentRequestBuilder paymentRequestBuilder = new PaymentRequestBuilder(PaymentType.ONLY_PG,
                    "20160419111212800110166289600002661", TransType.ACQUIRING, requestId,
                    payBillOptionsBuillder.build(), cashierEnvInfo).setExtendInfo(extendInfo);

            CashierRequestBuilder cashierRequestBuilder = new CashierRequestBuilder("213123123123123213",
                    RequestIdGenerator.generateRequestId(), CashierWorkflow.NB).setPaymentRequest(paymentRequestBuilder
                    .build());

            GenericCoreResponseBean<InitiatePaymentResponse> response = paymentServiceImpl
                    .initiate(cashierRequestBuilder.build());
            String message = response.getFailureMessage();
            System.out.println("Inside testValidRequestPgOnly message:= " + message);
            Assert.assertFalse(response.isSuccessfullyProcessed());
        } catch (CashierCheckedException e) {
            System.out.println("Exception Occurred " + e.getMessage());
        }

    }

}