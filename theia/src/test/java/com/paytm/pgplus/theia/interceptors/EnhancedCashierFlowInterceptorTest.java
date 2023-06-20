package com.paytm.pgplus.theia.interceptors;

import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.filter.MultiReadHttpServletRequestWrapper;
import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EnhancedCashierFlowInterceptorTest {

    private EnhancedCashierFlowInterceptor enhancedCashierFlowInterceptor = new EnhancedCashierFlowInterceptor();

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
        enhancedCashierFlowInterceptor.preHandle(request, response, "handler");

        when(request.getMethod()).thenReturn("method");
        when(((MultiReadHttpServletRequestWrapper) request).getMessageBody()).thenReturn(
                "{\"head\":{\"workFlow\":\"enhancedCashierFlow\",\"paymentCallFromDccPage\":\"enhancedCashierFlow\"}}");
        enhancedCashierFlowInterceptor.preHandle(request, response, "handler");

        when(((MultiReadHttpServletRequestWrapper) request).getMessageBody()).thenReturn(
                TheiaConstant.EnhancedCashierFlow.WORKFLOW);
        enhancedCashierFlowInterceptor.preHandle(request, response, "handler");

        when(((MultiReadHttpServletRequestWrapper) request).getMessageBody()).thenReturn(
                TheiaConstant.EnhancedCashierFlow.MERCHANT_ERROR_RESPONSE_CODE);
        enhancedCashierFlowInterceptor.preHandle(request, response, "handler");
    }
}