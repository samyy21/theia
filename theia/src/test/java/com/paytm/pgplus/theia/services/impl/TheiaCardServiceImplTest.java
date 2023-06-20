package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.pgproxycommon.models.SavedCardInfo;
import com.paytm.pgplus.savedcardclient.service.ISavedCardService;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.CardInfo;
import com.paytm.pgplus.theia.sessiondata.LoginInfo;
import com.paytm.pgplus.theia.sessiondata.OAuthUserInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class TheiaCardServiceImplTest {

    @InjectMocks
    TheiaCardServiceImpl theiaCardService;

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    ITheiaSessionDataService theiaSessionDataService;

    @Mock
    TransactionInfo txnInfo;

    @Mock
    LoginInfo loginInfo;

    @Mock
    ISavedCardService savedCardService;

    @Mock
    CardInfo cardInfo;

    @Mock
    SavedCardInfo savedCardInfo;

    @Mock
    OAuthUserInfo oAuthUserInfo;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessDeleteCardWhenIsSessionExistsFalse() {
        when(theiaSessionDataService.isSessionExists(any())).thenReturn(false);
        Assert.assertEquals("fail", theiaCardService.processDeleteCard(httpServletRequest, "test", true));
    }

    @Test
    public void testProcessDeleteCardWhenSavedCardIdBlank() {
        when(theiaSessionDataService.isSessionExists(any())).thenReturn(true);
        Assert.assertEquals("fail", theiaCardService.processDeleteCard(httpServletRequest, "", true));
    }

    @Test
    public void testProcessDeleteCardWhenLoginInfoNull() {
        List<SavedCardInfo> savedCardInfoList = new ArrayList<>();
        savedCardInfoList.add(savedCardInfo);

        Map<String, SavedCardInfo> testMap = new HashMap<>();
        testMap.put("123456", savedCardInfo);
        testMap.put("123456", savedCardInfo);
        List<SavedCardInfo> testList = new ArrayList<>();
        testList.add(savedCardInfo);

        when(theiaSessionDataService.isSessionExists(any())).thenReturn(true);
        when(theiaSessionDataService.getLoginInfoFromSession(any())).thenReturn(null);
        when(theiaSessionDataService.getTxnInfoFromSession(any())).thenReturn(txnInfo);
        when(txnInfo.getCustID()).thenReturn("test");
        when(httpServletRequest.getParameter("MID")).thenReturn("test");
        when(theiaSessionDataService.getCardInfoFromSession(any())).thenReturn(cardInfo);
        when(cardInfo.getAddAndPayViewCardsList()).thenReturn(savedCardInfoList);
        when(savedCardInfo.getCardId()).thenReturn(123456L);
        when(cardInfo.getAddAnPaySavedCardMap()).thenReturn(testMap);
        when(cardInfo.getMerchantViewSavedCardsList()).thenReturn(testList);
        when(cardInfo.getSavedCardMap()).thenReturn(testMap);
        Assert.assertEquals("success", theiaCardService.processDeleteCard(httpServletRequest, "123456", true));
    }

    @Test
    public void testProcessDeleteCardWhenLoginInfoNotNull() {
        List<SavedCardInfo> savedCardInfoList = new ArrayList<>();
        savedCardInfoList.add(savedCardInfo);

        Map<String, SavedCardInfo> testMap = new HashMap<>();
        testMap.put("123456", savedCardInfo);
        testMap.put("123456", savedCardInfo);
        List<SavedCardInfo> testList = new ArrayList<>();
        testList.add(savedCardInfo);

        when(theiaSessionDataService.isSessionExists(any())).thenReturn(true);
        when(theiaSessionDataService.getLoginInfoFromSession(any())).thenReturn(loginInfo);
        when(loginInfo.getUser()).thenReturn(oAuthUserInfo);
        when(theiaSessionDataService.getTxnInfoFromSession(any())).thenReturn(txnInfo);
        when(txnInfo.getCustID()).thenReturn("test");
        when(httpServletRequest.getParameter("MID")).thenReturn("test");
        when(theiaSessionDataService.getCardInfoFromSession(any())).thenReturn(cardInfo);
        when(cardInfo.getAddAndPayViewCardsList()).thenReturn(savedCardInfoList);
        when(savedCardInfo.getCardId()).thenReturn(123456L);
        when(cardInfo.getAddAnPaySavedCardMap()).thenReturn(testMap);
        when(cardInfo.getMerchantViewSavedCardsList()).thenReturn(testList);
        when(cardInfo.getSavedCardMap()).thenReturn(testMap);
        Assert.assertEquals("success", theiaCardService.processDeleteCard(httpServletRequest, "123456", true));
    }

    @Test
    public void testProcessDeleteCardWhenTxnInfoNullAndLoginInfoNull() {
        when(theiaSessionDataService.isSessionExists(any())).thenReturn(true);
        when(theiaSessionDataService.getTxnInfoFromSession(any())).thenReturn(txnInfo);
        when(theiaSessionDataService.getLoginInfoFromSession(any())).thenReturn(null);

        Assert.assertEquals("fail", theiaCardService.processDeleteCard(httpServletRequest, "123456", true));
    }

    @Test
    public void testProcessDeleteCardWhenTxnInfoNullAndUserIdBlank() {
        when(theiaSessionDataService.isSessionExists(any())).thenReturn(true);
        when(theiaSessionDataService.getTxnInfoFromSession(any())).thenReturn(txnInfo);
        when(theiaSessionDataService.getLoginInfoFromSession(any())).thenReturn(loginInfo);
        when(loginInfo.getUser()).thenReturn(oAuthUserInfo);
        when(oAuthUserInfo.getUserID()).thenReturn("");
        Assert.assertEquals("fail", theiaCardService.processDeleteCard(httpServletRequest, "123456", true));
    }

    @Test
    public void testProcessDeleteCardWhenTxnInfoNull() {
        when(theiaSessionDataService.isSessionExists(any())).thenReturn(true);
        when(theiaSessionDataService.getTxnInfoFromSession(any())).thenReturn(txnInfo);
        when(theiaSessionDataService.getLoginInfoFromSession(any())).thenReturn(loginInfo);
        when(loginInfo.getUser()).thenReturn(oAuthUserInfo);
        when(oAuthUserInfo.getUserID()).thenReturn("test");
        Assert.assertEquals("fail", theiaCardService.processDeleteCard(httpServletRequest, "123456", true));
    }

}