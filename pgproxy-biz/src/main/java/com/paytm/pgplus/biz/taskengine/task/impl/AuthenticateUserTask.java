package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
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

@Service("authUserTask")
public class AuthenticateUserTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    @Override
    public GenericCoreResponseBean<UserDetailsBiz> doBizExecute(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean workFlowResponseBean) {
        GenericCoreResponseBean<UserDetailsBiz> userDetails = workFlowHelper.fetchUserDetailsNoSavedCards(
                workFlowTransactionBean, workFlowRequestBean.getToken());
        if (userDetails.isSuccessfullyProcessed()) {
            workFlowTransactionBean.setUserDetails(userDetails.getResponse());
        }
        return userDetails;
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.AUTHENTICATE_USER;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return StringUtils.isNotBlank(inputBean.getToken());
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.AUTHENTICATE_USER_TIME, "2000"));
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        // not logged in flow doesn't require any ouath validation
        boolean isExecutable = StringUtils.isNotBlank(input.getToken());
        if (input.getUserDetailsBiz() != null) {
            if (StringUtils.isBlank(input.getUserDetailsBiz().getUserToken())
                    && StringUtils.isNotBlank(input.getToken())) {
                input.getUserDetailsBiz().setUserToken(input.getToken());
            }
            transBean.setUserDetails(input.getUserDetailsBiz());
            isExecutable = false;
        }
        return isExecutable;

    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setUserDetails(transBean.getUserDetails());
    }

}
