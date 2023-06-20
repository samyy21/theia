package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service("freshSubscriptionContractTask")
public class FreshSubscriptionContractTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    @Override
    public GenericCoreResponseBean<SubscriptionResponse> doBizExecute(WorkFlowRequestBean input,
            WorkFlowTransactionBean workFlowTransBean, WorkFlowResponseBean response) {
        final SubscriptionResponse freshSubscriptionResponse = workFlowHelper
                .processFreshSubscriptionContrat(workFlowTransBean);

        workFlowTransBean.setSubscriptionServiceResponse(freshSubscriptionResponse);
        workFlowTransBean.getWorkFlowBean().getExtendInfo()
                .setSubscriptionId(freshSubscriptionResponse.getSubscriptionId());

        return new GenericCoreResponseBean<>(freshSubscriptionResponse);
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FRESH_SUBSCRIPTION_CONTRACT_TASK;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return true;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.FRESH_SUBSCRIPTION_CONTRACT_TIME, "50"));
        // dummy
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setSubsType(transBean.getWorkFlowBean().getSubsTypes());
        response.setSubscriptionID(transBean.getSubscriptionServiceResponse().getSubscriptionId());
    }
}