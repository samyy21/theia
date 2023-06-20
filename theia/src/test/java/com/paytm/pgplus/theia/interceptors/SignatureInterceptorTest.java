package com.paytm.pgplus.theia.interceptors;

import com.paytm.pgplus.common.signature.wrapper.SignatureUtilWrapper;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import mockit.MockUp;
import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.paytm.pgplus.theia.interceptors.SignatureInterceptor.TRANSACTION_STATUS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SignatureInterceptorTest {

    @InjectMocks
    private SignatureInterceptor signatureInterceptor = new SignatureInterceptor();

    @Mock
    private ITheiaViewResolverService theiaViewResolverService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testPreHandle() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn(TheiaConstant.ExtraConstants.REQUEST_METHOD_OPTIONS);
        signatureInterceptor.preHandle(request, response, "handler");

        when(request.getMethod()).thenReturn("method");
        when(request.getRequestURI()).thenReturn("uri");
        signatureInterceptor.preHandle(request, response, "handler");

        when(request.getRequestURI()).thenReturn(TRANSACTION_STATUS);
        when(request.getHeader(any())).thenReturn(MediaType.APPLICATION_JSON_VALUE);
        signatureInterceptor.preHandle(request, response, "handler");

        when(request.getRequestURI()).thenReturn("uri");
        new MockUp<ConfigurationUtil>() {
            @mockit.Mock
            public String getProperty(String key) {
                return TheiaConstant.ExtraConstants.SIGNATURE_REQUIRED;
            }
        };
        when(request.getParameter(any())).thenReturn("signature");
        new MockUp<SignatureUtilWrapper>() {
            @mockit.Mock
            public boolean verifySignature(String response, String signature) {
                return true;
            }
        };
        signatureInterceptor.preHandle(request, response, "handler");

        new MockUp<SignatureUtilWrapper>() {
            @mockit.Mock
            public boolean verifySignature(String response, String signature) {
                return false;
            }
        };
        signatureInterceptor.preHandle(request, response, "handler");
    }
}