/**
 * 
 */
package com.paytm.pglus.cashier.workflow.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.paytm.pgplus.cashier.constant.CashierWorkflow;
import com.paytm.pgplus.cashier.enums.PaymentType;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.cashier.models.CashierEnvInfo;
import com.paytm.pgplus.cashier.models.CashierMerchant;
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
 * @author amit.dubey
 *
 */
public class HybridDCWorkFlowTest extends BaseWorkFlowTest {

    @Test
    public void testValidRequestHybridDebitCard() throws PaytmValidationException {
        try {
            String requestId = RequestIdGenerator.generateRequestId();

            CashierMerchant cashierMerchant = new CashierMerchant("ABCD1234");

            Map<PayMethod, String> payOptions = new HashMap<>();
            payOptions.put(PayMethod.BALANCE, "BALANCE");
            payOptions.put(PayMethod.DEBIT_CARD, "DEBIT_CARD_VISA");

            Map<String, String> extendInfo = new HashMap<>();
            extendInfo.put("testKey", "testvalue");

            PayBillOptionsBuilder payBillOptionsBuillder = new PayBillOptionsBuilder(100L, 0L, payOptions)
                    .setSaveChannelInfoAfterPay(false).setWalletBalance(50L)
                    .setPayerAccountNumber("20070000000000002280");

            // These are not production env values. Please ignore.
            final CashierEnvInfo cashierEnvInfo = new CashierEnvInfo.CashierEnvInfoBuilder("10.0.122.22",
                    TerminalType.WEB).clientKey("e5806b64-598d-414f-b7f7-83f9576eb6f").websiteLanguage("en_US")
                    .osType("Windows.PC").appVersion("1.0").sdkVersion("1.0")
                    .sessionId("8EU6mLl5mUpUBgyRFT4v7DjfQ3fcauthcenter")
                    .tokenId("a8d359d6-ca3d-4048-9295-bbea5f6715a6").orderOsType("orderOsType")
                    .orderTerminalType("orderTerminalType").merchantAppVersion("merchantAppVersion").build();

            PaymentRequestBuilder paymentRequestBuilder = new PaymentRequestBuilder(PaymentType.HYBRID,
                    "20160406111212800110166288500000014", TransType.ACQUIRING, requestId,
                    payBillOptionsBuillder.build(), cashierEnvInfo).setPayerUserId("216810000000000128283")
                    .setExtendInfo(extendInfo);

            CashierRequestBuilder cashierRequestBuilder = new CashierRequestBuilder("213123123123123213",
                    RequestIdGenerator.generateRequestId(), CashierWorkflow.ATM).setPaymentRequest(
                    paymentRequestBuilder.build()).setCashierMerchant(cashierMerchant);

            GenericCoreResponseBean<InitiatePaymentResponse> response = paymentServiceImpl
                    .initiate(cashierRequestBuilder.build());
            String message = response.getFailureMessage();
            System.out.println("message : " + message);
            Assert.assertFalse(response.isSuccessfullyProcessed());
        } catch (CashierCheckedException e) {
            System.out.println("Exception Occurred " + e.getMessage());
        }

    }

}
