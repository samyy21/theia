package com.paytm.pgplus.cashier.service.test.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cashier.cachecard.model.BinCardRequest;
import com.paytm.pgplus.cashier.cachecard.model.SavedCardRequest;
import com.paytm.pgplus.cashier.constant.CashierWorkflow;
import com.paytm.pgplus.cashier.enums.PaymentType;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.CardRequest;
import com.paytm.pgplus.cashier.models.CashierEnvInfo;
import com.paytm.pgplus.cashier.models.CashierMerchant;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.CashierRequest.CashierRequestBuilder;
import com.paytm.pgplus.cashier.pay.model.PSULimit;
import com.paytm.pgplus.cashier.pay.model.PaymentRequest;
import com.paytm.pgplus.cashier.pay.model.PaymentRequest.PaymentRequestBuilder;
import com.paytm.pgplus.cashier.pay.model.ValidationRequest;
import com.paytm.pgplus.cashier.payoption.PayBillOptions.PayBillOptionsBuilder;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.enums.ProductCodes;
import com.paytm.pgplus.facade.enums.TerminalType;
import com.paytm.pgplus.facade.enums.TransType;

/**
 * @author Vivek Kumar
 */

@Service
public class CashierRequestBuilderSavecard {

    public CashierRequest getSavedCardRequest() throws CashierCheckedException {

        SavedCardRequest savedCardRequest = new SavedCardRequest("1000867", "000", "CREDIT_CARD", "VISA");

        // acquirement id
        String acquirementId = "20170531111212800110166281600017320";

        // request id
        String requestId = "1a6b42c745af453bbcc65eae4b774157administratorthinkpadl450";

        // cashier merchant
        CashierMerchant cashierMerchant = new CashierMerchant("HYBADD50520222544592", "216820000000141015249", true, 3);

        // card request
        CardRequest cardRequest = new CardRequest(savedCardRequest);

        Map<PayMethod, String> payOptions = new HashMap<>();
        payOptions.put(PayMethod.DEBIT_CARD, "DEBIT_CARD_VISA");

        Map<String, String> channelInfo = new HashMap<>();
        channelInfo.put("isEMI", "N");
        channelInfo.put("toUseDirectPayment", "false");
        channelInfo.put("browserUserAgent",
                "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:47.0) Gecko/20100101 Firefox/47.0");
        channelInfo.put("shippingAddr1", "shippingAddr1");
        channelInfo.put("shippingAddr2", "shippingAddr2");
        channelInfo.put("cardHoldName", "cardHoldName");

        Map<String, String> payOptionExtendInfo = new HashMap<>();
        payOptionExtendInfo.put("totalTxnAmount", "2.0");
        payOptionExtendInfo.put("issuingBankName", "HDFC");
        payOptionExtendInfo.put("PAYTM_USER_ID", "10797790");
        payOptionExtendInfo.put("savedCardId", "1000867");
        payOptionExtendInfo.put("retryCount", "0");
        payOptionExtendInfo.put("txnType", "ONLY_PG");
        payOptionExtendInfo.put("merchantName", "HYB-AD");
        payOptionExtendInfo.put("userEmail", "tarsnghl@gmail.com");
        payOptionExtendInfo.put("ssoToken", "220dd1bd-69b6-4e7f-ab8a-f2c7a35024ff");
        payOptionExtendInfo.put("productCode", "51051000100000000002");
        payOptionExtendInfo.put("merchantTransId", "PARCEL113414");
        payOptionExtendInfo.put("paytmMerchantId", "HYBADD50520222544592");
        payOptionExtendInfo.put("alipayMerchantId", "216820000000141015249");
        payOptionExtendInfo.put("mccCode", "Retail");

        PayBillOptionsBuilder payBillOptionsBuillder = new PayBillOptionsBuilder(200L, 2L, payOptions)
                .setWalletBalance(45146932L).setTopAndPay(false).setSaveChannelInfoAfterPay(false)
                .setExtendInfo(payOptionExtendInfo).setChannelInfo(channelInfo).setIssuingCountry("IN")
                .setPayerAccountNumber("20070000000000002280");

        Map<String, String> extentInfo = new HashMap<String, String>();
        extentInfo.put("deviceId", "1fe8410952-10e1f-1c435-1b19b-1e9741a61616632");

        final CashierEnvInfo cashierEnvInfo = new CashierEnvInfo.CashierEnvInfoBuilder(null, TerminalType.WEB)
                .sessionId("1fe8410952-10e1f-1c435-1b19b-1e9741a61616632")
                .tokenId("1fe8410952-10e1f-1c435-1b19b-1e9741a61616632").osType("Linux (Ubuntu)")
                .extendInfo(extentInfo).build();

        Map<String, String> extendInfo = new HashMap<String, String>();
        extendInfo.put("totalTxnAmount", "2.0");
        extendInfo.put("PAYTM_USER_ID", "10797790");
        extendInfo.put("retryCount", "0");
        extendInfo.put("txnType", "ONLY_PG");
        extendInfo.put("merchantName", "HYB-AD");
        extendInfo.put("savedCardId", "1000867");
        extendInfo.put("mccCode", "Retail");
        extendInfo.put("productCode", "51051000100000000002");
        extendInfo.put("merchantTransId", "PARCEL582828");
        extendInfo.put("ssoToken", "220dd1bd-69b6-4e7f-ab8a-f2c7a35024ff");
        extendInfo.put("paytmMerchantId", "HYBADD50520222544592");
        extendInfo.put("alipayMerchantId", "216820000000141015249");

        Map<String, String> riskExtendInfo = new HashMap<String, String>();
        riskExtendInfo.put("customerType", "false");

        PaymentRequestBuilder paymentRequestBuilder = new PaymentRequestBuilder(PaymentType.ONLY_PG,
                "20170531111212800110166281600017320", TransType.ACQUIRING, requestId, payBillOptionsBuillder.build(),
                cashierEnvInfo).setPayerUserId(null).setSecurityId("sidea95c884e461352cd3924d7a8accc54d")
                .setExtendInfo(extendInfo).setRiskExtendInfo(riskExtendInfo);

        // Payment request
        PaymentRequest paymentRequest = paymentRequestBuilder.build();

        BinDetail binDetail = new BinDetail();
        binDetail.setActive(true);
        binDetail.setBank("HDFC Bank");
        binDetail.setBankCode("HDFC");
        binDetail.setBin((long) 471865);
        binDetail.setCardName("VISA");
        binDetail.setCardType("CREDIT_CARD");
        binDetail.setIsIndian(true);
        final BinCardRequest binCardRequest = new BinCardRequest(binDetail);

        PSULimit psuLimitInfo = new PSULimit();

        List<String> nbCapBankList = new ArrayList<String>();
        nbCapBankList.add("8565559");
        nbCapBankList.add("8565560");
        nbCapBankList.add("8565557");
        nbCapBankList.add("8565554");
        nbCapBankList.add("8565562");

        List<String> capMerchantList = new ArrayList<String>();
        capMerchantList.add("8726188");
        capMerchantList.add("8725845");
        capMerchantList.add("8727415");
        capMerchantList.add("8731949");
        capMerchantList.add("8731961");

        psuLimitInfo.setCapMerchantList(capMerchantList);
        psuLimitInfo.setNbCapBankList(nbCapBankList);
        psuLimitInfo.setNbCapMaxAmount("2");
        psuLimitInfo.setNbCapApplicable("yes");
        psuLimitInfo.setCardCapMaxAmount("4000");
        psuLimitInfo.setCardCapApplicable("yes");
        psuLimitInfo.setSbiCardEnabled("0");

        ValidationRequest validationRequest = new ValidationRequest("CC", null, "HDFC", false, "8738529", null,
                psuLimitInfo);

        CashierRequestBuilder builder = new CashierRequestBuilder(acquirementId, requestId, CashierWorkflow.ISOCARD)
                .setCashierMerchant(cashierMerchant).setBinCardRequest(binCardRequest).setCardRequest(cardRequest)
                .setPaymentRequest(paymentRequest).setValidationRequest(validationRequest);

        CashierRequest cashierRequest = builder.build();

        cashierRequest.setFundOrder(false);
        cashierRequest.setIsProcessed(false);
        cashierRequest.setInternalCardRequest(null);
        cashierRequest.setIsDirectBankCardPayRequest(false);
        cashierRequest.setTransCreatedtime("2017-05-31T17:36:23+05:30");
        cashierRequest.setProductCode(ProductCodes.StandardDirectPayAcquiringProdChargePayer);

        return cashierRequest;
    }
}
