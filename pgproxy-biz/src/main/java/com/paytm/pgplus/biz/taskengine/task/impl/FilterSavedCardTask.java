package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Task will filter save cards of user on the basis merchant consult
 *
 * Created by charu on 06/08/18.
 */

@Service("filterSavedCardTask")
public class FilterSavedCardTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    @Autowired
    FF4JUtil ff4JUtil;

    @Autowired
    Ff4jUtils ff4jUtils;

    @Override
    protected GenericCoreResponseBean<Boolean> doBizExecute(WorkFlowRequestBean input,
            WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        // check for not logged in flow
        if (transBean.getUserDetails() == null) {
            // filter saved cards on the basis of merchant consult
            workFlowHelper.filterSavedCardsUserNotLogged(transBean);
        } else {
            // filter saved cards on the basis of merchant consult
            workFlowHelper.filterSavedCards(transBean);
        }
        return new GenericCoreResponseBean<>(true);
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FILTER_SAVED_CARD;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.parseInt(ConfigurationUtil.getProperty(BizConstant.FILTER_SAVED_CARDS_TIME, "10"));
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setUserDetails(transBean.getUserDetails());
        response.setmIdCustIdCardBizDetails(transBean.getMidCustIdCardBizDetails());
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        if (ERequestType.RESELLER.name().equals(input.getRequestType().name())) {
            return false;
        }
        if (CollectionUtils.isNotEmpty(input.getDisabledPaymentModes())
                && input.getDisabledPaymentModes().contains(EPayMethod.SAVED_CARD.toString())) {
            return false;
        }
        String userId = null != transBean.getUserDetails() ? transBean.getUserDetails().getUserId() : null != input
                .getUserDetailsBiz() ? input.getUserDetailsBiz().getUserId() : null;
        return !(StringUtils.isNotBlank(input.getAccessToken()) && ff4jUtils.featureEnabledOnMultipleKeys(
                input.getPaytmMID(), userId, BizConstant.Ff4jFeature.BLACKLIST_SAVED_CARDS_IN_FPO_V2_WITH_ACCESS_TOKEN,
                false)) && ff4JUtil.fetchSavedCardFromService(userId, input.getPaytmMID(), input.getCustID());
    }
}
