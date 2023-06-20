package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.core.model.request.CreateTopUpResponseBizBean;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("createTopupTask")
public class CreateTopupTask extends AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateTopupTask.class);

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Override
    public GenericCoreResponseBean<CreateTopUpResponseBizBean> doBizExecute(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean workFlowResponseBean) {
        if (workFlowTransactionBean.getUserDetails() != null) {
            workFlowTransactionBean.getWorkFlowBean().getExtendInfo()
                    .setClientId(workFlowTransactionBean.getUserDetails().getClientId());
        }
        final GenericCoreResponseBean<CreateTopUpResponseBizBean> createTopUpResponse = workFlowHelper.createTopUp(
                workFlowTransactionBean, false);
        if (createTopUpResponse.isSuccessfullyProcessed()) {
            workFlowTransactionBean.setTransID(createTopUpResponse.getResponse().getFundOrderId());
            if (workFlowRequestBean.isEnhancedCashierPageRequest()) {
                setFundOrderIdInCache(workFlowRequestBean.getTxnToken(), workFlowTransactionBean.getTransID());
            }
        }
        return createTopUpResponse;
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.CREATE_TOPUP;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return true;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.CREATE_TOPUP_TIME, "90"));
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setTransID(transBean.getTransID());

    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        if (getFundOrderIdFromCache(input.getTxnToken(), transBean)) {
            return false;
        }
        if (input.isEnhancedCashierPageRequest()) {
            return input.isCreateTopupRequired();

        }
        return true;
    }

    private void setFundOrderIdInCache(String txnToken, String transId) {
        if (StringUtils.isNotBlank(txnToken) && StringUtils.isNotBlank(transId)) {
            LOGGER.info("Setting txn ID in cache : {}", transId);
            theiaSessionRedisUtil.hsetIfExist(txnToken, "transactionId", transId);
        }
    }

    private boolean getFundOrderIdFromCache(String txnToken, WorkFlowTransactionBean workFlowTransactionBean) {
        String fundOrderId = (String) theiaSessionRedisUtil.hget(txnToken, "transactionId");
        if (StringUtils.isNotBlank(fundOrderId)) {
            workFlowTransactionBean.setTransID(fundOrderId);
            return true;
        }
        return false;
    }
}
