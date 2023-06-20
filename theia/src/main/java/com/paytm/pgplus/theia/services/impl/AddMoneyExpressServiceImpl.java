package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.workflow.service.util.FailureLogUtil;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.theia.utils.AddMoneyToGvConsentUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.services.AbstractPaymentService;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

@Service("addMoneyExpressService")
public class AddMoneyExpressServiceImpl extends AbstractPaymentService {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(AddMoneyExpressServiceImpl.class);

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    @Qualifier("addMoneyExpressFlow")
    private IWorkFlow addMoneyExpressFlow;

    @Autowired
    private AddMoneyToGvConsentUtil addMoneyToGvConsentUtil;

    @Autowired
    private TransactionCacheUtils transactionCacheUtils;

    @Autowired
    @Qualifier("failureLogUtil")
    private FailureLogUtil failureLogUtil;

    @Override
    public PageDetailsResponse processPaymentRequest(PaymentRequestBean requestData, Model responseModel)
            throws TheiaServiceException {

        LOGGER.debug("requestData :{}, responseModel :{}", requestData, responseModel);
        LOGGER.info("Processing payment request for mid :{}, order id :{}, request type :{}", requestData.getMid(),
                requestData.getOrderId(), requestData.getRequestType());

        WorkFlowRequestBean workFlowRequestBean = null;

        try {
            workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(requestData);
        } catch (TheiaDataMappingException exception) {
            failureLogUtil.setFailureMsgForDwhPush(null, exception.getMessage(), null, true);
            LOGGER.error("SYSTEM_ERROR " + exception);
            return theiaResponseGenerator.getPageDetailsResponse(requestData, exception.getResponseConstant());
        }

        LOGGER.debug("WorkFlowRequestBean : {}", workFlowRequestBean);
        if (workFlowRequestBean == null) {
            failureLogUtil.setFailureMsgForDwhPush(null, BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL, null,
                    true);
            throw new TheiaServiceException(BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL);
        }

        workFlowRequestBean.setPaymentRequestBean(requestData);

        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = processBizWorkFlow(workFlowRequestBean);

        if (bizResponseBean.getResponseConstant() != null
                && ResponseConstants.GV_CONSENT_REQUIRED.equals(bizResponseBean.getResponseConstant())) {
            PageDetailsResponse pageDetailsResponse = addMoneyToGvConsentUtil.showConsentPageForRedirection(
                    requestData.getMid(), requestData.getOrderId(), true);
            theiaSessionDataService.setRedirectPageInSession(requestData.getRequest(),
                    pageDetailsResponse.getHtmlPage());
            pageDetailsResponse.setHtmlPage(null);
            pageDetailsResponse.setSuccessfullyProcessed(true);
            return pageDetailsResponse;
        }

        if (StringUtils.isNotBlank(bizResponseBean.getInternalErrorCode())) {
            requestData.setInternalErrorCode(bizResponseBean.getInternalErrorCode());
        }

        if (!bizResponseBean.isSuccessfullyProcessed() && bizResponseBean.getResponseConstant() != null
                && bizResponseBean.getResponseConstant().isResponseToMerchant()) {
            LOGGER.info("Sending response to merchant , Biz call is unsuccessful because : {}",
                    bizResponseBean.getFailureMessage());
            failureLogUtil.setFailureMsgForDwhPush(bizResponseBean.getResponseConstant().getCode(),
                    bizResponseBean.getFailureDescription(), null, true);
            return theiaResponseGenerator.getPageDetailsResponse(requestData, bizResponseBean.getResponseConstant());
        }

        if (ResponseConstants.KYC_VALIDATION_REQUIRED == bizResponseBean.getResponseConstant()) {
            PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
            pageDetailsResponse.setJspName("common/addMoneyExpressKycUser");
            pageDetailsResponse.setSuccessfullyProcessed(false);
            pageDetailsResponse.setKycPageRequired(true);
            return pageDetailsResponse;
        }

        WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();

        if (workFlowResponseBean == null) {
            LOGGER.error("SYSTEM_ERROR, Reason : {}", bizResponseBean.getFailureDescription());
            if (StringUtils.isNotBlank(bizResponseBean.getRiskRejectUserMessage())) {
                requestData.setNativeRetryErrorMessage(bizResponseBean.getRiskRejectUserMessage());
                LOGGER.info("WorkFlowResponseBean is null, Reason : {}", bizResponseBean.getRiskRejectUserMessage());
                if (ResponseConstants.RISK_REJECT == bizResponseBean.getResponseConstant()) {
                    return theiaResponseGenerator.getPageDetailsResponse(requestData, ResponseConstants.RISK_REJECT);
                }
                return theiaResponseGenerator.getPageDetailsResponse(requestData,
                        ResponseConstants.MERCHANT_RISK_REJECT);

            } else {
                failureLogUtil.setFailureMsgForDwhPush(
                        bizResponseBean.getResponseConstant() != null ? bizResponseBean.getResponseConstant().getCode()
                                : null,
                        BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL_REASON_IS
                                + bizResponseBean.getFailureDescription(), null, true);
                throw new TheiaServiceException(BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL_REASON_IS
                        + bizResponseBean.getFailureDescription());
            }
        }

        LOGGER.debug("WorkFlowResponseBean : {} ", workFlowResponseBean);
        String responsePage = theiaResponseGenerator.generateResponseForSeamless(workFlowResponseBean, requestData);
        theiaSessionDataService.setRedirectPageInSession(requestData.getRequest(), responsePage);

        transactionCacheUtils.putTransInfoInCache(workFlowResponseBean.getTransID(), workFlowRequestBean.getPaytmMID(),
                workFlowRequestBean.getOrderID(), true);

        return new PageDetailsResponse(true);
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(WorkFlowRequestBean workFlowRequestBean) {

        GenericCoreResponseBean<WorkFlowResponseBean> workFlowResponseBean = null;
        workFlowResponseBean = bizService.processWorkFlow(workFlowRequestBean, addMoneyExpressFlow);
        return workFlowResponseBean;
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.CHECKSUM_VALIDATION)
    @Override
    public ValidationResults validatePaymentRequest(PaymentRequestBean requestData)
            throws PaymentRequestValidationException {

        if (requestData.isGvConsentFlow()) {
            return ValidationResults.VALIDATION_SUCCESS;
        }

        final boolean validateChecksum = validateChecksum(requestData);

        if (!validateChecksum) {
            return ValidationResults.CHECKSUM_VALIDATION_FAILURE;
        }

        return ValidationResults.VALIDATION_SUCCESS;
    }
}
