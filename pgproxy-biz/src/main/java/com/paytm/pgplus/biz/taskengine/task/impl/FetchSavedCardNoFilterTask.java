package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.MidCustIdCardBizDetails;
import com.paytm.pgplus.biz.core.user.service.ISavedCards;
import com.paytm.pgplus.biz.core.validator.BizParamValidator;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Task will fetch saved card only and will not filter the cards
 *
 * Created by charu on 06/08/18.
 */

@Service("fetchSavedCardNoFilterTask")
public class FetchSavedCardNoFilterTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    @Autowired
    @Qualifier("savedCards")
    private ISavedCards savedCards;

    @Autowired
    private FF4JUtil ff4JUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Override
    public GenericCoreResponseBean<List<CardBeanBiz>> doBizExecute(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean workFlowResponseBean) {
        GenericCoreResponseBean<List<CardBeanBiz>> savedCardsList = null;
        if (workFlowRequestBean.isStoreCardPrefEnabled() && StringUtils.isNotBlank(workFlowRequestBean.getCustID())) {
            String userId = workFlowTransactionBean.getUserDetails() != null ? workFlowTransactionBean.getUserDetails()
                    .getUserId() : null;
            savedCardsList = savedCards.fetchSavedCardsByMidCustIdUserId(workFlowRequestBean.getPaytmMID(),
                    workFlowRequestBean.getCustID(), userId);
        } else if (workFlowTransactionBean.getUserDetails() != null) {
            savedCardsList = savedCards.fetchSavedCardsByUserId(workFlowTransactionBean.getUserDetails().getUserId());
        }
        if (savedCardsList != null && savedCardsList.isSuccessfullyProcessed()
                && BizParamValidator.validateInputListParam(savedCardsList.getResponse())) {
            // check for not logged in flow
            if (workFlowTransactionBean.getUserDetails() == null) {
                MidCustIdCardBizDetails midCustIdCardBizDetails = new MidCustIdCardBizDetails();
                midCustIdCardBizDetails.setCustId(workFlowRequestBean.getCustID());
                midCustIdCardBizDetails.setmId(workFlowRequestBean.getPaytmMID());
                midCustIdCardBizDetails.setMerchantCustomerCardList(savedCardsList.getResponse());
                workFlowTransactionBean.setMidCustIdCardBizDetails(midCustIdCardBizDetails);
            } else {
                workFlowTransactionBean.getUserDetails().setMerchantViewSavedCardsList(savedCardsList.getResponse());
            }
        }

        return savedCardsList;
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FETCH_SAVED_CARD_NO_FILTER;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.FETCH_SAVED_CARDS_NO_FILTER_TIME, "20"));
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

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setUserDetails(transBean.getUserDetails());
        response.setmIdCustIdCardBizDetails(transBean.getMidCustIdCardBizDetails());
    }
}
