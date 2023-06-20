package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.core.model.ppb.AccountBalanceResponse;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service("paymentBankBalanceTask")
public class PaymentBankBalanceTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    @Override
    public GenericCoreResponseBean<AccountBalanceResponse> doBizExecute(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean workFlowResponseBean) {
        GenericCoreResponseBean<AccountBalanceResponse> paytmBankBalanceInfo = workFlowHelper
                .fetchAccountBalance(workFlowTransactionBean);
        if (paytmBankBalanceInfo.isSuccessfullyProcessed() && paytmBankBalanceInfo.getResponse() != null) {
            workFlowTransactionBean.setAccountBalanceResponse(paytmBankBalanceInfo.getResponse());
        }
        return paytmBankBalanceInfo;
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.PAYMENTS_BANK_BALANCE;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.PAYMENTS_BANK_BALANCE_TIME, "500"));
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        return StringUtils.isNotBlank(input.getToken()) && workFlowHelper.paymentsBankAllowed(transBean);
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setAccountBalanceResponse(transBean.getAccountBalanceResponse());
    }
}
