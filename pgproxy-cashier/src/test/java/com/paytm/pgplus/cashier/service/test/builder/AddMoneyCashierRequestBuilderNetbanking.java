package com.paytm.pgplus.cashier.service.test.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.paytm.pgplus.cashier.constant.CashierWorkflow;
import com.paytm.pgplus.cashier.enums.PaymentType;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
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
import com.paytm.pgplus.facade.enums.TerminalType;
import com.paytm.pgplus.facade.enums.TransType;

/**
 * @author Vivek Kumar
 */

@Service
public class AddMoneyCashierRequestBuilderNetbanking {

    public CashierRequest getNetbankingRequest() throws CashierCheckedException {

        // acquirement id
        String acquirementId = "20170531111212800110166467300009542";

        // request id
        String requestId = "f1fd0d30ead74701a8f044245bda7b08administratorthinkpadl450";

        // cashier merchant
        CashierMerchant cashierMerchant = new CashierMerchant("DataCl59062077159771", "216820000000145754283", false, 0);

        Map<PayMethod, String> payOptions = new HashMap<>();
        payOptions.put(PayMethod.NET_BANKING, "NET_BANKING_ICICI");

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
        payOptionExtendInfo.put("ssoToken", "220dd1bd-69b6-4e7f-ab8a-f2c7a35024ff");
        payOptionExtendInfo.put("PAYTM_USER_ID", "10797790");
        payOptionExtendInfo.put("retryCount", "0");
        payOptionExtendInfo.put("txnType", "ONLY_PG");
        payOptionExtendInfo.put("merchantName", "DataClean");
        payOptionExtendInfo.put("productCode", "51053000100000000001");
        payOptionExtendInfo.put("merchantTransId", "PARCEL707105");
        payOptionExtendInfo.put("paytmMerchantId", "DataCl59062077159771");
        payOptionExtendInfo.put("alipayMerchantId", "216820000000145754283");
        payOptionExtendInfo.put("mccCode", "Retail");

        PayBillOptionsBuilder payBillOptionsBuillder = new PayBillOptionsBuilder(200L, 2L, payOptions)
                .setWalletBalance(0L).setTopAndPay(false).setSaveChannelInfoAfterPay(false)
                .setExtendInfo(payOptionExtendInfo).setChannelInfo(channelInfo).setIssuingCountry(null)
                .setPayerAccountNumber("20070000000000002280");

        Map<String, String> extentInfo = new HashMap<String, String>();
        extentInfo.put("deviceId", "198f416b96-1e671-199ec-13531-1ee681b5ff1d985");

        final CashierEnvInfo cashierEnvInfo = new CashierEnvInfo.CashierEnvInfoBuilder(null, TerminalType.WEB)
                .sessionId("198f416b96-1e671-199ec-13531-1ee681b5ff1d985")
                .tokenId("198f416b96-1e671-199ec-13531-1ee681b5ff1d985").osType("Linux (Ubuntu)")
                .extendInfo(extentInfo).build();

        Map<String, String> extendInfo = new HashMap<String, String>();
        extendInfo.put("totalTxnAmount", "2.0");
        extendInfo.put("retryCount", "0");
        extendInfo.put("txnType", "ONLY_PG");
        extendInfo.put("merchantName", "DataClean");
        extendInfo.put("mccCode", "Retail");
        extendInfo.put("productCode", "51053000100000000001");
        extendInfo.put("merchantTransId", "PARCEL707105");
        extendInfo.put("paytmMerchantId", "DataCl59062077159771");
        extendInfo.put("alipayMerchantId", "216820000000145754283");
        extendInfo.put("ssoToken", "220dd1bd-69b6-4e7f-ab8a-f2c7a35024ff");
        extendInfo.put("PAYTM_USER_ID", "10797790");

        Map<String, String> riskExtendInfo = new HashMap<String, String>();
        riskExtendInfo.put("customerType", "false");

        PaymentRequestBuilder paymentRequestBuilder = new PaymentRequestBuilder(PaymentType.ONLY_PG,
                "2017060820121481010100166282800034517", TransType.TOP_UP, requestId, payBillOptionsBuillder.build(),
                cashierEnvInfo).setPayerUserId("216810000000000128283")
                .setSecurityId("sid35703dea26c4e0c63136b23ae4c7b3f7").setExtendInfo(extendInfo)
                .setRiskExtendInfo(riskExtendInfo);

        // Payment request
        PaymentRequest paymentRequest = paymentRequestBuilder.build();

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

        ValidationRequest validationRequest = new ValidationRequest("NB", null, "ICICI", false, "8738955", "8565557",
                psuLimitInfo);

        CashierRequestBuilder builder = new CashierRequestBuilder(acquirementId, requestId,
                CashierWorkflow.ADD_MONEY_NB).setCashierMerchant(cashierMerchant).setPaymentRequest(paymentRequest)
                .setValidationRequest(validationRequest);

        CashierRequest cashierRequest = builder.build();

        cashierRequest.setFundOrder(false);
        cashierRequest.setIsProcessed(false);
        cashierRequest.setInternalCardRequest(null);
        cashierRequest.setIsDirectBankCardPayRequest(false);
        cashierRequest.setTransCreatedtime(null);
        cashierRequest.setProductCode(null);

        return cashierRequest;

    }

}
