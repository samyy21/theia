package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

import static com.paytm.pgplus.facade.constants.FacadeConstants.AUTH_USER_TYPE_POSTPAID_USER;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.IS_POSTPAID_ONBOARDING_FEATURE_ENABLED;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.POSTPAID_STATUS;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.POSTPAID_STATUS_LIVE;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.POSTPAID_STATUS_WHITELISTED;

@Service("authUserTypeAttributeTask")
public class AuthenticateUserTypeAttributeTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    public static final Logger LOGGER = LoggerFactory.getLogger(AuthenticateUserTypeAttributeTask.class);

    @Override
    public GenericCoreResponseBean<Boolean> doBizExecute(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean workFlowResponseBean) {

        if (null == workFlowTransactionBean.getUserDetails()) {
            LOGGER.error("User details are required to fetch userType Attribute data");
            return new GenericCoreResponseBean<Boolean>(Boolean.FALSE);
        }
        if (workFlowTransactionBean.getUserDetails().isPaytmCCEnabled()) {
            LOGGER.info("Getting CC Enabled flag true hence user is already live so no need to hit user type Attribute Api");
            return new GenericCoreResponseBean<Boolean>(Boolean.TRUE);
        }

        List<String> userTypes = workFlowTransactionBean.getUserDetails().getUserTypes();

        if (!CollectionUtils.isEmpty(userTypes) && userTypes.contains(AUTH_USER_TYPE_POSTPAID_USER)) {
            try {

                Map<String, Map<String, String>> userTypesMap = workFlowHelper.fetchUserTypeAttributesDetails(
                        workFlowRequestBean.getToken(), AUTH_USER_TYPE_POSTPAID_USER,
                        workFlowRequestBean.getOauthClientId(), workFlowRequestBean.getOauthSecretKey());

                String postpaidStatus = getPostpaidStatus(userTypesMap);

                if (StringUtils.isNotBlank(postpaidStatus)) {
                    workFlowTransactionBean.getUserDetails().setPostpaidStatus(postpaidStatus);
                    boolean isPaytmCCEnabled = isPaytmCCEnabled(workFlowRequestBean.isPostpaidOnboardingSupported(),
                            postpaidStatus);
                    if (isPaytmCCEnabled) {
                        workFlowTransactionBean.getUserDetails().setPaytmCCEnabled(true);
                    }
                }
            } catch (FacadeCheckedException e) {
                LOGGER.error("Unable to get data from FetchUserTypeAttribute API : ", e);
                return new GenericCoreResponseBean<Boolean>(Boolean.FALSE);
            }
        }
        return new GenericCoreResponseBean<Boolean>(Boolean.TRUE);

    }

    @Override
    public TaskName getTaskName() {
        return TaskName.AUTH_USER_TYPE_ATTRIBUTE;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.AUTH_USER_TYPE_ATTRIBUTE_TIME, "2000"));
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        if (StringUtils.isBlank(input.getToken())) {
            LOGGER.error("Token is required to run this task");
            return false;
        }
        return true;
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setUserDetails(transBean.getUserDetails());
    }

    private String getPostpaidStatus(Map<String, Map<String, String>> userTypesMap) {
        String postpaidStatus = null;
        if (!CollectionUtils.isEmpty(userTypesMap)
                && !CollectionUtils.isEmpty(userTypesMap.get(AUTH_USER_TYPE_POSTPAID_USER))) {
            postpaidStatus = userTypesMap.get(AUTH_USER_TYPE_POSTPAID_USER).get(POSTPAID_STATUS);
        } else {
            LOGGER.error("Unable to fetch data form fetchUserTypeAttributesDetails map");

        }
        return postpaidStatus;
    }

    private boolean isPaytmCCEnabled(boolean isPostpaidOnboardingSupported, String postpaidStatus) {
        boolean isPostPaidOnboardingFeatureEnabled = isPostPaidOnboardingFeatureEnabled();
        boolean paytmCCEnabled = POSTPAID_STATUS_LIVE.equals(postpaidStatus)
                || (isPostPaidOnboardingFeatureEnabled && isPostpaidOnboardingSupported && POSTPAID_STATUS_WHITELISTED
                        .equals(postpaidStatus));
        LOGGER.info(
                "Postpaid account status from auth for user is : {} and for current workflow isPostpaidOnboardingSupported: {}, isPostPaidOnboardingFeatureEnabled :{}, paytmCCEnabled :{}",
                postpaidStatus, isPostpaidOnboardingSupported, isPostPaidOnboardingFeatureEnabled, paytmCCEnabled);
        return paytmCCEnabled;
    }

    private boolean isPostPaidOnboardingFeatureEnabled() {
        String postPaidOnboardingFeatureEnabled = ConfigurationUtil.getProperty(IS_POSTPAID_ONBOARDING_FEATURE_ENABLED,
                "true");
        return Boolean.valueOf(postPaidOnboardingFeatureEnabled);
    }
}
