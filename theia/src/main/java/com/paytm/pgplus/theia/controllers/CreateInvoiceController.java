///**
// *
// */
//package com.paytm.pgplus.theia.controllers;
//
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Map.Entry;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.ws.rs.Consumes;
//import javax.ws.rs.Produces;
//import javax.ws.rs.core.MediaType;
//
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.slf4j.MDC;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.paytm.pgplus.biz.workflow.model.InvoiceResponseBean;
//import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
//import com.paytm.pgplus.facade.exception.FacadeCheckedException;
//import com.paytm.pgplus.facade.utils.JsonMapper;
//import com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants;
//import com.paytm.pgplus.theia.enums.ValidationResults;
//import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
//import com.paytm.pgplus.theia.services.IJsonResponsePaymentService;
//import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
//import com.paytm.pgplus.theia.utils.BeanParamValidator;
//
///**
// * @author namanjain
// *
// */
//@RestController
//public class CreateInvoiceController {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(CreateInvoiceController.class);
//
//    @Autowired
//    @Qualifier(value = "theiaViewResolverService")
//    private ITheiaViewResolverService theiaViewResolverService;
//
//    @Autowired
//    @Qualifier("createInvoicePaymentService")
//    IJsonResponsePaymentService createInvoicepaymentService;
//
//    @RequestMapping(value = "/createInvoice", method = { RequestMethod.GET, RequestMethod.POST })
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    public String invoiceResponse(final HttpServletRequest request, @RequestBody PaymentRequestBean paymentRequestBean) {
//        LOGGER.info("Request received for invoice response : {}", paymentRequestBean);
//        setMDC(paymentRequestBean);
//
//        try {
//            paymentRequestBean.setChecksumMap(getParamMap(paymentRequestBean));
//            paymentRequestBean.setRequest(request);
//            paymentRequestBean.setSessionRequired(false);
//            InvoiceResponseBean invoiceResponseBean = null;
//            /*
//             * ValidationResults validationResult =
//             * createInvoicepaymentService.validatePaymentRequest
//             * (paymentRequestBean);
//             *
//             * switch (validationResult) { case CHECKSUM_VALIDATION_FAILURE:
//             * invoiceResponseBean = new InvoiceResponseBean();
//             * invoiceResponseBean.setStatus(ResponseConstants.FAIL);
//             * invoiceResponseBean
//             * .setErrorCode(ResponseConstants.ResponseCodes.
//             * CHECKSUM_FAILURE_CODE );
//             * invoiceResponseBean.setErrorMessage(ResponseConstants.
//             * CHECKSUM_FAILURE_MESSAGE); break; case
//             * UNKNOWN_VALIDATION_FAILURE: invoiceResponseBean = new
//             * InvoiceResponseBean();
//             * invoiceResponseBean.setStatus(ResponseConstants.FAIL);
//             * invoiceResponseBean
//             * .setErrorCode(ResponseConstants.INVOIVE_FAILURE_CODE);
//             * invoiceResponseBean
//             * .setErrorMessage(ResponseConstants.INVOIVE_FAILURE_MESSAGE);
//             * break; case VALIDATION_SUCCESS:
//             */
//            WorkFlowResponseBean workFlowResponseBean = createInvoicepaymentService
//                    .processPaymentRequest(paymentRequestBean);
//            LOGGER.debug("Response data :{}", workFlowResponseBean);
//            if (!BeanParamValidator.validateInputObjectParam(workFlowResponseBean)
//                    || !BeanParamValidator.validateInputObjectParam(workFlowResponseBean.getInvoiceResponseBean())) {
//                invoiceResponseBean = new InvoiceResponseBean();
//                invoiceResponseBean.setStatus(ResponseConstants.FAIL);
//                invoiceResponseBean.setErrorCode(ResponseConstants.INVOIVE_FAILURE_CODE);
//                invoiceResponseBean.setErrorMessage(ResponseConstants.INVOIVE_FAILURE_MESSAGE);
//            } else {
//                invoiceResponseBean = workFlowResponseBean.getInvoiceResponseBean();
//                invoiceResponseBean.setStatus(ResponseConstants.SUCCESS);
//            }
//            // break;
//            // }
//            String jsonResponse = JsonMapper.mapObjectToJson(invoiceResponseBean);
//            LOGGER.info("Response returned : {}, JSON : {}", invoiceResponseBean, jsonResponse);
//            return jsonResponse;
//        } catch (Exception e) {
//            LOGGER.error("Exception Occurred while fetching invoiceJSON", e);
//        }
//        LOGGER.error("Response returned : NULL");
//        return null;
//    }
//
//    @SuppressWarnings("unchecked")
//    private Map<String, String> getParamMap(PaymentRequestBean paymentRequestData) throws FacadeCheckedException {
//
//        String jsonString = JsonMapper.mapObjectToJson(paymentRequestData);
//        if (StringUtils.isEmpty(jsonString)) {
//            return Collections.emptyMap();
//        }
//        Map<Object, Object> objectMap = JsonMapper.mapJsonToObject(jsonString, Map.class);
//        Map<String, String> finalMap = new HashMap<>();
//        for (Entry<Object, Object> entry : objectMap.entrySet()) {
//            finalMap.put(entry.getKey().toString(), entry.getValue().toString());
//        }
//        return finalMap;
//    }
//
//    private void setMDC(PaymentRequestBean paymentRequestBean) {
//        MDC.clear();
//        MDC.put("MID", paymentRequestBean.getMid());
//        MDC.put("ORDER_ID", paymentRequestBean.getOrderId());
//    }
// }
