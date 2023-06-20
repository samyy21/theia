package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.core.user.service.ISarvatraUserProfile;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.facade.user.models.request.FetchUserPaytmVpaRequest;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Created by charu on 10/03/19.
 */

/**
 * fetch Vpa task already exist that executes on basis of merchant configuration
 * this task will directly fetch vpa from upi switch without checking any
 * merchant configuration
 */

@Service("fetchPaytmVPATask")
public class FetchPaytmVPATask extends AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    @Autowired
    @Qualifier("userProfileSarvatra")
    private ISarvatraUserProfile sarvatraVpaDetails;

    @Override
    public GenericCoreResponseBean<?> doBizExecute(WorkFlowRequestBean input, WorkFlowTransactionBean transBean,
            WorkFlowResponseBean response) {
        FetchUserPaytmVpaRequest fetchUserPaytmVpaRequest = new FetchUserPaytmVpaRequest(input.getToken());
        if (transBean.getUserDetails() != null) {
            fetchUserPaytmVpaRequest.setUserId(transBean.getUserDetails().getUserId());
            fetchUserPaytmVpaRequest.setQueryParams(input.getQueryParams());
            fetchUserPaytmVpaRequest.setFetchLRNDetails(String.valueOf(input.isUpiLite()));
        }
        GenericCoreResponseBean<UserProfileSarvatra> paytmVpa = sarvatraVpaDetails
                .fetchUserProfileVpa(fetchUserPaytmVpaRequest);
        if (paytmVpa.isSuccessfullyProcessed() && null != paytmVpa.getResponse()) {
            transBean.setSarvatraUserProfile(paytmVpa.getResponse());
            transBean.setSarvatraVpa(workFlowHelper.getSarvatraVPAList(paytmVpa.getResponse().getResponse()));
        }
        return paytmVpa;
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FETCH_PAYTM_VPA;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        return StringUtils.isNotBlank(input.getToken());
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.FETCH_VPA_TIME, "500"));
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setSarvatraUserProfile(transBean.getSarvatraUserProfile());
        response.setSarvatraVpa(transBean.getSarvatraVpa());
    }
}
