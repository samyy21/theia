//package com.paytm.pgplus.theia.controllers;
//
//import com.paytm.pgplus.biz.enums.GenerateEsnEnum;
//import com.paytm.pgplus.biz.workflow.model.GenerateEsnResponseBody;
//import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
//import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
//import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
//import com.paytm.pgplus.common.signature.JWTWithHmacSHA256;
//import com.paytm.pgplus.facade.utils.JsonMapper;
//import com.paytm.pgplus.logging.ExtendedLogger;
//import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
//import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
//import com.paytm.pgplus.theia.nativ.utils.*;
//import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
//import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
//import com.paytm.pgplus.theia.models.*;
//import com.paytm.pgplus.theia.utils.ProcessTransactionUtil;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.RestController;
//import javax.servlet.http.HttpServletRequest;
//import javax.ws.rs.Consumes;
//import javax.ws.rs.POST;
//import javax.ws.rs.Produces;
//import javax.ws.rs.core.MediaType;
//import java.util.HashMap;
//import java.util.Map;
//import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.GenerateEsnKeys.EXTERNAL_SERIAL_NUMBER;
//
//@RestController
//public class GenerateEsnController {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateEsnController.class);
//    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(GenerateEsnController.class);
//
//    @Autowired
//    @Qualifier("bizRequestResponseMapper")
//    private IBizRequestResponseMapper bizRequestResponseMapper;
//
//    @Autowired
//    @Qualifier("GenerateEsnWorkflow")
//    private IWorkFlow bizWorkFlow;
//
//    @Autowired
//    private NativeSessionUtil nativeSessionUtil;
//
//    @Autowired
//    private ProcessTransactionUtil processTransactionUtil;
//
//    @POST
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    @RequestMapping(value = "/v1/generateEsn")
//    @ResponseBody
//    public GenerateEsnResponse generateEsn(@RequestBody String requestPayload, HttpServletRequest httpServletRequest) {
//        final long startTime = System.currentTimeMillis();
//        GenerateEsnRequest generateEsnRequest = null;
//        try {
//            generateEsnRequest = JsonMapper.mapJsonToObject(requestPayload, GenerateEsnRequest.class);
//            LOGGER.info("Received request for GenerateEsnRequest : {}", generateEsnRequest);
//        } catch (Exception e) {
//            LOGGER.error("Error While Parsing Request : {}", requestPayload);
//            return prepareGenerateEsnResponse(new WorkFlowResponseBean(), generateEsnRequest);
//        }
//        if (!validateJwtToken(generateEsnRequest)) {
//            return prepareGenerateEsnResponse(new WorkFlowResponseBean(), generateEsnRequest);
//        }
//        WorkFlowResponseBean workFlowResponseBean;
//        workFlowResponseBean = processGenerateEsnRequest(generateEsnRequest, httpServletRequest);
//        GenerateEsnResponse generateEsnResponse = new GenerateEsnResponse();
//        if (workFlowResponseBean != null) {
//            generateEsnResponse = prepareGenerateEsnResponse(workFlowResponseBean, generateEsnRequest);
//        }
//        LOGGER.info("Total time taken by generateEsn API : {}ms", System.currentTimeMillis() - startTime);
//        return generateEsnResponse;
//    }
//
//    private WorkFlowResponseBean processGenerateEsnRequest(GenerateEsnRequest request,
//            HttpServletRequest httpServletRequest) {
//        PaymentRequestBean paymentRequestBean;
//        GenerateEsnResponseBody response = new GenerateEsnResponseBody();
//        // Fetch Payment Request Bean from Redis that is cached against ESN
//        try {
//            paymentRequestBean = nativeSessionUtil.getPaymentRequestBeanAgainstEsn(request.getBody()
//                    .getExternalSerialNo());
//            LOGGER.info("PaymentRequestBean from Redis in generateESN : {}", paymentRequestBean);
//            if (paymentRequestBean == null) {
//                throw new TheiaServiceException();
//            }
//            paymentRequestBean.setRequest(httpServletRequest);
//            paymentRequestBean.setGenerateEsnRequest(true);
//            paymentRequestBean.setDeepLinkRequired(false);
//            paymentRequestBean.setDeepLinkFromInsta(false);
//            paymentRequestBean.setSessionRequired(false);
//            paymentRequestBean.setNativeJsonRequest(false);
//        } catch (TheiaServiceException e) {
//            LOGGER.error("Error While Parsing Cached Payment Request Bean");
//            setGenerateEsnResponse(GenerateEsnEnum.FAIL, response, GenerateEsnEnum.FAIL.getResultMsg());
//            WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
//            workFlowResponseBean.setGenerateEsnResponseBody(response);
//            return workFlowResponseBean;
//        }
//        GenericCoreResponseBean<WorkFlowResponseBean> workflowResponse;
//        try {
//            WorkFlowRequestBean workFlowRequestBean = bizRequestResponseMapper
//                    .mapWorkFlowRequestData(paymentRequestBean);
//            enhanceWorkFlowRequestBean(workFlowRequestBean);
//            processTransactionUtil.setBankFormOptimizationFlow(workFlowRequestBean, paymentRequestBean);
//            Map<String, String> channelInfo = workFlowRequestBean.getChannelInfo();
//            if (null == channelInfo) {
//                channelInfo = new HashMap<>();
//                workFlowRequestBean.setChannelInfo(channelInfo);
//            }
//            workflowResponse = bizWorkFlow.process(workFlowRequestBean);
//        } catch (Exception e) {
//            LOGGER.error("Exception occurred while processing GenerateEsnRequest {} :", e);
//            setGenerateEsnResponse(GenerateEsnEnum.FAIL, response, GenerateEsnEnum.FAIL.getResultMsg());
//            WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
//            workFlowResponseBean.setGenerateEsnResponseBody(response);
//            return workFlowResponseBean;
//        }
//        return (null != workflowResponse ? workflowResponse.getResponse() : null);
//    }
//
//    private boolean validateJwtToken(GenerateEsnRequest request) {
//        if (!verifyJwtToken(request)) {
//            LOGGER.error("JWT Validation failed returning response");
//            return false;
//        }
//        EXT_LOGGER.customInfo("JWT validated successfully");
//        return true;
//    }
//
//    private boolean verifyJwtToken(GenerateEsnRequest request) {
//        Map<String, String> jwtMap = new HashMap<>();
//        jwtMap.put(EXTERNAL_SERIAL_NUMBER, request.getBody().getExternalSerialNo());
//        return JWTWithHmacSHA256.verifyJsonWebToken(jwtMap, request.getHeader().getSignature());
//    }
//
//    private void enhanceWorkFlowRequestBean(WorkFlowRequestBean workFlowRequestBean) {
//        Map<String, String> channelInfo = workFlowRequestBean.getChannelInfo();
//        if (null == channelInfo) {
//            channelInfo = new HashMap<>();
//            workFlowRequestBean.setChannelInfo(channelInfo);
//        }
//    }
//
//    private GenerateEsnResponse prepareGenerateEsnResponse(WorkFlowResponseBean workFlowResponseBean,
//            GenerateEsnRequest generateEsnRequest) {
//        GenerateEsnResponse generateEsnResponse = new GenerateEsnResponse();
//        GenerateEsnResponseHeader header = new GenerateEsnResponseHeader();
//        GenerateEsnResponseBody body = new GenerateEsnResponseBody();
//        header.setResponseTimestamp(Long.valueOf(System.currentTimeMillis()));
//        header.setVersion("v1");
//        if (null == workFlowResponseBean.getGenerateEsnResponseBody()) {
//            setGenerateEsnResponse(GenerateEsnEnum.FAIL, body, GenerateEsnEnum.FAIL.getResultMsg());
//        } else {
//            body = workFlowResponseBean.getGenerateEsnResponseBody();
//            body.setOldExternalSerialNo(generateEsnRequest.getBody().getExternalSerialNo());
//            if (generateEsnRequest.getBody().isMandateFlow()) {
//                body.setMandateExternalSerialNo("PAYTMSUBS" + body.getNewExternalSerialNo());
//            }
//        }
//        generateEsnResponse.setHead(header);
//        generateEsnResponse.setBody(body);
//        LOGGER.info("Response returned for GenerateEsn is {} :", generateEsnResponse);
//        return generateEsnResponse;
//    }
//
//    private void setGenerateEsnResponse(GenerateEsnEnum resultEnum, GenerateEsnResponseBody body, String msg) {
//        body.setResultCode(resultEnum.getResultCode());
//        body.setResultCodeId(resultEnum.getResultCodeId());
//        body.setResultMsg(msg);
//    }
// }