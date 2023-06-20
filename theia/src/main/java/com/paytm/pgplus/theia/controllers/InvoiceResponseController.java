///**
// *
// */
//package com.paytm.pgplus.theia.controllers;
//
//import java.util.Locale;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//
//import com.paytm.pgplus.theia.exceptions.TheiaControllerException;
//import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
//import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
//import com.paytm.pgplus.theia.services.IPaymentService;
//import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
//
///**
// * @createdOn 21-Mar-2016
// * @author kesari
// */
//@Controller
//public class InvoiceResponseController {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceResponseController.class);
//
//    @Autowired
//    @Qualifier(value = "theiaViewResolverService")
//    private ITheiaViewResolverService theiaViewResolverService;
//
//    @Autowired
//    @Qualifier(value = "invoicePaymentService")
//    private IPaymentService invoicePaymentService;
//
//    @RequestMapping(value = "/invoiceResponse", method = { RequestMethod.GET, RequestMethod.POST })
//    public String invoiceResponse(final HttpServletRequest request, final HttpServletResponse response,
//            final Model model, final Locale locale) {
//        try {
//            final PaymentRequestBean paymentRequestData = new PaymentRequestBean(request);
//            LOGGER.info("Request received for invoice respone : {}", paymentRequestData);
//            boolean processed;
//
//            invoicePaymentService.validatePaymentRequest(paymentRequestData);
//            PageDetailsResponse pageDetailsResponse = invoicePaymentService.processPaymentRequest(paymentRequestData,
//                    model);
//            processed = pageDetailsResponse.isSuccessfullyProcessed();
//
//            LOGGER.debug("model data :{}", model);
//            if (!processed) {
//                throw new TheiaControllerException("Unable to process request");
//            }
//            return theiaViewResolverService.returnPaymentPage(request);
//        } catch (Exception ex) {
//            LOGGER.error("Exception in processing payment request:", ex);
//            throw new TheiaControllerException("Exception in processing payment request:", ex);
//        }
//    }
// }
