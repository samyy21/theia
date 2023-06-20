package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.logging.ExtendedLogger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service("fetchPostpaidBalanceTask")
public class FetchPostpaidBalanceTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(FetchPostpaidBalanceTask.class);

    @Override
    public GenericCoreResponseBean<Boolean> doBizExecute(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean workFlowResponseBean) {
        return workFlowHelper.fetchPostpaidBalanceResponse(workFlowTransactionBean, workFlowResponseBean);
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FETCH_POSTPAID;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.FETCH_POSTPAID_BALANCE, "90"));
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        // not logged in flow doesn't require any ouath validation

        // check for offline workflow
        if (input.isOfflineFetchPayApi())
            return StringUtils.isNotBlank(input.getToken());

        // currently fetchPaytmInstrumentsBalance is sent in
        // fetchQRpaymentOptionsapi. check for sending balance and if user has
        // postpaid enabled.
        return input.getUserDetailsBiz() != null
                && (input.isFetchPaytmInstrumentsBalance() || input.isFetchPostpaidBalance())
                && input.getUserDetailsBiz().isPaytmCCEnabled();
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean workFlowTransBean, WorkFlowResponseBean response) {
        // For Add Money Flows - Allow postpaid only in cases when the TXN
        // AMOUNT is
        // between the MIN & MAX configured amounts.
        boolean isPaytmAddMoneyMid = (StringUtils.equals(workFlowTransBean.getWorkFlowBean().getPaytmMID(),
                ConfigurationUtil.getTheiaProperty(BizConstant.MP_ADD_MONEY_MID)));
        if ((isPaytmAddMoneyMid || workFlowTransBean.getWorkFlowBean().isNativeAddMoney())) {
            String txnAmount = StringUtils.isNotBlank(workFlowTransBean.getWorkFlowBean().getTxnAmount()) ? workFlowTransBean
                    .getWorkFlowBean().getTxnAmount() : workFlowTransBean.getWorkFlowBean()
                    .getAmountForWalletConsultInRisk();
            EXT_LOGGER
                    .customInfo(
                            "Amount range check for Add Money via Postpaid :: isPaytmAddMoneyMid : {}, txnAmount in Paise : {}",
                            isPaytmAddMoneyMid, txnAmount);
            if (StringUtils.isNotBlank(txnAmount)) {
                Double minBalance = Double.valueOf(ConfigurationUtil.getTheiaProperty(
                        BizConstant.MIN_POSTPAID_BALANCE_AMOUNT, "2500"));
                Double maxBalance = Double.valueOf(ConfigurationUtil.getTheiaProperty(
                        BizConstant.MAX_POSTPAID_BALANCE_AMOUNT, "10000"));
                if (BooleanUtils
                        .isNotTrue(Double.valueOf(AmountUtils.getTransactionAmountInRupee(txnAmount)) > minBalance
                                && Double.valueOf(AmountUtils.getTransactionAmountInRupee(txnAmount)) < maxBalance)) {
                    disablePostpaidPayMethod(workFlowTransBean.getMerchantLiteViewConsult());
                }
            }
        }
    }

    private void disablePostpaidPayMethod(LitePayviewConsultResponseBizBean liteViewConsult) {
        if (Objects.nonNull(liteViewConsult) && Objects.nonNull(liteViewConsult.getPayMethodViews())) {
            liteViewConsult
                    .getPayMethodViews()
                    .stream()
                    .filter(payMethodViewsBiz -> EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod().equals(
                            payMethodViewsBiz.getPayMethod()))
                    .findFirst()
                    .ifPresent(
                            payMethodViewsBiz -> {
                                if (CollectionUtils.isNotEmpty(payMethodViewsBiz.getPayChannelOptionViews())
                                        && payMethodViewsBiz.getPayChannelOptionViews().size() == 1) {
                                    payMethodViewsBiz.getPayChannelOptionViews().get(0).setEnableStatus(false);
                                }
                            });

        }
    }
}
