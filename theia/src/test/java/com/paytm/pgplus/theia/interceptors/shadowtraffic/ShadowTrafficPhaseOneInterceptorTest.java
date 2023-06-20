package com.paytm.pgplus.theia.interceptors.shadowtraffic;

import com.paytm.pgplus.theia.cache.impl.MerchantPreferenceServiceImpl;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.StagingRequestException;
import com.paytm.pgplus.theia.filter.MultiReadHttpServletRequestWrapper;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.utils.StagingParamValidator;
import mockit.MockUp;
import org.junit.Test;
import org.mockito.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.FAST_FORWARD_URL;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*;

public class ShadowTrafficPhaseOneInterceptorTest {

    @InjectMocks
    private ShadowTrafficPhaseOneInterceptor shadowTrafficPhaseOneInterceptor = new ShadowTrafficPhaseOneInterceptor();

    @Mock
    protected MerchantPreferenceServiceImpl merchantPreferenceService;

    @Mock
    protected ITheiaViewResolverService theiaViewResolverService;

    @Mock
    private StagingParamValidator stagingParamValidator;

    @Mock
    private ShadowTrafficUtil shadowTrafficUtil;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testPreHandle() throws Exception {
        HttpServletRequest request = mock(MultiReadHttpServletRequestWrapper.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn(TheiaConstant.ExtraConstants.REQUEST_METHOD_OPTIONS);
        shadowTrafficPhaseOneInterceptor.preHandle(request, response, "handler");

        when(request.getMethod()).thenReturn("method");
        when(request.getParameter(any())).thenReturn("mid");
        shadowTrafficPhaseOneInterceptor.preHandle(request, response, "handler");

        new MockUp<ShadowTrafficUtil>() {
            @mockit.Mock
            public boolean isShadowRequest(HttpServletRequest request) {
                return true;
            }
        };
        new MockUp<ShadowTrafficUtil>() {
            @mockit.Mock
            public boolean isGlobalShadowSwitchEnabled() {
                return true;
            }
        };
        when(request.getRequestURI()).thenReturn("uri");
        when(merchantPreferenceService.isMockMerchant(any())).thenReturn(false);
        shadowTrafficPhaseOneInterceptor.preHandle(request, response, "handler");

        when(merchantPreferenceService.isMockMerchant(any())).thenReturn(true);
        shadowTrafficPhaseOneInterceptor.preHandle(request, response, "handler");

        when(merchantPreferenceService.isMockMerchant(any())).thenReturn(false);
        new MockUp<ShadowTrafficUtil>() {
            @mockit.Mock
            public boolean isShadowRequest(HttpServletRequest request) {
                return false;
            }
        };
        shadowTrafficPhaseOneInterceptor.preHandle(request, response, "handler");

        new MockUp<ShadowTrafficUtil>() {
            @mockit.Mock
            public boolean isGlobalShadowSwitchEnabled() {
                return false;
            }
        };
        shadowTrafficPhaseOneInterceptor.preHandle(request, response, "handler");

        new MockUp<ShadowTrafficUtil>() {
            @mockit.Mock
            public boolean isShadowRequest(HttpServletRequest request) {
                return true;
            }
        };
        shadowTrafficPhaseOneInterceptor.preHandle(request, response, "handler");

        when(request.getRequestURI()).thenReturn(FAST_FORWARD_URL);
        when(((MultiReadHttpServletRequestWrapper) request).getMessageBody()).thenReturn(
                "{\"head\":{\"name\":\"xyz\"},\"body\":{\"name\":\"xyz\"}}");
        shadowTrafficPhaseOneInterceptor.preHandle(request, response, "handler");

        exceptionRule.expect(StagingRequestException.class);
        when(request.getRequestURI()).thenReturn("uri");
        when(stagingParamValidator.isCustomPageEnabledForURL(any())).thenReturn(true);
        when(stagingParamValidator.midOrderIDCheck(any())).thenReturn(false);
        shadowTrafficPhaseOneInterceptor.preHandle(request, response, "handler");
    }

    @Test
    public void testAfterCompletion() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        shadowTrafficPhaseOneInterceptor.afterCompletion(request, response, "handler", new Exception());
    }
}