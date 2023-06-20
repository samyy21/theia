package com.paytm.pgplus.theia.interceptors;

import com.paytm.pgplus.theia.constants.TheiaConstant;
import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DisablePaymentModeForAddCardFlowInterceptorTest {

    private DisablePaymentModeForAddCardFlowInterceptor disablePaymentModeForAddCardFlowInterceptor = new DisablePaymentModeForAddCardFlowInterceptor();

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
        disablePaymentModeForAddCardFlowInterceptor.preHandle(request, response, "handler");

        when(request.getMethod()).thenReturn("method");
        when(request.getParameter(any())).thenReturn(TheiaConstant.RequestParams.PAYMENT_MODE_DISABLED);
        when(request.getAttribute(any())).thenReturn(TheiaConstant.RequestParams.PAYMENT_MODE_DISABLED);
        disablePaymentModeForAddCardFlowInterceptor.preHandle(request, response, "handler");

        when(request.getAttribute(any())).thenReturn(TheiaConstant.RequestParams.OAUTH_SSOID);
        disablePaymentModeForAddCardFlowInterceptor.preHandle(request, response, "handler");

        when(request.getParameter(any())).thenReturn(null);
        disablePaymentModeForAddCardFlowInterceptor.preHandle(request, response, "handler");

        when(request.getParameter(TheiaConstant.RequestParams.THEME)).thenReturn(
                TheiaConstant.ExtraConstants.ADD_CARD_THEME);
        disablePaymentModeForAddCardFlowInterceptor.preHandle(request, response, "handler");
    }
}