//package com.paytm.pgplus.theia.controllers;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.ws.rs.Consumes;
//import javax.ws.rs.Produces;
//import javax.ws.rs.core.MediaType;
//
//import com.paytm.pgplus.stats.util.AWSStatsDUtils;
//import com.paytm.pgplus.theia.services.helper.FF4JHelper;
//import org.apache.commons.io.Charsets;
//import org.apache.commons.io.IOUtils;
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
//import com.paytm.pgplus.common.model.EnvInfoRequestBean;
//import com.paytm.pgplus.facade.exception.FacadeCheckedException;
//import com.paytm.pgplus.facade.utils.JsonMapper;
//import com.paytm.pgplus.theia.constants.TheiaConstant;
//import com.paytm.pgplus.theia.enums.CloseOrderStatus;
//import com.paytm.pgplus.theia.models.CancelTransRequest;
//import com.paytm.pgplus.theia.models.CancelTransResponse;
//import com.paytm.pgplus.theia.services.ICloseOrderService;
//import com.paytm.pgplus.theia.utils.EnvInfoUtil;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * @author kartik
// * @date 05-07-2017
// */
//@RestController
//public class CloseOrderController {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(CloseOrderController.class);
//
//    @Autowired
//    @Qualifier("closeOrderServiceImpl")
//    private ICloseOrderService closeOrderServiceImpl;
//    @Autowired
//    private AWSStatsDUtils statsDUtils;
//
//    @Autowired
//    private FF4JHelper fF4JHelper;
//
//    @RequestMapping(value = "/closeOrder", method = RequestMethod.POST)
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED })
//    public String cancelTransOnUserDrop(final HttpServletRequest request) {
//        long startTime = System.currentTimeMillis();
//        /**
//         * To Support Content Type application/x-www-form-urlencoded for APP's
//         * Backward Compatibility
//         */
//
//        CancelTransRequest cancelTransRequest = null;
//        if (MediaType.APPLICATION_JSON.equals(request.getContentType())) {
//            try {
//                String requestData = IOUtils.toString(request.getInputStream(), Charsets.UTF_8.name());
//                cancelTransRequest = JsonMapper.mapJsonToObject(requestData, CancelTransRequest.class);
//            } catch (IOException e) {
//                LOGGER.warn(
//                        "IOException Occured while reading content from closeOrder API, application/json content-type {} ",
//                        e);
//                return generateCancelTxnResponse(closeOrderServiceImpl
//                        .generateCloseOrderResponse(CloseOrderStatus.INTERNAL_PROCESSING_ERROR));
//            } catch (FacadeCheckedException e) {
//                LOGGER.warn("Json Parsing Exception while parsing json from closeOrder request Body");
//                return generateCancelTxnResponse(closeOrderServiceImpl
//                        .generateCloseOrderResponse(CloseOrderStatus.INVALID_REQUEST));
//            }
//            LOGGER.info("JSON Request received for cancel Txn for MID: {}", cancelTransRequest.getMerchantId());
//        }
//        if (MediaType.APPLICATION_FORM_URLENCODED.equals(request.getContentType())) {
//            String mid = request.getParameter(TheiaConstant.RequestParams.MID);
//            String orderId = request.getParameter(TheiaConstant.RequestParams.ORDER_ID);
//            if (StringUtils.isBlank(mid) || StringUtils.isBlank(orderId)) {
//                LOGGER.info("Insufficient Parameter in CloseOrder Form Post");
//                return generateCancelTxnResponse(closeOrderServiceImpl
//                        .generateCloseOrderResponse(CloseOrderStatus.INVALID_REQUEST));
//            }
//            String userToken = request.getParameter("USER_TOKEN");
//            boolean isForceClose = Boolean.parseBoolean(request.getParameter("IS_FORCE_CLOSE"));
//            cancelTransRequest = new CancelTransRequest(mid, orderId, userToken, isForceClose);
//            LOGGER.info("Form Post Request received for cancel Txn for MID: {}", cancelTransRequest.getMerchantId());
//        }
//        setMDC(cancelTransRequest);
//        CancelTransResponse cancelTransResponse = null;
//        try {
//            LOGGER.info("Request received for cancel Txn : {}", cancelTransRequest);
//            if (cancelTransRequest != null
//                    && fF4JHelper.isFF4JFeatureForMidEnabled(
//                            TheiaConstant.ExtraConstants.EXEMPT_MID_LIST_FROM_CLOSE_ORDER_OLD_API,
//                            cancelTransRequest.getMerchantId())) {
//                return generateCancelTxnResponse(closeOrderServiceImpl
//                        .generateCloseOrderResponse(CloseOrderStatus.SUCCESS));
//            }
//            final EnvInfoRequestBean envInfo = EnvInfoUtil.fetchEnvInfo(request);
//            cancelTransResponse = closeOrderServiceImpl.processCancelOrderRequest(cancelTransRequest, envInfo);
//            try {
//                Map<String, String> responseMap = new HashMap<>();
//                responseMap.put("RESPONSE_STATUS", cancelTransResponse.getStatus());
//                responseMap.put("RESPONSE_MESSAGE", cancelTransResponse.getStatusMessage());
//                statsDUtils.pushResponse("CloseOrder", responseMap);
//            } catch (Exception exception) {
//                LOGGER.error("Error in pushing response message " + "CloseOrder" + "to grafana", exception);
//            }
//        } catch (Exception e) {
//            LOGGER.error("Exception occurred while processing cancel txn request {}", e);
//            cancelTransResponse = new CancelTransResponse();
//            cancelTransResponse.setStatus(CloseOrderStatus.INTERNAL_PROCESSING_ERROR.getStatus());
//            cancelTransResponse.setStatusMessage(CloseOrderStatus.INTERNAL_PROCESSING_ERROR.getStatusMessage());
//            cancelTransResponse.setStatusCode(CloseOrderStatus.INTERNAL_PROCESSING_ERROR.getStatusCode());
//        } finally {
//            LOGGER.info("Total time taken for CloseOrderController is {} ms", System.currentTimeMillis() - startTime);
//        }
//        return generateCancelTxnResponse(cancelTransResponse);
//    }
//
//    private void setMDC(CancelTransRequest cancelTransRequest) {
//        MDC.clear();
//        if ((cancelTransRequest != null) && StringUtils.isNotBlank(cancelTransRequest.getMerchantId())) {
//            MDC.put(TheiaConstant.RequestParams.MID, cancelTransRequest.getMerchantId());
//        }
//        if ((cancelTransRequest != null) && StringUtils.isNotBlank(cancelTransRequest.getOrderId())) {
//            MDC.put(TheiaConstant.RequestParams.ORDER_ID, cancelTransRequest.getOrderId());
//        }
//    }
//
//    private String generateCancelTxnResponse(CancelTransResponse response) {
//        String jsonResponse = null;
//        try {
//            jsonResponse = JsonMapper.mapObjectToJson(response);
//        } catch (FacadeCheckedException e) {
//            LOGGER.error("JSON mapping exception :", e);
//        }
//        LOGGER.info("JSON response generated : {}", jsonResponse);
//        return jsonResponse;
//    }
//
// }
