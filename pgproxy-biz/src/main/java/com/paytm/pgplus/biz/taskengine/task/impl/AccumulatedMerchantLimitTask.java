package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.facade.common.model.Money;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.merchantlimit.models.response.LimitAccumulationQueryResponse;
import com.paytm.pgplus.facade.merchantlimit.models.response.MerchantLimitInfo;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("accumulatedMerchantLimitTask")
public class AccumulatedMerchantLimitTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccumulatedMerchantLimitTask.class);

    @Override
    protected GenericCoreResponseBean<Boolean> doBizExecute(WorkFlowRequestBean input,
            WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        try {
            GenericCoreResponseBean<LimitAccumulationQueryResponse> limitQueryResponse = workFlowHelper
                    .getMerchantAccumulatedLimit(input.getAlipayMID());
            if (limitQueryResponse.getResponse() != null) {
                transBean.setLimitAccumulateVos(limitQueryResponse.getResponse().getBody().getLimitAccumulateVos());
                return new GenericCoreResponseBean<>(Boolean.TRUE);
            }
            return new GenericCoreResponseBean<>(Boolean.FALSE);

        } catch (FacadeCheckedException e) {
            LOGGER.error("Unable to get data from merchantLimit API : ", e);
            return new GenericCoreResponseBean<>(Boolean.FALSE);
        }
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.MERCHANT_ACCUMULATED_LIMIT_TASK;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.MERCHANT_ACCUMULATED_LIMIT_TIME, "90"));
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        if (Routes.PG2.equals(input.getRoute()))
            return false;
        return fetchAccumulatedLimitRequired(transBean.getMerchantLimitInfos());
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setLimitAccumulateVos(transBean.getLimitAccumulateVos());
    }

    private boolean fetchAccumulatedLimitRequired(List<MerchantLimitInfo> merchantLimitInfos) {
        if (CollectionUtils.isEmpty(merchantLimitInfos)) {
            return false;
        }
        for (MerchantLimitInfo merchantLimitInfo : merchantLimitInfos) {
            Money limitAmount = merchantLimitInfo.getAmountThreshold();

            if (!(limitAmount == null || StringUtils.equals("-1", limitAmount.getAmount()))) {
                return true;
            }
        }
        return false;
    }
}
