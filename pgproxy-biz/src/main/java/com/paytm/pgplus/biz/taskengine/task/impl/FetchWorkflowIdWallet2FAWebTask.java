package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.exception.BaseException;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

@Service("fetchWorkflowIdWallet2FATask")
public class FetchWorkflowIdWallet2FAWebTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {
    @Override
    protected GenericCoreResponseBean<Boolean> doBizExecute(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean response) throws BaseException {
        GenericCoreResponseBean<Boolean> fetchWorkflowIdWallet2FAWeb = workFlowHelper
                .fetchWorkflowIdWallet2FAWeb(workFlowTransactionBean);
        return fetchWorkflowIdWallet2FAWeb;
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FETCH_WORKFLOW_ID_WALLET_2FA_WEB;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.parseInt(ConfigurationUtil.getProperty(BizConstant.FETCH_WORKFLOW_ID_WALLET_2FA_TIME, "100"));
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {

        return workFlowHelper.isWebTxn(input)
                && transBean != null
                && transBean.getTwoFAConfig() != null
                && transBean.getTwoFAConfig().isTwoFAEnabled()
                && !transBean.getTwoFAConfig().isPassCodeExist()
                && (input.isPostConvenience() || ((StringUtils.isNotBlank(input.getTxnAmount())) && (Integer
                        .parseInt(input.getTxnAmount()) >= Integer.parseInt(AmountUtils
                        .getTransactionAmountInPaise(transBean.getTwoFAConfig().getAmount())))));
    }
}
