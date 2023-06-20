//package com.paytm.pgplus.theia.nativ.processor;
//
//import com.paytm.pgplus.aoaSubscriptionClient.model.request.AoaMandateCallbackRequest;
//import com.paytm.pgplus.aoaSubscriptionClient.model.request.AoaMandateCreateRequest;
//import com.paytm.pgplus.aoaSubscriptionClient.model.response.AoaMandateCallbackResponse;
//import com.paytm.pgplus.aoaSubscriptionClient.model.response.AoaMandateCreateResponse;
//import com.paytm.pgplus.aoaSubscriptionClient.service.IAoaSubscriptionService;
//import com.paytm.pgplus.payloadvault.subscription.request.MandateRequest;
//import com.paytm.pgplus.payloadvault.subscription.request.PaperMandateCreateRequest;
//import com.paytm.pgplus.payloadvault.subscription.request.ProcessedMandateRequest;
//import com.paytm.pgplus.payloadvault.subscription.response.MandateResponse;
//import com.paytm.pgplus.payloadvault.subscription.response.PaperMandateCreateResponse;
//import com.paytm.pgplus.payloadvault.subscription.response.ProcessedMandateResponse;
//import com.paytm.pgplus.payloadvault.subscription.response.ResultInfo;
//import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
//import com.paytm.pgplus.theia.exceptions.MandateException;
//import com.paytm.pgplus.theia.models.ProcessedBmResponse;
//import com.paytm.pgplus.theia.offline.enums.ResultCode;
//import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Service
//public class AOAMandateProcessor {
//    private static final Logger LOGGER = LoggerFactory.getLogger(AOAMandateProcessor.class);
//
//    @Autowired
//    private IAoaSubscriptionService aoasubscriptionService;
//
//    public AoaMandateCreateResponse createMandate(AoaMandateCreateRequest mandateRequest, String callBackUrl,
//                                          PaymentRequestBean requestBean) {
//
//        AoaMandateCreateResponse response = aoasubscriptionService.createAoaMandate(mandateRequest);
//        if (null != response) {
//            LOGGER.debug("Response received for Create Mandate {}", response);
//
//            if (response.getResultInfo().getStatus().equalsIgnoreCase("SUCCESS")) {
//                return response;
//            } else {
//                ResultInfo resultInfo = new ResultInfo();
//                if(response.getResultInfo() != null){
//                    resultInfo.setCode(response.getResultInfo().getCode());
//                    resultInfo.setMessage(response.getResultInfo().getMessage());
//                    resultInfo.setStatus(response.getResultInfo().getStatus());
//                }
//                throw new MandateException.ExceptionBuilder(callBackUrl, resultInfo, false).setRequestBean(requestBean)
//                        .build();
//            }
//        } else {
//            LOGGER.error("Errorneous response received from create mandate request api {}", response);
//            throw new MandateException.ExceptionBuilder(callBackUrl,
//                    OfflinePaymentUtils.resultInfo(ResultCode.MERCHANT_REDIRECT_REQUEST_EXCEPTION), false)
//                    .setRequestBean(requestBean).build();
//        }
//    }
//
//    public ProcessedBmResponse processMandate(AoaMandateCallbackRequest mandateRequest) {
//        AoaMandateCallbackResponse response = aoasubscriptionService.aoaMandateCallback(mandateRequest);
//        if (null != response) {
//            LOGGER.info("Response received for Process Mandate {}", response);
//            ProcessedBmResponse bmResponse = new ProcessedBmResponse(response);
//            return bmResponse;
//        } else {
//            LOGGER.error("Errorneous response received from aoaMandateCallback subscription service {}", response);
//            throw new MandateException.ExceptionBuilder(null,
//                    OfflinePaymentUtils.resultInfo(ResultCode.MERCHANT_REDIRECT_REQUEST_EXCEPTION), false).build();
//        }
//    }
// }
