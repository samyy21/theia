package com.paytm.pgplus.theia.interceptors.shadowtraffic;

import com.paytm.pgplus.theia.cache.impl.MerchantPreferenceServiceImpl;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NativeShadowTrafficPhaseOneInterceptorTest {

    @InjectMocks
    private NativeShadowTrafficPhaseOneInterceptor nativeShadowTrafficPhaseOneInterceptor = new NativeShadowTrafficPhaseOneInterceptor();

    @Mock
    protected MerchantPreferenceServiceImpl merchantPreferenceService;

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
        nativeShadowTrafficPhaseOneInterceptor.preHandle(request, response, "handler");

        when(request.getMethod()).thenReturn("method");
        when(request.getParameter(any())).thenReturn(null);
        nativeShadowTrafficPhaseOneInterceptor.preHandle(request, response, "handler");

        when(request.getParameter(any())).thenReturn("mid");
        when(merchantPreferenceService.isMockMerchant(any())).thenReturn(true);
        PrintWriter printWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(printWriter);
        nativeShadowTrafficPhaseOneInterceptor.preHandle(request, response, "handler");

        when(merchantPreferenceService.isMockMerchant(any())).thenReturn(false);
        nativeShadowTrafficPhaseOneInterceptor.preHandle(request, response, "handler");
    }
}