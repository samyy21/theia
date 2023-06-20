package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.utils.AuthUtil;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.sessiondata.ThemeInfo;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.text.DateFormat;
import java.util.*;

import static com.paytm.pgplus.biz.utils.BizConstant.ExtendedInfoKeys.SSO_TOKEN;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.*;

/**
 * Handles requests for the application home page.
 */
@Controller
public class TestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestController.class);

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService sessionDataService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    /**
     * Simply selects the home view to render by returning its name.
     */
    @RequestMapping(value = { "/merchantcheckout" }, method = RequestMethod.GET)
    public String home(Locale locale, Model model) {
        LOGGER.info("Welcome! The client locale is {}.", locale);
        Date date = new Date();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
        String formattedDate = dateFormat.format(date);
        model.addAttribute("serverTime", formattedDate);
        return "merchantcheckout";
    }

    @RequestMapping(value = { "/npciMock" }, method = RequestMethod.GET)
    public String npciMock(Locale locale, Model model) {
        LOGGER.info("Welcome! The client locale is {}.", locale);
        Date date = new Date();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
        String formattedDate = dateFormat.format(date);
        model.addAttribute("serverTime", formattedDate);
        return "npcimock";
    }

    @RequestMapping(value = { "/dmrc" }, method = RequestMethod.GET)
    public String dmrc(Locale locale, Model model) {
        LOGGER.info("Welcome! The client locale is {}.", locale);
        Date date = new Date();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
        String formattedDate = dateFormat.format(date);
        model.addAttribute("serverTime", formattedDate);
        return "dmrcmerchantcheckout";
    }

    /**
     * Simply selects the home view to render by returning its name.
     */
    @RequestMapping(value = { "/paytmCallback", "/" }, method = RequestMethod.POST)
    public String paytmCallback(Locale locale, Model model) {
        LOGGER.info("Received hit at Paytm Callback");
        Date date = new Date();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
        String formattedDate = dateFormat.format(date);
        model.addAttribute("serverTime", formattedDate);
        return "paytmCallback";
    }

    /*
     * @RequestMapping(value = "/checkoutForward", method = RequestMethod.POST)
     * public String checkoutCallback(Locale locale, Model model) {
     * LOGGER.info("Received hit at Checkoutjs Callback"); Date date = new
     * Date(); DateFormat dateFormat =
     * DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
     * String formattedDate = dateFormat.format(date);
     * model.addAttribute("serverTime", formattedDate); return
     * "checkoutForward"; }
     */

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String test(HttpServletRequest req, Locale locale, Model model) {
        Date date = new Date();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
        String formattedDate = dateFormat.format(date);
        model.addAttribute("serverTime", formattedDate);
        ThemeInfo themeInfo = new ThemeInfo();
        req.getSession().setAttribute(TheiaConstant.SessionDataAttributes.themeInfo.name(), themeInfo);
        themeInfo = (ThemeInfo) req.getSession().getAttribute(TheiaConstant.SessionDataAttributes.themeInfo.name());
        themeInfo.setChannel(" ststst ");
        LOGGER.debug(" ThemeInfo value ********** {}", themeInfo);
        return theiaViewResolverService.returnPaymentPage(req);
    }

    @RequestMapping(value = "/payment", method = { RequestMethod.GET, RequestMethod.POST })
    public String payment(HttpServletRequest request) {
        LOGGER.debug(" Calling Payment page. ");
        LOGGER.info(" Request Parameter ");

        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            for (int i = 0; i < paramValues.length; i++) {
                String paramValue = paramValues[i];
                LOGGER.info(" -->  {} : {}", paramName, paramValue);
            }
        }

        setTransactionConfiguration(request);
        setTransactionInfo(request);
        setWalletInfo(request);
        setLoginInfo(request);

        return theiaViewResolverService.returnPaymentPage(request);
    }

    @RequestMapping(value = "/oauth", method = RequestMethod.GET)
    public String oauth(HttpServletRequest request, Locale locale, Model model) {
        LOGGER.debug(" Calling auth page. ");
        setTransactionConfiguration(request);
        setTransactionInfo(request);
        setWalletInfo(request);
        setLoginInfo(request);
        return theiaViewResolverService.returnOAuthPage(request);
    }

    @RequestMapping(value = { "/oops", "/v1/oops" }, method = RequestMethod.GET)
    public String oops(HttpServletRequest request, Locale locale, Model model) {
        LOGGER.debug(" Calling oops page. ");
        setTransactionConfiguration(request);
        setTransactionInfo(request);
        setWalletInfo(request);
        setLoginInfo(request);
        return theiaViewResolverService.returnOOPSPage(request);
    }

    @RequestMapping(value = "/error", method = { RequestMethod.GET, RequestMethod.POST })
    public String error(HttpServletRequest request, Locale locale, Model model) {
        LOGGER.debug(" Calling error page. ");
        return theiaViewResolverService.returnOOPSPage(request);
    }

    @RequestMapping(value = "/processTransactiontest", method = { RequestMethod.GET, RequestMethod.POST })
    public String home2(HttpServletRequest request, Locale locale, Model model) {
        setTransactionConfiguration(request);
        setTransactionInfo(request);
        setWalletInfo(request);
        setLoginInfo(request);

        return theiaViewResolverService.returnPaymentPage(request);
    }

    private void setLoginInfo(HttpServletRequest request) {
        sessionDataService.getLoginInfoFromSession(request, true);
    }

    private void setWalletInfo(HttpServletRequest request) {
        sessionDataService.getWalletInfoFromSession(request, true);

    }

    private void setTransactionInfo(HttpServletRequest request) {
        sessionDataService.getTxnInfoFromSession(request, true);
    }

    private void setTransactionConfiguration(HttpServletRequest request) {
        sessionDataService.getTxnConfigFromSession(request, true);
    }

    @RequestMapping(value = "/testJSON", method = { RequestMethod.GET, RequestMethod.POST })
    @Consumes(MediaType.APPLICATION_JSON)
    public void testJSON(@RequestBody PaymentRequestBean paymentRequestData, HttpServletRequest httpRequest) {
        paymentRequestData.setRequest(httpRequest);
        // System.out.println("====================================");
        // System.out.println(paymentRequestData.getRequestType());
    }

    @RequestMapping(value = "/api/validateRefId", method = RequestMethod.POST)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseEntity<Map<String, Object>> processRequestForFetchingOrderId(HttpServletRequest httpRequest) {
        LOGGER.info("request to fetch orderId");

        try {
            String requestData = IOUtils.toString(httpRequest.getInputStream(), Charsets.UTF_8.name());
            if (StringUtils.isNotBlank(requestData)) {
                Map<String, String> requestMap = JsonMapper.mapJsonToObject(requestData, Map.class);
                if (requestMap == null) {
                    return null;
                }
                String refId = requestMap.get(REF_ID);
                String mid = requestMap.get(MID);
                String ssoToken = requestMap.get(SSO_TOKEN);

                String key = "REF_ID_" + refId + "_ORDER_ID_MAPPING";

                String cachedMid = (String) nativeSessionUtil.getField(key, MID);
                String cachedSsoToken = (String) nativeSessionUtil.getField(key, SSO_TOKEN);

                if (!StringUtils.equals(mid, cachedMid) || !StringUtils.equals(cachedSsoToken, ssoToken)) {
                    LOGGER.info("mid or ssoToken does not match");
                    return null;
                }

                if (!authUtil.isUserValid(ssoToken)) {
                    LOGGER.info("user is not valid");
                    return null;
                }

                Map<String, Object> response = new HashMap<>();
                String orderId = (String) nativeSessionUtil.getField(key, ORDER_ID);
                response.put(ORDER_ID, orderId);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } catch (Exception e) {
            LOGGER.error("Exception while fetching orderId ", e);
        }
        return null;
    }
}
