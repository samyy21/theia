package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.merchantlimit.models.response.LimitQueryResponse;
import com.paytm.pgplus.facade.merchantlimit.services.IMerchantLimitService;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.mappingserviceclient.service.impl.MerchantTxnLimitServiceImpl;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("totalMerchantLimitTask")
public class TotalMerchantLimitTask extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TotalMerchantLimitTask.class);

    @Autowired
    @Qualifier(value = "merchantLimitService")
    private IMerchantLimitService merchantLimitService;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Override
    protected GenericCoreResponseBean<Boolean> doBizExecute(WorkFlowRequestBean input,
            WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        try {
            GenericCoreResponseBean<LimitQueryResponse> limitQueryResponse = workFlowHelper.getMerchantTotalLimit(input
                    .getAlipayMID());
            if (limitQueryResponse.getResponse() != null) {
                transBean.setMerchantLimitInfos(limitQueryResponse.getResponse().getBody().getMerchantLimitInfos());
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
        return TaskName.MERCHANT_TOTAL_LIMIT_TASK;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.MERCHANT_TOTAL_LIMIT_TIME, "90"));
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        if (Routes.PG2.equals(input.getRoute()))
            return false;
        String userId = null != transBean.getUserDetails() ? transBean.getUserDetails().getUserId() : null != input
                .getUserDetailsBiz() ? input.getUserDetailsBiz().getUserId() : null;
        return !input.isFullPg2TrafficEnabled()
                && !(StringUtils.isNotBlank(input.getAccessToken()) && ff4jUtils.featureEnabledOnMultipleKeys(
                        input.getPaytmMID(), userId,
                        BizConstant.Ff4jFeature.BLACKLIST_SAVED_CARDS_IN_FPO_V2_WITH_ACCESS_TOKEN, false));
    }

    @Override
    protected void doBizPostProcess(WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        response.setMerchantLimitInfos(transBean.getMerchantLimitInfos());
    }
}
