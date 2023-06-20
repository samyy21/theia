package com.paytm.pgplus.theia.csrf.interceptor;

import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.csrf.CSRFInvalidException;
import com.paytm.pgplus.theia.csrf.CSRFToken;
import com.paytm.pgplus.theia.csrf.CSRFTokenManager;
import com.paytm.pgplus.theia.csrf.RequestMatcher;
import com.paytm.pgplus.theia.exceptions.TheiaControllerException;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import mockit.MockUp;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CSRFValidatorInterceptorTest {

    private CSRFValidatorInterceptor csrfValidatorInterceptor;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testPreHandle() throws Exception {
        CSRFTokenManager csrfTokenManager = mock(CSRFTokenManager.class);
        RequestMatcher requestMatcher = mock(RequestMatcher.class);
        ITheiaSessionDataService sessionDataService = mock(ITheiaSessionDataService.class);
        csrfValidatorInterceptor = new CSRFValidatorInterceptor(csrfTokenManager, requestMatcher, sessionDataService);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn(TheiaConstant.ExtraConstants.REQUEST_METHOD_OPTIONS);
        csrfValidatorInterceptor.preHandle(request, response, "handler");

        when(request.getMethod()).thenReturn("methodOptions");
        new MockUp<ConfigurationUtil>() {
            @mockit.Mock
            public String getProperty(String key, String defaultValue) {
                return "0";
            }
        };
        csrfValidatorInterceptor.preHandle(request, response, "handler");

        new MockUp<ConfigurationUtil>() {
            @mockit.Mock
            public String getProperty(String key, String defaultValue) {
                return "1";
            }
        };
        csrfValidatorInterceptor.preHandle(request, response, "handler");

        try {
            when(requestMatcher.matches(any())).thenReturn(true);
            when(sessionDataService.isSessionExists(any())).thenReturn(false);
            csrfValidatorInterceptor.preHandle(request, response, "handler");
        } catch (TheiaControllerException e) {
        }

        exceptionRule.expect(CSRFInvalidException.class);
        when(requestMatcher.matches(any())).thenReturn(true);
        when(sessionDataService.isSessionExists(any())).thenReturn(true);
        when(csrfTokenManager.loadTokenFromSession(any())).thenReturn(new CSRFToken("token"));
        when(csrfTokenManager.getTokenFromRequest(any())).thenReturn(new CSRFToken("newToken"));
        when(request.getRequestURL()).thenReturn(new StringBuffer());
        csrfValidatorInterceptor.preHandle(request, response, "handler");
    }

}