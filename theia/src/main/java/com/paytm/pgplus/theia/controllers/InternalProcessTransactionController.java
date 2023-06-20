//package com.paytm.pgplus.theia.controllers;
//
//import com.paytm.pgplus.theia.helper.BankTransferHelper;
//import com.paytm.pgplus.theia.models.NativeJsonResponse;
//import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//import javax.servlet.http.HttpServletRequest;
//
//@NativeControllerAdvice
//@RestController
//@RequestMapping("api/v1")
//public class InternalProcessTransactionController {
//    private static final Logger LOGGER = LoggerFactory.getLogger(InternalProcessTransactionController.class);
//
//    @Autowired
//    @Qualifier(value = "bankTransferHelper")
//    private BankTransferHelper bankTransferHelper;
//
//    @RequestMapping(value = "/internal/processTransaction", method = { RequestMethod.POST }, consumes = { MediaType.APPLICATION_JSON_VALUE })
//    public NativeJsonResponse internalProcessTransaction(HttpServletRequest request) {
//
//        final long startTime = System.currentTimeMillis();
//        // LOGGER.info("Request received for API: /v1/internal/processTransaction is: {}",
//        // request);
//
//        NativeJsonResponse response = bankTransferHelper.processNativeJsonRequest(request);
//
//        LOGGER.info("Total time taken for InternalProcessTransactionController is {} ms", System.currentTimeMillis()
//                - startTime);
//        return response;
//    }
// }