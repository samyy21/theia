package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.constants.ResponseCodeConstant;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.offline.exceptions.PassCodeValidationException;
import com.paytm.pgplus.theia.services.ISeamlessDirectBankCardService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.services.InternalPaymentRetryService;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service(value = "seamlessDirectBankCardServiceImpl")
public class SeamlessDirectBankCardServiceImpl implements ISeamlessDirectBankCardService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeamlessDirectBankCardServiceImpl.class);

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    @Qualifier("nativeRetryPaymentFlowService")
    private IWorkFlow nativeRetryPaymentFlowService;

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    @Autowired
    @Qualifier("bizInternalPaymentRetry")
    private InternalPaymentRetryService internalPaymentRetryService;

    private static final String VIEW_BASE = "/WEB-INF/views/jsp/";

    @Override
    public void processNativeRequest(HttpServletRequest request, HttpServletResponse response,
            WorkFlowRequestBean flowRequestBean) throws ServletException, IOException {
        LOGGER.info("Direct Bank Request for Native Flow");
        String jspName;
        PaymentRequestBean requestData = getPaymentRequestBean(flowRequestBean);
        GenericCoreResponseBean<WorkFlowResponseBean> flowResponseBean = doPayment(flowRequestBean);

        if (StringUtils.isNotBlank(flowResponseBean.getInternalErrorCode())) {
            requestData.setInternalErrorCode(flowResponseBean.getInternalErrorCode());
        }

        if (!flowResponseBean.isSuccessfullyProcessed()
                && flowResponseBean.getResponseConstant() != null
                && (ERequestType.NATIVE.getType().equals(requestData.getRequestType())
                        || ERequestType.NATIVE_MF.getType().equals(requestData.getRequestType())
                        || ERequestType.NATIVE_MF.getType().equals(requestData.getSubRequestType())
                        || ERequestType.NATIVE_ST.getType().equals(requestData.getSubRequestType())
                        || ERequestType.NATIVE_ST.getType().equals(requestData.getRequestType()) || flowResponseBean
                        .getResponseConstant().isResponseToMerchant())) {
            LOGGER.info("Sending response to merchant , Biz Call Unsuccessfull due to... {}",
                    flowResponseBean.getFailureDescription());
            if (flowResponseBean.getRiskRejectUserMessage() != null) {
                requestData.setNativeRetryErrorMessage(flowResponseBean.getRiskRejectUserMessage());
            }
            String htmlPage = merchantResponseService.processMerchantFailResponse(requestData,
                    flowResponseBean.getResponseConstant());
            response.setContentType("text/html");
            response.getOutputStream().print(htmlPage);
            return;
        }

        WorkFlowResponseBean workFlowResponseBean = flowResponseBean.getResponse();

        if (workFlowResponseBean == null) {
            if (StringUtils.isNotBlank(flowResponseBean.getRiskRejectUserMessage())) {
                throw new NativeFlowException.ExceptionBuilder(flowResponseBean.getResponseConstant())
                        .isHTMLResponse(false).isRedirectEnhanceFlow(false).isRetryAllowed(true)
                        .setMsg(flowResponseBean.getRiskRejectUserMessage()).build();

            } else if (null != flowResponseBean && null != flowResponseBean.getResponseConstant()
                    && flowResponseBean.getResponseConstant().equals(ResponseConstants.INVALID_PAYMENT_DETAILS)) {
                com.paytm.pgplus.common.model.ResultInfo resultInfo = new com.paytm.pgplus.common.model.ResultInfo();
                resultInfo.setRedirect(false);
                resultInfo.setResultCode(ResponseCodeConstant.INVALID_PAYMENT_DETAILS);
                resultInfo.setResultMsg(flowResponseBean.getFailureDescription());
                throw new PassCodeValidationException(resultInfo);
            }

            throw new NativeFlowException.ExceptionBuilder(flowResponseBean.getResponseConstant())
                    .isHTMLResponse(false).isRedirectEnhanceFlow(true).build();
        }
        // response setting will be different in Native via
        // requestDispacter
        String responsePage = theiaResponseGenerator.generateResponseForSeamless(workFlowResponseBean, requestData);
        response.setContentType("text/html");
        response.getOutputStream().print(responsePage);
        return;

    }

    @Override
    public void processNativeEnhanceRequest(HttpServletRequest request, HttpServletResponse response,
            WorkFlowRequestBean flowRequestBean) throws IOException {

        LOGGER.info("Direct Bank Request for Native Enhance Flow");
        PaymentRequestBean requestData = getPaymentRequestBean(flowRequestBean);
        GenericCoreResponseBean<WorkFlowResponseBean> flowResponseBean = doPayment(flowRequestBean);

        if (!flowResponseBean.isSuccessfullyProcessed() && flowResponseBean.getResponseConstant() != null
                && flowResponseBean.getResponseConstant().isResponseToMerchant()
                && !flowResponseBean.getResponseConstant().equals(ResponseConstants.INVALID_PAYMENT_DETAILS)) {
            LOGGER.info("Sending response to merchant , Biz Call Unsuccessfull due to... {}",
                    flowResponseBean.getFailureDescription());
            if (flowResponseBean.getRiskRejectUserMessage() != null) {
                requestData.setNativeRetryErrorMessage(flowResponseBean.getRiskRejectUserMessage());
            }
            throw new TheiaServiceException("Biz Call Unsuccessfull");
        }

        WorkFlowResponseBean workFlowResponseBean = flowResponseBean.getResponse();
        if (workFlowResponseBean == null) {
            if (StringUtils.isNotBlank(flowResponseBean.getRiskRejectUserMessage())) {
                throw new NativeFlowException.ExceptionBuilder(flowResponseBean.getResponseConstant())
                        .isHTMLResponse(false).isRedirectEnhanceFlow(false).isRetryAllowed(true)
                        .setMsg(flowResponseBean.getRiskRejectUserMessage()).build();

            } else if (null != flowResponseBean && null != flowResponseBean.getResponseConstant()
                    && flowResponseBean.getResponseConstant().equals(ResponseConstants.INVALID_PAYMENT_DETAILS)) {
                com.paytm.pgplus.common.model.ResultInfo resultInfo = new com.paytm.pgplus.common.model.ResultInfo();
                resultInfo.setRedirect(false);
                resultInfo.setResultCode(ResponseCodeConstant.INVALID_PAYMENT_DETAILS);
                resultInfo.setResultMsg(flowResponseBean.getFailureDescription());
                throw new PassCodeValidationException(resultInfo);
            }

            throw new NativeFlowException.ExceptionBuilder(flowResponseBean.getResponseConstant())
                    .isHTMLResponse(false).isRedirectEnhanceFlow(true).build();
        }
        String responsePage = theiaResponseGenerator.getBankPage(workFlowResponseBean, requestData);
        response.setContentType("text/html");
        response.getOutputStream().print(responsePage);
    }

    private PaymentRequestBean getPaymentRequestBean(WorkFlowRequestBean flowRequestBean) {
        PaymentRequestBean requestData = new PaymentRequestBean();
        requestData.setRequestType(flowRequestBean.getRequestType().toString());
        requestData.setMid(flowRequestBean.getPaytmMID());
        requestData.setOrderId(flowRequestBean.getOrderID());
        requestData.setCustId(flowRequestBean.getCustID());
        requestData.setTxnAmount(flowRequestBean.getTxnAmount());
        requestData.setTxnToken(flowRequestBean.getTxnToken());
        requestData.setNativeRetryErrorMessage("Something went wrong");
        return requestData;
    }

    public GenericCoreResponseBean<WorkFlowResponseBean> doPayment(WorkFlowRequestBean flowRequestBean) {
        // For DirectBankCard Flows : We need to allow internal retry flow.
        // Setting this flag specifically to handle this use case.
        flowRequestBean.setAllowInternalRetryOnDirectBankCardFlow(true);
        GenericCoreResponseBean<WorkFlowResponseBean> flowResponseBean = (GenericCoreResponseBean<WorkFlowResponseBean>) internalPaymentRetryService
                .retryBankFormFetchWithPayment(flowRequestBean, nativeRetryPaymentFlowService);
        // Check for Internal Retry
        if (flowResponseBean == null) {
            throw new TheiaServiceException("WorkFlowResponseBean is null");
        }
        if (internalPaymentRetryService.isInternalPaymentRetryRequired(flowResponseBean)) {
            GenericCoreResponseBean<WorkFlowResponseBean> retryFlowResponseBean = (GenericCoreResponseBean<WorkFlowResponseBean>) internalPaymentRetryService
                    .retryBankFormFetchWithPayment(flowRequestBean, nativeRetryPaymentFlowService);
            if (retryFlowResponseBean != null) {
                flowResponseBean = retryFlowResponseBean;
            }
        }
        return flowResponseBean;
    }
}
