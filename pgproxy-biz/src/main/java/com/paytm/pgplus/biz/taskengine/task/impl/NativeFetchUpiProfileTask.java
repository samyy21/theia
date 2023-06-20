package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.utils.NpciHealthUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatraV4;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @author mohit.gupta
 */

@Service("nativeFetchUpiProfile")
public class NativeFetchUpiProfileTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeFetchUpiProfileTask.class);

    @Autowired
    NpciHealthUtil npciHealthUtil;

    @Autowired
    Ff4jUtils ff4jUtils;

    @Override
    public GenericCoreResponseBean<?> doBizExecute(WorkFlowRequestBean input, WorkFlowTransactionBean transBean,
            WorkFlowResponseBean response) {

        if (StringUtils.isNotBlank(input.getToken()) || input.getUserDetailsBiz() != null) {
            return workFlowHelper.fetchAndSetUpiProfile(input, transBean);
        } else if (StringUtils.isBlank(input.getToken())) {
            return workFlowHelper.createAndSetUpiProfileWithOnlyNpciHealth(transBean);
        }

        return new GenericCoreResponseBean<>(Boolean.FALSE);
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FETCH_UPI_PROFILE;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        String userId = null != transBean.getUserDetails() ? transBean.getUserDetails().getUserId() : null != input
                .getUserDetailsBiz() ? input.getUserDetailsBiz().getUserId() : null;
        String allowedVersions[] = ConfigurationUtil
                .getTheiaProperty(TheiaConstant.ExtraConstants.FPO_ALLOWED_VERSIONS).split(",");
        return Arrays.stream(allowedVersions).anyMatch(n -> StringUtils.equalsIgnoreCase(input.getApiVersion(), n))
                && !(StringUtils.isNotBlank(input.getAccessToken()) && ff4jUtils.featureEnabledOnMultipleKeys(
                        input.getPaytmMID(), userId,
                        BizConstant.Ff4jFeature.BLACKLIST_INTERNAL_FETCH_UPI_PROFILE_V2_FPO, false));
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.FETCH_UPI_PROFILE, "1000"));
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        if (transBean.getSarvatraUserProfileV4() == null) {
            LOGGER.error("Fetching userProfile failed , setting only npci health in response");
            transBean.setSarvatraUserProfileV4(new UserProfileSarvatraV4(npciHealthUtil.getNpciHealthViaCache()));
        }
        response.setUpiProfileV4(transBean.getSarvatraUserProfileV4());
    }

}
