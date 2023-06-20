/**
 * 
 */
package com.paytm.pgplus.biz.workflow.service.impl;

import com.paytm.pgplus.biz.workflow.model.BizResultInfo;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.core.model.request.BizCancelOrderResponse;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.ETransType;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author vaishakhnair
 *
 */
@Service("cancelTransactionFlow")
public class CancelTransactionFlow implements IWorkFlow {
    private static final Logger LOGGER = LoggerFactory.getLogger(CancelTransactionFlow.class);

    @Autowired
    @Qualifier("cancelTransactionValidator")
    private IValidator validatorService;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> process(WorkFlowRequestBean flowRequestBean) {

        final GenericCoreResponseBean<Boolean> requestBeanValidationResult = validatorService.validate(flowRequestBean);
        if (!requestBeanValidationResult.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(requestBeanValidationResult.getFailureMessage(),
                    requestBeanValidationResult.getResponseConstant());
        }

        WorkFlowTransactionBean workFlowTransactionBean = new WorkFlowTransactionBean();
        workFlowTransactionBean.setWorkFlowBean(flowRequestBean);
        workFlowTransactionBean.setTransID(flowRequestBean.getTransID());
        workFlowTransactionBean.setWorkFlowBean(flowRequestBean);

        GenericCoreResponseBean<BizCancelOrderResponse> cancelOrderResponse = null;
        if ((ETransType.TOP_UP).equals(flowRequestBean.getTransType())) {
            cancelOrderResponse = workFlowHelper.closeFundOrder(workFlowTransactionBean);
        } else {
            cancelOrderResponse = workFlowHelper.closeOrder(workFlowTransactionBean);
        }

        if (!cancelOrderResponse.isSuccessfullyProcessed()) {
            return new GenericCoreResponseBean<>(cancelOrderResponse.getFailureMessage(),
                    cancelOrderResponse.getResponseConstant());
        }
        if (!cancelOrderResponse.getResponse().isSuccessfullyProcessed()) {
            if ((ETransType.TOP_UP).equals(flowRequestBean.getTransType())) {
                BizResultInfo bizResultInfo = cancelOrderResponse.getResponse().getBizResultInfo();
                if (bizResultInfo != null
                        && ResponseConstants.fetchResponseConstantByCode(bizResultInfo.getResultCodeId()) != null) {
                    LOGGER.info("sending {} for fundCloseOrder", cancelOrderResponse.getResponse().getDescription());
                    return new GenericCoreResponseBean<>(cancelOrderResponse.getResponse().getDescription(),
                            ResponseConstants.fetchResponseConstantByCode(bizResultInfo.getResultCodeId()));
                }
                return new GenericCoreResponseBean<>(cancelOrderResponse.getResponse().getDescription(),
                        cancelOrderResponse.getResponseConstant());
            }
            return new GenericCoreResponseBean<>(cancelOrderResponse.getResponse().getDescription(),
                    cancelOrderResponse.getResponseConstant());
        }
        WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
        workFlowResponseBean.setTransID(workFlowTransactionBean.getTransID());

        LOGGER.info("Returning Response Bean From {}, trans Id : {} ", "CancelTransactionFlow",
                workFlowResponseBean.getTransID());
        return new GenericCoreResponseBean<>(workFlowResponseBean);
    }

}
