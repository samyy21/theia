/**
 * 
 */
package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
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
import com.paytm.pgplus.theia.constants.TheiaConstant.RetryConstants;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.services.AbstractPaymentService;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

/**
 * @createdOn 27-Mar-2016
 * @author kesari
 */
@Service("invoicePaymentService")
public class InvoicePaymentServiceImpl extends AbstractPaymentService {

    private static final long serialVersionUID = 8813265087064432325L;
    private static final Logger LOGGER = LoggerFactory.getLogger(InvoicePaymentServiceImpl.class);

    @Autowired
    @Qualifier("invoicePostFlow")
    private IWorkFlow invoicePostFlow;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Override
    public PageDetailsResponse processPaymentRequest(PaymentRequestBean paymentRequestBean, Model responseModel)
            throws TheiaServiceException {
        LOGGER.debug("Processing payment request for invoice flow, order id : {}", paymentRequestBean.getOrderId());

        WorkFlowRequestBean workFlowRequestBean = null;
        try {
            workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(paymentRequestBean);
        } catch (TheiaDataMappingException e) {
            throw new TheiaServiceException("SYSTEM_ERROR : {}", e);
        }

        if (workFlowRequestBean == null) {
            throw new TheiaServiceException("WorkFlowRequestBean is null");
        }

        LOGGER.debug("WorkFlowRequestBean :{}", workFlowRequestBean);

        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = processBizWorkFlow(workFlowRequestBean);

        /*
         * Check if biz Response Failed and do we have to send response to
         * merchant
         */
        if (!bizResponseBean.isSuccessfullyProcessed() && bizResponseBean.getResponseConstant() != null
                && bizResponseBean.getResponseConstant().isResponseToMerchant()) {
            LOGGER.warn("Sending response to merchant, Biz Call Unsuccessfull due to : {}",
                    bizResponseBean.getFailureDescription());
        }

        WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();

        if (workFlowResponseBean == null) {
            throw new TheiaServiceException("WorkFlowResponseBean is null, Reason : "
                    + bizResponseBean.getFailureDescription());
        }

        LOGGER.debug("WorkFlowResponseBean : {}", workFlowResponseBean);
        bizRequestResponseMapper.mapWorkFlowResponseToSession(paymentRequestBean, workFlowResponseBean);

        // Setting RequestData in cache for Retry scenario
        setRequestDataInCache(workFlowResponseBean.getTransID(), paymentRequestBean);

        return new PageDetailsResponse(true);
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.REQUEST_VALIDATION)
    @Override
    public ValidationResults validatePaymentRequest(PaymentRequestBean requestData)
            throws PaymentRequestValidationException {
        final boolean validateChecksum = validateChecksum(requestData);
        if (!validateChecksum) {
            return ValidationResults.CHECKSUM_VALIDATION_FAILURE;
        }
        return ValidationResults.VALIDATION_SUCCESS;
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(WorkFlowRequestBean workFlowRequestBean) {
        return bizService.processWorkFlow(workFlowRequestBean, invoicePostFlow);
    }

    private void setRequestDataInCache(String transId, PaymentRequestBean requestData) {
        StringBuilder sb = new StringBuilder();
        sb.append(RetryConstants.RETRY_PAYMENT_).append(transId);
        theiaTransactionalRedisUtil.set(sb.toString(), requestData, 1 * 60 * 60);
    }
}