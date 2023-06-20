/**
 *
 */
package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.workflow.service.util.FailureLogUtil;
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
import com.paytm.pgplus.theia.services.helper.RetryServiceHelper;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

/**
 * @createdOn 27-Mar-2016
 * @author namanjain
 */
@Service("addMoneyPaymentService")
public class AddMoneyPaymentServiceImpl extends AbstractPaymentService {

    private static final long serialVersionUID = -3059884766173716078L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AddMoneyPaymentServiceImpl.class);

    @Autowired
    @Qualifier("addMoneyFlow")
    private IWorkFlow addMoneyFlow;

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    @Autowired
    private RetryServiceHelper retryServiceHelper;

    @Autowired
    @Qualifier("failureLogUtil")
    private FailureLogUtil failureLogUtil;

    @Override
    public PageDetailsResponse processPaymentRequest(PaymentRequestBean requestData, Model responseModel)
            throws TheiaServiceException {
        LOGGER.info("Processing payment request for add money flow, order id : {}", requestData.getOrderId());

        WorkFlowRequestBean workFlowRequestBean = null;
        try {
            workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(requestData);
        } catch (TheiaDataMappingException e) {
            failureLogUtil.setFailureMsgForDwhPush(null, e.getMessage(), null, true);
            LOGGER.error("SYSTEM_ERROR ", e);
            String htmlPage = merchantResponseService.processMerchantFailResponse(requestData, e.getResponseConstant());

            PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
            pageDetailsResponse.setSuccessfullyProcessed(false);
            pageDetailsResponse.setHtmlPage(htmlPage);
            return pageDetailsResponse;
        }

        LOGGER.debug("WorkFlowRequestBean : {}", workFlowRequestBean);
        if (workFlowRequestBean == null) {
            failureLogUtil.setFailureMsgForDwhPush(null, BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL, null,
                    true);
            throw new TheiaServiceException(BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL);
        }

        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = processBizWorkFlow(workFlowRequestBean);

        /*
         * Check if biz Response Failed and do we have to send response to
         * merchant
         */
        if (StringUtils.isNotBlank(bizResponseBean.getInternalErrorCode())) {
            requestData.setInternalErrorCode(bizResponseBean.getInternalErrorCode());
        }

        if (!bizResponseBean.isSuccessfullyProcessed() && bizResponseBean.getResponseConstant() != null
                && bizResponseBean.getResponseConstant().isResponseToMerchant()) {
            failureLogUtil.setFailureMsgForDwhPush(bizResponseBean.getResponseConstant().getCode(),
                    bizResponseBean.getFailureDescription(), null, true);
            LOGGER.info("Sending response to merchant , Biz Call Unsuccessfull due to... {}",
                    bizResponseBean.getFailureDescription());
            String htmlPage = merchantResponseService.processMerchantFailResponse(requestData,
                    bizResponseBean.getResponseConstant());

            PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
            pageDetailsResponse.setSuccessfullyProcessed(false);
            pageDetailsResponse.setHtmlPage(htmlPage);
            return pageDetailsResponse;
        }

        WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();
        LOGGER.debug("WorkFlowResponseBean : {}", workFlowResponseBean);

        if (workFlowResponseBean == null) {
            failureLogUtil.setFailureMsgForDwhPush(
                    bizResponseBean.getResponseConstant() != null ? bizResponseBean.getResponseConstant().getCode()
                            : null,
                    BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL_REASON_IS
                            + bizResponseBean.getFailureDescription(), null, true);
            throw new TheiaServiceException(BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL_REASON_IS
                    + bizResponseBean.getFailureDescription());
        }

        bizRequestResponseMapper.mapWorkFlowResponseToSession(requestData, workFlowResponseBean);
        retryServiceHelper.setRequestDataInCache(workFlowResponseBean.getTransID(), requestData);
        return new PageDetailsResponse(true);
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.REQUEST_VALIDATION)
    @Override
    public ValidationResults validatePaymentRequest(PaymentRequestBean requestData)
            throws PaymentRequestValidationException {
        final boolean validateChecksum = validateChecksum(requestData);
        LOGGER.debug("Checksum Valdation Result is : {}", validateChecksum);
        if (!validateChecksum) {
            return ValidationResults.CHECKSUM_VALIDATION_FAILURE;
        }
        return ValidationResults.VALIDATION_SUCCESS;
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(WorkFlowRequestBean workFlowRequestBean) {
        GenericCoreResponseBean<WorkFlowResponseBean> workFlowResponseBean = null;
        workFlowResponseBean = bizService.processWorkFlow(workFlowRequestBean, addMoneyFlow);
        return workFlowResponseBean;
    }
}