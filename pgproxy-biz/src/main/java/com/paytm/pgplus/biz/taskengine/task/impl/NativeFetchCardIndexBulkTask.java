package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.fetchBulkCardIndexNumber.INativeFetchSavedCardIndexNumberService;
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

@Service("nativeFetchCardIndexBulkTask")
public class NativeFetchCardIndexBulkTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    @Autowired
    INativeFetchSavedCardIndexNumberService nativeFetchSavedCardIndexNumberService;

    @Autowired
    FF4JUtil ff4JUtil;

    @Override
    protected GenericCoreResponseBean<Boolean> doBizExecute(WorkFlowRequestBean input,
            WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        if (transBean.getUserDetails() == null) {
            // filter saved cards on the basis of merchant consult
            return nativeFetchSavedCardIndexNumberService.fetchCardIndexNotLoggedIn(input, transBean, response);
        } else {
            return nativeFetchSavedCardIndexNumberService.fetchCardIndexLoggedIn(input, transBean, response);
        }
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FETCH_EMI_SUBVENTION_BULK_CARD_INDEX_TASK;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        // to be discussed
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.EMI_SUBVENTION_BULK_FETCH_CARD_INDEX, "300"));
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        boolean isSavedCardPresent = false;

        if (transBean.getMidCustIdCardBizDetails() != null
                && !CollectionUtils.isEmpty(transBean.getMidCustIdCardBizDetails().getMerchantCustomerCardList())
                || transBean.getUserDetails() != null
                && transBean.getUserDetails().getMerchantViewSavedCardsList() != null)
            isSavedCardPresent = true;

        String userId = null != transBean.getUserDetails() ? transBean.getUserDetails().getUserId() : null != input
                .getUserDetailsBiz() ? input.getUserDetailsBiz().getUserId() : null;

        if (input.isEmiSubventionRequired() && input.getItems() != null
                && input.getEmiSubventedTransactionAmount() != null && isSavedCardPresent
                && ff4JUtil.fetchSavedCardFromService(userId, input.getPaytmMID(), input.getCustID())) {
            return true;
        }
        return false;
    }
}
