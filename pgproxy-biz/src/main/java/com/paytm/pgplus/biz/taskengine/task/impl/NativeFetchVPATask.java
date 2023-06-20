package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service("nativeFetchVPATask")
public class NativeFetchVPATask extends FetchVPATask {

    @Autowired
    Ff4jUtils ff4jUtils;

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {

        String userId = null != transBean.getUserDetails() ? transBean.getUserDetails().getUserId() : null != input
                .getUserDetailsBiz() ? input.getUserDetailsBiz().getUserId() : null;
        return (StringUtils.isNotBlank(input.getToken()) || input.getUserDetailsBiz() != null)
                && !isVersionAllowed(input)
                && !(input.isInternalFetchPaymentOptions() && ff4jUtils.featureEnabledOnMultipleKeys(
                        input.getPaytmMID(), userId,
                        BizConstant.Ff4jFeature.BLACKLIST_INTERNAL_FETCH_UPI_PROFILE_V1_PTC, false));
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.NATIVE_FETCH_VPA;
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setSarvatraUserProfile(transBean.getSarvatraUserProfile());
        response.setSarvatraVpa(transBean.getSarvatraVpa());
    }

    private boolean isVersionAllowed(WorkFlowRequestBean input) {
        String allowedVersions[] = ConfigurationUtil
                .getTheiaProperty(TheiaConstant.ExtraConstants.FPO_ALLOWED_VERSIONS).split(",");
        return Arrays.stream(allowedVersions).anyMatch(n -> StringUtils.equalsIgnoreCase(input.getApiVersion(), n));
    }

}
