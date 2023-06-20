/**
 * 
 */
package com.paytm.pgplus.theia.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.services.IJsonResponsePaymentService;
import com.paytm.pgplus.theia.utils.ChecksumService;

/**
 * @createdOn 27-Mar-2016
 * @author kesari
 */
@Service("createInvoicePaymentService")
public class CreateInvoicePaymentServiceImpl implements IJsonResponsePaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateInvoicePaymentServiceImpl.class);

    @Autowired
    @Qualifier("invoicePreFlow")
    private IWorkFlow invoicePreFlow;

    @Autowired
    @Qualifier("bizRequestResponseMapper")
    private IBizRequestResponseMapper bizRequestResponseMapper;

    @Autowired
    @Qualifier("bizService")
    protected IBizService bizService;

    @Autowired
    protected ChecksumService checksumService;

    @Override
    public WorkFlowResponseBean processPaymentRequest(PaymentRequestBean requestData) throws TheiaServiceException {
        WorkFlowRequestBean workFlowRequestBean = null;
        try {
            workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(requestData);
        } catch (TheiaDataMappingException e) {
            throw new TheiaServiceException(e);
        }
        LOGGER.debug("WorkFlowRequestBean :{}", workFlowRequestBean);
        if (workFlowRequestBean == null) {
            throw new TheiaServiceException("WorkFlowRequestBean is null");
        }
        return processBizWorkFlow(workFlowRequestBean).getResponse();
    }

    @Override
    public ValidationResults validatePaymentRequest(PaymentRequestBean requestData)
            throws PaymentRequestValidationException {
        if (checksumService.validateChecksum(requestData)) {
            return ValidationResults.VALIDATION_SUCCESS;
        }
        LOGGER.error("CheckSum Validation Failure");
        return ValidationResults.CHECKSUM_VALIDATION_FAILURE;
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(WorkFlowRequestBean workFlowRequestBean) {
        return bizService.processWorkFlow(workFlowRequestBean, invoicePreFlow);
    }

    @Override
    public String getResponseWithChecksumForJsonResponse(String response, String clientId) {
        throw new TheiaServiceException("This operation is not implemented yet");
    }

}
