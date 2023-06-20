package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.core.payment.utils.LitePayViewEmiUtil;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("fetchWalletBalanceTask")
public class FetchWalletBalanceTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    @Autowired
    private LitePayViewEmiUtil emiUtil;

    @Override
    public GenericCoreResponseBean<Boolean> doBizExecute(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean workFlowResponseBean) {
        GenericCoreResponseBean<Boolean> fetchWalletBalance = workFlowHelper
                .fetchWalletBalanceResponse(workFlowTransactionBean);

        // after the wallet balance is fetched we need calculate emi rates.
        if (StringUtils.isNotBlank(workFlowRequestBean.getTxnAmount())) {
            emiUtil.setEmiInfoForLiteConsultPayview(workFlowTransactionBean, workFlowRequestBean.getTxnAmount());
        }

        // emi hack should not be applicable for native-subscription
        if (ERequestType.NATIVE_SUBSCRIPTION_PAY != workFlowRequestBean.getRequestType()) {

            // setting emi info in litePayview consult as well
            workFlowTransactionBean.getMerchantLiteViewConsult().setPayMethodViews(
                    workFlowTransactionBean.getMerchantViewConsult().getPayMethodViews());
        }

        // setting wallet account status to check if account is inactive
        workFlowResponseBean.setWalletInactive(workFlowTransactionBean.isWalletInactive());
        workFlowResponseBean.setWalletNewUser(workFlowTransactionBean.isWalletNewUser());

        workFlowResponseBean.setOfflineFlow(workFlowRequestBean.isOfflineFlow());

        return fetchWalletBalance;
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FETCH_WALLET;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.FETCH_WALLET_BALANCE, "90"));
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        // not logged in flow doesn't require any ouath validation
        return (StringUtils.isNotBlank(input.getToken()) || input.getUserDetailsBiz() != null);
    }
}
