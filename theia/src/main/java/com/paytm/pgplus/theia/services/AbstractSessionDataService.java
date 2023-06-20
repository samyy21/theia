package com.paytm.pgplus.theia.services;

import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;

/**
 * Created by ankitsinghal on 24/05/17.
 */

public abstract class AbstractSessionDataService implements ITheiaSessionDataService {

    private static final long serialVersionUID = 7312692464509365766L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSessionDataService.class);

    @Override
    public void setRedirectPageInSession(HttpServletRequest request, String redirectPage) {
        try {
            setRedirectPageInSession(request, redirectPage, false);
        } catch (Exception e) {
            LOGGER.info("Issue while setting redirect page in Session");
        }
    }

    @Override
    public void setRedirectPageInSession(HttpServletRequest request, String redirectPage, boolean createNew) {
        Assert.isTrue(StringUtils.isNotBlank(redirectPage), "Redirect Page to be returned cannot be blank");

        request.getSession(createNew)
                .setAttribute(TheiaConstant.ResponseConstants.RESPONSE_BANK_HTML_KEY, redirectPage);
    }

    @Override
    public boolean isSessionExists(HttpServletRequest request) {
        return request.getSession(false) != null;
    }

    @Override
    public boolean validateSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String orderId = request.getParameter(TheiaConstant.RequestParams.ORDER_ID);
        if (StringUtils.isBlank(orderId)) {
            Object orderIdAttr = request.getAttribute(TheiaConstant.RequestParams.ORDER_ID);
            if (orderIdAttr != null) {
                orderId = (String) orderIdAttr;
            }
        }

        String mid = request.getParameter(TheiaConstant.RequestParams.MID);

        if (session == null) {
            LOGGER.error("Session does not exist for mid : {} , orderId : {}", mid, orderId);
            throw new PaymentRequestValidationException("session does not exist");
        }

        Object value = session.getAttribute(TheiaConstant.SessionDataAttributes.orderIdMid.name());

        if (value == null) {
            session.setAttribute(TheiaConstant.SessionDataAttributes.orderIdMid.name(), orderId + "|" + mid);
            return true;
        }

        boolean isEnhancedNativeFlow = false;
        if (null != request.getAttribute("NATIVE_ENHANCED_FLOW")
                && Boolean.TRUE.equals(request.getAttribute("NATIVE_ENHANCED_FLOW"))) {
            isEnhancedNativeFlow = true;
        }

        String orderIdMid = value.toString();
        if (orderIdMid.equals(orderId + "|" + mid) && !isEnhancedNativeFlow) {
            return false;
        }

        Enumeration<String> sessionAttributes = session.getAttributeNames();
        while (sessionAttributes.hasMoreElements()) {
            session.removeAttribute(sessionAttributes.nextElement());
        }
        session.setAttribute(TheiaConstant.SessionDataAttributes.orderIdMid.name(), orderId + "|" + mid);
        return true;
    }

    @Override
    public boolean validateSession(HttpServletRequest request, boolean isRetry) {
        HttpSession session = request.getSession();
        String orderId = request.getParameter(TheiaConstant.RequestParams.ORDER_ID);
        String mid = request.getParameter(TheiaConstant.RequestParams.MID);

        if (session == null) {
            LOGGER.error("Session does not exist for mid : {} , orderId : {}", mid, orderId);
            throw new PaymentRequestValidationException("session does not exist");
        }

        Object value = session.getAttribute(TheiaConstant.SessionDataAttributes.orderIdMid.name());
        final TransactionInfo txnInfo = (TransactionInfo) session
                .getAttribute(TheiaConstant.SessionDataAttributes.txnInfo.name());

        if (txnInfo != null) {
            txnInfo.setRetry(isRetry);
            txnInfo.setDisplayMsg("Sorry, your transaction couldn't be processed. Please try again.");
        }

        if (value == null) {
            session.setAttribute(TheiaConstant.SessionDataAttributes.orderIdMid.name(), orderId + "|" + mid);
            return true;
        }
        String orderIdMid = value.toString();
        if (orderIdMid.equals(orderId + "|" + mid)) {
            return false;
        }

        Enumeration<String> sessionAttributes = session.getAttributeNames();
        while (sessionAttributes.hasMoreElements()) {
            session.removeAttribute(sessionAttributes.nextElement());
        }
        session.setAttribute(TheiaConstant.SessionDataAttributes.orderIdMid.name(), orderId + "|" + mid);
        return true;
    }

    @Override
    public void removeAttributeFromSession(HttpServletRequest request,
            TheiaConstant.SessionDataAttributes sessionDataAttributes) {
        HttpSession session = request.getSession();
        session.removeAttribute(sessionDataAttributes.name());
    }
}
