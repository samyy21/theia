package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.enums.LitePayviewConsultType;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by charu on 24/03/20.
 */

@Service("filterPlatformSavedAssetsForSubscriptionTask")
public class FilterPlatformSavedAssetsForSubscriptionTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    @Autowired
    FF4JUtil ff4JUtil;

    @Override
    protected GenericCoreResponseBean<Boolean> doBizExecute(WorkFlowRequestBean input,
            WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {

        /**
         * Merchant LitePayView in subscription flow will be filtered in theia
         * if DRM on respective mid is disabled on Platform.
         */

        if (ff4JUtil.blockingFilterPlatformSavedAssetsForSubs(input.getPaytmMID())
                && response.getMerchnatLiteViewResponse() != null
                && CollectionUtils.isNotEmpty(response.getMerchnatLiteViewResponse().getPayMethodViews())) {
            // filter merchant saved assets
            workFlowHelper.filterPlatformSavedAssetsForSubs(response.getMerchnatLiteViewResponse(),
                    input.getSubsPayMode(), LitePayviewConsultType.MerchantLitePayViewConsult,
                    input.isFromAoaMerchant());
        }

        /**
         * AddnPay LitePayView filtering will take place in theia itself.
         */

        if (response.getAddAndPayLiteViewResponse() != null
                && CollectionUtils.isNotEmpty(response.getAddAndPayLiteViewResponse().getPayMethodViews())) {
            // filter add and pay saved assets
            workFlowHelper
                    .filterPlatformSavedAssetsForSubs(response.getAddAndPayLiteViewResponse(), input.getSubsPayMode(),
                            LitePayviewConsultType.AddnPayLitePayViewConsult, input.isFromAoaMerchant());
        }
        return new GenericCoreResponseBean<>(true);
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FILTER_PLATFORM_SAVED_ASSETS_FOR_SUBSCRIPTION_TASK;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return true;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(
                BizConstant.FILTER_PLATFROM_SAVED_ASSETS_SUBSCRIPTION_EXECUTION_TIME, "30"));
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        String userId = null != transBean.getUserDetails() ? transBean.getUserDetails().getUserId() : null != transBean
                .getWorkFlowBean().getUserDetailsBiz() ? transBean.getWorkFlowBean().getUserDetailsBiz().getUserId()
                : null;
        return ff4JUtil.fetchSavedCardFromPlatform(input, userId);
    }
}
