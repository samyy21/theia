package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cache.model.BinDetailWithDisplayName;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.services.impl.TheiaSessionDataServiceImpl;
import com.paytm.pgplus.theia.viewmodel.BankInfo;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.Assert;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class BinUtilsTest extends AOAUtilsTest {

    @InjectMocks
    BinUtils binUtils;

    @Mock
    CardUtils cardUtils;

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    TheiaSessionDataServiceImpl theiaSessionDataService;

    @Mock
    EntityPaymentOptionsTO entityPaymentOptionsTO;

    @Test
    public void testValidateBinDetails() {
        Assert.assertFalse(binUtils.validateBinDetails(""));
    }

    @Test
    public void testRetrieveBinDetails() throws PaytmValidationException {
        when(cardUtils.fetchBinDetails(anyString())).thenReturn(null);
        Assert.assertNull(binUtils.retrieveBinDetails("test"));
    }

    @Test
    public void testRetrieveBinDetailsWhenReturnNull() throws PaytmValidationException {
        when(cardUtils.fetchBinDetails(anyString())).thenThrow(new PaytmValidationException("test"));
        Assert.assertNull(binUtils.retrieveBinDetails("test"));
    }

    @Test
    public void testRetrieveBinDetailsWhenReturnBinDetails() throws PaytmValidationException {
        when(cardUtils.fetchBinDetails(anyString())).thenReturn(new BinDetail());
        Assert.assertNotNull(binUtils.retrieveBinDetails("test"));
    }

    @Test
    public void testRetrieveBinDetailsWithDisplayName() throws PaytmValidationException {
        when(cardUtils.fetchBinDetailsWithDisplayName(anyString())).thenReturn(null);
        Assert.assertNull(binUtils.retrieveBinDetailsWithDisplayName("test"));
    }

    @Test
    public void testRetrieveBinDetailsWithDisplayNameWhenReturnNull() throws PaytmValidationException {
        when(cardUtils.fetchBinDetailsWithDisplayName(anyString())).thenThrow(new PaytmValidationException("test"));
        Assert.assertNull(binUtils.retrieveBinDetailsWithDisplayName("test"));
    }

    @Test
    public void testRetrieveBinDetailsWithDisplayNameWhenReturnBinDetailWithDisplayName()
            throws PaytmValidationException {
        when(cardUtils.fetchBinDetailsWithDisplayName(anyString())).thenReturn(new BinDetailWithDisplayName());
        Assert.assertNotNull(binUtils.retrieveBinDetailsWithDisplayName("test"));
    }

    @Test
    public void testCheckIfCardEnabled() {
        Assert.assertFalse(binUtils.checkIfCardEnabled(httpServletRequest, null));

    }

    @Test
    public void testCheckIfCardEnabledWhenReturnFalse() {
        when(httpServletRequest.getParameter(anyString())).thenReturn("1");
        when(theiaSessionDataService.getEntityPaymentOptions(httpServletRequest)).thenReturn(null);
        Assert.assertFalse(binUtils.checkIfCardEnabled(httpServletRequest, new BinDetail()));
    }

    @Test
    public void testCheckIfCardEnabledWhenPayMethodDC() {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setBankName("SBI");

        List<BankInfo> cardList = new ArrayList<BankInfo>();
        cardList.add(bankInfo);

        when(httpServletRequest.getParameter(anyString())).thenReturn("1");
        when(entityPaymentOptionsTO.getAddCompleteDcList()).thenReturn(cardList);
        when(theiaSessionDataService.getEntityPaymentOptions(httpServletRequest)).thenReturn(entityPaymentOptionsTO);

        BinDetail binDetail = new BinDetail();
        binDetail.setCardType("DEBIT_CARD");
        binDetail.setCardName("SBI");
        Assert.assertTrue(binUtils.checkIfCardEnabled(httpServletRequest, binDetail));
    }

    @Test
    public void testCheckIfCardEnabledWhenPayMethodCC() {
        BankInfo bankInfo = new BankInfo();
        bankInfo.setBankName("SBI");

        List<BankInfo> cardList = new ArrayList<BankInfo>();
        cardList.add(bankInfo);

        when(httpServletRequest.getParameter(anyString())).thenReturn("1");
        when(entityPaymentOptionsTO.getAddCompleteCcList()).thenReturn(cardList);
        when(theiaSessionDataService.getEntityPaymentOptions(httpServletRequest)).thenReturn(entityPaymentOptionsTO);

        BinDetail binDetail = new BinDetail();
        binDetail.setCardType("CREDIT_CARD");
        binDetail.setCardName("SBI");
        Assert.assertTrue(binUtils.checkIfCardEnabled(httpServletRequest, binDetail));
    }

    @Test
    public void testCheckIfCardEnabledWhenNullPointerException() {

        List<BankInfo> cardList = new ArrayList<BankInfo>();
        cardList.add(null);

        when(httpServletRequest.getParameter(anyString())).thenReturn("1");
        when(entityPaymentOptionsTO.getAddCompleteDcList()).thenReturn(cardList);
        when(theiaSessionDataService.getEntityPaymentOptions(httpServletRequest)).thenReturn(entityPaymentOptionsTO);

        BinDetail binDetail = new BinDetail();
        binDetail.setCardType("DEBIT_CARD");
        binDetail.setCardName("SBI");
        Assert.assertFalse(binUtils.checkIfCardEnabled(httpServletRequest, binDetail));
    }

    @Test
    public void testCheckIfCardEnabledWhenPayMethodDCReturnFalse() {
        when(httpServletRequest.getParameter(anyString())).thenReturn("2");
        when(entityPaymentOptionsTO.getAddCompleteDcList()).thenReturn(null);
        when(theiaSessionDataService.getEntityPaymentOptions(httpServletRequest)).thenReturn(entityPaymentOptionsTO);

        BinDetail binDetail = new BinDetail();
        binDetail.setCardType("DEBIT_CARD");
        binDetail.setCardName("SBI");
        Assert.assertFalse(binUtils.checkIfCardEnabled(httpServletRequest, binDetail));
    }

    @Test
    public void testCheckIfCardEnabledWhenPayMethodCCReturnFalse() {
        when(httpServletRequest.getParameter(anyString())).thenReturn("2");
        when(entityPaymentOptionsTO.getAddCompleteDcList()).thenReturn(null);
        when(theiaSessionDataService.getEntityPaymentOptions(httpServletRequest)).thenReturn(entityPaymentOptionsTO);

        BinDetail binDetail = new BinDetail();
        binDetail.setCardType("CREDIT_CARD");
        binDetail.setCardName("SBI");
        Assert.assertFalse(binUtils.checkIfCardEnabled(httpServletRequest, binDetail));
    }

}