package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowRequestCreationHelper;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("riskPolicyConsultForAddMoney")
public class RiskPolicyConsultForAddMoneyTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {
    @Override
    protected GenericCoreResponseBean<?> doBizExecute(WorkFlowRequestBean input, WorkFlowTransactionBean transBean,
            WorkFlowResponseBean response) {
        return workFlowHelper.checkForRiskConsultForAddMoney(input, transBean, response);
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.RISK_POLICY_CONSULT;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.RISK_POLICY_CONSULT_TIME, "90"));
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        return (input.getPaytmMID().equalsIgnoreCase(
                com.paytm.pgplus.common.config.ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID))
                && !input.isGvFlag() && !input.isTransitWallet());
    }
}
