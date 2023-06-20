package com.paytm.pgplus.theia.interceptors.shadowtraffic;

import com.paytm.pgplus.biz.core.risk.RiskConstants;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.filter.MultiReadHttpServletRequestWrapper;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.KYC_FLOW;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NativeShadowTrafficPhaseTwoInterceptorTest {

    @InjectMocks
    private NativeShadowTrafficPhaseTwoInterceptor nativeShadowTrafficPhaseTwoInterceptor = new NativeShadowTrafficPhaseTwoInterceptor();

    @Mock
    private NativeSessionUtil nativeSessionUtil;

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
        nativeShadowTrafficPhaseTwoInterceptor.preHandle(request, response, "handler");

        when(request.getMethod()).thenReturn("method");
        when(request.getParameter(KYC_FLOW)).thenReturn("yes");
        nativeShadowTrafficPhaseTwoInterceptor.preHandle(request, response, "handler");

        when(request.getParameter(KYC_FLOW)).thenReturn("no");
        when(request.getParameter(TheiaConstant.GvConsent.GV_CONSENT_FLOW)).thenReturn("true");
        nativeShadowTrafficPhaseTwoInterceptor.preHandle(request, response, "handler");

        when(request.getParameter(TheiaConstant.GvConsent.GV_CONSENT_FLOW)).thenReturn("false");
        when(request.getParameter(RiskConstants.RISK_VERIFIER_UI_KEY)).thenReturn("true");
        nativeShadowTrafficPhaseTwoInterceptor.preHandle(request, response, "handler");

        when(request.getParameter(RiskConstants.RISK_VERIFIER_UI_KEY)).thenReturn("false");
        when(request.getRequestURI()).thenReturn(TheiaConstant.ExtraConstants.NATIV_FETCH_PAYMENTOPTIONS_URL_V2);
        when(((MultiReadHttpServletRequestWrapper) request).getMessageBody()).thenReturn(
                "{\"head\":{\"name\":\"xyz\"},\"body\":{\"name\":\"xyz\"}}");
        nativeShadowTrafficPhaseTwoInterceptor.preHandle(request, response, "handler");

        when(((MultiReadHttpServletRequestWrapper) request).getMessageBody()).thenReturn(
                "{\"head\":{\"txnToken\":\"xyz\"},\"body\":{\"name\":\"xyz\"}}");
        nativeShadowTrafficPhaseTwoInterceptor.preHandle(request, response, "handler");

        when(request.getRequestURI()).thenReturn(TheiaConstant.ExtraConstants.NATIV_PROCESS_TRANSACTION_URL);
        when(request.getParameter(any())).thenReturn("txnToken");
        nativeShadowTrafficPhaseTwoInterceptor.preHandle(request, response, "handler");

        when(request.getParameter(any())).thenReturn(null);
        nativeShadowTrafficPhaseTwoInterceptor.preHandle(request, response, "handler");

        when(((MultiReadHttpServletRequestWrapper) request).getMessageBody()).thenReturn(
                "{\"head\":{\"name\":\"xyz\"},\"body\":{\"name\":\"xyz\"}}");
        nativeShadowTrafficPhaseTwoInterceptor.preHandle(request, response, "handler");

        when(request.getRequestURI()).thenReturn(TheiaConstant.ExtraConstants.NATIVE_APP_INVOKE_URL);
        when(request.getParameter(any())).thenReturn("txnToken");
        nativeShadowTrafficPhaseTwoInterceptor.preHandle(request, response, "handler");

        when(request.getParameter(any())).thenReturn(null);
        nativeShadowTrafficPhaseTwoInterceptor.preHandle(request, response, "handler");

        when(request.getRequestURI()).thenReturn(TheiaConstant.ExtraConstants.APPLY_PROMO_URL_V2);
        when(((MultiReadHttpServletRequestWrapper) request).getMessageBody()).thenReturn(
                "{\"head\":{\"token\":\"xyz\"},\"body\":{\"name\":\"xyz\"}}");
        when(nativeSessionUtil.isMockRequest(any())).thenReturn("true");
        nativeShadowTrafficPhaseTwoInterceptor.preHandle(request, response, "handler");
    }
}