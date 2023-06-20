package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service("fetchVPATask")
public class FetchVPATask extends AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    @Override
    public GenericCoreResponseBean<UserProfileSarvatra> doBizExecute(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowTransactionBean workFlowTransBean, WorkFlowResponseBean responseBean) {
        GenericCoreResponseBean<UserProfileSarvatra> sarvatraResponse = workFlowHelper
                .fetchUserProfileFromSarvatra(workFlowTransBean);
        if (sarvatraResponse.isSuccessfullyProcessed() && null != sarvatraResponse.getResponse()) {
            workFlowTransBean.setSarvatraUserProfile(sarvatraResponse.getResponse());
            workFlowTransBean.setSarvatraVpa(workFlowHelper.getSarvatraVPAList(sarvatraResponse.getResponse()
                    .getResponse()));
        }

        return sarvatraResponse;
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FETCH_VPA;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.FETCH_VPA_TIME, "500"));

    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        return StringUtils.isNotBlank(input.getToken()) && workFlowHelper.checkUPIPUSHEnabled(transBean);
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setSarvatraUserProfile(transBean.getSarvatraUserProfile());
        response.setSarvatraVpa(transBean.getSarvatraVpa());
        response.setMerchantUpiPushEnabled(transBean.isMerchantUpiPushEnabled());
        response.setMerchantUpiPushExpressEnabled(transBean.isMerchantUpiPushExpressEnabled());
        response.setAddUpiPushEnabled(transBean.isAddUpiPushEnabled());
        response.setAddUpiPushExpressEnabled(transBean.isAddUpiPushExpressEnabled());
    }
}
