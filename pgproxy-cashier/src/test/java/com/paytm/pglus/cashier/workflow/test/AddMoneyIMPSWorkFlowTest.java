/**
 * 
 */
package com.paytm.pglus.cashier.workflow.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.paytm.pgplus.cashier.cachecard.model.IMPSCardRequest;
import com.paytm.pgplus.cashier.constant.CashierWorkflow;
import com.paytm.pgplus.cashier.enums.PaymentType;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.cashier.models.CardRequest;
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
public class AddMoneyIMPSWorkFlowTest extends BaseWorkFlowTest {
    @Test
    public void testValidRequest() throws PaytmValidationException {
        try {
            String requestId = RequestIdGenerator.generateRequestId();
            CashierMerchant cashierMerchant = new CashierMerchant("31244444114600105612221540");

            Map<PayMethod, String> payOptions = new HashMap<>();
            payOptions.put(PayMethod.IMPS, "IMPS");

            Map<String, String> extendInfo = new HashMap<>();
            extendInfo.put("merchantTransId", "order_id_nair33");
            extendInfo.put("paytmMerchantId", "klbGlV59135347348753");
            extendInfo.put("alipayMerchantId", "216820000000000023235");
            extendInfo.put("ssoToken", "9a00d797-8b09-429b-aae1-ca8539be3a75");
            extendInfo.put("mccCode", "Retail");

            Map<String, String> channelInfo = new HashMap<>();
            channelInfo.put("isEMI", "N");

            PayBillOptionsBuilder payBillOptionsBuillder = new PayBillOptionsBuilder(100L, 0L, payOptions)
                    .setChannelInfo(channelInfo).setExtendInfo(extendInfo);

            // These are not production env values. Please ignore.
            final CashierEnvInfo cashierEnvInfo = new CashierEnvInfo.CashierEnvInfoBuilder("10.0.142.92",
                    TerminalType.WEB).clientKey("e5806b64-598d-414f-b7f7-83f9576eb6f").websiteLanguage("en_US")
                    .osType("Linux.PC").appVersion("1.0").sdkVersion("1.0")
                    .sessionId("8EU6mLl5mUpUBgyRFT4v7DjfQ3fcauthcenter")
                    .tokenId("a8d359d6-ca3d-4048-9295-bbea5f6715a6").orderOsType("orderOsType")
                    .orderTerminalType("orderTerminalType").merchantAppVersion("merchantAppVersion").build();

            PaymentRequestBuilder paymentRequestBuilder = new PaymentRequestBuilder(PaymentType.ONLY_PG,
                    "20160406111212800110166288500000014", TransType.TOP_UP, requestId, payBillOptionsBuillder.build(),
                    cashierEnvInfo).setPayerUserId("216810000000000128283").setExtendInfo(extendInfo);

            IMPSCardRequest impsCardRequest = new IMPSCardRequest("5566204900063606", "123", "HDFC", "9923431211");
            CardRequest cardRequest = new CardRequest(impsCardRequest);

            CashierRequestBuilder cashierRequestBuilder = new CashierRequestBuilder("213123123123123213",
                    RequestIdGenerator.generateRequestId(), CashierWorkflow.ADD_MONEY_IMPS);
            cashierRequestBuilder.setCashierMerchant(cashierMerchant);
            cashierRequestBuilder.setPaymentRequest(paymentRequestBuilder.build());
            cashierRequestBuilder.setCardRequest(cardRequest);

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
