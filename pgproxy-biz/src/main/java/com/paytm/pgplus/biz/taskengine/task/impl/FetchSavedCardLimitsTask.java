package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.helper.SavedCardLimitWorkFlowHelper;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_PG2_CARD_LIMIT;

@Service("fetchSavedCardLimitsTask")
public class FetchSavedCardLimitsTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    public static final Logger LOGGER = LoggerFactory.getLogger(FetchSavedCardLimitsTask.class);
    public static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(FetchSavedCardLimitsTask.class);

    @Autowired
    @Qualifier("savedCardLimitWorkFlowHelper")
    private SavedCardLimitWorkFlowHelper savedCardLimitWorkFlowHelper;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Override
    public GenericCoreResponseBean<Boolean> doBizExecute(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean workFlowResponseBean) {
        Boolean result = savedCardLimitWorkFlowHelper.fetchLimitsForSavedCards(workFlowTransactionBean);
        if (result.equals(Boolean.FALSE)) {
            LOGGER.info("Could not Apply Limits on Saved Cards :");
        }
        EXT_LOGGER.info("WorkflowResponse Bean after saved card limit application: {}", workFlowResponseBean);
        return new GenericCoreResponseBean<>(result);
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FETCH_SAVED_CARD_LIMIT_TASK;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.FETCH_SAVED_CARD_LIMIT_TIME, "2000"));
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        boolean isPG2CardLimitEnable = ff4jUtils.isFeatureEnabledOnMid(input.getPaytmMID(), ENABLE_PG2_CARD_LIMIT,
                false);
        if (input.getExtendInfo() != null && isPG2CardLimitEnable) {
            Routes lpvRoute = input.getExtendInfo().getLpvRoute();
            // Fetch saved card limits for both logged-in user and non-logged-in
            // user
            if (Routes.PG2.equals(lpvRoute) && transBean.getMerchantLiteViewConsult() != null
                    && transBean.getMerchantLiteViewConsult().getPayMethodViews() != null) {
                EXT_LOGGER.info("Task will execute for Remaining Limits of saved card");
                return true;
            }
        }
        return false;
    }

    // @Override
    // protected void doBizPostProcess(WorkFlowTransactionBean transBean,
    // WorkFlowResponseBean response) {
    // response.setUserDetails(transBean.getUserDetails());
    // response.setmIdCustIdCardBizDetails(transBean.getMidCustIdCardBizDetails());
    // }
}
