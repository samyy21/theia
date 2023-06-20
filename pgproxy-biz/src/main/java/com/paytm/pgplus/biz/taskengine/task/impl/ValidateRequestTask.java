package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.PG2Util;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("validationTask")
public class ValidateRequestTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    @Autowired
    private PG2Util pg2Util;

    @Override
    public GenericCoreResponseBean<Boolean> doBizExecute(WorkFlowRequestBean input,
            WorkFlowTransactionBean transactionBean, WorkFlowResponseBean response) {
        GenericCoreResponseBean<Boolean> requestBeanValidationResult = workFlowHelper.beanValidation(input,
                transactionBean.getValidator());

        WorkFlowRequestBean requestBean = transactionBean.getWorkFlowBean();
        Routes route = pg2Util.getRouteForLpvRequest(transactionBean.getWorkFlowBean().isFullPg2TrafficEnabled(),
                requestBean.getPaytmMID());
        if (requestBean.getExtendInfo() == null)
            requestBean.setExtendInfo(new ExtendedInfoRequestBean());
        requestBean.getExtendInfo().setLpvRoute(route);

        // TO SUPPORT SSO BASED FLOW WITHOUT ORDER ID IN NATIVE FETCH PAY
        // OPTIONS
        if ((ERequestType.NATIVE_PAY.equals(input.getRequestType())
                || ERequestType.UNI_PAY.equals(input.getRequestType()) || (input.isSuperGwFpoApiHit() && ERequestType.NATIVE_SUBSCRIPTION_PAY
                .equals(input.getRequestType())))
                && requestBeanValidationResult.getResponseConstant() == ResponseConstants.INVALID_ORDER_ID) {
            return workFlowHelper.specificBeanValidation(input, transactionBean.getValidator());
        }
        return requestBeanValidationResult;
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.VALIDATE_REQUEST_TASK;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return true;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.VALIDATE_REQUEST_TIME, "10"));
    }

}
