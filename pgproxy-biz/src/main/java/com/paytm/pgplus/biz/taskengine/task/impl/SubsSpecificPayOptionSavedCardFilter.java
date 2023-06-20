package com.paytm.pgplus.biz.taskengine.task.impl;

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

@Service("subsSpecificPayOptionSavedCardFilter")
public class SubsSpecificPayOptionSavedCardFilter extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    @Override
    protected GenericCoreResponseBean<Boolean> doBizExecute(WorkFlowRequestBean input,
            WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        if (StringUtils.isNotBlank(input.getToken())
                || (input.isSuperGwFpoApiHit() && input.getUserDetailsBiz() != null)) {
            workFlowHelper.filterOperationsLitePayView(transBean, true);
            workFlowHelper.filterSavedCardsForSubscription(transBean);
        } else {
            workFlowHelper.filterOperationsForUserNotLoggedIn(transBean, true);
            workFlowHelper.filterSavedCardsForSubscriptionNotLoggedIn(transBean);
        }
        return new GenericCoreResponseBean<>(true);
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.SUBSCRIPTION_SPECIFIC_PAYOPTION_SAVEDCARD_FILTER;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return true;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.FILTER_SAVED_CARDS_TIME, "30"));

    }

    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        return true;
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setMerchnatViewResponse(transBean.getMerchantViewConsult());
        response.setMerchnatLiteViewResponse(transBean.getMerchantLiteViewConsult());
        response.setUserDetails(transBean.getUserDetails());
        response.setmIdCustIdCardBizDetails(transBean.getMidCustIdCardBizDetails());
    }

}
