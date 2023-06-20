package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.helper.UIMicroserviceHelper;
import com.paytm.pgplus.theia.models.uimicroservice.response.UIMicroserviceResponse;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.Assert;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atMost;

public class AddMoneyToGvConsentUtilTest extends AOAUtilsTest {

    @InjectMocks
    AddMoneyToGvConsentUtil addMoneyToGvConsentUtil;

    @Mock
    UIMicroserviceHelper uiMicroserviceHelper;

    @Mock
    UIMicroserviceResponse uiMicroserviceResponse;

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    NativeSessionUtil nativeSessionUtil;

    @Mock
    Object object;

    @Test
    public void testShowConsentPageForRedirection() {
        when(uiMicroserviceResponse.getHtmlPage()).thenReturn("abc");
        when(uiMicroserviceHelper.getHtmlPageFromUI(any(), anyString(), anyString()))
                .thenReturn(uiMicroserviceResponse);
        Assert.assertNotNull(addMoneyToGvConsentUtil.showConsentPageForRedirection("test", "test", true));
    }

    @Test
    public void testShowConsentPageForNativePlus() {
        Assert.assertNotNull(addMoneyToGvConsentUtil.showConsentPageForNativePlus("test", "test"));
    }

    @Test(expected = TheiaServiceException.class)
    public void testSetAttributesForGvConsentFlowWhenThrowsException() {

        when(httpServletRequest.getParameter(anyString())).thenReturn("");
        addMoneyToGvConsentUtil.setAttributesForGvConsentFlow(httpServletRequest);
    }

    @Test(expected = TheiaServiceException.class)
    public void testSetAttributesForGvConsentFlowWhenSessionExpired() {
        when(nativeSessionUtil.getKey(anyString())).thenReturn(null);
        when(httpServletRequest.getParameter(anyString())).thenReturn("test");
        addMoneyToGvConsentUtil.setAttributesForGvConsentFlow(httpServletRequest);
    }

    @Test
    public void testExpireGvConsentFlowSession() {
        doNothing().when(nativeSessionUtil).deleteKey(anyString());
        verify(nativeSessionUtil, atMost(1)).deleteKey(anyString());
        addMoneyToGvConsentUtil.expireGvConsentFlowSession("test");
    }

    @Test
    public void testBypassGvConsentPage() {
        Assert.assertFalse(addMoneyToGvConsentUtil.bypassGvConsentPage());
    }

}