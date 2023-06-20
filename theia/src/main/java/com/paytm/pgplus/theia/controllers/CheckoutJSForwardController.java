//package com.paytm.pgplus.theia.controllers;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.paytm.pgplus.biz.utils.FF4JUtil;
//import com.paytm.pgplus.biz.utils.Ff4jUtils;
//import com.paytm.pgplus.common.config.ConfigurationUtil;
//import com.paytm.pgplus.facade.utils.JsonMapper;
//import com.paytm.pgplus.theia.constants.TheiaConstant;
//import com.paytm.pgplus.theia.models.CheckoutJsData;
//import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
//import com.paytm.pgplus.theia.nativ.model.payment.request.NativePaymentRequestBody;
//import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
//import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
//import com.paytm.pgplus.theia.validator.service.ValidationService;
//import com.paytm.pgplus.theia.exceptions.*;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.Locale;
//
//import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.BOOLEAN_FALSE;
//import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.BOOLEAN_TRUE;
//import static com.paytm.pgplus.theia.constants.TheiaConstant.GvConsent.PUSH_APP_DATA;
//import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.*;
//
//@Controller
//public class CheckoutJSForwardController {
//
//    @Autowired
//    private NativeSessionUtil nativeSessionUtil;
//
//    @Autowired
//    @Qualifier(value = "theiaViewResolverService")
//    private ITheiaViewResolverService theiaViewResolverService;
//
//    @Autowired
//    private FF4JUtil ff4JUtil;
//
//    @Autowired
//    private Ff4jUtils ff4JUtils;
//
//    @Autowired
//    private ValidationService validationService;
//
//    private static final String VIEW_BASE = "/WEB-INF/views/jsp/";
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(CheckoutJSForwardController.class);
//
//    @RequestMapping(value = "/checkoutForward", method = { RequestMethod.POST, RequestMethod.GET })
//    public void checkoutJSCallback(final HttpServletRequest request, final HttpServletResponse response) {
//        String orderId = request.getParameter("ORDER_ID");
//        String mid = request.getParameter("MID");
//        String respMsg = request.getParameter("RESPMSG");
//        if (ff4JUtils.isFeatureEnabled(THEIA_VALIDATE_MID_ENABLE, false) && !validationService.validateMid(mid)) {
//            throw new TheiaServiceException("CRITICAL_ERROR : Invalid MID : " + mid);
//        }
//        if (ff4JUtils.isFeatureEnabled(THEIA_VALIDATE_ORDER_ID_ENABLE, false)
//                && !validationService.validateOrderId(orderId)) {
//            throw new TheiaServiceException("CRITICAL_ERROR : Invalid ORDER_ID : " + orderId);
//        }
//        if (ff4JUtils.isFeatureEnabled(THEIA_VALIDATE_RESPMSG_ENABLE, false)
//                && !validationService.validateRespMsg(respMsg)) {
//            throw new TheiaServiceException("CRITICAL_ERROR : Invalid RESPMSG : " + respMsg);
//        }
//        LOGGER.info("Received hit at checkoutForward Callback orderId {} mid {} resMsg {}", orderId, mid, respMsg);
//        String htmlPage = ConfigurationUtil.getCheckoutForward();
//        try {
//
//            if (StringUtils.isNotBlank(htmlPage)
//                    && ff4JUtil.isFeatureEnabled(TheiaConstant.FF4J.THEIA_CHECKOUT_FORWARD_HTML_RENDER_ALLOWED, mid)) {
//                if (StringUtils.isNotBlank(mid) && StringUtils.isNotBlank(orderId)) {
//                    String txnToken = nativeSessionUtil.getTxnToken(mid, orderId);
//                    if (StringUtils.isNotBlank(txnToken)) {
//                        NativePaymentRequestBody nativePaymentRequestBody = nativeSessionUtil
//                                .getRetryDataAndMerchantConfigForCheckOutJs(txnToken);
//                        LOGGER.info("nativePaymentRequestBody from redis is {}", nativePaymentRequestBody);
//                        if (nativePaymentRequestBody != null) {
//                            nativePaymentRequestBody.setRetryErrorMsg(respMsg);
//                            CheckoutJsData checkoutJsData = new CheckoutJsData(orderId, mid, txnToken,
//                                    nativePaymentRequestBody);
//                            String checkoutJsConfigString = JsonMapper.mapObjectToJson(checkoutJsData);
//                            LOGGER.info("checkoutJsConfigString returned to PGP-UI {}", checkoutJsData);
//                            if (StringUtils.isNotBlank(checkoutJsConfigString)) {
//                                htmlPage = htmlPage.replace(PUSH_APP_DATA, checkoutJsConfigString);
//                            }
//                        }
//                        response.setContentType("text/html");
//                        response.getOutputStream().write(htmlPage.getBytes(StandardCharsets.UTF_8));
//                        return;
//                    } else {
//                        LOGGER.info("txnToken not found in redis ");
//                    }
//                }
//            }
//            request.getRequestDispatcher(VIEW_BASE + theiaViewResolverService.returnCheckOutJsPage() + ".jsp").forward(
//                    request, response);
//            return;
//        } catch (Exception e) {
//            LOGGER.error("Something went wrong in CheckoutForward Controller {} ", e);
//        }
//    }
//
// }
