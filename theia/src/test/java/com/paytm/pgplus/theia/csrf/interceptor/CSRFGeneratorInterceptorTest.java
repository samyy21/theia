package com.paytm.pgplus.theia.csrf.interceptor;

import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.csrf.CSRFTokenManager;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import mockit.MockUp;
import org.junit.Test;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CSRFGeneratorInterceptorTest {

    private CSRFGeneratorInterceptor csrfGeneratorInterceptor;

    @Test
    public void testPreHandle() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        CSRFTokenManager csrfTokenManager = mock(CSRFTokenManager.class);
        csrfGeneratorInterceptor = new CSRFGeneratorInterceptor(csrfTokenManager);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn(TheiaConstant.ExtraConstants.REQUEST_METHOD_OPTIONS);
        csrfGeneratorInterceptor.preHandle(request, response, "handler");

        when(request.getMethod()).thenReturn("methodOption");
        new MockUp<ConfigurationUtil>() {
            @mockit.Mock
            public String getProperty(String key, String defaultValue) {
                return "0";
            }
        };
        csrfGeneratorInterceptor.preHandle(request, response, "handler");

        new MockUp<ConfigurationUtil>() {
            @mockit.Mock
            public String getProperty(String key, String defaultValue) {
                return "2";
            }
        };
        csrfGeneratorInterceptor.preHandle(request, response, "handler");
    }
}