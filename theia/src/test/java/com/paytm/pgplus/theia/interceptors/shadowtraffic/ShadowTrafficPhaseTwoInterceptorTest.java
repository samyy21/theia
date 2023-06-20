package com.paytm.pgplus.theia.interceptors.shadowtraffic;

import com.paytm.pgplus.cache.model.TransactionInfo;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import static com.paytm.pgplus.theia.interceptors.shadowtraffic.ShadowTrafficPhaseTwoInterceptor.TRANSACTION_STATUS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ShadowTrafficPhaseTwoInterceptorTest {

    @InjectMocks
    private ShadowTrafficPhaseTwoInterceptor shadowTrafficPhaseTwoInterceptor = new ShadowTrafficPhaseTwoInterceptor();

    @Mock
    private TransactionCacheUtils transactionCacheUtils;

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
        shadowTrafficPhaseTwoInterceptor.preHandle(request, response, "handler");

        when(request.getMethod()).thenReturn("method");
        when(request.getRequestURI()).thenReturn(TRANSACTION_STATUS);
        shadowTrafficPhaseTwoInterceptor.preHandle(request, response, "handler");

        when(transactionCacheUtils.getTransInfoFromCache(any())).thenReturn(new TransactionInfo());
        shadowTrafficPhaseTwoInterceptor.preHandle(request, response, "handler");

        when(request.getRequestURI()).thenReturn("uri");
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(any())).thenReturn("true");
        when(request.getSession()).thenReturn(session);
        shadowTrafficPhaseTwoInterceptor.preHandle(request, response, "handler");
    }

    @Test
    public void testAfterCompletion() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        shadowTrafficPhaseTwoInterceptor.afterCompletion(request, response, "handler", new Exception());
    }
}