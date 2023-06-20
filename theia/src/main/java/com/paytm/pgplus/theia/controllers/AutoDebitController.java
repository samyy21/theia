//package com.paytm.pgplus.theia.controllers;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.paytm.pgplus.biz.workflow.model.AutoDebitResponse;
//import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
//import com.paytm.pgplus.common.enums.EPayMethod;
//import com.paytm.pgplus.common.enums.ERequestType;
//import com.paytm.pgplus.facade.exception.FacadeCheckedException;
//import com.paytm.pgplus.facade.utils.JsonMapper;
//import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
//import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
//import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
//import com.paytm.pgplus.theia.constants.TheiaConstant;
//import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
//import com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes;
//import com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants;
//import com.paytm.pgplus.theia.enums.ValidationResults;
//import com.paytm.pgplus.theia.models.AutoDebitRequest;
//import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
//import com.paytm.pgplus.theia.services.IJsonResponsePaymentService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.Locale;
//
///**
// * @author manojpal
// *
// */
//@RestController
//@RequestMapping("HANDLER_IVR")
//public class AutoDebitController {
//    private static final Logger LOGGER = LoggerFactory.getLogger(AutoDebitController.class);
//    private static final Logger EVENT_LOGGER = LoggerFactory.getLogger("EVENT_LOGGER");
//    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
//
//    @Autowired
//    @Qualifier("autoDebitService")
//    IJsonResponsePaymentService autoDebitService;
//
//    @Autowired
//    @Qualifier("merchantPreferenceService")
//    private IMerchantPreferenceService merchantPreferenceService;
//
//    @Autowired
//    private NativePaymentUtil nativePaymentUtil;
//
//    @RequestMapping(value = "/CLW_APP_PAY", method = { RequestMethod.GET, RequestMethod.POST })
//    public String processTransaction(final HttpServletRequest request,
//            @RequestParam("JsonData") String encodedRequestData, Locale locale) {
//        final long startTime = System.currentTimeMillis();
//
//        // Validate request data
//        AutoDebitResponse autoDebitResponse = new AutoDebitResponse();
//        try {
//            AutoDebitRequest requestData = JsonMapper.mapJsonToObject(encodedRequestData, AutoDebitRequest.class);
//            LOGGER.info("AutoDebitRequest data : {}", requestData);
//            nativePaymentUtil.logNativeRequests(request.toString());
//
//            PaymentRequestBean paymentRequestBean = preparePaymentRequestBean(requestData);
//            paymentRequestBean.setChecksumhash(requestData.getCheckSum());
//            paymentRequestBean.setRequest(request);
//            paymentRequestBean.setSessionRequired(false);
//            ValidationResults validationResult = autoDebitService.validatePaymentRequest(paymentRequestBean);
//            switch (validationResult) {
//            case CHECKSUM_VALIDATION_FAILURE:
//                autoDebitResponse.setStatus(ExternalTransactionStatus.TXN_FAILURE.name());
//                autoDebitResponse.setResponseCode(ResponseConstants.ResponseCodes.CHECKSUM_FAILURE_CODE);
//                autoDebitResponse.setResponseMessage(ResponseConstants.CHECKSUM_FAILURE_MESSAGE);
//                break;
//            case VALIDATION_SUCCESS:
//                // Only for Fast-Forward or Auto debit functionality
//                if (requestData.getReqType() != null && requestData.getReqType().equals(RequestTypes.AUTO_DEBIT)) {
//                    paymentRequestBean.setRequestType(ERequestType.DEFAULT.getType());
//                }
//                WorkFlowResponseBean workFlowResponseBean = autoDebitService.processPaymentRequest(paymentRequestBean);
//                LOGGER.debug("Response data : {}", workFlowResponseBean);
//                autoDebitResponse = workFlowResponseBean.getAutoDebitResponse();
//                break;
//            case UNKNOWN_VALIDATION_FAILURE:
//                break;
//            }
//            generateAutoDebitResponseData(paymentRequestBean, autoDebitResponse);
//            String jsonResponse = JsonMapper.mapObjectToJson(autoDebitResponse);
//            // LOGGER.info("JSON Response generated: {}", jsonResponse);
//            EVENT_LOGGER.info("JSON Response generated: {}", jsonResponse);
//            return jsonResponse;
//
//        } catch (final Exception e) {
//            LOGGER.error("SYSTEM_ERROR :", e);
//        } finally {
//            LOGGER.info("Total time takenn for AutoDebitController is {} ms", System.currentTimeMillis() - startTime);
//            nativePaymentUtil.logNativeResponse(startTime);
//        }
//        return generateResponseForExceptionCases(autoDebitResponse);
//    }
//
//    private String generateResponseForExceptionCases(AutoDebitResponse autoDebitResponse) {
//        autoDebitResponse.setStatus(ExternalTransactionStatus.TXN_FAILURE.name());
//        autoDebitResponse.setResponseMessage(com.paytm.pgplus.pgproxycommon.enums.ResponseConstants.SYSTEM_ERROR
//                .getMessage());
//        autoDebitResponse
//                .setResponseCode(com.paytm.pgplus.pgproxycommon.enums.ResponseConstants.SYSTEM_ERROR.getCode());
//        String jsonResponse = null;
//
//        try {
//            jsonResponse = JsonMapper.mapObjectToJson(autoDebitResponse);
//        } catch (FacadeCheckedException e) {
//            LOGGER.error("JSON mapping exception", e);
//        }
//
//        LOGGER.info("JSON Response generated : {}", jsonResponse);
//        return jsonResponse;
//    }
//
//    /**
//     * @param request
//     * @param requestData
//     */
//    private PaymentRequestBean preparePaymentRequestBean(AutoDebitRequest requestData) throws JsonProcessingException {
//        /*
//         * No need to set AutoMode,Channel, TokenType here. Channel will be set
//         * later using terminalType details which is SYSTEM in this case. AppIP
//         * will be set directly in clientIp in EnvInfo
//         */
//        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
//        paymentRequestBean.setRequestType(requestData.getReqType());
//        paymentRequestBean.setMid(requestData.getMid());
//        paymentRequestBean.setOrderId(requestData.getOrderId());
//        paymentRequestBean.setTxnAmount(requestData.getTxnAmount());
//        paymentRequestBean.setCustId(requestData.getCustomerId());
//        paymentRequestBean.setCurrency(requestData.getCurrency());
//        paymentRequestBean.setDeviceId(requestData.getDeviceId());
//        paymentRequestBean.setSsoToken(requestData.getSsoToken());
//        paymentRequestBean.setPaymentTypeId(requestData.getPaymentMode());
//        paymentRequestBean.setIsRefund(requestData.getRefund());
//        paymentRequestBean.setIndustryTypeId(requestData.getIndustryType());
//        paymentRequestBean.setAppIp(requestData.getAppIP());
//        paymentRequestBean.setClientId(requestData.getClientId());
//        paymentRequestBean.setSubwalletAmount(OBJECT_MAPPER.writeValueAsString(requestData.getSubwalletAmount()));
//        paymentRequestBean.setExchangeRate(requestData.getExchangeRate());
//        // setting extendInfo for AutoDebitController
//        if (requestData.getExtendInfo() != null) {
//            paymentRequestBean.setExtendInfo(requestData.getExtendInfo());
//        }
//
//        boolean isDefaultFFWebsiteEnabled = merchantPreferenceService.isDefaultFFWebsiteEnabled(paymentRequestBean
//                .getMid());
//
//        if (isDefaultFFWebsiteEnabled) {
//            paymentRequestBean.setWebsite(TheiaConstant.ExtraConstants.DEFAULT_WEBSITE_FF);
//        }
//
//        paymentRequestBean.setLoyaltyPointRootUserId(requestData.getLoyaltyPointRootUserId());
//        paymentRequestBean.setLoyaltyPointRootUserPGMid(requestData.getLoyaltyPointRootUserPGMid());
//        return paymentRequestBean;
//    }
//
//    private AutoDebitResponse generateAutoDebitResponseData(PaymentRequestBean requestData,
//            AutoDebitResponse autoDebitResponse) {
//        /**
//         * Set response data BankTxnId & MBID wont be available in Auto debit
//         * scenario
//         */
//        setBankName(requestData, autoDebitResponse);
//        autoDebitResponse.setCheckSum(requestData.getChecksumhash());
//        autoDebitResponse.setCustId(requestData.getCustId());
//        autoDebitResponse.setMerchantId(requestData.getMid());
//        autoDebitResponse.setOrderId(requestData.getOrderId());
//        autoDebitResponse.setPaymentMode(requestData.getPaymentTypeId());
//        autoDebitResponse.setTxnAmount(requestData.getTxnAmount());
//        return autoDebitResponse;
//    }
//
//    private void setBankName(PaymentRequestBean requestData, AutoDebitResponse autoDebitResponse) {
//        if (EPayMethod.LOYALTY_POINT.getMethod().equals(requestData.getPaymentTypeId())) {
//            autoDebitResponse.setBankName(ExtraConstants.LOYALTY_POINT_BANK_NAME);
//        } else if (EPayMethod.BALANCE.getOldName().equals(requestData.getPaymentTypeId())) {
//            autoDebitResponse.setBankName(ExtraConstants.AUTO_DEBIT_BANK_NAME);
//        }
//    }
// }