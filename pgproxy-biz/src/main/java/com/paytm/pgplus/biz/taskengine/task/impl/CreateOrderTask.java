package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.BizCreateOrderResponse;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("createOrderTask")
public class CreateOrderTask extends AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateOrderTask.class);

    @Autowired
    @Qualifier("workFlowHelper")
    protected WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Override
    public GenericCoreResponseBean<BizCreateOrderResponse> doBizExecute(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean workFlowResponseBean) {
        if (workFlowRequestBean.isEnhancedCashierPageRequest()) {
            workFlowTransactionBean.setPostConvenienceFeeModel(workFlowRequestBean.isPostConvenience());
        }
        workFlowHelper.setLinkPaymentsUserDetails(workFlowTransactionBean);
        GenericCoreResponseBean<BizCreateOrderResponse> createOrderResponse = workFlowHelper
                .createOrder(workFlowTransactionBean);
        if (createOrderResponse.isSuccessfullyProcessed()) {
            workFlowRequestBean.setTransID(createOrderResponse.getResponse().getTransId());
            workFlowTransactionBean.setTransID(createOrderResponse.getResponse().getTransId());
            if (workFlowRequestBean.isEnhancedCashierPageRequest()) {
                setAcquirementIdInCache(workFlowRequestBean.getTxnToken(), workFlowTransactionBean.getTransID());
            }
            // for zero subscription flow - to get dummy mid during close order
            // in global exceptional handler
            if (ERequestType.SUBSCRIBE == workFlowRequestBean.getRequestType()
                    || ERequestType.NATIVE_SUBSCRIPTION == workFlowRequestBean.getRequestType()) {
                if (StringUtils.isNotBlank(createOrderResponse.getResponse().getTransId())
                        && StringUtils.isNotBlank(workFlowRequestBean.getPaymentMid())) {
                    theiaSessionRedisUtil.hset(createOrderResponse.getResponse().getTransId(),
                            com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.PAYMENT_MID,
                            workFlowRequestBean.getPaymentMid(), 900);
                }
            }
        }
        return createOrderResponse;
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.CREATE_ORDER;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return true;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.CREATE_ORDER_TIME, "90"));
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setTransID(transBean.getTransID());

    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        if (input.isEnhancedCashierPageRequest()) {
            return input.isCreateOrderRequired();

        }
        return true;
    }

    private void setAcquirementIdInCache(String txnToken, String acqId) {
        if (StringUtils.isNotBlank(txnToken) && StringUtils.isNotBlank(acqId)) {
            LOGGER.info("Setting acquirement ID in cache : {}", acqId);
            theiaSessionRedisUtil.hsetIfExist(txnToken, "transactionId", acqId);
        }
    }
}
