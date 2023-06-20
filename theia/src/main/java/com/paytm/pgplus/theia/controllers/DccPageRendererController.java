//package com.paytm.pgplus.theia.controllers;
//
//import com.paytm.pgplus.biz.utils.Ff4jUtils;
//import com.paytm.pgplus.common.config.ConfigurationUtil;
//import com.paytm.pgplus.common.model.DccPageData;
//import com.paytm.pgplus.facade.utils.JsonMapper;
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
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.nio.charset.StandardCharsets;
//import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.*;
//import static com.paytm.pgplus.theia.constants.TheiaConstant.GvConsent.PUSH_APP_DATA;
//import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.*;
//
//@Controller
//public class DccPageRendererController {
//
//    @Autowired
//    private NativeSessionUtil nativeSessionUtil;
//    private static final Logger LOGGER = LoggerFactory.getLogger(DccPageRendererController.class);
//    @Autowired
//    @Qualifier(value = "theiaViewResolverService")
//    private ITheiaViewResolverService theiaViewResolverService;
//
//    @Autowired
//    private Ff4jUtils ff4JUtils;
//
//    @Autowired
//    private ValidationService validationService;
//
//    private static final String VIEW_BASE = "/WEB-INF/views/jsp/";
//
//    @RequestMapping(value = "/fetchDccPage", method = { RequestMethod.POST })
//    public void renderDccPage(final HttpServletRequest request, final HttpServletResponse response) {
//        final long startTime = System.currentTimeMillis();
//        String mid = request.getParameter(MID);
//        String orderId = request.getParameter(ORDER_ID);
//        String txnToken = request.getParameter(TXN_TOKEN);
//        if (ff4JUtils.isFeatureEnabled(THEIA_VALIDATE_MID_ENABLE, false) && !validationService.validateMid(mid)) {
//            throw new TheiaServiceException("CRITICAL_ERROR : Invalid MID : " + mid);
//        }
//        if (ff4JUtils.isFeatureEnabled(THEIA_VALIDATE_ORDER_ID_ENABLE, false)
//                && !validationService.validateOrderId(orderId)) {
//            throw new TheiaServiceException("CRITICAL_ERROR : Invalid ORDER_ID : " + orderId);
//        }
//        if (ff4JUtils.isFeatureEnabled(THEIA_VALIDATE_TXN_TOKEN_ENABLE, false)
//                && !validationService.validateTxnToken(txnToken)) {
//            throw new TheiaServiceException("CRITICAL_ERROR : Invalid TXN_TOKEN : " + txnToken);
//        }
//        LOGGER.info(" Received hit at Dcc Page Renderer orderId {} mid {} txntoken {}", orderId, mid, txnToken);
//        String htmlPage = ConfigurationUtil.getDccPage();
//        try {
//            if (StringUtils.isNotBlank(htmlPage)) {
//                if (StringUtils.isNotBlank(mid) && StringUtils.isNotBlank(orderId) && StringUtils.isNotBlank(txnToken)) {
//                    DccPageData dccPageData = nativeSessionUtil.getDccPageData(txnToken);
//                    if (dccPageData != null) {
//                        String dccPageDataString = JsonMapper.mapObjectToJson(dccPageData);
//                        LOGGER.info("dccPageDataString returned to PGP-UI {}", dccPageDataString);
//                        if (StringUtils.isNotBlank(dccPageDataString)) {
//                            htmlPage = htmlPage.replace(PUSH_APP_DATA, dccPageDataString);
//                        }
//                    } else {
//                        request.getRequestDispatcher(
//                                VIEW_BASE + theiaViewResolverService.returnOOPSPage(request) + ".jsp").forward(request,
//                                response);
//                    }
//                    response.setContentType("text/html");
//                    response.getOutputStream().write(htmlPage.getBytes(StandardCharsets.UTF_8));
//                    return;
//                } else {
//                    LOGGER.info("Invalid Request Parameters For Dcc Page Request");
//                }
//
//            } else {
//                LOGGER.info("Something went wrong no dcc html Page is loaded");
//            }
//        } catch (Exception e) {
//            LOGGER.info("Something went wrong in DccPageRendererController {} ", e.getMessage());
//        } finally {
//
//            LOGGER.info("Total time taken for DccPageRendererController is {} ms", System.currentTimeMillis()
//                    - startTime);
//        }
//
//    }
//
// }
