package com.paytm.pgplus.theia.csrf;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by ankitsinghal on 22/05/17.
 */
public interface RequestMatcher {
    boolean matches(HttpServletRequest request);
}
