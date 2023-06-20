/**
 *
 */
package com.paytm.pgplus.biz.workflow.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.model.request.BizCreateOrderResponse;
import com.paytm.pgplus.biz.core.order.service.IOrderService;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.InvoiceResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowRequestCreationHelper;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author namanjain
 */
@Service("invoicePreFlow")
public class InvoicePaymentPreFlow implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(InvoicePaymentPreFlow.class);

    @Autowired
    WorkFlowRequestCreationHelper workFlowCommonHelper;

    @Autowired
    IOrderService orderService;

    @Autowired
    @Qualifier("invoicePaymentPreValidator")
    IValidator validatorService;

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

        final WorkFlowTransactionBean workFlowTransBean = new WorkFlowTransactionBean();
        workFlowTransBean.setWorkFlowBean(flowRequestBean);

        // CREATE ORDER
        final GenericCoreResponseBean<BizCreateOrderResponse> createOrderResponse = workFlowHelper
                .createOrder(workFlowTransBean);
        if (!createOrderResponse.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(createOrderResponse.getFailureMessage(),
                    createOrderResponse.getResponseConstant());
        }
        workFlowTransBean.setTransID(createOrderResponse.getResponse().getTransId());

        final String invoiceLink = getInvoiceLink(workFlowTransBean);
        workFlowTransBean.setInvoiceLink(invoiceLink);

        // Generate InvoiceBean
        InvoiceResponseBean invoiceResponseBean = new InvoiceResponseBean(workFlowTransBean.getTransID(),
                workFlowTransBean.getInvoiceLink());
        workFlowTransBean.setInvoiceResponseBean(invoiceResponseBean);

        final WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        workFlowResponseBean.setInvoiceResponseBean(workFlowTransBean.getInvoiceResponseBean());

        LOGGER.info("Returning Response Bean From {}, Invoice Link : {} ", "InvoicePaymentPreFlow", invoiceLink);
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

    private String getInvoiceLink(WorkFlowTransactionBean workFlowTransBean) {
        StringBuilder invoiceLink = new StringBuilder(ConfigurationUtil.getProperty(BizConstant.CONTEXT_PATH_KEY));
        invoiceLink.append("?REQUEST_TYPE=").append(workFlowTransBean.getWorkFlowBean().getRequestType().getType())
                .append("&MID=").append(workFlowTransBean.getWorkFlowBean().getPaytmMID()).append("&ORDER_ID=")
                .append(workFlowTransBean.getWorkFlowBean().getOrderID());
        return invoiceLink.toString();
    }

}
