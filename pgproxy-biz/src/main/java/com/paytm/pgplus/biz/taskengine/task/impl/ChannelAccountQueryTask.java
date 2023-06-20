package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.core.model.request.ChannelAccountQueryResponseBizBean;
import com.paytm.pgplus.biz.core.payment.utils.LitePayViewEmiUtil;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by charu on 13/06/18.
 */

@Service("channelAccountQueryTask")
public class ChannelAccountQueryTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    @Autowired
    private LitePayViewEmiUtil emiUtil;

    @Override
    protected GenericCoreResponseBean<ChannelAccountQueryResponseBizBean> doBizExecute(WorkFlowRequestBean input,
            WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        GenericCoreResponseBean<ChannelAccountQueryResponseBizBean> channelAccountQueryResponse = workFlowHelper
                .channelAccountQuery(transBean);

        // after the wallet balance is fetched we need calculate emi rates.
        emiUtil.setEmiInfoForLiteConsultPayview(transBean, input.getTxnAmount());

        // setting emi info in litePayview consult as well
        transBean.getMerchantLiteViewConsult()
                .setPayMethodViews(transBean.getMerchantViewConsult().getPayMethodViews());

        return channelAccountQueryResponse;
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.CHANNEL_ACCOUNT_QUERY_TASK;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.parseInt(ConfigurationUtil.getProperty(BizConstant.CHANNEL_ACCOUNT_QUERY_TIME, "40"));
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        // in case of not logged in flow this task is not required
        return StringUtils.isNotBlank(input.getToken());
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setMerchnatViewResponse(transBean.getMerchantViewConsult());
        response.setMerchnatLiteViewResponse(transBean.getMerchantLiteViewConsult());
    }
}
