package com.paytm.pgplus.theia.csrf;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by ankitsinghal on 18/05/17.
 */
public interface CSRFTokenManager {

    CSRFToken generateTokenForSession(HttpServletRequest request);

    CSRFToken getTokenFromRequest(HttpServletRequest request);

    CSRFToken loadTokenFromSession(HttpServletRequest request);
}
