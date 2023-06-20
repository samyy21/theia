package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WalletLimits;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Created by charu on 11/06/18.
 */

@Service("payModeDecisionMakerTask")
public class PayModeDecisionMakerTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    @Override
    protected GenericCoreResponseBean<EPayMode> doBizExecute(WorkFlowRequestBean input,
            WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {

        EPayMode payMode;
        if (StringUtils.isBlank(input.getTxnAmount())) {
            input.setTxnAmount(input.getAmountForPaymentFlow());
        }

        if (StringUtils.isNotBlank(input.getTxnAmount())) {
            payMode = workFlowHelper.allowedPayMode(transBean);
        } else {
            payMode = workFlowHelper.allowedPayModeForNoTxnAmt(transBean);
        }

        // for subscription flow
        if (ERequestType.isSubscriptionRecurringRequest(input.getRequestType())) {
            if (EPayMode.HYBRID.equals(payMode)) {
                payMode = EPayMode.NONE;
            }
            if (input.isOverrideAddNPayBehaviourEnabled() && EPayMode.ADDANDPAY.equals(payMode)) {
                payMode = EPayMode.NONE;
            }
        }

        transBean.setAllowedPayMode(payMode);
        // LIMIT_REJECT EPayMode is returned when isLimitApplicable is true from
        // wallet consult response
        // in this case we are giving failure response, changes done as part of
        // PGP-10890
        response.setAddMoneyDestination(transBean.getAddMoneyDestination());

        if (EPayMode.LIMIT_REJECT.equals(payMode)) {

            if (ObjectUtils.notEqual(transBean.getWalletLimits(), null)
                    && "RWL_1004".equals(transBean.getWalletLimits().getLimitMessage())) {
                response.setWalletLimits(transBean.getWalletLimits());
                return new GenericCoreResponseBean<>(payMode);
            }

            return new GenericCoreResponseBean<>("Add money not allowed or failed ",
                    ResponseConstants.ADD_MONEY_WALLET_CONSULT_FAILED);
        }
        if (ObjectUtils.notEqual(transBean.getWalletLimits(), null)) {
            response.setWalletLimits(transBean.getWalletLimits());
        }
        return new GenericCoreResponseBean<>(payMode);
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.PAYMODE_DECISION_MAKER_TASK;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        // returning true because in case of wallet limit we are failing
        // transaction
        if (EPayMode.LIMIT_REJECT.equals(transBean.getAllowedPayMode())) {
            return true;
        }
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.parseInt(ConfigurationUtil.getProperty(BizConstant.PAYMODE_DECISION_MAKER_TIME, "20"));
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        if (StringUtils.isNotBlank(transBean.getWorkFlowBean().getToken())
                || transBean.getWorkFlowBean().getUserDetailsBiz() != null) {
            response.setAllowedPayMode(transBean.getAllowedPayMode());
            response.setOnTheFlyKYCRequired(transBean.isOnTheFlyKYCRequired());
        }
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        return (StringUtils.isNotBlank(input.getToken()) || input.getUserDetailsBiz() != null);
    }
}
