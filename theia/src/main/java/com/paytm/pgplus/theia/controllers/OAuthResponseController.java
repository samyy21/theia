/**
 *
 */
package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.TheiaControllerException;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.services.IOAuthResponseService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/**
 * @createdOn 21-Mar-2016
 * @author kesari
 */
@Controller
public class OAuthResponseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthResponseController.class);

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("oAuthResponseService")
    private IOAuthResponseService oAuthResponseService;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    private String OLD_PG_BASE_URL = ConfigurationUtil.getProperty(TheiaConstant.ExtraConstants.OLD_PG_BASE_URL_SKIP);

    @RequestMapping(value = "/oauthResponse")
    public void handleOAuthResponse(final HttpServletRequest request, HttpServletResponse response) {

        try {

            Map<String, String[]> paramMap = request.getParameterMap();

            StringBuilder stringBuilder = new StringBuilder().append("/theia/oauthResponseRedirect").append("?");
            String mid = "";
            String orderID = "";
            for (Map.Entry<String, String[]> entrySet : paramMap.entrySet()) {

                stringBuilder.append(entrySet.getKey()).append("=").append(entrySet.getValue()[0]).append("&");

                if (entrySet.getKey().equals("state")) {

                    String[] state = entrySet.getValue()[0].split(":");
                    stringBuilder.append("MID=").append(state[1]).append("&");
                    stringBuilder.append("ORDER_ID=").append(state[0]).append("&");
                    mid = state[1];
                    orderID = state[0];
                }
            }

            String referrar = request.getHeader(HttpHeaders.REFERER);
            String redirectUrl = "";
            LOGGER.info("refferr : " + referrar + " OLD_PG_BASE_URL " + OLD_PG_BASE_URL + " mid " + mid + " orderID "
                    + orderID);

            try {
                if (StringUtils.isNotBlank(mid) && StringUtils.isNotBlank(orderID)) {

                    String key = (String) nativeSessionUtil.getKey(mid + "~" + orderID + "~" + "oauthKey");
                    LOGGER.info("key is " + key);
                    if (StringUtils.isNotBlank(key) && key.contains(OLD_PG_BASE_URL)) {
                        redirectUrl = "https://" + OLD_PG_BASE_URL
                                + stringBuilder.substring(0, stringBuilder.length() - 1);
                        LOGGER.info("redirect url  is " + redirectUrl);
                        response.sendRedirect(redirectUrl);
                    }
                }
            } catch (Exception e) {
                LOGGER.info("exception occured while fetchiong value from redis .");
            }

            if (StringUtils.isNotBlank(referrar) && StringUtils.isNotBlank(OLD_PG_BASE_URL)
                    && referrar.contains(OLD_PG_BASE_URL)) {

                redirectUrl = "https://" + OLD_PG_BASE_URL + stringBuilder.substring(0, stringBuilder.length() - 1);
                if (StringUtils.isNotBlank(mid) && StringUtils.isNotBlank(orderID)) {
                    nativeSessionUtil.setKey(mid + "~" + orderID + "~" + "oauthKey", OLD_PG_BASE_URL, 900);
                }
                LOGGER.info("old pg request redirecing it to :" + redirectUrl);
            } else {
                redirectUrl = stringBuilder.substring(0, stringBuilder.length() - 1);
            }
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }
    }

    @RequestMapping(value = "/oauthResponseRedirect")
    public String processOAuthRedirect(final HttpServletRequest request, final Model model, final Locale locale) {

        long startTime = System.currentTimeMillis();

        final PaymentRequestBean paymentRequestData = new PaymentRequestBean(request);

        LOGGER.info("PaymentRequestBean received : {}", paymentRequestData);

        try {

            if (!theiaSessionDataService.isSessionExists(request)) {
                LOGGER.error("Call from oauth for Session that does not exist");
                return StringUtils.EMPTY;
            }

            oAuthResponseService.processLoginResponse(paymentRequestData);

        } catch (Exception e) {

            LOGGER.error("Exception Occurred :: ", e);
            oAuthResponseService.incrementLoginRetryCount(paymentRequestData);

        } finally {
            LOGGER.info("Total time taken for Controller {} is {} ms", "OAuthResponseController",
                    System.currentTimeMillis() - startTime);
        }
        return theiaViewResolverService.returnOauthLoginRedirectPage();
    }

    @RequestMapping(value = "/oauthResponseEnhancedCashier")
    public void processOAuthResponseAndRedirect(final HttpServletRequest request, final HttpServletResponse response) {

        long startTime = System.currentTimeMillis();

        try {

            String html = oAuthResponseService.processLoginForEnhancedCashierFlow(request);
            response.getOutputStream().print(html);
            response.setContentType("text/html");

        } catch (Exception e) {

            LOGGER.error("Exception Occurred :: ", e);

            throw RequestValidationException.getException();

        } finally {

            LOGGER.info("Total time taken for Controller {} is {} ms", "OAuthResponseController",
                    System.currentTimeMillis() - startTime);
        }
    }
}
