package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service("fetchMgvBalanceTask")
public class FetchMgvBalanceTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {
    @Override
    protected GenericCoreResponseBean<?> doBizExecute(WorkFlowRequestBean input, WorkFlowTransactionBean transBean,
            WorkFlowResponseBean response) {
        return workFlowHelper.fetchAndSetMgvBalance(transBean);
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FETCH_MGV;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.FETCH_MGV_BALANCE, "90"));
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        // Executing only when paymethod is configured
        boolean payMethodConfigured = false;
        if (StringUtils.isNotBlank(input.getToken())) {
            payMethodConfigured = workFlowHelper.isPayMethodConfiguredOnMerchant(
                    transBean.getMerchantLiteViewConsult(), EPayMethod.GIFT_VOUCHER);
        }
        return payMethodConfigured;
    }
}
