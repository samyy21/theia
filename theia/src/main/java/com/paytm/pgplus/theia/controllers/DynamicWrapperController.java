//package com.paytm.pgplus.theia.controllers;
//
//import com.paytm.pgplus.biz.utils.Ff4jUtils;
//import com.paytm.pgplus.common.config.ConfigurationUtil;
//import com.paytm.pgplus.theia.constants.TheiaConstant;
//import com.paytm.pgplus.theia.exceptions.*;
//import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
//import com.paytm.pgplus.theia.validator.service.ValidationService;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//
//import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.*;
//
//@Controller
//public class DynamicWrapperController {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicWrapperController.class);
//
//    @Autowired
//    private TheiaResponseGenerator theiaResponseGenerator;
//
//    @Autowired
//    private Ff4jUtils ff4JUtils;
//
//    @Autowired
//    private ValidationService validationService;
//
//    @RequestMapping(value = "/odisha", method = RequestMethod.POST)
//    public void odishaSyncResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        LOGGER.info("Request Recieved after wallet(sync) payment for Odisha");
//        String json = request.getParameter("json");
//        String mid = request.getParameter("merchantCode");
//        if (ff4JUtils.isFeatureEnabled(THEIA_VALIDATE_MID_ENABLE, false) && !validationService.validateMid(mid)) {
//            throw new TheiaServiceException("CRITICAL_ERROR : Invalid Merchant Code : " + mid);
//        }
//        String htmlPage = ConfigurationUtil.getWrapperConfirmationPage();
//        htmlPage = htmlPage.replace("PUSH_JSON_DATA", json).replace("{merchantCode}", mid);
//        LOGGER.debug("Response html {}", htmlPage);
//        response.getOutputStream().write(htmlPage.getBytes(StandardCharsets.UTF_8));
//    }
//
//    @RequestMapping(value = TheiaConstant.ExtraConstants.DYNAMIC_WRAPPER_PATH_URI, method = RequestMethod.POST)
//    public void dynamicWrapperTransitionPageResponse(HttpServletRequest request, HttpServletResponse response)
//            throws IOException {
//        LOGGER.info("Request Recieved on /dynamic-wrapper for Transition Page");
//        String wrapperTransitionPageEncodedJson = request.getParameter("PUSH_JSON_DATA");
//        if (StringUtils.isBlank(wrapperTransitionPageEncodedJson)) {
//            wrapperTransitionPageEncodedJson = request.getParameter("json");
//        }
//        String htmlPage = theiaResponseGenerator.wrapperTransitionHtmlPage(wrapperTransitionPageEncodedJson);
//        LOGGER.debug("Response html {}", htmlPage);
//        response.getOutputStream().write(htmlPage.getBytes(StandardCharsets.UTF_8));
//    }
//
// }
