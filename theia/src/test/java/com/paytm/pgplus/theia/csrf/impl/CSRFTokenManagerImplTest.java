package com.paytm.pgplus.theia.csrf.impl;

import com.paytm.pgplus.theia.csrf.CSRFToken;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import org.junit.Test;
import org.springframework.web.util.WebUtils;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class CSRFTokenManagerImplTest {

    private CSRFTokenManagerImpl csrfTokenManager;

    @Test
    public void testGenerateTokenForSession() {
        ITheiaSessionDataService sessionDataService = mock(ITheiaSessionDataService.class);
        csrfTokenManager = new CSRFTokenManagerImpl(sessionDataService);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession httpSession = mock(HttpSession.class);
        when(sessionDataService.isSessionExists(any())).thenReturn(false);
        when(request.getSession()).thenReturn(httpSession);
        WebUtils.getSessionMutex(httpSession);
        csrfTokenManager.generateTokenForSession(request);

        when(sessionDataService.isSessionExists(any())).thenReturn(true);
        CSRFToken csrfToken = new CSRFToken("token");
        when(request.getSession().getAttribute(any())).thenReturn(csrfToken);
        csrfTokenManager.generateTokenForSession(request);
    }

    @Test
    public void testGetTokenFromRequest() {
        ITheiaSessionDataService sessionDataService = mock(ITheiaSessionDataService.class);
        csrfTokenManager = new CSRFTokenManagerImpl(sessionDataService);
        HttpServletRequest request = mock(HttpServletRequest.class);
        CSRFToken csrfToken = new CSRFToken("token");
        when(request.getParameter(any())).thenReturn(String.valueOf(csrfToken));
        csrfTokenManager.getTokenFromRequest(request);

        when(request.getParameter(any())).thenReturn(null);
        csrfTokenManager.getTokenFromRequest(request);
    }
}