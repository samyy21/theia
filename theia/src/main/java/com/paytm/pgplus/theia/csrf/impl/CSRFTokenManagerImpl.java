package com.paytm.pgplus.theia.csrf.impl;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.csrf.CSRFToken;
import com.paytm.pgplus.theia.csrf.CSRFTokenManager;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;

/**
 * Created by ankitsinghal on 17/05/17.
 */
@Component
public class CSRFTokenManagerImpl implements CSRFTokenManager {

    private static final String CSRF_TOKEN_FOR_SESSION_ATTR_NAME = TheiaConstant.SessionDataAttributes.csrfToken.name();

    private ITheiaSessionDataService sessionDataService;

    @Autowired
    public CSRFTokenManagerImpl(@Qualifier("theiaSessionDataService") ITheiaSessionDataService sessionDataService) {
        this.sessionDataService = sessionDataService;
    }

    @Override
    public CSRFToken generateTokenForSession(HttpServletRequest request) {
        CSRFToken token = loadTokenFromSession(request);
        if (null == token) {
            HttpSession session = request.getSession();
            Object mutex = WebUtils.getSessionMutex(session);
            synchronized (mutex) {
                token = new CSRFToken(createNewTokenString());

                session.setAttribute(CSRF_TOKEN_FOR_SESSION_ATTR_NAME, token);
            }
        }
        return token;
    }

    @Override
    public CSRFToken loadTokenFromSession(HttpServletRequest request) {
        if (!sessionDataService.isSessionExists(request)) {
            return null;
        }
        return (CSRFToken) request.getSession().getAttribute(CSRF_TOKEN_FOR_SESSION_ATTR_NAME);
    }

    @Override
    public CSRFToken getTokenFromRequest(HttpServletRequest request) {
        String csrfToken = request.getParameter(CSRFToken.CSRF_PARAM_NAME);
        if (StringUtils.isBlank(csrfToken)) {
            csrfToken = request.getHeader(CSRFToken.CSRF_HEADER_NAME);
        }
        return new CSRFToken(csrfToken);
    }

    private String createNewTokenString() {
        return UUID.randomUUID().toString();
    }
}
