/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.workflow.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.model.request.BizCancelOrderResponse;
import com.paytm.pgplus.biz.core.model.request.BizCreateOrderResponse;
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
@Service(value = "paymentErrorFlow")
public class PaymentErrorFlow implements IWorkFlow {

    public static final Logger LOGGER = LoggerFactory.getLogger(PaymentErrorFlow.class);

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("paymentErrorValidator")
    IValidator validatorService;

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

        final GenericCoreResponseBean<BizCreateOrderResponse> createOrder = workFlowHelper
                .createOrder(workFlowTransBean);

        LOGGER.debug("Response from Create Order is :: {}", createOrder);
        if (!createOrder.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(createOrder.getFailureMessage(), createOrder.getResponseConstant());
        }
        workFlowTransBean.setTransID(createOrder.getResponse().getTransId());
        workFlowTransBean.getWorkFlowBean().setTransID(workFlowTransBean.getTransID());

        final GenericCoreResponseBean<BizCancelOrderResponse> cancelOrder = workFlowHelper
                .closeOrder(workFlowTransBean);
        if (!cancelOrder.isSuccessfullyProcessed()) {
            LOGGER.error("Cancel order failed due to :: {}", cancelOrder.getFailureMessage());
        }
        LOGGER.debug("Response from close order is : {}", cancelOrder);
        final WorkFlowResponseBean responseBean = new WorkFlowResponseBean();
        responseBean.setTransID(workFlowTransBean.getTransID());

        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "PaymentErrorFlow", responseBean.getTransID());
        return new GenericCoreResponseBean<>(responseBean);
    }
}