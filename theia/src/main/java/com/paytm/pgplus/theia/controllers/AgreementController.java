//package com.paytm.pgplus.theia.controllers;
//
//import com.paytm.pgplus.facade.paymentservices.models.request.CreateAgreementRequest;
//import com.paytm.pgplus.facade.paymentservices.models.response.CreateAgreementResponse;
//import com.paytm.pgplus.facade.paymentservices.services.IPaymentSevicesClient;
//import com.paytm.pgplus.facade.user.models.response.ValidateAuthCodeResponse;
//import com.paytm.pgplus.facade.utils.JsonMapper;
//import com.paytm.pgplus.theia.helper.AgreementHelper;
//import com.paytm.pgplus.theia.models.TheiaCreateAgreementRequest;
//import com.paytm.pgplus.theia.models.response.AgreementResponse;
//import com.paytm.pgplus.theia.models.response.TheiaCreateAgreementResponse;
//import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
//import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.web.bind.annotation.*;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
//
//@RestController
//public class AgreementController {
//
//    @Autowired
//    @Qualifier("paymentServicesClientImpl")
//    private IPaymentSevicesClient paymentServicesClientImpl;
//
//    @Autowired
//    @Qualifier("theiaViewResolverService")
//    private ITheiaViewResolverService theiaViewResolverService;
//
//    @Autowired
//    @Qualifier("agreementHelper")
//    private AgreementHelper agreementHelper;
//
//    @Autowired
//    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(AgreementController.class);
//    private static final String VIEW_BASE = "/WEB-INF/views/jsp/";
//
//    @RequestMapping(value = "/agreement/create/{id}", method = { RequestMethod.GET })
//    public void createAgreement(HttpServletRequest request, final HttpServletResponse response,
//            @PathVariable("id") String redisKey, @RequestParam("code") String authCode) {
//
//        ValidateAuthCodeResponse validateAuthCodeResponse;
//        CreateAgreementResponse createAgreementResponse;
//        try {
//            validateAuthCodeResponse = agreementHelper.getValidateAuthCodeResponse(authCode);
//
//            // if the response from oauth is succesfull and the token received
//            // is not null
//            if (validateAuthCodeResponse.isSuccessfullyProcessed()
//                    && (validateAuthCodeResponse.getAccessToken() != null)) {
//
//                Object obj = theiaTransactionalRedisUtil.get(redisKey);
//                TheiaCreateAgreementRequest theiaCreateAgreementRequest = JsonMapper.convertValue(obj,
//                        TheiaCreateAgreementRequest.class);
//
//                CreateAgreementRequest createAgreementRequest = agreementHelper
//                        .generateRequest(theiaCreateAgreementRequest);
//
//                createAgreementResponse = paymentServicesClientImpl.initiateCreateAgreement(createAgreementRequest);
//
//                TheiaCreateAgreementResponse theiaCreateAgreementResponse = agreementHelper
//                        .generateResponse(createAgreementResponse);
//
//                request.setAttribute("agreementResponse", theiaCreateAgreementResponse);
//                request.setAttribute("callBackURL", theiaCreateAgreementRequest.getBody().getCallBackURL());
//
//                request.getRequestDispatcher(
//                        VIEW_BASE + theiaViewResolverService.returnConfirmationPage(request) + ".jsp").forward(request,
//                        response);
//                return;
//            } else {
//                request.setAttribute("errorMessage", validateAuthCodeResponse.getResponseMessage());
//                // TODO return proper error page
//                return;
//            }
//        } catch (Exception e) {
//            request.setAttribute("errorMessage", e.getMessage());
//            // TODO return proper error page
//            return;
//        }
//    }
//
//    @RequestMapping(value = "/agreement/initiateRequest", method = { RequestMethod.POST })
//    public AgreementResponse initiateAgreement(HttpServletRequest request, HttpServletResponse response,
//            @RequestBody TheiaCreateAgreementRequest theiaCreateAgreementRequest) {
//
//        String redisKey = new StringBuilder().append(KEY).append(agreementHelper.generateRandom()).toString();
//        theiaTransactionalRedisUtil.set(redisKey, theiaCreateAgreementRequest);
//
//        String responseUrl = agreementHelper.generateResponseURL(redisKey);
//
//        AgreementResponse agreementResponse = agreementHelper.createResponse(theiaCreateAgreementRequest, responseUrl);
//
//        return agreementResponse;
//
//    }
//
//    @RequestMapping(value = "/agreement/initiate", method = { RequestMethod.POST })
//    public void initiateAgreementV2(HttpServletRequest request, HttpServletResponse response) {
//
//        TheiaCreateAgreementRequest theiaCreateAgreementRequest = agreementHelper.createRequest(request);
//
//        String redisKey = new StringBuilder().append(KEY).append(agreementHelper.generateRandom()).toString();
//        theiaTransactionalRedisUtil.set(redisKey, theiaCreateAgreementRequest);
//
//        String responseUrl = agreementHelper.generateResponseURL(redisKey);
//
//        response.setHeader("Location", responseUrl);
//        response.setStatus(302);
//
//    }
// }
