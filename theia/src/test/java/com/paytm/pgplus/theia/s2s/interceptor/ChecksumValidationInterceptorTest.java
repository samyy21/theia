package com.paytm.pgplus.theia.s2s.interceptor;

import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.filter.MultiReadHttpServletRequestWrapper;
import com.paytm.pgplus.theia.s2s.utils.PaymentS2SResponseUtil;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import net.sf.qualitycheck.Check;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.PrintWriter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChecksumValidationInterceptorTest {

    @InjectMocks
    ChecksumValidationInterceptor checksumValidationInterceptor;

    @Mock
    private IMerchantPreferenceService merchantPreferenceService;

    @Mock
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Mock
    private PaymentS2SResponseUtil responseUtil;

    @Test
    public void preHandle() throws Exception {

        MockitoAnnotations.initMocks(this);
        MultiReadHttpServletRequestWrapper request = mock(MultiReadHttpServletRequestWrapper.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getParameter("MID")).thenReturn("").thenReturn("mid").thenReturn("mid").thenReturn("mid");
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));
        assertFalse(checksumValidationInterceptor.preHandle(request, response, new Object()));
        when(merchantPreferenceService.isChecksumEnabled("mid")).thenReturn(true).thenReturn(true).thenReturn(true);
        when(request.getMessageBody()).thenReturn("\"body\"\n:\n{}").thenReturn("\"header\"\n:\n{}")
                .thenReturn("{\"body\"\n:\n{},\"header\"\n:\n{ \"signature\":\"signature\"}}");
        try {
            checksumValidationInterceptor.preHandle(request, response, new Object());
            fail();
        } catch (RuntimeException e) {

        }
        try {
            checksumValidationInterceptor.preHandle(request, response, new Object());
            fail();
        } catch (RuntimeException e) {

        }

        assertFalse(checksumValidationInterceptor.preHandle(request, response, new Object()));

    }
}