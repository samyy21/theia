/**
 * 
 */
package com.paytm.pgplus.biz.workflow.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author namanjain
 *
 */
@Service("invoiceRetryFlow")
public class InvoicePaymentRetryFlow implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(InvoicePaymentRetryFlow.class);

    @Autowired
    @Qualifier("invoicePaymentRetryValidator")
    private IValidator validatorService;

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(final WorkFlowRequestBean flowRequestBean) {

        // Request Bean Validation
        final GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.beanValidation(
                flowRequestBean, validatorService);
        if (!requestBeanValidationResult.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }

        WorkFlowTransactionBean flowTransBean = new WorkFlowTransactionBean();
        flowTransBean.setWorkFlowBean(flowRequestBean);

        // Fetch TransID
        flowTransBean.setTransID(flowRequestBean.getTransID());

        final GenericCoreResponseBean<ConsultPayViewResponseBizBean> consultViewResponseBean = workFlowHelper
                .consultPayView(flowTransBean);
        if (!consultViewResponseBean.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(consultViewResponseBean.getFailureMessage(),
                    consultViewResponseBean.getResponseConstant());
        }
        flowTransBean.setMerchantViewConsult(consultViewResponseBean.getResponse());
        final WorkFlowResponseBean responseBean = new WorkFlowResponseBean();
        responseBean.setTransID(flowTransBean.getTransID());
        responseBean.setMerchnatViewResponse(flowTransBean.getMerchantViewConsult());

        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "InvoicePaymentRetryFlow",
                responseBean.getTransID());
        return new GenericCoreResponseBean<>(responseBean);
    }
}
