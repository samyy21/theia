package com.paytm.pgplus.theia.csrf.impl;

import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import javax.servlet.http.HttpServletRequest;

public class DefaultRequiresCsrfMatcherTest {

    @Test
    public void testMatches() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        DefaultRequiresCsrfMatcher defaultRequiresCsrfMatcher = new DefaultRequiresCsrfMatcher();
        when(request.getMethod()).thenReturn("method");
        defaultRequiresCsrfMatcher.matches(request);
    }
}