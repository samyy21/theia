/**
 *
 */
package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.common.statistics.StatisticConstants;
import com.paytm.pgplus.common.statistics.StatisticsLogger;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.sessiondata.ThemeInfo;
import com.paytm.pgplus.theia.utils.StagingParamValidator;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static com.paytm.pgplus.biz.utils.BizConstant.FAILURE_RESULT_STATUS;
import static com.paytm.pgplus.biz.utils.BizConstant.SUCCESS_RESULT_STATUS;

/**
 * @author kesari
 * @createdOn 27-Mar-2016
 */
@Service(value = "theiaViewResolverService")
public class TheiaViewResolverServiceImpl implements ITheiaViewResolverService {

    private static final long serialVersionUID = -6184297475832442705L;

    private static final Logger LOGGER = LoggerFactory.getLogger(TheiaViewResolverServiceImpl.class);

    @Autowired
    @Qualifier(value = "theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    private StagingParamValidator stagingParamValidator;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Override
    public String returnOOPSPage(HttpServletRequest request) {
        logMerchantResponseOOps(request);
        HttpSession session = request.getSession(false);
        if (session != null) {
            LOGGER.info("sessionInvalidating");
            session.invalidate();
        }
        if (stagingParamValidator.isCustomPageEnabledForURL(request.getServerName())) {
            return ViewNames.STAGE_OOPS_PAGE;
        }
        theiaResponseGenerator.pushPtcResultStatusToStatsD(FAILURE_RESULT_STATUS);
        return ViewNames.OOPS_PAGE;
    }

    @Override
    public String returnErrorPage(HttpServletRequest request) {
        String viewName = ViewNames.ERROR_PAGE;
        if (stagingParamValidator.isCustomPageEnabledForURL(request.getServerName())) {
            viewName = ViewNames.STAGE_OOPS_PAGE;
        }
        return TheiaConstant.RetryConstants.JSP_PATH + viewName + ".jsp";
    }

    @Override
    public String returnConfirmationPage(HttpServletRequest request) {
        return ViewNames.CONFIRMATION_PAGE;
    }

    @Override
    public String returnOAuthRedirectErrorPage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            LOGGER.info("sessionInvalidating");
            session.invalidate();
        }
        return ViewNames.OAUTH_ERROR;
    }

    @Override
    public String returnOauthLoginRedirectPage() {
        return ViewNames.OAUTH_DONE;
    }

    @Override
    public String returnLinkPaymentStatusPage(HttpServletRequest request) {
        return ViewNames.LINK_PAYMENT_STATUS_PAGE;
    }

    @Override
    public String returnForwarderPage() {
        return ViewNames.TRANS_STATUS_PAGE;
    }

    @Override
    public String returnUPIPollPage() {
        return ViewNames.UPI_POLL_PAGE;
    }

    @Override
    public String returnNativeKycPage() {
        return ViewNames.NATIVE_KYC_JSP;
    }

    @Override
    public String returnKYCPage() {
        return ViewNames.KYC_USER_PAGE;
    }

    @Override
    public String returnScanAndPayTimeout(HttpServletRequest request) {
        return ViewNames.SCAN_PAY_TIMEOUT;
    }

    @Override
    public String returnPaymentPage(final HttpServletRequest request) {
        String paymentPageToOpen = ViewNames.PAYMENT_PAGE;
        String requestType = request.getParameter(TheiaConstant.RequestParams.REQUEST_TYPE);
        if (StringUtils.isBlank(requestType)) {
            requestType = request.getAttribute(TheiaConstant.RequestParams.REQUEST_TYPE) != null ? request
                    .getAttribute(TheiaConstant.RequestParams.REQUEST_TYPE).toString() : null;
        }

        if (TheiaConstant.RequestTypes.SUBSCRIPTION.equalsIgnoreCase(requestType)
                || (theiaSessionDataService.getTxnInfoFromSession(request) != null && TheiaConstant.RequestTypes.SUBSCRIPTION
                        .equalsIgnoreCase(theiaSessionDataService.getTxnInfoFromSession(request).getRequestType()))) {
            paymentPageToOpen = ViewNames.SUBSCRIPTION_PAGE;
        }
        String paymentPagePath = new StringBuilder(getThemePath(request)).append(paymentPageToOpen).toString();
        LOGGER.info("Theme Generated is : {} ", paymentPagePath);
        theiaResponseGenerator.pushPtcResultStatusToStatsD(SUCCESS_RESULT_STATUS);
        return paymentPagePath;
    }

    @Override
    public String returnRiskPaymentPage(final HttpServletRequest request) {
        ThemeInfo themeInfo = theiaSessionDataService.getThemeInfoFromSession(request);
        String channelId = themeInfo.getChannel();

        if (channelId.equals("WEB")) {
            return ViewNames.RISK_PAYMENT_PAGE_WEB;
        }
        return ViewNames.RISK_PAYMENNT_PAGE_WAP;
    }

    @Override
    public String returnOAuthPage(final HttpServletRequest request) {
        return new StringBuilder(getThemePath(request)).append(ViewNames.OAUTH_DONE).toString();
    }

    @Override
    public String returnNpciReqPage() {
        return ViewNames.NPCI_REQ_PAGE;
    }

    @Override
    public String returnNpciResPage() {
        return ViewNames.NPCI_RESP_PAGE;
    }

    @Override
    public String returnCheckOutJsPage() {
        return ViewNames.CHECKOUT_JS_PAGE;
    }

    private String getThemePath(final HttpServletRequest request) {
        final StringBuilder themePath = new StringBuilder("");
        if (request == null) {
            LOGGER.warn("Invalid request or session found, Either request or session is null.");
            return themePath.toString();
        }

        ThemeInfo viewInfo = theiaSessionDataService.setAndGetThemeInfoInSesion(request);
        LOGGER.debug("Fetched session theme data : {}", viewInfo);

        // Prepare theme Path
        if (viewInfo != null && StringUtils.isNotBlank(viewInfo.getChannel())) {
            themePath.append(viewInfo.getChannel().toLowerCase()).append(ViewNames.PATH_SEPARATOR);
        }
        if (viewInfo != null && StringUtils.isNotBlank(viewInfo.getTheme())) {
            themePath.append(viewInfo.getTheme()).append(ViewNames.PATH_SEPARATOR);
        }
        LOGGER.debug("Theme path :{}", themePath);
        return themePath.toString();
    }

    private void logMerchantResponseOOps(HttpServletRequest request) {

        if (request.getSession(false) != null && request.getAttribute("startTime") != null) {

            String destination = StatisticConstants.PGPLUS;
            String mid = request.getParameter(TheiaConstant.RequestParams.MID);
            String requestType = request.getParameter(TheiaConstant.RequestParams.REQUEST_TYPE);
            long startTime = Long.valueOf(request.getAttribute("startTime").toString());
            String timeElapsed = String.valueOf(System.currentTimeMillis() - startTime);

            String api = null;
            if (StringUtils.isNotBlank(request.getRequestURI())) {
                api = StringUtils.substringAfterLast(request.getRequestURI(), "/");
            }

            String status = StatisticConstants.FAIL;
            StatisticsLogger.logMerchantResponse(mid, api, destination, requestType, timeElapsed, status);

        }

    }

    /**
     * @author kesari
     * @createdOn 27-Mar-2016
     */
    private class ViewNames {
        static final String CONFIRMATION_PAGE = "confirmationPage";
        static final String OOPS_PAGE = "oops";
        static final String STAGE_OOPS_PAGE = "stagingoops";
        static final String ERROR_PAGE = "error";
        static final String PAYMENT_PAGE = "paymentForm";
        static final String SUBSCRIPTION_PAGE = "subscriptionForm";
        static final String OAUTH_DONE = "oauth";
        static final String PATH_SEPARATOR = "/";
        static final String TRANS_STATUS_PAGE = "forward";
        static final String OAUTH_ERROR = "OauthRedirectError";
        static final String UPI_POLL_PAGE = "common/upipoll";
        static final String RISK_PAYMENNT_PAGE_WAP = "common/postCon";
        static final String RISK_PAYMENT_PAGE_WEB = "common/postCon-web";
        static final String LINK_PAYMENT_STATUS_PAGE = "linkPaymentStatusPage";
        static final String SCAN_PAY_TIMEOUT = "scanpay_timeout";
        static final String KYC_USER_PAGE = "common/kycUser";
        static final String NATIVE_KYC_JSP = "common/nativeKycUser";
        static final String NPCI_REQ_PAGE = "npcirequest";
        static final String NPCI_RESP_PAGE = "npciresponse";
        static final String CHECKOUT_JS_PAGE = "checkoutForward";
    }

}
