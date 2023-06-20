package com.paytm.pgplus.theia.interceptors.shadowtraffic;

import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import mockit.MockUp;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import static org.mockito.Mockito.when;

public class ShadowTrafficUtilTest {

    @Test
    public void testAll() {
        ShadowTrafficUtil.unsetShadowContext();
        new MockUp<ConfigurationUtil>() {
            @mockit.Mock
            public String getProperty(String key, String defaultValue) {
                return "true";
            }
        };
        ShadowTrafficUtil.isGlobalShadowSwitchEnabled();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(any())).thenReturn(null);
        ShadowTrafficUtil.isShadowRequest(request);
        when(request.getParameter(any())).thenReturn("true");
        ShadowTrafficUtil.isShadowRequest(request);
        ShadowTrafficUtil.clearMDC();
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(null);
        ShadowTrafficUtil.setSessionAndAttributesForShadowContext(request);
        when(request.getSession()).thenReturn(session);
        ShadowTrafficUtil.setSessionAndAttributesForShadowContext(request);
        ShadowTrafficUtil.setAttributesForShadowContext();
    }
}