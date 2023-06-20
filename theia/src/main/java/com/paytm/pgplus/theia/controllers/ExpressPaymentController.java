//package com.paytm.pgplus.theia.controllers;
//
//import java.util.Locale;
//
//import javax.servlet.http.HttpServletRequest;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.paytm.pgplus.facade.exception.FacadeCheckedException;
//import com.paytm.pgplus.facade.utils.JsonMapper;
//import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
//import com.paytm.pgplus.theia.models.ExpressCardTokenRequest;
//import com.paytm.pgplus.theia.models.response.ExpressCardTokenResponse;
//import com.paytm.pgplus.theia.services.impl.ExpressPaymentService;
//
//@RestController
//@RequestMapping("PAYTM_EXPRESS")
//public class ExpressPaymentController {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressPaymentController.class);
//
//    @Autowired
//    private ExpressPaymentService expressPaymentService;
//
//    @RequestMapping(value = "getCardToken", method = RequestMethod.POST)
//    public String generateCardToken(HttpServletRequest request, Locale locale) {
//        final long startTime = System.currentTimeMillis();
//
//        ExpressCardTokenRequest requestData = new ExpressCardTokenRequest(request);
//        LOGGER.info("Request received for express card token : {}", requestData);
//        ExpressCardTokenResponse response = new ExpressCardTokenResponse();
//
//        try {
//            response = expressPaymentService.getCardToken(requestData, true);
//        } catch (Exception e) {
//            LOGGER.error("SYSTEM ERROR :", e);
//            response.setStatus(ResponseConstants.FAILURE.getMessage());
//            response.setErrorCode(ResponseConstants.INVALID_JSON_DATA.getCode());
//            response.setErrorMessage(ResponseConstants.INVALID_JSON_DATA.getMessage());
//
//        } finally {
//            LOGGER.info("Total time taken for ExpressPaymentController is {} ms", System.currentTimeMillis()
//                    - startTime);
//        }
//        return generateFinalResponse(response);
//    }
//
//    @RequestMapping(value = "getCardToken/billPayment", method = RequestMethod.POST)
//    public String generateCardTokenForBillPayment(HttpServletRequest request, Locale locale) {
//        final long startTime = System.currentTimeMillis();
//
//        ExpressCardTokenRequest requestData = new ExpressCardTokenRequest(request);
//        LOGGER.info("Request received for express card token : {}", requestData);
//        ExpressCardTokenResponse response = new ExpressCardTokenResponse();
//
//        try {
//            response = expressPaymentService.getCardToken(requestData, false);
//        } catch (Exception e) {
//            LOGGER.error("SYSTEM ERROR :", e);
//            response.setStatus(ResponseConstants.FAILURE.getMessage());
//            response.setErrorCode(ResponseConstants.INVALID_JSON_DATA.getCode());
//            response.setErrorMessage(ResponseConstants.INVALID_JSON_DATA.getMessage());
//
//        } finally {
//            LOGGER.info("Total time taken for ExpressPaymentController is {} ms", System.currentTimeMillis()
//                    - startTime);
//        }
//        return generateFinalResponse(response);
//    }
//
//    @RequestMapping(value = "getCardToken/emi", method = RequestMethod.POST)
//    public String generateCardTokenForEmiSubvention(HttpServletRequest request, Locale locale) {
//        final long startTime = System.currentTimeMillis();
//
//        ExpressCardTokenRequest requestData = new ExpressCardTokenRequest(request);
//        LOGGER.info("Request received for subvention express card token : {}", requestData);
//        ExpressCardTokenResponse response = new ExpressCardTokenResponse();
//
//        try {
//            expressPaymentService.setUserDetailsForLoggedInUser(request, requestData);
//            response = expressPaymentService.getCardToken(requestData, false);
//        } catch (Exception e) {
//            LOGGER.error("SYSTEM ERROR :", e);
//            response.setStatus(ResponseConstants.FAILURE.getMessage());
//            response.setErrorCode(ResponseConstants.INVALID_JSON_DATA.getCode());
//            response.setErrorMessage(ResponseConstants.INVALID_JSON_DATA.getMessage());
//
//        } finally {
//            LOGGER.info("Total time taken for EmiSubvention ExpressPaymentController is {} ms",
//                    System.currentTimeMillis() - startTime);
//        }
//        return generateFinalResponse(response);
//    }
//
//    private String generateFinalResponse(ExpressCardTokenResponse response) {
//        LOGGER.debug("Preparing final response JSON");
//        String jsonResponse = null;
//        try {
//            jsonResponse = JsonMapper.mapObjectToJson(response);
//        } catch (FacadeCheckedException e) {
//            LOGGER.error("JSON mapping exception :", e);
//            response.setStatus(ResponseConstants.FAILURE.getMessage());
//            response.setErrorCode(ResponseConstants.INTERNAL_PROCESSING_ERROR.getCode());
//            response.setErrorMessage(ResponseConstants.INTERNAL_PROCESSING_ERROR.getMessage());
//        }
//        LOGGER.info("JSON Response generated: {}", jsonResponse);
//        return jsonResponse;
//    }
// }
