package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.MidCustIdCardBizDetails;
import com.paytm.pgplus.biz.core.user.service.ISavedCards;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
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
 * Created by charu on 07/06/18.
 */
@Service("fetchSavedCardTask")
public class FetchSavedCardTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    @Autowired
    @Qualifier("savedCards")
    private ISavedCards savedCards;

    @Override
    public GenericCoreResponseBean<List<CardBeanBiz>> doBizExecute(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean workFlowResponseBean) {
        GenericCoreResponseBean<List<CardBeanBiz>> savedCardsList = null;
        MidCustIdCardBizDetails midCustIdCardBizDetails = null;
        if (workFlowRequestBean.isStoreCardPrefEnabled() && StringUtils.isNotBlank(workFlowRequestBean.getCustID())) {
            String userId = workFlowTransactionBean.getUserDetails() != null ? workFlowTransactionBean.getUserDetails()
                    .getUserId() : null;
            savedCardsList = savedCards.fetchSavedCardsByMidCustIdUserId(workFlowRequestBean.getPaytmMID(),
                    workFlowRequestBean.getCustID(), userId);
        } else if (workFlowTransactionBean.getUserDetails() != null) {
            savedCardsList = savedCards.fetchSavedCardsByUserId(workFlowTransactionBean.getUserDetails().getUserId());
        }

        // hack to support not logged in flow and merchant prefernce enabled on
        // merchant but no saved card saved till now
        // did this for issue PGP-8593
        if (workFlowRequestBean.isStoreCardPrefEnabled() && workFlowTransactionBean.getUserDetails() == null) {
            midCustIdCardBizDetails = new MidCustIdCardBizDetails();
            workFlowTransactionBean.setMidCustIdCardBizDetails(midCustIdCardBizDetails);
        }

        if (savedCardsList != null && savedCardsList.isSuccessfullyProcessed()) {
            // check for not logged in flow
            if (workFlowTransactionBean.getUserDetails() == null) {
                midCustIdCardBizDetails = new MidCustIdCardBizDetails();
                midCustIdCardBizDetails.setCustId(workFlowRequestBean.getCustID());
                midCustIdCardBizDetails.setmId(workFlowRequestBean.getPaytmMID());
                midCustIdCardBizDetails.setMerchantCustomerCardList(savedCardsList.getResponse());
                workFlowTransactionBean.setMidCustIdCardBizDetails(midCustIdCardBizDetails);
                // filter saved cards on the basis of merchant consult
                workFlowHelper.filterSavedCardsUserNotLogged(workFlowTransactionBean);
            } else {
                workFlowTransactionBean.getUserDetails().setMerchantViewSavedCardsList(savedCardsList.getResponse());
                // filter saved cards on the basis of merchant consult
                workFlowHelper.filterSavedCards(workFlowTransactionBean);
            }

            // added this to support Subscription flow
            if (ERequestType.SUBSCRIBE.equals(workFlowRequestBean.getRequestType())
                    && StringUtils.isNotBlank(workFlowRequestBean.getToken())) {
                workFlowHelper.filterSavedCardsForSubscription(workFlowTransactionBean);
            }
        }

        return savedCardsList;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.FETCH_SAVED_CARDS_TIME, "20"));
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FETCH_SAVED_CARD;
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
        return true;
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setUserDetails(transBean.getUserDetails());
        response.setmIdCustIdCardBizDetails(transBean.getMidCustIdCardBizDetails());
    }
}
