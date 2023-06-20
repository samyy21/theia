package com.paytm.pgplus.theia.utils.helper;

import com.paytm.pgplus.checksum.utils.AESMerchantService;
import com.paytm.pgplus.checksum.utils.CryptoUtils;
import com.paytm.pgplus.payloadvault.theia.response.ChildTransaction;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.theia.cache.impl.MerchantPreferenceServiceImpl;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class TheiaResponseGeneratorHelperTest extends AOAUtilsTest {

    @InjectMocks
    TheiaResponseGeneratorHelper theiaResponseGeneratorHelper;

    @Mock
    MerchantPreferenceServiceImpl merchantPreferenceService;

    @Mock
    MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Mock
    AESMerchantService aesMerchantService;

    @Test
    public void testEncryptedResponse() {
        when(merchantPreferenceService.isReturnPrepaidEnabled(anyString())).thenReturn(true);
        when(merchantPreferenceService.isChecksumEnabled(anyString())).thenReturn(true);
        when(merchantExtendInfoUtils.getMerchantKey(anyString(), anyString())).thenReturn("test");
        when(merchantPreferenceService.isSendCardSchemeEncryptedParamEnabled(anyString())).thenReturn(true);
        TransactionResponse transactionResponse = new TransactionResponse();
        ChildTransaction childTransaction = new ChildTransaction();
        List<ChildTransaction> testList = new ArrayList<ChildTransaction>();
        testList.add(childTransaction);
        transactionResponse.setOrderId("test");
        transactionResponse.setMid("test");
        transactionResponse.setTxnId("test");
        transactionResponse.setTxnAmount("100");
        transactionResponse.setPaymentMode("CC");
        transactionResponse.setCurrency("test");
        transactionResponse.setTxnDate("10102020");
        transactionResponse.setCustId("test");
        transactionResponse.setTransactionStatus("test");
        transactionResponse.setResponseCode("test");
        transactionResponse.setMerchUniqueReference("test");
        transactionResponse.setUdf1("test");
        transactionResponse.setUdf2("test");
        transactionResponse.setUdf3("test");
        transactionResponse.setAdditionalInfo("test");
        transactionResponse.setGateway("test");
        transactionResponse.setBankTxnId("test");
        transactionResponse.setBinNumber("123456");
        transactionResponse.setLastFourDigits("0000");
        transactionResponse.setPrepaidCard("test");
        transactionResponse.setResponseMsg("test");
        transactionResponse.setChildTxnList(testList);
        transactionResponse.setPromoCode("test");
        transactionResponse.setPromoResponseCode("test");
        transactionResponse.setPromoApplyResultStatus("test");
        transactionResponse.setCardScheme("test");
        transactionResponse.setResponseCode("33");

        Assert.assertNotNull(theiaResponseGeneratorHelper.encryptedResponse(transactionResponse, false, false));
    }

    @Test
    public void testEncryptedResponseWhenIsAES256EncryptedTrue() {
        when(merchantPreferenceService.isReturnPrepaidEnabled(anyString())).thenReturn(true);
        when(merchantPreferenceService.isChecksumEnabled(anyString())).thenReturn(true);
        when(merchantExtendInfoUtils.getMerchantKey(anyString(), anyString())).thenReturn("test");
        when(aesMerchantService.fetchAesEncDecKey(anyString())).thenReturn("test");
        when(merchantPreferenceService.isSendCardSchemeEncryptedParamEnabled(anyString())).thenReturn(true);
        TransactionResponse transactionResponse = new TransactionResponse();
        ChildTransaction childTransaction = new ChildTransaction();
        List<ChildTransaction> testList = new ArrayList<ChildTransaction>();
        testList.add(childTransaction);
        transactionResponse.setOrderId("test");
        transactionResponse.setMid("test");
        transactionResponse.setTxnId("test");
        transactionResponse.setTxnAmount("100");
        transactionResponse.setPaymentMode("CC");
        transactionResponse.setCurrency("test");
        transactionResponse.setTxnDate("10102020");
        transactionResponse.setCustId("test");
        transactionResponse.setTransactionStatus("test");
        transactionResponse.setResponseCode("test");
        transactionResponse.setMerchUniqueReference("test");
        transactionResponse.setUdf1("test");
        transactionResponse.setUdf2("test");
        transactionResponse.setUdf3("test");
        transactionResponse.setAdditionalInfo("test");
        transactionResponse.setGateway("test");
        transactionResponse.setBankTxnId("test");
        transactionResponse.setBinNumber("123456");
        transactionResponse.setLastFourDigits("0000");
        transactionResponse.setPrepaidCard("test");
        transactionResponse.setResponseMsg("test");
        transactionResponse.setChildTxnList(testList);
        transactionResponse.setPromoCode("test");
        transactionResponse.setPromoResponseCode("test");
        transactionResponse.setPromoApplyResultStatus("test");
        transactionResponse.setCardScheme("test");
        transactionResponse.setResponseCode("33");

        Assert.assertNotNull(theiaResponseGeneratorHelper.encryptedResponse(transactionResponse, true, false));
    }

    @Test
    public void testEncryptedResponse_PCFDetailsANDCardScheme_AESDisabled() {

        String key = "F1KMRebvXU+Y8r6rLr7jLF6lriHNtRfX4ArVmudkACk=";
        String decKey = null;
        try {
            decKey = CryptoUtils.decrypt(key);
        } catch (Exception e) {
            Assert.fail();
        }

        mockServices(key, decKey, true);

        TransactionResponse transactionResponse = getTxnResponse();

        StringBuilder res = theiaResponseGeneratorHelper.encryptedResponse(transactionResponse, false, false);
        int ind = res.indexOf(TheiaConstant.ResponseConstants.ENC_PARAMS);
        String encData = res.substring(ind + TheiaConstant.ResponseConstants.ENC_PARAMS.length()).substring(9,
                res.substring(ind + TheiaConstant.ResponseConstants.ENC_PARAMS.length()).length() - 3);
        try {
            Assert.assertNotNull(res);
            String decryptedResponse = CryptoUtils.decrypt(encData, decKey);
            List<String> list = Arrays.asList(decryptedResponse.split("[|]"));
            Assert.assertTrue(list.stream().anyMatch(e -> e.contains("cardScheme")));
            Assert.assertTrue(list.stream().anyMatch(e -> e.contains("PCFDetails")));
            Assert.assertTrue(list.stream().anyMatch(e -> e.contains("COUNTRY_CODE")));
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testEncryptedResponse_PCFDetailsANDCardScheme_AESEnabled() {

        String key = "F1KMRebvXU+Y8r6rLr7jLF6lriHNtRfX4ArVmudkACk=";
        String decKey = null;
        try {
            decKey = CryptoUtils.decrypt(key);
        } catch (Exception e) {
            Assert.fail();
        }

        mockServices(key, decKey, true);

        TransactionResponse transactionResponse = getTxnResponse();

        StringBuilder res = theiaResponseGeneratorHelper.encryptedResponse(transactionResponse, true, false);
        int ind = res.indexOf(TheiaConstant.ResponseConstants.ENC_PARAMS);
        String encData = res.substring(ind + TheiaConstant.ResponseConstants.ENC_PARAMS.length()).substring(9,
                res.substring(ind + TheiaConstant.ResponseConstants.ENC_PARAMS.length()).length() - 3);
        try {
            Assert.assertNotNull(res);
            String decryptedResponse = CryptoUtils.decrypt(encData, decKey);
            List<String> list = Arrays.asList(decryptedResponse.split("[|]"));
            Assert.assertTrue(list.stream().anyMatch(e -> e.contains("cardScheme")));
            Assert.assertTrue(list.stream().anyMatch(e -> e.contains("PCFDetails")));
            Assert.assertTrue(list.stream().anyMatch(e -> e.contains("COUNTRY_CODE")));
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testEncryptedResponse_PCFDetailsANDCardScheme_PreferenceFalse() {

        String key = "F1KMRebvXU+Y8r6rLr7jLF6lriHNtRfX4ArVmudkACk=";
        String decKey = null;
        try {
            decKey = CryptoUtils.decrypt(key);
        } catch (Exception e) {
            Assert.fail();
        }

        mockServices(key, decKey, false);

        TransactionResponse transactionResponse = getTxnResponse();

        StringBuilder res = theiaResponseGeneratorHelper.encryptedResponse(transactionResponse, true, false);
        int ind = res.indexOf(TheiaConstant.ResponseConstants.ENC_PARAMS);
        String encData = res.substring(ind + TheiaConstant.ResponseConstants.ENC_PARAMS.length()).substring(9,
                res.substring(ind + TheiaConstant.ResponseConstants.ENC_PARAMS.length()).length() - 3);
        try {
            Assert.assertNotNull(res);
            String decryptedResponse = CryptoUtils.decrypt(encData, decKey);
            List<String> list = Arrays.asList(decryptedResponse.split("[|]"));
            Assert.assertFalse(list.stream().anyMatch(e -> e.contains("cardScheme")));
            Assert.assertFalse(list.stream().anyMatch(e -> e.contains("PCFDetails")));
            Assert.assertFalse(list.stream().anyMatch(e -> e.contains("COUNTRY_CODE")));
        } catch (Exception e) {
            Assert.fail();
        }
    }

    private TransactionResponse getTxnResponse() {
        TransactionResponse transactionResponse = new TransactionResponse();

        ChildTransaction childTransaction = new ChildTransaction();

        List<ChildTransaction> testList = new ArrayList<ChildTransaction>();
        testList.add(childTransaction);

        transactionResponse.setOrderId("test");
        transactionResponse.setMid("test");
        transactionResponse.setTxnId("test");
        transactionResponse.setTxnAmount("100");
        transactionResponse.setPaymentMode("CC");
        transactionResponse.setCurrency("test");
        transactionResponse.setTxnDate("10102020");
        transactionResponse.setCustId("test");
        transactionResponse.setTransactionStatus("test");
        transactionResponse.setResponseCode("test");
        transactionResponse.setMerchUniqueReference("test");
        transactionResponse.setUdf1("test");
        transactionResponse.setUdf2("test");
        transactionResponse.setUdf3("test");
        transactionResponse.setAdditionalInfo("test");
        transactionResponse.setGateway("test");
        transactionResponse.setBankTxnId("test");
        transactionResponse.setBinNumber("123456");
        transactionResponse.setLastFourDigits("0000");
        transactionResponse.setPrepaidCard("test");
        transactionResponse.setResponseMsg("test");
        transactionResponse.setChildTxnList(testList);
        transactionResponse.setPromoCode("test");
        transactionResponse.setPromoResponseCode("test");
        transactionResponse.setPromoApplyResultStatus("test");
        transactionResponse.setCardScheme("test");
        transactionResponse.setResponseCode("33");
        transactionResponse.setCardScheme("cardScheme");
        transactionResponse.setChargeAmount("25372");
        return transactionResponse;
    }

    private void mockServices(String key, String decKey, boolean pref) {
        when(merchantPreferenceService.isReturnPrepaidEnabled(anyString())).thenReturn(true);
        when(merchantPreferenceService.isChecksumEnabled(anyString())).thenReturn(false);
        when(merchantExtendInfoUtils.getMerchantKey(anyString(), anyString())).thenReturn(key);
        when(aesMerchantService.fetchAesEncDecKey(anyString())).thenReturn(decKey);
        when(merchantPreferenceService.isSendCardSchemeEncryptedParamEnabled(anyString())).thenReturn(pref);
        when(merchantPreferenceService.isSendPCFDetailsEncryptedParamEnabled(anyString())).thenReturn(pref);
        when(merchantPreferenceService.isSendCountryCode(anyString())).thenReturn(pref);
    }
}