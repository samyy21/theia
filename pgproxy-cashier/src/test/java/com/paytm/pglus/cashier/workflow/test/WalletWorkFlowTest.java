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
import com.paytm.pgplus.cashier.models.CashierMerchant;
import com.paytm.pgplus.cashier.models.CashierRequest.CashierRequestBuilder;
import com.paytm.pgplus.cashier.models.DoPaymentResponse;
import com.paytm.pgplus.cashier.pay.model.PaymentRequest.PaymentRequestBuilder;
import com.paytm.pgplus.cashier.payoption.PayBillOptions.PayBillOptionsBuilder;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.enums.TerminalType;
import com.paytm.pgplus.facade.enums.TransType;
import com.paytm.pgplus.facade.utils.RequestIdGenerator;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author amit.dubey
 *
 */
public class WalletWorkFlowTest extends BaseWorkFlowTest {

    @Test
    public void testNullRequest() throws CashierCheckedException, PaytmValidationException {
        paymentServiceImpl.initiate(null);
    }

    @Test
    public void testValidRequestWalletOnly() throws PaytmValidationException {

        try {

            String requestId = RequestIdGenerator.generateRequestId();
            CashierMerchant cashierMerchant = new CashierMerchant("216820000000000023235");

            Map<PayMethod, String> payOptions = new HashMap<>();
            payOptions.put(PayMethod.BALANCE, "BALANCE");

            Map<String, String> extendInfo = new HashMap<>();
            extendInfo.put("merchantTransId", "order_id_1");
            extendInfo.put("paytmMerchantId", "klbGlV59135347348753");
            extendInfo.put("alipayMerchantId", "216820000000000023235");
            extendInfo.put("ssoToken", "9a00d797-8b09-429b-aae1-ca8539be3a75");
            extendInfo.put("mccCode", "Retail");
            extendInfo.put("website", "paytm");
            extendInfo.put("theme", "paytm");
            extendInfo.put("callbackURL", "https://pguat.paytm.com/MerchantResponse");
            extendInfo.put("peonURL", "https://pguat.paytm.com/MerchantResponse");

            // These are not production env values. Please ignore.
            extendInfo.put("email", "abc@paytm.com");
            extendInfo.put("phoneNo", "9999999999");
            extendInfo.put("clientIp", "10.0.122.22");

            // These are not production env values. Please ignore.
            PayBillOptionsBuilder payBillOptionsBuillder = new PayBillOptionsBuilder(100L, 0L, payOptions)
                    .setPayerAccountNumber("20070000000000002280").setWalletBalance(100L).setExtendInfo(extendInfo);

            // These are not production env values. Please ignore.
            final CashierEnvInfo cashierEnvInfo = new CashierEnvInfo.CashierEnvInfoBuilder("10.0.122.22",
                    TerminalType.WEB).clientKey("e5806b64-598d-414f-b7f7-83f9576eb6f").websiteLanguage("en_US")
                    .osType("Windows.PC").appVersion("1.0").sdkVersion("1.0")
                    .sessionId("8EU6mLl5mUpUBgyRFT4v7DjfQ3fcauthcenter")
                    .tokenId("a8d359d6-ca3d-4048-9295-bbea5f6715a6").orderOsType("orderOsType")
                    .orderTerminalType("orderTerminalType").merchantAppVersion("merchantAppVersion").build();

            PaymentRequestBuilder paymentRequestBuilder = new PaymentRequestBuilder(PaymentType.ONLY_WALLET,
                    "20160416111212800110166287100002325", TransType.ACQUIRING, requestId,
                    payBillOptionsBuillder.build(), cashierEnvInfo).setPayerUserId("216810000000000128283")
                    .setExtendInfo(extendInfo);

            CashierRequestBuilder cashierRequestBuilder = new CashierRequestBuilder("213123123123123213",
                    RequestIdGenerator.generateRequestId(), CashierWorkflow.WALLET).setCashierMerchant(cashierMerchant);
            cashierRequestBuilder.setPaymentRequest(paymentRequestBuilder.build());

            GenericCoreResponseBean<DoPaymentResponse> response = paymentServiceImpl
                    .initiateAndSubmit(cashierRequestBuilder.build());

            String message = response.getFailureMessage();
            System.out.println(message);
            Assert.assertFalse(response.isSuccessfullyProcessed());
        } catch (CashierCheckedException e) {
            System.out.println(e.getMessage());
        }

    }
}