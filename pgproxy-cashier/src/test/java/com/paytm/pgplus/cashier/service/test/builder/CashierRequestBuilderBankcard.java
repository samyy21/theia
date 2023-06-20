package com.paytm.pgplus.cashier.service.test.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cashier.cachecard.model.BankCardRequest;
import com.paytm.pgplus.cashier.cachecard.model.BinCardRequest;
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
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;

/**
 * @author Vivek Kumar
 */

@Service
public class CashierRequestBuilderBankcard {

    public CashierRequest getBankCardRequest() throws CashierCheckedException, PaytmValidationException {

        BankCardRequest bankCardRequest = new BankCardRequest("4160210903741737", "000", "2028", "10", "DEBIT_CARD");

        // acquirement id
        String acquirementId = "20170531111212800110166467300009542";

        // request id
        String requestId = "47a64a2d8c6d427b92507fa4b4846ea5administratorthinkpadl450";

        // cashier merchant
        CashierMerchant cashierMerchant = new CashierMerchant("HYBADD50520222544592", "216820000000141015249", true, 3);

        // card request
        CardRequest cardRequest = new CardRequest("416021", bankCardRequest);

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
        payOptionExtendInfo.put("PAYTM_USER_ID", "null");
        payOptionExtendInfo.put("retryCount", "0");
        payOptionExtendInfo.put("txnType", "ONLY_PG");
        payOptionExtendInfo.put("merchantName", "HYB-AD");
        payOptionExtendInfo.put("productCode", "51051000100000000002");
        payOptionExtendInfo.put("merchantTransId", "PARCEL113414");
        payOptionExtendInfo.put("paytmMerchantId", "HYBADD50520222544592");
        payOptionExtendInfo.put("alipayMerchantId", "216820000000141015249");
        payOptionExtendInfo.put("mccCode", "Retail");

        PayBillOptionsBuilder payBillOptionsBuillder = new PayBillOptionsBuilder(200L, 2L, payOptions)
                .setWalletBalance(0L).setTopAndPay(false).setSaveChannelInfoAfterPay(false)
                .setExtendInfo(payOptionExtendInfo).setChannelInfo(channelInfo).setIssuingCountry("IN");

        Map<String, String> extentInfo = new HashMap<String, String>();
        extentInfo.put("deviceId", "1c4411ac7f-1d222-130f8-12b6c-1bc8e120041e04e");

        final CashierEnvInfo cashierEnvInfo = new CashierEnvInfo.CashierEnvInfoBuilder(null, TerminalType.WEB)
                .sessionId("1c4411ac7f-1d222-130f8-12b6c-1bc8e120041e04e")
                .tokenId("1c4411ac7f-1d222-130f8-12b6c-1bc8e120041e04e").osType("Linux (Ubuntu)")
                .extendInfo(extentInfo).build();

        Map<String, String> extendInfo = new HashMap<String, String>();
        extendInfo.put("totalTxnAmount", "2.0");
        extendInfo.put("retryCount", "0");
        extendInfo.put("txnType", "ONLY_PG");
        extendInfo.put("merchantName", "HYB-AD");
        extendInfo.put("mccCode", "Retail");
        extendInfo.put("productCode", "51051000100000000002");
        extendInfo.put("merchantTransId", "PARCEL113414");
        extendInfo.put("paytmMerchantId", "HYBADD50520222544592");
        extendInfo.put("alipayMerchantId", "216820000000141015249");

        Map<String, String> riskExtendInfo = new HashMap<String, String>();
        riskExtendInfo.put("customerType", "false");

        PaymentRequestBuilder paymentRequestBuilder = new PaymentRequestBuilder(PaymentType.ONLY_PG,
                "20170531111212800110166467300009542", TransType.ACQUIRING, requestId, payBillOptionsBuillder.build(),
                cashierEnvInfo).setPayerUserId(null).setSecurityId("sid46080731f639b309323edb2b1141767a")
                .setExtendInfo(extendInfo).setRiskExtendInfo(riskExtendInfo);

        // Payment request
        PaymentRequest paymentRequest = paymentRequestBuilder.build();

        BinDetail binDetail = new BinDetail();
        binDetail.setActive(true);
        binDetail.setBank("HDFC Bank");
        binDetail.setBankCode("HDFC");
        binDetail.setBin((long) 416021);
        binDetail.setCardName("VISA");
        binDetail.setCardType("DEBIT_CARD");
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

        ValidationRequest validationRequest = new ValidationRequest("DC", null, "HDFC", false, "8738529", "8565560",
                psuLimitInfo);

        CashierRequestBuilder builder = new CashierRequestBuilder(acquirementId, requestId, CashierWorkflow.ISOCARD)
                .setCashierMerchant(cashierMerchant).setBinCardRequest(binCardRequest).setCardRequest(cardRequest)
                .setPaymentRequest(paymentRequest).setValidationRequest(validationRequest);

        CashierRequest cashierRequest = builder.build();

        cashierRequest.setFundOrder(false);
        cashierRequest.setIsProcessed(false);
        cashierRequest.setInternalCardRequest(null);
        cashierRequest.setIsDirectBankCardPayRequest(false);
        cashierRequest.setTransCreatedtime("2017-05-31T12:52:09+05:30");
        cashierRequest.setProductCode(ProductCodes.StandardDirectPayAcquiringProdChargePayer);

        return cashierRequest;

    }

    public CashierRequest getInvalidTopupBankcardRequest() throws CashierCheckedException, PaytmValidationException {

        BankCardRequest bankCardRequest = new BankCardRequest("4160210903741737", "000", "2028", "10", "DEBIT_CARD");

        // acquirement id
        String acquirementId = "20170531111212800110166467300009542";

        // request id
        String requestId = "47a64a2d8c6d427b92507fa4b4846ea5administratorthinkpadl450";

        // cashier merchant
        CashierMerchant cashierMerchant = new CashierMerchant("HYBADD50520222544592", "216820000000141015249", true, 3);

        // card request
        CardRequest cardRequest = new CardRequest("416021", bankCardRequest);

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
        payOptionExtendInfo.put("PAYTM_USER_ID", "null");
        payOptionExtendInfo.put("retryCount", "0");
        payOptionExtendInfo.put("txnType", "ONLY_PG");
        payOptionExtendInfo.put("merchantName", "HYB-AD");
        payOptionExtendInfo.put("productCode", "51051000100000000002");
        payOptionExtendInfo.put("merchantTransId", "PARCEL113414");
        payOptionExtendInfo.put("paytmMerchantId", "HYBADD50520222544592");
        payOptionExtendInfo.put("alipayMerchantId", "216820000000141015249");
        payOptionExtendInfo.put("mccCode", "Retail");

        PayBillOptionsBuilder payBillOptionsBuillder = new PayBillOptionsBuilder(200L, 2L, payOptions)
                .setWalletBalance(0L).setTopAndPay(false).setSaveChannelInfoAfterPay(false)
                .setExtendInfo(payOptionExtendInfo).setChannelInfo(channelInfo).setIssuingCountry("IN");

        Map<String, String> extentInfo = new HashMap<String, String>();
        extentInfo.put("deviceId", "1c4411ac7f-1d222-130f8-12b6c-1bc8e120041e04e");

        final CashierEnvInfo cashierEnvInfo = new CashierEnvInfo.CashierEnvInfoBuilder(null, TerminalType.WEB)
                .sessionId("1c4411ac7f-1d222-130f8-12b6c-1bc8e120041e04e")
                .tokenId("1c4411ac7f-1d222-130f8-12b6c-1bc8e120041e04e").osType("Linux (Ubuntu)")
                .extendInfo(extentInfo).build();

        Map<String, String> extendInfo = new HashMap<String, String>();
        extendInfo.put("totalTxnAmount", "2.0");
        extendInfo.put("retryCount", "0");
        extendInfo.put("txnType", "ONLY_PG");
        extendInfo.put("merchantName", "HYB-AD");
        extendInfo.put("mccCode", "Retail");
        extendInfo.put("productCode", "51051000100000000002");
        extendInfo.put("merchantTransId", "PARCEL113414");
        extendInfo.put("paytmMerchantId", "HYBADD50520222544592");
        extendInfo.put("alipayMerchantId", "216820000000141015249");

        Map<String, String> riskExtendInfo = new HashMap<String, String>();
        riskExtendInfo.put("customerType", "false");

        PaymentRequestBuilder paymentRequestBuilder = new PaymentRequestBuilder(PaymentType.ONLY_PG,
                "20170531111212800110166467300009542", TransType.TOP_UP, requestId, payBillOptionsBuillder.build(),
                cashierEnvInfo).setPayerUserId(null).setSecurityId("sid46080731f639b309323edb2b1141767a")
                .setExtendInfo(extendInfo).setRiskExtendInfo(riskExtendInfo);

        // Payment request
        PaymentRequest paymentRequest = paymentRequestBuilder.build();

        BinDetail binDetail = new BinDetail();
        binDetail.setActive(true);
        binDetail.setBank("HDFC Bank");
        binDetail.setBankCode("HDFC");
        binDetail.setBin((long) 416021);
        binDetail.setCardName("VISA");
        binDetail.setCardType("DEBIT_CARD");
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

        ValidationRequest validationRequest = new ValidationRequest("DC", null, "HDFC", false, "8738529", "8565560",
                psuLimitInfo);

        CashierRequestBuilder builder = new CashierRequestBuilder(acquirementId, requestId, CashierWorkflow.ISOCARD)
                .setCashierMerchant(cashierMerchant).setBinCardRequest(binCardRequest).setCardRequest(cardRequest)
                .setPaymentRequest(paymentRequest).setValidationRequest(validationRequest);

        CashierRequest cashierRequest = builder.build();

        cashierRequest.setFundOrder(false);
        cashierRequest.setIsProcessed(false);
        cashierRequest.setInternalCardRequest(null);
        cashierRequest.setIsDirectBankCardPayRequest(false);
        cashierRequest.setTransCreatedtime("2017-05-31T12:52:09+05:30");
        cashierRequest.setProductCode(ProductCodes.StandardDirectPayAcquiringProdChargePayer);

        return cashierRequest;

    }
}
