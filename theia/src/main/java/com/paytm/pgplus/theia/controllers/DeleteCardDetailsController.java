///**
// *
// */
//package com.paytm.pgplus.theia.controllers;
//
//import java.io.IOException;
//import java.util.Locale;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import com.paytm.pgplus.logging.ExtendedLogger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import com.paytm.pgplus.theia.services.ITheiaCardService;
//import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
//import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
//
///**
// * @createdOn 21-Mar-2016
// * @author kesari
// */
//@Controller
//public class DeleteCardDetailsController {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteCardDetailsController.class);
//    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(DeleteCardDetailsController.class);
//
//    private static final String VIEW_BASE = "/WEB-INF/views/jsp/";
//
//    @Autowired
//    @Qualifier(value = "theiaViewResolverService")
//    private ITheiaViewResolverService theiaViewResolverService;
//
//    @Autowired
//    @Qualifier("theiaSessionDataService")
//    private ITheiaSessionDataService theiaSessionDataService;
//
//    @Autowired
//    @Qualifier("theiaCardService")
//    private ITheiaCardService theiaCardService;
//
//    @RequestMapping(value = "/DeleteCardDetails")
//    public void deleteCardDetails(final HttpServletRequest request, final HttpServletResponse response,
//            final Model model, final Locale locale) throws IOException, ServletException {
//        try {
//            String savedCardId = request.getParameter("savedCardId");
//            LOGGER.info("Request received for delete card, card id :{}", savedCardId);
//
//            boolean isAjax = !"no".equals(request.getParameter("ajax"));
//            String result = theiaCardService.processDeleteCard(request, savedCardId, true);
//            EXT_LOGGER.customInfo("Values for isAjax : {}, & result :{}", isAjax, result);
//
//            if (isAjax) {
//                response.getOutputStream().print(result);
//                return;
//            } else {
//                StringBuilder sb = new StringBuilder(VIEW_BASE);
//                sb.append(theiaViewResolverService.returnPaymentPage(request)).append(".jsp");
//                LOGGER.info("Return path : {}", sb.toString());
//
//                request.getRequestDispatcher(sb.toString()).forward(request, response);
//                return;
//            }
//        } catch (Exception e) {
//            LOGGER.error("Exception occured while deleting the saved card, due to ", e);
//        }
//    }
// }
