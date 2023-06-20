//package com.paytm.pgplus.theia.controllers;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//
//import com.paytm.pgplus.theia.acs.service.IAcsUrlService;
//import com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams;
//import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
//import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
//
//@Controller
//public class AcsUrlResolutionController {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(AcsUrlResolutionController.class);
//
//    @Autowired
//    @Qualifier("theiaViewResolverService")
//    private ITheiaViewResolverService theiaViewResolverService;
//
//    @Autowired
//    @Qualifier("theiaSessionDataService")
//    private ITheiaSessionDataService sessionDataService;
//
//    @Autowired
//    @Qualifier("acsUrlServiceImpl")
//    private IAcsUrlService acsUrlService;
//
//    @RequestMapping(value = "/resolveACS", method = { RequestMethod.GET })
//    public String resolveAcsUrl(HttpServletRequest request, HttpServletResponse response) {
//        long startTime = System.currentTimeMillis();
//        try {
//            LOGGER.info("Received hit to resolve ACS URL, with MID : {} , Order ID : {} and Unique ID : {} ",
//                    request.getParameter(RequestParams.MID), request.getParameter(RequestParams.ORDER_ID),
//                    request.getParameter(RequestParams.ID));
//            String bankForm = acsUrlService.resolveACSUrl(request.getParameter(RequestParams.MID),
//                    request.getParameter(RequestParams.ORDER_ID), request.getParameter(RequestParams.ID));
//            sessionDataService.setRedirectPageInSession(request, bankForm, true);
//            acsUrlService.purgeAcsUrl(request.getParameter(RequestParams.MID),
//                    request.getParameter(RequestParams.ORDER_ID));
//            return theiaViewResolverService.returnForwarderPage();
//        } catch (Exception e) {
//            LOGGER.error("Exception :  ", e);
//        } finally {
//            LOGGER.info("Total time taken for AcsUrlResolutionController is {} ms", System.currentTimeMillis()
//                    - startTime);
//        }
//        return theiaViewResolverService.returnOOPSPage(request);
//    }
//
// }
