//package com.paytm.pgplus.theia.controllers;
//
//import java.io.IOException;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import com.paytm.pgplus.theia.utils.ResponseCodeUtil;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.slf4j.MDC;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//
//import com.paytm.pgplus.biz.workflow.model.SeamlessACSPaymentResponse;
//import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
//import com.paytm.pgplus.cache.model.ResponseCodeDetails;
//import com.paytm.pgplus.dynamicwrapper.service.IWrapperService;
//import com.paytm.pgplus.facade.exception.FacadeCheckedException;
//import com.paytm.pgplus.facade.utils.JsonMapper;
//import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
//import com.paytm.pgplus.mappingserviceclient.service.IResponseCodeService;
//import com.paytm.pgplus.payloadvault.dynamicwrapper.enums.API;
//import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
//import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
//import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
//import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
//import com.paytm.pgplus.theia.constants.TheiaConstant;
//import com.paytm.pgplus.theia.services.IJsonResponsePaymentService;
//
//@Controller
//public class FBWrappedPaymentController {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(FBWrappedPaymentController.class);
//    private static final String errorMessage = "{\"response_code\":\"311\",\"response_message\":\"Payment Error\"}";
//    @Autowired
//    @Qualifier(value = "seamlessACSPaymentService")
//    private IJsonResponsePaymentService seamlessACSPaymentService;
//
//    @Autowired
//    @Qualifier(value = "wrapperImpl")
//    private IWrapperService wrapperService;
//
//    @Autowired
//    @Qualifier(value = "merchantPreferenceService")
//    private IMerchantPreferenceService merchantPreferenceService;
//
//    @Autowired
//    private ResponseCodeUtil responseCodeUtil;
//
//    @RequestMapping(value = "/payment/credit-card/authorize", method = { RequestMethod.GET, RequestMethod.POST })
//    public void paymentCreditCardAuthorize(final HttpServletRequest request, final HttpServletResponse response,
//            final Model model) throws IOException, ServletException {
//        long startTime = System.currentTimeMillis();
//        LOGGER.info("Received request for paymentCreditCardAuthorize");
//        response.setContentType("application/json");
//        try {
//            PaymentRequestBean requestBean = wrapperService.wrapRequest(request, "FacebookPaymentCCConfig",
//                    API.PROCESS_TRANSACTION);
//            String s2sResponse = process(request, requestBean);
//            LOGGER.info("S2S response returned by service is : {} ", s2sResponse);
//            String finalResponse = JsonMapper.mapObjectToJson(wrapperService.wrapResponse(s2sResponse, requestBean,
//                    "FacebookPaymentCCConfig", API.PROCESS_TRANSACTION));
//            LOGGER.info("Final response being sent back is : {}", finalResponse);
//            response.getOutputStream().write(finalResponse.getBytes());
//            return;
//        } catch (Exception e) {
//            LOGGER.error("Exception , ", e);
//        } finally {
//            LOGGER.info("Total time taken to process paymentCreditCardAuthorize is : {} ms", System.currentTimeMillis()
//                    - startTime);
//        }
//        response.getOutputStream().write(errorMessage.getBytes());
//    }
//
//    private String process(final HttpServletRequest request, PaymentRequestBean requestBean)
//            throws FacadeCheckedException {
//        MDC.put(TheiaConstant.RequestParams.MID, requestBean.getMid());
//        MDC.put(TheiaConstant.RequestParams.ORDER_ID, requestBean.getOrderId());
//        requestBean.setRequest(request);
//        requestBean.setSessionRequired(false);
//        if (merchantPreferenceService.isMerchantLinkedToFacebook(requestBean.getMid())) {
//            WorkFlowResponseBean workFlowResponseBean = seamlessACSPaymentService.processPaymentRequest(requestBean);
//            return JsonMapper.mapObjectToJson(workFlowResponseBean.getSeamlessACSPaymentResponse());
//        }
//        LOGGER.error("Merchant MID : {} is not linked to facebook platform.", requestBean.getMid());
//        SeamlessACSPaymentResponse seamlessACSPaymentResponse = new SeamlessACSPaymentResponse();
//        seamlessACSPaymentResponse.setMid(requestBean.getMid());
//        seamlessACSPaymentResponse.setOrderId(requestBean.getOrderId());
//        seamlessACSPaymentResponse.setRespCode(ResponseConstants.INVALID_MID.getCode());
//        seamlessACSPaymentResponse.setStatus(ExternalTransactionStatus.TXN_FAILURE.name());
//        setResponseMessage(ResponseConstants.INVALID_MID, seamlessACSPaymentResponse);
//        return JsonMapper.mapObjectToJson(seamlessACSPaymentResponse);
//    }
//
//    @RequestMapping(value = "/payment/online-banking", method = { RequestMethod.GET, RequestMethod.POST })
//    public void paymentOnlineBanking(final HttpServletRequest request, final HttpServletResponse response,
//            final Model model) throws IOException, ServletException {
//        long startTime = System.currentTimeMillis();
//        // LOGGER.info("Received request for paymentOnlineBanking");
//        response.setContentType("application/json");
//        try {
//            PaymentRequestBean requestBean = wrapperService.wrapRequest(request, "FacebookPaymentNBConfig",
//                    API.PROCESS_TRANSACTION);
//            String s2sResponse = process(request, requestBean);
//            LOGGER.info("S2S response returned by service is : {} ", s2sResponse);
//            String finalResponse = JsonMapper.mapObjectToJson(wrapperService.wrapResponse(s2sResponse, requestBean,
//                    "FacebookPaymentNBConfig", API.PROCESS_TRANSACTION));
//            LOGGER.info("Final response being sent back is : {}", finalResponse);
//            response.getOutputStream().write(finalResponse.getBytes());
//            return;
//        } catch (Exception e) {
//            LOGGER.error("Exception , ", e);
//        } finally {
//            LOGGER.info("Total time taken to process paymentOnlineBanking is : {} ms", System.currentTimeMillis()
//                    - startTime);
//        }
//        response.getOutputStream().write(errorMessage.getBytes());
//    }
//
//    private void setResponseMessage(ResponseConstants responseConstants, SeamlessACSPaymentResponse response) {
//        ResponseCodeDetails responseCodeDetails = responseCodeUtil.getResponseCodeDetails(null,
//                responseConstants.getSystemResponseCode(), response.getStatus());
//        if (responseCodeDetails != null && StringUtils.isNotBlank(responseCodeDetails.getResponseCode())) {
//            response.setRespCode(responseCodeDetails.getResponseCode());
//            response.setRespMsg(responseCodeUtil.getResponseMsg(responseCodeDetails));
//        } else {
//            response.setRespCode(responseConstants.getCode());
//            response.setRespMsg(responseConstants.getMessage());
//        }
//    }
//
// }
